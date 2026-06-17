# OpenClinica Legacy Code Refactoring Plan

> **Last updated:** 2026-06-17 (Legacy code removal is **not complete**. Tracked workflow progress is 924/963 closed, **96.0%**. Phase B schema ownership, Phase C SPI widening, and Phase 1 web/JSP/servlet deletion are complete. Remaining blockers are 39 DAO SPI Java files under `shared/dao`. Phase 3 ledger: 720/878 methods module-backed; 878/878 module-backed or removed, **100.0%**; 0 unused rows remain; 158 removed; 0 fallback-SQL, legacy-only, or adapter-gap rows remain. See `docs/refactor/remove-legacy-code-plan.md`.)
> **Scope:** All remaining legacy code in `shared/` plus app-hosted compatibility classes migrated from `web/`; keep SOAP compatibility audits only if `ws/` reappears
> **Strategy:** Strangler Fig — new modules replace legacy, legacy code is deleted only after replacement is proven

---

## Current Status Summary

### Done (Phase A+B+D+E+H complete, Phase C SPI Impl deletion + JWT fix + module tests complete, Phase F+G infrastructure built)

| Sprint | Module | Type | API |
|--------|--------|------|-----|
| 0 | `legacy-gateway` | DAO REST wrapper | `/api/v1/legacy/*` |
| 0 | `crf` (fixed) | Anti-corruption adapter | `/api/v1/crfs` |
| 1 | `audit` | New table + events | `/api/v1/audit` |
| 2 | `study` | Bridge to `study` table | `/api/v1/studies` |
| 3 | `subject` | Bridge to `subject`/`study_subject` | `/api/v1/subjects` |
| 4 | `event` | Bridge to `study_event`/`event_crf` | `/api/v1/events` |
| 5 | `datacapture` | Bridge to `item_data`/`response_set` | `/api/v1/data-capture` |
| — | `identity` | Bridge to `user_account`/`study_user_role` | `/api/v1/identity` |

### Remaining legacy code baseline (2026-06-12, current worktree after regenerated inventory)

```
shared/   241 Java files → bean/ dao SPI/ domain/ job/ exception/ i18n/ patterns/ core/ support/
           39 Java files under shared/src/main/java/org/researchedc/dao
web/        0 files → directory absent; needed import/validation compatibility classes migrated to app/
ws/         0 Java files → SOAP module absent in current tree
inventory 39 active artifacts -> 39 keep compatibility, 0 unknown
phase-3  720/878 DAO SPI methods module-backed; 878/878 module-backed or removed (100.0%); 0 unused rows remain (0.0%); 158 removed; 0 fallback-SQL, legacy-only, or adapter-gap rows remain
```

Important distinction: `legacy-core/` removal was a module consolidation into `shared/`; it was not full legacy code removal.

---

## Phase A: Write Operations (✅ COMPLETE)

All new modules are now **write-capable** — they can create, update, and delete data in addition to reading.

### A1: Study Module Write ✅
- `StudyService`: `createStudy()`, `updateStudy()`, `deleteStudy()`, `updateStudyStatus()` — all `@Transactional`
- Validation: name uniqueness check, required fields
- Endpoints: `POST /api/v1/studies`, `PUT /api/v1/studies/{id}`, `DELETE /api/v1/studies/{id}`, `PATCH /api/v1/studies/{id}/status`
- Audit: `AuditService.recordAudit()` wired into all write operations
- Tests: 14 unit tests (JUnit 5 + Mockito) all passing

### A2: Subject Module Write ✅
- `SubjectService`: `createSubject()`, `enrollSubject()` (creates study_subject record)
- Endpoints: `POST /api/v1/subjects`, `POST /api/v1/subjects/enroll`
- Audit: `AuditService.recordAudit()` wired into create and enroll

### A3: Event Module Write ✅
- `EventService`: `scheduleEvent()`, `updateEvent()`, `completeEvent()`
- Endpoints: `POST /api/v1/events`, `PUT /api/v1/events/{id}`, `POST /api/v1/events/{id}/complete`
- Audit: `AuditService.recordAudit()` wired into all write operations

### A4: Data Capture Module Write ✅
- `DataCaptureService`: `saveItemData()` (upsert for event CRF values), `batchSaveItems()`
- Endpoints: `POST /api/v1/data-capture/items`, `POST /api/v1/data-capture/items/batch`
- Status transitions: initial (status=1) through save
- Audit: `AuditService.recordAudit()` wired (CREATE/UPDATE event types)

### A5: Identity Module Write ✅
- `IdentityService`: `createUser()`, `assignRole()`
- Endpoints: `POST /api/v1/identity/users`, `POST /api/v1/identity/roles/assign`
- Audit: `AuditService.recordAudit()` wired into create and assign

---

## Phase B: Schema Ownership (✅ COMPLETE)

Modules now own `module_*` tables with bidirectional sync triggers back to the legacy tables. Phase B implemented **Option B: New tables with bidirectional sync triggers** for all 10 domains and PostgreSQL validation completed on 2026-06-07.

### B0: Final State (2026-06-05)

**10 bidirectional sync trigger migration files created (3,494 lines total):**

Each domain has two PostgreSQL trigger functions (`sync_module_X_to_X` + `sync_X_to_module_X`) with `pg_trigger_depth() > 1` recursion guard, ensuring legacy and module-owned tables stay in sync during the transition.

### B1: PostgreSQL Validation (2026-06-07)

Status: **complete** in commit `0963eec2c`.

- Applied `shared/src/main/resources/migration/master.xml` cleanly to disposable PostgreSQL databases.
- Added `scripts/ci/check-phase-b-postgres.sh`, gated by explicit `PHASE_B_PG*` environment variables.
- Verified 54 expected Phase B sync functions and 54 expected sync triggers.
- Proved representative bidirectional insert/update/delete sync for `study` <-> `module_study`, `filter` <-> `module_filter`, and `discrepancy_note` <-> `module_discrepancy_note`.
- Confirmed repeated bidirectional updates converge without recursive trigger loops.
- Fixed discrepancy-note sync for the actual legacy schema: legacy `discrepancy_note` has no `entity_id`, so module copy/sync stores `NULL AS entity_id` from legacy and omits `entity_id` when writing back to legacy.

