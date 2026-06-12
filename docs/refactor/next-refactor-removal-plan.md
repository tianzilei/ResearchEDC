# Next Refactor And Removal Plan

**Created:** 2026-06-11
**Updated:** 2026-06-12
**Status source:** current worktree plus regenerated `docs/refactor/legacy-workflow-inventory.{csv,md}`.

## Current Status

Legacy removal is not complete.

Completed foundations:

- Phase 0 inventory and guardrails are in place.
- Phase B schema ownership is complete.
- SOAP `ws/` is absent.
- DAO SPI widening is complete for the 24 main DAO families.
- Phase 1 web/JSP/servlet deletion is closed; the `web/` directory is absent.
- A `dataimport` Modulith module and SPA import wizard exist with focused service/controller tests, typed preview/result contracts, commit audit events, and hardened attachment download.

Current generated legacy inventory (2026-06-12):

| Surface | Remaining |
|---|---:|
| Active legacy workflow artifacts | 125 |
| JSP views | 0 |
| Legacy servlets | 0 |
| Spring MVC route artifacts | 0 |
| DAO files under `shared/dao` | 95 |
| Shared services in inventory | 30 |
| Unknown inventory rows | 0 |

Remaining blockers:

| Slice | Count | Status |
|---|---:|---|
| Phase 3 DAO implementation deletion | 95 | Blocked by module-owned repository/service replacements for every SPI method and removal of adapter fallback SQL |
| Phase 4 shared service deletion | 30 | Blocked by active callers, import/export compatibility, ODM/rule/data-entry behavior, or DAO extraction |
| Import/export compatibility hardening | module work | Initial upload/validate/commit/audit and attachment download hardening complete in commit `bc1f24d97`; rollback proof added after commit `ae72d2415`; remaining compatibility gap is broader ODM/OpenRosa/export contract coverage; legacy import job scheduling is retired in the current tree and guarded against reintroduction |

## Current Import State

The current worktree contains:

- `app/src/main/java/org/researchedc/module/dataimport/` with `ImportJob`, repository, DTOs, controller, service, and `ImportCrfDataAdapter`.
- `POST /api/v1/imports/upload`, which uploads and creates one import job.
- A temporary `/api/legacy/import/upload` bridge that delegates to `ImportService`.
- SPA `frontend/src/pages/admin/ImportManager.tsx`, which uploads through `/api/v1/imports/upload`, displays typed validation preview output, and validates/commits existing jobs.
- `DataCaptureController`/`DataCaptureService` attachment support now uses event-CRF keyed routes and opaque attachment ids.

Completed in commit `bc1f24d97`:

- Focused `ImportService`, import controller, legacy upload bridge, data-capture service/controller, and import audit event tests.
- Legacy `/api/legacy/import/upload` resolves `requestedBy` from the session user when available.
- `ImportService.validate()` returns `ImportPreviewDTO`; `GET /api/v1/imports/{id}/preview` exposes stored preview.
- `ImportService.commit()` returns `ImportResultDTO`, records result stats, and publishes a Modulith event consumed by the audit module.
- Attachment list/download/upload routes are keyed by event CRF; downloads use opaque attachment ids and permission/path-traversal checks.

Remaining import/export compatibility issues to resolve:

- Done after commit `ae72d2415`: item persistence failures in `ImportCrfDataAdapter.commitImport()` now fail the commit instead of logging-and-continuing; `scripts/ci/check-import-rollback-postgres.sh` proves failed import-style transactions leave no partial writes against an explicit disposable PostgreSQL database.
- Done after commit `15c27c51e`: legacy `ImportSpringJob`/`ExampleSpringJob` classes are absent from the current tree; import scheduling is formally retired unless a new module-owned worker is explicitly added, and guardrails now prevent those Quartz jobs from returning.
- Done in this slice: rule XML import is formally retired in the current tree; no app/frontend upload route exists, the stale Spring bean wiring was removed, and guardrails prevent reintroducing app/frontend XML import wiring.
- Done in this slice: OpenRosa submission processing now resolves the CRF version OID from the submitted XForm root when clients omit an explicit form context value, and unit tests cover canonical context propagation plus explicit-value precedence.
- Done in this slice: export API controller contract coverage now protects create/list/get/cancel/retry JSON behavior, including an ODM_XML job request.
- Broaden representative ODM import fixture coverage beyond the focused module tests.

## Next Plan

### 1. Add Import Module Tests

Status: **complete in commit `bc1f24d97`**.

Goal: protect the existing upload -> job -> validate -> commit lifecycle before changing behavior.

Actions:

- Add `ImportServiceTest` for upload/create/list/get/validate/commit/failure transitions.
- Add controller contract tests for canonical `/api/v1/imports/upload`, list, validate, and commit.
- Cover the legacy `/api/legacy/import/upload` bridge if it remains.
- Add migration smoke coverage if existing migration checks do not include `2026-06-11-import-tables.xml`.
- Run Modulith verification to confirm `dataimport` boundaries are valid.

