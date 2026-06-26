# Phase 12B: LegacyCrfAdapter Replacement Ledger

**Created:** 2026-06-26
**Status:** COMPLETE

## Summary

`LegacyCrfAdapter` was a thin entity→DTO mapping layer over module-owned CRF repositories. All 4 methods have been inlined into `CrfService` and the adapter class has been deleted.

## Method Replacement Ledger

| Method | Replaced | Notes |
|--------|----------|-------|
| `findAllCrfs()` | ✅ Inlined into `CrfService.listCrfs()` | Uses `CrfRepository.findAll()` + `CrfVersionRepository.findByCrfIdOrderByCrfVersionId()` for version count |
| `findVersionById(int)` | ✅ Inlined into `CrfService.getVersion(int)` | Uses `CrfVersionRepository.findById()` + `SectionRepository.findByCrfVersionIdOrderByOrdinal()` |
| `findItemsBySectionAndVersion(int, int)` | ✅ Inlined into `CrfService.getItemsBySection(int, int)` | Uses `ItemFormMetadataRepository.findByCrfVersionId()` + `ItemRepository.findById()` |
| `findSectionsByVersionId(int)` (private) | ✅ Inlined as private `CrfService.findSectionsByVersionId(int)` | Uses `SectionRepository.findByCrfIdOrderByCrfVersionId()` |

## Files Changed

- **Deleted:** `app/src/main/java/org/researchedc/module/crf/internal/adapter/LegacyCrfAdapter.java`
- **Modified:** `app/src/main/java/org/researchedc/module/crf/service/CrfService.java` — adapter logic inlined, constructor updated (5 repos + SCD adapter)
- **Modified:** `app/src/test/java/org/researchedc/module/crf/service/CrfServiceTest.java` — mocks updated to repos directly, tests cover inlined logic

## Pre-existing Fixes (in this changeset)

- `LegacyGatewayContractTest.java` — removed stale references to deleted `LegacyDatasetController` and `LegacyFilterController`
- `RandomizationControllerTest.java` — added missing `CurrentUserUtils` mock to match updated constructor

## Verification

- `mvn test -Dtest=CrfServiceTest` — 22/22 ✅
- `mvn test -Dtest=ModulithVerificationTest` — 1/1 ✅
- Full backend suite: 474/478 pass (4 pre-existing `DataCaptureServiceTest` null-ordinal errors, unrelated)
- `pnpm typecheck` — 0 errors ✅
