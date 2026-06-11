# Phase 1 — Spring MVC Route Classification

**Date:** 2026-06-11
**Status:** Classification complete — 2 deletable artifacts now DELETED
**Session:** auto-next run (classification) + run-78/run-79 deletions

## Summary

| Classification | Count | Action |
|----------------|-------|--------|
| **KEEP COMPATIBILITY** | 8 | External API contract (Participant Portal) |
| **REPLACE (blocked)** | 1 | JSP workflow — blocked by DataEntry page |
| **DELETED** | 6 | StudyController (run-78) + UserAccountController (run-79) |
| **TOTAL REMAINING** | 9 | 8 AccountController + 1 InitialDataEntryServlet |

## OpenRosa Note

OpenRosa routes are **NOT** among the 15 Spring MVC artifacts. The Modulith `OpenRosaController` (`/api/v1/openrosa/*`) owns all 5 OpenRosa endpoints. Legacy bridges exist in `WebMvcConfig.java` (`/openrosa` → `/api/v1/openrosa`, `/rest2/openrosa` → `/api/v1/openrosa`). No legacy web/ controllers handle OpenRosa paths.

---

## Detail: 15 Spring MVC Route Artifacts

### AccountController — KEEP COMPATIBILITY (8 routes)

**File:** `web/src/main/java/org/researchedc/controller/AccountController.java`
**Base path:** `/accounts`
**Purpose:** Participant Portal external API — mobile app / patient portal integration

