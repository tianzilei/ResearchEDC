# Phase 0 Full Product Audit Report

**Created:** 2026-06-27
**Plan:** `docs/edc-convergence/phase-0-full-product-audit-plan.md`
**Status:** Complete
**Release posture:** no RC tag, no publish action

## Executive Summary

Phase 0 is complete enough to activate Phase 1. The verification baseline is
healthy, but the product is not yet an operator-usable EDC baseline end to end.

The first Phase 1 slice should focus on core workflow continuity:

1. restore study event definition creation from the SPA
2. repair subject detail -> event CRF navigation
3. make event scheduling assign safe default statuses and ordinals
4. align CRF list/preview routing with CRF version identifiers
5. fix deploy/runtime drift in the bare-host script and documentation

`randomization` and `questionnaire-service` were reviewed only at boundary level,
as requested by the Phase 0 plan.

## Verification Results

| Area | Command | Result | Classification | Notes |
|---|---|---:|---|---|
| Backend compile | `mvn clean compile -DskipTests` | PASS | product | Build succeeded on Java 21. Maven still reports git safe-directory warnings and compiler warnings. |
| Backend targeted tests | `mvn test -pl app -am -Dtest=ModulithVerificationTest,OdmExportGeneratorTest,ExportArtifactWriterTest -Dsurefire.failIfNoSpecifiedTests=false` | PASS | product | 27 tests passed: Modulith 1, ODM export 21, artifact writer 5. |
| Frontend typecheck | `pnpm -C frontend typecheck` | PASS | product | `pnpm` hydrated `frontend/node_modules` from the lockfile. |
| Frontend lint | `pnpm -C frontend lint` | PASS | product | 0 lint errors. |
| Frontend tests | `pnpm -C frontend test --run` | PASS | product | 25 Vitest tests passed. |
| Questionnaire tests | `uv run --group dev pytest app/tests/ -v` | PASS | product | 40 pytest tests passed. Direct `python3 -m pytest` failed because ambient Python has no `pytest`; use `uv`. |
| Sandbox behavior | several simple read/test commands | WARN | environment | The execution sandbox intermittently failed before command startup with `bwrap: loopback: Failed RTM_NEWADDR: Operation not permitted`; reruns outside sandbox worked. |

## Prioritized Findings

### Blockers

| ID | Finding | Evidence | Scope Label | Type Label | Owner | Phase 1 Action |
|---|---|---|---|---|---|---|
| P0-B1 | Study event definition creation is exposed in the SPA but has no backend route. | `frontend/src/api/events.ts` posts to `/api/v1/events/definitions`; `EventController` exposes GET, GET by id, DELETE, and PATCH for definitions, but no POST. `EventDefinitionsPage` presents a create modal that calls this missing API. | core-logic | business-logic | event module | Fixed in Phase 1 slice 1: create endpoint/service support added with audit record. |
| P0-B2 | Subject detail CRF action links to an unregistered route, blocking the visible path from subject detail to data entry. | `SubjectDetail` navigates to `/app/subjects/:id/events/:eventId/crfs`; router only registers `/app/subjects/:subjectId/events` and `/app/subjects/:subjectId/events/:eventId/crfs/:eventCrfId/entry`. `EventList` already contains the working expandable CRF list. | core-logic | business-logic | frontend/event workflow | Fixed in Phase 1 slice 1: subject detail now routes to the registered event list with expandable CRFs. |
| P0-B3 | Event scheduling can persist null status fields and ignores frontend ordinal. | `ScheduleEventRequest` accepts status fields but frontend does not send them; `EventService.scheduleEvent` copies them directly and never sets `sampleOrdinal` or `sedOrdinal` from the request. | core-logic | business-logic | event module | Fixed in Phase 1 slice 1: scheduled events now default status and calculate/store ordinal. |
| P0-B4 | Bare-host deploy path has drift from the current tree. | Docs/index mention root `deploy.sh`, but checkout has `deploy-bare.sh` and `deploy-docker.sh`. `deploy-bare.sh build/clean` still referenced removed `web/` and `ws/` paths and tried to copy `web/src/main/resources/extract.properties`. | core-logic | code-quality | deploy/runtime | Fixed in Phase 1 slice 1: dead `web/`/`ws/` assumptions removed from `deploy-bare.sh`, README points to real deploy entry points, and `bash -n deploy-bare.sh` passes. Remaining: dry-run build/start/status path on a clean host or disposable runtime. |

