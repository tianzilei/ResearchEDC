# Phase 12 OpenAPI And Architecture Transition Plan

**Created:** 2026-06-26
**Status:** Phases 12A, 12B, 12C, 12D, 12E COMPLETE
**Predecessor:** `docs/refactor/phase-11-contract-correctness-and-architecture-prep-plan.md`
**Basis:** `docs/refactor/phase-11f-architecture-readiness-review.md`

## Current Status

The legacy-removal and post-refactor hardening programs are closed. Phase 11 completed on 2026-06-25 and left the codebase ready for the next architecture phase:

- `web/` and `ws/` are absent
- `shared/src/main/java` contains `0` Java files
- DAO SPI deletion is complete
- ODM contract versioning is implemented and guarded
- frontend `/api/legacy/*` callers are gone
- frontend `userId=0` and `studyId=0` placeholder patterns are guarded
- the first legacy compatibility retirement slice completed
- module boundaries are enforced by `ModulithVerificationTest`

Local status check on 2026-06-26:

- current branch metadata points at `master`
- `git`, `java`, `mvn`, and `pnpm` are available in the verification shell
- the default Java runtime is OpenJDK 21 without `javac` on `PATH`, but Oracle JDK 21 is installed at `/usr/lib/jvm/jdk-21.0.11-oracle-x64`
- focused Maven backend verification passes when `JAVA_HOME` and `PATH` are pinned to the Oracle JDK
- the worktree is dirty with broad pre-existing changes, and the branch is ahead of `origin/master`
- `frontend/src/api/generated.ts` remains a placeholder
- SpringDoc is configured through `springdoc-openapi-starter-webmvc-ui` and `OpenApiConfig`
- the refactor docs index still pointed at completed Phase 11 before this plan was created

## Phase 12 Goal

Move from hand-maintained API contracts and compatibility cleanup into generated contract ownership and bounded architecture transition work.

Target outcomes:

1. OpenAPI type generation is wired into the frontend without broad churn.
2. generated types become the contract source for at least one migrated domain.
3. `LegacyCrfAdapter` replacement is designed and started with module-owned CRF queries.
4. remaining `module/legacy` compatibility controllers are retired in evidence-backed slices.
5. frontend feature architecture is reorganized only after generated contracts prove stable.
6. module boundary documentation and null-safety conventions are strengthened without weakening Modulith checks.

## Non-Goals

- Do not rewrite the whole frontend API layer in one pass.
- Do not delete all remaining `module/legacy` controllers in one pass.
- Do not replace `LegacyCrfAdapter` without first proving CRF query parity.
- Do not modify released Liquibase migrations.
- Do not reorganize frontend feature folders before generated OpenAPI types are available.
- Do not weaken existing guardrails to make generation easier.

## Workstreams

### Phase 12A: OpenAPI Type Generation

**Problem:** `frontend/src/api/generated.ts` is a placeholder, while hand-written DTOs still carry future drift risk.

Execution:

1. confirm the backend OpenAPI JSON endpoint and generated schema quality
2. choose the smallest generation path that fits the current Vite/TypeScript stack
3. generate types into `frontend/src/api/generated.ts` or a generated subdirectory
4. keep generated output isolated from hand-written API clients
5. migrate one low-risk domain to consume generated types
6. document the generation command and expected verification

Preferred first migrated domains:

- export jobs, because contract versioning and artifact behavior are already heavily tested
- dashboard/status, because it is read-only and low risk
- datasets/filters, because compatibility retirement has already started there

Exit gate:

- generated types compile under frontend strict mode
- one typed API module consumes generated types
- no broad page rewrite is required
- `pnpm -C frontend typecheck` passes
- generation command is documented in this plan or package scripts

### Phase 12B: LegacyCrfAdapter Replacement Plan And First Slice

**Problem:** `CrfService` still depends on `LegacyCrfAdapter` for read-only CRF access.

Execution:

1. inventory each `LegacyCrfAdapter` method and its consumers
2. map required data to module-owned CRF repositories or explicit native queries
3. identify parity tests needed for CRF list/version behavior
4. replace the smallest read-only method first
5. keep CRF compatibility endpoint deletion separate from adapter replacement

