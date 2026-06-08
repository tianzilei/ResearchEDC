# Remove Legacy Code Plan

**Last updated:** 2026-06-09
**Status:** Legacy removal is **not complete**. This plan tracks the remaining deletion work after `legacy-core/` consolidation, DAO SPI widening, and 6 Phase 1 deletion slices. SPA migration coverage mapping added 2026-06-08.

## Current Baseline

These counts come from the current repository tree (updated 2026-06-09 after Phase 1 login auxiliary, Enterprise, and mail-delivery cleanup):

| Surface | Count (before) | Count (after) | Meaning |
|---------|----------------|---------------|---------|
| `shared/src/main/java/org/researchedc` | 793 | 713 | Legacy beans, DAOs, services, entities, rules, jobs, exceptions, utilities (-80) |
| `shared/src/main/java/org/researchedc/dao` | 186 | 175 | DAO SPI interfaces plus legacy DAO implementations (-11 old DataSource-constructor DAOs) |
| `web/src/main/java` | 480 | 263 | Legacy servlet/Spring MVC/JSP helper surface (-217) |
| `web/src/main/webapp/**/*.jsp` | 416 | 175 | JSP views and fragments (-241) |
| `SecureController`/`CoreSecureController` subclasses | 186 | 87 | Legacy servlet workflows still present (-99) |
| `ws/` | 75 | 0 | SOAP module directory is absent in the current tree (-75) |

### Phase 1 Deletion Summary (6 slices completed)

| Slice | Servlets Deleted | JSPs Deleted | web.xml Lines Removed |
|-------|-----------------|--------------|----------------------|
| Admin Read-Only | ~25 | ~30 | ~50 |
| CRF Metadata | 13 | 27 | 26 |
| Study/Subject/Event | 48 | 76+ | 430 |
| Export/Dataset/Filter | 9 | ~40 | 77 |
| Data Entry/Discrepancy | 19 | 17 | ~50 |
| Login Auxiliary/Enterprise/Mail Delivery | 6 | 11 | 55 |
| **TOTAL** | **~120** | **~201** | **~688** |

**Result:** Phase 1 deletion slices have removed the largest low-risk JSP/servlet surfaces. Current Enterprise and mail-delivery code paths are retired; shared+app+web BUILD SUCCESS.

**Remaining Phase 1 work:** ~175 JSPs and ~87 SecureController subclasses remain, blocked by data entry, import, password/admin-user, study-management fallbacks, and OpenRosa workflows that require SPA or module-owned replacements before deletion.

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

Completed on 2026-06-07 after Phase B validation:

- `scripts/ci/generate-legacy-inventory.py` generates CSV and Markdown inventories for legacy servlets, JSPs, Spring MVC routes, SOAP endpoints, DAO files, Quartz jobs, and shared services.
- `docs/refactor/legacy-workflow-inventory.csv` initially recorded 963 artifacts with `replace`, `keep compatibility`, or `unknown` classifications and deletion gates; after deleting `ReportController`, the active inventory records 959 artifacts.
- `docs/refactor/legacy-workflow-inventory.md` now summarizes the active inventory: 764 `replace`, 125 `keep compatibility`, and 70 `unknown` artifacts.
- `scripts/ci/generate-legacy-report.sh` now includes the workflow inventory artifacts in the generated legacy report.
- The first low-risk Phase 1 vertical slice is selected: `phase-1-admin-read-only`, documented in `docs/refactor/phase-1-admin-read-only-slice.md`.
- `docs/refactor/phase-1-admin-read-only-ledger.csv` maps the 51 admin read-only rows: 1 `deleted`, 3 `covered`, 45 `needs replacement`, and 2 `blocked`.

Remaining Phase 0 work:

- Reduce the 70 `unknown` inventory rows by assigning owners/categories in follow-up slice ledgers.
- Add per-workflow owner metadata once the first slice ledger is created.

Current next action:

1. Continue audit rows: compare legacy JSP/servlet filters and fields against `/api/v1/audit` and `/app/admin/audit-log`; move rows from `needs replacement` to `covered` only with permission and output parity proof.
2. For system/log/job rows, add or identify module-owned backend APIs before deleting JSP/servlet paths.
3. Delete only rows marked `covered` or `retire`, one workflow at a time, after route, permission, audit/status/log, and reference parity are proven.

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

### Phase 1 SPA Migration Coverage

**Goal:** map every remaining legacy workflow to its SPA replacement status, so deletion slices can proceed with confidence.

#### Existing SPA Routes (44 paths, 38 distinct page components)

SPA route definitions live in `frontend/src/router/index.tsx` (React Router 7, `createBrowserRouter`). All authenticated routes are wrapped in `<ProtectedRoute>` → `<AppLayout>`.

