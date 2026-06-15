# Remove Legacy Code Plan

**Last updated:** 2026-06-14 (updated)
**Status:** Legacy removal is **not complete**. Tracked workflow progress is **88.1%** (848/963 artifacts removed or closed; 115 active artifacts remain). **Phase 1 web/ module DELETED** (102 Java files deleted or migrated to app/, entire web/ directory removed). **Phase 4 dead code scavenging EXHAUSTED** (73 files, -8570L across runs 93-95). **Phase 5 EXHAUSTED.** Remaining work is now concentrated in: (1) Phase 3 module-owned DAO replacement/deletion → 75 DAO/SPI/support Java files in `shared/dao`; (2) Phase 4 shared service/domain cleanup gated by those DAO replacements and import/export compatibility. Phase 3 ledger status: 759/878 methods module-backed, 878/878 module-backed or removed (100.0%), 0 fallback-SQL, 0 legacy-only, 0 adapter-gap, 0 unused (0.0%), 119 removed.

## Current Baseline

These counts come from the current repository tree and regenerated inventory (updated 2026-06-14):

| Surface | Count (before) | Count (after) | Meaning |
|---------|----------------|---------------|---------|
| `shared/src/main/java/org/researchedc` | 793 | 504 | Legacy beans, DAOs, services, entities, rules, jobs, exceptions, utilities (-289) |
| `shared/src/main/java/org/researchedc/dao` | 186 | 88 | DAO SPI interfaces plus legacy DAO implementations/support (-98) |
| `web/` | 480 | 0 | **ENTIRE DIRECTORY DELETED** — 102 files migrated to app/ or deleted (-480) |
| Legacy servlet inventory artifacts | 186 | 0 | No active servlet artifacts; `web/` is absent (-186) |
| `ws/` | 75 | 0 | SOAP module directory is absent in the current tree (-75) |
| Active legacy workflow inventory | 963 | 115 | Regenerated artifacts across DAO and shared service surfaces only (-848) |

Progress snapshot: active workflow inventory is **848/963 closed (88.1%)**; DAO method replacement/removal coverage is **878/878 (100%)**; remaining unused DAO SPI rows are **0/878 (0%)**; DAO-surface file deletion is **111/186 (59.7%)**.

### Phase 1 Deletion Summary (8 slices completed)

| Slice | Servlets Deleted | JSPs Deleted | web.xml Lines Removed |
|-------|-----------------|--------------|----------------------|
| Admin Read-Only | ~26 | ~31 | ~50 |
| CRF Metadata | 13 | 27 | 26 |
| Study/Subject/Event | 48 | 76+ | 430 |
| Export/Dataset/Filter | 9 | ~40 | 77 |
| Data Entry/Discrepancy | 19 | 17 | ~50 |
| Login Auxiliary/Enterprise/Mail Delivery | 6 | 11 | 55 |
| Webapp Static Assets/JSPs (run 96) | 0 | 1414 files | 270 |
| **TOTAL** | **~121** | **~1616** | **~958** |

**Result:** Phase 1 deletion slices removed all JSP views, static assets, GWT remnants, TLDs, tags, dead servlet registrations, and the entire `web/` directory. Needed import/validation compatibility classes were migrated into `app/` under `org.researchedc.control.*` and `org.researchedc.web.crfdata.*`. Enterprise and mail-delivery code paths are retired.

**Remaining Phase 1 work:** none in `web/`; the directory is gone. Follow-up import/export and CRF data-entry parity work now lives in app/module code and is tracked as compatibility hardening, not web deletion.

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
- `docs/refactor/legacy-workflow-inventory.csv` initially recorded 963 artifacts. The regenerated 2026-06-14 inventory now records 115 active artifacts after `web/` deletion, SOAP retirement, and Phase 3 DAO/support deletion passes.
- `docs/refactor/legacy-workflow-inventory.md` now summarizes the active inventory: 70 `replace`, 45 `keep compatibility`, and 0 `unknown` artifacts. The remaining rows include 75 live DAO Java files plus 27 shared services; regenerate the workflow inventory before using its older DAO-surface row count as a deletion metric.
- `scripts/ci/generate-legacy-report.sh` now includes the workflow inventory artifacts in the generated legacy report.
- The first low-risk Phase 1 vertical slice, `phase-1-admin-read-only`, is now closed; no active artifacts remain in the generated inventory.
- `docs/refactor/phase-1-admin-read-only-ledger.csv` maps the 51 admin read-only rows; all rows are now covered/deleted or formally retired, and the generated inventory has no active `phase-1-admin-read-only` artifacts.

