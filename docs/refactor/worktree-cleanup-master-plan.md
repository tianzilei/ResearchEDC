# Phase 17 Worktree Cleanup Execution Plan

**Created:** 2026-06-26
**Status:** Active companion plan for Phase 17
**Scope:** Clear the remaining dirty worktree in one Phase 17 cleanup effort, using small explicit commits instead of one giant commit.
**Baseline commit:** `aea8fe0ca` (`docs: design phase 17 large worktree cleanup`)

## Current Dirty Baseline

Measured on 2026-06-26 after Phase 17 design:

| Area | Dirty Files | Risk | Notes |
|---|---:|---|---|
| app | 458 | High | 401 Java files; likely broad line-ending/refactor churn mixed with possible functional changes |
| frontend | 119 | Medium-High | 97 `frontend/src` files plus config/type/generated files |
| questionnaire-service | 83 | Medium | Python app/tests/config/lockfile/schema files |
| shared | 146 | High | includes 117 released migration files; migrations are protected by default |
| docs | 27 | Medium | 24 `docs/refactor` files; includes generated ledgers/historical docs |
| deploy | 2 | Low | verified whitespace/line-ending-only |
| scripts | 5 | Low | verified whitespace/line-ending-only |
| root/config | 10 | Medium | root docs/config/build metadata |
| `.github` | 1 | Low-Medium | dependency/CI metadata |
| research-edc-bom | 1 | Medium | Maven dependency metadata |
| **Total** | **852** | Mixed | clear with explicit staged file lists only |

Follow-up verification on the same baseline showed:

- normal diff: `852 files changed, 86709 insertions(+), 86705 deletions(-)`
- whitespace-insensitive diff: `2 files changed, 6 insertions(+), 2 deletions(-)`

This means the cleanup is mostly line-ending/whitespace churn, but it still needs controlled commits because the dirty set includes protected migrations and a few real documentation changes.

## Phase Strategy

Use one overall phase:

- **Phase 17: Worktree Cleanup**

Do not create separate Phase 18-26 cleanup phases. Instead, execute Phase 17 through reviewable waves. Each wave should be one commit when possible, or a blocker note if the wave cannot be completed safely.

The goal is to finish with either:

1. a clean worktree, or
2. only explicitly deferred protected migration files plus a written reason.

## Global Rules

1. Do not create RC tags or release artifacts.
2. Do not use `git reset --hard`, broad `git checkout --`, or destructive cleanup.
3. Do not use `git add .`.
4. Stage explicit file lists only.
5. Keep line-ending-only cleanup separate from semantic cleanup.
6. Treat `shared/src/main/resources/migration/**` as protected until the migration decision wave.
7. Do not modify released migrations without explicit approval and full migration/Liquibase validation.
8. Generated ledgers/inventories must be regenerated or left alone; do not hand-edit generated evidence files.
9. Each wave ends with a commit, explicit deferral, or blocker ledger.
10. Stop and ask if a file contains mixed whitespace and semantic changes that do not fit the current wave.

## Phase 17 Wave Plan

### Wave 0: Plan Lock

**Goal:** Make the cleanup plan itself consistent before touching the broad dirty set.

Scope:

- `docs/refactor/worktree-cleanup-master-plan.md`
- `docs/refactor/phase-17-worktree-cleanup-plan.md`
- related active plan metadata only

Verification:

```bash
git diff --check -- docs/refactor/worktree-cleanup-master-plan.md docs/refactor/phase-17-worktree-cleanup-plan.md
git diff --ignore-all-space -- docs/refactor/worktree-cleanup-master-plan.md docs/refactor/phase-17-worktree-cleanup-plan.md
```

Exit:

- plan states that all cleanup remains inside Phase 17
- no Phase 18-26 cleanup phases remain in the roadmap

### Wave 1: Deploy And CI Line-Endings

**Goal:** Clear the lowest-risk known whitespace-only files first.

Candidate files:

- `deploy/compose/docker-compose.yml`
- `deploy/docker/seed-data.sql`
- `scripts/ci/check-import-rollback-postgres.sh`
- `scripts/ci/check-phase-b-migrations.sh`
- `scripts/ci/check-phase-b-postgres.sh`
- `scripts/ci/daily-gauntlet.sh`
- `scripts/ci/questionnaire-test.sh`

Verification:

```bash
git diff --ignore-all-space --exit-code -- <wave-1-files>
git diff --check -- <wave-1-files>
```

Commit shape:

- `chore: normalize deploy ci line endings`

Exit:

- the seven files are committed if still whitespace-only
- otherwise split out any file with semantic changes

### Wave 2: Root And Build Metadata

**Goal:** Review root config/build metadata before source trees.

Scope:

- `.github/dependabot.yml`
- `.gitignore`
- `.trivy-config.yml`
- `MODIFICATIONS.md`
- `Makefile`
- `README.md`
- `apidoc.json`
- `pom.xml`
- `research-edc-bom/pom.xml`
- root-only files not owned by frontend/app/questionnaire/shared

Verification:

```bash
git diff --check -- <wave-2-files>
git diff --ignore-all-space --exit-code -- <line-ending-only-files>
```

If Maven metadata has semantic changes, also run:

```bash
env JAVA_HOME=/usr/lib/jvm/jdk-21.0.11-oracle-x64 PATH=/usr/lib/jvm/jdk-21.0.11-oracle-x64/bin:$PATH bash scripts/ci/backend-build.sh
```