| SPA Route | Page Component | Replaces Legacy |
|-----------|---------------|----------------|
| `/app/dashboard` | `Dashboard` | Home page, study/site selector, module cards |
| `/app/studies` | `StudyList` | study list + site tabs |
| `/app/studies/create` | `StudyWizard` | create study workflow |
| `/app/studies/:id` | `StudyDetail` | study detail view |
| `/app/studies/:id/edit` | `StudyEditor` | study metadata editing |
| `/app/studies/:id/sites` | `SiteManagement` | site CRUD |
| `/app/studies/:studyId/event-definitions` | `EventDefinitionsPage` | event definition management |
| `/app/studies/:studyId/subject-groups` | `SubjectGroupsPage` | subject group CRUD |
| `/app/studies/:studyId/rules` | `RulesListPage` | rule set listing |
| `/app/subjects` | `SubjectList` | subject list with create + enroll |
| `/app/subjects/:id` | `SubjectDetail` | subject profile + events table |
| `/app/subjects/:subjectId/events` | `EventList` | event schedule/list/complete |
| `/app/subjects/:subjectId/events/:eventId/crfs/:eventCrfId/entry` | `DataEntryPage` | CRF data entry (basic) |
| `/app/crfs` | `CrfList` | CRF library |
| `/app/crfs/:versionId` | `CrfPreview` | CRF content preview |
| `/app/crfs/:crfId/versions` | `CrfVersionManager` | CRF version history |
| `/app/randomization` | `RandomizationDashboard` | randomization overview |
| `/app/randomization/schemes/:id` | `SchemeEditor` | scheme design |
| `/app/randomization/schemes/:id/allocate` | `AllocationPage` | subject allocation |
| `/app/randomization/schemes/:id/unblind` | `UnblindingPage` | emergency unblinding |
| `/app/randomization/schemes/:id/audit` | `AuditViewer` | randomization audit trail |
| `/app/data-export` | `ExportCenter` | export job management |
| `/app/data-export/datasets` | `DatasetBuilder` | dataset creation |
| `/app/data-export/filters` | `FilterBuilder` | filter creation |
| `/app/questionnaires/templates` | `QuestionnaireTemplates` | template library |
| `/app/questionnaires/templates/:templateId/versions` | `QuestionnaireVersionEditor` | version management |
| `/app/questionnaires/assignments` | `QuestionnaireAssignments` | subject assignment |
| `/app/questionnaires/responses` | `QuestionnaireResponses` | response browser |
| `/app/questionnaires/export` | `QuestionnaireExport` | response export |
| `/app/questionnaires/my-tasks` | `QuestionnaireMyTasks` | assigned questionnaires |
| `/app/admin` | `AdminDashboard` | admin navigation hub |
| `/app/admin/users` | `UserManagement` | user CRUD + role assignment |
| `/app/admin/audit-log` | `AuditLogViewer` | global audit log |
| `/app/admin/system` | `SystemConfiguration` | health/version/config |
| `/app/admin/crf-library` | `CrfAdmin` | CRF admin with version links |
| `/app/admin/jobs` | `JobManager` | Quartz job management |
| `/app/admin/import` | `ImportManager` | import upload (basic) |
| `/app/admin/password-policy` | `PasswordPolicy` | password requirements config |
| `/app/admin/logs` | `LogViewer` | application log viewer |
| `/app/admin/studies/:studyId/users` | `StudyUserRoleEditor` | per-study user role editing |
| `/app/profile` | `Profile` | user profile |
| `/app/instructions`, `/app/instructions/:topic` | `Instructions` | workflow instructions |
| `/app/actions/:entity/:action/:id` | `EntityAction` | remove/restore actions |
| `/app/login` | `Login` | username/password login |
| `/q/fill/:token` | `QuestionnaireFill` | public questionnaire fill |

**Plus:** `/app/legacy/*` → `LegacyFrame` (iframe) for any unmigrated workflow.

#### Orphan Components (built but NOT routed)

| Component File | What It Does | Action Needed |
|---------------|-------------|---------------|
| `pages/rules/RuleSetDetail.tsx` | Rule set detail/edit page | Wire as route `studies/:studyId/rules/:ruleSetId` |
| `components/questionnaire-builder/QuestionnaireBuilder.tsx` | Visual questionnaire builder | Wire as route `questionnaires/builder` |
| `components/DiscrepancyNotes.tsx` | Discrepancy note CRUD component | Either give dedicated page route or keep inline only |

#### SPA → Legacy Fallbacks (SPA pages that still open legacy JSPs)

| SPA Page | Fallback Target | Workflow Missing |
|----------|----------------|-----------------|
| `SubjectDetail` | `/legacy/SignStudySubject` | E-signature capture |
| `SubjectDetail` | `/legacy/ReassignStudySubject` | Subject reassignment |
| `SubjectDetail` | `/legacy/CreateNewStudyEvent` | Study event creation wizard |
| `EntityAction` | `/legacy/Remove*` / `/legacy/Restore*` | Remove/restore for unsupported entity types |

#### Blocking Workflows: Remaining Legacy JSPs/Servlets by Category

These are the legacy artifacts that have **no SPA replacement** and are blocking further deletion:

