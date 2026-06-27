# Phase 0 App Business Logic Review

**Created:** 2026-06-27
**Status:** Complete
**Scope:** `app/src/main/java/org/researchedc/module/*`
**Purpose:** Phase 0 rerun artifact describing the current Java app business logic.

## Verification Rerun

The Phase 0 verification gate was rerun on 2026-06-27.

| Area | Command | Result | Notes |
|---|---|---:|---|
| Backend compile | `mvn clean compile -DskipTests` | PASS | Build succeeded. Maven still reports git dubious-ownership warnings from the buildnumber plugin and compiler warnings. |
| Backend targeted tests | `mvn test -pl app -am -Dtest=ModulithVerificationTest,OdmExportGeneratorTest,ExportArtifactWriterTest -Dsurefire.failIfNoSpecifiedTests=false` | PASS | 27 tests passed: Modulith 1, ODM export 21, artifact writer 5. |
| Frontend typecheck | `pnpm -C frontend typecheck` | PASS | pnpm still warns that `pnpm.onlyBuiltDependencies` in `package.json` is ignored by pnpm 11. |
| Frontend lint | `pnpm -C frontend lint` | PASS | 0 lint errors. Same pnpm config warning. |
| Frontend tests | `pnpm -C frontend test --run` | PASS | 25 Vitest tests passed. |
| Questionnaire tests | `uv run --group dev pytest app/tests/ -v` | PASS | 40 pytest tests passed. |

One first attempt at the backend targeted tests failed because it was run in
parallel with `mvn clean compile`; both Maven processes wrote to `target/`, and
`testResources` observed a missing output directory. The sequential rerun passed
and is the valid product result.

## Business Model

The Java app is a Spring Boot modular monolith. The current core EDC path is:

```text
authenticated user
  -> study / site context
  -> subject creation and study enrollment
  -> study event definition and event scheduling
  -> event CRF lookup
  -> CRF metadata, sections, items, response sets
  -> item data save, attachment upload/download, rule lookup
  -> discrepancy, audit, import, export, randomization, OpenRosa boundaries
```

The app uses module-owned JPA entities and repositories under
`org.researchedc.module.*`. `shared/` is resource-only for the current app
logic.

## Security And Session Contract

`SecurityConfig` applies to `/api/**`.

Current behavior:

- `/api/v1/auth/login`, `/api/v1/auth/me`, and `/api/v1/openrosa/**` are public.
- all other `/api/**` endpoints require an authenticated session.
- logout is `/api/v1/auth/logout`, invalidates the HTTP session, and deletes `JSESSIONID`.
- session fixation protection migrates the session after authentication.
- maximum concurrent sessions is set to 1; later logins are allowed to replace earlier sessions.
- CSRF is disabled in the backend.
- method security is enabled, but only selected controllers use `@PreAuthorize`.

Business implication:

- the app currently relies on coarse session authentication for most module APIs.
- study-scope and role-scope authorization is not consistently enforced at method level.
- frontend CSRF token handling remains stricter than the backend contract.

## Study And Site Management

Primary API: `/api/v1/studies`

Business behavior:

- list top-level studies and child sites separately.
- search studies by name.
- get study detail; top-level studies include site summaries.
- create study with required `name`.
- generated study OID defaults to `S_` plus a sanitized uppercase unique identifier, or `S_STUDY`.
- `featureFlags` defaults to `{}` on create.
- update study fields by partial request.
- delete study physically through repository delete.
- update status by setting `statusId`.
- expose and update study feature flags.

Audit behavior:

- create, update, delete, and status updates write audit records with source module `study`.

Current business constraints:

- only study name is explicitly required during create.
- delete is not a soft-delete in `StudyService`; it calls repository delete.
- role/study authorization is not enforced in `StudyService`; it depends on API/session layer and callers.

## Identity, Roles, And Authentication

Primary APIs:

- `/api/v1/auth`
- `/api/v1/identity`

Business behavior:

- authentication uses Spring Security `DaoAuthenticationProvider` and `ResearchEdcUserDetailsService`.
- users can be searched by first or last name.
- users can be fetched by id or username.
- users can be created with `userName`, profile fields, enabled/account-unlocked defaults, and owner id.
- duplicate usernames are rejected.
- roles are study-scoped records with `userName`, `studyId`, `roleName`, and `statusId`.
- roles are assigned, soft-removed with `statusId=5`, and restored with `statusId=1`.
- user profiles can be updated.
- password change requires matching the current encoded password.

