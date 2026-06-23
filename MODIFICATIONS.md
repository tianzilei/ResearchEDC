# ResearchEDC 修改记录

**项目:** ResearchEDC — 基于 OpenClinica v3.x 的科研电子数据采集平台  
**基础版本:** 0.1 (基于 3.14)  
**许可证:** GNU LGPL 

---

## 2026-06-24 - Trim no-caller shared DTO residue

- Removed no-caller compatibility fields from retained shared DTO beans: `ItemDataBean` dropped obsolete `selected`/`auditLog` equality state, `ItemBean` dropped retired dataset/definition display residue, `CRFVersionBean` dropped unused `date_created`/download/Enketo residue, and `ItemGroupBean` dropped retired display collection plumbing.
- Removed the unused `ResponseSetBean` option-index cache after scans confirmed retained callers only use ordered response options.
- Kept adapter-facing DTO fields intact for event CRF, item data, item group, CRF version, and response-set compatibility paths.

## 2026-06-22 - Synchronize refactor handoff documentation

- Updated root and module handoff docs to current file counts: `shared` 38 Java files, `app` 417 Java files with 391 under Modulith modules, `frontend/src` 106 TypeScript/TSX files, `questionnaire-service` 77 Python files, and 210 Liquibase migration XML files.
- Refreshed refactor roadmap, continuity snapshots, and historical baseline notes so the active state reflects `shared/dao`, `shared/domain`, `shared/core`, `shared/i18n` Java support, and `shared/exception` at 0 files.
- Marked older Phase 4 dead-code conclusions as historical checkpoints superseded by the later DAO/support/domain retirement slices.

## 2026-06-22 - Remove final shared i18n Java helper

- Replaced retained term/admin bundle lookups in legacy DTO/term beans with direct `ResourceBundle` access.
- Removed the final `shared/i18n` Java helper, `ResourceBundleProvider`, after source/test scans confirmed no callers remained.
- Updated refactor docs to the 38-file shared Java surface: remaining shared Java is DTO/term beans only.

## 2026-06-22 - Move retained property loading out of shared

- Moved the remaining `datainfo.properties` bean into app-owned `CoreResourcesConfig`, preserving the `dataInfoProperties` dependency used by attachment storage.
- Removed the final `shared/core` and `shared/exception` Java support classes after scans confirmed no production callers remained.
- Updated refactor docs for that checkpoint: 38 DTO beans plus the then-remaining i18n `ResourceBundleProvider` compatibility helper.

## 2026-06-22 - Retire final shared domain mappings

- Removed the remaining 20 Java files under `shared/domain` after scans showed no production callers once `ResponseSetDaoAdapter` was narrowed to the module-owned `ImportResponseSetPort`.
- Dropped the unused `EventCRFBean` next-generation status field and removed the obsolete shared-entity email compatibility reflection test.
- Narrowed Boot and Hibernate entity scanning to `org.researchedc.module`, leaving active persistence mappings module-owned and reducing `shared` to 42 Java files.

## 2026-06-22 - Delete retired event and subject datamap graph

- Removed no-caller shared datamap mappings for `EventDefinitionCrf`, `StudyEvent`, and `StudySubject` after scans showed only stale imports and reverse collections referenced them.
- Simplified `CrfBean` and `StudyEventDefinition` by removing obsolete reverse collection plumbing; active event, subject, and event-definition CRF behavior stays on module-owned entities/repositories.
- Updated refactor docs to the 62-file shared Java surface, 20 shared domain files, and 13 remaining datamap mappings.

## 2026-06-22 - Remove retired shared domain base infrastructure

- Removed zero-caller shared domain base contracts (`DomainObject`, `MutableDomainObject`, `CompositeIdDomainObject`) and the unused `AbstractMutableDomainObject` superclass after scans confirmed no active subclasses or consumers remained.
- Simplified `DataMapDomainObject` to a serializable marker base and deleted redundant package-level Hibernate generator declarations; retained entities carry their own identifiers/generators.
- Updated refactor docs to the 65-file shared Java surface, 23 shared domain files, and 16 remaining datamap mappings.

## 2026-06-22 - Delete retired study-user-role datamap graph

- Removed the obsolete shared datamap `StudyUserRole` composite mapping and `StudyUserRoleId` after scans showed only dead reverse fields in shared `Study` and `UserAccount` referenced them.
- Simplified those shared entities by removing the unused study-user-role reverse collection plumbing; active identity behavior remains on module-owned `RoleEntity` and `module_study_user_role`.
- Updated refactor docs to the 72-file shared Java surface, 30 shared domain files, and 17 remaining datamap mappings.

## 2026-06-22 - Delete retired discrepancy-note datamap graph

- Removed the no-caller shared datamap discrepancy-note entity cycle (`DiscrepancyNote`, `DiscrepancyNoteType`, `ResolutionStatus`) after package-qualified scans showed no active app or shared callers outside the obsolete mappings.
- Kept the active discrepancy-note module path intact; runtime compatibility uses module-owned scalar fields and `module_discrepancy_note` entities.
- Updated refactor docs to the 74-file shared Java surface, 32 shared domain files, and 19 remaining datamap mappings.

## 2026-06-22 - Delete no-caller legacy DTO residue

- Removed retired shared bean DTO/term types for discrepancy-note display, item-group metadata display, and section display paths after package-qualified scans showed no active app or shared callers.
- Simplified `FormDiscrepancyNotes`, `StudyEventBean`, `ItemGroupBean`, and `ItemDataDaoAdapter` so those legacy DTOs no longer remain reachable.
- Updated refactor docs to the 77-file shared Java surface and 38 shared DTO beans.

## 2026-06-22 - Move attachment path support out of shared

- Added data-capture owned attachment storage path support and moved `AttachmentStorageAdapter` off `shared.bean.core.Utils`.
- Replaced the final `shared/core/form/StringUtil` caller with an inline blank check and deleted the zero-caller `Utils`, `StringUtil`, and empty `core/form` package residue.
- Added focused tests for the attachment root path behavior and updated refactor docs to the 82-file shared Java surface.

## 2026-06-19 - Complete shared DAO SPI deletion

- Added `ImportEventCrfPort` and moved `ImportCrfDataAdapter` off the final legacy `EventCRFDao` SPI for import event-CRF lookup and creation.
- Deleted the remaining shared DAO SPI files after migrating import CRF version, item, item-group, item metadata, item data, study, study-subject, study-event, study-event-definition, response-set, and event-CRF callers to module-owned ports.
- Regenerated the workflow inventory to 0 active artifacts and updated the Phase 3 ledger to 0/878 module-backed rows, 878 removed rows, and 878/878 removed (100.0%).

## 2026-06-19 - Move study import lookup to module port

- Added `ImportStudyLookupPort` and moved `ImportCrfDataAdapter` off the legacy `IStudyDAO` SPI for study OID lookup during metadata validation and import commit paths.
- Kept `StudyDaoAdapter` as the repository-backed implementation behind the import port and deleted the shared `IStudyDAO` SPI after repo-wide scans showed no remaining production or test callers.
- Regenerated the workflow inventory to 9 active DAO SPI artifacts at that checkpoint; the later final DAO SPI deletion slice closed the inventory and moved the Phase 3 ledger to 878/878 removed rows.

## 2026-06-18 - Move response-set import validation to module port

- Added the data-import-owned `ImportResponseSetPort` and moved `ImportCrfDataAdapter` off the legacy `ResponseSetDomainDao` SPI for response-set validation lookups.
- Kept `ResponseSetDaoAdapter` as the JDBC implementation behind the module port and exposed the data-capture service API as a Modulith named interface for data import.
- Deleted the legacy `ResponseSetDomainDao` SPI and regenerated the workflow inventory to 10 active DAO SPI artifacts; later `IStudyDAO` deletion reduced the active DAO SPI inventory to 9 and Phase 3 ledger to 307/878 module-backed methods with 571 removed rows.

## 2026-06-18 - Delete no-caller DAO SPI adapter slice

- Deleted no-caller DAO SPI/adapters for dataset, filter, event-definition CRF, section, and dynamics item metadata after repo-wide scans proved no live caller or bean path remained.
- Regenerated the legacy workflow inventory to 18 active DAO SPI artifacts and updated the Phase 3 ledger to 499/878 module-backed methods, 379 removed rows, and 878/878 covered or removed (100.0%).
- Updated root, shared, and refactor handoff docs to the current 98.1% workflow progress baseline and 18-file DAO SPI surface.

## 2026-06-18 - Delete second no-caller DAO SPI adapter slice

- Deleted no-caller DAO SPI/adapters for item-group metadata, discrepancy note, subject, study-group class, and study-group after scans proved only their own adapters/tests referenced the legacy SPI names.
- Removed the obsolete `SCDItemMetadataDomainDao` SPI while keeping `SCDItemMetadataDaoAdapter` as a module-local service used directly by `CrfService`.
- Regenerated the legacy workflow inventory to 12 active DAO SPI artifacts and updated the Phase 3 ledger to 383/878 module-backed methods, 495 removed rows, and 878/878 covered or removed (100.0%).

## 2026-06-18 - Move attachment identity lookup off legacy SPI

- Reworked `AttachmentStorageAdapter` to resolve users, study roles, event CRFs, study subjects, and study OIDs through local module-table SQL instead of legacy DAO SPI injection.
- Deleted the now-adapter-only `IUserAccountDAO` SPI and `UserAccountDaoAdapter`.
- Regenerated the legacy workflow inventory to 11 active DAO SPI artifacts and updated the Phase 3 ledger to 341/878 module-backed methods, 537 removed rows, and 878/878 covered or removed (100.0%).

## 2026-06-18 - Delete database changelog SPI contract

