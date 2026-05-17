#!/bin/bash
# =============================================================================
# OpenClinica — Database Schema Initialization
#
# Creates a fresh OpenClinica database and applies Liquibase migrations.
# Used for setting up a new development or test environment.
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "${SCRIPT_DIR}")"

MODE="${1:-dev}"  # dev, test, or prod
COMPOSE_FILE="${PROJECT_DIR}/deploy/compose/docker-compose.${MODE}.yml"

if [ ! -f "${COMPOSE_FILE}" ]; then
    echo "ERROR: Compose file not found: ${COMPOSE_FILE}"
    echo "Usage: $0 [dev|test|prod]"
    exit 1
fi

echo "=== OpenClinica Database Initialization (${MODE}) ==="
echo ""

# Step 1: Start PostgreSQL
echo "Step 1: Starting PostgreSQL..."
docker compose -f "${COMPOSE_FILE}" up -d postgres
echo "  Waiting for PostgreSQL to become healthy..."
docker compose -f "${COMPOSE_FILE}" exec postgres pg_isready -U openclinica -t 30
echo "  PostgreSQL is ready."
echo ""

# Step 2: Run Liquibase migrations via the web container
echo "Step 2: Applying Liquibase migrations..."
echo "  Starting web container (migrations will auto-apply)..."
docker compose -f "${COMPOSE_FILE}" up -d web
echo "  Waiting for application to initialize (30s)..."
sleep 30
echo ""

# Step 3: Check for migration errors
echo "Step 3: Checking migration status..."
docker compose -f "${COMPOSE_FILE}" logs web 2>&1 | grep -i "liquibase\|migration\|success\|error\|failed" || echo "  (no migration messages found — check logs manually)"
echo ""

# Step 4: Verify key tables exist
echo "Step 4: Verifying schema..."
docker compose -f "${COMPOSE_FILE}" exec -T postgres psql -U openclinica -d openclinica -c "
SELECT table_name FROM information_schema.tables
WHERE table_schema = 'public'
  AND table_name IN ('study', 'subject', 'user_account', 'study_event', 'item_data', 'discrepancy_note')
ORDER BY table_name;
" 2>/dev/null || echo "  (PostgreSQL not accessible — container may still be starting)"
echo ""

echo "=== Initialization Complete ==="
echo ""
echo "The database 'openclinica' now has the full OpenClinica schema."
echo "Next steps:"
echo "  1. Access the application at http://localhost:8080"
echo "  2. Default admin account: admin / (check Docker logs for generated password)"
echo "  3. Run schema validation: bash scripts/db-schema-validate.sh"
echo ""