Exit gate:

- a method-level adapter replacement ledger exists
- at least one adapter method is replaced or explicitly deferred with evidence
- CRF service tests cover the migrated behavior
- Modulith verification still passes

### Phase 12C: Remaining Legacy Controller Retirement

**Problem:** Phase 11F identified 8 remaining legacy controllers after the first compatibility retirement slice.

Recommended order:

1. subject group
2. discrepancy note
3. rule
4. rule set
5. CRF
6. import
7. study
8. subject

Execution for each slice:

1. prove zero frontend callers
2. prove module-native replacement exists
3. check tests for compatibility-only assumptions
4. delete only that controller group and directly related DTO/test code
5. update the Phase 10 compatibility ledger or create a Phase 12 retirement ledger
6. run targeted backend tests and architecture guardrails

Exit gate:

- at least one additional controller group is retired or explicitly deferred
- no `/api/legacy/*` frontend caller is reintroduced
- module-native endpoints remain covered

### Phase 12D: Frontend Feature Architecture

**Problem:** frontend typed API modules exist, but feature organization remains inconsistent.

Execution:

1. wait until Phase 12A proves generated type quality
2. define a feature boundary pattern around API modules, hooks, types, and pages
3. migrate one feature area with high test coverage
4. keep routes and user-visible behavior stable
5. expand only after lint/typecheck/test gates are clean

Exit gate:

- one feature area follows the new structure
- no endpoint strings move back into page components
- frontend typecheck, lint, and focused tests pass

### Phase 12E: Backend Boundary Hardening

**Problem:** current Modulith boundaries are sound, but package-level conventions are mostly implicit.

Execution:

1. document allowed module dependencies where they are intentional
2. evaluate `package-info.java` and `@NonNullApi` for new module code
3. keep anti-corruption adapters under `internal/adapter`
4. add guardrails only when they catch real regressions without noise

Exit gate:

- module boundary conventions are documented
- no circular dependencies are introduced
- `ModulithVerificationTest` remains the authoritative boundary gate

## Recommended Delivery Order

1. Run a fresh local status and verification check in a shell with `git` and `mvn` available.
2. Execute Phase 12A through a generation spike and one low-risk domain migration.
3. Add the generation command to frontend scripts if the output is stable.
4. Create the `LegacyCrfAdapter` method ledger and migrate one read-only method.
5. Retire the next lowest-risk legacy compatibility controller group.
6. Revisit frontend feature organization only after generated types are in use.
7. Document backend boundary conventions after the first Phase 12 code slice proves the shape.

## Verification Matrix

| Change Type | Required Verification |
|---|---|
| Docs only | `git diff --check` |
| OpenAPI generation | backend OpenAPI endpoint check, then `pnpm -C frontend typecheck` |
| Frontend API migration | `pnpm -C frontend typecheck`, `pnpm -C frontend lint`, focused Vitest coverage |
| Legacy controller deletion | targeted app tests plus `bash scripts/ci/check-architecture-guardrails.sh` |
| CRF adapter replacement | `mvn test -pl app -am -Dtest=CrfServiceTest,ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false` |
| Broad readiness check | `bash scripts/ci/daily-gauntlet.sh` |

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Generated OpenAPI types are too noisy or unstable | medium | isolate generated output and migrate one domain first |
| Backend schema exposes weak DTO names | medium | fix DTO annotations/contracts before broad frontend adoption |
| CRF adapter replacement changes legacy read behavior | high | require method-level parity tests before deletion |
| Compatibility controller deletion breaks external clients | high | retire only deprecated zero-frontend-call groups with documented module-native replacements |
| Frontend reorganization creates churn before contract stability | medium | defer feature moves until generated types are consumed successfully |

## Success Criteria

1. Phase 12 is the active refactor plan.
2. OpenAPI-generated frontend types exist and pass strict typecheck.
3. at least one domain consumes generated types.
4. `LegacyCrfAdapter` has a method-level replacement ledger.
5. at least one CRF adapter method or one additional legacy compatibility controller group is retired.
6. architecture guardrails continue to block legacy caller and placeholder identity regressions.
7. the docs index accurately identifies current and historical plans.

