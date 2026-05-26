#!/bin/bash
# =============================================================================
# ResearchEDF — Hibernate Schema Validation
#
# Validates that Hibernate entity mappings match the actual database schema
# using hibernate.hbm2ddl.auto=validate in a Docker environment.
#
# Prerequisites:
#   - Docker and docker-compose installed
#   - A PostgreSQL instance with ResearchEDF schema (or use the compose setup)
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "${SCRIPT_DIR}")"
COMPOSE_FILE="${PROJECT_DIR}/deploy/compose/docker-compose.dev.yml"

echo "=== ResearchEDF Hibernate Schema Validation ==="
echo ""

# Ensure Docker is available
if ! command -v docker &> /dev/null; then
    echo "ERROR: Docker is required but not found."
    exit 1
fi

# Check if a database is available
echo "Step 1: Checking database connectivity..."
if docker compose -f "${COMPOSE_FILE}" exec postgres pg_isready -U researchedc &>/dev/null 2>&1; then
    echo "  Database is running and accessible."
    DB_RUNNING=true
else
    echo "  Database is not running. Starting PostgreSQL container..."
    docker compose -f "${COMPOSE_FILE}" up -d postgres
    echo "  Waiting for PostgreSQL to become healthy..."
    docker compose -f "${COMPOSE_FILE}" exec postgres pg_isready -U researchedc -t 30
    DB_RUNNING=true
fi

echo ""
echo "Step 2: Building web image with validation mode..."
OC_HIBERNATE_DDL_AUTO=validate docker compose -f "${COMPOSE_FILE}" build web

echo ""
echo "Step 3: Running web container in validate mode..."
echo "  (This will start Tomcat, load Hibernate, and validate all entity mappings)"
echo "  Container will exit after startup — check logs for validation errors."
echo ""

# Run with a timeout (30s should be enough to detect validation failures)
docker compose -f "${COMPOSE_FILE}" run --rm -e OC_HIBERNATE_DDL_AUTO=validate web &
CONTAINER_PID=$!

# Wait for startup or failure
sleep 20

# Check container logs for validation messages
echo ""
echo "Step 4: Checking validation results..."
docker compose -f "${COMPOSE_FILE}" logs web 2>&1 | tail -100 || true

echo ""
echo "=== Validation Complete ==="
echo ""
echo "Check the output above for:"
echo "  - 'Schema-validation: missing table'  → table exists in entity but not in DB"
echo "  - 'Schema-validation: missing column' → column exists in entity but not in DB"
echo "  - 'HHH000342: could not read the hi value' → sequence/ID generator mismatch"
echo "  - 'Unsuccessful: ...' → DDL execution failure"
echo ""
echo "If validation passes, the application will continue starting up."
echo "If validation fails, the application will abort with a descriptive error."
echo ""
echo "To run the application normally after validation:"
echo "  docker compose -f ${COMPOSE_FILE} up web"
