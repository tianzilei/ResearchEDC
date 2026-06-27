# Phase 1 EDC Usability Convergence Plan

**Created:** 2026-06-26
**Status:** Active / in progress
**Predecessor:** `docs/edc-convergence/phase-0-full-product-audit-plan.md`
**Successor:** `docs/product/researchedc-final-open-source-modular-plan.md`
**Release posture:** no RC tag, no publish action, no new module family work until the
current EDC baseline is usable end-to-end

## Context

The refactor/removal program is complete and the remaining work in
`docs/refactor/` is a final verification closure. The product goal before
starting the next R&D phase is to converge the current EDC into a stable,
operator-usable baseline:

- login and session flow are predictable
- study, subject, event, CRF, and data-capture workflows are coherent
- discrepancy and export paths are usable
- frontend and backend error handling are consistent
- deploy and rollback expectations are documented and repeatable

This phase exists to separate product usability hardening from both:

- historical refactor cleanup
- future module expansion such as task engine, participant identity, eConsent,
  or eCOA/ePRO productization

This phase should consume a logic-first audit result:

- fix core EDC logic and workflow continuity first
- treat `randomization` and `questionnaire-service` as boundary integrations
  unless a later phase explicitly expands their logic scope
- solve business-logic defects before code-quality or refactor-oriented issues

## Goal

Make the existing ResearchEDC baseline usable, testable, and deployable enough
to serve as the foundation for the next product-development phase.

Target outcomes:

1. the current EDC workflow has one documented end-to-end operator path
2. baseline verification is repeatable across backend, frontend, and
   questionnaire-service
3. auth, permissions, and error states are predictable in the SPA and REST
   surfaces
4. deploy/runtime expectations are documented well enough for a clean bare-host
   run
5. follow-up work can move into new module R&D without mixing in baseline repair

## Execution Priority

The execution order for this phase is:

1. business logic correctness
2. workflow continuity and permission correctness
3. user-facing failures that block the main EDC path
4. operational and deployment gaps
5. code-quality, refactor, and non-blocking engineering cleanup

Code-related work should be pulled forward only when it is required to unblock a
business-logic fix or to keep a fix safe and testable.

## Non-Goals

- do not reopen broad legacy-removal or DAO-deletion work
- do not start `Notification And Task Engine`
- do not start `Participant Identity And Access`
- do not remove `randomization` or `questionnaire-service`
- do not create RC tags, release artifacts, or publish local commits

## Activation Gate

This plan becomes active only after:

1. `docs/refactor/phase-18-post-cleanup-verification-baseline-plan.md` has
   current verification results
2. `docs/edc-convergence/phase-0-full-product-audit-plan.md` has produced a
   prioritized finding set
3. no Phase 18 blocker requires returning to refactor cleanup
4. the next task is explicitly product convergence rather than release work

## Workstreams

### Phase 1A: Audit Findings Triage

Goal: convert audit output into the ordered convergence backlog.

Execution:

1. consolidate audit findings across workflow, auth, UX, deploy, and
   observability
2. group items by blocker, high-friction, medium-friction, and later polish
3. separate `core-logic` defects from `boundary-integration` defects
4. separate `business-logic` defects from `code-quality` defects
5. separate defects from documentation gaps and test gaps
6. define the first convergence slice from the ranked findings

Exit gate:

- the first convergence slice is evidence-based
- audit findings have explicit owners and categories
- core EDC logic repairs are ranked ahead of optional boundary-module polish
- business-logic repairs are ranked ahead of code cleanup

### Phase 1B: Verification Baseline Promotion

Goal: turn the Phase 18 verification matrix and audit findings into the working
quality contract for convergence.

Execution:

1. copy or reference the final Phase 18 verification results
2. classify failures as environment, product, or tooling issues
3. identify which checks become required local gates for convergence work
4. keep backend, frontend, and questionnaire-service independently runnable

Exit gate:

- the verification baseline is explicit
- every failing check has an owner and classification

### Phase 1C: Core Workflow Closure

Goal: make the current operator workflow usable from study setup to export.

Workflow scope:

1. study selection and context bootstrap
2. subject creation and lookup
3. event / visit creation and status viewing
4. CRF access and data capture
5. discrepancy note visibility
6. export initiation and artifact retrieval

Execution:

1. identify broken or missing links in the end-to-end flow
2. fix high-friction UX and contract gaps first
3. document the happy path and important operator caveats
4. add targeted regression coverage for repaired workflow edges