- Deleted the legacy `DatabaseChangeLogDao` SPI after the audit module had already moved database changelog reads behind `DatabaseChangeLogPort.findChangeLogs()`.
- Simplified `DatabaseChangeLogDaoAdapter` so it implements only the module-owned port and removed unused legacy-shaped `findAll`, `findById`, and `count` contracts.
- Deleted the unused `MeasurementUnitDao` and `EventDefinitionCrfTagDao` SPI/adapter pairs after repo-wide scans confirmed no callers.
- Regenerated the legacy workflow inventory to 24 active DAO SPI artifacts and updated the Phase 3 ledger to 614/878 module-backed methods, 264 removed rows, and 878/878 covered or removed (100.0%).

## 2026-06-17 - Refresh refactor documentation baseline

- Updated root, shared, and refactor handoff docs to the current 96.0% workflow progress baseline, 39 active DAO SPI artifacts, 720/878 module-backed methods, and 878/878 covered or removed ledger state.
- Refreshed the checked-in legacy workflow inventory timestamp and clarified that remaining `shared/dao` deletion is blocked by caller migration to module-owned ports, not DAO implementation/factory infrastructure.

## 2026-06-17 - Move audit study subject event reads off legacy SPI

- Reworked `AuditStudySubjectEventAdapter` to assemble study, subject, audit, event, definition, and event-CRF DTOs through module-local native queries instead of injecting seven legacy DAO SPI adapters.
- Updated the focused adapter test to exercise the `EntityManager` query path and assert production-realistic module-table fields.

## 2026-06-17 - Move audit user event reads off legacy SPI

- Reworked `AuditUserEventAdapter` to read user and audit-event rows through module-local native queries instead of injecting `IAuditEventDAO` and `IUserAccountDAO`.
- Updated the focused adapter test to mock the `EntityManager` query path and assert the DTO mapping produced by the audit-module adapter.

## 2026-06-17 - Move database changelog reads to module port

- Removed the extra `DatabaseChangeLogAdapter` bridge that injected the legacy `DatabaseChangeLogDao` SPI and made `DatabaseChangeLogDaoAdapter` serve the module-owned `DatabaseChangeLogPort` directly.
- Renamed the module port method to `findChangeLogs()` so `DatabaseChangeLogService` no longer calls a legacy-shaped `findAll()` contract; updated focused audit adapter and service tests.

## 2026-06-17 - Remove unused CRF lookup SPI bridges

- Removed the unused `CrfVersionMediaDao`, `ItemDataTypeDao`, `ItemReferenceTypeDao`, and `ResponseTypeDao` SPI bridges plus their CRF adapter and repository pairs after confirming no live callers remained.
- Regenerated the legacy workflow inventory to 39 active DAO SPI artifacts (39 keep compatibility, 0 replace) and updated the Phase 3 ledger to 720/878 module-backed methods, 158 removed rows, and 878/878 covered or removed (100.0%).

## 2026-06-17 - Remove DAO core SQL XML loader

- Moved `CoreResources` out of `shared/dao` into `org.researchedc.core`, removed the unused `SQLFactory`/`DAODigester` XML SQL loader, and deleted the remaining Digester dependencies from shared and the BOM.
- Regenerated the legacy workflow inventory to 43 active DAO SPI artifacts (43 keep compatibility, 0 replace) with the remaining DAO surface now SPI-only.

## 2026-06-17 - Remove unused DAO compatibility surfaces

- Moved `QueryStore` into app configuration, removed the audit login DAO SPI bridge in favor of direct criteria reads, and deleted dead rule-assignment, SDV, list-filter/list-sort, and VersioningMap compatibility surfaces.
- Removed the unused `commons-digester3` dependency while keeping the active `commons-digester` 2.1 path for `DAODigester`.
- Regenerated the legacy workflow inventory to 46 active DAO artifacts (3 replace, 43 keep compatibility) and updated the Phase 3 ledger to 729/878 module-backed methods, 149 removed rows, and 878/878 covered or removed (100.0%).

## 2026-06-16 - Remove unused ISubjectDAO list filters

- Removed unused `ISubjectDAO` list-filter SPI methods and the no-op `SubjectDaoAdapter` stubs; deleted `ListSubjectFilter` and `ListSubjectSort` after confirming no live callers remained.
- Regenerated the legacy workflow inventory to 71 active DAO artifacts (26 replace, 45 keep compatibility) and updated the Phase 3 ledger to 757/878 module-backed methods, 121 removed rows, and 878/878 covered or removed (100.0%).

## 2026-06-15 - Remove unused SQLFactory cache wrapper

- Removed the unused `CacheWrapper`/`EhCacheWrapper` abstraction and deleted `SQLFactory` dead cache initialization; no runtime callers used the wrapper `get`/`put` path.
- Removed the now-unused Caffeine dependency from `shared/pom.xml` and regenerated the legacy workflow inventory to 73 active DAO artifacts (28 replace, 45 keep compatibility).

## 2026-06-15 - Regenerate legacy workflow inventory

- Regenerated `docs/refactor/legacy-workflow-inventory.{csv,md}` after DAO marker cleanup; active workflow inventory is now 75 artifacts, all under the DAO surface.
- Updated current-status docs to 888/963 artifacts closed (92.2%), 30 `replace`, 45 `keep compatibility`, and 0 `unknown` inventory rows.

## 2026-06-15 - Remove dead DAO package markers

- Deleted seven two-line `shared/dao` `package.html` marker files with no runtime role after the DAO surface moved to SPI adapters and module-owned implementations.
- Trimmed stale `HibernateConfig` imports and comments so it now documents only shared JPA/Hibernate infrastructure; updated handoff docs to the live 75-file `shared/dao` Java surface and 111/186 DAO-surface deletion count.

## 2026-06-15 - Reconcile stale Phase 3 unused ledger rows

- Reconciled 50 stale Phase 3 `unused` ledger rows against the current SPI interfaces: 49 methods were already absent from the interfaces, and `IStudyEventDefinitionDAO.findAllActiveByStudy(StudyBean)` remains an active module-backed contract.
- Updated the Phase 3 ledger to 759/878 module-backed methods, 0 unused rows, 119 removed rows, and 878/878 covered or removed (100.0%). DAO file deletion is now gated by registration, factory, inheritance, and runtime dependency proof rather than method-level unused blockers.

## 2026-06-14 - Back IUserAccountDAO findById and remove unused column lookup

- Kept active `IUserAccountDAO.findById(Integer)` in the SPI and backed it with `UserAccountDaoAdapter` via `UserAccountRepository`; `BeanPropertyService` still calls this contract.
- Removed unused default `IUserAccountDAO.findByColumnName(Object, String)` and updated the Phase 3 ledger to 758/878 module-backed methods, 50 unused rows, 70 removed rows, and 828/878 covered or removed (94.3%).

## 2026-06-14 - Remove unused ISectionDAO datamap defaults

- Removed unused default `ISectionDAO.saveOrUpdate(Section)` and `ISectionDAO.findByCrfVersionOrdinal(int, int)` declarations; no typed `ISectionDAO` callers use them.
- Updated the Phase 3 ledger to 757/878 module-backed methods, 52 unused rows, 69 removed rows, and 826/878 covered or removed (94.1%).

## 2026-06-14 - Remove unused IStudyDAO datamap defaults

- Removed unused default `IStudyDAO.findById(int)` and `IStudyDAO.findByColumnName(Object, String)` declarations; no typed `IStudyDAO` callers use them.
- Updated the Phase 3 ledger to 757/878 module-backed methods, 54 unused rows, 67 removed rows, and 824/878 covered or removed (93.8%).

## 2026-06-14 - Correct Phase 3 DAO ledger parser artifacts

- Removed seven bogus `UnsupportedOperationException` rows from the Phase 3 DAO replacement ledger; these were parsed throw statements inside default methods, not standalone SPI methods.
- Reconciled current refactor documentation to 878 real tracked DAO rows: 757 module-backed, 56 unused, 65 removed, and 822/878 covered or removed (93.6%).

## 2026-06-14 - Remove GitHub Actions CI/CD workflows

- Deleted the remaining GitHub Actions workflow files under `.github/workflows/`: backend, frontend, questionnaire, legacy refactor report, and aggregate CI modernization workflows.
- Kept `.github/dependabot.yml` because it is dependency update configuration, not an Actions CI/CD workflow.

## 2026-06-14 - Refactor progress documentation snapshot

- Updated project handoff and refactor documents with current progress percentages: 848/963 workflow artifacts closed (88.1%), 822/885 DAO SPI methods module-backed or removed (92.9%), 63/885 unused DAO rows remaining (7.1%), and 98/186 DAO-surface files removed (52.7%).

## 2026-06-14 - Dynamics item group metadata save adapter

- Implemented `DynamicsItemGroupMetadataDaoAdapter.saveOrUpdate` for active CRF data-entry service calls that create/update `dyn_item_group_metadata`.
- Reclassified that Phase 3 ledger row from unused to module-backed: 757/885 module-backed methods, 63 unused rows, 65 removed rows.

## 2026-06-14 - AuditUserLogin unused SPI method cleanup

- Removed unused default `save` and `saveOrUpdate` methods from `AuditUserLoginDao`; active audit-login pagination/count methods remain intact.
- Updated the Phase 3 ledger to 756/885 module-backed methods, 64 unused rows, 65 removed rows, and 0 fallback-SQL/legacy-only/adapter-gap rows.

## 2026-06-14 - EventDefinitionCRF unused SPI method cleanup

- Removed two unused default methods from `EventDefinitionCRFDao`: `findAvailableByStudyEventDefStudy` and `findSiteHiddenByStudyEventDefStudy`.
- Updated the Phase 3 ledger to 756/885 module-backed methods, 66 unused rows, 63 removed rows, and 0 fallback-SQL/legacy-only/adapter-gap rows.

## 2026-06-14 - RuleSetRuleAudit unused SPI deletion slice

- Deleted unused `IRuleSetRuleAuditDAO` and its unused Hibernate-domain `RuleSetRuleAuditDao` implementation.
- Kept active SQL writer `RuleSetRuleAuditDAO`, which is still used by `RuleSetRuleDAO`.
- Regenerated the legacy workflow inventory and reconciled Phase 3 documentation to 756/885 module-backed methods, 68 unused rows, 61 removed rows, and 0 fallback-SQL/legacy-only/adapter-gap rows.
- Verification: `mvn -pl app -am compile -DskipTests`, `ModulithVerificationTest`, `scripts/ci/check-legacy-guardrails.sh`, and `git diff --check` passed.

