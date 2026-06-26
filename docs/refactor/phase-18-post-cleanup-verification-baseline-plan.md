# Phase 18 Post-Cleanup Verification Baseline Plan

**Created:** 2026-06-26
**Status:** Active
**Predecessor:** `docs/refactor/phase-17-worktree-cleanup-plan.md`
**Release posture:** no RC tag, no release artifact, no push/publish action

## Context

Phase 17 completed the repository cleanup that followed Phase 16. The working tree is clean, line endings are normalized, root `.gitattributes` now protects the repository from Windows/WSL line-ending churn, and local-only commit metadata has been normalized to `Zilei Tian <tianzilei@live.com>`.

Phase 18 is a verification baseline. It should confirm that the cleaned repository is still buildable and testable without opening a new product workstream or resuming RC tagging.

## Goal

Close Phase 17 and verify the post-cleanup repository baseline.

Target outcomes:

1. Phase 17 is marked complete and historical.
2. Phase 18 is the only active refactor plan.
3. The post-cleanup verification matrix has current results.
4. No RC tag, release artifact, or push/publish action is created by this phase.
5. Any verification failure becomes a focused follow-up item instead of reopening broad cleanup.

## Non-Goals

- Do not create `v0.1-rc1` or any other release tag.
- Do not publish or push local commits as part of Phase 18.
- Do not start Phase 19 hardening implementation.
- Do not add product backlog features.
- Do not edit released Liquibase migrations except to document a verified blocker.

## Execution Plan

### Phase 18A: Metadata Closure

1. mark Phase 17 as complete in its plan document
2. update `docs/refactor/README.md` so Phase 18 is active
3. keep Phase 15 RC tagging deferred/historical
4. add the Phase 19+ hardening/backlog reference plan
5. update root `AGENTS.md` current status

Exit gate:

- the docs index shows Phase 18 as active
- Phase 17 is complete/historical
- no document claims RC tagging is active

### Phase 18B: Repository State Check

1. confirm `git status --short --branch`
2. confirm local commits remain ahead of `origin/master`
3. confirm no untracked or unstaged source changes exist before verification
4. confirm repository-local Git identity is `Zilei Tian <tianzilei@live.com>`

Exit gate:

- worktree is clean before running verification
- publication remains a separate decision

### Phase 18C: Verification Matrix

Run the focused verification matrix:

```bash
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl app -am -Dtest=OdmExportGeneratorTest,ExportArtifactWriterTest -Dsurefire.failIfNoSpecifiedTests=false
pnpm -C frontend typecheck
pnpm -C frontend lint
pnpm -C frontend test --run
cd questionnaire-service/apps/api && uv run python -m pytest app/tests/ -v
```

Optional broader checks:

```bash
mvn clean compile -DskipTests
bash scripts/ci/daily-gauntlet.sh
```

Exit gate:

- verification results are recorded
- failures are classified as environment, test, or product issues
- no broad cleanup is reopened

### Phase 18D: Handoff Decision

After verification, choose one path:

1. keep local commits unpublished and start Phase 19 planning
2. push local commits after explicit user approval
3. resume deferred RC/tag work through the Phase 15 plan after explicit user approval

Exit gate:

- the next action is explicit
- verification and publication are not conflated

## Verification Record

To be filled during Phase 18 execution.

| Check | Result | Notes |
|---|---|---|
| `git status --short --branch` | Pending | Must be clean before verification |
| Modulith verification | Pending |  |
| ODM export tests | Pending |  |
| frontend typecheck | Pending |  |
| frontend lint | Pending |  |
| frontend tests | Pending |  |
| questionnaire pytest | Pending |  |

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| verification failure after line-ending cleanup | medium | classify and fix narrowly |
| accidental RC/tag creation | medium | keep release action out of Phase 18 |
| pushing rewritten local commits before review | high | treat publication as a separate explicit decision |
| reopening broad refactor work | medium | use focused follow-up plans only |

## Success Criteria

1. Phase 17 is complete/historical.
2. Phase 18 is active in the refactor docs index.
3. verification results are recorded.
4. no RC/tag/release action occurs.
5. the next phase is either Phase 19 hardening or an explicitly approved publication/release action.
