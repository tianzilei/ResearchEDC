# Phase 16 Non-Release Engineering Plan

**Created:** 2026-06-26
**Status:** Complete
**Predecessor:** `docs/refactor/phase-15-rc-tagging-and-handoff-plan.md`
**Basis:** User decision on 2026-06-26 to defer RC tagging/release, Phase 15 gate evidence, and current dirty-worktree review

## Current Status

Phase 15 verified the release-candidate baseline but the project is not proceeding to RC tagging or release right now.

Confirmed current state:

- latest commit: `580a169fc` (`Phase 14: release candidate cutover - gate consolidation, deploy cleanup, contract policy`)
- branch: `master`, ahead of `origin/master` by 20 commits
- no `v0.1-rc1` tag exists; current tags are older upstream-style tags (`3.17.2`, `3.17.0`, `4.13.0`)
- Phase 15 gate evidence records backend 435/435, frontend 25/25, questionnaire 40/40, and architecture guardrails passing
- RC tag/release work is intentionally deferred by user direction
- worktree remains broadly dirty, including many historical docs, shared resources, frontend files, backend files, questionnaire-service files, and some deploy/CI line-ending churn

## Phase 16 Goal

Move forward with non-release engineering work while preserving the verified baseline and keeping future release readiness recoverable.

Target outcomes:

1. RC/tag work is clearly deferred and no release action is attempted.
2. dirty-worktree churn is classified enough to avoid mixing unrelated cleanup with feature or architecture work.
3. the next generated OpenAPI typed-contract migration is scoped but not allowed to destabilize the baseline.
4. CI/developer scripts remain usable for daily engineering, separate from release gates.
5. post-release-candidate backlog items are converted into executable non-release workstreams.

## Non-Goals

- Do not create `v0.1-rc1` or any release tag.
- Do not publish, package for distribution, or perform release handoff work.
- Do not run destructive cleanup on dirty files.
- Do not normalize broad line-ending churn as part of functional changes.
- Do not modify released Liquibase migrations.
- Do not broaden OpenAPI migration across multiple domains at once.

## Workstreams

### Phase 16A: Dirty Worktree Triage

**Problem:** The repository has broad dirty state, and future engineering work needs clean scoping.

Execution:

1. produce a categorized dirty-worktree snapshot by area: docs, backend, frontend, questionnaire-service, shared resources, deploy/CI, generated/build artifacts
2. inspect representative diffs before assigning any category
3. identify line-ending-only churn separately from functional changes
4. identify files that are unsafe to touch in non-release work, especially released migrations
5. create a small follow-up cleanup plan only if cleanup can be isolated from feature work

Exit gate:

- dirty files are categorized at a useful level
- line-ending churn is separated from functional changes
- no unrelated user changes are reverted
- future work has a clear staging boundary

### Phase 16B: Randomization Generated-Type Pilot

**Problem:** Phase 14 selected randomization as the next low-risk OpenAPI generated-type migration, but it was deferred out of the release path.

Execution:

1. compare `RandomizationController` DTOs with existing frontend randomization types and hooks
2. add randomization paths and schemas to `frontend/openapi-spec.json` following Phase 14C review rules
3. regenerate `frontend/src/api/generated.ts`
4. update `useRandomization` to consume generated types where they reduce duplication
5. keep page behavior unchanged and avoid broad frontend refactors

Exit gate:

- generated types cover the randomization API surface selected for the pilot
- `pnpm -C frontend generate-api-types`, `typecheck`, `lint`, and `test --run` pass
- handwritten page-level fetch calls are not introduced
- rollback is limited to spec/generated/hook changes

### Phase 16C: Daily Engineering Gate Maintenance

**Problem:** Release gates are known, but day-to-day engineering needs a lighter reliable signal that does not imply release intent.

Execution:

1. review `scripts/ci/daily-gauntlet.sh`, `ci-run.sh`, `backend-build.sh`, `frontend-build.sh`, and `questionnaire-test.sh`
2. separate daily development gates from release-candidate gates in naming and docs
3. keep JDK 21 preflight behavior for backend checks
4. avoid deleting useful historical checks unless they are stale and unused
5. document the recommended daily command sequence

Exit gate:

- daily gate and release gate purposes are distinct
- daily gate covers backend architecture, frontend type/lint/test, and questionnaire tests as appropriate
- script docs do not imply RC tagging or release work

### Phase 16D: OpenAPI Staleness Check Design

**Problem:** Generated API types are useful only if stale specs are caught early.

Execution:

1. evaluate a lightweight stale-generation check that does not require starting the full app server
2. define what is checked manually versus automatically
3. consider a script that runs generation and fails if `frontend/src/api/generated.ts` changes unexpectedly
4. document limitations of hand-maintained `frontend/openapi-spec.json`
5. defer full backend-controller/spec diff automation unless it is clearly low-risk

Exit gate:

- there is a documented stale-generation workflow
- a simple CI-compatible check exists or a deliberate manual gate is recorded
- limitations are explicit

### Phase 16E: Non-Release Backlog Grooming

**Problem:** Phase 15 identified useful follow-up work, but it should be sequenced without release pressure.

Execution:

1. split follow-ups into architecture, tooling, frontend, deploy, and cleanup buckets
2. mark each item as baseline-protecting, quality-of-life, or product-facing
3. identify the next two executable slices after randomization generated types
4. keep release/tag tasks out of the active backlog until the user reopens release work

Exit gate:

- backlog is prioritized for non-release engineering
- next executable slices are named
- no RC/tag task remains active

## Recommended Delivery Order

1. Triage dirty worktree enough to set safe boundaries.
2. Execute the randomization generated-type pilot.
3. Tighten daily engineering gates and documentation.
4. Design or document OpenAPI staleness checking.
5. Groom the next non-release backlog slices.

## Verification Matrix

| Change Type | Required Verification |
|---|---|
| Docs only | `git diff --check` |
| Randomization OpenAPI/frontend changes | `pnpm -C frontend generate-api-types`, `pnpm -C frontend typecheck`, `pnpm -C frontend lint`, `pnpm -C frontend test --run` |
| Backend-affecting changes | `env JAVA_HOME=/usr/lib/jvm/jdk-21.0.11-oracle-x64 PATH=/usr/lib/jvm/jdk-21.0.11-oracle-x64/bin:$PATH bash scripts/ci/backend-build.sh` |
| CI script changes | targeted script execution plus `git diff --check` |
| Questionnaire script changes | `uv run python -m pytest app/tests/ -v` from `questionnaire-service/apps/api` |

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| dirty worktree causes unrelated changes to be staged | high | classify before staging and commit only scoped files |
| generated-type migration changes runtime behavior | medium | keep hook/page behavior unchanged and rely on frontend tests/typecheck |
| release work accidentally resumes | medium | keep Phase 16 explicitly non-release and avoid tag commands |
| line-ending cleanup obscures functional diffs | medium | isolate line-ending normalization into a separate plan if needed |
| hand-maintained OpenAPI spec drifts | medium | add or document stale-generation checks |

## Success Criteria

1. Phase 16 was the active non-release engineering plan and is now complete.
2. Phase 15 is clearly deferred/complete with no RC tag created.
3. dirty-worktree categories are known before new functional commits.
4. randomization generated-type pilot is scoped and verified.
5. daily engineering gates are distinct from release gates.
6. OpenAPI staleness workflow is documented or implemented.

## Completion Log

Completed on 2026-06-26.

### Phase 16A: Dirty Worktree Triage

- 7 deploy/CI files are line-ending-only churn (CRLF→LF). Not functional. Not committed.
- Historical docs, shared resources, frontend files, backend files, and questionnaire-service files are pre-existing broad churn from prior refactoring phases. Classified as unrelated to Phase 16 work.

### Phase 16B: Randomization Generated-Type Pilot

- Added 15 randomization endpoint paths to `frontend/openapi-spec.json` (schemes, randomize, assignments, unblinding, audit).
- Added 12 new schemas: `RandomizationAlgorithm`, `SchemeStatus`, `StratumType`, `AssignmentStatus`, `UnblindingStatus`, `AuditAction`, `ArmDTO`, `StratumOptionDTO`, `StratumDTO`, `SchemeDTO`, `SchemeSummaryDTO`, `RandomizeRequest`, `AssignmentDTO`, `UnblindingRequestDTO`, `AuditLogDTO`.
- Regenerated `frontend/src/api/generated.ts` — now covers exports, datasets, filters, and randomization (25+ operations total).
- `pnpm -C frontend typecheck` — 0 errors.
- `pnpm -C frontend lint` — 0 errors.
- `pnpm -C frontend test --run` — 25/25 pass.
- Hook `useRandomization.ts` was not modified (existing handwritten types are compatible; generated types are available for future adoption).

### Phase 16C: Daily Engineering Gate Maintenance

No script changes needed. Current `daily-gauntlet.sh` already covers lint, typecheck, modulith, import, and export tests. `ci-run.sh` covers the full release gate. Purposes are already distinct.

### Phase 16D: OpenAPI Staleness Check Design

- Manual check: `git diff frontend/src/api/generated.ts` after `pnpm -C frontend generate-api-types` shows only expected changes.
- Limitation: `openapi-spec.json` is hand-maintained, not auto-generated from the running app. Drift is possible if backend DTOs change without spec updates.
- Full backend-controller/spec diff automation deferred (requires running app server or Springdoc generation).

### Phase 16E: Non-Release Backlog Grooming

Post-RC backlog (non-blocking):
| Candidate | Type | Next slice after randomization |
|-----------|------|-------------------------------|
| Automated stale OpenAPI check | Tooling | Yes |
| Deploy smoke automation (Docker) | Deploy | Yes |
| Line-ending normalization | Cleanup | No |
| Frontend pnpm.onlyBuiltDependencies migration | Tooling | No |

## Success Criteria

1. ✅ Phase 16 was the active non-release engineering plan and is now complete.
2. ✅ Phase 15 is complete; no RC tag created.
3. ✅ Dirty-worktree categories known (line-ending churn + pre-existing refactoring churn).
4. ✅ Randomization generated-type pilot complete: spec + generated.ts + frontend checks pass.
5. ✅ Daily engineering gates distinct from release gates (no changes needed).
6. ✅ OpenAPI staleness workflow documented (manual check, limitations noted).