Exit gate:

- one operator can complete the core EDC path without guessing hidden steps
- known limitations are documented rather than implicit

### Phase 1D: Auth, Permissions, And Error Handling

Goal: make security and failure behavior predictable for daily use.

Execution:

1. verify form login, session expiry, and CSRF behavior across SPA and REST
2. review module endpoint authorization for study-scoped access
3. standardize 401, 403, 404, and 500 handling in the frontend
4. verify download and export permissions explicitly

Exit gate:

- expected authorization behavior is consistent
- common failure states render understandable UI feedback

### Phase 1E: Deploy And Runtime Readiness

Goal: ensure the existing product can be deployed and operated without
refactor-era tribal knowledge.

Execution:

1. dry-run the bare-host deployment path
2. verify reverse proxy, `/app/*`, `/api/*`, and `/q/*` routing assumptions
3. document required environment variables and service startup order
4. verify health, readiness, and rollback expectations

Exit gate:

- deploy steps are documented and believable
- runtime failure points are visible before user traffic

### Phase 1F: Observability And Operational Confidence

Goal: make common failures diagnosable.

Execution:

1. review logs for login, export, questionnaire, and async job paths
2. add or improve request correlation where practical
3. expose meaningful status for background work and failed jobs
4. document the minimum operator troubleshooting checklist

Exit gate:

- common support questions can be answered from logs and status surfaces

## Deliverables

- updated convergence checklist for the current EDC baseline
- documented end-to-end operator workflow
- targeted fixes for broken UX, permission, or deploy/runtime gaps
- verification notes for backend, frontend, and questionnaire-service
- a bounded handoff into the new R&D roadmap

## Exit Criteria

This phase is complete when:

1. the current EDC path is usable end-to-end
2. baseline verification is repeatable and current
3. deploy/runtime expectations are documented
4. major auth/error-handling surprises are removed
5. starting `Notification And Task Engine` would no longer be blocked by basic
   product instability


## Progress Log

### 2026-06-27

- Activated Phase 1 after Phase 0 completion and rerun.
- Slice 1 core workflow continuity is already represented in the current worktree: event definition create support, subject detail route repair, event scheduling status/ordinal defaults, CRF route correction, and bare deploy drift cleanup.
- Slice 2 started with export download hardening: completed jobs now verify artifact existence/readability before returning a download resource, missing/unreadable artifacts return a 404 text response, and targeted export service/controller tests cover the behavior.
- Slice 2 also removed obsolete package-level pnpm config after confirming `frontend/pnpm-workspace.yaml` already contains the supported `allowBuilds` setting; frontend typecheck/lint now run without the pnpm 11 warning.
- Slice 2 added the first global frontend auth-failure path: `ApiClient` emits 401/403 events, `AuthProvider` clears local session and redirects to `/login` for 401, and redirects to `/app/403` for 403. Data-capture attachment list/upload, import upload, and export download now use `ApiClient`; LogViewer actuator fetch emits the same auth-failure event on 401/403. Remaining direct `fetch` usage is auth bootstrap/login/logout and client internals.
- Slice 2 aligned the current CSRF contract by removing obsolete frontend XSRF token/header injection while backend CSRF remains disabled; restoring CSRF later requires a token-bootstrap design rather than a silent frontend-only assumption.

### 2026-06-28