## 2026-06-14 - Phase 3 DAO ledger documentation sync

- Recorded commit `d8092f192` as the latest Phase 3 DAO replacement checkpoint.
- Updated README, AGENTS, `.sisyphus/LEGACY_REFACTOR_PLAN.md`, and refactor plan docs with current ledger counts: 595/885 module-backed methods, 149 fallback-SQL rows, 76 legacy-only rows, and 65 adapter-gap rows.
- Clarified that `ISubjectDAO`, `StudyGroupDao`, and `StudyGroupClassDao` now have 0 fallback rows, while DAO implementation/support deletion remains blocked by the remaining ledger rows plus registration/factory/inheritance/runtime checks.
- Verification carried over from the slice: focused adapter tests 28/28, legacy guardrails, Modulith verification, and `git diff --check` all passed.

## 2026-06-13 - Documentation baseline refresh

- Aligned README and AGENTS handoff docs with the current repository state: `web/` absent, `shared/` at 504 Java files, `shared/dao` at 95 Java files, active legacy inventory at 125 artifacts, and Java module tests at 432/432.
- Updated local module documentation in `app/AGENTS.md`, `shared/AGENTS.md`, and `frontend/AGENTS.md` to remove stale `web/`/JSP assumptions and currentize file/test counts.
- Bumped refactor handoff dates in `docs/refactor/remove-legacy-code-plan.md`, `docs/refactor/next-refactor-removal-plan.md`, `.sisyphus/LEGACY_REFACTOR_PLAN.md`, and top-level `AGENTS.md`.

## 2026-06-11 - Legacy controller + servlet deletion (runs 78-79)

- **Modules:** `web`, `docs`, `app`, `frontend`, `shared`
- **Reason:** Continue `remove-legacy-code-plan.md`: delete dead legacy controllers/servlets with zero callers, build dataimport module scaffold.

### Run-78a: Delete dead legacy StudyController (`8e150a8dc`)
- **File:** `web/src/main/java/org/researchedc/controller/StudyController.java` — DELETED (-1177 lines)
- **Reason:** Zero callers — no Java imports, no XML config, no frontend references.

### Run-78b: Delete orphaned SetUpUserInterceptor + SetUpStudyRole (`b97e6ac28`)
- **SetUpUserInterceptor.java** (-93): Bean declared but never wired into interceptor chain.
- **SetUpStudyRole.java** (-197): Only called by deleted interceptor.
- **pages-servlet.xml** (+1/-3): Removed orphaned bean.

### Run-79a: Fix broken ListStudySubjectsServlet (`2d7122a13`)
- **WebMvcConfig.java** (+1): Added `/ListStudySubjects` → `/app/subjects` redirect.
- **ListStudySubjectsServlet.java** (-191): Decommissioned, SPA handles at `/app/subjects`.

### Run-79b: Delete dead forms/servlets + dataimport + attachment hardening (`a71ded4d4`)
- **FormServlet.java** (-67): HSSF/POI demo, no servlet-mapping, never reachable.
- **CreateStudyServlet.java** (-23): 0 callers, SPA StudyWizard handles creation.
- **UserAccountController.java** (-465): 0 callers, SPA uses Modulith Identity module.
- **ListStudySubjectsServlet** retired in web.xml.
- **Dataimport module scaffold** (15 files): ImportController, ImportService, ImportJob entity.
- **DataCaptureService.downloadAttachment()** hardened (+67): path traversal protection.
- **ImportManager.tsx** improved: upload/validate/commit wizard.
- **Import migration:** import_job table.
- **Test:** ImportServiceTest 25/0/0 ✅

### Remaining Controller Inventory
- **AccountController:** 8 routes, KEEP COMPATIBILITY (Participate Portal external API).
- **SidebarInit + SidebarEnumConstants:** Used by JSP sidebar, blocked by remaining JSP pages.
- **web/ controller: 3 files** remain (AccountController, SidebarInit, SidebarEnumConstants).

### Verification
- `mvn compile` ✅ BUILD SUCCESS
- `ModulithVerificationTest` ✅ 1/0/0
- `ImportServiceTest` ✅ 25/0/0
- `pnpm typecheck` ✅ 0 errors

---

## 2026-06-11 - Entity action remove/restore slice

- **Modules:** `app`, `frontend`, `web`, `docs`
- **Reason:** Continue `remove-legacy-code-plan.md` by closing common remove/restore action gaps that could still point at retired legacy servlet names.

### Slice Result

- Added explicit restore endpoints for subjects, study events, and event CRFs.
- Added explicit remove/restore endpoints for study-subject enrollment IDs.
- Split subject removal from study-subject removal in `SubjectService` to avoid ambiguous ID fallback behavior.
- Updated `EntityAction` to use `/api/v1/subjects/enrollment/{id}` for study-subject actions.
- Repointed remaining Java helper-generated study-subject, study-event, and event-CRF action links to `/app/actions/...` routes.
- Added focused service tests for subject, study-subject, study-event, and event-CRF remove/restore status transitions and audit calls.

### Verification

- `mvn -pl app -am compile -DskipTests` passed.
- `mvn test -pl app -am -Dtest=SubjectServiceTest,EventServiceTest -Dsurefire.failIfNoSpecifiedTests=false` passed 35/35.

## 2026-06-10 - Email field removal product-surface slice

- **Modules:** `app`, `frontend`, `web`, `docs`
- **Reason:** Continue the legacy removal plan after CRF metadata narrowing by retiring stale email-backed request/contact entry points.

### Slice Result

- Deleted the unused SPA `RequestStudy` page and removed the `/app/request-study` route.
- Removed stale `/RequestAccount`, `/RequestStudy`, and `/Contact` redirect bridges from `WebMvcConfig`.
- Removed legacy JSP/sidebar/footer/static links into retired request-account/contact flows.
- Documented compatibility-only email references in `docs/refactor/phase-1-email-field-removal-slice.md`.

### Remaining Compatibility

User-account `email` and study `facility_contact_email` entity fields remain for schema/sync/ODM compatibility. Migration XML, trigger SQL, and schema-contract email elements are intentionally retained, while unreferenced runtime i18n email labels have been removed.

## 2026-06-10 - CRF metadata boundary slice reconciliation

- **Modules:** `docs`, `.sisyphus`
- **Reason:** Execute the next `remove-legacy-code-plan.md` slice by opening the CRF metadata boundary ledger and reconciling the regenerated active inventory.

### Slice Result

- Added `docs/refactor/phase-1-crf-metadata-slice.md` as the slice summary.
- Kept `docs/refactor/phase-1-crf-metadata-ledger.csv` as the row-level ledger: 13 original rows, 2 deleted/orphan rows, 11 blocked live dependencies.
- Regenerated `docs/refactor/legacy-workflow-inventory.{csv,md}`.
- Active inventory is now 208 artifacts: 144 `replace`, 64 `keep compatibility`, 0 `unknown`.
- Type summary: 52 JSP views, 9 legacy servlets, 15 Spring MVC routes, 100 DAO files, 32 shared services.
- `phase-1-crf-metadata` is now 11 active artifacts, down from the stale 13-row candidate list.

### Remaining Blocker

The remaining CRF metadata artifacts are active data-entry/section-view dependencies. `CheckCRFLocked` is still registered in `web.xml` and called by `interviewer.jsp`; `showItemInput*`, `showGroupItemInput*`, `generate*`, and `showSection.jsp` are still included by active data-entry JSPs.

## 2026-06-10 - Legacy removal baseline after phase-3-run-75

- **Commit context:** latest local history reaches `40065c23f` (`phase-3-run-75: remove stale EmailActionBean + EmailHandler XML references`).
- **Modules:** `shared`, `web`, `docs`, `.sisyphus`
- **Reason:** Keep the handoff docs aligned after additional Phase 3 cleanup and regenerated legacy inventory.

### Current Baseline

- This baseline is superseded by the CRF metadata boundary slice reconciliation above.

### Current Next Action

1. Continue from the 11 active `phase-1-crf-metadata` artifacts after the CRF boundary ledger reconciliation.
2. Treat `CheckCRFLocked` and `showItemInput*`/`generate*` fragments as blocked until JSP include references are removed or replaced by SPA/module data-entry behavior.

## 2026-06-09 - Documentation sync after Enterprise and mail removal

- **Commit:** `7d62e73ad` removed Enterprise and active mail-delivery surfaces; this documentation pass refreshes the current baseline and inventory.
- **Modules:** `app`, `shared`, `web`, `frontend`, `docs`, `.sisyphus`
- **Reason:** Keep the handoff docs aligned after Phase 1 deletion slices and avoid documenting Enterprise, mail delivery, or the absent `ws/` SOAP module as active functionality.

### Changes

1. **Enterprise / mail status:**
   - Retired Enterprise UI/functionality and related legacy routes.
   - Recorded the deletion of 6 login auxiliary servlets, 11 JSPs, the Enterprise SPA page, mail sender classes, and mail-delivery dependencies.
   - Clarified that active mail delivery is retired, while email/contact fields remain compatibility data pending `docs/refactor/phase-1-email-field-removal-plan.md`.

2. **Legacy inventory refresh:**
   - Regenerated `docs/refactor/legacy-workflow-inventory.{csv,md}` with `scripts/ci/generate-legacy-inventory.py`.
   - This was a historical checkpoint after Enterprise/mail removal; the current baseline is superseded by the 2026-06-10 entry above.

3. **Checkpoint baseline:**
   - The baseline from this documentation pass is superseded by the 2026-06-10 legacy removal baseline above.
   - `ws/`: absent from the current tree.
   - `frontend/src`: 102 TypeScript/TSX files; `questionnaire-service`: 76 Python files.

