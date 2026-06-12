# Phase 1 — Import/Export Compatibility Slice

**Created:** 2026-06-11
**Status:** INVESTIGATION (gaps documented, parity path not yet built)
**Artifacts:** 10 (2 legacy servlets, 8 JSP views)
**Ledger:** `phase-1-import-export-ledger.csv`

## Summary

The import/export compatibility slice covers 10 artifacts (all classified `keep compatibility` in the legacy inventory). Unlike previous Phase 1 slices where SPA coverage was sufficient to delete, these artifacts represent **core data processing paths** that have no SPA or module-owned replacement.

## Inventory Breakdown

### Core Servlets (2)

| Artifact | Function | SPA Status |
|----------|----------|------------|
| `ImportCRFDataServlet.java` | Full ODM XML import pipeline: validate→map→commit | Upload only, delegates to legacy |
| `DownloadAttachedFileServlet.java` | File attachment download with permissions | None |

### Import Workflow (1 JSP)

| Artifact | Function |
|----------|----------|
| `import.jsp` | File upload form, posts to ImportCRFData?action=confirm |

### Print Views (6 JSPs)

All render CRF data in print format — used for regulatory inspection, ODM export verification, and CRF review.

| Artifact | Variant |
|----------|---------|
| `showAllSectionPrint.jsp` | All sections, with comments |
| `showAllSectionWithoutCommentsPrint.jsp` | All sections, without comments |
| `showSectionWithoutCommentsPrint.jsp` | Single section, without comments |
| `showFixedItemInputPrint.jsp` | Fixed item input print |
| `showFixedSectionPrint.jsp` | Fixed section print |
| `showItemInputPrint.jsp` | Single item input print |

### File Download (1 JSP)

| Artifact | Function |
|----------|----------|
| `downloadAttachedFile.jsp` | File download interstitial page |

## Current State

### What Exists (SPA/API)

| Component | Coverage | Notes |
|-----------|----------|-------|
| SPA `ImportManager.tsx` | File upload + redirect to legacy | Uploads via `POST /api/legacy/import/upload`, then opens legacy window |
| `ImportUploadController` | File staging only | Saves to `~/ResearchEDC/data/imports/`, sets session attrs. No processing. |
| SPA `ExportCenter.tsx` | Export job CRUD | Manages jobs via `/api/v1/exports`. No actual export execution in module. |
| Export module | Job state machine | `ExportService` manages PENDING→RUNNING→COMPLETED/FAILED. No data extraction logic. |
| SPA `DatasetBuilder.tsx` | Dataset creation UI | Uses legacy APIs |
| SPA `FilterBuilder.tsx` | Filter creation UI | Uses legacy APIs |

### What's Missing (Gaps)

| Gap | Impact | Effort |
|-----|--------|--------|
| No ODM XML validation pipeline in module | Cannot replace `ImportCRFDataServlet` | HIGH |
| No study metadata/OID validation in module | ImportCRFDataService is legacy-only | HIGH |
| No edit check execution during import | Validation errors shown in legacy JSP only | HIGH |
| No CRF definition (Excel) import in module | SPA ImportManager broken for `crf-def` type | MEDIUM |
| No file attachment download endpoint | `DownloadAttachedFileServlet` blocks deletion | MEDIUM |
| No print-mode CRF rendering in SPA | 6 print JSPs blocked | LOW (can be deferred) |
| No import job scheduling in module | Legacy Quartz jobs handle import batch | MEDIUM |
| Rule XML import path retired | No app/frontend upload route exists; stale `rulesPostImportContainerService` app wiring removed and guardrail added | DONE |

## The Import Pipeline (Legacy → Target)

### Current Legacy Path
```
import.jsp → ImportCRFDataServlet (action=confirm)
  ├── FileUploadHelper.returnFiles()
  ├── ODM 1.3/1.2.1 XML schema validation
  ├── Castor unmarshalling (ODMContainer)
  ├── ImportCRFDataService.validateStudyMetadata()
  ├── ImportCRFDataService.eventCRFStatusesValid()
  ├── ImportCRFDataService.fetchEventCRFBeans()
  ├── ImportCRFDataService.lookupValidationErrors() [edit checks]
  ├── ImportCRFDataService.generateSummaryStatsBean()
  └── Forward to verifyImport.jsp → commit
```

### Target Module-Owned Path (TO BUILD)
```
SPA ImportManager
  ├── Step 1: Upload file → POST /api/v1/imports/upload (staging)
  ├── Step 2: Validate → GET /api/v1/imports/{id}/validate (schema + metadata)
  ├── Step 3: Preview → GET /api/v1/imports/{id}/preview (summary + errors)
  ├── Step 4: Commit → POST /api/v1/imports/{id}/commit (process data)
  └── Step 5: Result → GET /api/v1/imports/{id}/result (status + stats)
```

## Dependency Chain

```
ImportCRFDataServlet deletion requires:
  ├── Module-owned import service (ODM validation, Castor → Jackson migration?)
  ├── ImportCRFDataService extraction from web/ → app/module/import/
  ├── REST API for import workflow steps
  └── SPA ImportManager rewrite (multi-step wizard)

import.jsp deletion requires:
  └── SPA ImportManager handles full workflow inline

DownloadAttachedFileServlet deletion requires:
  └── REST endpoint for file attachment download

Print JSPs deletion requires:
  ├── SPA DataEntryPage print mode, OR
  └── Proof that ODM XML export replaces print format

downloadAttachedFile.jsp deletion requires:
  └── DownloadAttachedFileServlet replaced first
```

## Blocking/Deferred Assessment

### Can Defer (print JSPs — 6 artifacts)
The 6 print-view JSPs are "nice to have" for regulatory review but can be deferred. They are:
- **Read-only** (no data mutation)
- **ODM-replaceable** — ODM XML export may suffice for audit purposes
- **Not navigated** from SPA — accessed via legacy paths only

**Recommendation:** Defer print JSPs until DataEntryPage has print mode OR ODM export is proven sufficient. These are NOT blocking the import/export compatibility slice.

### Must Build (core servlets — 2 artifacts)
`ImportCRFDataServlet` and `DownloadAttachedFileServlet` are actively used and have no replacement. The SPA ImportManager explicitly delegates to `ImportCRFDataServlet`.

### Non-Blocking JSP (2 artifacts)
`import.jsp` and `downloadAttachedFile.jsp` are UI shells — they can be deleted AFTER their backing servlets are replaced.

## Next Steps

1. **Create module-owned import service** (`app/module/import/`) with:
   - `ImportService` — orchestrates validate/map/commit
   - `ImportController` — REST API (`/api/v1/imports`)
   - Extract logic from `ImportCRFDataService.java` and `ImportCRFDataServlet.java`
   - Replace Castor with Jackson or standard XML parser for ODM

2. **Create file attachment download endpoint** in `module/datacapture/` or `module/legacy/`

3. **Rewrite SPA ImportManager** as multi-step wizard (upload→validate→preview→commit)

4. **Defer print JSPs** — these are ODM-compatible, read-only, and non-urgent

5. **After all above are done + tested** → delete the 10 legacy artifacts

## Verification

After each step:
```bash
git status --short
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl app -am
cd frontend && pnpm typecheck
```
