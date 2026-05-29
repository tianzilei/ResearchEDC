# OpenClinica Legacy Code Refactoring Plan

> **Last updated:** 2026-05-29 (Phase C resumed: `DaoProvider.getDao()` and direct legacy DAO/`StudyConfigService` construction remain 0. The first high-volume DAO family, `StudyDAO` / `StudySubjectDAO` / `SubjectDAO` / `UserAccountDAO`, has been widened to SPI at consumer sites and is now boundary-only; DAO deletion remains blocked by concrete DAO implementation classes, adapters, and broader module extraction.)
> **Scope:** All remaining legacy code in `shared/`, `web/`, `ws/`
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

### Remaining legacy code (~1,274 files)

```
shared/   ~770 files →  bean/  dao/  domain/  service/  logic/  job/  exception/  validator/  i18n/  patterns/  core/  log/
web/      ~484 files →  control/ (186 servlets)  controller/ Spring MVC
            + 419 JSP pages
ws/        ~75 files →  SOAP endpoints + validators + beans + logic + client
```

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

## Phase B: Schema Ownership (Documented ✅ — Ready for Implementation)

Currently modules bridge to the **same tables** that legacy code uses. Full strangulation requires module-owned tables. The table ownership declarations below are derived from the actual JPA entities in each module.

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
| **notification** | (event-driven, no owned tables) | — | — | (event-driven) |
| **legacy** | (gateway only, no owned tables) | — | — | `/api/v1/legacy/*` |

**Key constraint:** Modules with SHARED tables share them with legacy `core/domain/*` entities. These tables must not be altered by the module without coordinating with legacy code. The module writes to these tables via JPA Repository; legacy code reads/writes via `EntityDAO` subclasses. Both paths are in use until legacy DAOs are deleted.

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

## Phase C: Legacy Code Deletion (DAO .java files remain — blocked by concrete DAO dependencies and module extraction)

> **Status (2026-05-29):** Phase C is complete for app/ module cleanup and is now focused on legacy consumer type narrowing. All 28 SPI Impl wrapper files have been deleted. `DaoProvider.getDao()` and direct `new XxxDAO(...)` / `new StudyConfigService(...)` call sites remain 0. Web/ws/base-controller, rule-runner, study/subject/user-account consumers, and related validators now use injected SPI collaborators instead of the removed static provider bridge. 22 hardcoded userId=1 instances across 11 modulith controllers have been replaced with CurrentUserUtils JWT extraction. 7 new module test files have been written.
>
> **Remaining blocker:** The direct `new XxxDAO(...)` / `new StudyConfigService(...)` matches have been eliminated, and `StudyDAO` / `StudySubjectDAO` / `SubjectDAO` / `UserAccountDAO` concrete consumer references are now boundary-only. The DAO `.java` files in `shared/` still cannot be deleted because they are the current SPI implementations, a few adapters still wrap concrete classes, and broader legacy services, DAO internals, ODM/export/import logic, validators, and web/ws helpers still depend on other concrete DAO types. These must be migrated to module-owned services or repository-backed ports before DAO deletion.
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
> - `StudySubjectServiceImpl`, `ParticipantEventService`, `JobTriggerService`, `ApiSecurityFilter`, `SubjectTransferValidator`, `SetUpStudyRole`, and `MetadataCollectorResource` have been moved toward injected collaborators.
> - **`mvn -pl app -am compile -DskipTests`** ✅
>
> **Completed in 2026-05-29 SPI consumer widening:**
> - Commit `10f0f6ea2` (`Refactor legacy DAO consumers to SPI`) widened high-volume `StudyDAO`, `StudySubjectDAO`, `SubjectDAO`, and `UserAccountDAO` consumers across shared/web/ws/app to `IStudyDAO`, `IStudySubjectDAO`, `ISubjectDAO`, and `IUserAccountDAO` where the SPI already covered the calls.
> - Added `app/src/main/java/org/researchedc/config/DaoRegistrar.java` for central DAO bean registration and kept default manual construction contained in `shared/src/main/java/org/researchedc/dao/LegacyDaoFactory.java`.
> - `LegacyDaoFactory` now exposes SPI-returning factories for study, study-subject, subject, and user-account DAOs.
> - `OidcSessionBridgeSuccessHandler` and legacy validators/controllers now resolve user-account access through `IUserAccountDAO` instead of concrete `UserAccountDAO`.
> - Current concrete references for this DAO family are boundary-only: DAO implementation classes, `LegacyDaoFactory`, and the remaining WS `UserAccountAdapter` containment wrapper.
> - Follow-up, currently uncommitted WS CRF slice widens `CrfBusinessLogicHelper`, `ImportCRFDataService`, and `StudyEventDefinitionEndpoint` from concrete `CRFDAO` fields/imports to `ICrfDAO`; `mvn -pl app -am compile -DskipTests` and `git diff --check` passed before the documentation refresh.
>
> **Next steps for DAO deletion:**
> 1. Replace concrete DAO typed fields/parameters in legacy services and helpers with module-owned service ports.
> 2. Move remaining DAO-heavy web/ws workflows behind Modulith APIs.
> 3. Only then can the legacy DAO `.java` files be assessed for safe deletion.