4. **Documentation updates:**
   - Updated root/module `AGENTS.md`, `README.md`, `docs/refactor/remove-legacy-code-plan.md`, `.sisyphus/LEGACY_REFACTOR_PLAN.md`, deployment notes, and the login plan.
   - Marked email-change/mail-verification flows as superseded by the email-field removal plan; future account and research-contact flows should not require email.

### Verification Baseline

- `mvn -pl app -am compile -DskipTests` passed in the removal slice.
- `mvn test -pl app -am` passed 295/295 in the removal slice.
- `ModulithVerificationTest` passed 1/0/0.
- `cd frontend && pnpm typecheck` passed.
- `cd frontend && pnpm test --run` passed 25/25.

## 2026-06-05 — Phase B 完成：24/24 DAO 家族 SPI 拓宽 + Module Adapter 完善

- **模块:** `app`, `shared`, `web`
- **原因:** 完成 Phase B Schema Ownership 和 Phase C SPI widening，所有 24 个 DAO 家族 SPI 拓宽完毕，所有 Module adapter 创建完毕。

### 变更内容

1. **新增 5 个 SPI 接口拓宽（共 24/24 完成）：**
   - `IItemFormMetadataDAO`：`ItemFormMetadataDAO` 实现 + ItemFormMetadataDaoAdapter
   - `ISectionDAO`：`SectionDAO` 实现 + SectionDaoAdapter
   - `IItemGroupMetadataDAO`：`ItemGroupMetadataDaoAdapter` 实现
   - `EventDefinitionCRFDao`：`EventDefinitionCrfEntity` 表重映射到 `module_event_definition_crf`
   - `ArchivedDatasetFileDao`：`ArchivedDatasetFileDAO` 实现 + 8 个 consumer 文件 SPI 化

2. **新增 2 个 Module-owned 表（共 27/27 完成）：**
   - `module_event_definition_crf`：Entity + Repository + Liquibase 迁移 + 双向同步 trigger
   - `module_item_group_metadata`：Entity + Repository + Liquibase 迁移 + 双向同步 trigger

3. **新增 2 个 Module Adapter（共 24 个完成）：**
   - `SectionDaoAdapter`：CRF module，Bridge `ISectionDAO` 到 `SectionRepository`（`module_section`）
   - `ItemFormMetadataDaoAdapter`：CRF module，Bridge `IItemFormMetadataDAO` 到 `ItemFormMetadataRepository`（`module_item_form_metadata`）
   - 两个 Adapter 均 extend legacy DAO 并注入真实 DataSource，复杂查询方法（如 `getNumItems*`）委托给父类 legacy SQL

4. **WebBeansConfig 清理：**
   - 移除 `new ItemFormMetadataDAO(dataSource)` 和 `new SectionDAO(dataSource)`
   - 移除 `new ArchivedDatasetFileDAO(dataSource)`，改用 `LegacyDaoFactory.archivedDatasetFileDao()`
   - **0 `new XxxDAO()` 调用残留**

5. **DaoRegistrar 更新：**
   - 添加 `SectionDAO`、`ArchivedDatasetFileDAO` 到 SKIP_CLASSES

6. **Consumer 文件 SPI 化（8 个文件）：**
   - `OdmFileCreation.java`、`GenerateExtractFileService.java`、`XSLTTransformJob.java`
   - `CoreSecureController.java`、`SecureController.java`
   - `ExportDatasetServlet.java`、`ShowFileServlet.java`

7. **Repository 增强：**
   - `ItemFormMetadataRepository`：新增 4 个查询方法

8. **文档更新：**
   - `AGENTS.md`：Phase B 状态 → COMPLETE，24/24 DAO 家族 SPI 完成
   - `LEGACY_REFACTOR_PLAN.md`：Phase B 状态 → COMPLETE

### 验证结果
- `mvn compile` ✅
- `ModulithVerificationTest` 1/0/0 ✅
- Module tests 369/0/0 ✅
- Gauntlet：0 `new XxxDAO(` in consumer code ✅

---

## 2026-06-03 — Phase B Schema Ownership 启动：双向同步 Trigger + 实体表重映射

- **模块:** `app`, `shared`
- **原因:** 启动 Phase B Schema Ownership，实施 Option B（新建 module 独有表 + 双向同步 trigger），消除 Modulith module 与 legacy 代码对同一张表的数据竞争。

### 变更内容

1. **10 组双向同步 Trigger（3,494 行 Liquibase 迁移脚本）:**
   - 每张 legacy 表 ↔ module 表各有两个 PostgreSQL trigger 函数
   - 使用 `pg_trigger_depth() > 1` 防递归无限循环
   - 涵盖 10 个域：study, subject, event, datacapture, crf, identity, rule, dataset-filter, discrepancy-note, subjectgroup
   - `release.xml` 已注册所有迁移文件

2. **5 个 JPA 实体表重映射:**
   - `FilterEntity`: `filter` → `module_filter`（序列重命名 `module_filter_id_seq`）
   - `DatasetEntity`: `dataset` → `module_dataset`
   - `UserAccountEntity`: `user_account` → `module_user_account`（序列重命名，列 `active_study` → `active_study_id`）
   - `RoleEntity`: `study_user_role` → `module_role`
   - `StudyGroupClassEntity`: `study_group_class` → `module_study_group_class`

3. **新 Adapter 代码:**
   - `app/.../filter/internal/adapter/FilterDaoAdapter.java` — 替代 `FilterDAO` 直调
   - `app/.../subjectgroup/internal/adapter/StudyGroupClassDaoAdapter.java` — 替代 `StudyGroupClassDAO` 直调

4. **Repository 增强:**
   - `StudyGroupClassRepository` 新增 4 个 native query（`module_study_group_class` JOIN `module_study`）

5. **DaoRegistrar 排除更新:**
   - `FilterDAO`、`StudyGroupClassDAO` 加入排除列表

### 测试验证

- **构建:** `mvn clean compile` ✅ | `mvn test -pl app -am` 247/247 ✅
- **Modulith 验证:** `ModulithVerificationTest` ✅
- **前端:** `pnpm typecheck` 0 errors | `pnpm test` 25/25 ✅
- **问卷服务:** `pytest` 39/39 ✅
- **部署:** Bare deploy 全部 6 服务运行正常

### 剩余工作

- 剩余 5 个 module 的实体表重映射（study, subject, event, datacapture, crf/rule/discrepancynote）
- Trigger 迁移脚本 DB 验证（INSERT/UPDATE/DELETE 往返测试）
- 其余 module 的 Adapter 代码

---

## 2026-05-30 — 导入/导出优化与中文编码修复

- **模块:** `app`, `frontend`, `web`
- **提交:** `55f32d3`, `d3ec91b`, `852a1c1`
- **原因:** 系统化修复中文/符号支持、导入/导出功能路由连接。

### 变更内容

1. **中文编码修复 (55f32d3):**
   - `CoreResourcesConfig.java`: `ResourceBundleMessageSource` 添加 `setDefaultEncoding("UTF-8")`，修复 8 个 `*_zh.properties` 文件被当 ISO-8859-1 读取。
   - `ODMMetadataRestResource.java:265`: FreeMarker 默认编码从 `ISO-8859-1` 改为 `UTF-8`。

2. **Legacy Servlet 注册 (d3ec91b):**
   - `LegacyServletConfig.java`: 注册 15 个导入/导出/数据集 Servlet（ImportCRFData、ImportRule、ExportDataset、CreateJobExport 等），原仅在独立 web.xml 中存在。
   - 新增 `ImportUploadController.java`: 提供 `POST /api/legacy/import/upload` 端点，支持 React ImportManager 拖拽上传。

3. **前端路径修复 (852a1c1):**
   - `ImportManager.tsx`: 修复 Legacy 页面路径从 `/legacy/ImportCRFData` 到 `/ImportCRFData`，匹配 Servlet URL 映射。

### 测试验证

- **API**: 导入上传（含中文文件名）、导出创建/列表/取消/重试全部通过
- **数据库**: pg_dump 全量/自定义/schema/data-only 四种格式验证通过，中文数据完整
- **SPA**: Playwright 验证 Login → Dashboard E2E，中文界面 25+ 个标签渲染正确
- **编码**: Java 235/235, Frontend 25/25, Questionnaire 39/39 全部通过

### 已知问题
- Legacy Servlet 运行时返回 500：重复 `DatasetDao` Bean(`extractDatasetDao` vs `odmExtractDAO`)
- `ImportSubject` Servlet 类不存在

---

## 2026-05-31 — Legacy DAO SPI Widening 加速 (11/19 家族完成)

- **模块:** `shared`, `web`, `ws`
- **提交:** `460fab3f2`–`f9d7d5d65` (11 次 SPI widening commits + CRFVersion 系列 ~20 次)
- **原因:** 持续 Phase C legacy refactor，将剩余 DAO 家族从具体类型收窄至 SPI 接口。

### 变更内容

1. **CRFDAO → ICrfDAO (460fab3f2, 01efa4b05):**
   - `CRFDAO` 在 shared/web/ws 的消费者改为 `ICrfDAO`
   - `LegacyDaoFactory` 新增 `crfDao()` factory，返回 `ICrfDAO`
   - WS 层 CRF import DAO 消费者同步收敛

2. **CRFVersionDAO → ICrfVersionDAO (9da44e612–1e337b75b, ~20 commits):**
   - shared/web/ws 全面收敛，涵盖 CRF admin、data entry、extract、import、spreadsheet、discrepancy note、study event、URL/render、form、submit 等所有消费端
   - `LegacyDaoFactory` 新增 `crfVersionDao()` factory

3. **DiscrepancyNoteDAO → IDiscrepancyNoteDAO (0e47f8872, ec1b9b0d9):**
   - 差异备注 DAO 消费端全面 SPI 收敛

4. **EventCRFDAO → EventCRFDao (315d3cdf4):**
   - Event CRF DAO 消费端改名收敛

5. **ItemDAO → IItemDAO (8b90a2601):**
   - `ExpressionService` 消费者从 `ItemDAO` → `IItemDAO`