Exit gate:

- Targeted import tests pass.
- `ModulithVerificationTest` passes with the `dataimport` module.
- `mvn -pl app -am compile -DskipTests` and `cd frontend && pnpm typecheck` pass.

### 2. Replace String Summary With Typed Preview

Status: **complete in commit `bc1f24d97`**.

Goal: make validation output usable by the SPA and testable without parsing ad hoc JSON fragments.

Actions:

- Add a typed validation/preview DTO for metadata errors, event CRF status, edit-check results, warnings, and counts.
- Add `GET /api/v1/imports/{id}/preview` or return the typed preview from validate.
- Persist preview/result in `summaryJson` only as storage, not as the service/controller contract.
- Update `ImportManager` to show structured errors and warnings.

Exit gate:

- A representative valid ODM file reaches `VALIDATED` with counts.
- A representative invalid ODM file exposes structured errors.
- SPA displays the same decision points as the legacy confirm/verify path.

### 3. Prove CRF Data Validation Coverage

Status: **initial focused coverage complete in commit `bc1f24d97`; broader representative ODM fixture coverage remains.**

Goal: prove the imported ODM path validates the clinical data conditions that still matter.

Actions:

- Validate ODM XML schema/version.
- Validate study metadata/OID correspondence.
- Validate event CRF status eligibility.
- Preserve existing edit-check coverage in `ImportCrfDataAdapter.validateEditChecks()`.
- Add tests for blocked status, metadata mismatch, empty event CRF data, and edit-check failures.

Exit gate:

- Valid and invalid representative ODM files produce deterministic preview results.
- Validation failures do not advance to commit.

### 4. Prove Commit And Audit

Status: **audit event/result DTO coverage complete in commit `bc1f24d97`; failed item persistence now aborts commit and disposable PostgreSQL rollback proof is covered by `scripts/ci/check-import-rollback-postgres.sh`.**

Goal: prove the SPA/API path performs actual data mutation with rollback and audit parity.

Actions:

- Preserve transaction boundaries and rollback on partial failure.
- Record audit events equivalent to the legacy import action.
- Store result stats: subjects/events/event CRFs/items inserted or updated, warnings, errors.

Exit gate:

- Validated import commits data in a disposable database.
- Failed commit leaves no partial writes.
- Audit/result output is visible through `/api/v1/imports/{id}` or preview/result endpoints.

### 5. Harden Attachment Download

Status: **complete in commit `bc1f24d97` for current filesystem-backed attachment compatibility path.**

Goal: make attachment download safe as app/module compatibility code.

Actions:

- Replace raw `fileName` download with a route keyed by event CRF/item data/file id.
- Resolve the stored file path server-side.
- Enforce the same `mayViewData`/study access checks as the legacy servlet.
- Block path traversal with canonical-path checks against the attachment root.
- Add service/controller tests for allowed download, missing file, unauthorized access, and traversal.

Exit gate:

- SPA data-entry/file components can download attachments without raw path parameters.
- Security tests cover the high-risk cases.

### 6. Phase 3 DAO Replacement Ledger

Goal: turn the 95 remaining `shared/dao` files into an actionable deletion queue.

Actions:

- Generate a per-SPI method ledger for the 24 widened DAO families plus minor DAO families.
- Mark every method as `module-backed`, `fallback-sql`, `legacy-only`, or `unused`.
- Prioritize high-value fallback SQL removals in CRF/data capture, study event, rule, dataset/filter, and discrepancy-note groups.
- Delete only implementation/support files with no bean registration, factory method, inheritance dependency, or caller.

Exit gate:

- A checked-in DAO deletion ledger exists.
- At least one low-risk DAO implementation/support file is proven removable or explicitly deferred with the blocking SPI methods listed.

### 7. Reconcile Inventory After Each Slice

Actions:

- Regenerate `legacy-workflow-inventory.{csv,md}`.
- Update `remove-legacy-code-plan.md` counts after regeneration.
- Update this plan current inventory table after every deletion/replacement slice.
- Keep `AGENTS.md` and `.sisyphus/LEGACY_REFACTOR_PLAN.md` aligned when the top-level baseline changes.

Exit gate:

- Generated inventory and handoff docs agree on counts and next action.
- Legacy guardrails pass.
- Backend compile, Modulith verification, app tests, frontend typecheck/tests pass for code changes.

## Verification Commands

Run in this order for the next slice:

```bash
git status --short
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl app -am -Dtest="Import*Test,DataCaptureServiceTest"
cd frontend && pnpm typecheck
cd frontend && pnpm test --run
```

For deletion or baseline updates, also run:

```bash
bash scripts/ci/check-legacy-guardrails.sh
scripts/ci/generate-legacy-inventory.py --output-dir docs/refactor --basename legacy-workflow-inventory
```

## Recommended Immediate Commit Boundary

Next commit should start the Phase 3 DAO deletion ledger and remove only artifacts with proven replacement coverage.