- Slice 2 continued with minimum role gates for core EDC surfaces. Added shared backend authority expressions and applied `@PreAuthorize` to study administration, CRF management, subject mutations, event mutations, data-capture read/write endpoints, discrepancy-note read/write endpoints, import jobs, and export jobs/downloads.
- Added `CoreControllerAuthorizationTest` reflection coverage for 52 secured controller methods so the minimum role contract is explicit and guarded without requiring a database-backed security integration test.
- Verified the slice with `mvn test -pl app -am '-Dtest=CoreControllerAuthorizationTest,ModulithVerificationTest' '-Dsurefire.failIfNoSpecifiedTests=false'` (53 tests passed).
- Continued Slice 2 with export download study scoping: `ExportController` now passes the current user id into `ExportService.getDownload`, and `ExportService` verifies same-study export permission through `CurrentStudyAccessService` before returning artifacts. Sysadmin/techadmin users bypass per-study role lookup; regular users need an active export-capable role on the export job's study. Monitor-only or inactive roles are denied.
- Verified export scoping with `mvn test -pl app -am '-Dtest=CurrentStudyAccessServiceTest,ExportServiceTest,ExportControllerTest,CoreControllerAuthorizationTest,ModulithVerificationTest' '-Dsurefire.failIfNoSpecifiedTests=false'` (87 tests passed). Remaining authorization work is extending the same resource-scoping pattern beyond export downloads.
- Slice 2 export scoping was extended from downloads to the export job lifecycle: create, list, get, cancel, and retry now use the authenticated user id and require same-study export access. New jobs set `requestedBy` from the session user instead of trusting request body input.
- Verified expanded export scoping with `mvn test -pl app -am '-Dtest=ExportServiceTest,ExportControllerTest,CurrentStudyAccessServiceTest,ModulithVerificationTest' '-Dsurefire.failIfNoSpecifiedTests=false'` (37 tests passed).
- Slice 3 started with runtime readiness clarification: `deploy-bare.sh` now exposes a `health` command that checks local app `/actuator/health`, local questionnaire `/health`, and Caddy proxy responses for `/app/`, `/api/v1/auth/me`, and `/q/health`. `docs/HOST_DEPLOYMENT.md` was rewritten to use `deploy-bare.sh`, document route ownership, distinguish local actuator readiness from authenticated operator dashboard health, and record rollback steps.
- Bash validation for `deploy-bare.sh` could not run in the current Windows environment because `bash` is not installed on `PATH`. Static inspection of the changed command dispatch/help block completed; remaining Slice 3 work is to run `bash -n deploy-bare.sh`, `bash deploy-bare.sh help`, and a dry start/status/health path on a Linux host or disposable runtime.
- Slice 4 started with audit context hardening: event service audit records now derive study id for scheduled/updated/completed/removed/restored study events, event CRF remove/restore, and event definition remove/restore. Data capture item-data create/update audit records now derive study id from event CRF to study subject. Global subject and identity audit records remain intentionally study-null where no single study context is guaranteed.
- Verified audit context hardening with `mvn test -pl app -am '-Dtest=EventServiceTest,DataCaptureServiceTest,ModulithVerificationTest' '-Dsurefire.failIfNoSpecifiedTests=false'` (37 tests passed).
- Slice 4 continued with request correlation: added a highest-precedence servlet filter that accepts a safe `X-Request-ID` or generates one, stores it in MDC as `requestId`, echoes it on responses, and clears MDC after each request. Console and file log patterns now include the request id, with `no-request` for non-request logs.
- Verified request correlation with `mvn test -pl app -am '-Dtest=RequestCorrelationFilterTest,ModulithVerificationTest' '-Dsurefire.failIfNoSpecifiedTests=false'` (4 tests passed).
- Slice 4 continued with common API error responses: added a global REST exception handler for common authorization, not-found, and bad-request failures. Responses now use a JSON envelope with timestamp, status, error, message, path, and the current request correlation id so frontend and logs can reference the same failure id.
- Verified API error responses with `mvn test -pl app -am '-Dtest=ApiExceptionHandlerTest,RequestCorrelationFilterTest,ModulithVerificationTest' '-Dsurefire.failIfNoSpecifiedTests=false'` (7 tests passed).
- Slice 4 continued on the frontend error path: `ApiClient` now parses backend JSON error envelopes, keeps text responses readable for download failures, and exposes `requestId`/`path` on `ApiError` so page-level UX can show the same id operators see in logs.
- Verified frontend error parsing with `pnpm -C frontend test -- --run src/api/client.test.ts` (2 tests passed) and `pnpm -C frontend typecheck` (0 errors). The first Windows pnpm run had to refresh `frontend/node_modules` because existing shims pointed at an older Linux path.
- Slice 4 continued with visible frontend request-id feedback: added a shared API error formatter and applied it to export download, admin export job create/cancel/retry, and import upload/validate/commit errors so structured backend `requestId` values appear in operator-facing error text.
- Verified frontend request-id formatting with `pnpm -C frontend test -- --run src/api/client.test.ts src/api/errors.test.ts` (4 tests passed) and `pnpm -C frontend typecheck` (0 errors). The first sandboxed Vitest run produced pnpm temp-file unlink errors on Windows; rerunning in CI mode outside the sandbox passed.

## Next Phase

After this phase completes, activate the first new-module backlog derived from:

- `docs/product/researchedc-final-open-source-modular-plan.md`

Initial scope:

1. `Notification And Task Engine`
2. `Participant Identity And Access`