Audit behavior:

- user creation, role assignment/removal/restoration, profile update, and password change create audit records.

Current business constraints:

- `CreateUserRequest` creation does not set an initial password in `IdentityService`.
- role assignment does not validate that the study or user exists in the service method.
- role names are also used by dashboard module selection, so spelling/casing matters.

## Dashboard Bootstrap And Operator Context

Primary API: `/api/v1/dashboard`

Business behavior:

- resolves the current user id from the security context.
- loads current user, roles, top-level studies, site names, module permissions, task counts, recent audit events, system status, health, and runtime info.
- default study is the first top-level study where the user has a matching role.
- default site is the first child site with an explicit user role, or the first child site as fallback.
- module visibility is role-name driven.
- task counts currently include pending CRFs and open discrepancy notes.
- health checks database validity and local disk free space.

Current business constraints:

- dashboard module permissions are an app-level mapping, not a full authorization policy.
- role names are mixed style, including camelCase and snake_case entries.
- health is app-local and separate from any Spring Actuator readiness contract.

## Subject And Enrollment Workflow

Primary API: `/api/v1/subjects`

Business behavior:

- search subjects by unique identifier.
- create a subject with required `uniqueIdentifier`; optional DOB, gender, and DOB-collected fields are stored.
- list study subjects by study.
- enroll an existing subject into a study with label, secondary label, enrollment date, and OC OID.
- generated study-subject OID defaults to `SS_{subjectId}_{studyId}`.
- enrollment can optionally schedule an event immediately when `eventDefinitionId` is provided.
- study-subject reassignment updates `studyId`.
- subject and study-subject remove/restore are soft status changes: removed is `statusId=5`, available is `statusId=1`.
- e-signature currently records an audit note; it does not persist a distinct signature object.

Audit behavior:

- subject create/remove/restore and study-subject enrollment/reassignment/sign/remove/restore are audited.

Current business constraints:

- enroll validates the subject exists, but does not validate the study exists.
- enrollment does not prevent duplicate study-subject rows for the same subject/study combination.
- optional enrollment-triggered event scheduling uses event status defaults and the current owner id.

## Event Workflow

Primary API: `/api/v1/events`

Business behavior:

- list event definitions by study.
- create an event definition with required `studyId` and `name`.
- event definitions default to non-repeating `false` and `statusId=1` when not provided.
- remove/restore event definitions are soft status changes.
- list study events by study subject.
- schedule a study event with required `studySubjectId` and `studyEventDefinitionId`.
- scheduled events default to `statusId=1` and `subjectEventStatusId=2`.
- event ordinal uses the request ordinal when positive; otherwise it is the next ordinal for the subject/definition pair.
- update events through partial field replacement.
- complete event sets both status fields to `7`.
- remove/restore study events are soft status changes.
- list event CRFs for a study event.
- get, remove, and restore event CRFs.

Audit behavior:

- event definition, study event, and event CRF mutations create audit records with source module `event`.

Current business constraints:

- event audit records often use `studyId=null` because the service does not derive study id from related subject/event rows.
- event scheduling does not validate that the event definition belongs to the same study as the study subject.
- completing an event does not verify that all related event CRFs or required item data are complete.

## CRF Metadata Workflow

Primary APIs:

- `/api/v1/crfs`
- `/api/v1/crfs/manage`

Business behavior:

- list CRFs with version counts.
- fetch CRF version detail including ordered sections.
- fetch items by section and CRF version.
- item DTOs include required, regexp, default value, ordinal, PHI flag, units, OID, and text data type.
- fetch simple conditional display metadata for a section.
- create and update CRFs.
- create CRF versions with default `statusId=1`.
- delete CRF versions by repository delete.
- update CRF version status.

Current business constraints:

- CRF create/update/version operations do not currently write audit records in `CrfService`.
- item data type is currently returned as `"text"` in the DTO mapping.
- deleting a CRF version is physical deletion.
- CRF metadata management does not enforce lifecycle constraints such as "cannot delete version with captured data" at service level.

## Data Capture Workflow

Primary API: `/api/v1/data-capture`

Business behavior:

