# Post-Refactor Product Hardening Plan

**Created:** 2026-06-24
**Status:** ✅ Completed on 2026-06-24; historical record, now superseded
**Superseded by:** `docs/refactor/post-hardening-stabilization-plan.md`
**Purpose:** historical execution plan for the first post-refactor hardening phase.

## Completion Note

This plan has been executed in substance.

Delivered outcomes:

- ODM export execution pipeline added
- ODM contract versioning connected to export execution
- export artifact writing and download endpoint added
- frontend lint errors reduced to zero
- frontend warnings reduced to zero

Representative delivery commits:

- `b3e2e5dbc feat: ODM export execution pipeline + frontend lint zero-error + warning reduction`
- `5e798b38f refactor(frontend): eliminate no-explicit-any warnings (48→0, 15 warnings remain)`
- `b893a01d3 refactor(frontend): eliminate all lint warnings (76→0)`

## Why This Document Is Historical

Its original baseline is now outdated:

- the export module no longer stops at job metadata management
- frontend lint no longer has `11` errors / `76` warnings

A fresh verification pass after these deliveries exposed a new stabilization concern:

- backend compile/test verification is currently red because `app/` still relies on dependencies that are no longer arriving transitively after `shared/` became resource-only

That follow-up work is tracked in:

- `docs/refactor/post-hardening-stabilization-plan.md`

The remainder of this document is preserved as the original pre-delivery execution baseline and is no longer current state.

## Summary

The legacy-removal program is complete. The next work is no longer source cleanup. It is product hardening:

1. turn the export module from job metadata management into a real ODM export execution path,
2. restore the frontend lint gate to zero errors, and
3. refresh verification and documentation baselines after those changes land.

## Current Verified State

### Refactor Baseline

- `shared/src/main/java` contains `0` Java files.
- Legacy workflow inventory is closed.
- DAO SPI deletion is complete.
- `web/` and `ws/` are absent.
- ODM contract versioning is complete:
  - `OC2-0` compatibility schema retained
  - `OC2-1` email-free schema added
  - `OdmContractVersion` and `OdmSchemaResourceResolver` exist in the export module

### Export Module Baseline

- `app/src/main/java/org/researchedc/module/export/` currently supports:
  - job creation
  - job status persistence
  - job list/detail/cancel/retry endpoints
- The module does **not yet** expose a full ODM export execution pipeline in the current tree.
- `ExportJob` already stores:
  - `exportFormat`
  - `odmContractVersion`
  - `filePath`
  - `fileSize`
  - `errorMessage`

### Frontend Quality Baseline

`pnpm -C frontend lint` currently fails with:

- `11` errors
- `76` warnings

High-signal error clusters are currently in:

- `frontend/src/components/StudySwitcher.tsx`
- `frontend/src/components/form-engine/DataEntryPrintView.tsx`
- `frontend/src/components/form-engine/SectionTabs.tsx`
- `frontend/src/hooks/useCrf.ts`
- `frontend/src/pages/admin/ImportManager.tsx`
- `frontend/src/pages/EntityAction.tsx`

## Primary Goal

Move the project from "refactor complete" to "operationally ready baseline" by finishing the export product path and restoring tighter quality gates.

## Workstreams

### Workstream A: ODM Export Execution

**Goal:** make `ODM_XML` exports produce real artifacts governed by `OdmContractVersion`.

#### A1. Define The Execution Path

Add an explicit runtime seam for export generation. The current service should no longer be only a CRUD/status service.

Recommended additions:

- `ExportExecutionService` or `OdmExportExecutionService`
- `ExportArtifactWriter`
- optional `ExportJobRunner` / `ExportJobProcessor` if execution is backgrounded

#### A2. Connect Contract Version Selection To Output

Use `OdmSchemaResourceResolver` during export generation, not only as a passive utility.

Required behavior:

- `ODM_XML` export must resolve the selected contract version
- `OC2_1` is the default for new jobs
- `OC2_0_COMPAT` remains opt-in compatibility behavior

#### A3. Generate And Persist Export Artifacts

Implement the minimum viable artifact flow:

1. mark job `RUNNING`
2. generate ODM payload
3. write file to a deterministic export location
4. persist `filePath` and `fileSize`
5. mark job `COMPLETED`
6. on failure, persist `FAILED` plus `errorMessage`

Design rule:

- the first slice can support only `ODM_XML`
- `CSV`, `SAS_XPORT`, and `EXCEL` may remain job-level formats if they already rely on other paths or are deferred

#### A4. Decide Execution Model

Choose one explicit model and document it:

- synchronous execution inside request flow for the first slice, or
- background execution via app-managed runner/poller

Recommended first step:

