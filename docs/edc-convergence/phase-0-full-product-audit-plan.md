# Phase 0 Full Product Audit Plan

**Created:** 2026-06-26
**Status:** Prepared / pending activation
**Predecessor:** `docs/refactor/phase-18-post-cleanup-verification-baseline-plan.md`
**Successor:** `docs/edc-convergence/phase-1-edc-usability-convergence-plan.md`
**Release posture:** no RC tag, no publish action, no new module expansion during
audit

## Purpose

Run one complete audit of the current ResearchEDC product before convergence
implementation begins.

This audit should establish a factual baseline for:

- what already works
- what is partially working
- what is broken or misleading
- what is missing for daily operator use

Primary focus for this round:

- core EDC business logic
- workflow continuity across study, subject, event, CRF, data capture,
  discrepancy, and export
- authorization and state-transition correctness in the main operator path

Boundary-only focus for this round:

- `randomization`
- `questionnaire-service`

The audit is the first execution step of the EDC convergence program.

## Goal

Produce a complete, prioritized view of product readiness across the current EDC
surface so convergence work starts from evidence instead of intuition.

Target outcomes:

1. the current product surface is inventoried by workflow and area
2. major blockers are classified by severity and ownership
3. verification, usability, security, deploy, and observability gaps are
   separated clearly
4. core logic defects are separated from integration-edge defects
5. convergence implementation can be sequenced from audit findings

## Non-Goals

- do not start feature expansion
- do not reopen broad refactor/removal work
- do not tag, release, or publish local commits
- do not remove `randomization` or `questionnaire-service`
- do not treat `randomization` or `questionnaire-service` as full-scope business
  logic audit targets in this round

## Audit Priority

The audit is logic-first.

Priority order:

1. core EDC workflow logic
2. authorization, state transitions, and data integrity behavior
3. operator-facing UX gaps that block the main workflow
4. deploy/runtime and observability gaps
5. integration-boundary checks for `randomization` and
   `questionnaire-service`

Business-first handling rule:

1. identify and rank business-logic defects before code-quality defects
2. treat broken workflow behavior, wrong state transitions, missing permission
   rules, and inconsistent business outcomes as first-class blockers
3. defer refactor, cleanup, test-shape, naming, and non-blocking code-structure
   concerns unless they directly prevent business-logic repair

## Audit Coverage

### 0A. Verification Audit

Scope:

1. backend compile and targeted test gates
2. frontend typecheck, lint, and test gates
3. questionnaire-service test gate
4. contract and generated-artifact drift checks where relevant

Output:

- pass/fail status
- reproduction commands
- failure classification: environment, tooling, product, or flaky

### 0B. Workflow Audit

Scope:

1. login and study context bootstrap
2. study management
3. subject enrollment and lookup
4. visit / event workflow
5. CRF entry and data capture
6. discrepancy note visibility
7. export workflow
8. questionnaire entry points and integration boundaries only
9. randomization entry points and integration boundaries only

Output:

- happy-path availability
- blockers, dead ends, confusing transitions, and missing states
- workflow-specific severity ranking
- explicit separation between core-logic blockers and boundary-module blockers

### 0C. Authorization And Security Audit

Scope:

1. session and CSRF behavior
2. module endpoint authorization
3. download and export permission checks
4. public questionnaire exposure review at the access-boundary level
5. operator-visible error leakage review

Output:

- authorization defects
- inconsistent auth behavior
- unsafe or ambiguous access paths

### 0D. Frontend UX Audit

Scope:

1. loading, empty, and error states
2. routing coherence under `/app/*`
3. Chinese and English text rendering
4. navigation clarity for core workflows
5. operator feedback around failed actions
6. boundary-module entry behavior without full logic-depth review

Output:

- UX friction list
- broken or misleading UI states
- high-impact polish backlog

### 0E. Deploy And Runtime Audit

Scope:

1. bare-host deploy flow
2. reverse proxy routing for `/app/*`, `/api/*`, and `/q/*`
3. required environment configuration
4. health, readiness, and restart expectations
5. rollback and failure recovery notes

Output:

- deploy risks
- undocumented runtime assumptions
- operator runbook gaps

### 0F. Observability Audit

Scope:

1. login, export, questionnaire, and async job logging
2. audit-module visibility for meaningful business events
3. request correlation and traceability
4. failed-job visibility
5. minimum troubleshooting path

Special note:

- audit logging coverage should be checked most deeply on the core EDC logic
  path; `randomization` and `questionnaire-service` only need boundary-level
  signal verification in this round

Output:

- logging blind spots
- missing operational signals
- short observability improvement list

## Deliverables

- one audit report or checklist covering all audited areas
- prioritized finding list grouped by severity
- convergence entry backlog based on audit evidence
- explicit recommendation for what Phase 1 should fix first
- explicit label on every finding: `core-logic` or `boundary-integration`
- explicit label on every finding: `business-logic` or `code-quality`

## Exit Criteria

This phase is complete when:

1. every scope area above has been reviewed
2. blockers are grouped into verification, workflow, auth/security, UX,
   deploy/runtime, and observability buckets
3. core-logic findings are clearly separated from randomization/questionnaire
   boundary findings
4. the next convergence tasks are evidence-based and ordered
5. Phase 1 can start without redefining what "usable EDC baseline" means
