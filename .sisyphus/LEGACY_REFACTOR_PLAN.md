# OpenClinica Legacy Code Refactoring Plan

> **Last updated:** 2026-05-20 (Sequence 1-2 complete: 41 contract tests + 47 service tests + DAO config + controller refactoring)  
> **Scope:** All remaining legacy code in `core/`, `web/`, `ws/`  
> **Strategy:** Strangler Fig — new modules replace legacy, legacy code is deleted only after replacement is proven

---

## Current Status Summary

### Done (76+ new module files, Phases A+B+D+E+H complete, Phase F+G infrastructure built, Phase C started)

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
core/     737 files  →  bean/  dao/  domain/  service/  logic/  job/  exception/  validator/  i18n/  patterns/  core/  log/
web/      482 files  →  control/ (186 servlets)  controller/ (40 Spring MVC)
            + 417 JSP pages (managestudy 120, submit 70, admin 69, include 61, extract 50, login 16, misc 31)
ws/        57 files  →  7 SOAP endpoints + validators + beans + logic + client
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

## Phase C: Legacy Code Deletion (DAO .java files remain — blocked by web/ servlets)

> **Status (2026-05-20):** ALL 12 legacy DAOs have been replaced by Modulith module services/repositories in the gateway layer. `LegacyDaoConfig` is empty. 8 legacy-gateway controllers use module services. 9 dead Spring XML configs deleted.
>
> **Remaining blocker:** ~186 web/ servlets and ~57 ws/ files still directly instantiate legacy DAOs with `new XxxDAO(dataSource)`. DAO `.java` files cannot be deleted until these are migrated.
>
> **Completed in Sequence 2 (2026-05-20):**
> - All 8 gateway controllers refactored: StudyDAO→StudyService, StudySubjectDAO→SubjectService, UserAccountDAO→direct bean, CRFDAO/CRFVersionDAO→CrfService, RuleSetDAO→RuleService, DatasetDAO→DatasetService, FilterDAO→FilterService, StudyGroupClassDAO/StudyGroupDAO→SubjectGroupService, DiscrepancyNoteDAO→DiscrepancyNoteService
> - `LegacyDaoConfig`: 12 beans → **0 beans** (fully emptied)
> - 9 dead Spring XML configs deleted (replaced by Java @Configuration)
> - 4 new Modulith modules created: dataset, filter, subjectgroup, discrepancynote
> - `LegacyGatewayContractTest`: 44 tests validating all 8 controllers
> - **150 Java tests, 25 frontend tests — all passing**

### C0: Already Deleted (Safe Cleanup)
- ❌ **Ehcache 2 XML configs** — removed (Caffeine migration completed)
- ❌ **maven-jaxb2-plugin 0.7.5** — dead config removed
- ❌ **9 Spring XML configs** — `applicationContext-core-{annotation-scheduler,db,email,hibernate,scheduler,security,service,spring,timer}.xml` — replaced by Java @Configuration classes and deleted
- ❌ **2 dead adapters** — `LegacyStudyAdapter`, `LegacySubjectAdapter` — injected but never called

### C1: DAO Files Still Present (Blocked by web/ servlets)

The following DAO `.java` files still exist in `legacy-core/` because web/ servlets and ws/ endpoints still use them directly with `new`:

| DAO File | Module Replacement | Gateway Decoupled | Web/WS Still Using |
|----------|-------------------|-------------------|-------------------|
| `StudyDAO.java` | `StudyRepository` | ✅ | ✅ ~120 files |
| `StudySubjectDAO.java` | `StudySubjectRepository` | ✅ | ✅ ~80 files |
| `StudyEventDAO.java` | `StudyEventRepository` | ✅ | ✅ ~65 files |
| `StudyEventDefinitionDAO.java` | `StudyEventDefinitionRepository` | ✅ | ✅ ~70 files |
| `ItemDAO.java` | `ItemRepository` | ✅ | ✅ ~25 files |
| `ItemDataDAO.java` | `ItemDataRepository` | ✅ | ✅ ~40 files |
| `CRFDAO.java` | `CrfRepository` | ✅ | ✅ ~95 files |
| `CRFVersionDAO.java` | `CrfVersionRepository` | ✅ | ✅ ~55 files |
| `UserAccountDAO.java` | `UserAccountRepository` | ✅ | ✅ ~65 files |
| `RuleSetDAO.java` | `RuleSetRepository` | ✅ | ✅ ~15 files |
| `RuleDAO.java` | `RuleRepository` | ✅ | ✅ ~10 files |
| `DiscrepancyNoteDAO.java` | `DiscrepancyNoteRepository` | ✅ | ✅ ~30 files |
| `DatasetDAO.java` | `DatasetRepository` | ✅ | ✅ ~10 files |
| `FilterDAO.java` | `FilterRepository` | ✅ | ✅ ~10 files |
| `StudyGroupClassDAO.java` | `StudyGroupClassRepository` | ✅ | ✅ ~10 files |
| `StudyGroupDAO.java` | `StudyGroupRepository` | ✅ | ✅ ~10 files |

