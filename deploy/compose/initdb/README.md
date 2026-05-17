# Database Initialization for Docker

## Development (fresh database)

For a fresh development database, Liquibase migrations will auto-apply on
first application startup. No manual init needed.

The PostgreSQL container creates the database and user from env vars:
`POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_DB`.

## Restoring from a backup

To restore an existing OpenClinica database into the Docker PostgreSQL:

### Option A: Auto-restore via initdb

Place your SQL dump in this directory:
```bash
cp /path/to/openclinica_dump.sql deploy/compose/initdb/
```

It must be named with `.sql` or `.sql.gz` extension to auto-execute on
first container startup.

### Option B: Manual restore (container already running)

```bash
# Copy dump into container
docker cp openclinica_dump.sql oc-postgres:/tmp/

# Restore
docker exec -i oc-postgres psql -U openclinica -d openclinica < openclinica_dump.sql
```

### Option C: Manual restore with docker-compose

```bash
# Start postgres first
docker compose -f deploy/compose/docker-compose.dev.yml up -d postgres

# Wait for healthy, then restore
docker exec -i oc-postgres psql -U openclinica -d openclinica < openclinica_dump.sql

# Then start the app
docker compose -f deploy/compose/docker-compose.dev.yml up -d web ws
```

## Schema Validation

After restoring a backup, validate that Hibernate entity mappings match the schema:

```bash
# Start with validation mode
OC_HIBERNATE_DDL_AUTO=validate docker compose -f deploy/compose/docker-compose.dev.yml up web

# Or use the validation script
bash scripts/db-schema-validate.sh
```

## Creating an anonymized backup

For development/testing, create an anonymized dump from production:

```bash
# Export schema only (no data)
pg_dump -U openclinica -s openclinica > openclinica_schema.sql

# Export with anonymized data (customize the anonymization queries)
pg_dump -U openclinica --data-only openclinica \
  --exclude-table=audit_* \
  --exclude-table=user_account \
  > openclinica_data_anon.sql

# See scripts/db-init-schema.sh for automated workflow
```
