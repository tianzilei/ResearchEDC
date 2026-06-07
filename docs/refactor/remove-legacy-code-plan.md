# Remove Legacy Code Plan

**Last updated:** 2026-06-07  
**Status:** Legacy removal is **not complete**. This plan tracks the remaining deletion work after `legacy-core/` consolidation and DAO SPI widening.

## Current Baseline

These counts come from the current repository tree on 2026-06-07:

| Surface | Count | Meaning |
|---------|-------|---------|
| `shared/src/main/java/org/researchedc` | 793 Java files | Legacy beans, DAOs, services, entities, rules, jobs, exceptions, utilities |
| `shared/src/main/java/org/researchedc/dao` | 182 files | DAO SPI interfaces plus legacy DAO implementations |
| `web/src/main/java` | 484 Java files | Legacy servlet/Spring MVC/JSP helper surface |
| `web/src/main/webapp/**/*.jsp` | 419 files | JSP views and fragments |
| `SecureController`/`CoreSecureController` subclasses | 189 matches | Legacy servlet workflows still present |
| `ws/src/main/java` | 75 Java files | SOAP endpoints, validators, adapters, SOAP support |

Completed work should be described precisely:

- `legacy-core/` was consolidated into `shared/`.
- `DaoProvider` was removed.
- Direct `new XxxDAO(...)` / `new StudyConfigService(...)` construction has been eliminated from active consumers.
- Target DAO families have been SPI-widened.
- Module-owned repositories and adapters exist for the main domains.

None of the above means legacy code is removed. It means the code is better contained and ready for deletion once replacement coverage is proven.

## Removal Principles

1. Delete only after replacement behavior is proven by tests and route/API usage.
2. Do not remove JSP/servlet code only because a React page exists; verify navigation, permissions, audit, validation, import/export behavior, and rollback path.
3. Do not delete a DAO implementation until every SPI method it provides is backed by module-owned repository/service code or is proven unused.
4. Prefer deleting whole workflows over deleting isolated helper classes.
5. Keep OpenClinica-derived ODM compatibility code until import/export contracts are explicitly replaced or versioned.

## Phase 0: Inventory And Gates

**Goal:** make deletion measurable and prevent accidental claims of completion.

- Create a generated inventory of all legacy routes, servlets, JSPs, SOAP endpoints, DAO implementations, Quartz jobs, and shared services.
- Map every legacy route to one of: `replace`, `retire`, `keep compatibility`, or `unknown`.
- Add CI guard scripts for:
  - `DaoProvider.getDao` remains 0.
  - direct `new XxxDAO(...)` remains 0 outside implementation boundaries.
  - no new `SecureController` subclass is added.
  - no new JSP file is added.
- Record current counts in release notes whenever the baseline changes.

**Exit gate:** every legacy artifact has an owner category and deletion gate.

### Phase 0 Status

Completed on 2026-06-07:

- `scripts/ci/legacy-baseline.sh` generates Markdown and JSON counts for the current legacy surface.
- `scripts/ci/check-legacy-guardrails.sh` enforces the zero-regression checks for `DaoProvider`, direct DAO construction, public module legacy imports, new JSPs, and new legacy servlet subclasses.
- `scripts/ci/check-phase-b-migrations.sh` statically verifies expected Phase B trigger migration files are present, registered in `release.xml`, and include trigger/function definitions plus recursion guards.
- Backend and legacy-report GitHub workflows run the guardrail scripts.
- `scripts/ci/generate-legacy-report.sh` includes the generated baseline artifact.

Remaining Phase 0 work:

- Build the legacy route/workflow inventory and classify each artifact as `replace`, `retire`, `keep compatibility`, or `unknown`.
- Add owner/deletion-gate metadata for each workflow slice.

Current next action:

1. Generate the route/workflow inventory for `web/` servlets, JSPs, Spring MVC controllers, SOAP endpoints, DAO implementations, Quartz jobs, and shared services.
2. Tag each artifact with `replace`, `retire`, `keep compatibility`, or `unknown`.
3. Pick the first low-risk Phase 1 vertical slice, preferably admin read-only pages, and define its route parity, permission parity, audit parity, and deletion proof.

## Phase B PostgreSQL Validation

Status: **complete on 2026-06-07** against disposable PostgreSQL databases.

Validated sequence:

1. Created disposable PostgreSQL databases owned by the `researchedc` role.
2. Applied Liquibase from `shared/src/main/resources/migration/master.xml`.
3. Verified expected `sync_*` functions and `trg_sync_*` triggers for the 12 Phase B migration files checked by `scripts/ci/check-phase-b-migrations.sh`.
4. Tested bidirectional sync for representative low-risk domains:
   - `study` <-> `module_study`
   - `filter` <-> `module_filter`
   - `discrepancy_note` <-> `module_discrepancy_note`
5. Verified legacy-to-module and module-to-legacy insert/update/delete propagation for each tested pair.
6. Confirmed repeated bidirectional updates converge without recursive trigger loops.
7. Added `scripts/ci/check-phase-b-postgres.sh`, gated by explicit database environment variables so it only runs when a disposable DB is available.