Next work is no longer Phase B. Continue with the legacy route/workflow inventory and Phase 1 JSP/servlet deletion slices tracked in `docs/refactor/remove-legacy-code-plan.md`.

| Migration File | Lines | Tables |
|---|---|---|
| `2026-06-03-study-bidirectional-sync-trigger.xml` | 367 | `study` ↔ `module_study` |
| `2026-06-03-subject-bidirectional-sync-trigger.xml` | 248 | `subject`/`study_subject` ↔ `module_subject`/`module_study_subject` |
| `2026-06-03-event-bidirectional-sync-trigger.xml` | 453 | `study_event`/`study_event_definition`/`event_crf` ↔ `module_*` |
| `2026-06-03-datacapture-bidirectional-sync-trigger.xml` | 337 | `item_data`/`response_set`/`item_group` ↔ `module_*` |
| `2026-06-03-crf-bidirectional-sync-trigger.xml` | 631 | `crf`/`crf_version`/`item`/`section` ↔ `module_*` |
| `2026-06-03-identity-bidirectional-sync-trigger.xml` | 255 | `user_account`/`study_user_role` ↔ `module_user_account`/`module_role` |
| `2026-06-03-rule-bidirectional-sync-trigger.xml` | 478 | `rule_set`/`rule`/`rule_set_rule`/`rule_action` ↔ `module_*` |
| `2026-06-03-dataset-filter-bidirectional-sync-trigger.xml` | 376 | `dataset`/`filter` ↔ `module_dataset`/`module_filter` |
| `2026-06-03-discrepancy-note-bidirectional-sync-trigger.xml` | 139 | `discrepancy_note` ↔ `module_discrepancy_note` |
| `2026-06-03-subjectgroup-bidirectional-sync-trigger.xml` | 210 | `study_group_class`/`study_group` ↔ `module_study_group_class`/`module_study_group` |

**5 JPA entities remapped to module-owned tables:**

| Module | Entity | Old Table | New Table |
|--------|--------|-----------|-----------|
| filter | `FilterEntity` | `filter` | `module_filter` |
| dataset | `DatasetEntity` | `dataset` | `module_dataset` |
| identity | `UserAccountEntity` | `user_account` | `module_user_account` |
| identity | `RoleEntity` | `study_user_role` | `module_role` |
| subjectgroup | `StudyGroupClassEntity` | `study_group_class` | `module_study_group_class` |

**24 `@Primary @Component` adapter classes created** — all SPI interfaces are bridge to module-owned repositories:

| Module | Adapter | SPI | Backed By |
|--------|---------|-----|-----------|
| study | `StudyDaoAdapter` | `IStudyDAO` | `StudyRepository` |
| subject | `SubjectDaoAdapter` | `ISubjectDAO` | `SubjectRepository` |
| subject | `StudySubjectDaoAdapter` | `IStudySubjectDAO` | `StudySubjectRepository` |
| event | `StudyEventDaoAdapter` | `IStudyEventDAO` | `StudyEventRepository` |
| event | `StudyEventDefinitionDaoAdapter` | `IStudyEventDefinitionDAO` | repo |
| event | `EventCrfDaoAdapter` | `EventCRFDao` | repo |
| event | `EventDefinitionCrfDaoAdapter` | `EventDefinitionCRFDao` | repo |
| crf | `CrfDaoAdapter` | `ICrfDAO` | `CrfRepository` |
| crf | `CrfVersionDaoAdapter` | `ICrfVersionDAO` | `CrfVersionRepository` |
| crf | `ItemDaoAdapter` | `IItemDAO` | `ItemRepository` |
| crf | `SectionDaoAdapter` | `ISectionDAO` | `SectionRepository` |
| crf | `ItemFormMetadataDaoAdapter` | `IItemFormMetadataDAO` | `ItemFormMetadataRepository` |
| datacapture | `ItemDataDaoAdapter` | `IItemDataDAO` | `ItemDataRepository` |
| datacapture | `ItemGroupDaoAdapter` | `IItemGroupDAO` | repo |
| datacapture | `ItemGroupMetadataDaoAdapter` | `IItemGroupMetadataDAO` | repo |
| identity | `UserAccountDaoAdapter` | `IUserAccountDAO` | `UserAccountRepository` |
| rule | `RuleSetDaoAdapter` | `IRuleSetDAO` | repo |
| rule | `RuleDaoAdapter` | `IRuleDAO` | repo |
| dataset | `DatasetDaoAdapter` | `DatasetDao` | `DatasetRepository` |
| filter | `FilterDaoAdapter` | `FilterDao` | `FilterRepository` |
| subjectgroup | `StudyGroupClassDaoAdapter` | `StudyGroupClassDao` | `StudyGroupClassRepository` |
| subjectgroup | `StudyGroupDaoAdapter` | `StudyGroupDao` | repo |
| discrepancynote | `DiscrepancyNoteDaoAdapter` | `IDiscrepancyNoteDAO` | repo |
| datacapture | *(also)* `ItemFormMetadataDaoAdapter` | `IItemFormMetadataDAO` | `ItemFormMetadataRepository` |

**ALL 27 module entities now point to `module_*` tables. 12 bidirectional sync trigger migration files registered in release.xml.**

**+5 SPI families widened on 2026-06-05:** `IItemFormMetadataDAO`, `ISectionDAO`, `IItemGroupMetadataDAO`, `EventDefinitionCRFDao`, `ArchivedDatasetFileDao` — consumers in shared/ (5 files), web/ (4 files), ws/ (0 files) all converted to SPI types.

**DaoRegistrar exclusion updated:** `SectionDAO`, `ArchivedDatasetFileDAO` added to exclusion list.