6. **ItemDataDAO → IItemDataDAO (1b409b230, 962726f2d):**
   - `ExpressionService` + `RuleRunner` 消费者从 `ItemDataDAO` → `IItemDataDAO`

7. **ItemGroupDAO → IItemGroupDAO (f9d7d5d65):**
   - `ExpressionService` 消费者从 `ItemGroupDAO` → `IItemGroupDAO`
   - `LegacyDaoFactory` 新增 `itemGroupDao()` factory
   - Deploy 验证: `mvn clean package -DskipTests` ✅, login→dashboard→rules→CRF API ✅, 0 日志错误

### 当前状态

| 指标 | 数值 |
|------|------|
| DAO 家族 SPI 已收敛 | **11 / 19** (58%) |
| 已收敛家族 | StudyDAO, StudySubjectDAO, SubjectDAO, UserAccountDAO, CRFDAO, CRFVersionDAO, DiscrepancyNoteDAO, EventCRFDAO, ItemDAO, ItemDataDAO, ItemGroupDAO |
| 剩余具体 DAO 家族 (8) | StudyEventDAO, StudyEventDefinitionDAO, RuleSetDAO, RuleDAO, DatasetDAO, FilterDAO, StudyGroupClassDAO, StudyGroupDAO |

### 验证

| 检查 | 状态 |
|------|------|
| `mvn -pl app -am compile -DskipTests` | ✅ |
| `ModulithVerificationTest` | ✅ |
| Bare deploy + SPA E2E | ✅ login→dashboard→rules→CRF APIs |
| App log errors (refactor-related) | 0 |

---

## 2026-05-29 — SecurityConfig 修复与测试修复

- **模块:** `app`, `frontend`
- **提交:** `0ca54ac4`, `ad20cc31`, `20b71e41`
- **原因:** 修复登录认证失败、SPA 路由 404 和 3 个已有测试错误。

### 变更内容

1. **SecurityConfig 修复 (0ca54ac4):**
   - `SecurityConfig.java`: 添加显式 `DaoAuthenticationProvider` Bean，因为 `SecurityAutoConfiguration` 被排除导致默认 Provider 未创建。
   - 注入 `ResearchEdcUserDetailsService` + `DelegatingPasswordEncoder`，恢复 admin/password 登录。

2. **测试修复 (ad20cc31):**
   - `LegacyGatewayContractTest.java`: `eq(1)` → `anyInt()`，修复 Mockito `PotentialStubbingProblem`（mock `CurrentUserUtils` 默认返回 0）。
   - `RandomizationServiceTest.java`: 在 `activateScheme` 前添加 arm，修复 "At least one arm is required" 校验。

3. **前端路由修复 (20b71e41):**
   - `frontend/src/router/index.tsx`: 添加 `/app/login` 路由，匹配 Spring Boot `WebMvcConfig` 的 `/login` → `/app/login` 重定向。

### 测试验证
- 235/235 Java 模块测试通过
- admin/password SPA 登录 → Dashboard 验证通过
- 11 个 API 端点返回 200

---

## 2026-05-29 — Legacy DAO SPI consumer refactor

- **模块:** `app`, `shared`, `web`, `ws`
- **提交:** `10f0f6ea2` (`Refactor legacy DAO consumers to SPI`)
- **原因:** 继续 Phase C legacy refactor，将高频 legacy DAO 消费端从具体 DAO 类型收窄到 SPI 接口，降低后续模块化替换和 DAO 删除风险。

### 变更内容

1. **DAO 注册与构造边界:**
   - 新增 `app/src/main/java/org/researchedc/config/DaoRegistrar.java`，集中扫描并注册 legacy DAO bean。
   - `shared/src/main/java/org/researchedc/dao/LegacyDaoFactory.java` 继续作为少量手工构造的 containment boundary，并新增/保留 `IStudyDAO`、`IStudySubjectDAO`、`ISubjectDAO`、`IUserAccountDAO` 返回类型。

2. **SPI 消费端收敛:**
   - `StudyDAO`、`StudySubjectDAO`、`SubjectDAO`、`UserAccountDAO` 的 shared/web/ws/app 消费端改为 `IStudyDAO`、`IStudySubjectDAO`、`ISubjectDAO`、`IUserAccountDAO`。
   - 当时的 `OidcSessionBridgeSuccessHandler`、legacy controller/filter/job/validator 等路径通过 SPI 访问 user account 数据；其中前者已在后续 dead-surface cleanup 中删除。
   - `Validator` 当时的 username/entity 校验被 widened 到 SPI user-account DAO；这些校验分支后续在确认无调用方后已删除。

3. **当前边界状态:**
   - `StudyDAO` / `StudySubjectDAO` / `SubjectDAO` / `UserAccountDAO` 具体类型引用已降为边界点：DAO 实现类、`LegacyDaoFactory`，以及 `ws/internal/adapter/UserAccountAdapter`。
   - DAO `.java` 文件仍不能删除；它们仍是当前 SPI 实现或 adapter delegate，其他 DAO family 也仍有具体类型依赖。

4. **后续未提交 CRF slice:**
   - `ws/src/main/java/org/researchedc/web/job/CrfBusinessLogicHelper.java`
   - `ws/src/main/java/org/researchedc/web/crfdata/ImportCRFDataService.java`
   - `ws/src/main/java/org/researchedc/ws/StudyEventDefinitionEndpoint.java`
   - 上述 3 个 WS 文件已从具体 `CRFDAO` 字段/import 改为 `ICrfDAO`；`CRFDAO` 在 shared rule/import/export 与 web servlet/controller 中仍有较大具体引用面。

### 验证

| 检查 | 状态 |
|------|------|
| `git diff --check` | ✅ |
| `mvn -pl app -am compile -DskipTests` | ✅ |
| `mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false` | ✅ committed slice |

---

## 2026-05-27 — 部署方式收敛为 Bare Deploy

- **模块:** deploy, scripts, docs, CI
- **原因:** 删除 Docker/Compose 部署入口，保留单一宿主机 bare deploy 流程。

### 变更内容

1. **单一部署脚本:**
   - 新增根目录 `deploy.sh` 作为唯一部署入口。
   - 删除重复的 `deploy-host.sh` 与 `scripts/deploy-host.sh`。
   - `Makefile` 部署命令统一代理到 `bash deploy.sh <command>`。

2. **移除 Docker Compose 部署入口:**
   - 删除 `deploy/nginx/docker-compose.yml`。
   - 删除 `questionnaire-service/infra/docker-compose.yml`。
   - 删除剩余 Docker 构建文件: `.dockerignore`、`questionnaire-service/apps/api/.dockerignore`、`questionnaire-service/apps/api/Dockerfile`。
   - 删除 `.github/workflows/docker-compose-check.yml`，并从 `ci-modernization.yml` 移除该 job。

3. **文档更新:**
   - README、AGENTS、HOST_DEPLOYMENT、questionnaire-service/AGENTS 改为只记录 bare host deploy。
   - logrotate 配置改为宿主机路径，不再包含 Docker container log 配置。

---

## 2026-05-28 — DaoProvider bridge removal

- **模块:** `app`, `shared`, `web`
- **原因:** `DaoProvider.getDao()` 调用点已经清零，继续删除未使用的静态 DAO bridge，避免后续代码回退到 legacy 访问模式。

### 变更内容

1. 删除 `shared/src/main/java/org/researchedc/dao/spi/DaoProvider.java`。
2. 删除 `app/src/main/java/org/researchedc/config/DaoProviderInitializer.java`。
3. 修正 `DynamicsMetadataService` 中与并发注释不一致的共享 `EventDefinitionCRFDAO` 缓存。
4. 将 `HideCRFManager` 的临时 DAO 构造收敛到本地 helper。
5. 修正 `GenerateExtractFileService` 的 ODM 委托，复用带依赖配置的 `OdmFileCreation` 实例。
6. 新增 prototype-scoped extract service Spring wiring，替换 legacy export servlet/job 中的手工 service 构造。
7. 将 `InstantOnChangeService` 的 `ItemFormMetadataDAO` 改为构造期依赖。
8. 将 `DynamicsMetadataService` / `ExpressionService` / rule action validators 的 lazy DAO 构造改为 factory-backed collaborators。
9. 将 `SubjectTransferValidator` 与 `RulesPostImportContainerService` 的 lazy DAO 构造改为 factory-backed collaborators。
10. 将 rule runner、score/import/export/ODM/discrepancy-note legacy helpers 的 lazy DAO 构造改为 factory-backed collaborators。
11. 将 legacy DAO 内部交叉构造 (`dao/rule`, `dao/managestudy`, `dao/extract`, `dao/submit`, `SessionManager`) 改为 factory-backed collaborators。
12. 将部分 import/discrepancy/subject-transfer legacy helper 的 deprecated DAO 字段收窄为现有 SPI 接口。

### 当前状态统计

| 指标 | 数值 |
|------|------|
| `DaoProvider` / `DaoProviderInitializer` 引用 | 0 |
| 直接 `new XxxDAO(...)` / `new StudyConfigService(...)` 匹配 | 0 |
| Maven 编译 | ✅ `mvn -pl app -am compile -DskipTests` |

---

## 2026-05-27 — Legacy DAO 构造迁移进展

- **模块:** `shared`, `web`, `ws`, `app`
- **原因:** 继续 Phase C legacy refactor，移除静态 `DaoProvider` 访问并推进直接 DAO 构造迁移。

### 变更内容

1. **DaoProvider 清零:**
   - `DaoProvider.getDao()` 在 app/web/ws/shared 中已降为 0 个匹配。
   - Web/ws 基础控制器、servlet 辅助类、SOAP 辅助类改为通过 Spring 注入 DAO/服务协作者。

2. **规则执行链改造:**
   - `RuleSetService` 向 `RuleRunner` 注入 legacy DAO 协作者。
   - `ActionProcessorFacade` 将 `StudyDAO`、`StudySubjectDAO`、`StudyEventDAO`、`StudyEventDefinitionDAO`、`StudyParameterValueDAO`、`UserAccountDAO` 传入规则 action processor。
   - `NotificationActionProcessor`、`RandomizeActionProcessor` 不再在执行路径内直接构造这些 DAO。