- load item data by event CRF.
- load response set options.
- load item groups by CRF or by CRF version.
- save one item datum by event CRF, item id, value, optional ordinal, and optional status.
- existing item data is found by event CRF + item id, or event CRF + item id + ordinal when ordinal is present.
- existing item data is updated in place; otherwise a new row is created.
- new item data defaults to `statusId=1` and `deleted=false`.
- batch save loops through single item save.
- rule lookup returns applicable rule sets, rules, and expressions for an event CRF, but does not perform full expression evaluation.
- attachment list/download/upload is authorized through `AttachmentStorageAdapter.canViewEventCrfData`.
- attachment ids are URL-safe base64 encoded filenames.
- attachment filenames reject blank names, path separators, and traversal-shaped names.
- attachments are stored under local study attachment directories.

Audit behavior:

- item data create/update writes audit records with source module `datacapture`.
- attachment upload/download/list do not currently write audit records in `DataCaptureService`.

Current business constraints:

- item save does not validate CRF item membership for the event CRF before persisting.
- item save does not enforce required fields, regex validation, data type validation, or CRF completion state.
- rule lookup is metadata retrieval, not rule execution.
- attachments use local filesystem storage and directory scanning, which is targeted by the MinIO storage convergence plan.

## Discrepancy Notes

Primary API: `/api/v1/discrepancy-notes`

Business behavior:

- list notes with optional study, entity type, entity id, and status filters.
- fetch a note by id.
- create a note.
- resolve a note.
- dashboard counts open notes.

Current business constraints:

- discrepancy note behavior is available but not deeply integrated into the data capture save path.
- Phase 0 did not perform a deep discrepancy lifecycle audit beyond API/service availability.

## Import Workflow

Primary API: `/api/v1/imports`

Business behavior:

- create an import job directly or upload a file and create the job in one step.
- upload stores files under `~/ResearchEDC/data/imports/{uuid}`.
- upload supports import type strings `CRF_DEFINITION` and `CRF_DATA`; other values default to CRF data.
- new import jobs start as `STAGED`.
- validation moves through `VALIDATING` and then `VALIDATED`, `INVALID`, `BLOCKED`, or `FAILED`.
- validation parses ODM, validates metadata for the target study, validates event CRF status, and runs edit-check validation summary.
- commit requires status `VALIDATED`, a parsed preview status of `validated`, no preview errors, and zero edit-check errors.
- commit parses ODM again, delegates persistence to `ImportCrfDataAdapter`, records a result summary, publishes `ImportCommittedEvent`, and marks the job `COMPLETED`.

Audit behavior:

- import commit publishes an application event.
- audit handling for import commit is implemented by `ImportAuditEventHandler`.

Current business constraints:

- import file staging uses local filesystem storage and `Path` based parsing.
- upload does not currently enforce file size/type guardrails before storing.
- validation and commit both require the stored local path to remain readable.

## Export Workflow

Primary API: `/api/v1/exports`

Business behavior:

- create export jobs with study id, name, export format, requested user, criteria JSON, and ODM contract version.
- ODM contract defaults to `OC2_1`.
- new jobs start as `PENDING`.
- `ODM_XML` jobs execute immediately in the service call.
- execution marks jobs running, generates ODM XML, writes an artifact, and marks completed.
- jobs can be listed by study and filtered in memory by status, format, contract version, requester, and creation date range.
- pending/running jobs can be cancelled.
- failed retryable jobs can be reset to pending with incremented retry count.
- download requires job status `COMPLETED` and a non-null `filePath`.
- download returns a `FileSystemResource`, filename `export_{id}.xml`, and stored file size.

Current business constraints:

- export artifact download does not verify file existence/readability before returning the resource.
- export artifacts are local filesystem files.
- export creation does not currently write audit records.
- non-ODM formats are queued as `PENDING` but have no execution path in `ExportService`.

## Audit

Primary API: `/api/v1/audit`

Business behavior:

- module services call `AuditService.recordAudit` for many create/update/delete/assign operations.
- audit records include study id, event type, entity type/id/label, old value, new value, performed-by user, details, and source module.
- after saving, `AuditService` publishes `AuditRecordedEvent`.
- list endpoints expose audit logs globally or by study.
- selected audit endpoints require `@PreAuthorize`, including user login, user event, study subject event, and database changelog reads.

Current business constraints:

- some core services pass `studyId=null` even when study context can be derived.
- `AuditService.onAuditRecorded` is currently empty.
- audit coverage is uneven across modules.

## Rules, Datasets, Filters, Subject Groups

Primary APIs:

- `/api/v1/rules`
- `/api/v1/rules/schedule`
- `/api/v1/datasets`
- `/api/v1/filters`
- `/api/v1/subject-groups`

Business behavior:

