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

## Next Phase

After this phase completes, activate the first new-module backlog derived from:

- `docs/product/researchedc-final-open-source-modular-plan.md`

Initial scope:

1. `Notification And Task Engine`
2. `Participant Identity And Access`
