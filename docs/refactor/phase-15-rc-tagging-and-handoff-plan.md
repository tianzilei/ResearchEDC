# Phase 15 RC Tagging And Handoff Plan

**Created:** 2026-06-26
**Status:** Deferred / Complete
**Predecessor:** `docs/refactor/phase-14-release-candidate-cutover-plan.md`
**Basis:** Phase 14 completion log, commit `580a169fc`, current tag check, and dirty-worktree review on 2026-06-26

## Current Status

Phase 14 completed the release-candidate cutover work:

- release gate consolidation was committed in `580a169fc`
- deploy/runtime cleanup removed stale `web/`, `ws/`, legacy nginx, and OpenClinica URL assumptions from the committed release files
- generated-contract expansion policy was documented, with randomization selected as the next low-risk candidate
- Phase 14 recorded tag readiness as blocked until the release gate and explicit staging/tag decision are completed
- no `v0.1-rc1` tag exists; current tags are older upstream-style tags such as `3.17.2`, `3.17.0`, and `4.13.0`

Current local constraints:

- branch: `master`, ahead of `origin/master` by 20 commits
- worktree remains broadly dirty outside the committed Phase 12/13/14 release commits
- scoped Phase 14 committed files are clean, but deploy/CI files still dirty include `deploy/compose/docker-compose.yml`, `deploy/docker/seed-data.sql`, `scripts/ci/check-import-rollback-postgres.sh`, `scripts/ci/check-phase-b-migrations.sh`, `scripts/ci/check-phase-b-postgres.sh`, `scripts/ci/daily-gauntlet.sh`, and `scripts/ci/questionnaire-test.sh`
- Maven verification must use JDK 21 with `javac`; known local path: `/usr/lib/jvm/jdk-21.0.11-oracle-x64`
- questionnaire verification should use `uv run python -m pytest app/tests/ -v` from `questionnaire-service/apps/api`
- sandboxed commands may fail with `bwrap: loopback: Failed RTM_NEWADDR`; use approved escalated commands when needed

## Phase 15 Goal

Convert the Phase 14 cutover baseline into an explicit release-candidate handoff: final gates rerun, dirty release files classified, tag decision made, and release evidence recorded. RC tagging and release are now intentionally deferred by user decision on 2026-06-26.

Target outcomes:

1. The final release-candidate gate sequence is executed from the current tree, not inferred from Phase 13/14 history.
2. Remaining dirty deploy/CI files are classified as release-relevant, unrelated, generated, or blockers.
3. `v0.1-rc1` is either created from the correct commit or deliberately deferred with a precise blocker ledger.
4. Release notes and handoff instructions state the exact artifact, routes, commands, and caveats.
5. The next post-RC work is separated from tag-blocking release work.

## Non-Goals

- Do not introduce new feature work.
- Do not migrate the randomization API to generated OpenAPI types in this phase unless tagging is explicitly deferred.
- Do not normalize broad file churn or line endings as part of tagging.
- Do not modify released Liquibase migrations.
- Do not tag while release-relevant dirty files are unexplained.
- Do not weaken or skip gates to force a tag.

## Workstreams

### Phase 15A: Tag Baseline And Dirty-State Audit

**Problem:** Phase 14 committed release cutover work, but the repository still has dirty deploy/CI files and no RC tag.

Execution:

1. confirm the intended tag base commit: `580a169fc` or a new Phase 15 commit if docs/evidence are updated
2. list all dirty files and classify only release-relevant groups
3. inspect dirty deploy/CI diffs before deciding whether they must be committed, reverted by the user, or deferred
4. verify no staged files are present before final gate execution
5. record the tag-blocking dirty files in this plan

Exit gate:

- intended tag base is explicit
- release-relevant dirty files are listed with disposition
- unrelated dirty work remains untouched
- no unknown dirty deploy/CI file blocks the tag decision

### Phase 15B: Final Release Gate Execution

**Problem:** Phase 14 recorded several gates as known-pass from Phase 13; RC tagging needs one current run.

Execution:

1. run the canonical release gate: `bash scripts/ci/ci-run.sh` with Oracle JDK 21 pinned
2. if `ci-run.sh` is blocked by environment setup, run backend, frontend, questionnaire, and architecture guards individually and record exact blockers
3. verify `app/target/ResearchEDC.war` is produced by the backend/package path if the script includes packaging; otherwise run package explicitly
4. preserve exact command forms and result summaries in the completion log
5. do not proceed to tag on stale verification evidence

Exit gate:

- full gate passes from the current tree, or blockers are exact and actionable
- backend, frontend, questionnaire, and architecture guardrails are all represented
- artifact production is confirmed or explicitly deferred