**WebBeansConfig:** 0 `new XxxDAO()` calls remain. All DAO beans use `LegacyDaoFactory` or `@Primary` adapters. Only `new ArchivedDatasetFileDAO` was the last concrete construction, now replaced with `LegacyDaoFactory.archivedDatasetFileDao()`.

**Build verification:** `mvn clean compile` ✅ | `mvn test -pl app -am` 432/432 in latest project handoff ✅ | `ModulithVerificationTest` ✅

The table ownership declarations below are derived from the actual JPA entities in each module.

### B1: Table Ownership Declarations

Each Modulith module owns specific database tables. These declarations define the schema boundary for each module:

| Module | OWNED Tables | Entity | Repository | API Base Path |
|--------|-------------|--------|------------|---------------|
| **randomization** | `randomization_scheme`, `randomization_assignment`, `randomization_stratum`, `randomization_stratum_value`, `randomization_audit_log`, `randomization_statistics` + 2 more | 8 entities | 6 repos | `/api/v1/randomization` |
| **export** | `export_job` | `ExportJobEntity` | `ExportJobRepository` | `/api/v1/exports` |
| **audit** | `audit_log` | `AuditLog` | `AuditLogRepository` | `/api/v1/audit` |
| **study** | `study` | `StudyEntity` (`@Table(name = "study")`) | `StudyRepository` | `/api/v1/studies` |
| **subject** | `subject`, `study_subject` | `SubjectEntity`, `StudySubjectEntity` | `SubjectRepository`, `StudySubjectRepository` | `/api/v1/subjects` |
| **event** | `study_event`, `study_event_definition`, `event_crf` | `StudyEventEntity`, `StudyEventDefinitionEntity`, `EventCrfEntity` | `StudyEventRepository`, `StudyEventDefinitionRepository`, `EventCrfRepository` | `/api/v1/events` |
| **datacapture** | `item_data`, `response_set`, `item_group` | `ItemDataEntity`, `ResponseSetEntity`, `ItemGroupEntity` | `ItemDataRepository`, `ResponseSetRepository`, `ItemGroupRepository` | `/api/v1/data-capture` |
| **identity** | `user_account`, `study_user_role` | `UserAccountEntity`, `RoleEntity` | `UserAccountRepository`, `RoleRepository` | `/api/v1/identity` |
| **crf** | `crf`, `crf_version`, `item`, `section`, `item_form_metadata` | (LegacyCrfAdapter pattern) | None (anti-corruption only) | `/api/v1/crfs` |
| **openrosa** | OpenRosa REST/XML compatibility API | controller/service DTOs | no owned tables | `/api/v1/openrosa` |
| **legacy** | (gateway only, no owned tables) | — | — | `/api/v1/legacy/*` |

**Key constraint:** Modules with shared-schema tables must coordinate table changes with compatibility code. Module-owned repositories and adapters are the supported path; remaining legacy SPI callers must move to module-owned ports before the SPI interfaces can be deleted.

### B2: Schema Migration Strategy

Two options for graduating from SHARED to OWNED:

**Option A: Add module-owned columns** (lower risk, incrementally adds to existing tables)
- New columns get `module_` prefix: `module_study_uuid`, etc.
- Write operations go to new columns; legacy code still sees old columns
- Data migration script copies legacy data to new columns
- **Best for:** study, subject, event (highly coupled with legacy code)

**Option B: New tables with sync** (cleaner, but more work)
- Create `module_study`, `module_study_subject`, etc.
- Dual-write during transition (legacy DAO + new repository)
- Cron job or trigger keeps tables in sync
- Cut over when confident
- **Best for:** randomization, audit, export (already own their tables)

**Recommendation:** Option A for the first SHARED module migration, Option B for subsequent ones.

### B3: Module API Contracts

Modules communicate via:
1. **REST APIs** (HTTP JSON) — for synchronous CRUD operations
2. **ApplicationEvents** — for asynchronous cross-module notifications
3. **Direct Repository access** — only within the module's boundary (never across modules)