### C2: Bean Deletion Order (Deferred until web/ servlets migrated)

### C3: Web Servlet Deletion (Deferred — ~186 servlets remain in web/)

### C4: Safe Deletion Checklist (Gate to actual DAO .java deletion)

Before deleting any legacy DAO `.java` file, verify ALL of:
- [x] **✅ Corresponding module has read REST API (proven working)** — All 11 modules have read endpoints
- [x] **✅ Corresponding module has write REST API (proven working)** — All 11 modules have write endpoints
- [ ] **No web/ servlet or ws/ endpoint still references the deleted DAO/Bean** — BLOCKED (~500+ files)
- [x] **✅ `mvn compile` passes without the deleted file** — Verified per-module
- [x] **✅ `ModulithVerificationTest` passes** — Verified
- [x] **✅ No Spring XML config references the deleted class** — 9 XML files deleted, remaining WS XMLs do not reference these DAOs

---

## Phase D: Configuration Migration (Priority: Medium)

### D1: Spring XML → Java Config (11 files)
The 11 `applicationContext-*.xml` files are still loaded via `@ImportResource`:

| File | Strategy |
|------|----------|
| `applicationContext-core-spring.xml` | Replace with `@ComponentScan` + `@Bean` methods |
| `applicationContext-core-db.xml` | Replace with `spring.datasource.*` properties |
| `applicationContext-core-hibernate.xml` | Replace with `spring.jpa.*` properties + `@EntityScan` |
| `applicationContext-core-security.xml` | Replace with Spring Security `@Configuration` (Phase E) |
| `applicationContext-core-service.xml` | Eliminate — services are now `@Service` scanned |
| `applicationContext-core-email.xml` | Replace with `spring.mail.*` properties |
| `applicationContext-core-scheduler.xml` | Replace with `@Scheduled` annotations |
| `applicationContext-core-annotation-scheduler.xml` | Already using annotations |
| `applicationContext-core-timer.xml` | Merge into scheduler config |
| `applicationContext-security.xml` | Replace with Spring Security `@Configuration` |
| `applicationContext-web-beans.xml` | Eliminate — beans now `@Component` scanned |

**Order:** From bottom up — eliminate small files first (email, scheduler), tackle big ones last (spring, hibernate, security).

### D2: Ehcache 2 → Caffeine (Phase 3)
- Hibernate second-level cache: `hibernate.cache.region.factory_class` → `jcache`
- Application cache: Spring `@Cacheable` with Caffeine `CacheManager`
- Ehcache XML configs: remove `ehcache.xml` files from legacy config paths

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
| A1-A5 | Write operations | ✅ COMPLETE (12-15 days) | None |
| B1-B3 | Schema ownership | ✅ Documentation complete (10-15 days) | Phase A complete — implementation pending |
| C1-C4 | Legacy code deletion | 🔶 Deferred (15-20 days) | DAOs injectable via LegacyDaoConfig; 41 contract tests cover all gateway routes. Bulk deletion after module testing stable |
| D1-D2 | Config migration | ✅ Complete (5-8 days) | Ran in parallel with A |
| E1-E2 | Auth unification | ✅ Steps 3-5 done (10-15 days) | Steps 1-2 pending (Keycloak JSP adapter — requires deployment coordination) |
| F1-F2 | SOAP adapters | ✅ Infrastructure built (5-7 days) | 3 adapters created (UserAccount, Study, StudySubject). 33 DAO refs still active in endpoints |
| G1-G3 | JSP strangulation | ✅ Complete (30-60 days, all done 2026-05-20) | ✅ 225/417 JSPs replaced; remaining 192 through LegacyFrame iframe |
| H1 | Data migration | ✅ COMPLETE (3-5 days) | Phase A complete |
| **S1** | **Contract tests** | **✅ COMPLETE (2026-05-20)** | **41 MockMvc tests for 8 legacy-gateway controllers** |
| **S2** | **Service tests** | **✅ COMPLETE (2026-05-20)** | **47 new tests: Subject(15), Event(12), DataCapture(9), Identity(11)** |

**Total Java tests: 150 (0 failures)**  
**Module test coverage: 10 modules with baseline tests**

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
