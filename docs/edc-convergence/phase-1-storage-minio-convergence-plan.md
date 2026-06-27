# Phase 1 Storage MinIO Convergence Plan

**Created:** 2026-06-27
**Status:** Prepared / pending activation
**Parent:** `docs/edc-convergence/phase-1-edc-usability-convergence-plan.md`
**Release posture:** no RC tag, no publish action

## Purpose

Move all active upload, generated-artifact, and download flows from local
filesystem storage to MinIO-backed object storage.

This plan covers both the Java EDC app and `questionnaire-service`. The outcome
should be one storage contract, one deploy/runtime configuration model, and no
hidden dependency on local ephemeral paths for business files.

## Current File Storage Inventory

| Area | Current path | Code | Current behavior | Target |
|---|---|---|---|---|
| ODM/data export artifacts | `./exports/odm/{jobId}/export_{jobId}.xml` | `ExportArtifactWriter`, `ExportService.getDownload` | Writes and downloads local files through `FileSystemResource` | MinIO object write/read through storage port |
| Import uploads | `~/ResearchEDC/data/imports/{uuid}` | `ImportService.uploadFile`, `ImportService.validate`, `ImportService.commit` | Upload writes local file; validate/commit parse local path | MinIO object upload, then stream object into parser |
| Data capture attachments | `filePath/attached_files/{studyOid}/{filename}` or `attached_file_location` | `DataCaptureService`, `AttachmentStorageAdapter`, `AttachmentStorageProperties` | Upload, list, and download from local study directories | MinIO object prefix per study/event CRF |
| Questionnaire export artifacts | `/tmp/exports/{jobId}.{format}` | `questionnaire-service/app/workers/export_tasks.py` | Worker writes local file path to DB | MinIO upload with stored object key/download URL |
| Questionnaire storage helper | MinIO with `/tmp/exports` fallback | `questionnaire-service/app/services/storage_service.py` | MinIO exists but is not used by export worker; fallback writes local | Make MinIO primary and fail clearly or explicitly mark local fallback dev-only |
| Static SPA assets | classpath `/static` | `WebMvcConfig`, deploy build | Build artifact serving, not business storage | Keep local/classpath; out of scope |
| XSD/i18n/templates | classpath resources | shared/app resources | App resources, not uploaded business files | Keep local/classpath; out of scope |

## Target Architecture

### Storage Contract

Create a small app-owned object storage abstraction in the Java app:

1. `ObjectStorageService` interface with operations:
   - `put(String key, InputStream stream, long size, String contentType)`
   - `get(String key)` returning stream, size, content type, and metadata
   - `exists(String key)`
   - `delete(String key)`
   - `list(String prefix)`
   - `presignGet(String key, Duration ttl)` when browser-direct download is needed later
2. `MinioObjectStorageService` implementation backed by the official MinIO Java SDK.
3. `LocalObjectStorageService` only for tests/dev if explicitly enabled. It must not be the silent production default.
4. Store object keys in job/entity tables, not absolute filesystem paths.

Recommended key scheme:

```text
edc/exports/odm/{studyId}/{jobId}/export_{jobId}.xml
edc/imports/{studyId}/{jobId}/{uuid}-{safeOriginalName}
edc/attachments/{studyOid}/event-crf-{eventCrfId}/{safeFileName}
questionnaire/exports/{studyId}/{jobId}.{format}
```

### Configuration

Use a shared naming convention across deploy and services:

```text
RESEARCHEDC_OBJECT_STORAGE_ENDPOINT=http://localhost:9000
RESEARCHEDC_OBJECT_STORAGE_ACCESS_KEY=minio
RESEARCHEDC_OBJECT_STORAGE_SECRET_KEY=minio-password
RESEARCHEDC_OBJECT_STORAGE_BUCKET=researchedc
RESEARCHEDC_OBJECT_STORAGE_REGION=us-east-1
RESEARCHEDC_OBJECT_STORAGE_SECURE=false
RESEARCHEDC_OBJECT_STORAGE_REQUIRE=true
```

Map existing questionnaire `MINIO_*` values to these names or keep aliases with
one documented source of truth.

### Download Strategy