### C0: Already Deleted (Safe Cleanup)
- ❌ **Ehcache 2 XML configs** — removed (Caffeine migration completed)
- ❌ **maven-jaxb2-plugin 0.7.5** — dead config removed
- ❌ **9 Spring XML configs** — `applicationContext-core-{annotation-scheduler,db,email,hibernate,scheduler,security,service,spring,timer}.xml` — replaced by Java @Configuration classes and deleted
- ❌ **2 dead adapters** — `LegacyStudyAdapter`, `LegacySubjectAdapter` — injected but never called
- ❌ **28 SPI Impl wrappers** — all deleted, app/ no longer extends legacy-core DAOs
- ❌ **22 hardcoded userId=1** — replaced with JWT extraction across 11 controllers

### C1: DAO Files Still Present (Blocked by web/ws consumption)

The following DAO `.java` files still exist in `shared/`. As of 2026-05-29, **0 `DaoProvider.getDao()` call sites** and **0 direct `new XxxDAO(...)` / `new StudyConfigService(...)` matches** remain across app/web/ws/shared. The study/subject/user-account DAO family is boundary-only at concrete type level, but the DAO files survive because they implement the SPI contracts and because other concrete DAO field/type dependencies remain in legacy services, DAO internals, ODM/export/import logic, validators, and web/ws helpers.

| DAO File | SPI exposure | `new` eliminated | Can delete? |
|----------|--------------|-------------------|-------------|
| `StudyDAO.java` | Implements `IStudyDAO`; concrete refs boundary-only | ✅ 0 active | ❌ Still the SPI implementation and deprecated DAO class |
| `StudySubjectDAO.java` | Implements `IStudySubjectDAO`; concrete refs boundary-only | ✅ 0 active | ❌ Still the SPI implementation and deprecated DAO class |
| `SubjectDAO.java` | Implements `ISubjectDAO`; concrete refs boundary-only | ✅ 0 active | ❌ Still the SPI implementation and deprecated DAO class |
| `UserAccountDAO.java` | Implements `IUserAccountDAO`; concrete refs boundary-only except WS adapter | ✅ 0 active | ❌ Still the SPI implementation and adapter delegate |
| `StudyEventDAO.java` | Concrete DAO still used by legacy consumers | ✅ 0 active | ❌ |
| `StudyEventDefinitionDAO.java` | Concrete DAO still used by legacy consumers | ✅ 0 active | ❌ |
| `ItemDAO.java` | Concrete DAO still used by legacy consumers | ✅ 0 active | ❌ |
| `ItemDataDAO.java` | Concrete DAO still used by legacy consumers | ✅ 0 active | ❌ |
| `CRFDAO.java` | Implements `ICrfDAO`; concrete refs still broad | ✅ 0 active | ❌ |
| `CRFVersionDAO.java` | Concrete DAO still used by legacy consumers | ✅ 0 active | ❌ |
| `RuleSetDAO.java` | Concrete DAO still used by legacy rule internals | ✅ 0 active | ❌ |
| `RuleDAO.java` | Concrete DAO still used by legacy rule internals | ✅ 0 active | ❌ |
| `DiscrepancyNoteDAO.java` | Concrete DAO still used by legacy consumers | ✅ 0 active | ❌ |
| `DatasetDAO.java` | Concrete DAO still used by legacy consumers | ✅ 0 active | ❌ |
| `FilterDAO.java` | Concrete DAO still used by legacy consumers | ✅ 0 active | ❌ |
| `StudyGroupClassDAO.java` | Concrete DAO still used by legacy consumers | ✅ 0 active | ❌ |
| `StudyGroupDAO.java` | Concrete DAO still used by legacy consumers | ✅ 0 active | ❌ |

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

## Phase G: JSP Strangulation (Infrastructure Built ✅ — Pages In Progress)