Remaining Phase 0 work:

- ✅ All 7 `unknown` rows classified on 2026-06-10; 0 unknown artifacts remain in the regenerated inventory.
- ✅ `scripts/ci/generate-legacy-inventory.py` updated to classify layout fragments (`include/` JSPs) and `menu.jsp` automatically.
- ⬜ Add per-workflow owner metadata once the first slice ledger is created.

Current next action (updated 2026-06-12):

1. ✅ Done: Closed the common `EntityAction` remove/restore gaps for study-subject, study-event, and event-CRF actions.
2. ✅ Done: Import/export compatibility slice. ImportCrfDataAdapter.validateEditChecks() now covers 8 validation types (NO_BLANKS, 5 data-type, 2 response-set). ImportCRFDataServlet + import.jsp deleted.
3. ✅ Done: OpenRosa/Spring MVC compatibility classification. OpenRosa is active Modulith (18 files, `/api/v1/openrosa`). AccountController deleted (0 callers). SidebarInit/SidebarEnumConstants deleted (0 injections).
4. ✅ Done: Webapp surface cleanup — 29 JSPs, ~1400 static assets, GWT remnants, TLDs, tags, pages-servlet.xml deleted. web.xml 310→40 lines. SDVUtil bean removed from WebBeansConfig.
5. ✅ Done: web/ module DELETED — 9 needed files (Validator, DiscrepancyValidator, FormDiscrepancyNotes, Validation, EanCheckDigit, ValidatorRegularExpression, ImportCRFInfoContainer, ImportCRFInfo, ImportCRFDataService, ImportHelper) migrated to app/, 93 dead legacy servlets/views/helpers deleted. Entire web/ directory removed. JSP/JSTL dependencies cleaned from app/pom.xml.
6. ⬜ Phase 3 DAO deletion: 5 dead files deleted (ScheduledJobSort, OCContextLoaderListener, SubjectGroupMapDao, OpenClinicaVersionDAO + SPI). 88 remaining DAO files are blocked by final deletion proof: 759/878 methods are module-backed, 0 unused SPI rows remain, 119 rows are removed, and 0 fallback-SQL/legacy-only/adapter-gap rows remain. This slice also deleted 52 dead legacy rule engine files (RuleSetService, BeanPropertyService, ExpressionService, rule runners, expression tree, action processors, validators, helper DTOs — 12,461 lines) and removed all 9 remaining unused SPI default methods. Phase 3 SPI method coverage is now 100%. Next: prove DAO implementation/support file registration/factory/inheritance/runtime dependencies are safe before deletion.
7. ✅ Done: Phase 4 shared bean deletion — EXHAUSTED. 73 files (-8570L) across runs 81-95. 0 dead code remaining.
8. ✅ Done: Phase 5 dependency cleanup — EXHAUSTED. 19 dead deps removed; remaining 8 all active.
9. ✅ Initial import/export compatibility hardening complete in commit `bc1f24d97`: focused dataimport/legacy bridge/data-capture tests, typed validation preview/result output, commit audit event, result stats, and secure attachment download keyed by event CRF plus opaque attachment ids. Rollback proof was added after commit `ae72d2415`; remaining compatibility work: deterministic ODM preview validation fixtures; representative ODM parse, OpenRosa submission form-context, and export API contract coverage added; rule XML import is retired and guarded against reintroduction; legacy import job scheduling is retired in the current tree.

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
| `components/questionnaire-builder/QuestionnaireBuilder.tsx` | Visual questionnaire builder | Wire as route `questionnaires/builder` |
| `components/DiscrepancyNotes.tsx` | Discrepancy note CRUD component | Either give dedicated page route or keep inline only |

> **Note:** `RuleSetDetail.tsx` was listed here but is already wired as `studies/:studyId/rules/:ruleSetId` (router line 96) with full rule CRUD (create/add/remove modals, 380 lines). The plan document was outdated.