Validation uncovered and fixed a discrepancy-note migration bug: the legacy `discrepancy_note` table does not have `entity_id`, so legacy-to-module sync now stores `NULL AS entity_id` and module-to-legacy sync no longer writes that column.

Recorded in commit `0963eec2c`.

Exit gate:

- Static migration checks pass: `bash scripts/ci/check-phase-b-migrations.sh`.
- Liquibase applies cleanly to PostgreSQL from `shared/src/main/resources/migration/master.xml`.
- Bidirectional sync is proven for representative low-risk domains.
- A repeatable PostgreSQL validation script exists for future Phase B changes: `scripts/ci/check-phase-b-postgres.sh`.

## Phase 1: Web/JSP Workflow Deletion

**Goal:** remove `web/` workflows in vertical slices.

Recommended order:

1. Admin read-only pages: logs, status, configuration, audit browsing.
2. CRF metadata administration.
3. Study/subject/event management.
4. Export/dataset/filter workflows.
5. Data entry and discrepancy note workflows.
6. Import and rule verification workflows.
7. Login/profile/password JSPs and remaining layout/include fragments.

For each slice:

- Confirm the SPA route is the default navigation path.
- Confirm the backend route either redirects to SPA or is no longer registered.
- Compare permissions with `SecureController.mayProceed()` behavior.
- Compare audit output for create/update/delete actions.
- Run module tests, servlet tests for still-present code, frontend typecheck/tests, and relevant E2E path.
- Delete servlet/controller/JSP/helper files only after no route, include, test, or config references remain.

**Exit gate:** `web/src/main/webapp/**/*.jsp` count is 0 and no production servlet extends `SecureController` or `CoreSecureController`.

## Phase 2: SOAP Retirement

**Goal:** remove `ws/` or reduce it to a separately versioned compatibility adapter.

- Identify consumers for each SOAP endpoint.
- Provide REST/module API replacements or formally deprecate the endpoint.
- Replace adapter calls that still rely on legacy DAOs with module service ports.
- Add contract tests for retained compatibility endpoints.
- Remove SOAP schema/build dependencies only after no endpoint remains.

**Exit gate:** `ws/` is deleted, or only an explicit compatibility module remains with no dependency on legacy DAOs/services.

## Phase 3: DAO Implementation Deletion

**Goal:** delete legacy DAO implementation classes from `shared/dao`.

For each DAO family:

- List every SPI method.
- Mark each method as `module-backed`, `unused`, or `legacy-only`.
- Replace `legacy-only` behavior with a module service/repository or retire the caller.
- Remove adapter delegation to parent legacy SQL.
- Delete the DAO implementation class only when no Spring bean registration, factory method, inheritance relationship, test, or runtime path needs it.

High-risk DAO groups:

- CRF/data capture DAOs: complex form metadata, item data, response sets, ODM export dependencies.
- Study event DAOs: scheduling/status transitions and discrepancy note relationships.
- Rule DAOs: rule import, expression evaluation, and action execution.
- Extract/dataset/filter DAOs: export formats and historical compatibility.

**Exit gate:** `shared/src/main/java/org/researchedc/dao` contains only active module-owned SPI/port interfaces, or is deleted entirely.

## Phase 4: Shared Bean/Service/Domain Deletion

**Goal:** remove legacy DTOs, services, rule helpers, jobs, and OpenClinica-era utility code that no longer has callers.

- Delete DTO beans after the last servlet/SOAP/DAO caller is gone.
- Move business rules needed by the SPA/modules into module-owned services.
- Retire Quartz jobs or rewrite them against module services.
- Replace OpenClinica-specific exception/utility types where they leak into public APIs.
- Keep schema migration files and historical data compatibility code unless a migration policy explicitly allows archival.

**Exit gate:** `shared/` contains only genuinely shared, modernized code or is split into smaller module-owned packages.

## Phase 5: Dependency And Build Cleanup

**Goal:** remove libraries kept only for legacy runtime.

Candidates after prior phases:

- JSP/Sitemesh/JSTL/JMesa dependencies.
- SOAP/JAXB plugin configuration not needed by retained APIs.
- Castor/OpenClinica ODM dependencies if export/import has module-owned replacements.
- JExcel/legacy POI paths after export migration.
- Legacy Spring MVC/security compatibility shims.

**Exit gate:** Maven modules `web` and `ws` are removed or no longer contain legacy runtime dependencies.

## Verification Commands

Run these after each deletion slice:

```bash
git status --short
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl app -am
cd frontend && pnpm typecheck && pnpm test --run
```

Use targeted E2E/curl checks for the workflow being removed.

## Done Definition

Legacy code removal is complete only when all are true:

- `web/` has no JSPs, no `SecureController`/`CoreSecureController` production subclasses, and no legacy servlet registrations.
- `ws/` is removed or explicitly retained as a non-legacy compatibility adapter.
- `shared/dao` legacy implementations are removed or replaced by module-owned ports with no legacy SQL delegation.
- Legacy-only beans/services/jobs/utilities in `shared/` have no callers and are deleted.
- Legacy-only dependencies are removed from Maven.
- Full backend, frontend, questionnaire, and E2E verification passes.
