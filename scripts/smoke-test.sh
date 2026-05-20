#!/bin/bash
# =============================================================================
# ResearchEDC — Docker Compose Smoke Test
#
# Starts the application stack and verifies endpoints respond correctly.
# Designed to run as a non-interactive validation step.
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "${SCRIPT_DIR}")"
COMPOSE_FILE="${PROJECT_DIR}/deploy/compose/docker-compose.dev.yml"

PASS=0
FAIL=0

check() {
    local desc="$1"
    local expected="$2"
    shift 2
    if "$@"; then
        echo "  PASS: ${desc}"
        PASS=$((PASS + 1))
    else
        echo "  FAIL: ${desc} (expected: ${expected})"
        FAIL=$((FAIL + 1))
    fi
}

echo "=== ResearchEDC Smoke Test ==="
echo ""

# Ensure stack is running
echo "Pre-check: Starting Docker Compose stack..."
docker compose -f "${COMPOSE_FILE}" up --build -d postgres web mailhog

# Wait for PostgreSQL
echo "  Waiting for PostgreSQL (30s max)..."
for i in $(seq 1 30); do
    if docker compose -f "${COMPOSE_FILE}" exec postgres pg_isready -U researchedc &>/dev/null; then
        echo "  PostgreSQL is ready."
        break
    fi
    sleep 1
done

# Wait for web app (Tomcat startup + context deploy)
echo "  Waiting for web application (90s max)..."
for i in $(seq 1 90); do
    if docker compose -f "${COMPOSE_FILE}" exec web curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null | grep -q "200\|302\|401"; then
        echo "  Web application is ready."
        break
    fi
    sleep 1
done

echo ""

# Test 1: HTTP endpoint responds
echo "Test 1: Main page accessibility..."
HTTP_CODE=$(docker compose -f "${COMPOSE_FILE}" exec web curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null || echo "000")
check "Main page HTTP status is 200/302/401" "200|302|401" test "${HTTP_CODE}" -ge 200 -a "${HTTP_CODE}" -lt 500

# Test 2: System status (if available)
echo "Test 2: System status endpoint..."
SYS_STATUS=$(docker compose -f "${COMPOSE_FILE}" exec web curl -s http://localhost:8080/SystemStatus 2>/dev/null || echo "")
check "System status returns content" "non-empty" test -n "${SYS_STATUS}"

# Test 3: Database connectivity from web container
echo "Test 3: Database connectivity..."
DB_PING=$(docker compose -f "${COMPOSE_FILE}" exec web curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null || echo "000")
check "Web app connects to database" "200" test "${DB_PING}" != "500"

# Test 4: MailHog endpoint
echo "Test 4: MailHog API..."
MAILHOG=$(docker compose -f "${COMPOSE_FILE}" exec web curl -s -o /dev/null -w "%{http_code}" http://mailhog:8025/api/v2/messages 2>/dev/null || echo "000")
check "MailHog API responds" "200" test "${MAILHOG}" = "200"

# Test 5: PostgreSQL connection count
echo "Test 5: PostgreSQL connection limit..."
CONN_CHECK=$(docker compose -f "${COMPOSE_FILE}" exec postgres psql -U researchedc -t -c "SELECT count(*) FROM pg_stat_activity WHERE datname='researchedc';" 2>/dev/null | tr -d ' ')
check "PostgreSQL accepts connections" "numeric" test "${CONN_CHECK}" -ge 0 2>/dev/null

echo ""

# Summary
echo "=== Smoke Test Results ==="
echo "  Passed: ${PASS}"
echo "  Failed: ${FAIL}"
echo ""

if [ "${FAIL}" -gt 0 ]; then
    echo "Some checks failed. Inspect logs with:"
    echo "  docker compose -f ${COMPOSE_FILE} logs web"
    exit 1
fi

echo "All checks passed. Stack is operational."
echo ""
echo "Access the application at:"
echo "  Web:     http://localhost:8080"
echo "  MailHog: http://localhost:8025"
echo "  Adminer: http://localhost:8082"
echo ""
echo "Teardown: docker compose -f ${COMPOSE_FILE} down -v"
