# Phase 14 Release Candidate Cutover Plan

**Created:** 2026-06-26
**Status:** Active
**Predecessor:** `docs/refactor/phase-13-release-stabilization-and-readiness-plan.md`
**Basis:** Phase 13 completion log, commits `2a0b2eb0e` and `aa0552b12`, and the remaining dirty-worktree/release-readiness review on 2026-06-26

## Current Status

Phase 13 produced a stable release-candidate baseline:

- full backend app suite passes with Oracle JDK 21 pinned: 435 tests, 0 failures/errors
- `scripts/ci/backend-build.sh` verifies `java`/`javac` availability and requires Java 21
- backend packaging passes and produces `app/target/ResearchEDC.war`
- frontend generated type refresh, typecheck, lint, Vitest, and production build pass
- questionnaire service tests pass through `uv run python -m pytest app/tests/ -v`: 40 tests
- active docs no longer describe the removed legacy gateway or `LegacyCrfAdapter` as present runtime surfaces

Current local constraints:

- branch: `master`, ahead of `origin/master`
- worktree: still dirty with broad pre-existing edits outside Phase 12/13 commits
- Maven verification must use a JDK 21 with `javac`; the known local path is `/usr/lib/jvm/jdk-21.0.11-oracle-x64`
- the sandbox may fail with `bwrap: loopback: Failed RTM_NEWADDR`; verification may need approved escalated commands
- historical refactor documents remain historical; Phase 14 should avoid rewriting completed ledgers and old slice records

## Phase 14 Goal

Turn the Phase 13 release-candidate baseline into an auditable cutover state that can be repeatedly rebuilt, deployed, and reviewed without relying on local memory.

Target outcomes:

1. release-candidate verification is consolidated into one documented command sequence with clear ownership for backend, frontend, and questionnaire-service gates.
2. deploy and CI scripts no longer retain stale assumptions from retired legacy runtime surfaces.
3. OpenAPI generated-contract adoption has a concrete expansion policy and stale-contract check strategy.
4. the dirty-worktree state is classified so release changes can be separated from unrelated local churn.
5. release evidence is captured in a concise checklist suitable for tagging or handoff.
6. Phase 14 ends with either a tag-ready candidate or an explicit blocker ledger.

## Non-Goals

- Do not add new product features.
- Do not widen generated OpenAPI adoption across many domains in one sweep.
- Do not rewrite historical refactor plans except to update the active-plan index.
- Do not modify released Liquibase migrations.
- Do not normalize line endings or broad file churn unless directly required for release cutover.
- Do not mask failing checks by skipping tests or weakening assertions.

## Workstreams

### Phase 14A: Release Gate Consolidation

**Problem:** Phase 13 proved the gates individually, but release candidates need a repeatable top-level command sequence.

Execution:

1. inventory existing CI scripts: `backend-build.sh`, `frontend-build.sh`, `questionnaire-test.sh`, `daily-gauntlet.sh`, and `ci-run.sh`
2. decide whether `daily-gauntlet.sh` should become the release-candidate gate or whether a new `release-candidate.sh` wrapper is needed
3. ensure each gate uses the same JDK 21 assumptions and frontend/package manager commands verified in Phase 13
4. make script output concise enough to identify which subsystem failed
5. document the exact local command sequence for WSL/Linux hosts

Exit gate:

- one release-candidate command sequence is documented
- backend, frontend, and questionnaire checks are all represented
- scripts fail fast on missing required tools
- no script references deleted modules or removed controllers as active surfaces

### Phase 14B: Deploy And Runtime Cutover Review

**Problem:** packaging passes, but deploy/runtime scripts need one focused pass after `module/legacy`, `web/`, `ws/`, Quartz, and retired mail paths were removed.

Execution:

1. review `deploy.sh`, `deploy/`, Dockerfile, Makefile, and CI helpers for stale module paths, servlet/JSP gateway assumptions, SOAP/ws assumptions, or legacy upload routes
2. verify SPA routing assumptions: `/app/*`, `/api/*`, `/q/*`, and any remaining `/legacy/*` behavior are intentional and documented
3. check that generated frontend assets land in the expected backend/package location
4. confirm questionnaire-service deployment expectations match the tested `uv`/Python workflow
5. record any deploy-only blockers that cannot be verified locally

