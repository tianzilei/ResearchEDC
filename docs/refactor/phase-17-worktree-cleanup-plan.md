# Phase 17 Worktree Cleanup Plan

**Created:** 2026-06-26
**Status:** Active
**Predecessor:** `docs/refactor/phase-16-non-release-engineering-plan.md`
**Basis:** Phase 16 completion commit `44b03730f`, current dirty-worktree review, and user direction to continue workspace cleanup without RC tagging or release

## Current Status

Phase 16 is complete and committed:

- latest commit: `44b03730f` (`Phase 16: non-release engineering - randomization OpenAPI pilot, staleness workflow`)
- branch: `master`, ahead of `origin/master` by 21 commits
- no `v0.1-rc1` tag exists, and release/tag work remains deferred
- Phase 16 completed the randomization OpenAPI generated-type pilot and documented OpenAPI staleness workflow
- worktree remains broadly dirty with pre-existing source/resource/docs churn
- `docs/refactor/phase-15-rc-tagging-and-handoff-plan.md` exists as an untracked deferred-plan document and should be tracked because the refactor index references it

## Phase 17 Goal

Clean the working tree in controlled slices without reverting user work or mixing unrelated changes.

Target outcomes:

1. plan metadata is internally consistent: Phase 17 active, Phase 16 historical, Phase 15 deferred/historical and tracked.
2. dirty files are grouped into reviewable cleanup buckets.
3. line-ending-only churn is identified separately from functional changes.
4. released migration files are protected from accidental cleanup edits.
5. the first cleanup commit contains only plan/status metadata and safe documentation state.

## Non-Goals

- Do not create RC tags or release artifacts.
- Do not run destructive commands or revert broad user changes.
- Do not normalize all line endings in the same commit as functional work.
- Do not modify released Liquibase migrations.
- Do not stage broad backend/frontend/questionnaire churn without reviewing representative diffs.

## Workstreams

### Phase 17A: Plan Metadata Cleanup

**Problem:** Phase 16 is complete, but the refactor docs index still presents it as active and Phase 15 is referenced while untracked.

Execution:

1. mark Phase 16 as complete/historical in the refactor index
2. add Phase 17 as the active plan
3. keep Phase 15 as deferred/historical and track its document
4. update AGENTS status only if it is not already aligned
5. run `git diff --check` on plan files

Exit gate:

- only Phase 17 is active
- Phase 15 and Phase 16 are historical/deferred as appropriate
- `phase-15-rc-tagging-and-handoff-plan.md` is no longer an untracked referenced document

### Phase 17B: Dirty Worktree Inventory

**Problem:** `git status` shows broad dirty state across backend, frontend, questionnaire-service, shared resources, docs, and deploy/CI files.

Execution:

1. produce area-level counts for dirty files: backend, frontend, questionnaire-service, shared, docs, deploy/CI, root config
2. identify likely line-ending-only groups with `git diff --numstat` and `git diff --ignore-all-space`
3. list files that are unsafe to clean automatically, especially `shared/src/main/resources/migration/**`
4. record a cleanup order with small commit boundaries
5. leave unrelated user changes untouched unless explicitly included later

Exit gate:

- dirty files are categorized by area and risk
- migration files are marked protected
- cleanup order is explicit

### Phase 17C: Line-Ending Churn Isolation

**Problem:** Several deploy/CI files were previously classified as CRLF-to-LF churn, and many other files may be similar.

Execution:

1. detect line-ending-only files with whitespace-insensitive diffs
2. separate line-ending-only files from functional diffs
3. decide whether to normalize line endings in a dedicated cleanup commit or defer
4. avoid touching generated/vendor-like files unless they are already part of an approved cleanup slice

Exit gate:

- line-ending-only candidates are listed
- normalization decision is recorded
- no functional changes are hidden inside line-ending cleanup

### Phase 17D: Documentation Churn Review

**Problem:** Many historical refactor documents are dirty; some may be generated or historical and should not be hand-edited further.

Execution:

1. classify dirty docs as active plan docs, historical plans, generated ledgers, or broad churn
2. keep generated inventories/ledgers generated and avoid hand edits
3. commit only current active-plan corrections unless a historical doc change is explicitly needed
4. defer historical bulk cleanup to a separate plan if necessary

Exit gate:

- active docs are clean and consistent
- historical docs are not mixed into source cleanup
- generated docs are not manually rewritten

### Phase 17E: Cleanup Commit Strategy

**Problem:** Broad dirty state can easily produce noisy commits.

Execution:

1. define the first cleanup commit scope: plan metadata only
2. define later candidate scopes: line endings, deploy/CI, docs, backend, frontend, questionnaire-service, shared resources
3. require verification per scope before committing
4. keep each commit reviewable and reversible

Exit gate:

- cleanup commits have clear boundaries
- no staged file belongs to an unrelated area
- verification commands are recorded per cleanup scope

## Recommended Delivery Order

1. Fix plan metadata and track deferred Phase 15 documentation.
2. Generate area-level dirty inventory.
3. Identify line-ending-only candidates.
4. Decide first non-metadata cleanup slice.
5. Commit only scoped cleanup after verification.

## Verification Matrix

| Change Type | Required Verification |
|---|---|
| Plan/docs metadata | `git diff --check` |
| Line-ending-only cleanup | `git diff --ignore-all-space --exit-code` per file group before staging |
| Backend cleanup | `env JAVA_HOME=/usr/lib/jvm/jdk-21.0.11-oracle-x64 PATH=/usr/lib/jvm/jdk-21.0.11-oracle-x64/bin:$PATH bash scripts/ci/backend-build.sh` |
| Frontend cleanup | `pnpm -C frontend typecheck`, `pnpm -C frontend lint`, `pnpm -C frontend test --run` |
| Questionnaire cleanup | `uv run python -m pytest app/tests/ -v` from `questionnaire-service/apps/api` |
| Deploy/CI cleanup | targeted script execution or documented environment blocker |

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| accidental revert of user work | high | never use broad reset/checkout; stage explicit files only |
| noisy cleanup commit hides functional changes | high | isolate line-ending-only changes from functional changes |
| released migration files are modified | high | treat migration files as protected unless explicitly requested |
| generated docs are hand-edited | medium | classify ledgers/inventories before editing |
| verification cost delays cleanup | medium | use scope-appropriate verification, not full release gates for docs-only changes |

## Success Criteria

1. Phase 17 is the active plan.
2. Phase 15 deferred document is tracked or explicitly removed from references.
3. Phase 16 is complete/historical in the index.
4. dirty worktree has area-level classification and a cleanup order.
5. first cleanup commit is scoped to plan/status metadata only.

## Immediate Next Action

Stage and commit only plan metadata cleanup after verifying the scoped diff:

```bash
git diff --check -- docs/refactor/README.md docs/refactor/phase-15-rc-tagging-and-handoff-plan.md docs/refactor/phase-17-worktree-cleanup-plan.md
git add docs/refactor/README.md docs/refactor/phase-15-rc-tagging-and-handoff-plan.md docs/refactor/phase-17-worktree-cleanup-plan.md
git commit -m "docs: start phase 17 worktree cleanup"
```