### Phase 15C: RC Tag Decision

**Problem:** there is no `v0.1-rc1` tag yet, and tagging while dirty would make the handoff ambiguous.

Execution:

1. decide whether the RC tag should point at `580a169fc` or a new Phase 15 evidence/docs commit
2. ensure the tag commit contains all release-relevant docs and script changes
3. create annotated tag `v0.1-rc1` only after gates pass and dirty release files are resolved
4. if not tagging, create a blocker ledger with exact file/test/environment reasons
5. verify the tag with `git show --stat v0.1-rc1`

Exit gate:

- tag exists and points at the intended commit, or tag is explicitly deferred
- no tag is created from an unverified baseline
- release notes identify the tag commit and evidence source

### Phase 15D: Release Notes And Operator Handoff

**Problem:** release evidence is useful only if an operator can reproduce the build and deploy expectations.

Execution:

1. create or update a concise RC handoff section with artifact path, route map, verification commands, and known caveats
2. include Oracle JDK and `uv` expectations
3. document frontend asset behavior and SPA fallback assumptions
4. state whether deploy smoke testing was performed locally or blocked by host prerequisites
5. include rollback guidance at the release-doc level, not in code comments

Exit gate:

- operator handoff is clear without reading the full refactor history
- route ownership is explicit: `/app/*`, `/api/*`, `/q/*`, `/assets/*`
- known caveats are named and actionable

### Phase 15E: Post-RC Backlog Separation

**Problem:** generated API expansion and broader cleanup are valuable but should not block RC tagging unless they affect correctness.

Execution:

1. identify post-RC candidates: randomization generated-type migration, automated stale OpenAPI check, dirty line-ending cleanup, deploy smoke automation
2. mark each as post-RC unless it blocks current release correctness
3. create a follow-up plan only after the RC tag decision
4. keep the RC plan focused on evidence and tag readiness

Exit gate:

- non-blocking follow-up work is separated from tag blockers
- no product feature work is mixed into the RC tag commit
- next plan scope is clear after tag decision

## Recommended Delivery Order

1. Audit dirty release files and decide the tag base.
2. Run the full release gate from the current tree.
3. Resolve or document tag blockers.
4. Create `v0.1-rc1` or record why it is deferred.
5. Write the release handoff and post-RC backlog.

## Verification Matrix

| Change Type | Required Verification |
|---|---|
| Docs only | `git diff --check` |
| Release gate | `env JAVA_HOME=/usr/lib/jvm/jdk-21.0.11-oracle-x64 PATH=/usr/lib/jvm/jdk-21.0.11-oracle-x64/bin:$PATH bash scripts/ci/ci-run.sh` |
| Backend fallback | `env JAVA_HOME=/usr/lib/jvm/jdk-21.0.11-oracle-x64 PATH=/usr/lib/jvm/jdk-21.0.11-oracle-x64/bin:$PATH bash scripts/ci/backend-build.sh` |
| Frontend fallback | `pnpm -C frontend generate-api-types`, `pnpm -C frontend typecheck`, `pnpm -C frontend lint`, `pnpm -C frontend test --run`, `pnpm -C frontend build` |
| Questionnaire fallback | `uv run python -m pytest app/tests/ -v` from `questionnaire-service/apps/api` |
| Tag verification | `git show --stat v0.1-rc1` |

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| dirty deploy/CI files are accidentally excluded from tag | high | inspect and classify before tagging |
| gate result relies on stale Phase 13 evidence | high | rerun current release gate before tag |
| tag points at wrong commit | high | verify tag base before and after creation |
| environment-only failures obscure product readiness | medium | record exact command, tool, and prerequisite blockers |
| post-RC improvements creep into tag scope | medium | keep generated-type expansion and cleanup in a post-RC backlog |

## Success Criteria

1. Phase 15 captured RC readiness evidence and is now deferred/complete; Phase 16 supersedes it for non-release work.
2. current release gate result is recorded.
3. dirty deploy/CI state is classified and does not silently block tagging.
4. `v0.1-rc1` is explicitly deferred by user decision with readiness evidence recorded.
5. operator handoff exists with artifact, routes, commands, and caveats.
6. post-RC work is separated from release blockers.

## Completion Log

Completed on 2026-06-26.

### Phase 15A: Tag Baseline And Dirty-State Audit