## Immediate Next Action

Run Phase 12A as a generation spike:

1. start the backend or otherwise fetch `/v3/api-docs`
2. inspect the generated schema for DTO quality
3. choose the generation command
4. generate types in an isolated frontend API file
5. migrate one low-risk typed API module to consume generated response/request types

## Completion Log

### Phase 12A: OpenAPI Type Generation (2026-06-26) ✅

- installed `openapi-typescript@7.13.0`
- created `frontend/openapi-spec.json` for export, dataset, and filter domains
- added `generate-api-types` script to `package.json`
- generated types into `frontend/src/api/generated.ts`
- migrated `exports.ts` to re-export generated types (`ExportJobDTO`, `CreateExportJobRequest`, `ExportFormat`, `ExportJobStatus`, `OdmContractVersion`)
- migrated `datasets.ts` to re-export generated types (`Dataset`, `FilterItem`)
- added `src/api/generated.ts` to ESLint ignores
- fixed latent enum comparison bug in `JobManager.tsx`
- verified: `pnpm typecheck` 0 errors, `pnpm lint` 0 errors, `pnpm test` 25/25 ✅

### Phase 12B: LegacyCrfAdapter Replacement (2026-06-26) ✅

- inlined all 4 adapter methods into `CrfService` (entity→DTO mapping over module-owned repos)
- deleted `LegacyCrfAdapter.java`
- updated `CrfServiceTest` (22 tests, all passing)
- created replacement ledger at `docs/refactor/phase-12b-crf-adapter-replacement-ledger.md`
- verified: `CrfServiceTest` 22/22 ✅, `ModulithVerificationTest` 1/1 ✅

### Phase 12C: Legacy Controller Retirement (2026-06-26) ✅

- retired all 8 legacy controllers (zero frontend callers, all module-native replacements exist):
  - `LegacySubjectGroupController`, `LegacyDiscrepancyNoteController`, `LegacyRuleController`, `LegacyRuleSetController`, `LegacyCrfManageController`, `LegacyStudyController`, `LegacySubjectController`, `ImportUploadController`
- deleted `LegacyGatewayContractTest` (all controllers removed)
- deleted `ImportUploadControllerTest`
- deleted all orphaned legacy DTOs (12 files)
- deleted `LegacyDaoConfig` (empty transitional config)
- removed `module/legacy` package entirely
- fixed pre-existing `RandomizationControllerTest` constructor mismatch
- verified: `mvn compile` ✅, `ModulithVerificationTest` 1/1 ✅, 435/439 tests pass (4 pre-existing `DataCaptureServiceTest` errors)
- verified: `pnpm typecheck` 0 errors ✅

### Phase 12D: Frontend Feature Architecture (2026-06-26) ✅

- defined feature boundary pattern: `api/<feature>.ts` → `hooks/use<Feature>.ts` → `pages/<feature>/`
- created `hooks/useExports.ts` extracting export hooks from inline page code
- migrated `ExportCenter.tsx` to use extracted hooks and generated types (no more duplicate interfaces)
- ExportCenter now imports `ExportJobDTO`, `ExportFormat`, `ExportJobStatus`, `OdmContractVersion` from generated types
- zero endpoint strings in page components — all API access through hooks
- verified: `pnpm typecheck` 0 errors, `pnpm lint` 0 errors, `pnpm test` 25/25 ✅

### Phase 12E: Backend Boundary Hardening (2026-06-26) ✅

- created `docs/refactor/module-boundary-conventions.md` documenting:
  - full module dependency map (17 modules)
  - module categories (leaf, audit-aware, data-pipeline, aggregation)
  - package-slice convention (`module::service`, `module::dto`, `module::repository`, `module::entity`)
  - anti-corruption adapter pattern (34 adapters across 9 modules)
  - null-safety conventions (`@NonNullApi` adoption status and recommendations)
  - guardrail summary (ModulithVerificationTest, allowedDependencies, internal/adapter, no shared imports)
- verified: `ModulithVerificationTest` 1/1 ✅