### High Friction

| ID | Finding | Evidence | Scope Label | Type Label | Owner | Phase 1 Action |
|---|---|---|---|---|---|---|
| P0-H1 | CRF list links CRF ids into a route that fetches CRF version ids. | `CrfList` navigates to `/app/crfs/${r.crfId}`; `CrfPreview` reads `versionId` and fetches `/api/v1/crfs/versions/${vId}`. | core-logic | business-logic | frontend/CRF | Fixed in Phase 1 slice 1: CRF list now opens the CRF version manager. |
| P0-H2 | API security disables CSRF while frontend still injects XSRF headers. | `SecurityConfig` intentionally keeps `.csrf(csrf -> csrf.disable())`; frontend previously read `XSRF-TOKEN` and sent `X-XSRF-TOKEN` anyway. | core-logic | business-logic | security/frontend | Fixed in Phase 1 slice 2 for the current contract: removed frontend XSRF token injection so SPA behavior matches the backend session API. Future CSRF enablement should be a separate token-bootstrap design. |
| P0-H3 | Method-level authorization is sparse across module controllers. | Security requires authentication for `/api/**`, but only selected audit endpoints show `@PreAuthorize`; many mutating core endpoints rely on session presence and service-level checks are uneven. | core-logic | business-logic | security/modules | Partially fixed in Phase 1 slice 2: shared role expressions and `@PreAuthorize` gates now cover study administration, CRF management, subject mutations, event mutations, data capture read/write, discrepancy notes, import jobs, and export jobs/downloads. Export download now also checks same-study export permission before returning artifacts. Remaining: extend per-study resource scoping beyond export downloads. |
| P0-H4 | Frontend has no global 401/403 handling for API client calls. | `ApiClient` threw raw `ApiError`; router has `/app/403`, but request failures were handled ad hoc by pages. `ProtectedRoute` returns `null` while auth initializes. | core-logic | business-logic | frontend/security | Partially fixed in Phase 1 slice 2/4: `ApiClient` emits auth-failure events, `AuthProvider` clears local session and redirects on 401, and redirects to `/app/403` on 403. Data-capture attachment list/upload, import upload, and export download now use `ApiClient`; LogViewer actuator fetch now emits the same auth-failure event on 401/403. `ApiClient` now also parses backend JSON error envelopes and exposes `requestId`/`path` while preserving text download errors. Remaining direct `fetch` usage is limited to auth bootstrap/login/logout and low-level client internals; page-level rendering of request ids remains a UX pass. |
| P0-H5 | Export download does not verify artifact existence before returning metadata. | `ExportService.getDownload` wrapped `job.getFilePath()` in `FileSystemResource` without checking artifact readability. | core-logic | business-logic | export module | Fixed in Phase 1 slice 2: download now verifies artifact existence/readability and maps missing/unreadable artifacts to a 404 text response consumed by the existing frontend download error path. |

### Medium Friction