Initial implementation should keep API-compatible downloads:

- Java export download remains `GET /api/v1/exports/{id}/download`.
- Java attachment download remains `GET /api/v1/data-capture/events/{eventCrfId}/attachments/{attachmentId}`.
- Controllers stream objects from MinIO through the app so existing auth checks remain effective.
- Presigned URLs can be added later only after authorization and audit behavior is explicit.

## Execution Phases

### Phase S0: Storage Contract And Runtime Gate

Goal: introduce the shared storage foundation without moving business flows yet.

Tasks:

1. Add Java MinIO dependency in the appropriate Maven module/BOM.
2. Add object storage configuration properties.
3. Implement `ObjectStorageService` and MinIO-backed implementation.
4. Add bucket startup validation when `RESEARCHEDC_OBJECT_STORAGE_REQUIRE=true`.
5. Add focused unit tests with a fake/in-memory implementation and integration test profile notes for real MinIO.
6. Update `deploy-bare.sh` and `deploy-docker.sh` env generation to use the unified object storage names.

Exit gate:

- App starts with MinIO config.
- Missing MinIO fails fast when storage is required.
- Tests can use a fake/local implementation without Docker.

### Phase S1: Export Artifacts To MinIO

Goal: move generated EDC export artifacts off local disk first. This is the
lowest-risk generated-file path.

Tasks:

1. Replace `ExportArtifactWriter` local `Files.write` with `ObjectStorageService.put`.
2. Change export job persistence from absolute `filePath` to object key semantics.
   - Short term: reuse `filePath` column to store `object:minio:{key}` or raw key.
   - Preferred: add `artifact_object_key`, `artifact_storage_provider`, and keep `filePath` read-only/deprecated.
3. Update `ExportService.getDownload` to stream from object storage and verify object existence/readability.
4. Add missing-artifact behavior: completed job with missing object returns operator-readable 404/error.
5. Add tests for write, download, missing object, and failed storage write.

Exit gate:

- New exports are written to MinIO.
- Downloads work through existing API.
- Local export path is no longer used for new jobs.

### Phase S2: Import Uploads To MinIO

Goal: move import upload staging to object storage while preserving validation
and commit behavior.

Tasks:

1. Replace `ImportService.uploadFile` local `Files.copy` with object storage upload.
2. Persist object key and original filename.
3. Refactor `ImportCrfDataAdapter.parseOdm(Path filePath)` to accept `InputStream` or a `ReadableResource` abstraction.
4. Update validate/commit to open the object stream from MinIO.
5. Add size/type guardrails before upload.
6. Add tests for upload, validate, commit, missing object, and storage failure.

Exit gate:

- Import uploads no longer write to `~/ResearchEDC/data/imports`.
- Validation and commit can parse directly from object storage streams.

### Phase S3: Data Capture Attachments To MinIO

Goal: move CRF/event attachments from study directories to object storage.

Tasks:

1. Replace `AttachmentStorageAdapter` filesystem path resolution with object-key resolution.
2. Keep current event-CRF authorization checks before every list/download/upload.
3. Store attachment metadata if needed. Options:
   - derive list from MinIO prefix for minimal schema change
   - preferred: add `module_attachment` metadata table with object key, filename, size, content type, event CRF id, study oid, owner, created date
4. Replace `MultipartFile.transferTo(dest)` with object storage upload.
5. Replace `FileInputStream` download streaming with object storage stream.
6. Add duplicate filename behavior explicitly: reject, overwrite with audit, or version key.
7. Add tests for upload/list/download, path traversal names, unauthorized access, and missing object.

Exit gate:

- Attachment upload/list/download no longer touches `attached_files` local directories.
- Existing safe filename and authorization protections remain covered by tests.

### Phase S4: Questionnaire Exports To MinIO

Goal: make questionnaire-service use MinIO as the primary export artifact store.

Tasks:

1. Replace `save_to_file(buffer, /tmp/exports/...)` in `export_tasks.py` with `storage_service.upload`.
2. Store object key or returned download URL consistently in `ExportJob.file_path`.
   - Preferred: add `object_key` and `storage_provider`; keep `file_path` as deprecated response compatibility field.