#### SPA Action Fallbacks

| SPA Page | Action Target | Coverage Status |
|----------|----------------|-----------------|
| `EntityAction` | `/app/actions/*` to module REST | Study-subject, study-event, and event-CRF remove/restore now covered; keep remaining unsupported entity types gated |

> **Note:** `SignStudySubject`, `ReassignStudySubject`, and `CreateNewStudyEvent` fallbacks were previously listed here.
> - `SignStudySubject` — resolved in run-63: `POST /api/v1/subjects/{id}/sign` endpoint added (commit `c5af47499`)
> - `ReassignStudySubject` — resolved in run-63: reassign path fixed (`/{id}/reassign`, commit `c5af47499`)
> - `CreateNewStudyEvent` — resolved in run-48: servlet deleted, SPA SubjectDetail handles event creation via `scheduleEvent` mutation

#### Blocking Workflows: Remaining Legacy JSPs/Servlets by Category

These are the legacy artifacts that have **no SPA replacement** and are blocking further deletion:

| Category | Active Inventory | Key Missing SPA/Module Features |
|----------|------------------|---------------------------------|
| **CRF metadata boundary** | 11 artifacts | CRF section viewer plus active data-entry rendering dependencies; 2 orphaned rows dropped from regenerated inventory. |
| **Data Entry / Discrepancy** | 26 artifacts plus CRF rendering dependencies | Full CRF rendering (sections, items, repeating groups), double data entry mode, discrepancy note workflow, rule execution during data entry, CRF print view, file upload/download on CRFs |
| **Import/Export Compatibility** | 10 artifacts | Initial SPA/API upload → validate → commit path is hardened; remaining blocker is broader ODM/OpenRosa/export contract coverage; rule XML import is retired and guarded against reintroduction; legacy import job scheduling is retired in the current tree |
| **Study/Subject/Event Fallbacks** | 22 artifacts | Remaining legacy route fallbacks, subject/event actions, and Spring MVC compatibility routes |
| **Layout/Common** | 6 artifacts | Shared JSP fragments used by remaining JSP pages — delete last |
| **OpenRosa/Spring MVC** | Included in 15 Spring MVC route artifacts | Spring MVC controllers with legacy shared dependencies need Modulith migration or formal compatibility gates |

#### SPA Migration Priority (recommended order)

Based on risk, effort, and dependency chain:

1. **Phase 3 DAO replacement ledger** — Current executable slice. The ledger exists; continue reducing `fallback-sql`, `legacy-only`, and `adapter-gap` rows with module-owned repository/service behavior and focused tests, then delete only proven dead DAO implementation/support files.
2. **OpenRosa/Spring MVC compatibility classification** — Separate public compatibility contracts from routes that can move behind module-owned services/adapters.
3. **CRF metadata/data-entry renderer** — Keep `CheckCRFLocked`, `showItemInput*`, `generate*`, section/item/group JSP fragments, and discrepancy/data-entry JSPs blocked until SPA `DataEntryPage` proves full CRF rendering, double data entry, discrepancy notes, rule execution, print/export, and remaining file-attachment parity beyond the hardened compatibility download path.
4. **Inventory and plan hygiene** — Keep `legacy-workflow-inventory.{csv,md}` and handoff docs aligned with each committed slice; Phase 0 unknown classification is currently closed at 0 unknown artifacts.
5. **Entity Action residuals** — Common study-subject, study-event, and event-CRF remove/restore is covered by the 2026-06-11 slice. Only reopen for concrete unsupported entity types found by inventory or route testing.
6. **Layout/Common** — Delete only after all JSP pages and include references are migrated.

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

**Goal:** keep SOAP retired in the current tree, or reduce any future compatibility surface to a separately versioned adapter.

- Current baseline: `ws/` is absent and the generated inventory has no active SOAP endpoints.
- Provide REST/module API replacements or formally deprecate the endpoint.
- Replace adapter calls that still rely on legacy DAOs with module service ports.
- Add contract tests for retained compatibility endpoints.
- Remove SOAP schema/build dependencies only after no endpoint remains.

**Exit gate:** `ws/` remains absent, or only an explicit compatibility module exists with no dependency on legacy DAOs/services.

## Phase 3: DAO Implementation Deletion