- start with a simple app-owned execution service and explicit status transitions
- avoid introducing Quartz or external worker infrastructure unless clearly required

#### A5. Add Export Tests

Add or extend tests to cover:

- default contract selection is `OC2_1`
- compatibility mode selects `OC2_0_COMPAT`
- schema resolver paths are used by export execution
- `ODM_XML` export omits `FacilityContactEmail` when `OC2_1` is selected
- job state transitions are correct on success/failure

Stretch tests:

- file artifact existence
- malformed generation path writes `FAILED` status and message

#### A6. Optional API Follow-Up

If useful after execution works, add:

- download endpoint for completed export artifacts
- artifact metadata in response DTO
- clear error payloads for failed exports

### Workstream B: Frontend Lint Recovery

**Goal:** restore `pnpm -C frontend lint` to zero errors first, then reduce warning noise intentionally.

#### B1. Fix The 11 Errors

Prioritize only blocking lint errors in the first slice.

Recommended order:

1. `StudySwitcher.tsx`
2. `DataEntryPrintView.tsx`
3. `SectionTabs.tsx`
4. `useCrf.ts`
5. `ImportManager.tsx`
6. `EntityAction.tsx`

Patterns already identified:

- unnecessary type assertions
- prefer `??` over `||`
- `Array<T>` style violations
- optional-chain preference
- restricted `+` operand typing

#### B2. Group Warnings By Theme

After errors are gone, group warnings into batches instead of fixing them randomly:

- `no-explicit-any`
- hook dependency warnings
- non-null assertions
- deprecated API usages
- fast-refresh export structure warnings

#### B3. Define A Warning Policy

Choose one policy explicitly:

- leave warnings as tracked debt with a documented count, or
- ratchet warnings down in batches with per-slice targets

Recommended policy:

- do not promise zero warnings immediately
- do require zero errors
- then reduce warnings by category with focused slices

### Workstream C: Baseline Refresh

**Goal:** refresh docs and verification commands so the repository state matches reality.

#### C1. Update Documentation Counts

After A and B land, refresh:

- `AGENTS.md`
- `docs/refactor/README.md`
- `docs/refactor/refactor-removal-roadmap.md`

Update only factual baselines that changed, such as:

- frontend lint counts
- export module capabilities
- verification status

#### C2. Refresh Verification Pass

Run the standard checks after each major slice:

```bash
git status --short
bash scripts/ci/check-legacy-guardrails.sh
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
pnpm -C frontend typecheck
pnpm -C frontend lint
```

Add targeted export tests when Workstream A lands.

## Recommended Delivery Order

### Phase 1: Export Execution Minimum Slice

Deliver the smallest real ODM export path first.

Scope:

- runtime execution service
- schema-version-aware generation
- file persistence
- success/failure status transitions
- focused backend tests

Why first:

- this turns existing export contract/versioning work into user-visible product behavior
- it closes the biggest gap between current architecture and current capability

### Phase 2: Frontend Lint Zero-Error Slice

After the export path exists, clear the frontend lint errors.

Scope:

- only the 11 current errors
- no broad warning cleanup yet

Why second:

- brings frontend quality back to a harder gate quickly
- keeps scope bounded

### Phase 3: Warning Reduction And Baseline Refresh

After hard blockers are gone:

- reduce frontend warnings by category
- refresh docs and status counts
- run a broader regression pass

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Export execution expands too far into a full reporting platform | scope creep | first slice supports only `ODM_XML` |
| Contract versioning remains metadata-only | false sense of completion | require runtime resolver usage in generation path |
| Background execution adds unnecessary complexity | slow delivery | start with simple app-owned execution path |
| Frontend warning cleanup overwhelms the slice | loss of focus | fix errors first, warnings in categorized batches |
| Docs drift again after changes | misleading baseline | reserve explicit baseline refresh as a final phase |

## Success Criteria

### For Workstream A

- `ODM_XML` jobs produce real artifacts
- `OdmContractVersion` changes output contract selection
- success/failure states are persisted correctly
- tests cover contract selection and export transitions

### For Workstream B

- `pnpm -C frontend lint` has `0` errors
- warning count is either reduced or explicitly documented as accepted debt

### For Workstream C

- top-level project docs reflect current capabilities and counts
- verification commands pass for the delivered slices

## Immediate Next Action

Start with **Phase 1: Export Execution Minimum Slice**.

Concrete first implementation step:

1. add an app-owned ODM export execution service,
2. wire `ExportService` to invoke it for `ODM_XML` jobs,
3. use `OdmSchemaResourceResolver` during generation,
4. persist artifact metadata and state transitions,
5. add focused tests before expanding the API surface.
