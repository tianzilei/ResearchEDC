-- =============================================================================
-- ResearchEDF — PostgreSQL Initial Schema
--
-- This script runs on first container startup (PostgreSQL initdb).
-- It creates the minimal schema objects needed for Liquibase to run.
-- The full schema is managed by Liquibase migrations in legacy-core/src/main/resources/migration/.
--
-- To restore from a production backup instead:
--   1. Place the dump file at ./initdb/researchedc_dump.sql
--   2. It will auto-execute if named *.sql or *.sql.gz
--   3. Or restore manually:
--      docker exec -i oc-postgres psql -U researchedf -d researchedf < dump.sql
-- =============================================================================

-- Liquibase tracking tables are created by Liquibase itself.
-- No manual schema creation needed — everything comes from migration scripts.

-- However, for dev environments without an existing DB, we create a minimal
-- marker so the application can start and Liquibase can apply migrations.

-- Create the OC schema if using schema-based isolation (not typically used for PG)
-- CREATE SCHEMA IF NOT EXISTS researchedc;
-- SET search_path TO researchedc;

-- Quartz scheduler tables are created via Liquibase migrations (oc_qrtz_* tables).
-- They are defined in legacy-core/src/main/resources/migration/.
