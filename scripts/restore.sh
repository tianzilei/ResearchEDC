#!/bin/bash
# =============================================================================
# OpenClinica — Database & Files Restore
#
# Restores from a backup created by scripts/backup.sh.
# Usage: bash scripts/restore.sh <TIMESTAMP> [--db-only] [--data-only]
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "${SCRIPT_DIR}")"

if [ $# -lt 1 ]; then
    echo "Usage: $0 <TIMESTAMP> [--db-only] [--data-only]"
    echo "  TIMESTAMP format: YYYYMMDD_HHMMSS (from backup filename)"
    echo ""
    echo "Available backups:"
    ls -1 "${PROJECT_DIR}/backups/" 2>/dev/null || echo "  No backups found in ${PROJECT_DIR}/backups/"
    exit 1
fi

TIMESTAMP="$1"
BACKUP_DIR="${BACKUP_DIR:-${PROJECT_DIR}/backups}"
DB_ONLY=false
DATA_ONLY=false

while [[ $# -gt 0 ]]; do
    case "$1" in
        --db-only) DB_ONLY=true; shift ;;
        --data-only) DATA_ONLY=true; shift ;;
        *) shift ;;
    esac
done

DB_CONTAINER="oc-prod-postgres"
DB_USER="${POSTGRES_USER:-openclinica}"
DB_NAME="${POSTGRES_DB:-openclinica}"

echo "=== OpenClinica Restore (${TIMESTAMP}) ==="
echo ""

# Sanity checks
if [ ! -f "${BACKUP_DIR}/openclinica_db_${TIMESTAMP}.dump" ] && [ ! -f "${BACKUP_DIR}/openclinica_db_${TIMESTAMP}.dump.gz" ]; then
    echo "ERROR: DB backup not found for timestamp ${TIMESTAMP}"
    echo "  Looked for: ${BACKUP_DIR}/openclinica_db_${TIMESTAMP}.dump[.gz]"
    exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
    echo "ERROR: Database container '${DB_CONTAINER}' is not running."
    echo "  Start the stack first: docker compose -f deploy/compose/docker-compose.prod.yml up -d postgres"
    exit 1
fi

# Restore database
if [ "${DATA_ONLY}" = false ]; then
    echo "Step 1: Restoring PostgreSQL database..."
    echo "  WARNING: This will DROP and recreate the '${DB_NAME}' database."
    echo "  Proceeding in 5 seconds... (Ctrl+C to abort)"
    sleep 5

    # Handle compressed backup
    if [ -f "${BACKUP_DIR}/openclinica_db_${TIMESTAMP}.dump.gz" ]; then
        gunzip -c "${BACKUP_DIR}/openclinica_db_${TIMESTAMP}.dump.gz" > "/tmp/oc_restore_${TIMESTAMP}.dump"
        RESTORE_FILE="/tmp/oc_restore_${TIMESTAMP}.dump"
    else
        RESTORE_FILE="${BACKUP_DIR}/openclinica_db_${TIMESTAMP}.dump"
    fi

    # Drop existing connections and restore
    docker exec "${DB_CONTAINER}" psql -U "${DB_USER}" -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname='${DB_NAME}' AND pid <> pg_backend_pid();" 2>/dev/null || true
    docker exec "${DB_CONTAINER}" dropdb -U "${DB_USER}" --if-exists "${DB_NAME}"
    docker exec "${DB_CONTAINER}" createdb -U "${DB_USER}" "${DB_NAME}"
    docker cp "${RESTORE_FILE}" "${DB_CONTAINER}:/tmp/oc_restore.dump"
    docker exec "${DB_CONTAINER}" pg_restore -U "${DB_USER}" \
        --dbname="${DB_NAME}" \
        --jobs=4 \
        --verbose \
        "/tmp/oc_restore.dump"
    docker exec "${DB_CONTAINER}" rm -f "/tmp/oc_restore.dump"

    if [ -f "/tmp/oc_restore_${TIMESTAMP}.dump" ]; then
        rm -f "/tmp/oc_restore_${TIMESTAMP}.dump"
    fi

    echo "  Database restored successfully."
    echo ""
fi

# Restore application data
if [ "${DB_ONLY}" = false ] && [ -f "${BACKUP_DIR}/openclinica_data_${TIMESTAMP}.tar" ]; then
    echo "Step 2: Restoring application data..."
    DATA_VOLUME="oc-prod-data"
    if docker volume inspect "${DATA_VOLUME}" &>/dev/null; then
        docker run --rm \
            -v "${DATA_VOLUME}:/data" \
            -v "${BACKUP_DIR}:/backup" \
            alpine tar xf "/backup/openclinica_data_${TIMESTAMP}.tar" -C /data
        echo "  Application data restored."
    else
        echo "  WARNING: Volume ${DATA_VOLUME} not found."
    fi
fi

echo ""
echo "=== Restore Complete ==="
echo "  Restart the application to pick up the restored data:"
echo "  docker compose -f deploy/compose/docker-compose.prod.yml restart web"