**Restricted imports (enforced by ModulithVerificationTest):**
- No module may import `org.researchedc.bean.*` (legacy DTOs)
- No module may import `org.researchedc.dao.*` (legacy DAOs)
- No module may import `org.researchedc.domain.*` (legacy entities) — use adapter instead
- Allowed: `org.researchedc.module.<name>.*` (other modules' public API via events only)

---

## Phase C: Legacy Code Deletion (DAO .java files remain — blocked by remaining concrete DAO dependencies and module extraction)

> **Status (2026-06-02):** Phase C DAO SPI widening is **COMPLETE**. All 24 DAO families are SPI-widened. `DaoProvider.getDao()` and direct `new XxxDAO(...)` / `new StudyConfigService(...)` call sites remain 0. The DAO `.java` files in `shared/` still cannot be deleted because they are the current SPI implementations. All consumer references in web/ (45+ files), shared/ (15+ files), and ws/ (0 files) now use SPI interfaces. Remaining concrete type names are limited to DAO implementation classes, `LegacyDaoFactory`, and commented-out code — all harmless.
>
> **Completed SPI Widening — 19 families:**
> - ✅ `StudyDAO` → `IStudyDAO` — boundary-only; concrete refs limited to impl, `LegacyDaoFactory`
> - ✅ `StudySubjectDAO` → `IStudySubjectDAO` — boundary-only
> - ✅ `SubjectDAO` → `ISubjectDAO` — boundary-only
> - ✅ `UserAccountDAO` → `IUserAccountDAO` — boundary-only (except `UserAccountAdapter`)
> - ✅ `CRFDAO` → `ICrfDAO` — `460fab3f2`, `01efa4b05`
> - ✅ `CRFVersionDAO` → `ICrfVersionDAO` — ~20 commits across shared/web/ws (`9da44e612`–`1e337b75b`)
> - ✅ `DiscrepancyNoteDAO` → `IDiscrepancyNoteDAO` — `0e47f8872`, `ec1b9b0d9`
> - ✅ `EventCRFDAO` → `EventCRFDao` — `315d3cdf4`
> - ✅ `ItemDAO` → `IItemDAO` — `8b90a2601`; `ExpressionService` consumer widened
> - ✅ `ItemDataDAO` → `IItemDataDAO` — `1b409b230`, `962726f2d`; `ExpressionService` + `RuleRunner` consumers widened
> - ✅ `ItemGroupDAO` → `IItemGroupDAO` — `f9d7d5d65`; `ExpressionService` consumer widened
> - ✅ `StudyEventDAO` → `IStudyEventDAO` — ~10 commits (`6896446c1`–`df4a832e5`); 45 web/+ 15 shared/ consumer files all SPI-typed
> - ✅ `StudyEventDefinitionDAO` → `IStudyEventDefinitionDAO` — ~10 commits (`868a4f6fa`–`968391b3b`); 40+ web/+ 15+ shared/ consumer files all SPI-typed
> - ✅ `RuleSetDAO` → `IRuleSetDAO` — `cf22f06d2`, `579cbfab0`; 4 web/ + 4 shared/ consumers all SPI-typed
> - ✅ `RuleDAO` → `IRuleDAO` — `62595dd32`; 1 web/ + 3 shared/ consumers all SPI-typed
> - ✅ `DatasetDAO` → `DatasetDao` — `d374b275c`; web/ has commented code only, shared/ uses SPI
> - ✅ `FilterDAO` → `FilterDao` — web/ has commented code only, all consumers use SPI
> - ✅ `StudyGroupClassDAO` → `StudyGroupClassDao` — 4 shared/ consumers all SPI-typed
> - ✅ `StudyGroupDAO` → `StudyGroupDao` — 3 shared/ consumers all SPI-typed
>
> **Completed in 2026-06-02 (remaining 8 DAO families SPI-widened):**
> - ~20 commits (`868a4f6fa`–`968391b3b`) widened `StudyEventDAO`, `StudyEventDefinitionDAO`, `RuleSetDAO`, `RuleDAO`, `DatasetDAO`, `FilterDAO`, `StudyGroupClassDAO`, and `StudyGroupDAO` consumers across shared/web/ws from concrete types to SPI interfaces.
> - All 45+ web/ consumer files, 15+ shared/ consumer files, and 0 ws/ consumer files now use SPI-typed fields exclusively.
> - Remaining concrete type names (DAO impl classes, `LegacyDaoFactory`, `OdmExtractDAO extends DatasetDAO`, commented-out code in 3 extract servlets) are harmless — none represent active concrete consumer dependencies.
> - Verified: `mvn -pl app -am compile -DskipTests` ✅, `ModulithVerificationTest` ✅, clean working tree.
>
> **Completed in Sequence 18-19 (2026-05-23):**
> - **DaoProvider** (`legacy-core/.../dao/spi/DaoProvider.java`): static Spring context bridge for legacy servlets/SOAP endpoints
> - **DaoProviderInitializer**: Spring `ApplicationListener` wiring DaoProvider at startup
> - **ServiceConfig**: All 28 SPI DAO interfaces now registered as `@Bean` (13 were missing)
> - **1,710 batch replacements**: `new XxxDAO(dataSource)` → `DaoProvider.getDao(XxxDAO.class)` across 28 DAO types in 237 files (web/ + ws/)
> - **`mvn compile`** ✅ | **`ModulithVerificationTest`** ✅
> - **Phase D cleanup**: 2 dead XML configs deleted, 2 stripped to minimal stubs
> - **28 SPI Impl wrappers DELETED** — all `extends` inheritance from app/ to legacy-core DAOs eliminated
> - **22 hardcoded userId=1** replaced with `CurrentUserUtils.getCurrentUserId()` JWT extraction (11 controllers)
> - **CurrentUserUtils** created at `config/CurrentUserUtils.java` — dual-mode auth (JWT + session)
> - **7 module tests** added for previously untested services (Rule, Dataset, Filter, SubjectGroup, DiscrepancyNote, Crf, Audit)
>
> **Completed in 2026-05-27 legacy constructor migration:**
> - **0 `DaoProvider.getDao()` call sites** remain across app/web/ws/shared.
> - Base servlet/controller paths now receive DAOs and `StudyConfigService` through Spring injection instead of `DaoProvider`.
> - Rule-runner action processors moved to injected collaborators through `RuleSetService`/`RuleRunner`/`ActionProcessorFacade`; the high-volume study/subject/user-account collaborators were later widened to SPI where covered.
> - `StudySubjectServiceImpl`, `ParticipantEventService`, `JobTriggerService`, `SubjectTransferValidator`, `SetUpStudyRole`, and `MetadataCollectorResource` have been moved toward injected collaborators; the former `ApiSecurityFilter` compatibility class was later removed as dead surface.
> - The app-hosted import bridge later dropped its dead `DataSource` seam as `ImportCRFDataService` shed preview-only helpers, then pulled commit item shaping, study metadata/OID validation, and the remaining event/status compatibility logic into `ImportCrfDataAdapter`; that final slice deleted `ImportCRFDataService` and brought non-adapter legacy DAO/SPI imports in `app/src/main/java` down to zero.
> - A follow-up cleanup deleted the remaining dead `app/src/main/java/org/researchedc/web/*` classes (`ImportHelper` and `OpenClinicaLdapAuthoritiesPopulator`), leaving no Java sources under the former app-hosted `org.researchedc.web` compatibility package.
> - **`mvn -pl app -am compile -DskipTests`** ✅
>
> **Completed in 2026-05-29 SPI consumer widening:**
> - Commit `10f0f6ea2` (`Refactor legacy DAO consumers to SPI`) widened high-volume `StudyDAO`, `StudySubjectDAO`, `SubjectDAO`, and `UserAccountDAO` consumers across shared/web/ws/app to `IStudyDAO`, `IStudySubjectDAO`, `ISubjectDAO`, and `IUserAccountDAO` where the SPI already covered the calls.
> - Added `app/src/main/java/org/researchedc/config/DaoRegistrar.java` for central DAO bean registration and kept default manual construction contained in `shared/src/main/java/org/researchedc/dao/LegacyDaoFactory.java`; later cleanup removed `DaoRegistrar` once no concrete DAO classes remained in `shared/src/main/java/org/researchedc/dao`.
> - `LegacyDaoFactory` now exposes SPI-returning factories for study, study-subject, subject, and user-account DAOs.
> - The now-deleted `OidcSessionBridgeSuccessHandler` and then-active legacy validators/controllers were widened from concrete `UserAccountDAO` usage to `IUserAccountDAO`; later cleanup removed the dead handler and dead DAO-backed `Validator` branches with no remaining callers.
> - Current concrete references for this DAO family are boundary-only: DAO implementation classes, `LegacyDaoFactory`, and the remaining WS `UserAccountAdapter` containment wrapper.
> - Follow-up, an earlier WS CRF slice widened `CrfBusinessLogicHelper`, the since-deleted `ImportCRFDataService`, and `StudyEventDefinitionEndpoint` from concrete `CRFDAO` fields/imports to `ICrfDAO`; `mvn -pl app -am compile -DskipTests` and `git diff --check` passed before the documentation refresh.
>
> **Next steps for DAO deletion:**
> 1. Replace concrete DAO typed fields/parameters in legacy services and helpers with module-owned service ports.
> 2. Keep DAO-heavy compatibility workflows behind Modulith APIs while Phase 3 DAO deletion proof continues.
> 3. Only then can the legacy DAO `.java` files be assessed for safe deletion.

### C0: Already Deleted (Safe Cleanup)
- ❌ **Ehcache 2 XML configs** — removed (Caffeine migration completed)
- ❌ **maven-jaxb2-plugin 0.7.5** — dead config removed
- ❌ **9 Spring XML configs** — `applicationContext-core-{annotation-scheduler,db,email,hibernate,scheduler,security,service,spring,timer}.xml` — replaced by Java @Configuration classes and deleted
- ❌ **2 dead adapters** — `LegacyStudyAdapter`, `LegacySubjectAdapter` — injected but never called
- ❌ **28 SPI Impl wrappers** — all deleted, app/ no longer extends legacy-core DAOs
- ❌ **22 hardcoded userId=1** — replaced with JWT extraction across 11 controllers

### C1: DAO Files Still Present (Blocked by remaining concrete consumers)

Latest Phase 3 ledger checkpoint (2026-06-17): overall ledger status is 720 `module-backed`, 0 `unused`, and 158 `removed` across 878 tracked methods. DAO SPI deletion is blocked by caller migration from legacy SPI names to module-owned ports.

The following DAO `.java` files still exist in `shared/`. As of 2026-06-02, **0 `DaoProvider.getDao()` call sites** and **0 direct `new XxxDAO(...)` / `new StudyConfigService(...)` matches** remain across app/web/ws/shared. **All 24 DAO families** are SPI-widened. All DAO `.java` files must remain because they are the current SPI implementations; deletion is blocked by the need for module-owned replacements and workflow strangulation.

| DAO File | SPI Status | Can delete? |
|----------|-----------|-------------|
| `StudyDAO.java` | ✅ `IStudyDAO` — boundary-only | ❌ Still the SPI implementation |
| `StudySubjectDAO.java` | ✅ `IStudySubjectDAO` — boundary-only | ❌ Still the SPI implementation |
| `SubjectDAO.java` | ✅ `ISubjectDAO` — boundary-only | ❌ Still the SPI implementation |
| `UserAccountDAO.java` | ✅ `IUserAccountDAO` — boundary-only (except WS adapter) | ❌ Still the SPI implementation and adapter delegate |
| `CRFDAO.java` | ✅ `ICrfDAO` — consumers widened | ❌ Still the SPI implementation |
| `CRFVersionDAO.java` | ✅ `ICrfVersionDAO` — consumers widened (~20 commits) | ❌ Still the SPI implementation |
| `DiscrepancyNoteDAO.java` | ✅ `IDiscrepancyNoteDAO` — consumers widened | ❌ Still the SPI implementation |
| `EventCRFDAO.java` | ✅ `EventCRFDao` — consumers widened | ❌ Still the SPI implementation |
| `ItemDAO.java` | ✅ `IItemDAO` — consumers widened | ❌ Still the SPI implementation |
| `ItemDataDAO.java` | ✅ `IItemDataDAO` — consumers widened | ❌ Still the SPI implementation |
| `ItemGroupDAO.java` | ✅ `IItemGroupDAO` — consumers widened | ❌ Still the SPI implementation |
| `StudyEventDAO.java` | ✅ `IStudyEventDAO` — consumers widened (~10 commits) | ❌ Still the SPI implementation |
| `StudyEventDefinitionDAO.java` | ✅ `IStudyEventDefinitionDAO` — consumers widened (~10 commits) | ❌ Still the SPI implementation |
| `RuleSetDAO.java` | ✅ `IRuleSetDAO` — consumers widened | ❌ Still the SPI implementation |
| `RuleDAO.java` | ✅ `IRuleDAO` — consumers widened | ❌ Still the SPI implementation |
| `DatasetDAO.java` | ✅ `DatasetDao` — consumers widened | ❌ Still the SPI implementation |
| `FilterDAO.java` | ✅ `FilterDao` — consumers widened | ❌ Still the SPI implementation |
| `StudyGroupClassDAO.java` | ✅ `StudyGroupClassDao` — consumers widened | ❌ Still the SPI implementation |
| `StudyGroupDAO.java` | ✅ `StudyGroupDao` — consumers widened | ❌ Still the SPI implementation |

### C2: Bean Deletion Order (Deferred until web/ servlets migrated)

### C3: Web Servlet Deletion (Deferred — ~186 servlets remain in web/)

### C4: Safe Deletion Checklist (Gate to actual DAO .java deletion)

Before deleting any legacy DAO `.java` file, verify ALL of:
- [x] **✅ Corresponding module has read REST API (proven working)** — All 11 modules have read endpoints
- [x] **✅ Corresponding module has write REST API (proven working)** — All 11 modules have write endpoints
- [x] **✅ No web/ servlet or ws/ endpoint still directly instantiates DAO with `new`** — ALL 1,710 eliminated (2026-05-23)
- [x] **✅ 28 SPI Impl wrappers refactored from `extends` to delegation** — All deleted, ServiceConfig updated (2026-05-23)
- [x] **✅ `mvn compile` passes** — Verified
- [x] **✅ `ModulithVerificationTest` passes** — Verified
- [x] **✅ No Spring XML config references the deleted class** — 9 XML files deleted, remaining stubs are minimal
- [x] **✅ `DaoProvider.getDao()` call sites migrated away** — 0 matches across app/web/ws/shared (2026-05-27)
- [x] **Direct `new XxxDAO(...)` / `new StudyConfigService(...)` matches eliminated** — 0 matches across shared/web/ws (2026-05-28)
- [ ] **3 `@Deprecated(forRemoval=true)` DAO classes removed** — StudyDAO, StudySubjectDAO, SubjectDAO (each has 100+ callers)

### C5: @Deprecated DAO Assessment (Plan 4)

Three DAO classes are marked `@Deprecated(since="3.18", forRemoval=true)`:

| DAO | Direct constructor sites | DaoProvider.getDao() calls | Concrete consumer refs | Status |
|-----|---------------------|---------------------------|------------------------|--------|
| **StudyDAO** | 0 direct constructor matches | 0 | Boundary-only (`StudyDAO.java`, `LegacyDaoFactory`) | ❌ Cannot delete until replacement SPI implementation exists |
| **StudySubjectDAO** | 0 direct constructor matches | 0 | Boundary-only (`StudySubjectDAO.java`, `LegacyDaoFactory`) | ❌ Cannot delete until replacement SPI implementation exists |
| **SubjectDAO** | 0 direct constructor matches | 0 | Boundary-only (`SubjectDAO.java`, `LegacyDaoFactory`) | ❌ Cannot delete until replacement SPI implementation exists |

**Total:** 0 direct legacy constructor matches remain across app/web/ws/shared.
**All three must remain** until module-owned/repository-backed SPI implementations replace the deprecated DAO classes and the remaining legacy workflows are strangulated.

---

## Phase D: Configuration Migration ✅ COMPLETE

### D1: Spring XML → Java Config ✅
All 11 `applicationContext-*.xml` files have been replaced by Java `@Configuration` classes. Zero `@ImportResource` annotations remain.

| File | Status |
|------|--------|
| `applicationContext-core-spring.xml` | ✅ `CoreResourcesConfig.java` |
| `applicationContext-core-db.xml` | ✅ `DbConfig.java` |
| `applicationContext-core-hibernate.xml` | ✅ `HibernateConfig.java` |
| `applicationContext-core-security.xml` | ✅ `SecurityConfig.java` |
| `applicationContext-core-service.xml` | ✅ `ServiceConfig.java` |
| `applicationContext-core-email.xml` | ✅ `MailConfig.java` |
| `applicationContext-core-scheduler.xml` | ✅ `SchedulingConfig.java` |
| `applicationContext-core-annotation-scheduler.xml` | ✅ (already annotation-based) |
| `applicationContext-core-timer.xml` | ✅ merged into `SchedulerConfig.java` |
| `applicationContext-security.xml` | ✅ `SecurityConfig.java`, file stripped to stub (2026-05-23) |
| `applicationContext-web-beans.xml` | ✅ `WebBeansConfig.java`, file stripped to stub (2026-05-23) |

Cleanup (2026-05-23): `application-context-web-beans.xml` deleted (duplicate stub). `ws/applicationContext-web-beans.xml` deleted (empty).

### D2: Ehcache 2 → Caffeine ✅
- Hibernate second-level cache: migrated to `jcache`
- Application cache: Spring `@Cacheable` with Caffeine `CacheManager`
- Ehcache XML configs: removed

---

## Phase E: Authentication Unification (Priority: High)

### E1: Current State
- JSP pages: Spring Security 3.x with legacy `SecureController` session-based auth
- React SPA: Keycloak OIDC with JWT tokens
- SOAP endpoints: WS-Security with password callback
- Python questionnaire: Keycloak JWT validation

### E2: Migration Steps
1. Deploy Keycloak Tomcat Adapter for JSP pages (Phase 2)
2. Add JWT filter that bridges to `SecureController` session
3. Add Spring Security 6 Resource Server config as `@Configuration`
4. Remove old `spring-security-oauth2` dependency
5. Remove `spring-security-oauth2` 2.0.x from `pom.xml`

---

## Phase F: SOAP Endpoint Adapter (Infrastructure Built ✅)

### F1: Current State
7 SOAP endpoints in `ws/` are fully coupled to legacy core DAOs. Created adapter infrastructure to decouple them.

### F2: Migration Completed
- Created `ws/internal/adapter/` package with clean repository facade pattern
- **UserAccountAdapter** — wraps `UserAccountDAO.findByPK()`, `findByUserName()` (used by ALL 6 SOAP endpoints)
- **StudyAdapter** — wraps `StudyDAO.findByPK()` (used by StudyEndpoint, StudySubjectEndpoint, StudyEventDefinitionEndpoint)
- **StudySubjectAdapter** — wraps `StudySubjectDAO.findByPK()`, `findByLabelAndStudy()` (used by StudySubjectEndpoint)
- Each adapter delegates to the existing DAO through a clean `@Repository` interface
- Pattern: adapters can be individually swapped to JdbcTemplate or module REST API calls
- Adapter DAO map: UserAccountDAO → study_user_role + user_account queries,
  StudyDAO → study queries, StudySubjectDAO → study_subject queries

---

## Phase G: JSP Strangulation (Infrastructure Built ✅ — Deletion NOT Complete)

### G1: Current State
The `web/` module and JSP pages are gone from the current tree. React SPA now replaces or provides:
- ✅ Dashboard
- ✅ CRF list/preview
- ✅ Data export
- ✅ Randomization
- ✅ Questionnaires
- ✅ **Subject management** (new — replaces ~40 `managestudy/` JSPs)
- ✅ **Hybrid Shell** (LegacyFrame iframe component — all remaining JSPs accessible via `/app/legacy/*`)
- ✅ **Feature flags** (study-level JSONB column + API — enables per-study rollout)
- ✅ **Subject list + detail pages** (CRUD via REST API)

### G2: Infrastructure Delivered

| Component | Status | Details |
|-----------|--------|---------|
| **Hybrid Shell (LegacyFrame)** | ✅ | Iframe embed, loading/error states, retry, sandbox isolation |
| **Router integration** | ✅ | `path: "legacy/*"` under `/app` → maps to `/legacy/<path>` on backend |
| **Feature flag system** | ✅ | JSONB column on `study` table, PUT/GET endpoints, `useFeatureFlags` hook |
| **Subject List page** | ✅ | Table view, create + enroll modal, study context, status tags |
| **Subject Detail page** | ✅ | Profile card, enrollment info, events table, schedule action |
| **Study List page** | ✅ | Studies/sites tabs, create study, select study context |
| **Event List page** | ✅ | List events, schedule, complete, status tags |
| **User Management page** | ✅ | List/create users, role assignment modal with tabs |
| **Admin Audit Log page** | ✅ | Global audit log, module filter, type color coding |
| **System Config page** | ✅ | Health status, version info, component status dashboard |
| **Admin Dashboard** | ✅ | Admin landing page with navigation cards to all admin sections |
| **CRF Admin page** | ✅ | CRF library listing with version explorer, links to preview |
| **Legacy Gateway API** | ✅ | `LegacyStudyController`, `LegacySubjectController` — REST wrappers |
| **Study management page** | ✅ | StudyList/Create/Detail/Edit/Sites via React |
| **Event management page** | ✅ | EventList/Schedule/Complete via React |
| **Data entry page** | ✅ | DataEntryPage with form engine, auto-save, discrepancy notes |
| **Admin pages** | ✅ | UserManagement, AuditLogViewer, SystemConfig, CRFAdmin, JobManager, ImportManager, PasswordPolicy, LogViewer, EntityAction, Instructions |

### G3: Page Migration Roadmap

SPA replacement coverage exists for major workflows, and physical JSP/servlet deletion is complete in the current tree: `web/` is absent, JSP count is 0, and legacy servlet inventory is 0. Treat the list below as workflow coverage context; remaining deletion proof is concentrated in DAO/shared-service compatibility.

#### Confirmed Coverage (JSP files physically deleted in Phase 1 slices)

| Area | JSPs Deleted | SPA Replacement |
|------|-------------|-----------------|
| `managestudy/study/*` | ~20 | React StudyList/Create/Detail/Edit/Sites |
| `managestudy/subject/*` | ~40 | React SubjectList + SubjectDetail |
| `managestudy/event/*` | ~15 | React EventList/Schedule/Complete |
| `admin/user/*` | ~15 | React UserManagement |
| `admin/audit/*` | ~5 | React AuditLogViewer |
| `admin/system/*` | ~5 | React SystemConfiguration |
| `admin/crf/*` | ~20 | CrfAdmin + CRF version management |
| `admin/jobs/*` | ~10 | JobManager |
| `admin/rest/*` | ~14 | EntityAction page |
| `admin/other/*` | ~5 | PasswordPolicy, LogViewer, StudyUserRoleEditor |
| `extract/*` | ~50 | ExportCenter, DatasetBuilder, FilterBuilder |
| `include/*` (deleted) | ~61 | React AppLayout shell |
| **Total deleted** | **~260** | |

#### SPA Pages Exist But Legacy NOT Deleted (Blocked by Missing Features)

| Area | Remaining JSPs | Blocked By |
|------|---------------|------------|
| `submit/` / CRF rendering | 26 data-entry/discrepancy artifacts plus 11 CRF-metadata candidates | Full CRF rendering (sections, repeating groups, discrepancy notes, rule execution), double data entry mode, file attachments, CRF print |
| `login/` (auxiliary) | 0 | Deleted on 2026-06-09; stale RequestAccount/RequestStudy/Contact redirects and SPA request page removed on 2026-06-10. |
| `login/` (profile/password) | 0 active `phase-1-login-profile` artifacts | Listed ledger is historical; account-like compatibility routes are tracked under study/subject/event where still active |
| `submit/` (import/export compatibility) | 10 artifacts | Step-by-step import wizard with validation preview, rule import, ODM/OpenRosa/export compatibility |
| `include/` / layout common | 6 classified artifacts | Delete only after all include references are gone |

#### SPA Action / Compatibility Gaps

| Area | Current Status | Next Action |
|------|----------------|-------------|
| `EntityAction.tsx` | Study-subject, study-event, and event-CRF remove/restore covered on 2026-06-11 via module REST | Reopen only for concrete unsupported entity types found by inventory or route testing |
| Import/export compatibility | SPA `ImportManager` exists but legacy import/export artifacts remain | Prove upload -> validate -> map -> commit parity, rule XML import, import job scheduling, and ODM/OpenRosa/export contracts |

#### Orphan SPA Components (built but not wired to routes)

| Component | Action Needed |
|-----------|--------------|
| `components/questionnaire-builder/QuestionnaireBuilder.tsx` | Wire as route `questionnaires/builder` |

#### Feature Flag System

Available at `GET/PUT /api/v1/studies/:id/feature-flags` (JSONB on `study` table). Hook: `useFeatureFlags()`. Currently has zero default flags. Recommended migration flags: `spa_data_entry`, `spa_e_signature`, `spa_import`, `spa_subject_detail`.

---

## Phase H: Data Migration (✅ COMPLETE)

### H1: Audit Log Data ✅
- The new `audit_log` table exists and is empty
- Legacy `audit_log_event` table has years of historical data
- Migration script written: `migration/3.18/2026-05-20-audit-migration.xml`
  - Maps legacy `audit_log_event_type_id` (1-43) to `AuditEventType` enum values (CREATE/UPDATE/DELETE/LOCK/SYSTEM)
  - Preserves all fields: audit_date→performed_date, audit_table→entity_type, entity_name→entity_label, reason_for_change→details
  - Sets `source_module = 'legacy'` for migrated records
  - Idempotent: uses LEFT JOIN to skip already-migrated records
  - Includes rollback: `DELETE FROM audit_log WHERE source_module = 'legacy'`
  - Logs migration completion count as a SYSTEM audit record

### H2: Other Data
- No other data migration needed — new modules read existing tables
- Schema ownership migration (Phase B) will handle table migration when ready

---

## Effort Estimate

| Phase | Description | Estimated Effort | Dependencies |
|-------|-------------|-----------------|--------------|
| A1-A5 | Write operations | ✅ COMPLETE | None |
| B1-B3 | Schema ownership | ✅ COMPLETE | 12 bidirectional sync triggers, 27 entities remapped, 24 adapters |
| C1-C4 | Legacy code deletion | 🔶 In progress, not complete | **0 direct legacy constructor matches, 0 `DaoProvider.getDao()` calls, 24/24 DAO families SPI-widened. Workflow progress is 924/963 closed (96.0%). Physical deletion remains blocked by legacy SPI caller migration to module-owned ports.** |
| D1-D2 | Config migration | ✅ Complete | 11 XML → Java Config, dead XML stubs cleanup (2026-05-23) |
| E1-E2 | Auth unification | ✅ Complete | Dual SecurityFilterChain (JWT API + OIDC web) |
| F1-F2 | SOAP retirement | ✅ Current tree retired | `ws/` is absent; keep compatibility audit only if SOAP endpoints reappear |
| G1-G3 | JSP strangulation | ✅ Complete in current tree | `web/` is absent; JSP pages and legacy servlet inventory are 0. |
| H1 | Data migration | ✅ COMPLETE | Phase A complete |
| **S1** | **Contract tests** | **✅ COMPLETE** | **41 MockMvc tests for 8 legacy-gateway controllers** |
| **S2** | **Service tests** | **✅ COMPLETE** | **47 new tests + 25 frontend + 31 questionnaire** |

**Total Java tests: 150 → 161 (0 failures) — 11 new tests for Phase B adapters**  
**Module test coverage: 10 modules with baseline tests**  
**DAO instantiation coverage: direct legacy DAO and `StudyConfigService` construction is 0 active; next metric is concrete DAO type references by family.**

---

## Risk Register

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Breaking JSP pages during write operation rollout | High | Medium | Add feature flags; deploy behind `/api/v2/*` routes |
| Data inconsistency during dual-write period | High | Medium | Add reconciliation check script |
| Rules engine (57 files) too complex to extract | High | High | Treat as separate project; keep in legacy until clear extraction path |
| Quartz job dependencies on legacy DAOs | Medium | Medium | Audit all 9 jobs before deleting any DAO |

## Legacy Dependency Impact Summary

| Dependency | Used In | Usage | Removal Strategy |
|------------|---------|-------|-----------------|
| **JMesa** 2.5.2 | historical `legacy-core`/`web` use | Retired JSP table rendering | Removed with JSP strangulation; keep absent. |
| **Castor** 1.4.1 | `shared`, `web` | ODM XML export in MetaDataReportBean, rule handlers (OidHandler, EmailHandler) | Remove with export/import migration; SOAP is already absent |
| **OpenClinica ODM** 2.2 | `legacy-core` | Internal ODM data model (bean/odmbeans/) | Remove with export/import module migration |
| **Sitemesh** | historical `web` use | Retired JSP page decoration | Removed with JSP strangulation; keep absent. |
| **Rome/RSS** | `web` | RSS feed URL display on legacy dashboard | Low impact, remove with JSP |
| **MVEL** | `legacy-core` | Expression language — only commented-out import | Safe to remove (verify no runtime reflection) |
| **Commons BeanUtils / Validator** | All modules | General utility libraries | Low risk, replace incrementally |
| **Apache POI / JExcel** | `shared`, `web` | Excel export for legacy reporting | Remove with export module migration |

---

## Quick Wins — All Complete ✅

1. ✅ **Commons Lang 2 → 3**: Global `org.apache.commons.lang` → `org.apache.commons.lang3` package rename
2. ✅ **Commons Collections 3 → 4**: Global `org.apache.commons.collections` → `org.apache.commons.collections4`
3. ✅ **Javassist 3.8 removal**: Removed unused dependency from `pom.xml`
4. ✅ **Maven plugin upgrades**: `maven-surefire-plugin:3.2.5`, `maven-failsafe-plugin:3.2.5`, `maven-release-plugin:3.0.1`, `maven-assembly-plugin:3.7.1`, `cargo-maven2-plugin:1.10.13`, `liquibase-maven-plugin:4.28.0`
5. ✅ **OWASP Dependency Check**: `dependency-check-maven:10.0.4` configured in CI with suppressions
6. ✅ **Trivy scanning**: `.trivy-config.yml` + CI scan job + `scripts/scan.sh` for image/filesystem/SBOM
7. ✅ **Maven BOM**: `research-edc-bom` created with centralized version management
8. ✅ **Ehcache 2 → Caffeine**: Full migration completed (Hibernate 2LC + application cache)
9. ✅ **maven-jaxb2-plugin**: Dead `0.7.5` config removed from parent POM (WS module uses `jaxb-maven-plugin:4.0.6`)
10. ✅ **Audit wiring**: `AuditService.recordAudit()` wired into all 5 write-capable modules (Study, Subject, Event, DataCapture, Identity)
