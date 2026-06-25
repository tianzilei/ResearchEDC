# Phase 6 Post-Upgrade Baseline Hygiene Plan

**Created:** 2026-06-25
**Status:** Complete
**Purpose:** define the next engineering phase after the completed legacy-removal, export-productization, and platform-upgrade work.
**Predecessor:** `docs/refactor/phase-5-platform-upgrade-plan.md`

## Current Context

The repository is no longer in an active legacy-refactor phase.

Completed baseline:

- legacy workflow inventory closed: `963/963`
- DAO SPI ledger removed: `878/878`
- `shared/src/main/java`: `0` Java files
- `web/`: deleted
- `ws/`: absent
- export productization completed
- Phase 5 platform upgrade completed
- Spring Boot baseline: `3.5.2`
- Spring Modulith baseline: `1.4.1`
- ArchUnit baseline: `1.4.1`

The next useful work is not more broad legacy deletion. The next useful work is tightening the post-upgrade baseline so future feature work starts from a clean, measurable, low-drift foundation.

## Phase 6 Goal

Establish a clean post-upgrade engineering baseline by converging dependency ownership, confirming verification gates, cleaning remaining misleading documentation state, and identifying the next product-facing workstream.

## Non-Goals

- Do not reopen the completed legacy-removal roadmap.
- Do not reintroduce `shared` Java code.
- Do not create new DAO SPI or web/JSP compatibility layers.
- Do not modify released Liquibase migrations.
- Do not start broad product feature work before the baseline is confirmed.

## Workstreams

### Phase 6A: Dependency Ownership Convergence

**Goal:** remove ambiguity about which file owns dependency versions after the Phase 5 upgrade.

#### 6A.1 Compare Version Sources

Inspect at least:

- root `pom.xml`
- `research-edc-bom/pom.xml`
- module POMs that import or override dependency management

Focus first on known drift candidates:

- Mockito
- Byte Buddy
- Spring Boot
- Spring Modulith
- ArchUnit
- Castor
- Hibernate
- Surefire / Maven test tooling

#### 6A.2 Decide Ownership Rules

Document which version source is authoritative:

- parent root POM
- project BOM
- Spring Boot dependency management
- Spring Modulith BOM
- explicit test-tooling overrides

Expected rule of thumb:

- Spring platform versions should stay aligned through Spring Boot and Spring Modulith BOMs.
- Test instrumentation versions may stay explicit only when the Java runtime requires it.
- Duplicate version declarations should either match or be removed.

#### 6A.3 Apply Minimal Convergence

Make only the smallest version or dependency-management changes needed to remove drift.

Initial candidate:

- align Mockito / Byte Buddy declarations between root `pom.xml` and `research-edc-bom/pom.xml`, or remove the duplicate source if it is not active.

#### 6A Exit Gate

- dependency ownership is documented
- known duplicate version declarations are either aligned or intentionally retained
- `mvn -pl app -am test -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false` passes

### Phase 6B: Verification Baseline Confirmation

**Goal:** confirm that the current post-upgrade baseline is green using the consolidated project gate.

#### 6B.1 Run Daily Gauntlet

Primary command:

```bash
bash scripts/ci/daily-gauntlet.sh
```

If the full gauntlet is too slow or environment-bound, run the focused gates individually:

```bash
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -pl app -am test -Dtest=ImportServiceTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -pl app -am test -Dtest=ExportServiceTest,OdmExportExecutionServiceTest,OdmExportGeneratorTest,ExportControllerTest,ExportArtifactWriterTest -Dsurefire.failIfNoSpecifiedTests=false
cd frontend && pnpm typecheck
cd frontend && pnpm lint
```

#### 6B.2 Record Results

Capture:

- command run
- pass/fail result
- test counts where available
- environment blockers, if any
- whether failures are product regressions, test fragility, or local infrastructure issues

#### 6B Exit Gate

- daily gauntlet passes, or
- failures are explicitly classified with a follow-up owner and next action

### Phase 6C: Documentation Index Re-Baselining

**Goal:** keep documentation from pointing contributors back into completed phases.

#### 6C.1 Promote Phase 6 As Current Plan

Update:

- `docs/refactor/README.md`
- `AGENTS.md`, if project-level handoff text should mention the current engineering plan

Expected wording:

- refactor/removal roadmap remains historical
- Phase 5 platform upgrade remains completed
- Phase 6 is the active baseline hygiene plan

#### 6C.2 Scan For Misleading Active-Language

Search for stale phrases such as:

- `active roadmap`
- `active master plan`
- `Immediate Next Action`
- `remaining legacy reduction`
- `compatibility strangulation inside`
- `deferred until Spring Boot 3.5`

Do not rewrite historical documents wholesale. Only update text that would mislead current execution.

#### 6C Exit Gate

- there is one clear current plan
- completed plans are marked historical or complete
- old immediate-action sections are either historical or clearly superseded

### Phase 6D: Product Workstream Candidate Selection

**Goal:** identify the next product-facing workstream after the baseline is clean.

Candidate areas:

1. **Export operations polish**
   - improve export queue visibility
   - improve download and failed-job recovery UX
   - add clearer job metadata and filtering

2. **Frontend API type generation**
   - replace `frontend/src/api/generated.ts` placeholder with generated types
   - decide between `openapi-generator` and `openapi-typescript`
   - wire generation into CI or a documented local command

3. **Event workflow correctness**
   - replace placeholder `studyId=0` usage in event pages
   - derive study context from authenticated dashboard/session state
   - add frontend tests around study-scoped event listing

4. **Legacy gateway usage inventory**
   - inventory active SPA calls to `/api/v1/legacy/*`
   - classify long-term bridge endpoints vs candidates for module-native APIs
   - avoid removal work unless there is a product or maintenance payoff

#### 6D Exit Gate

- one product workstream is selected
- scope is narrow enough for a follow-up plan
- baseline hygiene findings are not blocking the selected workstream

## Recommended Delivery Order

1. Complete Phase 6A dependency ownership convergence.
2. Run Phase 6B verification baseline confirmation.
3. Apply Phase 6C documentation index updates.
4. Use Phase 6D to choose the next product-facing plan.

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Dependency cleanup changes runtime behavior unexpectedly | medium | keep changes minimal and run Modulith/import/export gates |
| Daily gauntlet exposes environment-only failures | medium | classify failures instead of treating all failures as product regressions |
| Documentation cleanup becomes broad rewriting | low | only update misleading execution status, leave historical detail intact |
| Product work starts before baseline is measurable | medium | complete 6A and 6B before selecting implementation work |

## Verification Commands

Minimum verification after dependency or build-tooling changes:

```bash
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
```

Preferred consolidated gate:

```bash
bash scripts/ci/daily-gauntlet.sh
```

Frontend-focused gate when touching `frontend/`:

```bash
cd frontend && pnpm typecheck
cd frontend && pnpm lint
cd frontend && pnpm test --run
```

## Success Criteria

1. dependency version ownership is explicit and drift is reduced
2. the post-upgrade verification baseline is confirmed or failures are classified
3. documentation points to Phase 6 as the current active engineering plan
4. completed refactor/removal and platform-upgrade plans remain historical
5. the next product-facing workstream is selected with a narrow follow-up scope

## Immediate Next Action

Start with **Phase 6A.1: compare version declarations in root `pom.xml` and `research-edc-bom/pom.xml`**, then decide whether Mockito / Byte Buddy should be aligned or have a single authoritative owner.

## Completion Record (2026-06-25)

### Phase 6A: Dependency Ownership Convergence — COMPLETE

Drift found and fixed:

| Dependency | Root POM | BOM (stale) | BOM (fixed) |
|---|---|---|---|
| Mockito | 5.23.0 | 5.11.0 | **5.23.0** |
| Byte Buddy | 1.17.8 | 1.14.12 | **1.17.8** |
| Liquibase | 4.3.5 | 4.28.0 | **4.3.5** |

Added missing `mockito-junit-jupiter` to BOM.

Ownership rule: root POM properties are authoritative for Phase 5+ versions; BOM mirrors them. Spring platform versions flow through Spring Boot / Spring Modulith BOMs.

### Phase 6B: Verification Baseline Confirmation — COMPLETE

| Gate | Result |
|---|---|
| `ModulithVerificationTest` | 1/0/0 ✅ |
| `ImportServiceTest` | 35/0/0 ✅ |
| Export tests (5 classes) | 58/0/0 ✅ |
| `pnpm typecheck` | 0 errors ✅ |
| `pnpm lint` | 0 errors ✅ |

### Phase 6C: Documentation Index Re-Baselining — COMPLETE

- `docs/refactor/README.md` updated: Phase 6 listed as active plan, stale "active master plan" language removed.
- Phase 6 plan status set to Complete.

### Phase 6D: Product Workstream Candidate Selection — COMPLETE

Selected: **Export operations polish** — improve export queue visibility, download/failure recovery UX, job metadata and filtering. Scope is narrow enough for a follow-up plan.