- rules expose rule sets, rules, rule-set/rule mappings, and schedule helper checks.
- data capture can retrieve rule metadata applicable to an event CRF.
- datasets and filters provide basic list/get/create surfaces.
- subject groups expose group classes and groups, with create/update behavior.

Current business constraints:

- these areas are available as module APIs but were not the primary Phase 0 business-depth target.
- rule evaluation in data capture is not a full edit-check execution engine.

## Randomization Boundary

Primary API: `/api/v1/randomization`

Business behavior:

- create and update schemes while they are `DRAFT`.
- schemes require study id, name, algorithm, and valid arms.
- activate requires `DRAFT` status and at least one arm.
- close moves any non-closed scheme to `CLOSED`.
- randomization requires an `ACTIVE` scheme and rejects duplicate subject assignment within a scheme.
- supported algorithms are simple, block, and stratified block.
- stratified block builds a stratum path from configured stratifications and request values.
- assignments are persisted with selected arm, subject id, stratum path, active status, and assigned-by user.
- randomization has its own audit log.

Boundary note:

- Phase 0 treats randomization as an available boundary module; this document does not assert full statistical or allocation-depth validation.

## OpenRosa Boundary

Primary API: `/api/v1/openrosa`

Business behavior:

- public endpoint under security config.
- form list requires a valid study OID.
- XForm download requires valid study OID and CRF version OID.
- manifest returns XML for a form id after study OID validation.
- submission accepts raw XML or multipart `xml_submission_file` with `ecid`.
- successful submissions return XML with HTTP 201; rejected submissions return XML with HTTP 406.
- HEAD submission advertises OpenRosa version and accepted content length.

Boundary note:

- OpenRosa is intentionally public at the route level. Authorization and study access semantics should be a conscious product decision, not an implicit default.

## File And Artifact Storage

Current local filesystem business storage exists in three Java app paths:

- import upload staging: `~/ResearchEDC/data/imports/{uuid}`
- export artifacts: local `filePath` returned as `FileSystemResource`
- data capture attachments: local study attachment directories resolved by `AttachmentStorageAdapter`

Business implication:

- local storage is still an active runtime dependency for import, export, and attachments.
- this directly supports the need for `phase-1-storage-minio-convergence-plan.md`.

## Phase 0 Rerun Findings

### Still Healthy

- backend compile passes.
- targeted Modulith/export tests pass.
- frontend typecheck, lint, and Vitest pass.
- questionnaire pytest passes with `uv`.
- Phase 1 slice 1 fixes are visible in current app logic: event definition create exists, event scheduling defaults status/ordinal, and core routes expose the previously missing endpoints.

### Still Open Business Risks

| ID | Risk | Current Evidence | Recommended Phase |
|---|---|---|---|
| BL-1 | Authorization is mostly session-level, not role/study-scope complete. | `SecurityConfig` authenticates `/api/**`; only selected audit endpoints use `@PreAuthorize`. | Phase 1 auth slice |
| BL-2 | CSRF contract remains inconsistent. | Backend disables CSRF while frontend still sends XSRF headers. | Phase 1 auth slice |
| BL-3 | Study context is missing in several audit records. | event and data capture services often record `studyId=null`. | Phase 1 audit slice |
| BL-4 | Data capture save is permissive. | item save does not enforce item membership, required fields, regex, type validation, or CRF completion state. | Phase 1 data capture hardening |
| BL-5 | Local business file storage remains active. | import, export, and attachments all use filesystem paths. | Storage MinIO convergence |
| BL-6 | Export download did not validate artifact existence/readability. | Fixed in Phase 1 slice 2: `ExportService.getDownload` checks existence/readability and missing artifacts return 404 text. | Complete |
| BL-7 | Some destructive operations are physical deletes. | study delete and CRF version delete call repository delete. | Product policy decision |
| BL-8 | Non-ODM export formats have no execution path. | `ExportService.createJob` executes only `ODM_XML`; other formats remain pending. | Export productization |

## Recommended Next Work

1. Finish Phase 1 auth predictability: align CSRF, add global 401/403 UX, and define study/role-scope rules.
2. Harden data capture persistence around item membership, required fields, regex/type validation, and event/CRF completion state.
3. Execute `phase-1-storage-minio-convergence-plan.md` to remove hidden local filesystem dependencies.
5. Normalize audit study id population for study-scoped mutations.
6. Decide product policy for physical delete versus status-based removal in study and CRF version management.
