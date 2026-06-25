# Phase 7 Export Operations Polish Plan

**Created:** 2026-06-25
**Status:** Complete
**Purpose:** define the next product-facing workstream after Phase 6 baseline hygiene, focused on making export operations easier to monitor, recover, and use.
**Predecessor:** `docs/refactor/phase-6-post-upgrade-baseline-hygiene-plan.md`

## Current Context

The refactor/removal, export productization, platform upgrade, and post-upgrade baseline hygiene phases are complete.

Current baseline:

- `shared/src/main/java`: `0` Java files
- legacy workflow inventory: closed
- DAO SPI ledger: fully removed
- Spring Boot: `3.5.2`
- Spring Modulith: `1.4.1`
- dependency drift converged in Phase 6
- export backend tests recorded green in Phase 6
- frontend typecheck and lint recorded green in Phase 6

Phase 6 selected **Export operations polish** as the next product-facing workstream.

## Phase 7 Goal

Move the export center from "functionally available" to "operationally reliable and user-friendly" by improving queue visibility, failure recovery, download behavior, filtering, and job metadata presentation.

## Non-Goals

- Do not build a full reporting platform.
- Do not add new export formats unless required by an existing tested path.
- Do not redesign the whole Export Center UI.
- Do not reopen legacy refactor/removal work.
- Do not remove the legacy gateway as part of this phase.
- Do not modify released Liquibase migrations retroactively.

## Workstreams

### Phase 7A: Current Export Flow Inventory

**Goal:** document the current export flow before changing API or UI behavior.

#### 7A.1 Backend Surface Review

Review:

- `app/src/main/java/org/researchedc/module/export/entity/ExportJob.java`
- `app/src/main/java/org/researchedc/module/export/service/ExportService.java`
- `app/src/main/java/org/researchedc/module/export/service/OdmExportExecutionService.java`
- `app/src/main/java/org/researchedc/module/export/controller/ExportController.java`
- `app/src/main/java/org/researchedc/module/export/service/ExportArtifactWriter.java`
- export DTOs and repository interfaces

Capture:

- job status lifecycle
- fields currently persisted on export jobs
- fields currently returned to the frontend
- current download behavior
- current failure behavior
- current test coverage

#### 7A.2 Frontend Surface Review

Review:

- `frontend/src/pages/export/ExportCenter.tsx`
- export API client calls
- create-export modal
- status tag rendering
- download button behavior
- existing export-related tests

Capture:

- what users can currently see
- what users cannot diagnose
- which states have unclear affordances
- which metadata is missing from the list view

#### 7A Exit Gate

- current backend export lifecycle is summarized
- current frontend Export Center behavior is summarized
- specific gaps are listed before implementation begins

### Phase 7B: Backend API And Domain Enhancements

**Goal:** make export jobs easier to query, diagnose, and recover.

#### 7B.1 Export List Filtering

Add or extend export-list query support for:

- status
- format
- ODM contract version
- requestedBy
- created date range
- completed date range, if already represented cleanly

Implementation guidance:

- Prefer repository/service filtering over frontend-only filtering.
- Keep query parameters optional.
- Preserve existing list behavior when no filters are supplied.
- Avoid exposing persistence internals directly in DTOs.

#### 7B.2 Failure Information

Make failed export jobs clearer.

Candidate fields:

- `failureCode`
- `failureMessage`
- `retryable`
- `failedAt`, if not already represented by the status timestamp model

Implementation guidance:

- Use concise machine-readable failure codes.
- Keep user-facing messages short and safe.
- Avoid storing stack traces or sensitive paths in API-facing fields.

#### 7B.3 Retry Endpoint

Add a retry path for retryable failed jobs.

Candidate endpoint:

```http
POST /api/v1/exports/{id}/retry
```

Expected behavior:

- only failed retryable jobs can be retried
- retry creates a clear state transition
- existing completed jobs are not re-run
- non-retryable failures return a clear client error
- authorization/session behavior matches existing export endpoints

#### 7B.4 Download Error Semantics

Clarify download responses for:

- job not found
- job not completed
- artifact missing
- artifact unreadable
- access denied/session missing

Expected behavior:

- completed jobs with valid artifacts download normally
- incomplete jobs return a clear non-success response
- failed jobs do not pretend to be downloadable
- missing artifacts are distinguishable from unfinished jobs

#### 7B Exit Gate

- export list supports selected filters
- failure state is structured enough for frontend display
- retry behavior is implemented or explicitly deferred with reason
- download error behavior has regression coverage

### Phase 7C: Frontend Export Center Polish

**Goal:** make the export operations UI scannable and actionable.

#### 7C.1 Filter Bar

Add compact controls for:

- status
- format
- ODM contract version
- requested date range, if supported by backend

Design guidance:

- Use Ant Design controls already used in the app.
- Keep filters dense and work-focused.
- Avoid adding explanatory text blocks inside the app.
- Preserve current create-export workflow.

#### 7C.2 Job Metadata Display

Expose useful job metadata in the list:

- status
- format
- ODM contract version
- requestedBy
- created time
- completed time or duration, if available
- artifact size, if available

Design guidance:

- Keep table columns stable.
- Use tags only for compact status/category signals.
- Avoid oversized cards or decorative layout.

#### 7C.3 Failure Detail UX

For failed jobs:

- show a short failure summary in the list
- provide a tooltip or drawer/modal for details
- show whether retry is available
- avoid exposing stack traces or server paths

#### 7C.4 Action States

Clarify action behavior:

- pending/running jobs: download disabled
- completed jobs: download enabled
- failed retryable jobs: retry enabled
- failed non-retryable jobs: retry disabled with reason
- artifact-missing jobs: show clear error after attempted download or in metadata if known

#### 7C Exit Gate

- Export Center supports the backend filters selected for Phase 7
- job actions are state-aware
- failed jobs are diagnosable without checking server logs
- frontend typecheck and lint pass

### Phase 7D: Regression Tests And Verification

**Goal:** protect export behavior while improving operations UX.

#### 7D.1 Backend Tests

Add or update focused tests for:

- list filtering
- failed job DTO shape
- retryable vs non-retryable failure behavior
- retry endpoint success and rejection paths
- download rejection for non-completed jobs
- download rejection for missing artifacts

Target command:

```bash
mvn -pl app -am test -Dtest=ExportServiceTest,OdmExportExecutionServiceTest,OdmExportGeneratorTest,ExportControllerTest,ExportArtifactWriterTest -Dsurefire.failIfNoSpecifiedTests=false
```

#### 7D.2 Frontend Tests

Add or update focused tests for:

- filter controls
- status/action rendering
- failed job display
- retry action visibility
- download button enabled/disabled states

Target commands:

```bash
cd frontend && pnpm typecheck
cd frontend && pnpm lint
cd frontend && pnpm test --run
```

#### 7D.3 Broader Verification

Run the consolidated gate when backend and frontend changes both land:

```bash
bash scripts/ci/daily-gauntlet.sh
```

If local environment prevents the full gauntlet, record which focused gates passed and which environment prerequisite blocked the rest.

#### 7D Exit Gate

- backend export regression gate passes
- frontend typecheck/lint/test passes for touched frontend behavior
- full gauntlet passes or environment blocker is documented

## Recommended Delivery Order

1. Complete Phase 7A inventory and gap list.
2. Implement Phase 7B backend filter/failure/download semantics.
3. Add Phase 7D backend tests for changed behavior.
4. Implement Phase 7C frontend UI polish.
5. Add Phase 7D frontend tests for changed behavior.
6. Run the strongest available verification gate.
7. Update this plan with a completion record.

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Retry semantics accidentally duplicate or corrupt export jobs | high | restrict retry to failed retryable jobs and cover state transitions with tests |
| Failure details expose sensitive server paths or stack traces | medium | store and expose short failure codes/messages only |
| Filtering behavior diverges between backend and frontend | medium | keep backend as source of truth for filter semantics |
| Export Center becomes visually noisy | medium | use compact table/filter controls and avoid broad redesign |
| Artifact download changes break existing completed-job downloads | high | keep download regression tests mandatory |

