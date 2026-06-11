# Next Refactor And Removal Plan

**Created:** 2026-06-11
**Status source:** current worktree plus `docs/refactor/remove-legacy-code-plan.md` and generated inventory.

## Current Status

Legacy removal is not complete.

Completed foundations:

- Phase 0 inventory and guardrails are in place.
- Phase B schema ownership is complete.
- SOAP `ws/` is absent.
- DAO SPI widening is complete for the 24 main DAO families.
- Major Phase 1 JSP/servlet deletion slices are closed: admin read-only, CRF metadata cleanup, study/subject/event, export/dataset/filter, data entry/discrepancy, login/profile/enterprise/mail, and entity-action remove/restore gaps.

Current generated legacy inventory:

| Surface | Remaining |
|---|---:|
| Active legacy workflow artifacts | 208 |
| JSP views | 52 |
| Legacy servlets | 9 |
| Spring MVC route artifacts | 15 |
| DAO files under `shared/dao` | 100 |
| Shared services in inventory | 32 |
| Unknown inventory rows | 0 |

Remaining Phase 1 blockers:

| Slice | Count | Status |
|---|---:|---|
| CRF metadata/data-entry rendering | 11 + related data-entry rows | Blocked by full SPA CRF renderer, repeating groups, rules, discrepancy notes, print, attachments |
| Data entry/discrepancy | 26 | Blocked by same renderer/workflow parity |
| Import/export compatibility | 10 | Active next executable slice |
| Study/subject/event fallbacks | 22 | Needs route-by-route compatibility closure |
| Layout/common JSPs | 6 | Delete last |
| Spring MVC/OpenRosa compatibility | 15 routes | Needs public contract classification |

## In-Progress Worktree

The current worktree already contains an import/export implementation start:

- New `app/src/main/java/org/researchedc/module/dataimport/` module.
- New `ImportJob` entity, repository, DTOs, controller, service, and legacy `ImportCRFDataService` adapter.
- New Liquibase file `shared/src/main/resources/migration/3.18/2026-06-11-import-tables.xml`, included from `release.xml`.
- SPA `frontend/src/pages/admin/ImportManager.tsx` rewritten as a multi-step import manager.
- `ImportUploadController` now creates an import job during upload.
- `DataCaptureController`/`DataCaptureService` now expose a first attachment download endpoint.

This is not deletion-ready yet. It is an early scaffold and needs contract cleanup, real validation/commit behavior, tests, and security hardening before any legacy import/export artifacts are removed.

Immediate issues to resolve:

- Upload creates an `ImportJob`, then the SPA creates another job from the upload result.
- SPA sends `storedFile`, but backend `CreateImportJobRequest` expects `storedFilePath`.
- `POST /api/legacy/import/upload` still uses the legacy namespace and should either become `/api/v1/imports/upload` or be explicitly documented as a temporary bridge.
- `ImportService.validate()` and `commit()` only change statuses; they do not validate ODM, map data, run edit checks, or commit CRF data.
- `ImportCrfDataAdapter` exists but is not used by validation/commit.
- Attachment download accepts a raw file path/name and needs event CRF/item-level permission checks before it can replace `DownloadAttachedFileServlet`.
- No tests exist yet for the new `dataimport` module or attachment endpoint.

## Next Plan

### 1. Stabilize Import API Contract

Goal: make the new import module compile and expose one coherent upload -> job -> validate -> commit contract.

Actions:

- Move file upload into `ImportController` as `POST /api/v1/imports/upload`.
- Return a single `ImportJobDTO` from upload.
- Remove the duplicate SPA `createJob` step or change it into an explicit metadata-confirm step that updates the existing job.
- Align request/response field names (`storedFilePath`, `fileName`, `fileSize`, `importJobId`).
- Set `requestedBy` from the authenticated session instead of `null`.
- Keep `/api/legacy/import/upload` only as a temporary delegating compatibility endpoint if needed.

Exit gate:

- SPA no longer creates duplicate jobs.
- `ImportManager` can upload one file and show exactly one `STAGED` job.
- `mvn -pl app -am compile -DskipTests` and `cd frontend && pnpm typecheck` pass.

### 2. Add Import Module Tests

Goal: protect job lifecycle behavior before adding real import parsing.

Actions:

- Add `ImportServiceTest` for create/list/get/validate/commit/failure transitions.
- Add `ImportControllerTest` or web-layer contract tests for upload/list/validate/commit.
- Add migration smoke coverage if the existing migration checks do not include `2026-06-11-import-tables.xml`.
- Run Modulith verification to confirm the new `dataimport` module boundaries are valid.

Exit gate:

- Targeted import tests pass.
- `ModulithVerificationTest` passes with the new module.

### 3. Implement Real CRF Data Validation

Goal: replace the `ImportCRFDataServlet` validation path enough that `import.jsp` no longer provides unique behavior.

Actions:

- Call the existing `ImportCRFDataService` through `ImportCrfDataAdapter` as a temporary anti-corruption layer.
- Validate ODM XML schema/version.
- Validate study metadata/OID correspondence.
- Validate event CRF status eligibility.
- Collect validation/edit-check errors into structured `summaryJson` or a typed preview DTO.
- Add `GET /api/v1/imports/{id}/preview`.
- Show validation summary and errors in `ImportManager`.

Exit gate:

- A representative valid ODM file reaches `VALIDATED`.
- A representative invalid ODM file reaches `FAILED` or remains staged with structured errors.
- SPA displays the same decision points as the legacy confirm/verify path.

### 4. Implement Commit And Audit

Goal: make the SPA path perform the actual data mutation currently owned by `ImportCRFDataServlet`.

Actions:

- Wire `commit()` to the legacy service initially, then isolate module-owned pieces for later extraction.
- Preserve transaction boundaries and rollback on partial failure.
- Record audit events equivalent to the legacy import action.
- Store result stats: subjects/events/event CRFs/items inserted or updated, warnings, errors.

Exit gate:

- Validated import commits data in a disposable database.
- Failed commit leaves no partial writes.
- Audit/result output is visible through `/api/v1/imports/{id}` or preview/result endpoints.

### 5. Harden Attachment Download

Goal: replace `DownloadAttachedFileServlet` safely.

Actions:

- Replace raw `fileName` download with a route keyed by event CRF/item data/file id.
- Resolve the stored file path server-side.
- Enforce the same `mayViewData`/study access checks as the legacy servlet.
- Block path traversal with canonical-path checks against the attachment root.
- Add service/controller tests for allowed download, missing file, unauthorized access, and traversal.

Exit gate:

- SPA data-entry/file components can download attachments without `DownloadAttachedFileServlet`.
- Security tests cover the high-risk cases.

### 6. Reconcile Inventory And Delete Only Proven Artifacts

Goal: remove only artifacts whose replacement path is proven.

Deletion candidates after steps 1-5:

- `web/src/main/java/org/researchedc/control/submit/ImportCRFDataServlet.java`
- `web/src/main/webapp/WEB-INF/jsp/submit/import.jsp`
- `web/src/main/java/org/researchedc/control/submit/DownloadAttachedFileServlet.java`
- `web/src/main/webapp/WEB-INF/jsp/submit/downloadAttachedFile.jsp`

Defer:

- The six print JSPs until SPA print mode or ODM export equivalence is proven.

Actions:

- Remove servlet registrations/mappings.
- Delete JSPs only after no includes, forwards, links, or tests reference them.
- Regenerate `legacy-workflow-inventory.{csv,md}`.
- Update `phase-1-import-export-ledger.csv` with `covered/deleted/deferred` statuses.
- Update `remove-legacy-code-plan.md` counts after regeneration.

Exit gate:

- Generated inventory count drops for the deleted artifacts.
- Legacy guardrails pass.
- Backend compile, Modulith verification, app tests, frontend typecheck/tests pass.

## Verification Commands

Run in this order for the next slice:

```bash
git status --short
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl app -am -Dtest='Import*Test,DataCaptureServiceTest'
cd frontend && pnpm typecheck
cd frontend && pnpm test --run
```

For deletion steps, also run:

```bash
bash scripts/ci/check-legacy-guardrails.sh
scripts/ci/generate-legacy-inventory.py --output-dir docs/refactor --basename legacy-workflow-inventory
```

## Recommended Immediate Commit Boundary

First commit should only stabilize the scaffold:

- API contract cleanup.
- Duplicate job creation removed.
- Compile/typecheck fixes.
- Import service/controller tests for job lifecycle.
- No legacy servlet/JSP deletion.

Second commit should add real validation/preview.

Third commit should add commit/audit behavior and attachment download hardening.

Only the fourth commit should delete import/download legacy artifacts, and only if the exit gates above are satisfied.
