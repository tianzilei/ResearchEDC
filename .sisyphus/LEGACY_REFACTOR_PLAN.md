# OpenClinica Legacy Code Refactoring Plan

> **Last updated:** 2026-05-18  
> **Scope:** All remaining legacy code in `core/`, `web/`, `ws/`  
> **Strategy:** Strangler Fig — new modules replace legacy, legacy code is deleted only after replacement is proven

---

## Current Status Summary

### Done (76 new module files)

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

## Phase A: Write Operations (Priority: Critical)

Currently ALL new modules are **read-only bridges**. They can query existing tables but cannot create/update/delete data. To truly replace legacy code, each module needs write capability.

### A1: Study Module Write (2-3 days)
1. Add `@Transactional` write methods to `StudyService`: `createStudy()`, `updateStudy()`, `updateStatus()`
2. Add validation (name uniqueness, required fields)
3. Add `POST /api/v1/studies`, `PUT /api/v1/studies/{id}`
4. Wire audit logging: call `AuditService.recordAudit()` on write operations

### A2: Subject Module Write (2-3 days)
1. Add `createSubject()`, `enrollSubject()` (create study_subject record)
2. Add `POST /api/v1/subjects`, `POST /api/v1/subjects/enroll`
3. Wire audit logging

### A3: Event Module Write (2-3 days)
1. Add `scheduleEvent()`, `updateEvent()`, `completeEvent()`
2. Add `POST/PUT /api/v1/events/{id}`
3. Wire audit logging

### A4: Data Capture Module Write (3-4 days)
1. Add `saveItemData()` (upsert values for an event CRF)
2. Add `POST /api/v1/data-capture/items` (batch save)
3. Handle status transitions (initial → draft → submitted → locked)
4. Wire audit logging

### A5: Identity Module Write (2 days)
1. Add `createUser()`, `assignRole()`, `updateUser()`
2. Add `POST/PUT /api/v1/identity/users`

---

## Phase B: Schema Ownership (Priority: High)

Currently modules bridge to the **same tables** that legacy code uses. Full strangulation requires module-owned tables.

### B1: Table Analysis
For each module, identify which tables it should eventually own:
- `study` → `study` table (Study module)
- `subject` + `study_subject` → Subject module
- `study_event` + `study_event_definition` + `event_crf` → Event module
- `item_data` + `response_set` + `item_group` → Data Capture module
- `user_account` + `study_user_role` → Identity module
- `crf` + `crf_version` + `item` + `section` + `item_form_metadata` → CRF module

### B2: Schema Migration Strategy
Two options:

**Option A: Add module-owned columns** (lower risk, incrementally adds to existing tables)
- New columns get `module_` prefix: `module_study_uuid`, etc.
- Write operations go to new columns; legacy code still sees old columns
- Data migration script copies legacy data to new columns

**Option B: New tables with sync** (cleaner, but more work)
- Create `module_study`, `module_study_subject`, etc.
- Dual-write during transition (legacy DAO + new repository)
- Cron job or trigger keeps tables in sync
- Cut over when confident

**Recommendation:** Option A for the first module, Option B for subsequent modules.

---

## Phase C: Legacy Code Deletion (Priority: Medium)

Only after a module has proven write capability and stable schema ownership can legacy code be deleted.

### C1: DAO Deletion Order
1. `StudyDAO.java` — replaced by `StudyRepository`
2. `StudySubjectDAO.java` — replaced by `StudySubjectRepository` + `SubjectRepository`
3. `StudyEventDAO.java` + `StudyEventDefinitionDAO.java` — replaced by `StudyEventRepository` + `StudyEventDefinitionRepository`
4. `ItemDAO.java` + `ItemDataDAO.java` — replaced by `ItemDataRepository`
5. `CRFDAO.java` + `CRFVersionDAO.java` — after CRF module gets write
6. `UserAccountDAO.java` (both JDBC + JPA) — after Identity module gets write

### C2: Bean Deletion Order
After each DAO deletion, delete the corresponding bean since no code references it:
1. `StudyBean.java` → after `StudyDAO` deletion + all `control/` servlets migrated
2. `StudySubjectBean.java` → after `StudySubjectDAO` deletion
3. `StudyEventBean.java` → after `StudyEventDAO` deletion
4. `ItemDataBean.java` / `ItemBean.java` → after `ItemDAO` deletion
5. Every `EntityDAO` subclass → after its corresponding service/controller migration