3. Update `questionnaire_exports.download_export` to return a presigned URL or stream through the API.
4. Remove silent local fallback for production. Keep fallback only behind an explicit dev setting.
5. Update health output to distinguish `required-unavailable` from `optional-unavailable`.
6. Add pytest coverage with mocked storage service.

Exit gate:

- Questionnaire export worker writes to MinIO.
- Download endpoint returns a usable MinIO-backed download path.

### Phase S5: Migration And Compatibility

Goal: handle existing local files without breaking operators.

Tasks:

1. Add a one-time migration tool to scan:
   - `./exports`
   - `~/ResearchEDC/data/imports`
   - configured `attached_file_location` / `filePath/attached_files`
   - `/tmp/exports` for questionnaire artifacts only if preserving local dev artifacts matters
2. Upload discovered files to MinIO using the target key scheme.
3. Update DB rows to object keys after successful upload.
4. Produce a dry-run report before mutation.
5. Leave local files untouched until validation confirms object checksums/sizes.
6. Document rollback: DB rows can point back to local paths only before local storage code is removed.

Exit gate:

- Migration dry-run and apply modes exist.
- Existing artifacts can be moved without manual key editing.

### Phase S6: Remove Local Business Storage Defaults

Goal: prevent regression to local filesystem business storage.

Tasks:

1. Remove or guard old local directories from active code paths.
2. Delete misleading deploy log text such as local fallback for exports unless still explicitly supported.
3. Add `rg` guardrail to CI for forbidden patterns in business storage code:
   - `new File(` in data capture storage paths
   - `FileSystemResource` in export downloads
   - `Files.copy(file.getInputStream())` for uploads
   - `/tmp/exports` in questionnaire worker
4. Update operator docs and `.env` examples.

Exit gate:

- New business files require object storage unless a named dev profile is active.
- CI fails on accidental reintroduction of local business file storage.

## Data Model Notes

Preferred schema additions:

- `export_job.artifact_object_key`
- `export_job.artifact_storage_provider`
- `import_job.source_object_key`
- `import_job.source_storage_provider`
- `module_attachment` table for event CRF attachments
- questionnaire `export_job.object_key` and `storage_provider`

Compatibility option:

- Keep existing `filePath` / `storedFilePath` fields temporarily, but treat them
  as object keys after cutover. This is faster but less explicit.

Recommendation: add explicit object key columns for clarity and keep legacy path
columns only for migration visibility.

## Verification Gates

Run after each implementation slice:

```bash
mvn test -pl app -am -Dtest=ExportServiceTest,ExportArtifactWriterTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl app -am -Dtest=ImportServiceTest,DataCaptureServiceTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
pnpm -C frontend typecheck
pnpm -C frontend lint
cd questionnaire-service/apps/api && uv run --group dev pytest app/tests/ -v
bash -n deploy-bare.sh
bash deploy-bare.sh help
```

Add MinIO-backed integration checks when Docker is available:

```bash
docker run -d --name researchedc-minio-test -p 9000:9000 -p 9001:9001 \
  -e MINIO_ROOT_USER=minio -e MINIO_ROOT_PASSWORD=minio-password \
  minio/minio server /data --console-address :9001
```

## Risks And Decisions

1. **Presigned URL vs app-streamed download:** start with app-streamed downloads to preserve authorization and audit behavior.
2. **Existing DB columns:** explicit object-key columns reduce ambiguity but require migrations.
3. **Questionnaire fallback:** silent `/tmp` fallback is convenient but hides production misconfiguration. Make it explicit dev-only.
4. **Attachment listing:** prefix listing is simple but weak for metadata and audit. A metadata table is better for EDC-grade behavior.
5. **Object deletion:** do not physically delete objects on job cancellation or UI delete until retention policy is defined.

## Exit Criteria

This plan is complete when:

1. all new business uploads and generated artifacts are written to MinIO
2. Java export, import, and attachment downloads read from MinIO
3. questionnaire exports write and download through MinIO
4. local filesystem business storage is dev-only or migration-only
5. deploy scripts and docs expose one object storage configuration model
6. tests cover successful storage, missing object, and storage failure behavior