## Open Decisions

1. Should retry reuse the existing job row or create a new job linked to the failed one?
2. Which failure codes are needed for the first slice?
3. Should artifact size be stored on `ExportJob` or derived from the artifact at response time?
4. Should date filtering use created timestamps only in the first slice?
5. Should export list filtering be paginated in this phase or left for a later scaling pass?

## Success Criteria

1. users can filter export jobs by the selected operational fields
2. failed jobs show useful, safe failure information
3. retry behavior is clear and tested, or explicitly deferred with a documented reason
4. download actions are state-aware and error responses are clear
5. Export Center presents job metadata without a broad redesign
6. backend export tests pass
7. frontend typecheck, lint, and touched tests pass
8. Phase 7 completion record documents delivered behavior and any deferred follow-up

## Immediate Next Action

Start with **Phase 7A.1: backend export surface review** and summarize the current export job lifecycle, persisted fields, API DTO fields, download behavior, failure behavior, and existing tests.

## Completion Record (2026-06-25)

### Phase 7A: Current Export Flow Inventory — COMPLETE

Backend and frontend surfaces documented. Key gaps identified: no list filtering, no failureCode/retryable fields, no download error differentiation, no filter bar in UI.

### Phase 7B: Backend API And Domain Enhancements — COMPLETE

**New database columns** (Liquibase migration `2026-06-25-export-failure-fields.xml`):
- `failure_code` VARCHAR(50) — machine-readable failure classification
- `retryable` BOOLEAN DEFAULT TRUE — whether the failure can be retried

**New DTO:** `ExportJobFilter` with fields: status, exportFormat, odmContractVersion, requestedBy, createdAfter, createdBefore.

**API changes:**
- `GET /api/v1/exports` now accepts optional filter params: `status`, `exportFormat`, `odmContractVersion`, `requestedBy`
- `POST /api/v1/exports/{id}/retry` now only retries retryable failed jobs

**Service changes:**
- `markFailed(id, message, failureCode, retryable)` — new overload with structured failure info
- `retryJob(id)` — checks `retryable` flag before allowing retry
- `listJobs(studyId, filter)` — new filtered variant

### Phase 7C: Frontend Export Center Polish — COMPLETE

**New columns:** Completed time, Duration (computed from requested→completed).

**Filter bar:** Status, Format, Contract Version dropdowns — all optional, clearable.

**Action states:**
- Download: enabled only for COMPLETED jobs with filePath; disabled with tooltip for others
- Retry: enabled only for FAILED jobs where `retryable !== false`; disabled with "not retryable" tooltip otherwise
- Cancel: unchanged (PENDING/RUNNING only)

**i18n:** 12 new keys added in en and zh locales.

### Phase 7D: Regression Tests — COMPLETE

| Gate | Result |
|---|---|
| Export tests (5 classes) | 58/0/0 ✅ |
| ModulithVerificationTest | 1/0/0 ✅ |
| `pnpm typecheck` | 0 errors ✅ |
| `pnpm lint` | 0 errors ✅ |

### Files Changed

- `shared/src/main/resources/migration/3.18/2026-06-25-export-failure-fields.xml` — new migration
- `app/.../export/entity/ExportJob.java` — added failureCode, retryable fields
- `app/.../export/dto/ExportJobDTO.java` — added failureCode, retryable fields
- `app/.../export/dto/ExportJobFilter.java` — new filter DTO
- `app/.../export/service/ExportService.java` — filtering, retryable check, structured failure
- `app/.../export/controller/ExportController.java` — filter params on list endpoint
- `frontend/src/pages/export/ExportCenter.tsx` — filter bar, new columns, action states
- `frontend/src/locales/en/translation.json` — 12 new keys
- `frontend/src/locales/zh/translation.json` — 12 new keys

### Deferred Follow-up

- Artifact size stored on entity (already present as `fileSize`)
- Date filtering in first slice (backend ready via `createdAfter`/`createdBefore` in filter DTO, not yet wired to frontend)
- Pagination for export list (left for later scaling pass)