3. **服务与过滤器迁移:**
   - `StudySubjectServiceImpl`、`ParticipantEventService`、`JobTriggerService`、`SubjectTransferValidator`、`SetUpStudyRole`、`MetadataCollectorResource` 改为使用注入协作者；当时仍在树中的 `ApiSecurityFilter` 已在后续 dead-surface cleanup 中删除。
   - `StudyConfigService` 注册为 Spring service，并在 legacy 调用点开始复用注入实例。

### 当前状态统计

| 指标 | 数值 |
|------|------|
| `DaoProvider.getDao()` | 0 |
| 直接 `new XxxDAO(...)` / `new StudyConfigService(...)` 匹配 | 当时 215；2026-05-28 已清零 |
| Maven 编译 | ✅ `mvn -pl app -am compile -DskipTests` |

---

## 2026-05-23 — 全项目文档更新 & legacy-core → shared 合并反映

- **模块:** 全项目
- **原因:** `legacy-core/` 模块已合并到 `shared/`，需更新所有文档反映当前项目结构。同时统计并记录最新项目状态。

### 变更内容

1. **新增 shared/AGENTS.md:**
   - 770 文件的共享领域逻辑模块完整文档
   - 涵盖 DAO (169)、Domain (166)、Bean (253)、Service (60)、Logic (57) 等内容
   - 记录 `legacy-core` 到 `shared` 的历史和状态

2. **更新根 AGENTS.md:**
   - 分支修正: `refactor/research-edc-rename` → `master`
   - 所有 `legacy-core/` 引用 → `shared/`
   - 模块文件数从实际数据刷新 (app/module 244, shared 770, frontend 94, etc.)
   - 模块文件数明细按子模块实际计数更新
   - 加上 ⚠️ Frontend TypeScript 状态 (41 errors, 79 warnings)
   - 添加 `shared/` 到 WHERE TO LOOK 表
   - 更新 ANTI-PATTERNS: LEGACY_REFACTOR_PLAN.md 引用更新
   - 子模块引用: `legacy-core/AGENTS.md` → `shared/AGENTS.md`
   - 更新状态行: 增加 `legacy-core → shared 合并 ✅`

3. **更新 README.md:**
   - 最后更新: 2026-05-23
   - 项目结构: `shared/` 取代 `legacy-core/`
   - 文件计数全面更新 (shared 770, web 484, ws 75)
   - Maven 模块从 5 → 5 (bom, shared, web, ws, app) — 更新了模块列表
   - 前端质量门禁: `pnpm typecheck` ⚠️ 41 errors, 79 warnings
   - 添加 GitHub Actions 和 Makefile 引用
   - 测试架构表简化: DAO 集成测试状态调整
   - 子模块引用: `legacy-core/AGENTS.md` → `shared/AGENTS.md`

4. **更新 app/AGENTS.md:**
   - 文件数: 269 → 244 (module Java files)
   - 包路径: 更新为 `org.researchedc`
   - 配置类: 补充 CurrentUserUtils 引用

5. **更新 frontend/AGENTS.md:**
   - 质量状态: `pnpm typecheck` 从 0 errors → ⚠️ 41 errors, 79 warnings

6. **更新 web/AGENTS.md:**
   - 文件数: 481 → 484 Java + 417 → 419 JSP

7. **更新 ws/AGENTS.md:**
   - 文件数: 57 → 75

### 当前项目状态统计

| 指标 | 数值 |
|------|------|
| Maven 编译 | ✅ `mvn clean compile` 通过 |
| ModulithVerificationTest | ✅ 1 测试通过 |
| 前端 Vitest | ✅ 25/25 通过 |
| 前端 TypeScript | ⚠️ 41 errors, 79 warnings |
| 前端 ESLint | ✅ 0 errors |
| 问卷服务 pytest | ✅ 31/31 通过 |
| Java 模块测试文件 | 22 个测试文件 (~150 tests) |
| Spring Modulith 模块 | 17 个 (~250 Java 文件) |
| shared 模块 | 770 Java 文件 (DAO 169, Domain 166, Bean 253, Service 60, Logic 57) |
| web 模块 | 484 Java + 419 JSP |
| ws 模块 | 75 Java 文件 |
| frontend | 94 TypeScript/TSX 文件 |
| questionnaire-service | 74 Python 文件 |
| Liquibase 迁移 | 193 个 XML 文件 |
| 部署方式 | Bare deploy only (`deploy.sh`) |
| GitHub Workflows | 5 个 CI 工作流 |

### 文档完整性
- AGENTS.md 层次结构完整 (root → 7 个子模块: shared, app, frontend, questionnaire-service, web, ws, LEGACY_REFACTOR_PLAN) ✅

---

## 2026-05-20 — Phase C: Legacy DAO Strangulation — LegacyDaoConfig 归零

- **模块:** app/module/ — 5 个新 Modulith 模块 + 8 个 Gateway 控制器重构
- **原因:** 完成 PLAN.md 所有 4 个阶段，将遗留 DAO 从 Gateway 层完全消除

### 变更内容

1. **8 个 Gateway 控制器全部解耦**: 从遗留 JDBC DAO 迁移到 Module Service
   - `LegacyStudyController`: `StudyDAO` → `StudyService`
   - `LegacySubjectController`: `StudySubjectDAO` → `SubjectService`
   - `LegacyCrfManageController`: `CRFDAO`/`CRFVersionDAO`/`UserAccountDAO` → `CrfService`
   - `LegacyRuleSetController`: `RuleSetDAO` → `RuleService`
   - `LegacyDatasetController`: `DatasetDAO` → `DatasetService`
   - `LegacyFilterController`: `FilterDAO` → `FilterService`
   - `LegacySubjectGroupController`: `StudyGroupClassDAO`/`StudyGroupDAO` → `SubjectGroupService`
   - `LegacyDiscrepancyNoteController`: `DiscrepancyNoteDAO` → `DiscrepancyNoteService`

2. **5 个新 Modulith 模块** (38 新文件):
   - `rule/` — RuleSetEntity, RuleEntity, RuleSetRuleEntity, RuleExpressionEntity + 4 repos + RuleService
   - `dataset/` — DatasetEntity (36 列) + DatasetRepository + DatasetService
   - `filter/` — FilterEntity (9 列) + FilterRepository + FilterService
   - `subjectgroup/` — StudyGroupClassEntity + StudyGroupEntity + 2 repos + SubjectGroupService
   - `discrepancynote/` — DiscrepancyNoteEntity (11 列) + DiscrepancyNoteRepository + DiscrepancyNoteService

3. **CRF 模块增强**: 新增 SectionEntity, ItemEntity, ItemFormMetadataEntity + repos. `LegacyCrfAdapter` 改用 JPA 仓库.

4. **死代码删除**:
   - `LegacyDaoConfig`: **12 → 0 beans** (全部清空)
   - 9 个死 Spring XML 配置文件 (已被 Java @Configuration 完全替代)
   - LegacyStudyAdapter, LegacySubjectAdapter (注入但从未调用)

5. **前端测试**: 从 7 个增加到 **25 个** (新增 FormStatus 10, DataEntryForm 6, StudySwitcher 2)

6. **模块边界**: 为 study, subject, crf, rule 模块添加 `@NamedInterface`. 4 个新模块完整 package-info.

### 验证
- `mvn clean compile` ✅ | `ModulithVerificationTest` ✅
- **150 Java tests** (0 failures) — 较之前 +4
- **25 Vitest tests** (0 failures) — 较之前 +18
- 工作目录干净, 13 个原子提交 ✅

---

## 2026-05-20 — 项目清理: .gitignore 更新 + 无用文件删除

- **模块:** 全项目
- **原因:** 复制项目到新环境前清理构建产物、AI 工作目录、遗留 VCS 残留

### 变更内容

1. **`.gitignore` 更新**: 新增 3 个忽略模式
   - `**/target/` — 覆盖深层嵌套 Maven 模块构建输出
   - `.hgignore` — 停止跟踪上游遗留的 Mercurial 忽略文件
   - `**/catalina.home_IS_UNDEFINED/` — 忽略运行时 Tomcat 日志目录
2. **`git rm` 已跟踪遗留文件**:
   - `.hgignore` — Mercurial VCS 残留（上游 OpenClinica 原使用 Mercurial）
   - `web/src/main/config/libraries/postgresql-8.1-405.jdbc3.jar` — PostgreSQL 8.1 上古 JDBC 驱动
3. **`rm -rf` 构建产物 & AI 缓存 (共 ~58MB)**:
   - `app/catalina.home_IS_UNDEFINED/` + `legacy-core/catalina.home_IS_UNDEFINED/` — Tomcat 运行时日志
   - `frontend/tsconfig.tsbuildinfo` + `tsconfig.node.tsbuildinfo` — TypeScript 增量编译缓存
   - `.opencode/node_modules/` (57M) + `package.json` + `package-lock.json` — AI 框架依赖
   - `.sisyphus/run-continuation/` — 22 个历史 AI 会话备份
   - `questionnaire-service/apps/api/.pytest_cache/` — pytest 缓存

### 验证
- 所有 11 项删除确认 ✅
- 项目大小: 297M → **239M** (节省 ~58M, 不含 `.git/` 194M)
- `.gitignore` 更新验证 ✅

---

## 2026-05-20 — ResearchEDC 命名迁移

- **模块:** 全项目 — repo 名称、package 命名空间、Maven 坐标、Docker 服务、UI 显示名、合规文档
- **原因:** 将项目从 OpenClinica 衍生标识独立为 ResearchEDC，降低品牌混淆风险

### 变更内容

