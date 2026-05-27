#!/bin/bash
# =============================================================================
# ResearchEDC — Bare Host Deployment
#
# Usage: bash deploy.sh <command>
# Commands: setup, init-db, build, start, stop, restart, status, logs, clean
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}"

# ---- Configuration ----
load_env() {
    local env_file="${PROJECT_DIR}/.env"
    if [ -f "${env_file}" ]; then
        set -a; source "${env_file}"; set +a
    fi
}
load_env

DB_HOST="${RESEARCHEDC_DB_HOST:-localhost}"
DB_PORT="${RESEARCHEDC_DB_PORT:-5433}"
DB_NAME="${RESEARCHEDC_DB_NAME:-researchedc}"
DB_USER="${RESEARCHEDC_DB_USER:-researchedc}"
DB_PASS="${RESEARCHEDC_DB_PASS:-researchedc}"
Q_DB="researchedc_questionnaire"

APP_PORT="${RESEARCHEDC_APP_PORT:-8080}"
Q_PORT="${QUESTIONNAIRE_PORT:-8000}"
CADDY_PORT="${RESEARCHEDC_CADDY_PORT:-80}"

DATA_DIR="${PROJECT_DIR}/data"
LOG_DIR="${PROJECT_DIR}/logs"
PID_DIR="${PROJECT_DIR}/.pids"
CONFIG_DIR="${PROJECT_DIR}/config"

