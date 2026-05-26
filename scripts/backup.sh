#!/bin/bash
# =============================================================================
# ResearchEDF — Database & Files Backup
#
# Creates a timestamped backup of PostgreSQL database and application data.
# Supports local and S3 (via aws cli) destinations.
#
# Usage:
#   bash scripts/backup.sh [--s3-bucket BUCKET] [--compress]
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "${SCRIPT_DIR}")"

TIMESTAMP=$(date -u +"%Y%m%d_%H%M%S")
BACKUP_DIR="${BACKUP_DIR:-${PROJECT_DIR}/backups}"
COMPRESS=false
S3_BUCKET=""
MODE="${OC_ENV:-production}"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --s3-bucket) S3_BUCKET="$2"; shift 2 ;;
        --compress) COMPRESS=true; shift ;;
        --dir) BACKUP_DIR="$2"; shift 2 ;;
        *) echo "Usage: $0 [--s3-bucket BUCKET] [--compress] [--dir PATH]"; exit 1 ;;
    esac
done

mkdir -p "${BACKUP_DIR}"

DB_CONTAINER="oc-prod-postgres"
DB_USER="${POSTGRES_USER:-researchedc}"
DB_NAME="${POSTGRES_DB:-researchedc}"
DATA_VOLUME="oc-prod-data"

echo "=== ResearchEDF Backup (${TIMESTAMP}) ==="
echo "  Mode:      ${MODE}"
echo "  Backup dir: ${BACKUP_DIR}"
echo ""

# Step 1: Database dump
echo "Step 1: Dumping PostgreSQL database..."
DB_FILE="${BACKUP_DIR}/researchedc_db_${TIMESTAMP}.sql"
if docker ps --format '{{.Names}}' | grep -q "^${DB_CONTAINER}$"; then
    docker exec "${DB_CONTAINER}" pg_dump \
        -U "${DB_USER}" \
        --format=custom \
        --verbose \
        --file="/tmp/oc_backup_${TIMESTAMP}.dump" \
        "${DB_NAME}"

    docker cp "${DB_CONTAINER}:/tmp/oc_backup_${TIMESTAMP}.dump" "${BACKUP_DIR}/researchedc_db_${TIMESTAMP}.dump"
    docker exec "${DB_CONTAINER}" rm -f "/tmp/oc_backup_${TIMESTAMP}.dump"
    echo "  DB backup: ${BACKUP_DIR}/researchedc_db_${TIMESTAMP}.dump"

    # Also create a readable SQL backup (schema only, no sensitive data)
    docker exec "${DB_CONTAINER}" pg_dump \
        -U "${DB_USER}" \
        --schema-only \
        --file="/tmp/oc_schema_${TIMESTAMP}.sql" \
        "${DB_NAME}"
    docker cp "${DB_CONTAINER}:/tmp/oc_schema_${TIMESTAMP}.sql" "${BACKUP_DIR}/researchedc_schema_${TIMESTAMP}.sql"
    docker exec "${DB_CONTAINER}" rm -f "/tmp/oc_schema_${TIMESTAMP}.sql"
    echo "  Schema:    ${BACKUP_DIR}/researchedc_schema_${TIMESTAMP}.sql"
else
    echo "  WARNING: Container ${DB_CONTAINER} not running. Skipping DB backup."
fi
echo ""

# Step 2: Application data files
echo "Step 2: Backing up application data..."
DATA_FILE="${BACKUP_DIR}/researchedc_data_${TIMESTAMP}.tar"
if docker volume inspect "${DATA_VOLUME}" &>/dev/null; then
    docker run --rm \
        -v "${DATA_VOLUME}:/data:ro" \
        -v "${BACKUP_DIR}:/backup" \
        alpine tar cf "/backup/researchedc_data_${TIMESTAMP}.tar" -C /data .
    echo "  Data backup: ${DATA_FILE}"
else
    echo "  WARNING: Volume ${DATA_VOLUME} not found. Skipping data backup."
fi
echo ""

# Step 3: Compress
if [ "${COMPRESS}" = true ]; then
    echo "Step 3: Compressing backups..."
    gzip "${BACKUP_DIR}/researchedc_db_${TIMESTAMP}.dump" 2>/dev/null || true
    gzip "${BACKUP_DIR}/researchedc_schema_${TIMESTAMP}.sql" 2>/dev/null || true
    gzip "${BACKUP_DIR}/researchedc_data_${TIMESTAMP}.tar" 2>/dev/null || true
    echo "  Compressed with gzip."
fi
echo ""

# Step 4: Upload to S3 (if configured)
if [ -n "${S3_BUCKET}" ]; then
    echo "Step 4: Uploading to S3..."
    aws s3 cp "${BACKUP_DIR}/researchedc_db_${TIMESTAMP}.dump" "${S3_BUCKET}/db/" || echo "  WARNING: S3 upload failed"
    aws s3 cp "${BACKUP_DIR}/researchedc_data_${TIMESTAMP}.tar" "${S3_BUCKET}/data/" || echo "  WARNING: S3 upload failed"
    echo "  Uploaded to ${S3_BUCKET}"
fi

echo ""
echo "=== Backup Complete ==="
echo "  Location: ${BACKUP_DIR}"
ls -lh "${BACKUP_DIR}/researchedc_"*"${TIMESTAMP}"* 2>/dev/null || true
echo ""
echo "To restore: bash scripts/restore.sh ${TIMESTAMP}"
