#!/bin/bash
# =============================================================================
# ResearchEDF — Caddy Reverse Proxy Deployment
#
# Deploys the ResearchEDF stack with Caddy as the reverse proxy (pure proxy
# mode — no static file serving, all traffic proxied to backend containers).
#
# Usage:
#   Build Docker images:
#     bash scripts/deploy-caddy.sh build
#
#   Development (HTTP, default):
#     bash scripts/deploy-caddy.sh start
#
#   Development with Docker rebuild:
#     bash scripts/deploy-caddy.sh start --build
#
#   Production with auto HTTPS (requires domain name):
#     bash scripts/deploy-caddy.sh start --prod --domain edc.example.com
#
#   Production-like test via server IP (HTTP, no domain needed):
#     bash scripts/deploy-caddy.sh start --prod
#
#   Stop all services:
#     bash scripts/deploy-caddy.sh stop
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "${SCRIPT_DIR}")"
COMPOSE_DIR="${PROJECT_DIR}/deploy/compose"

MODE="dev"
DOMAIN=""
REBUILD=false
START=false
STOP=false
BUILD_ONLY=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        start)    START=true; shift ;;
        stop|shutdown) STOP=true; shift ;;
        build)    BUILD_ONLY=true; shift ;;
        --prod)   MODE="prod"; shift ;;
        --domain) DOMAIN="$2"; shift 2 ;;
        --build)  REBUILD=true; shift ;;
        --help)
            sed -n '2,17p' "${BASH_SOURCE[0]}"
            exit 0
            ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

if [ "${START}" = false ] && [ "${STOP}" = false ] && [ "${BUILD_ONLY}" = false ]; then
    sed -n '2,17p' "${BASH_SOURCE[0]}"
    exit 1
fi

# ---- Prerequisites ----
command -v docker &>/dev/null || { echo "ERROR: docker not found"; exit 1; }

if docker compose version &>/dev/null; then
    DOCKER_COMPOSE="docker compose"
elif docker-compose --version &>/dev/null; then
    DOCKER_COMPOSE="docker-compose"
else
    echo "ERROR: docker compose not found"
    exit 1
fi

# ---- Stoppage ----
if [ "${STOP}" = true ]; then
    echo "=== Stopping ResearchEDF (Caddy) ==="
    ${DOCKER_COMPOSE} -f "${COMPOSE_DIR}/docker-compose.${MODE}.yml" \
        -f "${COMPOSE_DIR}/docker-compose.caddy.yml" down
    echo "=== Stopped ==="
    exit 0
fi

# ---- Build only ----
if [ "${BUILD_ONLY}" = true ]; then
    echo "=== Building ResearchEDF ==="
    cd "${PROJECT_DIR}/frontend"
    if [ ! -d node_modules ]; then
        pnpm install --frozen-lockfile
    fi
    pnpm build
    cd "${PROJECT_DIR}"
    echo ""
    ${DOCKER_COMPOSE} -f "${COMPOSE_DIR}/docker-compose.${MODE}.yml" build
    echo "=== Build Complete ==="
    exit 0
fi

if [ -n "${DOMAIN}" ]; then
    PROTO="https"
    ACCESS_ADDR="${DOMAIN}"
    CADDYFILE="${PROJECT_DIR}/deploy/caddy/Caddyfile"
    if grep -q "^:80 " "${CADDYFILE}" 2>/dev/null; then
        cp "${CADDYFILE}" "${CADDYFILE}.bak"
        sed -i "s/^:80 /${DOMAIN} /" "${CADDYFILE}"
        echo "  Caddyfile: activated domain ${DOMAIN} (backup at Caddyfile.bak)"
    fi
else
    PROTO="http"
    ACCESS_ADDR="localhost"
    if command -v hostname &>/dev/null; then
        SERVER_IP=$(hostname -I 2>/dev/null | awk '{print $1}')
        if [ -n "${SERVER_IP}" ]; then
            ACCESS_ADDR="${SERVER_IP}"
        fi
    fi
    CADDYFILE="${PROJECT_DIR}/deploy/caddy/Caddyfile"
    if [ -f "${CADDYFILE}.bak" ] && ! grep -q "^:80 " "${CADDYFILE}" 2>/dev/null; then
        cp "${CADDYFILE}.bak" "${CADDYFILE}"
        echo "  Caddyfile: restored :80 from backup (${CADDYFILE}.bak)"
    fi
fi

# ---- Frontend build check ----
if [ ! -d "${PROJECT_DIR}/frontend/dist" ]; then
    echo "=== Building frontend ==="
    cd "${PROJECT_DIR}/frontend"
    if [ ! -d node_modules ]; then
        pnpm install --frozen-lockfile
    fi
    pnpm build
    cd "${PROJECT_DIR}"
    echo ""
fi

# ---- Docker build (optional) ----
if [ "${REBUILD}" = true ]; then
    echo "=== Rebuilding Docker images ==="
    ${DOCKER_COMPOSE} -f "${COMPOSE_DIR}/docker-compose.${MODE}.yml" build
    echo ""
fi

# ---- Deploy ----
echo "=== ResearchEDF Caddy Deployment ==="
echo "  Mode:      ${MODE}"
echo "  Access:    ${PROTO}://${ACCESS_ADDR}"
echo "  Compose:   ${MODE}.yml + caddy.yml"
echo "  Rebuild:   ${REBUILD}"
echo ""

if [ "${MODE}" = "prod" ] && [ -z "${DOMAIN}" ]; then
    echo "  [Note] No domain provided — using HTTP via server IP."
    echo "  For HTTPS, add: --domain your-domain.com"
    echo ""
fi

COMPOSE_FILE="${COMPOSE_DIR}/docker-compose.${MODE}.yml"
CADDY_COMPOSE="${COMPOSE_DIR}/docker-compose.caddy.yml"

if [ "${MODE}" = "prod" ]; then
    ${DOCKER_COMPOSE} -f "${COMPOSE_FILE}" -f "${CADDY_COMPOSE}" pull --quiet 2>/dev/null || true
fi

${DOCKER_COMPOSE} -f "${COMPOSE_FILE}" -f "${CADDY_COMPOSE}" up -d

echo ""
echo "=== Deployment Complete ==="
echo "  URL:      ${PROTO}://${ACCESS_ADDR}"
echo "  Proxy:    Caddy (pure reverse proxy)"
echo ""

${DOCKER_COMPOSE} -f "${COMPOSE_FILE}" -f "${CADDY_COMPOSE}" ps

# ---- Health check ----
echo ""
echo "Waiting for Caddy to be ready..."
for i in $(seq 1 12); do
    if curl -sf -o /dev/null "http://localhost/actuator/health" 2>/dev/null; then
        echo "  App is ready at http://localhost/actuator/health"
        echo "  Open:     ${PROTO}://${ACCESS_ADDR}"
        break
    fi
    if [ "${i}" -eq 12 ]; then
        echo "  Warning: app not responding yet"
        echo "  Check logs: ${DOCKER_COMPOSE} -f ${COMPOSE_FILE} -f ${CADDY_COMPOSE} logs -f"
    fi
    sleep 5
done
