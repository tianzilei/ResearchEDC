# EDC Convergence Docs Index

**Updated:** 2026-06-27

This directory contains the post-refactor execution plans for converging the current
ResearchEDC product into a stable, usable EDC baseline before starting the next
module-expansion roadmap.

## Scope

Use this directory for active work that is no longer part of legacy removal,
shared-Java retirement, or refactor cleanup.

Relationship to existing plan sets:

- `docs/refactor/` keeps the completed refactor/removal history and the final
  Phase 18 verification closure.
- `docs/product/researchedc-final-open-source-modular-plan.md` remains the
  12-month expansion roadmap for new modules after convergence is complete.
- `docs/edc-convergence/` is the operational bridge between those two states.

## Next Prepared Plans

**Prepared next plans:**

1. `phase-0-full-product-audit-report.md`
2. `phase-1-edc-usability-convergence-plan.md`
3. `phase-1-storage-minio-convergence-plan.md`

Activation sequence:

1. keep `docs/refactor/phase-18-post-cleanup-verification-baseline-plan.md`
   as the closed verification record
2. use `phase-0-full-product-audit-report.md` as the completed audit baseline
3. use the audit findings to activate
   `phase-1-edc-usability-convergence-plan.md`

## Primary Documents

| Document | Role | Status |
|---|---|---|
| `phase-0-full-product-audit-plan.md` | First post-refactor execution step: complete audit of verification, workflows, security, UX, deploy/runtime, and observability | Complete |
| `phase-0-full-product-audit-report.md` | Completed Phase 0 audit findings, verification results, and ordered Phase 1 entry backlog | Complete |
| `phase-1-edc-usability-convergence-plan.md` | First post-refactor product phase: stabilize the current EDC workflow, deployment, permissions, and operator usability | Active / in progress |
| `phase-1-storage-minio-convergence-plan.md` | Storage convergence plan to move uploads, generated artifacts, downloads, and questionnaire exports to MinIO-backed object storage | Prepared / pending activation |

## Directory Rules

1. Keep active EDC hardening and convergence plans here, not under `docs/refactor/`.
2. Do not reopen broad refactor/removal work unless a concrete regression requires it.
3. Keep module-expansion plans in `docs/product/` until the convergence exit gate is met.
4. Prefer small, workflow-oriented plans over broad umbrella documents.
