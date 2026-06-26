# Phase 13 Release Stabilization And Readiness Plan

**Created:** 2026-06-26
**Status:** Complete
**Predecessor:** `docs/refactor/phase-12-openapi-and-architecture-transition-plan.md`
**Basis:** Phase 12 completion log, current verification status, and dirty-worktree review on 2026-06-26

## Current Status

Phase 12 moved the codebase past the contract-generation and compatibility-retirement threshold:

- OpenAPI type generation is wired into the frontend.
- export and dataset/filter API types are generated from `frontend/openapi-spec.json`.
- the export frontend feature now follows the `api` -> `hooks` -> `pages` boundary pattern.
- `LegacyCrfAdapter` is deleted and CRF reads are handled by module-owned repositories.
- the `module/legacy` package is removed.
- module boundary conventions are documented in `docs/refactor/module-boundary-conventions.md`.
- focused backend verification passes when Maven is pinned to Oracle JDK 21 at `/usr/lib/jvm/jdk-21.0.11-oracle-x64`.

Current local status:

- branch: `master`, ahead of `origin/master`
- worktree: dirty with broad pre-existing changes
- Phase 12 artifacts were committed before Phase 13 execution; Phase 13 stabilization changes are tracked separately
- default `java` is OpenJDK 21 without `javac` on `PATH`; Oracle JDK 21 provides the compiler
- focused backend gate passes: `ModulithVerificationTest`, `OdmExportGeneratorTest`, `ExportArtifactWriterTest`
- frontend gate passes: typecheck, lint, and 25 Vitest tests
- full backend test baseline passes after fixing nullable `ItemDataEntity.ordinal` mapping in `DataCaptureService`

## Phase 13 Goal

Turn the completed Phase 12 architecture work into a stable release candidate baseline.

Target outcomes:

1. The full backend test suite has a known-good baseline or a documented, isolated failure ledger.
2. Java 21 compiler selection is encoded in scripts/docs so Maven verification is reproducible.
3. generated OpenAPI contracts have a repeatable refresh and review workflow.
4. the post-legacy-removal codebase has no orphaned package, DTO, route, or docs references.
5. deployment/package verification reflects the current SPA, module, and questionnaire-service layout.
6. documentation clearly separates active work from completed Phase 12 artifacts.

## Non-Goals

- Do not introduce new product features.
- Do not broaden OpenAPI generation to every endpoint before the first generated-contract workflow is stable.
- Do not rewrite the frontend folder structure beyond focused cleanup needed for release readiness.
- Do not modify released Liquibase migrations.
- Do not hide failing tests by weakening assertions or excluding coverage without a documented reason.

## Workstreams

### Phase 13A: Full Test Baseline Cleanup

**Problem:** focused gates pass, but Phase 12 recorded 4 `DataCaptureServiceTest` errors in the broader backend suite.

Execution:

1. rerun the full app test suite with Oracle JDK 21 pinned
2. isolate the 4 `DataCaptureServiceTest` failures and classify each as regression, stale test fixture, or environment dependency
3. fix real regressions in the smallest affected module
4. update stale tests only when production behavior is already covered and correct
5. rerun full backend and focused architecture/export gates

Exit gate:

- `bash scripts/ci/backend-build.sh` passes, or a short failure ledger exists with exact test names, root cause, and owner
- `ModulithVerificationTest` still passes
- no test is disabled without a linked explanation in the plan or test comment

### Phase 13B: Toolchain Reproducibility

**Problem:** Maven succeeds only when the Oracle JDK compiler is explicitly selected; the default runtime lacks `javac`.

Execution:

1. update CI/dev scripts to prefer `JAVA_HOME` when set
2. document the local Oracle JDK command prefix for backend verification
3. decide whether `scripts/ci/backend-build.sh` should fail fast when `javac` is missing
4. keep script changes portable for Linux hosts and WSL

Exit gate:

- backend verification command is copy-pasteable from docs
- scripts fail with a clear message when Java 21 `javac` is unavailable
- focused Maven gate passes from the documented command

### Phase 13C: Generated Contract Workflow

**Problem:** generated frontend types now exist, but the refresh workflow needs guardrails before broader adoption.

Execution:

1. verify `frontend/openapi-spec.json` provenance and refresh command
2. document when generated files should be committed
3. add a lightweight check that detects stale generated types if feasible
4. migrate only one more low-risk domain after the workflow is stable
5. keep handwritten API clients as the integration layer, not page-level fetch calls

Exit gate:

- `pnpm -C frontend generate-api-types` is documented and passes
- generated output compiles under strict TypeScript
- at least one review rule explains how to inspect OpenAPI/spec diffs

### Phase 13D: Legacy Removal Aftercare

**Problem:** `module/legacy` and `LegacyCrfAdapter` are removed, so references, docs, tests, and route assumptions need one cleanup pass.

Execution:

1. search for stale `module/legacy`, `LegacyCrfAdapter`, and `/api/v1/legacy` references
2. update docs that still describe the legacy gateway as active
3. remove orphaned frontend or backend compatibility notes
4. keep historical plans intact; update only current status and active docs

Exit gate:

- no active docs describe removed legacy controllers as present
- source search has no active stale references outside historical records
- refactor docs index points at Phase 13 as active and Phase 12 as historical

### Phase 13E: Release Candidate Packaging

**Problem:** the architecture changed materially; release packaging should be checked against the new module and frontend shape.

Execution:

1. run `mvn clean package -DskipTests` with Oracle JDK 21 pinned
2. run frontend build after generated type verification
3. review `deploy.sh` and CI scripts for stale legacy gateway assumptions
4. verify questionnaire-service test command still passes or record environment blockers
5. collect the minimal release checklist in this plan

Exit gate:

- backend package builds
- frontend build passes
- questionnaire-service tests pass or have an explicit environment blocker
- deploy/CI scripts do not refer to deleted modules or removed legacy controllers

## Recommended Delivery Order

1. Stabilize the full backend test baseline, starting with `DataCaptureServiceTest`.
2. Encode the Oracle JDK 21 toolchain requirement in scripts/docs.
3. Verify generated OpenAPI type refresh and document the review workflow.
4. Clean stale legacy references from active docs and scripts.
5. Run package/build checks and produce a release candidate checklist.

## Verification Matrix

| Change Type | Required Verification |
|---|---|
| Docs only | `git diff --check` |
| Backend test fixes | `mvn test -pl app -am` with Oracle JDK 21 |
| Architecture guardrails | `mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false` |
| Export contract guardrails | `mvn test -pl app -am -Dtest=OdmExportGeneratorTest,ExportArtifactWriterTest -Dsurefire.failIfNoSpecifiedTests=false` |
| Frontend contract changes | `pnpm -C frontend generate-api-types`, `pnpm -C frontend typecheck`, `pnpm -C frontend lint`, `pnpm -C frontend test --run` |
| Packaging readiness | `mvn clean package -DskipTests`, `pnpm -C frontend build` |
| Questionnaire service | `python -m pytest app/tests/ -v` from `questionnaire-service/apps/api` |

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| full backend failures are masked by focused gates | high | make full-suite cleanup the first workstream |
| local Java runtime differs from CI Java runtime | medium | fail fast on missing Java 21 `javac` and document Oracle JDK prefix |
| generated OpenAPI spec becomes stale | medium | document refresh/review workflow before broad adoption |
| stale legacy docs mislead future work | medium | update active docs while preserving historical plans |
| packaging scripts retain deleted-module assumptions | high | run package/build checks before declaring release readiness |

## Success Criteria

1. Phase 13 is the active refactor plan.
2. full backend tests pass or have a concise failure ledger with exact ownership.
3. Java 21 compiler selection is reproducible from scripts/docs.
4. generated API type refresh is documented and verified.
5. active docs no longer describe removed legacy controllers as present.
6. backend package, frontend build, and questionnaire-service checks are either passing or explicitly blocked with evidence.

## Completion Log

Completed on 2026-06-26.

- Phase 13A full backend cleanup: fixed `DataCaptureService` DTO mapping so a null persisted ordinal preserves the `ItemDataDTO` default of `1`; `DataCaptureServiceTest` passes 18/18 and `mvn test -pl app -am` passes 435/435 with Oracle JDK 21 pinned.
- Phase 13B toolchain reproducibility: `scripts/ci/backend-build.sh` now fails fast when `java`/`javac` are missing or `javac` is not Java 21, and its Modulith verification step uses `-pl app -am` so reactor-local `shared` is available after clean builds.
- Phase 13C generated contract workflow: `pnpm -C frontend generate-api-types`, `pnpm -C frontend typecheck`, `pnpm -C frontend lint`, and `pnpm -C frontend test --run` all pass.
- Phase 13D legacy removal aftercare: active `AGENTS.md` and `app/AGENTS.md` no longer describe `LegacyCrfAdapter`, `module/legacy`, `LegacyGatewayContractTest`, or `/api/v1/legacy/*` as current runtime surfaces; historical records are preserved.
- Phase 13E release packaging: `mvn clean package -DskipTests` passes with Oracle JDK 21 pinned and produces `app/target/ResearchEDC.war`; `pnpm -C frontend build` passes; questionnaire service tests pass via `uv run python -m pytest app/tests/ -v` with 40/40 tests.

## Release Candidate Checklist

- Backend full suite: PASS, 435 tests, 0 failures/errors.
- Backend CI script: PASS, JDK 21 preflight + compile + Modulith verification + module tests.
- Backend package: PASS, `ResearchEDC.war` built.
- Frontend generated types: PASS.
- Frontend typecheck/lint/test/build: PASS.
- Questionnaire service tests: PASS, 40 tests.
- Active docs: PASS, current status references updated for Phase 12/13 and legacy gateway removal.

## Immediate Next Action

Phase 13 is complete. For the next release-candidate confirmation run the consolidated backend script with Oracle JDK 21 pinned:

```bash
env JAVA_HOME=/usr/lib/jvm/jdk-21.0.11-oracle-x64 PATH=/usr/lib/jvm/jdk-21.0.11-oracle-x64/bin:$PATH bash scripts/ci/backend-build.sh
```

If failures reappear, create a short release-candidate failure ledger with exact test names before changing production code.
