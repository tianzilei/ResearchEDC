#!/bin/bash
# =============================================================================
# ResearchEDF — Host Machine Deployment (No Docker)
#
# Usage: bash deploy-host.sh <command>
# Commands:
#   build              Build frontend + backend WAR
#   dbinit             Initialize database + schema + admin/admin account
#   start              Start application (tmux, persistent)
#   stop               Stop application
#   restart            Stop then start
#   status             Show service status
#   logs               Tail log files
#   clean              Remove build artifacts
# =============================================================================
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# ---- Configuration ----
DB_HOST="${RESEARCHEDC_DB_HOST:-localhost}"
DB_PORT="${RESEARCHEDC_DB_PORT:-5433}"
DB_NAME="${RESEARCHEDC_DB_NAME:-researchedc}"
DB_USER="${RESEARCHEDC_DB_USER:-researchedc}"
DB_PASS="${RESEARCHEDC_DB_PASS:-researchedc}"

APP_PORT="${RESEARCHEDC_APP_PORT:-8080}"

WAR="${PROJECT_DIR}/app/target/ResearchEDF.war"
DATA_DIR="${PROJECT_DIR}/data"
LOG_DIR="${PROJECT_DIR}/logs"

# ---- Colors ----
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_ok()      { echo -e "${GREEN}[OK]${NC} $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

# ---- Build ----
cmd_build() {
    log_info "Building frontend..."
    cd "${PROJECT_DIR}/frontend"
    pnpm install --frozen-lockfile || pnpm install
    pnpm build
    cd "${PROJECT_DIR}"

    log_info "Copying frontend to app resources..."
    mkdir -p app/src/main/resources/static
    cp -r frontend/dist/* app/src/main/resources/static/

    log_info "Building backend WAR..."
    mvn clean package -DskipTests -pl app -am

    if [ -f "${WAR}" ]; then
        log_ok "Build complete: $(ls -lh "${WAR}" | awk '{print $5}')"
    else
        log_error "WAR not found at ${WAR}"
        exit 1
    fi
}

# ---- Database Init ----
cmd_init() {
    log_info "Checking PostgreSQL..."
    pg_isready -h "${DB_HOST}" -p "${DB_PORT}" &>/dev/null || {
        log_error "PostgreSQL not running on ${DB_HOST}:${DB_PORT}"
        log_info "Start: sudo systemctl start postgresql"
        exit 1
    }

    # ---- Database reset (requires sudo for full drop/recreate) ----
    log_info "Requesting sudo access for PostgreSQL admin operations..."
    if sudo -v 2>/dev/null; then
        sudo -u postgres psql -tc "SELECT 1 FROM pg_roles WHERE rolname='${DB_USER}'" | grep -q 1 \
            || sudo -u postgres psql -c "CREATE USER ${DB_USER} WITH PASSWORD '${DB_PASS}';"

        log_info "Dropping database '${DB_NAME}' if exists..."
        sudo -u postgres psql -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='${DB_NAME}' AND pid <> pg_backend_pid();" 2>/dev/null || true
        sudo -u postgres psql -c "DROP DATABASE IF EXISTS \"${DB_NAME}\";"
        sudo -u postgres psql -c "CREATE DATABASE \"${DB_NAME}\" OWNER ${DB_USER};"
        sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE \"${DB_NAME}\" TO ${DB_USER};"
        log_ok "Database recreated from scratch"
    else
        log_warn "sudo password prompt failed — skipping database drop/recreate"
        log_info "  Ensure database '${DB_NAME}' exists manually if needed."
    fi

    log_info "Running Liquibase migrations (via liquibase-core directly)..."
    cd "${PROJECT_DIR}"
    # Compile shared module classes first (needed for classpath)
    mvn compile -pl shared -DskipTests -q 2>/dev/null || log_warn "Maven compile failed"
    local lb_classpath
    lb_classpath=$(mvn dependency:build-classpath -pl shared -DincludeScope=runtime -q -Dmdep.outputFile=/dev/stdout 2>/dev/null):shared/target/classes
    # Drop all existing objects then apply migrations
    java -cp "${lb_classpath}" liquibase.integration.commandline.Main \
        --changeLogFile=migration/master.xml \
        --url="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}" \
        --username="${DB_USER}" \
        --password="${DB_PASS}" \
        --classpath=shared/target/classes \
        dropAll 2>&1 | grep -v "^##\|^$" || true
    java -cp "${lb_classpath}" liquibase.integration.commandline.Main \
        --changeLogFile=migration/master.xml \
        --url="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}" \
        --username="${DB_USER}" \
        --password="${DB_PASS}" \
        --classpath=shared/target/classes \
        update 2>&1 | grep -v "^##\|^$" || true

    log_info "Creating admin/admin account..."
    local bcrypt_hash
    bcrypt_hash=$(python3 -c "
import bcrypt
h = bcrypt.hashpw(b'admin', bcrypt.gensalt(rounds=10)).decode()
print('{bcrypt}' + h)
")

    if [ -n "${bcrypt_hash}" ]; then
        result=$(PGPASSWORD="${DB_PASS}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "
INSERT INTO user_account (user_name, passwd, first_name, last_name, email, status_id, owner_id, date_created, enabled, account_non_locked)
SELECT 'admin', '${bcrypt_hash}', '管理', '员', 'admin@example.com', 1, 1, CURRENT_DATE, true, true
WHERE NOT EXISTS (SELECT 1 FROM user_account WHERE user_name = 'admin');
INSERT INTO study_user_role (user_name, role_name, study_id, status_id, owner_id, date_created)
SELECT 'admin', 'Study_Director', 1, 1, 1, CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM study_user_role WHERE user_name = 'admin' AND role_name = 'Study_Director');
" 2>&1)
        echo "${result}"
        if echo "${result}" | grep -q "INSERT 0 1"; then
            log_ok "Admin account created (admin / admin)"
        else
            log_warn "Admin account may already exist — continuing"
        fi
    else
        log_error "Failed to generate password hash — is python3-bcrypt installed?"
        exit 1
    fi

    log_ok "Database initialization complete"
}

# ---- Start ----
cmd_start() {
    if [ ! -f "${WAR}" ]; then
        log_error "WAR not found. Run 'bash deploy-host.sh build' first."
        exit 1
    fi

    mkdir -p "${DATA_DIR}" "${LOG_DIR}"

    log_info "Killing existing app on port ${APP_PORT}..."
    lsof -ti:"${APP_PORT}" 2>/dev/null | xargs kill 2>/dev/null || true
    sleep 1

    log_info "Starting ResearchEDF on port ${APP_PORT}..."

    if command -v tmux &>/dev/null; then
        tmux kill-session -t researchedc 2>/dev/null || true
        tmux new-session -d -s researchedc -c "${PROJECT_DIR}"
        tmux send-keys -t researchedc "export RESEARCHEDC_DB_PORT=${DB_PORT}" Enter
        tmux send-keys -t researchedc "java -jar ${WAR} --server.port=${APP_PORT} 2>&1 | tee ${LOG_DIR}/app.log" Enter
        log_ok "App started in tmux session 'researchedc'"
    else
        RESEARCHEDC_DB_PORT="${DB_PORT}" \
        nohup java -jar "${WAR}" --server.port="${APP_PORT}" \
            > "${LOG_DIR}/app.log" 2>&1 &
        log_ok "App started (PID $!)"
    fi

    echo ""
    echo "  URL:      http://localhost:${APP_PORT}"
    echo "  Login:    admin / admin"
    echo "  Logs:     tail -f ${LOG_DIR}/app.log"
    echo "  Attach:   tmux attach -t researchedc"
}

# ---- Stop ----
cmd_stop() {
    log_info "Stopping..."
    if command -v tmux &>/dev/null && tmux has-session -t researchedc 2>/dev/null; then
        tmux send-keys -t researchedc C-c
        sleep 2
        tmux kill-session -t researchedc 2>/dev/null || true
    fi
    lsof -ti:"${APP_PORT}" 2>/dev/null | xargs kill 2>/dev/null || true
    log_ok "App stopped"
}

# ---- Restart ----
cmd_restart() {
    cmd_stop
    sleep 2
    cmd_start
}

# ---- Status ----
cmd_status() {
    echo ""
    printf "  %-20s %s\n" "SERVICE" "STATUS"
    printf "  %-20s %s\n" "───────" "──────"

    local app_st="stopped"
    if lsof -ti:"${APP_PORT}" 2>/dev/null | grep -q .; then
        app_st="running"
    fi
    printf "  %-20s %s\n" "App (port ${APP_PORT})" "${app_st}"

    local pg_st="stopped"
    pg_isready -h "${DB_HOST}" -p "${DB_PORT}" &>/dev/null && pg_st="running"
    printf "  %-20s %s\n" "PostgreSQL (${DB_PORT})" "${pg_st}"

    local tmux_st="not used"
    if command -v tmux &>/dev/null && tmux has-session -t researchedc 2>/dev/null; then
        tmux_st="active"
    fi
    printf "  %-20s %s\n" "tmux session" "${tmux_st}"
    echo ""
}

# ---- Logs ----
cmd_logs() {
    if [ -f "${LOG_DIR}/app.log" ]; then
        tail -f "${LOG_DIR}/app.log"
    else
        log_warn "No logs yet — start the app first"
    fi
}

# ---- Clean ----
cmd_clean() {
    log_info "Cleaning..."
    cmd_stop 2>/dev/null || true
    rm -rf "${PROJECT_DIR}/app/target" "${PROJECT_DIR}/frontend/dist"
    rm -rf "${DATA_DIR}" "${LOG_DIR}"
    log_ok "Clean complete"
}

# ---- Main ----
case "${1:-help}" in
    build)    cmd_build   ;;
    init)     cmd_init    ;;
    start)    cmd_start   ;;
    stop)     cmd_stop    ;;
    restart)  cmd_restart ;;
    status)   cmd_status  ;;
    logs)     cmd_logs    ;;
    clean)    cmd_clean   ;;
    help|*)
        cat << EOF
ResearchEDF Host Deployment (no Docker)

Usage: bash deploy-host.sh <command>

Commands:
  build              Build frontend + backend WAR
  init              Create database + schema + admin/admin account
  start             Start application (persistent tmux session)
  stop              Stop application
  status             Show service status
  logs               Tail application log
  clean              Remove build artifacts and data

Example:
  bash deploy-host.sh build
  bash deploy-host.sh init
  bash deploy-host.sh start

Config (via environment variables):
  RESEARCHEDC_DB_HOST    (default: localhost)
  RESEARCHEDC_DB_PORT    (default: 5433)
  RESEARCHEDC_DB_NAME    (default: researchedc)
  RESEARCHEDC_DB_USER    (default: researchedc)
  RESEARCHEDC_DB_PASS    (default: researchedc)
  RESEARCHEDC_APP_PORT   (default: 8080)

Defaults account: admin / admin
EOF
        ;;
esac