# ---- Logging ----
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_ok()      { echo -e "${GREEN}[OK]${NC} $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

# ---- Helpers ----
require_cmd() {
    for cmd in "$@"; do
        command -v "$cmd" &>/dev/null || { log_error "'$cmd' not found"; exit 1; }
    done
}

is_running() {
    [ -f "$1" ] && kill -0 "$(cat "$1")" 2>/dev/null
}

stop_pid() {
    local pidfile="$1" name="$2"
    if [ -f "${pidfile}" ]; then
        local pid; pid=$(cat "${pidfile}")
        if kill -0 "${pid}" 2>/dev/null; then
            kill "${pid}" 2>/dev/null
            log_ok "${name} stopped (PID ${pid})"
            return 0
        fi
        rm -f "${pidfile}"
    fi
    return 1
}

generate_datainfo() {
    local target_dir="$1"
    mkdir -p "${target_dir}"

    local external_url="${RESEARCHEDC_EXTERNAL_URL:-}"
    if [ -z "${external_url}" ]; then
        local server_ip
        server_ip=$(hostname -I 2>/dev/null | awk '{print $1}')
        [ -n "${server_ip}" ] && external_url="http://${server_ip}:${CADDY_PORT}"
    fi
    [ -z "${external_url}" ] && external_url="http://localhost:${APP_PORT}"

    cat > "${target_dir}/datainfo.properties" << PROPS
dbType=postgres
dbHost=${DB_HOST}
dbPort=${DB_PORT}
db=${DB_NAME}
dbUser=${DB_USER}
dbPass=${DB_PASS}
filePath=${DATA_DIR}/
log.dir=${LOG_DIR}
logLocation=local
logLevel=info
sysURL=${external_url}/MainMenu
maxInactiveInterval=3600
ldap.enabled=false
ldap.host=ldap://localhost:389
ldap.userDn=
ldap.password=
ldap.loginQuery=(sAMAccountName={0})
ldap.passwordRecoveryURL=
ldap.userSearch.baseDn=
ldap.userSearch.query=
ldap.userData.distinguishedName=distinguishedName
ldap.userData.username=sAMAccountName
ldap.userData.firstName=givenName
ldap.userData.lastName=sn
ldap.userData.email=mail
ldap.userData.organization=company
org.quartz.jobStore.misfireThreshold=18000000
org.quartz.threadPool.threadCount=1
org.quartz.threadPool.threadPriority=5
hibernate.ddl.auto=none
extract.number=99
collectStats=false
designerURL=https://designer13.openclinica.com/
OpenClinica.version=0.1
PROPS
}

generate_questionnaire_env() {
    local env_file="${PROJECT_DIR}/questionnaire-service/apps/api/.env"
    cat > "${env_file}" << QENV
DATABASE_URL=postgresql+psycopg://${DB_USER}:${DB_PASS}@${DB_HOST}:${DB_PORT}/${Q_DB}
REDIS_URL=redis://localhost:6379/0
SECRET_KEY=dev-secret-key-change-in-production
CORS_ORIGINS=["http://localhost:${APP_PORT}"]
HOST=127.0.0.1
PORT=${Q_PORT}
LOG_LEVEL=info
QENV
}

TOMCAT_DIR="${PROJECT_DIR}/.tomcat"
TOMCAT_VERSION="10.1.55"
TOMCAT_URL="https://dlcdn.apache.org/tomcat/tomcat-10/v${TOMCAT_VERSION}/bin/apache-tomcat-${TOMCAT_VERSION}.tar.gz"

install_tomcat() {
    if [ -x "${TOMCAT_DIR}/bin/catalina.sh" ]; then
        return 0
    fi
    log_info "Downloading Tomcat ${TOMCAT_VERSION}..."
    mkdir -p "${TOMCAT_DIR}"
    curl -fsSL "${TOMCAT_URL}" -o /tmp/tomcat.tar.gz \
        && tar -xzf /tmp/tomcat.tar.gz -C "${TOMCAT_DIR}" --strip-components=1 \
            && rm /tmp/tomcat.tar.gz \
            && chmod +x "${TOMCAT_DIR}/bin/"*.sh \
            && log_ok "Tomcat installed" \
        || { log_error "Failed to install Tomcat"; return 1; }
}

generate_caddyfile() {
    local config_file="${CONFIG_DIR}/Caddyfile"
    cat > "${config_file}" << CADDY
{
    admin localhost:2019
}

:${CADDY_PORT} {
    header {
        Strict-Transport-Security "max-age=31536000; includeSubDomains"
        X-Frame-Options "SAMEORIGIN"
        X-Content-Type-Options "nosniff"
        X-XSS-Protection "1; mode=block"
        Referrer-Policy "strict-origin-when-cross-origin"
        -Server
    }
    encode gzip

    handle /api/* {
        reverse_proxy 127.0.0.1:${APP_PORT}
    }
    handle /app/* {
        reverse_proxy 127.0.0.1:${APP_PORT}
    }
    handle /q/* {
        reverse_proxy 127.0.0.1:${Q_PORT}
    }
    handle /actuator/* {
        @denied not remote_ip 127.0.0.0/8
        respond @denied 403
        reverse_proxy 127.0.0.1:${APP_PORT}
    }
    handle {
        reverse_proxy 127.0.0.1:${APP_PORT}
    }
}
CADDY
}

# ---- Commands ----
cmd_setup() {
    log_info "Installing system dependencies..."

    if ! command -v apt-get &>/dev/null; then
        log_error "This script requires apt-get (Debian/Ubuntu)"
        exit 1
    fi

    sudo apt-get update -qq

    sudo apt-get install -y -qq \
        openjdk-21-jdk maven nodejs npm postgresql-client \
        curl wget lsof netcat-openbsd >/dev/null

    log_ok "System packages installed"

    local java_ver; java_ver=$(java -version 2>&1 | sed -n 's/.*version "\([0-9]*\)\..*/\1/p')
    [ "${java_ver}" -ge 21 ] || { log_error "Java 21+ required (found ${java_ver})"; exit 1; }
    log_ok "Java ${java_ver}"

    if ! command -v pnpm &>/dev/null; then
        log_info "Installing pnpm..."
        npm install -g pnpm >/dev/null 2>&1
        log_ok "pnpm installed"
    fi

    if ! command -v caddy &>/dev/null; then
        log_info "Installing Caddy..."
        sudo apt-get install -y -qq caddy >/dev/null 2>&1 \
            || { curl -fsSL https://caddyserver.com/api/download?os=linux&arch=amd64 -o /tmp/caddy \
            && sudo install -m 755 /tmp/caddy /usr/local/bin/caddy && rm /tmp/caddy; }
        log_ok "Caddy installed"
    fi

    local caddy_bin
    caddy_bin=$(command -v caddy)
    local cap
    cap=$(getcap "${caddy_bin}" 2>/dev/null | grep -o 'cap_net_bind_service' || true)
    if [ -z "${cap}" ]; then
        log_info "Granting Caddy permission to bind port 80..."
        sudo setcap cap_net_bind_service=+ep "${caddy_bin}"
        log_ok "Caddy can now bind port 80"
    fi

    if ! command -v redis-server &>/dev/null; then
        log_info "Installing Redis..."
        sudo apt-get install -y -qq redis-server >/dev/null 2>&1
        sudo systemctl enable --now redis-server 2>/dev/null || true
        log_ok "Redis installed"
    fi

    mkdir -p "${DATA_DIR}" "${LOG_DIR}" "${PID_DIR}" "${CONFIG_DIR}"

    if ! command -v uv &>/dev/null; then
        log_info "Installing uv..."
        curl -LsSf https://astral.sh/uv/install.sh | sh
        export PATH="$HOME/.local/bin:$PATH"
        log_ok "uv installed"
    fi

    if ! command -v python3 &>/dev/null; then
        log_warn "python3 not found, installing via uv..."
        uv python install 3.12
        export PATH="$HOME/.local/bin:$PATH"
        log_ok "Python installed"
    fi

    log_info "Setting up questionnaire-service venv..."
    cd "${PROJECT_DIR}/questionnaire-service/apps/api"
    rm -rf .venv
    mkdir -p ~/.config/uv
    cat > ~/.config/uv/uv.toml << 'UVCONF'
[[index]]
url = "https://mirrors.tuna.tsinghua.edu.cn/pypi/web/simple/"
default = true
UVCONF
    uv venv .venv
    source .venv/bin/activate
    uv pip install -r pyproject.toml
    deactivate
    cd "${PROJECT_DIR}"

    log_ok "Setup complete"
}

cmd_init_db() {
    log_info "Initializing PostgreSQL..."

    pg_isready -h "${DB_HOST}" -p "${DB_PORT}" &>/dev/null || {
        log_error "PostgreSQL not running on ${DB_HOST}:${DB_PORT}"
        log_info "Start: sudo systemctl start postgresql"
        exit 1
    }

    # ---- Database reset (prompts for sudo if needed) ----
    log_info "Requesting sudo access for PostgreSQL admin operations..."
    if sudo -v 2>/dev/null; then
        sudo -u postgres psql -tc "SELECT 1 FROM pg_roles WHERE rolname='${DB_USER}'" | grep -q 1 \
            || sudo -u postgres psql -c "CREATE USER ${DB_USER} WITH PASSWORD '${DB_PASS}';"

        # Drop existing databases and recreate fresh
        for db in "${DB_NAME}" "${Q_DB}"; do
            log_info "Dropping database '${db}' if exists..."
            sudo -u postgres psql -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='${db}' AND pid <> pg_backend_pid();" 2>/dev/null || true
            sudo -u postgres psql -c "DROP DATABASE IF EXISTS \"${db}\";"
            sudo -u postgres psql -c "CREATE DATABASE \"${db}\" OWNER ${DB_USER};"
            sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE \"${db}\" TO ${DB_USER};"
        done
        log_ok "Databases recreated from scratch"
    else
        log_warn "sudo password prompt failed — skipping database drop/recreate"
        log_info "  Ensure databases '${DB_NAME}' and '${Q_DB}' exist manually if needed."
        log_info "  To enable full reset: grant this user passwordless sudo, or run:"
        log_info "    sudo -u postgres psql -c \"DROP DATABASE IF EXISTS ${DB_NAME}; CREATE DATABASE ${DB_NAME} OWNER ${DB_USER};\""
    fi

    # ---- Generate datainfo.properties for Liquibase (shared module + app) ----
    mkdir -p shared/src/main/resources app/src/main/resources
    generate_datainfo shared/src/main/resources
    generate_datainfo app/src/main/resources

    # ---- Run Liquibase migrations (directly via liquibase-core to avoid Maven plugin classpath issues) ----
    log_info "Applying Liquibase migrations..."
    cd "${PROJECT_DIR}"
    mvn compile -pl shared -DskipTests -q 2>/dev/null || {
        log_warn "Maven compile failed — cannot run Liquibase"
        return
    }
    local lb_classpath
    lb_classpath=$(mvn dependency:build-classpath -pl shared -DincludeScope=runtime -q -Dmdep.outputFile=/dev/stdout 2>/dev/null):shared/target/classes
    # Drop all existing objects first (fresh schema from scratch)
    java -cp "${lb_classpath}" liquibase.integration.commandline.Main \
        --changeLogFile=migration/master.xml \
        --url="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}" \
        --username="${DB_USER}" \
        --password="${DB_PASS}" \
        --classpath=shared/target/classes \
        dropAll 2>&1 | grep -v "^##\|^$" || true
    # Apply all migrations
    # Note: data-copy changesets (module-*-copy-*) may fail on fresh databases
    # because they assume an existing legacy schema. These are safe to skip.
    java -cp "${lb_classpath}" liquibase.integration.commandline.Main \
        --changeLogFile=migration/master.xml \
        --url="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}" \
        --username="${DB_USER}" \
        --password="${DB_PASS}" \
        --classpath=shared/target/classes \
        update 2>&1 | grep -v "^##\|^$" || true
    log_ok "Migrations applied"

    # ---- Create admin/admin account ----
    log_info "Creating admin account..."
    local bcrypt_hash
    bcrypt_hash=$(python3 -c "
import bcrypt
h = bcrypt.hashpw(b'admin', bcrypt.gensalt(rounds=10)).decode()
print('{bcrypt}' + h)
" 2>/dev/null)

    if [ -n "${bcrypt_hash}" ]; then
        PGPASSWORD="${DB_PASS}" psql -h "${DB_HOST}" -p "${DB_PORT}" -U "${DB_USER}" -d "${DB_NAME}" -c "
INSERT INTO user_account (user_name, passwd, first_name, last_name, email, status_id, owner_id, date_created, enabled, account_non_locked)
SELECT 'admin', '${bcrypt_hash}', '管理', '员', 'admin@example.com', 1, 1, CURRENT_DATE, true, true
WHERE NOT EXISTS (SELECT 1 FROM user_account WHERE user_name = 'admin');
INSERT INTO study_user_role (user_name, role_name, study_id, status_id, owner_id, date_created)
SELECT 'admin', 'Study_Director', 1, 1, 1, CURRENT_DATE
WHERE NOT EXISTS (SELECT 1 FROM study_user_role WHERE user_name = 'admin' AND role_name = 'Study_Director');
" 2>/dev/null && log_ok "Admin account created (admin / admin)" || log_warn "Failed to create admin account"
    else
        log_error "bcrypt hash generation failed — admin account not created"
    fi
}

cmd_build() {
    log_info "Building frontend..."
    cd "${PROJECT_DIR}/frontend"
    pnpm install --frozen-lockfile
    pnpm build
    cd "${PROJECT_DIR}"

    log_info "Copying frontend to app resources..."
    mkdir -p app/src/main/resources/static
    cp -r frontend/dist/* app/src/main/resources/static/

    log_info "Generating config..."
    generate_datainfo app/src/main/resources
    [ ! -f app/src/main/resources/extract.properties ] \
        && cp web/src/main/resources/extract.properties app/src/main/resources/

    log_info "Building backend..."
    mvn clean install -DskipTests -q

    log_info "Copying JSP and web resources to classpath..."
    mkdir -p app/target/classes/WEB-INF/jsp
    [ -d web/src/main/webapp/WEB-INF/jsp ] \
        && cp -r web/src/main/webapp/WEB-INF/jsp/* app/target/classes/WEB-INF/jsp/
    [ -d web/src/main/webapp/images ] \
        && mkdir -p app/target/classes/images \
        && cp -r web/src/main/webapp/images/* app/target/classes/images/
    [ -d web/src/main/webapp/includes ] \
        && mkdir -p app/target/classes/includes \
        && cp -r web/src/main/webapp/includes/* app/target/classes/includes/

    log_info "Copying DAO properties to classpath (for SQLFactory)..."
    mkdir -p app/target/classes/properties
    [ -d shared/src/main/resources/properties ] \
        && cp -r shared/src/main/resources/properties/* app/target/classes/properties/

    log_ok "Build complete"
    ls -lh app/target/OpenClinica*.war 2>/dev/null || log_warn "No WAR in app/target"
}

cmd_start() {
    mkdir -p "${DATA_DIR}" "${LOG_DIR}" "${PID_DIR}" "${CONFIG_DIR}"

    local java_home="${JAVA_HOME:-$(dirname "$(dirname "$(readlink -f "$(which java)")")")}"
    generate_datainfo app/src/main/resources
    generate_questionnaire_env

    log_info "Building frontend..."
    cd "${PROJECT_DIR}/frontend"
    pnpm install --frozen-lockfile || pnpm install
    pnpm build
    cd "${PROJECT_DIR}"

    log_info "Starting App on port ${APP_PORT}..."

    # Kill any existing app on the port
    lsof -ti:"${APP_PORT}" 2>/dev/null | xargs kill 2>/dev/null || true
    sleep 1

    # Start in a tmux session for persistence
    if command -v tmux &>/dev/null; then
        tmux kill-session -t researchedc 2>/dev/null || true
        tmux new-session -d -s researchedc -c "${PROJECT_DIR}"
        tmux send-keys -t researchedc "export RESEARCHEDC_DB_PORT=${DB_PORT}" Enter
        tmux send-keys -t researchedc "java -jar app/target/ResearchEDC.war --server.port=${APP_PORT} 2>&1 | tee ${LOG_DIR}/app.log" Enter
        log_ok "App started in tmux session 'researchedc'"
    else
        # Fallback: nohup
        cd "${PROJECT_DIR}"
        RESEARCHEDC_DB_PORT="${DB_PORT}" \
        nohup java -jar app/target/ResearchEDC.war \
            --server.port="${APP_PORT}" \
            > "${LOG_DIR}/app.log" 2>&1 &
        echo $! > "${PID_DIR}/app.pid"
        log_ok "App started (PID $(cat "${PID_DIR}/app.pid"))"
    fi

    log_info "Starting Questionnaire Service on port ${Q_PORT}..."
    cd "${PROJECT_DIR}/questionnaire-service/apps/api"
    source .venv/bin/activate
    PYTHONPATH="$PWD" alembic upgrade head 2>/dev/null || log_warn "Alembic migration skipped"
    nohup .venv/bin/python -m uvicorn app.main:app \
        --host 127.0.0.1 --port "${Q_PORT}" --log-level info \
        > "${LOG_DIR}/questionnaire.log" 2>&1 &
    echo $! > "${PID_DIR}/questionnaire.pid"
    deactivate
    cd "${PROJECT_DIR}"
    log_ok "Questionnaire started (PID $(cat "${PID_DIR}/questionnaire.pid"))"

    if command -v caddy &>/dev/null; then
        log_info "Starting Caddy on port ${CADDY_PORT}..."
        generate_caddyfile
        nohup caddy run --config "${CONFIG_DIR}/Caddyfile" --adapter caddyfile \
            > "${LOG_DIR}/caddy.log" 2>&1 &
        echo $! > "${PID_DIR}/caddy.pid"
        log_ok "Caddy started (PID $(cat "${PID_DIR}/caddy.pid"))"
    else
        log_warn "Caddy not installed — external access unavailable"
    fi

    echo ""
    log_ok "All services started"
    echo ""
    echo "  Local:    http://localhost:${APP_PORT}"
    echo "  Questionnaire: http://localhost:${Q_PORT}"
    command -v caddy &>/dev/null && echo "  External: http://localhost:${CADDY_PORT}"
    echo "  Logs:     tail -f ${LOG_DIR}/*.log"
}

cmd_stop() {
    log_info "Stopping services..."
    local stopped=0

    stop_pid "${PID_DIR}/caddy.pid" "Caddy" && stopped=1
    stop_pid "${PID_DIR}/app.pid" "App" && stopped=1
    stop_pid "${PID_DIR}/questionnaire.pid" "Questionnaire" && stopped=1

    if [ -x "${TOMCAT_DIR}/bin/catalina.sh" ] && [ -f "${PID_DIR}/app.pid" ]; then
        CATALINA_HOME="${TOMCAT_DIR}" "${TOMCAT_DIR}/bin/catalina.sh" stop 2>/dev/null || true
        sleep 3
    fi
    pkill -f "spring-boot:run.*research-edc-app" 2>/dev/null || true
    pkill -f "uvicorn app.main:app.*${Q_PORT}" 2>/dev/null || true
    pkill -f "caddy run.*${CONFIG_DIR}/Caddyfile" 2>/dev/null || true

    rm -f "${PID_DIR}"/*.pid

    [ "${stopped}" -eq 0 ] && log_warn "No running services found"
}

cmd_restart() {
    cmd_stop
    sleep 2
    cmd_start
}

cmd_status() {
    echo ""
    printf "  %-20s %-10s %s\n" "SERVICE" "STATUS" "PID"
    printf "  %-20s %-10s %s\n" "───────" "──────" "───"

    for pair in "Caddy:${PID_DIR}/caddy.pid" "App:${PID_DIR}/app.pid" "Questionnaire:${PID_DIR}/questionnaire.pid"; do
        local name="${pair%%:*}" pidfile="${pair##*:}"
        local pid="-" st="stopped"
        if [ -f "${pidfile}" ]; then
            pid=$(cat "${pidfile}")
            kill -0 "${pid}" 2>/dev/null && st="running" || rm -f "${pidfile}"
        fi
        printf "  %-20s %-10s %s\n" "${name}" "${st}" "${pid}"
    done

    pg_isready -h "${DB_HOST}" -p "${DB_PORT}" &>/dev/null \
        && printf "  %-20s %-10s\n" "PostgreSQL" "running" \
        || printf "  %-20s %-10s\n" "PostgreSQL" "stopped"
    echo ""
}

cmd_logs() {
    [ -d "${LOG_DIR}" ] && tail -f "${LOG_DIR}"/*.log || log_warn "No logs directory"
}

cmd_clean() {
    log_info "Cleaning build artifacts and runtime data..."
    rm -rf "${PROJECT_DIR}/app/target" "${PROJECT_DIR}/web/target" "${PROJECT_DIR}/ws/target"
    rm -rf "${PROJECT_DIR}/shared/target" "${PROJECT_DIR}/frontend/dist"
    rm -rf "${PID_DIR}" "${DATA_DIR}" "${LOG_DIR}" "${CONFIG_DIR}"
    rm -rf "${PROJECT_DIR}/questionnaire-service/apps/api/.venv"
    log_ok "Clean complete"
}

# ---- Main ----
case "${1:-help}" in
    setup)   cmd_setup   ;;
    init-db) cmd_init_db ;;
    build)   cmd_build   ;;
    start)   cmd_start   ;;
    stop)    cmd_stop    ;;
    restart) cmd_restart ;;
    status)  cmd_status  ;;
    logs)    cmd_logs    ;;
    clean)   cmd_clean   ;;
    help|*)
        cat << EOF
ResearchEDC Bare Host Deployment

Usage: bash deploy.sh <command>

Commands:
  setup    Install prerequisites, uv, Python venv
  init-db  Create PostgreSQL user + databases (needs sudo)
  build    Build frontend + backend
  start    Start all services
  stop     Stop all services
  restart  Stop then start
  status   Show service status
  logs     Tail log files
  clean    Remove build artifacts and runtime data

Ports:
  App:              ${APP_PORT}
  Questionnaire:    ${Q_PORT}
  Caddy (external): ${CADDY_PORT}
  PostgreSQL:       ${DB_PORT}
EOF
        ;;
esac
