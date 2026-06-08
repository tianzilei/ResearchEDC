# Phase 1: Data Entry Vertical Slice

**Created:** 2026-06-08
**Status:** Planning
**Goal:** Unblock Phase 3/4 by retiring web/ routes that have SPA/module-owned replacements

## Current Blockers

### Phase 3 Blockers (DAO Implementation Deletion)
- **241 web/ files** import DAO SPI interfaces
- **Key blockers:**
  - `EventProcessor` (8 SPI interfaces) — OpenRosa mobile submission, NO SPA replacement
  - `ItemProcessor` (10 SPI interfaces) — OpenRosa item processing, NO SPA replacement
  - `IdtViewController` (11 SPI interfaces) — SDV view, retire candidate

### Phase 4 Blockers (Shared Bean/Service/Domain Deletion)
- `bean/submit/crfdata/*` (13 files) — blocked by web/ callers
- `bean/odmbeans/*` (37 remaining) — blocked by web/ callers
- Services with zero app/ callers (~42 files) — blocked by web/ callers
- Quartz jobs (XsltTransformJob, etc.) — blocked by web/ callers

## SPA Current Capabilities

### Data Entry ✅
- **SPA Route:** `/app/subjects/:subjectId/events/:eventId/event-crf/:eventCrfId`
- **API:** `POST /api/v1/data-capture/items/batch` (module-owned)
- **Features:** Section-based CRF rendering, auto-save, discrepancy notes, event completion
- **Status:** Fully functional for SPA users

### SDV (Source Data Verification) ❌
- **SPA Route:** None
- **API:** None (IdtViewController uses legacy DAOs directly)
- **Status:** No SPA replacement exists

### OpenRosa Mobile Submission ❌
- **Route:** `POST /openrosa/{studyOID}/submission`
- **Processors:** EventProcessor → ItemProcessor → StudySubjectProcessor → UserProcessor
- **Status:** Critical for mobile/external form submissions (Enketo, etc.)
- **SPA Replacement:** None — this is a separate submission path

## Retirement Strategy

### Tier 1: Retire Now (Have SPA Replacements)
These web/ routes have functional SPA replacements and can be retired:

#### Data Entry Servlets
- `DataEntryServlet` — SPA `DataEntryPage.tsx` replaces this
- `InitialDataEntryServlet` — SPA `DataEntryPage.tsx` replaces this
- `AdministrativeEditingServlet` — SPA `DataEntryPage.tsx` replaces this
- `DoubleDataEntryServlet` — SPA `DataEntryPage.tsx` replaces this
- `MarkEventCRFCompleteServlet` — SPA "Complete Event" button replaces this

#### Study Subject Management
- `ListStudySubjectsServlet` — SPA `SubjectListPage.tsx` replaces this
- `ViewStudySubjectServlet` — SPA `SubjectDetailPage.tsx` replaces this
- `AddNewSubjectServlet` — SPA `SubjectCreatePage.tsx` replaces this
- `UpdateStudySubjectServlet` — SPA replaces this

#### Event Management
- `EnterDataForStudyEventServlet` — SPA `EventDetailPage.tsx` replaces this
- `ViewStudyEventsServlet` — SPA `EventListPage.tsx` replaces this
- `CreateNewStudyEventServlet` — SPA replaces this

#### Discrepancy Notes
- `ViewDiscrepancyNoteServlet` — SPA `DiscrepancyNotes` component replaces this
- `CreateDiscrepancyNoteServlet` — SPA `DiscrepancyNotes` component replaces this
- `ListDiscNotesSubjectServlet` — SPA replaces this
- `ListDiscNotesForCRFServlet` — SPA replaces this

### Tier 2: Retire After Adding Module APIs
These need new module-owned APIs before retirement:

#### CRF Management
- `ViewCRFServlet` — Needs `/api/v1/crfs/{id}` detail endpoint
- `CreateCRFServlet` — Needs `/api/v1/crfs` POST endpoint
- `UpdateCRFServlet` — Needs `/api/v1/crfs/{id}` PUT endpoint
- `RemoveCRFServlet` — Needs `/api/v1/crfs/{id}` DELETE endpoint