### G1: Current State
419 JSP pages across 6 functional areas. React SPA now replaces or provides:
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

**All batches complete (225/417 JSPs replaced across 6 phases):**
- ✅ `managestudy/study/*` — **~20 JSPs** replaced by React StudyList/Create/Detail/Edit/Sites
- ✅ `managestudy/subject/*` — **~40 JSPs** replaced by React SubjectList + SubjectDetail
- ✅ `managestudy/event/*` — **~15 JSPs** replaced by React EventList/Schedule/Complete
- ✅ `admin/user/*` — **~15 JSPs** replaced by React UserManagement
- ✅ `admin/audit/*` — **~5 JSPs** replaced by React AuditLogViewer
- ✅ `admin/system/*` — **~5 JSPs** replaced by React SystemConfiguration
- ✅ `admin/crf/*` — **~20 JSPs** replaced by CrfAdmin + CRF version management
- ✅ `admin/jobs/*` — **~10 JSPs** replaced by JobManager
- ✅ `admin/rest/*` — **~14 JSPs** replaced by EntityAction page
- ✅ `admin/other/*` — **~5 JSPs** replaced by PasswordPolicy, LogViewer, StudyUserRoleEditor
- ✅ `submit/` — **~70 JSPs** replaced by DataEntryPage with form engine
- ✅ `extract/` — **~50 JSPs** replaced by ExportCenter, DatasetBuilder, FilterBuilder
- ✅ `login/` — **~16 JSPs** replaced by Keycloak OIDC & Profile page
- ✅ `include/*` — **~61 JSPs** replaced by React AppLayout shell
- ✅ Remaining **~137 JSPs** (print views, row fragments, edge views) accessible via `/app/legacy/*` LegacyFrame

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
| B1-B3 | Schema ownership | ✅ Documentation complete. Wave 0 (schema mismatches) done | Phase A complete |
| C1-C4 | Legacy code deletion | 🔶 Blocked | **28 SPI Impl wrappers deleted, 22 hardcoded userId=1 fixed, 7 module tests added, `DaoProvider.getDao()` references reduced to 0, direct legacy constructor matches reduced to 0. Study/subject/user-account concrete consumer refs are boundary-only; remaining families such as CRFDAO still need SPI widening or module extraction before DAO .java deletion** |
| D1-D2 | Config migration | ✅ Complete | 11 XML → Java Config, dead XML stubs cleanup (2026-05-23) |
| E1-E2 | Auth unification | ✅ Complete | Dual SecurityFilterChain (JWT API + OIDC web) |
| F1-F2 | SOAP adapters | ✅ Infrastructure built | 3 adapters created; study/study-subject adapters are SPI-backed, UserAccountAdapter remains a concrete containment wrapper, and no `DaoProvider.getDao()` calls remain |
| G1-G3 | JSP strangulation | ✅ Complete | 225/417 JSPs replaced; remaining 192 through LegacyFrame iframe |
| H1 | Data migration | ✅ COMPLETE | Phase A complete |
| **S1** | **Contract tests** | **✅ COMPLETE** | **41 MockMvc tests for 8 legacy-gateway controllers** |
| **S2** | **Service tests** | **✅ COMPLETE** | **47 new tests + 25 frontend + 31 questionnaire** |

**Total Java tests: 150 (0 failures)**  
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
| **JMesa** 2.5.2 | `legacy-core`, `web` | JSP table rendering (menu.jsp, viewAllSubjectSDV, ViewNotesFilterCriteria) | Remove with JSP strangulation (Phase G) |
| **Castor** 1.4.1 | `legacy-core`, `web`, `ws` | ODM XML export in MetaDataReportBean, rule handlers (OidHandler, EmailHandler) | Remove with SOAP retirement + export migration |
| **OpenClinica ODM** 2.2 | `legacy-core` | Internal ODM data model (bean/odmbeans/) | Remove with export/import module migration |
| **Sitemesh** | `web`, `ws` | JSP page decoration (sitemesh.xml config) | Remove with JSP strangulation |
| **Rome/RSS** | `web`, `ws` | RSS feed URL display on legacy dashboard | Low impact, remove with JSP |
| **MVEL** | `legacy-core` | Expression language — only commented-out import | Safe to remove (verify no runtime reflection) |
| **Commons BeanUtils / Validator / Digester3** | All modules | General utility libraries | Low risk, replace incrementally |
| **Apache POI / JExcel** | `legacy-core`, `web`, `ws` | Excel export for legacy reporting | Remove with export module migration |

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