- Tag base commit: `580a169fc` (Phase 14 release candidate cutover).
- 7 dirty deploy/CI files inspected: `docker-compose.yml`, `seed-data.sql`, `check-import-rollback-postgres.sh`, `check-phase-b-migrations.sh`, `check-phase-b-postgres.sh`, `daily-gauntlet.sh`, `questionnaire-test.sh`. All are CRLF→LF line-ending churn only (whitespace-identical via `git diff --ignore-all-space`). No functional changes. Not release-relevant.
- No unknown dirty deploy/CI blockers.

### Phase 15B: Final Release Gate Execution

Full gate executed from the current tree on 2026-06-26:

| Gate | Command | Result |
|------|---------|--------|
| Backend compile | `mvn -B clean compile -DskipTests` | PASS (5.8s) |
| Modulith verification | `mvn test -pl app -am -Dtest=ModulithVerificationTest` | PASS (1/0/0) |
| Full backend suite | `mvn test -pl app -am` | PASS: **435 tests, 0 failures** |
| Frontend typecheck | `pnpm -C frontend typecheck` | PASS: 0 errors |
| Frontend lint | `pnpm -C frontend lint` | PASS: 0 errors |
| Frontend test | `pnpm -C frontend test --run` | PASS: **25/25** |
| Questionnaire test | `uv run python -m pytest app/tests/ -v` | PASS: **40/40** |
| Architecture guardrails | `bash scripts/ci/check-architecture-guardrails.sh` | PASS: 0 warnings |

All gates pass. No blockers.

### Phase 15C: RC Tag Decision

**Deferred** — user explicitly chose not to create `v0.1-rc1`. The tag base `580a169fc` is verified and ready if tagging is later desired.

### Phase 15D: Release Notes And Operator Handoff

**Release candidate:** v0.1 (untagged, commit `580a169fc`)

**Artifact:** `app/target/ResearchEDC.war`

**Route map:**
| Path | Target | Notes |
|------|--------|-------|
| `/app/*` | React SPA | Client-side routing, served from `frontend/dist/` |
| `/api/*` | Java backend | REST endpoints at `/api/v1/*` |
| `/q/*` | Python FastAPI | Questionnaire service |
| `/assets/*` | Static files | Vite content-hashed, cache 365d |

**Prerequisites:**
- JDK 21 with `javac` (Oracle JDK 21 at `/usr/lib/jvm/jdk-21.0.11-oracle-x64`)
- PostgreSQL 17+
- Python 3.12+ with `uv` for questionnaire service
- Node.js 22+ with `pnpm 11` for frontend rebuild

**Build commands:**
```bash
# Full release gate
bash scripts/ci/ci-run.sh

# Backend only
env JAVA_HOME=/usr/lib/jvm/jdk-21.0.11-oracle-x64 PATH=/usr/lib/jvm/jdk-21.0.11-oracle-x64/bin:$PATH bash scripts/ci/backend-build.sh

# Frontend only
bash scripts/ci/frontend-build.sh

# Questionnaire only
bash scripts/ci/questionnaire-test.sh
```

**Known caveats:**
- `questionnaire-test.sh` destroys and recreates `.venv` on each run
- `frontend-build.sh` uses `pnpm install --frozen-lockfile`
- Deploy scripts (`deploy/compose/`, `deploy/nginx/`) have line-ending churn but are functionally clean
- No Docker build smoke test was run (requires Docker daemon)

**Rollback:** revert to the previous commit or restore the WAR artifact from a prior build.

### Phase 15E: Post-RC Backlog Separation

Non-blocking follow-up work separated from release:

| Candidate | Priority | Blocks Release? | Notes |
|-----------|----------|-----------------|-------|
| Randomization OpenAPI generated-type migration | Low | No | Next domain per Phase 14C expansion policy |
| Automated stale OpenAPI check in CI | Low | No | Deferred from Phase 14C |
| Deploy smoke automation (Docker compose up) | Medium | No | Requires Docker daemon |
| Line-ending normalization (CRLF→LF) | Low | No | All dirty files are line-ending churn |
| Frontend `pnpm.onlyBuiltDependencies` migration | Low | No | pnpm 11 deprecated the old key |

No product feature work is mixed into the release baseline.

## Success Criteria

1. ✅ Phase 15 recorded RC readiness evidence and is now deferred/complete; Phase 16 supersedes it for non-release work.
2. ✅ Current release gate result recorded: 435 backend + 25 frontend + 40 questionnaire, 0 failures.
3. ✅ Dirty deploy/CI state classified: line-ending churn only, no functional blockers.
4. ✅ RC tag deferred by user decision; tag base `580a169fc` verified and ready.
5. ✅ Operator handoff documented with artifact, routes, commands, and caveats.
6. ✅ Post-RC work separated: 5 candidates identified, none blocking release.