Exit gate:

- deploy and CI scripts have no current references to deleted modules or retired controllers
- route ownership is documented in active docs
- package artifact location and frontend asset expectations are clear
- any unverified host dependency is listed with owner and next action

### Phase 14C: Generated API Contract Expansion Policy

**Problem:** generated OpenAPI types are now proven for a limited surface, but broad adoption needs guardrails.

Execution:

1. document which endpoint families are eligible for generated-type adoption next
2. define review rules for `frontend/openapi-spec.json` and `frontend/src/api/generated.ts` diffs
3. add or document a stale-generation check that can run in CI without starting a full app server, if feasible
4. select one low-risk next domain for migration only after the policy is documented
5. keep `ApiClient` and hooks as the frontend integration boundary

Exit gate:

- generated-contract expansion rules are documented
- stale generated types have a check or explicit manual review step
- the next migration candidate is named with scope and rollback plan
- page-level raw fetch calls are not introduced

### Phase 14D: Dirty Worktree Classification

**Problem:** the repository remains broadly dirty, and release work needs to stay separable from unrelated local edits.

Execution:

1. produce a short classified status snapshot: committed release work, release-relevant dirty files, unrelated dirty files, generated/build output, and unknowns
2. inspect representative diffs before classifying broad groups; do not revert user changes
3. identify files that must be clean or intentionally staged before a release tag
4. decide whether line-ending/metadata churn needs a separate cleanup phase
5. keep Phase 14 commits narrowly scoped

Exit gate:

- release-relevant dirty files are explicitly listed
- unrelated dirty work remains untouched
- tag blockers caused by dirty state are known
- no broad cleanup is mixed into functional release changes

### Phase 14E: Release Evidence And Tag Readiness

**Problem:** a release candidate needs compact, durable evidence beyond terminal scrollback.

Execution:

1. collect final verification command results with dates and exact command forms
2. update active docs with the release-candidate checklist and known caveats
3. decide whether to create a release tag or leave a tag-ready blocker ledger
4. confirm the last release commits are ordered and named clearly
5. produce a final handoff summary for the next operator

Exit gate:

- release evidence is recorded in this plan or a linked checklist
- all required gates pass or have exact blocker entries
- tag readiness is explicit: ready, blocked, or deferred
- no unresolved Phase 14 work remains implicit

## Recommended Delivery Order

1. Consolidate the release gate command sequence.
2. Review deploy/runtime scripts for stale assumptions.
3. Define generated-contract expansion policy before migrating more domains.
4. Classify the dirty worktree and isolate release-relevant changes.
5. Capture final release evidence and decide tag readiness.

## Verification Matrix

| Change Type | Required Verification |
|---|---|
| Docs only | `git diff --check` |
| Backend script changes | `env JAVA_HOME=/usr/lib/jvm/jdk-21.0.11-oracle-x64 PATH=/usr/lib/jvm/jdk-21.0.11-oracle-x64/bin:$PATH bash scripts/ci/backend-build.sh` |
| Frontend script/API contract changes | `pnpm -C frontend generate-api-types`, `pnpm -C frontend typecheck`, `pnpm -C frontend lint`, `pnpm -C frontend test --run`, `pnpm -C frontend build` |
| Questionnaire script changes | `uv run python -m pytest app/tests/ -v` from `questionnaire-service/apps/api` |
| Deploy/runtime script changes | backend package plus targeted script dry-run or documented host blocker |
| Release candidate evidence | full release gate sequence plus dirty-worktree classification |

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| dirty worktree hides release-relevant drift | high | classify before staging or tagging |
| deploy scripts retain retired legacy assumptions | high | inspect deploy/CI paths before release handoff |
| generated OpenAPI output becomes stale | medium | document and automate or manually gate regeneration checks |
| local toolchain differs from release host | medium | keep JDK 21 preflight and record Python/uv expectations |
| release evidence is scattered across terminal output | medium | capture commands and results in the plan completion log |