**Goal:** delete legacy DAO implementation classes from `shared/dao`.

Current ledger state (updated 2026-06-15):

| Status | Methods | Meaning |
|---|---:|---|
| `module-backed` | 759 | Adapter/repository/service path exists; still requires registration and caller checks before implementation deletion |
| `unused` | 0 | No unused SPI methods remain |
| `removed` | 119 | SPI interface and implementation deleted; legacy service references cleaned up |

For each DAO family:

- List every SPI method.
- Mark each method as `module-backed`, `fallback-sql`, `legacy-only`, `adapter-gap`, or proven unused.
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

**Status: EXHAUSTED (2026-06-11, runs 81-95)**

**Goal:** remove legacy DTOs, services, rule helpers, jobs, and OpenClinica-era utility code that no longer has callers.

- Delete DTO beans after the last servlet/DAO caller is gone; SOAP is already absent in the current tree.
- Move business rules needed by the SPA/modules into module-owned services.
- Retire Quartz jobs or rewrite them against module services.
- Replace OpenClinica-specific exception/utility types where they leak into public APIs.
- Keep schema migration files and historical data compatibility code unless a migration policy explicitly allows archival.

**Exit gate:** `shared/` contains only genuinely shared, modernized code or is split into smaller module-owned packages.

### Phase 4 Progress (2026-06-11, runs 81-95)

**Conclusively exhausted.** All 509 remaining shared Java files have at least one active caller:
- Runs 81-82: 8 dead DTOs in bean/login/ deleted (-513L)
- Run 93: XmlSchemaValidationHelper deleted (-175L)
- Run 94: UserDTO + HideCRFManager ×2 deleted (-418L)
- Run 95: GenerateClinicalDataService deleted
- 47 additional dead web/ classes deleted (spreadsheet validators, admin stats, Preview, JAX-RS, job helpers, etc.)
- Run 95 final scan: **0 dead classes found** across shared/ and web/. Every remaining file has verifiable callers.

No more low-risk Phase 4 deletions possible. Remaining 509 shared files are either: (a) actively used by legacy servlets, (b) referenced by module adapters, (c) Spring infrastructure, or (d) entity/DAO/domain classes gated on Phase 3.

## Phase 5: Dependency And Build Cleanup

**Status: EXHAUSTED (2026-06-08)**

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
- retired ws/pom.xml surface: POI, Commons Math3, Commons Collections4, Commons Validator, Commons FileUpload, Commons IO, JDOM2, Jakarta Mail, Joda Time, Commons Lang3, ByteBuddy (11)
- parent pom.xml + research-edc-bom/pom.xml: jxl.version, sitemesh, jersey, jdom2, stax-ex, commons-logging, commons-beanutils-core properties and dependency management entries

**Cannot remove yet (still actively used):**
- JMesa — 252 imports in codebase (table rendering)
- Castor — ~30 imports (import/export XML marshalling)
- JSON-Lib — 35 imports (2 files: JSONClinicalDataPostProcessor, MetadataCollectorResource)
- OpenPDF — **CONFIRMED DEAD** (0 Java imports; DownloadDiscrepancyNote deleted, no POM reference found — run-81 audit)
- Saxon — 1 import (InputWidget, uses javax.xml.transform — standard JDK)
- Rome RSS — **CONFIRMED DEAD** (0 Java imports; RssReaderServlet deleted in Phase 1, no POM reference found — run-81 audit)
- Quartz — 148 imports (job scheduling)
- Commons Digester — 2 imports (DAODigester)
- Old commons-dbcp — 7 imports (ExtendedBasicDataSource, controllers)
- JSTL — 575 JSP references

**Exit gate:** Maven module `web` is removed or no longer contains legacy runtime dependencies; `ws` remains absent.

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

- `web/` directory is deleted. ✅ (2026-06-12)
- `ws/` remains absent or is explicitly retained as a non-legacy compatibility adapter. ✅
- `shared/dao` legacy implementations are removed or replaced by module-owned ports with no legacy SQL delegation.
- Legacy-only beans/services/jobs/utilities in `shared/` have no callers and are deleted.
- Legacy-only dependencies are removed from Maven.
- Full backend, frontend, questionnaire, and E2E verification passes.