### C3: Web Servlet Deletion (186 servlets)
Servlets depend on legacy DAOs. Delete in this order:
1. `control/managestudy/*` (93 files) — after study/subject/event modules have write
2. `control/submit/*` (56 files) — after data-capture module has write
3. `control/admin/*` (68 files) — after identity module has write
4. `control/extract/*` (23 files) — after migration to new export module
5. `control/login/*` (11 files) — after OIDC migration

### C4: Safe Deletion Checklist
Before deleting any legacy file, verify:
- [ ] Corresponding module has read REST API (proven working)
- [ ] Corresponding module has write REST API (proven working)
- [ ] No JSP page or servlet still references the deleted DAO/Bean
- [ ] `mvn compile` passes without the deleted file
- [ ] `ModulithVerificationTest` passes
- [ ] No Spring XML config references the deleted class

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

## Phase F: SOAP Endpoint Adapter (Priority: Low)

### F1: Current State
7 SOAP endpoints in `ws/` are fully coupled to legacy core DAOs.

### F2: Migration
- Create `ws/internal/adapter/` following CRF module's adapter pattern
- Each endpoint gets an adapter wrapping the legacy DAO calls
- Adapters delegate to new module REST APIs internally
- No endpoint code changes — adapters replace direct DAO injection

---

## Phase G: JSP Strangulation (Priority: Medium)

### G1: Current State
419 JSP pages across 6 functional areas. React SPA already replaces:
- Dashboard
- CRF list/preview
- Data export
- Randomization
- Questionnaires

### G2: Remaining Batches
**Batch 1 (Highest value — Phase 2):**
- `submit/` (70 JSP) — Data entry (CRF submission, eSignature, double data entry)
- `managestudy/` (120 JSP) — Study/Site/Subject/Event CRUD

**Batch 2 (Phase 3):**
- `admin/` (69 JSP) — System config, user management, job management

**Batch 3 (Phase 4):**
- `extract/` (50 JSP) — Reporting, data export UI
- `login/` (16 JSP) — After OIDC migration

---

## Phase H: Data Migration (Priority: Low)

### H1: Audit Log Data
- The new `audit_log` table is empty
- Legacy `audit_log_event` table has years of historical data
- Write a one-time migration script to copy legacy audit to new `audit_log` table
- Run after audit module proves stable in production

### H2: Other Data
- No other data migration needed — new modules read existing tables
- Schema ownership migration (Phase B) will handle table migration

---

## Effort Estimate

| Phase | Description | Estimated Effort | Dependencies |
|-------|-------------|-----------------|--------------|
| A1-A5 | Write operations | 12-15 days | None |
| B1-B2 | Schema ownership | 10-15 days | Phase A complete |
| C1-C4 | Legacy code deletion | 15-20 days | Phases A+B complete |
| D1-D2 | Config migration | 5-8 days | Can run in parallel with A |
| E1-E2 | Auth unification | 10-15 days | Phase A complete |
| F1-F2 | SOAP adapters | 5-7 days | Phase A complete |
| G1-G2 | JSP strangulation | 30-60 days | Phases A+B complete |
| H1 | Data migration | 3-5 days | Phase A complete |

**Total estimated: 90-145 days (4-7 months)**

---

## Risk Register

| Risk | Impact | Likelihood | Mitigation |
|------|--------|------------|------------|
| Breaking JSP pages during write operation rollout | High | Medium | Add feature flags; deploy behind `/api/v2/*` routes |
| Data inconsistency during dual-write period | High | Medium | Add reconciliation check script |
| Rules engine (57 files) too complex to extract | High | High | Treat as separate project; keep in legacy until clear extraction path |
| Ehcache → Caffeine breaks Hibernate caching | Medium | Low | Test with full integration test suite before prod rollout |
| Quartz job dependencies on legacy DAOs | Medium | Medium | Audit all 9 jobs before deleting any DAO |

---

## Quick Wins (Can Be Done This Week)

1. **Commons Lang 2 → 3**: Global `org.apache.commons.lang` → `org.apache.commons.lang3` package rename
2. **Commons Collections 3 → 4**: Global `org.apache.commons.collections` → `org.apache.commons.collections4`
3. **Javassist 3.8 removal**: Remove unused dependency from `pom.xml`
4. **Maven plugin upgrades**: `maven-release-plugin`, `maven-assembly-plugin`, `liquibase-plugin`
5. **OWASP Dependency Check**: Add to CI pipeline