| # | Method | Path | Purpose | Classification |
|---|--------|------|---------|----------------|
| 1 | GET | `/accounts/study/{studyOid}/crc/{crcUserName}` | Lookup CRC user's study access | KEEP — participate login |
| 2 | GET | `/accounts/study/{studyOid}/accesscode/{accessCode}` | Participant login by access code → exchange for API key | KEEP — mobile auth |
| 3 | GET | `/accounts/study/{studyOid}/studysubject/{studySubjectId}` | Participant lookup by study subject ID | KEEP — participant lookup |
| 4 | POST | `/accounts/` | Create or update participant account | KEEP — account CRUD |
| 5 | POST | `/accounts/timezone` | Update subject timezone | KEEP — timezone sync |
| 6 | GET | `/accounts/study/{studyOid}` | List all participants per study | KEEP — participant listing |
| 7 | POST | `/accounts/update` | Update participant account (alias of #4) | KEEP — alias |
| 8 | — | `/accounts` (class-level) | Base mapping counted by inventory | KEEP |

**Rationale:** This is an **external API contract**. Mobile apps (OpenClinica Participate, ODK-based collectors) and external patient portals depend on these exact `/accounts/*` paths, request formats, and response schemas. Deleting or renaming these routes would break external clients.

**Migration path:** Move to a dedicated `app/module/participate/` module with backward-compatible route mappings. Keep the existing paths as URL bridges.

**Blocking:** None — these routes are self-contained and don't block JSP/servlet deletion.

---

### StudyController — DELETED (4 routes, run-78)

**File:** `web/src/main/java/org/researchedc/controller/StudyController.java` — **DELETED** (commit `8e150a8dc`, -1177 lines)
**Base path:** `/auth/api/v1/studies`
**Reason:** Zero callers confirmed — no Java imports, no XML config, no frontend references. All 3 POST endpoints have Modulith equivalents at `/api/v1/studies`.

---

### UserAccountController — DELETED (2 routes, run-79)

**File:** `web/src/main/java/org/researchedc/controller/UserAccountController.java` — **DELETED** (-465 lines)
**Base path:** `/auth/api/v1`
**Reason:** Zero callers confirmed — no Java imports, no XML references, no frontend callers. SPA `UserManagement` page uses Modulith Identity module endpoints (`/api/v1/identity/users`, `/api/v1/identity/roles/assign`).

---

### InitialDataEntryServlet — REPLACE (1 route, blocked)

**File:** `web/src/main/java/org/researchedc/control/submit/InitialDataEntryServlet.java`
**Base path:** `/InitialDataEntry`
**Classification:** phase-1-data-entry-discrepancy (not study-subject-event)
**Purpose:** JSP-rendered initial data entry form for CRFs

| # | Method | Path | Purpose | Classification |
|---|--------|------|---------|----------------|
| 15 | ANY | `/InitialDataEntry` | Legacy servlet dispatch — JSP data entry workflow | REPLACE (BLOCKED) |

**Rationale:** Hybrid `@Controller` + `SecureController` servlet. Renders JSP data entry forms. The SPA `DataEntryPage` (`/app/subjects/:subjectId/events/:eventId/crfs/:eventCrfId/entry`) partially covers this but does not yet handle full CRF rendering (sections, items, repeating groups, file uploads).

**Blocking:** Full SPA data entry page with CRF section/item rendering, file upload, discrepancy notes, and double data entry mode. See `phase-1-data-entry-discrepancy`.

---

## Phase 1 Study/Subject/Event JSPs + Servlet (additional 8 artifacts)

These 8 artifacts complete the 22-artifact `phase-1-study-subject-event` slice (14 Spring MVC routes + 7 JSPs + 1 servlet = 22).

### JSP Include Files — REPLACE (blocked)

| # | File | Purpose | Deletable? |
|---|------|---------|------------|
| J1 | `managestudy-header.jsp` | Study management page header include | ❌ Blocked by index.jsp |
| J2 | `showTableWithTabForSubject.jsp` | Subject table with tabs include | ❌ Blocked by index.jsp |
| J3 | `sideIconsSubject.jsp` | Subject sidebar icons | ❌ Blocked by index.jsp |
| J4 | `studySideInfo.jsp` | Study sidebar info panel | ❌ Blocked by index.jsp |
| J5 | `workflow.jsp` | Breadcrumb workflow box (only `index.jsp` includes it) | ❌ Blocked by index.jsp |
| J6 | `index.jsp` (managestudy) | Main study management landing page | ❌ Blocked — ManageStudyServlet forwards here |
| J7 | `managestudy_body.jsp` | Study management body content | ❌ Blocked by index.jsp |

**Reference chain:**
- `ManageStudyServlet.processRequest()` → `forwardPage(Page.MANAGE_STUDY)` → `index.jsp`
- `index.jsp` includes: `managestudy-header.jsp`, `managestudy_body.jsp`, `sidebar include`
- `ResolveDiscrepancyServlet` → `getAdminServlet()` returns `Page.MANAGE_STUDY_SERVLET` (error redirect)

### ManageStudyServlet — REPLACE (blocked)

**File:** `web/src/main/java/org/researchedc/control/managestudy/ManageStudyServlet.java`
**Path:** `/ManageStudy`
**Purpose:** Study management entry point — resolves study/site context, checks permissions, forwards to index.jsp

**Blocking dependency:** `ResolveDiscrepancyServlet` (phase-1-data-entry-discrepancy) redirects here on error.

**Migration path:**
1. Add `WebMvcConfig` redirect: `/ManageStudy` → `/app/dashboard`
2. Update `ResolveDiscrepancyServlet` to redirect to SPA path or use a module-owned error page
3. Decommission ManageStudyServlet
4. Delete all 7 dependent JSPs
5. Unregister `/ManageStudy` servlet

**SPA coverage:** `/app/dashboard` provides current study/site context + module cards (replaces index.jsp). `/app/studies/:id` replaces study detail view.

---

## Next Executable Steps

1. ~~**Add SPA redirect for `/ManageStudy`**~~ → `/app/dashboard` in `WebMvcConfig.java` (done: run-75)
2. **Update `ResolveDiscrepancyServlet`** — change `Page.MANAGE_STUDY_SERVLET` to SPA redirect (requires data-entry-discrepancy slice)
3. ~~**Migrate StudyController POST endpoints**~~ — DELETED run-78 (0 callers)
4. ~~**Migrate UserAccountController**~~ — DELETED run-79 (0 callers, SPA already uses Identity module)
5. **AccountController** — keep as compatibility contract; no immediate action needed
6. **InitialDataEntryServlet** — blocked until SPA DataEntryPage reaches parity
7. **Investigate remaining controllers** — SidebarInit/SidebarEnumConstants(blocked by JSP pages), only AccountController remains active

## Verification Commands

```bash
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
```

**Updates:**
- Run-78 (2026-06-11): Deleted StudyController (-1177L, 0 callers)
- Run-79 (2026-06-11): Deleted UserAccountController (-465L, 0 callers, SPA already migrated to Identity module)
