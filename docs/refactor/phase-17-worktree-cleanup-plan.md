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

## Large-Scale Cleanup Design

Phase 17 cleanup must proceed as an audit-and-slice operation, not as a broad reset or bulk formatter run.

### Current Dirty Inventory Snapshot

Measured on 2026-06-26 after `c6f4b08ce`:

| Area | Dirty Files | Cleanup Risk | Notes |
|---|---:|---|---|
| app | 458 | High | 401 Java files; likely broad line-ending/refactor churn mixed with possible functional changes |
| frontend | 119 | Medium-High | 97 `frontend/src` files plus config; may include generated/type changes and line endings |
| questionnaire-service | 83 | Medium | 66 app files plus Python metadata/config; verify with pytest for any functional cleanup |
| shared | 146 | High | includes 117 released migration files; migrations are protected |
| docs | 27 | Medium | 24 `docs/refactor` files; includes generated ledgers and historical plans |
| deploy | 2 | Low | previously verified as whitespace/line-ending-only |
| scripts | 5 | Low | previously verified as whitespace/line-ending-only |
| root/config | 10 | Medium | project metadata and root docs/config |
| `.github` | 1 | Low-Medium | CI/dependency metadata |
| research-edc-bom | 1 | Medium | Maven BOM; requires dependency review |

### Protection Rules

1. Never use `git reset --hard`, broad checkout, or destructive cleanup.
2. Treat `shared/src/main/resources/migration/**` as protected. Do not normalize or edit released migrations unless explicitly requested.
3. Separate whitespace/line-ending-only cleanup from functional cleanup.
4. Stage explicit file lists only; never `git add .` for this phase.
5. Each cleanup wave must have a rollback boundary: one commit, one area, one reason.
6. Generated ledgers/inventories are not manually edited unless regeneration is part of the same slice.

### Cleanup Waves

| Wave | Scope | Action | Verification | Commit Type |
|---|---|---|---|---|
| 0 | Plan metadata | Done in `c6f4b08ce` | `git diff --check` | committed |
| 1 | deploy/CI line-ending-only files | Commit 7 known whitespace-only files or defer if user wants zero line-ending commits | `git diff --ignore-all-space --exit-code -- <files>` before staging | cleanup |
| 2 | root/config metadata | Inspect `.gitignore`, `.trivy-config.yml`, `Makefile`, `README.md`, `MODIFICATIONS.md`, `apidoc.json`, BOM/pom files | targeted diff review; backend/frontend checks if build metadata changes | review/cleanup |
| 3 | docs/refactor historical churn | Classify historical plans vs generated ledgers; commit only deliberate docs updates | `git diff --check`; no hand edits to generated ledgers | docs cleanup |
| 4 | shared non-migration resources | Inspect i18n/properties/XSD/XSLT/logback files; avoid migrations | backend compile/package if resources affect runtime | resource cleanup |
| 5 | frontend | Split generated/API/types/config/pages; inspect `openapi-spec.json` and `generated.ts` separately | `generate-api-types`, `typecheck`, `lint`, `test --run` | frontend cleanup |
| 6 | questionnaire-service | Split app code, tests, schemas, lockfile/config | `uv run python -m pytest app/tests/ -v` | service cleanup |
| 7 | app Java | Split by module/package; start with DTO/package-info/format-only groups, then services/controllers | backend build script with Oracle JDK 21 | backend cleanup |
| 8 | protected migrations | Default: defer. Only handle if user explicitly asks for migration line-ending normalization | full migration/Liquibase validation required | explicit-only |

### Wave 1 Candidate Files

These 7 files were verified with `git diff --ignore-all-space --exit-code` as whitespace-only:

- `deploy/compose/docker-compose.yml`
- `deploy/docker/seed-data.sql`
- `scripts/ci/check-import-rollback-postgres.sh`
- `scripts/ci/check-phase-b-migrations.sh`
- `scripts/ci/check-phase-b-postgres.sh`
- `scripts/ci/daily-gauntlet.sh`
- `scripts/ci/questionnaire-test.sh`

Decision needed before execution: commit these line-ending-only changes as a dedicated cleanup commit, or defer all line-ending normalization until a repository-wide `.gitattributes` decision.

### Large Cleanup Execution Protocol

For each wave:

1. list candidate files with `git status --short -- <scope>`
2. inspect representative diffs and identify whitespace-only files
3. run the scope-specific verification from the matrix
4. stage explicit files only
5. run `git diff --cached --check` and `git diff --cached --name-status`
6. commit with a message that names the scope and cleanup type
7. re-check global `git status --short --branch`

### Stop Conditions

Stop and ask before proceeding if:

- a protected migration file needs to be modified
- a diff contains functional code changes that cannot be attributed to the current cleanup wave
- verification fails for a scope
- a cleanup would require reverting user changes
- line-ending-only churn is mixed with semantic changes in the same file

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

Decide whether to execute Wave 1 as a dedicated deploy/CI line-ending-only cleanup commit:

```bash
git diff --ignore-all-space --exit-code -- deploy/compose/docker-compose.yml deploy/docker/seed-data.sql scripts/ci/check-import-rollback-postgres.sh scripts/ci/check-phase-b-migrations.sh scripts/ci/check-phase-b-postgres.sh scripts/ci/daily-gauntlet.sh scripts/ci/questionnaire-test.sh
git diff --check -- deploy/compose/docker-compose.yml deploy/docker/seed-data.sql scripts/ci/check-import-rollback-postgres.sh scripts/ci/check-phase-b-migrations.sh scripts/ci/check-phase-b-postgres.sh scripts/ci/daily-gauntlet.sh scripts/ci/questionnaire-test.sh
```