#### Study Management
- `ManageStudyServlet` — Needs `/api/v1/studies/{id}` detail endpoint
- `CreateStudyServlet` — Needs `/api/v1/studies` POST endpoint
- `UpdateStudyServlet` — Needs `/api/v1/studies/{id}` PUT endpoint
- `RemoveStudyServlet` — Needs `/api/v1/studies/{id}` DELETE endpoint

#### Export/Dataset
- `CreateDatasetServlet` — Needs `/api/v1/datasets` POST endpoint
- `ExportDatasetServlet` — Needs `/api/v1/exports` POST endpoint
- `ViewDatasetsServlet` — Needs `/api/v1/datasets` GET endpoint

### Tier 3: Cannot Retire Yet (Critical Path)
These are critical for specific workflows and cannot be retired without full replacements:

#### OpenRosa Mobile Submission
- `OpenRosaSubmissionController` — Critical for mobile submissions
- `EventProcessor` — Part of OpenRosa submission chain
- `ItemProcessor` — Part of OpenRosa submission chain
- `StudySubjectProcessor` — Part of OpenRosa submission chain
- `UserProcessor` — Part of OpenRosa submission chain

**Reason:** No SPA replacement for mobile/external form submissions. Retiring this would break Enketo and other OpenRosa-compatible clients.

#### SDV (Source Data Verification)
- `IdtViewController` — SDV view and workflow management
- `SDVController` — SDV operations
- `SDVUtil` — SDV helper utilities

**Reason:** No SPA replacement for SDV. This is a specialized workflow for clinical monitors.

#### Import/Export Jobs
- `ImportSpringJob` — Scheduled import jobs
- `ExampleSpringJob` — Example job template
- `CrfBusinessLogicHelper` — CRF import helper

**Reason:** These are Quartz jobs that run in the background. They need module-owned job services before retirement.

## Execution Plan

### Phase 1A: Retire Tier 1 Data Entry Servlets (This Session)
1. Verify SPA routes work for each workflow
2. Add redirects to `LegacyRedirectFilter` for each route
3. Delete servlets, JSPs, and related code
4. Update web.xml and Page constants
5. Verify backend compile and tests

### Phase 1B: Add Module APIs for Tier 2 (Future Session)
1. Create module-owned CRUD endpoints for CRF, Study, Dataset
2. Add SPA pages for management workflows
3. Retire Tier 2 servlets after SPA verification

### Phase 1C: Handle Tier 3 Blockers (Future Session)
1. **OpenRosa:** Either create module-owned submission API or keep as compatibility layer
2. **SDV:** Create module-owned SDV API and SPA page
3. **Jobs:** Create module-owned job service

## Expected Impact

### After Phase 1A (Tier 1 Retirement)
- **web/ files deleted:** ~50-80 servlets, JSPs, and helpers
- **DAO SPI callers reduced:** ~30-50 files (data entry, study subject, event, discrepancy note servlets)
- **Phase 4 unblocked:** Some crfdata beans and services may become deletable

### After Phase 1B (Tier 2 Retirement)
- **web/ files deleted:** ~30-50 additional files
- **DAO SPI callers reduced:** ~20-30 files (CRF, study, export management)
- **Phase 4 unblocked:** More services and beans become deletable

### After Phase 1C (Tier 3 Handling)
- **Phase 3 unblocked:** DAO implementation deletion can begin
- **Phase 4 unblocked:** Remaining dead code can be deleted

## Verification Commands

After each retirement:
```bash
# Backend compile
mvn -pl app -am compile -DskipTests

# Full test suite
mvn test -pl app -am

# Modulith verification
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false

# Frontend typecheck
cd frontend && pnpm typecheck

# Frontend tests
cd frontend && pnpm test --run
```

## Risk Assessment

### Low Risk
- Tier 1 retirements have proven SPA replacements
- Redirects ensure backward compatibility
- Module-owned APIs are tested

### Medium Risk
- Tier 2 retirements need new module APIs
- Need to verify SPA coverage for all management workflows

### High Risk
- Tier 3 blockers are critical for specific workflows
- OpenRosa is used by external clients (Enketo)
- SDV is used by clinical monitors
- Need careful planning before retirement

## Decision Points

1. **OpenRosa Path:** Keep as compatibility layer OR create module-owned submission API?
2. **SDV:** Create module-owned SDV API OR keep as legacy compatibility?
3. **Jobs:** Create module-owned job service OR keep as legacy Quartz jobs?

These decisions should be made before Phase 1C execution.