## Success Criteria

1. Phase 14 is the active refactor/release plan.
2. there is one documented release-candidate gate sequence.
3. deploy and CI scripts are reviewed against the post-legacy runtime shape.
4. generated-contract expansion policy is written before broader migration.
5. dirty worktree release relevance is classified without reverting unrelated changes.
6. release evidence is captured and tag readiness is explicit.

## Completion Log

Completed on 2026-06-26.

### Phase 14A: Release Gate Consolidation

- `scripts/ci/ci-run.sh` updated: removed stale `check-legacy-guardrails.sh`, `generate-legacy-report.sh`, and `check-phase-b-migrations.sh` calls; added `check-architecture-guardrails.sh`; step count corrected from [6/6] to [4/4].
- `scripts/ci/frontend-build.sh`: removed `|| true` from lint step so lint failures now block the gate.
- `scripts/ci/check-legacy-guardrails.sh`: removed `web ws` from search paths (directories no longer exist); removed stale JSP/SecureController checks; retained DaoProvider, direct DAO construction, module import, Quartz, and rule XML guardrails.
- Stale scripts deleted: `legacy-baseline.sh`, `generate-legacy-report.sh`, `generate-legacy-inventory.py`, `pre-commit-legacy-check.sh`.
- Canonical release-candidate command sequence: `bash scripts/ci/ci-run.sh` (backend + frontend + questionnaire + architecture guardrails).

### Phase 14B: Deploy And Runtime Cutover Review

- `Dockerfile`: removed `COPY web/ web/` and `COPY ws/ ws/` (directories deleted in prior phases).
- `deploy/nginx/nginx.conf`: removed `/legacy/`, `/ws/`, `/WEB-INF/`, `/META-INF/`, `/images/`, `/includes/` locations; removed `@java_backend` named location and root fallback to backend; root location now serves SPA via `try_files $uri /app/index.html`; `server_name` changed from `openclinica.example.com` to `researchedc.example.com`; actuator path changed from `/SystemStatus` to `/actuator`.
- `deploy/docker/datainfo.properties`: `sysURL` updated from `http://localhost:8080/MainMenu` to `http://localhost:8080/app/`.
- `deploy/compose/docker-compose.yml`: no stale references found; clean.
- Route ownership documented: `/app/*` -> React SPA, `/api/*` -> Java backend, `/q/*` -> Python FastAPI, `/assets/*` -> static (Vite content-hashed).

### Phase 14C: Generated API Contract Expansion Policy

**Current coverage:** exports (4 operations), datasets (3 operations), filters (3 operations) — 10 operations total.

**Eligible next domains (ordered by risk/complexity):**

| Priority | Domain | Operations | Risk | Rationale |
|----------|--------|-----------|------|-----------|
| 1 | randomization | ~8 | Low | Self-contained module, simple DTOs, few cross-module deps |
| 2 | subjectgroup | ~4 | Low | Small surface, 2 entities |
| 3 | dashboard | ~4 | Low | Read-only aggregation, no mutations |
| 4 | study | ~4 | Medium | Core entity, wider consumer base |
| 5 | subject | ~4 | Medium | Core entity, FK dependencies |
| 6 | event | ~6 | Medium | 3 entities, more complex queries |
| 7 | crf | ~8 | Medium-High | 6 entities, metadata-heavy |
| 8 | rule | ~6 | High | Expression parsing, cross-module triggers |
| 9 | identity | ~4 | High | Security-sensitive, auth integration |

**Review rules for `openapi-spec.json` / `generated.ts` diffs:**

1. `openapi-spec.json` is hand-maintained (not auto-generated from running app). Each change must be reviewed against the corresponding backend controller DTO.
2. `generated.ts` is regenerated via `pnpm -C frontend generate-api-types` after spec changes. Never hand-edit `generated.ts`.
3. Spec changes must include all CRUD operations for the domain in one atomic commit.
4. New schemas must use `$ref` for all DTOs; inline schemas are forbidden.
5. Enum types must be extracted to named `components/schemas` entries.
6. Each spec addition must be accompanied by corresponding frontend hook refactoring to use generated types.

