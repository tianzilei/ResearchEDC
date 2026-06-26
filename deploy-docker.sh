#!/bin/bash
# =============================================================================
# ResearchEDC — Docker Compose Deployment
#
# Usage: bash deploy-docker.sh <command>
# Commands: build, up, down, restart, logs, status, clean
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
DB_PORT="${RESEARCHEDC_DB_PORT:-5432}"
DB_NAME="${RESEARCHEDC_DB_NAME:-researchedc}"
DB_USER="${RESEARCHEDC_DB_USER:-researchedc}"
DB_PASS="${RESEARCHEDC_DB_PASS:-researchedc}"

MINIO_PORT="${RESEARCHEDC_MINIO_PORT:-9000}"
MINIO_CONSOLE_PORT="${RESEARCHEDC_MINIO_CONSOLE_PORT:-9001}"
MINIO_ROOT_USER="${RESEARCHEDC_MINIO_ROOT_USER:-minio}"
MINIO_ROOT_PASS="${RESEARCHEDC_MINIO_ROOT_PASS:-minio-password}"
MINIO_BUCKET="${RESEARCHEDC_MINIO_BUCKET:-questionnaire-exports}"

NGINX_HTTP_PORT="${RESEARCHEDC_NGINX_HTTP_PORT:-80}"
NGINX_HTTPS_PORT="${RESEARCHEDC_NGINX_HTTPS_PORT:-443}"

COMPOSE_FILE="${PROJECT_DIR}/deploy/compose/docker-compose.yml"
COMPOSE_ARGS="-f ${COMPOSE_FILE}"

# ---- Logging ----
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; BLUE='\033[0;34m'; NC='\033[0m'
log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_ok()      { echo -e "${GREEN}[OK]${NC} $1"; }
log_warn()    { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

# ---- Helpers ----
require_docker() {
    if ! command -v docker &>/dev/null; then
        log_error "Docker not found — install Docker first"
        return 1
    fi
    if ! docker compose version &>/dev/null; then
        log_error "docker compose plugin not available"
        return 1
    fi
    return 0
}

# ---- Commands ----
cmd_build() {
    require_docker || return 1
    log_info "Building Docker images (this may take several minutes)..."
    docker compose ${COMPOSE_ARGS} build --parallel "$@"
    log_ok "Docker images built"
}

cmd_up() {
    require_docker || return 1
    log_info "Starting Docker services..."
    docker compose ${COMPOSE_ARGS} up -d --remove-orphans "$@"
    log_info "Waiting for services to be healthy..."
    sleep 5
    cmd_status
}

cmd_down() {
    require_docker || return 1
    log_info "Stopping Docker services..."
    docker compose ${COMPOSE_ARGS} down "$@"
    log_ok "Docker services stopped"
}

cmd_restart() {
    require_docker || return 1
    log_info "Restarting Docker services..."
    docker compose ${COMPOSE_ARGS} restart "$@"
    log_ok "Docker services restarted"
}

cmd_logs() {
    require_docker || return 1
    docker compose ${COMPOSE_ARGS} logs -f --tail=100 "$@"
}

cmd_status() {
    require_docker || return 1
    echo ""
    docker compose ${COMPOSE_ARGS} ps --format "table {{.Name}}\t{{.Image}}\t{{.Status}}\t{{.Ports}}"
    echo ""
}

cmd_clean() {
    require_docker || return 1
    log_warn "Removing Docker services, volumes, and orphan containers..."
    docker compose ${COMPOSE_ARGS} down -v --remove-orphans "$@"
    log_ok "Docker environment cleaned"
}

# ---- Main ----
case "${1:-help}" in
    build)   cmd_build "${@:2}"   ;;
    up)      cmd_up "${@:2}"      ;;
    down)    cmd_down "${@:2}"    ;;
    restart) cmd_restart "${@:2}" ;;
    logs)    cmd_logs "${@:2}"    ;;
    status)  cmd_status           ;;
    clean)   cmd_clean "${@:2}"   ;;
    help|*)
        cat << EOF
ResearchEDC Docker Compose Deployment

Usage: bash deploy-docker.sh <command>

Commands:
  build    Build all Docker images
  up       Start all services (docker compose up -d)
  down     Stop all services
  restart  Restart all services
  logs     Tail container logs
  status   Show container status
  clean    Stop services + remove volumes

Services:
  nginx         — Reverse proxy (port 80, 443)
  backend       — Spring Boot app
  questionnaire — Python FastAPI
  postgres      — PostgreSQL 17 (port ${DB_PORT})
  redis         — Redis 7
  minio         — Object storage (ports ${MINIO_PORT}, ${MINIO_CONSOLE_PORT})

For bare host deployment: bash deploy-bare.sh
EOF
        ;;
esac