| ID | Finding | Evidence | Scope Label | Type Label | Owner | Phase 1 Action |
|---|---|---|---|---|---|---|
| P0-M1 | Dashboard/system health is app-internal, while deploy proxy exposes `/actuator/*` assumptions. | `DashboardController` has `/api/v1/dashboard/health`; Caddy config has `/actuator/*`, but actuator endpoint availability was not verified in code. | core-logic | code-quality | runtime/observability | Partially fixed in Phase 1 slice 3/4: runtime health surfaces are documented in `docs/HOST_DEPLOYMENT.md`, `deploy-bare.sh health` checks the app/questionnaire/proxy routes, request ids are included in logs and responses, and common API errors now return a JSON envelope with the same request id. Remaining: run the bare-host health path on Linux. |
| P0-M2 | Audit coverage exists for core mutations but lacks study context on some records. | Event and data capture services call `AuditService.recordAudit`, often with `studyId` set to `null`. | core-logic | business-logic | audit/modules | Partially fixed in Phase 1 slice 4: event and item-data audit records now populate study id where derivable from event definitions, event CRFs, and study subjects. Remaining: review other modules for safely derivable context without inventing ambiguous study links. |
| P0-M3 | Operator text is mixed English/Chinese in core workflow pages. | Event list, CRF/data entry, and admin pages mix English labels with Chinese UI. | core-logic | code-quality | frontend/UX | Normalize core workflow copy through i18n keys after workflow blockers are fixed. |
| P0-M4 | Questionnaire test command in historical docs can fail on a clean host. | Direct `python -m pytest` failed because `python` is absent; direct `python3 -m pytest` failed because ambient Python has no `pytest`; project-local `uv run --group dev pytest` passed. | boundary-integration | code-quality | questionnaire-service | Update convergence docs/CI notes to use `uv` for the local gate. |
| P0-M5 | `pnpm` warns that the package-level `pnpm.onlyBuiltDependencies` field is ignored by pnpm 11. | Typecheck/lint/test printed the warning while `frontend/pnpm-workspace.yaml` already had the supported `allowBuilds` setting. | core-logic | code-quality | frontend/tooling | Fixed in Phase 1 slice 2: removed obsolete `package.json` pnpm block; `pnpm -C frontend typecheck` and `pnpm -C frontend lint` run without the warning. |

## Workflow Audit

| Workflow | Status | Notes |
|---|---|---|
| Login/session bootstrap | Partially working | Auth provider checks `/api/v1/auth/me`, login posts JSON to `/api/v1/auth/login`, and protected routes gate `/app/*`. Current contract is session-backed with backend CSRF disabled and no frontend XSRF header injection. |
| Study management | Partially working | Study list/detail/create surfaces exist. Event definition creation from study detail is blocked by the missing POST endpoint. |
| Subject enrollment and lookup | Partially working | Subject list/detail APIs exist and detail page loads enrollment, subject, and events. The visible CRF action on detail page goes to a dead route. |
| Visit/event workflow | Blocked for setup | Event list and scheduling surfaces exist, but definition creation is blocked and scheduled event defaults/ordinals are unsafe. |
| CRF and data capture | Partially working | Event list expandable CRF entry route exists; data capture service persists item data and attachments. CRF library preview routing is likely wrong because CRF id is used as version id. |
| Discrepancy notes | Available but not deeply exercised | REST controller and service exist; no critical static mismatch found in this pass. Needs endpoint and UI smoke coverage in Phase 1. |
| Export workflow | Mostly available | Export job create/list/cancel/retry/download surfaces exist, ODM execution is covered by tests. Missing artifact behavior needs hardening. |
| Questionnaire boundary | Healthy boundary | `uv` pytest gate passed 40 tests. Public fill route exists at `/q/fill/:token`; service is reverse-proxied under `/q/*` by bare deploy config. |
| Randomization boundary | Available boundary | Routes and endpoints are present. Logic-depth review deferred by plan. |

## Authorization And Security Audit

- API session gating is present for `/api/**`; `/api/v1/auth/login`,
  `/api/v1/auth/me`, and `/api/v1/openrosa/**` are public.
- CSRF is disabled in backend security, and frontend XSRF header injection was removed in Phase 1 slice 2 to match that current contract.
- Method-level authorization is present on selected audit endpoints, but not
  consistently on core mutating study/subject/event/export endpoints.
- Attachment data access has explicit `canViewEventCrfData` checks and path
  traversal protections.
- OpenRosa is public at the API boundary; that should remain a boundary-level
  Phase 1 decision rather than a hidden default.

## Frontend UX Audit

- Core routes are present under `/app/*`.
- Loading and empty states exist on many pages, but error handling is uneven. Backend common authorization, not-found, and bad-request failures now return a JSON error envelope with the request correlation id; `ApiClient` preserves the envelope and request id for callers, but frontend rendering of that context remains uneven.
- `ProtectedRoute` returns a blank screen while auth initializes; acceptable for
  a short initial state but should become a visible loading state if auth checks
  are slow.
- Global API error handling is missing for 401/403/session expiry.
- Several core pages mix English and Chinese strings.
- The main workflow contains dead or misleading links as listed in blockers and
  high-friction findings.