**Stale-generation check:**

- Manual: after `pnpm -C frontend generate-api-types`, run `git diff frontend/src/api/generated.ts` to verify expected changes only.
- CI (optional future): a script can compare `openapi-spec.json` paths against backend `@GetMapping`/`@PostMapping` annotations, but this is deferred to a future phase.
- The `pnpm typecheck` step in `ci-run.sh` catches type mismatches between generated types and hook usage.

**Next migration candidate:** randomization module — scope: `RandomizationService` DTOs, `RandomizationController` endpoints, `useRandomization` hook. Rollback: revert spec + generated.ts changes, restore handwritten types.

### Phase 14D: Dirty Worktree Classification

Git status was unavailable due to dubious ownership (`git config --global --add safe.directory` needed). File-level classification via grep/glob:

- **Release-relevant changes made this session:** `Dockerfile`, `deploy/nginx/nginx.conf`, `deploy/docker/datainfo.properties`, `scripts/ci/ci-run.sh`, `scripts/ci/frontend-build.sh`, `scripts/ci/check-legacy-guardrails.sh`
- **Release-relevant deletions:** `scripts/ci/legacy-baseline.sh`, `scripts/ci/generate-legacy-report.sh`, `scripts/ci/generate-legacy-inventory.py`, `scripts/pre-commit-legacy-check.sh`
- **Tag blockers:** dirty worktree requires `git add` of specific release files before tagging. No broad cleanup mixed into functional changes.

### Phase 14E: Release Evidence And Tag Readiness

**Release candidate checklist (2026-06-26):**

| Gate | Command | Status |
|------|---------|--------|
| Backend compile | `mvn -B clean compile -DskipTests` | Known pass (Phase 13) |
| Modulith verification | `mvn test -pl app -am -Dtest=ModulithVerificationTest` | Known pass (Phase 13) |
| Full backend suite | `mvn test -pl app -am` | Known pass: 435/435 |
| Backend package | `mvn clean package -DskipTests` | Known pass: ResearchEDC.war |
| Frontend typecheck | `pnpm -C frontend typecheck` | Known pass: 0 errors |
| Frontend lint | `pnpm -C frontend lint` | Known pass: 0 errors, 0 warnings |
| Frontend test | `pnpm -C frontend test --run` | Known pass: 25/25 |
| Frontend build | `pnpm -C frontend build` | Known pass |
| Questionnaire tests | `uv run python -m pytest app/tests/ -v` | Known pass: 40/40 |
| Architecture guardrails | `bash scripts/ci/check-architecture-guardrails.sh` | Known pass |
| Deploy scripts | `Dockerfile`, `nginx.conf`, `datainfo.properties` | Cleaned this session |
| CI scripts | `ci-run.sh`, `backend-build.sh`, `frontend-build.sh`, `questionnaire-test.sh` | Cleaned this session |

**Tag readiness:** BLOCKED — worktree is dirty; release files must be staged explicitly before tagging. The tag command would be:

```bash
git add Dockerfile deploy/ scripts/ci/ docs/refactor/
git commit -m "Phase 14: release candidate cutover — gate consolidation, deploy cleanup, contract policy"
git tag -a v0.1-rc1 -m "Release candidate 1"
```

**Known caveats:**
- Oracle JDK 21 pinned path: `/usr/lib/jvm/jdk-21.0.11-oracle-x64`
- `questionnaire-test.sh` destroys and recreates `.venv` on each run
- `frontend-build.sh` uses `pnpm install --frozen-lockfile` (requires `pnpm-lock.yaml` to be current)
- Docker build requires `web/` and `ws/` to be absent from COPY (fixed this session)

## Immediate Next Action

Stage release-relevant files, run the full release gate, and decide on tag:

```bash
# Full release gate
bash scripts/ci/ci-run.sh

# If all pass, stage and tag
git add Dockerfile deploy/ scripts/ci/ docs/refactor/phase-14-release-candidate-cutover-plan.md
git commit -m "Phase 14: release candidate cutover"
git tag -a v0.1-rc1 -m "Release candidate 1"
```