Commit shape:

- `chore: clean root build metadata`

Exit:

- root/build metadata is clean
- semantic build changes, if any, are isolated from pure whitespace cleanup

### Wave 3: Documentation

**Goal:** Clear docs churn without corrupting historical/generated evidence.

Scope:

- `docs/refactor/**`
- active Phase 17 docs
- historical plan status corrections
- generated ledgers only if regenerated or explicitly classified as whitespace-only

Verification:

```bash
git diff --check -- docs/refactor
git diff --ignore-all-space --exit-code -- <docs-whitespace-only-files>
```

Commit shape:

- `docs: clean refactor plan records`

Exit:

- active docs are internally consistent
- generated ledgers are either clean, regenerated, or deferred with a reason

### Wave 4: Shared Non-Migration Resources

**Goal:** Clean `shared/` resources without touching released migrations.

Scope:

- `shared/AGENTS.md`
- `shared/pom.xml`
- `shared/src/main/resources/datainfo.properties`
- `shared/src/main/resources/logback*.xml*`
- `shared/src/main/resources/org/researchedc/i18n/**`
- `shared/src/main/resources/properties/**`
- `shared/src/main/resources/org/researchedc/ws/client/client-config.xml`

Protected:

- `shared/src/main/resources/migration/**`

Verification:

```bash
git diff --check -- <shared-non-migration-files>
git diff --ignore-all-space --exit-code -- <shared-non-migration-whitespace-only-files>
```

If runtime resources or POMs have semantic changes, run:

```bash
env JAVA_HOME=/usr/lib/jvm/jdk-21.0.11-oracle-x64 PATH=/usr/lib/jvm/jdk-21.0.11-oracle-x64/bin:$PATH bash scripts/ci/backend-build.sh
```

Commit shape:

- `chore: clean shared resource churn`

Exit:

- shared non-migration resources are clean
- migration files remain untouched for Wave 8

### Wave 5: Frontend

**Goal:** Clear frontend churn in reviewable groups.

Suggested sub-slices:

1. generated/API contract files
2. hooks/types/API client
3. pages/components/layout/styles
4. config/test/build metadata
5. locale/i18n resources

Verification:

```bash
pnpm -C frontend generate-api-types
pnpm -C frontend typecheck
pnpm -C frontend lint
pnpm -C frontend test --run
```

Commit shape:

- `chore: clean frontend workspace churn`

Exit:

- `frontend/` is clean
- generated files are reproducible or explicitly deferred

### Wave 6: Questionnaire Service

**Goal:** Clear questionnaire-service churn with Python test coverage.

Suggested sub-slices:

1. API routers/services/repositories
2. models/schemas/scoring
3. tests
4. Alembic/config/lockfile/schema packages
5. Docker/AGENTS metadata

Verification from `questionnaire-service/apps/api`:

```bash
uv run python -m pytest app/tests/ -v
```

Commit shape:

- `chore: clean questionnaire service churn`

Exit:

- `questionnaire-service/` is clean
- lockfile/config changes are reviewed before commit

### Wave 7: Backend App

**Goal:** Clean the largest source tree after lower-risk areas are resolved.

Suggested sub-slices:

1. `package-info.java`
2. DTOs/enums/value types
3. entities/repositories with whitespace-only diffs
4. config classes after explicit review
5. services/controllers/adapters last

Verification:

```bash
env JAVA_HOME=/usr/lib/jvm/jdk-21.0.11-oracle-x64 PATH=/usr/lib/jvm/jdk-21.0.11-oracle-x64/bin:$PATH bash scripts/ci/backend-build.sh
```

Add focused tests for any module with semantic changes.

Commit shape:

- `chore: clean backend app churn`

Exit:

- `app/` is clean
- behavioral changes are either verified or split into follow-up tasks

### Wave 8: Protected Migrations And Final Sweep

**Goal:** Decide whether to normalize the 117 dirty released migration files, then finish the workspace.

Default:

- do not commit `shared/src/main/resources/migration/**`
- document them as protected if the user does not approve normalization

Allowed only with explicit approval:

- line-ending-only normalization of released migration files

Required verification if migrations are committed:

```bash
git diff --ignore-all-space --exit-code -- shared/src/main/resources/migration
git diff --check -- shared/src/main/resources/migration
```

Then run Liquibase/database validation on a disposable database if available.

Final sweep:

```bash
git status --short --branch
git diff --check
```

Commit shape if approved:

- `chore: normalize migration line endings`

Exit:

- worktree is clean, or only protected migrations remain with an explicit deferral note

## Execution Protocol Per Wave

1. List candidate files with `git status --short -- <scope>`.
2. Inspect representative diffs.
3. Prove whitespace-only candidates with `git diff --ignore-all-space --exit-code -- <files>`.
4. Run the wave-specific verification.
5. Stage explicit files only.
6. Run `git diff --cached --check`.
7. Run `git diff --cached --name-status`.
8. Commit with a wave-specific message.
9. Re-check `git status --short --branch`.

## Stop Conditions

Stop the wave and ask before proceeding if:

- a protected migration file needs semantic edits
- a file combines line-ending-only churn with unclear functional changes
- required verification fails
- cleanup requires reverting changes not made in the current cleanup wave
- generated files cannot be reproduced
- staging would require broad `git add .`

## Immediate Next Action

Finish Wave 0 by committing this execution plan and the Phase 17 pointer update. Then execute Wave 1 deploy/CI line-ending-only cleanup.