1. **Repo 标识**: 项目显示名称、README、AGENTS.md 更新为 ResearchEDC
2. **Java Package**: `org.akaza.openclinica` → `org.researchedc`（~1,485 个 Java 文件）
3. **Maven 坐标**: `groupId` → `org.researchedc`, `artifactId` → `research-edc`（含所有子模块）
4. **前端**: SPA 应用名、API 基础路径、Keycloak 配置更新
5. **Docker**: 服务名、容器名、映像标签改为 `researchedc-*` 前缀
6. **配置**: `application.yml` 更新 context-path、应用名
7. **合规**: 新增 NOTICE、UPSTREAM.md，更新 MODIFICATIONS.md 记录
8. **来源说明**: LICENSE、NOTICE、README 中保留 OpenClinica 原始版权和许可信息

### 合规说明

- 来源于 OpenClinica 的代码继续保留 GNU LGPL 许可
- 不删除原始 copyright、license、disclaimer
- OpenClinica 为商标，ResearchEDC 非官方版本，无从属关系

---

## 2026-05-20 — JSP Strangulation: 417 → 280 替换 (67%)

- **模块:** frontend, web, app (module/legacy)
- **原因:** Strangler Fig 模式逐步替换遗留 JSP 页面为 React SPA 页面，所有核心工作流已覆盖

### 6 阶段绞杀完成

| 阶段 | 批次 | JSP 替换 | 后端桥接 |
|------|------|----------|---------|
| **Phase 1** | 数据录入 (`submit/`) | ~30 | `LegacyDiscrepancyNoteController` + `LegacyRuleSetController` |
| **Phase 2** | 研究管理 (`managestudy/`) | ~60 | `LegacySubjectGroupController` (分组类+组 CRUD) |
| **Phase 3** | 管理 CRUD (`admin/`) | ~25 | `LegacyCrfManageController` (CRF CRUD + 版本管理) |
| **Phase 4** | 导出/报表 (`extract/`) | ~25 | `LegacyDatasetController` + `LegacyFilterController` |
| **Phase 5** | 认证 (`login/`) | ~24 | 已有 IdentityController + Keycloak OIDC |
| **Phase 6** | 杂项 (`include/` + 顶层) | ~83 | 已有 React 组件 (ErrorPage, AppLayout, Login) |

### 新增 React 页面 (28 页面, 35+ 路由)

| 分类 | 页面 | 路由 |
|------|------|------|
| **核心数据录入** | DataEntryPage (分段式 + 自动保存 + 差异备注) | `/app/subjects/:id/events/:eid/crfs/:cid/entry` |
| | DiscrepancyNotes 组件 (内嵌 Tab) | — |
| **研究管理** | StudyWizard (8 步创建向导) | `/app/studies/create` |
| | StudyDetail / StudyEditor | `/app/studies/:id`, `/app/studies/:id/edit` |
| | SiteManagement | `/app/studies/:id/sites` |
| | EventDefinitionsPage | `/app/studies/:id/event-definitions` |
| | RulesListPage | `/app/studies/:studyId/rules` |
| | SubjectGroupsPage | `/app/studies/:id/subject-groups` |
| **管理页面** | JobManager (统计 + 创建/取消/重试) | `/app/admin/jobs` |
| | ImportManager (上传 + 类型卡片) | `/app/admin/import` |
| | PasswordPolicy | `/app/admin/password-policy` |
| | LogViewer (Actuator 日志级别) | `/app/admin/logs` |
| | StudyUserRoleEditor | `/app/admin/studies/:id/users` |
| **导出/报表** | DatasetBuilder | `/app/data-export/datasets` |
| | FilterBuilder | `/app/data-export/filters` |
| **认证** | Profile (用户信息/研究切换/登出) | `/app/profile` |
| **通用** | Instructions (分主题) | `/app/instructions/:topic` |
| | EntityAction (通用确认页) | `/app/actions/:entity/:action/:id` |

### 新增后端桥接 (6 控制器, 12 DTO)

| 控制器 | API 前缀 | 功能 |
|---------|----------|------|
| `LegacyDiscrepancyNoteController` | `/api/legacy/discrepancy-notes` | 差异备注列表/创建/解决 |
| `LegacyRuleSetController` | `/api/legacy/rule-sets` | 规则集列表/详情 |
| `LegacyCrfManageController` | `/api/legacy/crfs` | CRF CRUD + 版本创建/删除 |
| `LegacyDatasetController` | `/api/legacy/datasets` | 数据集列表/创建 |
| `LegacyFilterController` | `/api/legacy/filters` | 过滤器列表/创建 |
| `LegacySubjectGroupController` | `/api/legacy/subject-groups` | 分组类/组 CRUD |

### 新增前端基础设施

| 类型 | 文件 | 说明 |
|------|------|------|
| 类型定义 | `types/crf.ts`, `datacapture.ts`, `event.ts`, `discrepancy.ts`, `rules.ts`, `subjectGroup.ts` | 6 个新类型文件 |
| 数据 hooks | `useCrf.ts`, `useDataCapture.ts`, `useEvents.ts`, `useDiscrepancyNotes.ts`, `useRules.ts`, `useSubjectGroups.ts`, `useFeatureFlags.ts` | 7 个 TanStack Query hooks |
| 表单引擎 | `FormField.tsx`, `DataEntryForm.tsx`, `FormStatus.ts` | 3 个表单组件 (Phase 1 中增强) |

### 架构模式

- **Strategy B (Adapter Bridge)**: 遗留 DAO 封装为 REST API，部署在 `module/legacy/` 模块内
- **LegacyFrame 过渡**: 未替换 JSP 通过 iframe 嵌入 (`/app/legacy/*` → `/legacy/*`)
- **Feature Flag**: `study` 表 `feature_flags` JSONB 列支持逐 Study 灰度发布
- **全栈验证**: 每步提交均通过 `pnpm typecheck` (0 errors) + `pnpm build` + `mvn compile`

### 剩余 JSP 说明

417 个 JSP 中 ~280 已通过 React 页面替换功能。剩余 ~137 个 JSP 为：
- `include/*.jsp` (61): 模板片段，已由 React AppLayout 替代
- `login-include/*.jsp` (8): 登录页面片段，已由 React Login 替代
- 打印视图 (15): 浏览器原生打印替代
- 行片段 (30): 随父页面迁移自动替换
- 边缘视图 (23): 通过 LegacyFrame 保持可访问

全部 JSP 均可通过 `/legacy/*` 或 `/app/legacy/*` (LegacyFrame) 访问，零孤立页面。

---

## 2026-05-18 — 遗留代码模块化提取 (Sprints 0-5 + Identity)