## Deploy And Runtime Audit

- The actual root deployment entry points are `deploy-bare.sh` and
  `deploy-docker.sh`; the historical `deploy.sh` named in project notes is not
  present.
- `deploy-bare.sh` generates Caddy routes for `/api/*`, `/app/*`, `/q/*`, and
  `/actuator/*`.
- Phase 1 slice 1 removed the dead `web/` and `ws/` assumptions from
  `deploy-bare.sh`; the remaining runtime readiness gap is a clean-host or
  disposable build/start/status smoke run.
- Required tools include Java 21, Maven, pnpm, uv, Docker/PostgreSQL, Redis,
  Caddy, and optionally MinIO.
- Rollback/recovery is mostly process-based (`stop`, `restart`, logs, pid
  files); no versioned release rollback path was audited because Phase 0 forbids
  RC tagging/publishing.

## Observability Audit

- Export service logs job create, completion, failure, cancellation, and retry.
- Questionnaire worker code records job status transitions; boundary log depth
  was not audited beyond tests and deploy wiring.
- Audit service persists module audit records and publishes an event, but
  `onAuditRecorded` is currently empty.
- Request correlation/MDC was not found in the reviewed surfaces.
- Operator-visible job state exists for export/import-style workflows, but core
  workflow failures still depend heavily on page-level messages or raw API
  errors.

## Phase 1 Entry Backlog

### Slice 1: Core Workflow Continuity

1. Add event definition creation API/service support and tests.
2. Fix subject detail CRF navigation to the existing event CRF list/entry path.
3. Default scheduled event status and ordinal behavior in backend service.
4. Fix CRF list preview/version routing.
5. Add targeted regression coverage around the study -> event definition ->
   subject event -> event CRF entry path.

### Slice 2: Auth And Error Predictability

1. Current CSRF contract aligned in Phase 1 slice 2: backend remains disabled and frontend no longer sends XSRF headers. Future CSRF restoration requires a token-bootstrap design.
2. ApiClient 401/403 handling added in Phase 1 slice 2; data-capture attachment list/upload, import upload, export download, and LogViewer actuator auth failures are covered. Remaining direct fetch is auth bootstrap/login/logout and client internals.
3. Minimum role authorization rules added in Phase 1 slice 2 for core mutating endpoints plus data/artifact read surfaces. Remaining: per-study resource scoping.
4. Missing artifact behavior fixed in Phase 1 slice 2; export download now has a role gate and same-study export permission check.

### Slice 3: Runtime Readiness

1. Removed in Phase 1 slice 1: dead `web/`/`ws/` references from `deploy-bare.sh`.
2. Updated in Phase 1 slice 1: README now points to `deploy-bare.sh` and
   `deploy-docker.sh` as the real deploy entry points.
3. Started in Phase 1 slice 3: deploy readiness now uses local `/actuator/health`, questionnaire readiness uses `/health`, Caddy route checks use `/app/`, `/api/v1/auth/me`, and `/q/health`, and operator status remains `/api/v1/dashboard/status` plus `/api/v1/dashboard/health`.
4. Remaining: run `bash -n deploy-bare.sh`, `bash deploy-bare.sh help`, and a dry build/start/status/health path on a clean Linux host or disposable runtime.

### Slice 4: Observability And UX Polish

1. Started in Phase 1 slice 4: populated study id for event and item-data audit records where derivable.
2. Added in Phase 1 slice 4: `X-Request-ID` request correlation is propagated to responses and log MDC.
3. Added in Phase 1 slice 4: common REST authorization, not-found, and bad-request failures return a JSON envelope containing the request id.
4. Normalize mixed English/Chinese strings on the core EDC path.
5. Obsolete package-level pnpm config removed in Phase 1 slice 2.

## Exit Gate Assessment

Phase 0 exit criteria are met:

1. verification, workflow, authorization/security, UX, deploy/runtime, and
   observability were reviewed
2. blockers are grouped by bucket and severity
3. core EDC findings are separated from boundary integration findings
4. Phase 1 has an ordered, evidence-based first slice
5. "usable EDC baseline" now means the documented study -> subject -> event ->
   CRF/data capture -> discrepancy/export operator path works without hidden
   dead ends