| Category | JSPs | SecureControllers | Key Missing SPA Features |
|----------|------|-------------------|-------------------------|
| **Data Entry** | ~66 | ~26 | Full CRF rendering (sections, items, repeating groups), double data entry mode, discrepancy note workflow, rule execution during data entry, CRF print view, file upload/download on CRFs |
| **Import** | 8 | 7 | Step-by-step import wizard (upload → validate → map → commit), rule XML import, import job scheduling |
| **Login Auxiliary** | 0 | 0 | Completed on 2026-06-09: legacy ChangeStudy, Enterprise, RequestAccount, RequestStudy, Contact, and UpdateProfile JSP/servlet paths deleted; old routes now redirect to SPA where retained. |
| **Profile/Password** | 5 | 3 | Login, logout, forgot-password, reset-password, and admin password policy/user-account JSPs remain; email field removal is tracked separately. |
| **OpenRosa** | 0 | 0* | Spring MVC controller with legacy shared dependencies — needs modulith migration |
| **Layout/Common** | 60 | — | Shared JSP fragments (headers, footers, sidebars, table renderers, navigation) — delete last |

\*OpenRosa uses Spring MVC `@Controller`, not `SecureController`, but depends on legacy `web/` infrastructure.

#### SPA Migration Priority (recommended order)

Based on risk, effort, and dependency chain:

1. **Password and admin-user cleanup** (low effort, 5 login JSPs, 3 login servlets plus admin user pages) — Keep Spring Security behavior intact while retiring forgot/reset password JSPs and password policy screens.
2. **Email field removal** — Execute `docs/refactor/phase-1-email-field-removal-plan.md`: remove user-account and study-contact email fields only after compatibility review.
3. **Entity Action completeness** — Extend `EntityAction` page to handle all entity types, removing legacy `/legacy/Remove*`/`/legacy/Restore*` fallbacks.
4. **Subject Detail fallbacks** — Replace `/legacy/SignStudySubject` (e-signature), `/legacy/ReassignStudySubject`, `/legacy/CreateNewStudyEvent` with SPA-native components.
5. **Rule editing** — Wire orphaned `RuleSetDetail.tsx` route; add rule creation/editing form.
6. **Import** (moderate effort, 8 JSPs, 7 servlets) — SPA `ImportManager` exists but is basic. Build step-by-step import wizard with validation preview.
7. **OpenRosa migration** (moderate effort, 0 JSPs) — Extract `OpenRosaSubmissionController` and its pform helpers into a Modulith module with module-owned DAO access.
8. **Data Entry** (high effort, ~66 JSPs, ~26 servlets) — The critical path. SPA `DataEntryPage` exists but is thin. Requires: full CRF section/item/group rendering, double data entry mode, discrepancy notes inline, rule execution UI, CRF print/export, file attachments.
9. **Layout/Common** — Delete only after ALL JSP pages are migrated.

#### Feature Flag System (available for gated rollout)

The SPA has a feature flag system at `hooks/useFeatureFlags.ts` backed by a JSONB column on `study` table:
- API: `GET/PUT /api/v1/studies/:id/feature-flags`
- Hook: `useFeatureFlags()`, `useUpdateFeatureFlags()`
- Current default flags: none defined (`DEFAULT_FLAGS = {}`)

**Recommended migration flags to add:**
```
spa_data_entry        — Use SPA DataEntryPage instead of legacy JSP data entry
spa_e_signature       — Use SPA e-signature instead of legacy SignStudySubject
spa_import            — Use SPA import wizard instead of legacy import JSPs
spa_subject_detail    — Use full SPA subject detail (no legacy fallbacks)
```

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

### Phase 5 Progress (2026-06-08)

**Removed dependencies (19 total):**
- web/pom.xml: XOM, EZMorph, spring-security-acl, Jersey (3 deps), jdom2 (6)
- shared/pom.xml: JExcel (1)
- ws/pom.xml: POI, Commons Math3, Commons Collections4, Commons Validator, Commons FileUpload, Commons IO, JDOM2, Jakarta Mail, Joda Time, Commons Lang3, ByteBuddy (11)
- parent pom.xml + research-edc-bom/pom.xml: jxl.version, sitemesh, jersey, jdom2, stax-ex, commons-logging, commons-beanutils-core properties and dependency management entries

**Cannot remove yet (still actively used):**
- JMesa — 252 imports in codebase (table rendering)
- Castor — ~30 imports (import/export XML marshalling)
- JSON-Lib — 35 imports (2 files: JSONClinicalDataPostProcessor, MetadataCollectorResource)
- OpenPDF — 1 import (DownloadDiscrepancyNote)
- Saxon — 1 import (InputWidget)
- Rome RSS — 9 imports (RssReaderServlet)
- Quartz — 148 imports (job scheduling)
- Commons Digester — 2 imports (DAODigester)
- Old commons-dbcp — 7 imports (ExtendedBasicDataSource, controllers)
- JSTL — 575 JSP references

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