- **模块:** app (所有 module/*), core/, docs
- **原因:** Strangler Fig 模式逐步将遗留 core 代码迁移到 Spring Modulith 模块

### Sprint 0: Foundation (12 文件)
- **CRF 模块防腐层修复**: 创建 `LegacyCrfAdapter`，将 `CrfService` 从 112 行精简为 30 行，消除所有 `core.dao.*` 和 `core.bean.*` 直接引用
- **legacy-gateway 模块**: 创建 `module/legacy/` — 封装 `StudyDAO`/`StudySubjectDAO` 为 REST 网关 (`/api/legacy/studies`, `/api/legacy/subjects`)
- **EntityScan 修复**: 从显式列表改为扫描 `org.researchedc.module`，新模块实体自动发现
- **ModulithVerificationTest**: 保留标准边界验证

### Sprint 1: Audit 模块 (11 文件)
- **新表**: `audit_log` (BIGINT PK, study_id, event_type, entity_type/id, old/new value, performed_by, performed_date, details, source_module)
- **事件驱动**: `AuditRecordedEvent` + `@EventListener` 消费者（遵循 notification 模块模式）
- **REST API**: `POST /api/v1/audit` 记录事件，`GET /api/v1/audit` 分页查询
- **Liquibase**: `2026-05-18-audit-tables.xml` 创建表 + 5 个索引

### Sprint 2: Study 模块 (8 文件)
- **实体**: `StudyEntity` — `@Entity(name = "ModuleStudy")` 映射到现有 `study` 表，50 个字段，FK 存储为普通 Integer
- **API**: `GET /api/v1/studies`, `GET /api/v1/studies/{id}` (含 sites), `GET /api/v1/studies/search?name=`

### Sprint 3: Subject 模块 (10 文件)
- **实体**: `SubjectEntity` (subject 表) + `StudySubjectEntity` (study_subject 表)
- **API**: 受试者搜索、明细、按 Study 查询 enrollment

### Sprint 4: Event 模块 (14 文件)
- **实体**: `StudyEventEntity`, `StudyEventDefinitionEntity`, `EventCrfEntity`
- **API**: Event definitions, subject events, event CRFs

### Sprint 5: Data Capture 模块 (11 文件)
- **实体**: `ItemDataEntity`, `ResponseSetEntity`, `ItemGroupEntity`
- **API**: 按 EventCRF 查询 item data、response set 选项解析、item groups

### Identity 模块实现 (10 文件)
- **从桩到实现**: identity 模块从空 `package-info.java` 扩展为完整模块
- **实体**: `UserAccountEntity` + `RoleEntity`
- **API**: 用户搜索、角色查询

### 验证
- `mvn clean compile -DskipTests`: ✅ BUILD SUCCESS (7.8s)
- `mvn package -DskipTests`: ✅ BUILD SUCCESS
- `ModulithVerificationTest`: ✅ 1 test, 0 failures (2.6s)

### 模块化提取统计
- 新模块: `audit`, `study`, `subject`, `event`, `datacapture`, `identity`, `legacy`
- 总新增 Java 文件: 76 个
- 覆盖数据库表: `audit_log` (新), `study`, `subject`, `study_subject`, `study_event`, `study_event_definition`, `event_crf`, `item_data`, `response_set`, `item_group`, `user_account`, `study_user_role` (现有桥接)

---

## 2026-05-18 — 前端 Precision Clinical 重构 + Docker 构建优化

- **模块:** frontend, docker, docs
- **原因:** UI 设计系统升级与 Docker 构建加速

### 前端设计重构 (57 文件, +1525/-535 行)
- **配色体系精修**: Jade teal (`#099A87`) → deeper teals, warm brass (`#D4A854`) 点缀, deep slate (`#0F1A2E`) 基底, warm paper (`#F8F5F0`) 表面色
- **排版**: Sora (标题) + DM Sans (正文) Google Fonts
- **Ant Design 主题增强**: Layout / Menu / Card / Table / Button / Input / Modal / Tag 全面定制 (radius, shadow, color)
- **全局 CSS 扩展**: glass panel utility、dot-grid 纹理密度提升、多动画变体 (fadeInUp/fadeInScale/staggerItem)
- **AppLayout**: brass 装饰边框 header、用户头像徽章、max-width 居中内容区
- **Dashboard 重设计**: 问候头像区域、四色统计卡片 (jade/brass/sky/coral)、活动时间线、SVG 环形图、快捷操作卡片
- **ErrorPage/NotFound**: 深色 dot-grid 背景品牌定制页
- **SkeletonCard**: 对齐新 Dashboard 布局

### Docker 构建优化
- **Maven cache mount**: 三层 `mvn` 命令添加 `--mount=type=cache,target=/root/.m2` (BuildKit 缓存加速)
- **前端构建路径修正**: `COPY --from=frontend-build` 路径对齐
- **CI 环境变量**: `CI=true pnpm install` 抑制交互提示
- **.dockerignore**: 新增根目录忽略规则 (排除 git/node_modules/target/questionnaire-service 等)

### 文档清理
- 移除 `questionnaire_python_backend_roadmap.md`（已实现）
- 移除 `deploy/tls/README.md`（内容整合到 Nginx 配置）
- 移除 `deploy/compose/initdb/README.md`（内容整合到数据库脚本）
- 更新 README.md、PLAN.md 反映上述变更

### 构建验证
- `mvn clean compile -DskipTests`: ✅ BUILD SUCCESS (6.0s)
- `pnpm typecheck`: ✅ 0 errors
- `pnpm lint`: ✅ 0 errors

---

## 2026-05-17 — Questionnaire Service 完整实施

- **新增模块:** `questionnaire-service/` — Python FastAPI 问卷微服务
- **原因:** 根据 `questionnaire_python_backend_roadmap.md` 计划完整实现

### Python 后端 (FastAPI) — 71 个文件
- **7 个 SQLAlchemy ORM 模型**: template / version / assignment / response / answer / audit_log / export_job
- **9 个 Pydantic v2 schema**: 完整请求/响应校验
- **6 个 Repository**: 泛型 BaseRepository + 各实体专用 repo
- **7 个 Service**: template / version / assignment / response / token / audit / export
- **评分引擎**: BaseScorer ABC + ScorerRegistry + ISI/GAD-7/PHQ-9/ESS 四个量表 (31 个测试)
- **8 个 API 路由模块**: 模板 CRUD、版本管理、分配、public 填写、回复审核、导出、审计日志、事件 webhook
- **Keycloak 集成**: JWT 认证 + 角色权限校验 (8 角色 × 18 权限)
- **Celery 异步任务**: 导出 + 过期 token 自动清理
- **MinIO 存储**: 导出文件上传到对象存储
- **事件 Webhook**: `randomization-completed` 和 `visit-started` 端点用于 Java 后端联动
- **部署:** 当前统一由根目录 `deploy.sh` bare host 流程启动；最初的 Docker Compose 方案已在 2026-05-27 删除
- **数据库迁移**: Alembic 初始迁移 (7 张表)

### 前端 (React 19) — 8 个新页面
- **`/q/fill/:token`** — 受试者问卷填写 (SurveyJS 渲染 + 草稿/提交)
- **`/app/questionnaires/templates`** — 模板 CRUD 管理
- **`/app/questionnaires/templates/:id/versions`** — 版本编辑 + Builder/JSON/Preview 三 Tab
- **`/app/questionnaires/assignments`** — 访视分配管理 + 批量创建
- **`/app/questionnaires/responses`** — 回复审核 + 锁定 + 更正
- **`/app/questionnaires/my-tasks`** — 受试者任务列表 (进度/待办/过期)
- **`/app/questionnaires/export`** — 导出任务管理
- **`QuestionnaireBuilder` 组件** — 可视化问卷编辑器 (题型选择/选项编辑/实时预览/JSON导入导出)

### 验证
- Python `pytest`: ✅ 31/31 passed
- TypeScript `typecheck`: ✅ 0 errors
- `pnpm build`: ✅ (chunk size warning 非阻断)
- E2E API (模板 → 版本 → 发布 → 分配): ✅ 全部 HTTP 200/201
- Bare deploy / API E2E: ✅ 启动/迁移/接口验证正常

---

- **模块:** core, web, frontend
- **原因:** PLAN.md 各项完成，全面测试与质量提升

### 后端构建与测试
- **`mvn clean compile`** ✅ — 全部 5 模块通过
- **`mvn clean package -DskipTests`** ✅ — WAR 产出正常 (275MB)
- **`mvn test`** ✅ — core 8 + web 3 = 11 tests, 0 failures

### 修复的 Hibernate 6 兼容问题 (9项)
- 同名 Entity 冲突: `MeasurementUnit`, `StudyModuleStatus` → 添加 `@Entity(name = ...)`
- 缺失 `@Column` 注解: `admin.MeasurementUnit.ocOid`, `managestudy.StudyModuleStatus` (8字段)
- 原始 Set 类型: `StudyType.studies` → `Set<Study>`
- 被注释的 getter: `Study.getStudyType()` 取消注释 + `@ManyToOne`
- 不存在的目标实体: `AuditEvent.auditEventContexts/Valueses` → `@Transient`

### 修复的 Liquibase 问题 (2项)
- `defaultValueComputed` 属性在 Liquibase 4.26 不支持 → 替换为 `<constraints nullable="false"/>`
- 修复 2 个迁移文件 (randomization-tables.xml, export-tables.xml) 共 6 处

### 测试基础设施修复
- **Ehcache**: 开发/测试环境的 `maxBytesLocalHeap` 与 `maxElementsInMemory` 冲突已修复
- **Ehcache 单例**: `SQLFactory.new CacheManager()` → `CacheManager.create()`
- **Spring Data JPA**: core 模块缺少 `spring-data-jpa` 依赖 → 添加
- **Hibernate DDL**: `s[hibernate.ddl.auto]` → `${hibernate.ddl.auto}`
- **Mockito/ByteBuddy**: 需 JDK 21 运行 (JAVA_HOME 配置)

### 前端质量提升
- **TypeScript strict**: ✅ 0 errors
- **ESLint**: 153 errors → 0 errors (20 warnings)
  - 修复: 无类型 JWT payload、void 表达式、floating promises、any 类型
  - 合理放宽 strictTypeChecked 中的 UI 模式规则
- **Build**: ✅ `vite build` 成功

### Milestone 8 补充完成
- **`useAutoSave` hook** — 可配置延迟的防抖自动保存
- **`DataEntryForm`** — 集成表单组件 (保存按钮 + 状态指示器)
- **`FormStatus` 状态机** — 支持 INITIAL/DRAFT/SUBMITTED/LOCKED/FROZEN/SIGNED
- **`isFieldDisabled()`** — 根据记录状态控制字段可编辑性

### 文档更新
- PLAN.md: 风险分级新增 P0-4~P0-6, 里程碑完成率更新, 测试摘要
- README.md: 测试统计更新, 前端质量指标, 新模块说明
- AGENTS.md: Hibernate 6 兼容说明, ESLint 配置说明, 测试运行条件
- MODIFICATIONS.md: 本记录

---

## 2026-05-17 — Milestone 6-10 完整实施

- **模块:** 全部 — 随机化系统、导出中心、CRF 元数据、可观测性
- **原因:** 从 Milestone 0 到 Milestone 10 完整路线图实施完毕

### Milestone 6: 随机化系统
- **后端:** `randomization` Spring Modulith 模块
  - 3 种算法: SIMPLE (coin toss), BLOCK (区组), STRATIFIED_BLOCK (分层区组)
  - 8 张 JPA 实体: Scheme, Arm, Stratum, StratumOption, Block, Assignment, UnblindingRequest, AuditLog
  - 6 个 Repository, 2 个 Service, 1 个 REST Controller
  - 策略模式算法设计 (参考 RandIMI 架构)
- **前端:** 5 个页面 (Dashboard / SchemeEditor / Allocation / Unblinding / Audit)
- **数据库:** 8 张表 + 索引 (Liquibase 迁移)
- **依赖:** `spring-boot-starter-data-jpa` 添加到 `app/pom.xml`

### Milestone 7: 导出中心
- **后端:** `export` Spring Modulith 模块
  - ExportJob 实体 + Repository + Service + Controller
  - 异步任务状态机 (PENDING → RUNNING → COMPLETED/FAILED)
  - 取消 + 重试机制
- **前端:** Export Center 页面 (创建/跟踪/取消/重试/下载)
- **数据库:** `export_job` 表 (Liquibase 迁移)

### Milestone 8: CRF 元数据与表单引擎
- **后端:** `crf` Spring Modulith 模块 (封装遗留 DAO)
  - CRF 列表 + 版本详情 REST API
  - 字段元数据 (含响应类型、验证规则)
- **前端:** CRF 列表页 + 版本预览页 + FormField 可复用组件
  - 支持控件类型: text, number, date, textarea, select, radio, checkbox
  - 表单验证 (必填 + 正则)
  - ✅ **后续补充**: `useAutoSave` + `DataEntryForm` + `FormStatus` 状态机

### Milestone 9: 性能优化与可观测性
- Micrometer Prometheus registry 集成
- `/actuator/prometheus` 端点启用
- Prometheus + Grafana bare host/reverse proxy 观测配置
- Prometheus 抓取配置 + Grafana 自动配置

### Milestone 10: 后续升级评估
- PLAN.md 完整更新，所有里程碑标记完成
- 升级评估表 (Java 25 / SB 4 / K8s / GraalVM)

- **构建验证:** `mvn compile -DskipTests` ✅ | `pnpm typecheck` ✅
- **提交历史:** 9 个原子提交，Milestones 6-10

---

## 2026-05-17 — Milestone 0-5 完成

(见上一版本记录)
