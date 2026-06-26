# Phase 4 Export Productization Plan

**Created:** 2026-06-24
**Updated:** 2026-06-24
**Status:** Complete / historical (2026-06-24)
**Purpose:** define the next execution phase after post-hardening stabilization completed, with focus on export productization, broader verification, and remaining Java 26 tooling follow-up.
**Predecessor:** `docs/refactor/post-hardening-stabilization-plan.md`

## Why Phase 4 Exists

Phases 0-3D completed the stabilization work:

- backend build baseline was restored
- ODM namespace and contract output were hardened
- Java 26 Mockito / Byte Buddy compatibility was repaired
- export generator, artifact writer, controller, and service regression gates were expanded
- docs were refreshed to match the verified post-fix baseline

That means the project is no longer in a “recover the baseline” phase.
The next work should shift from stabilization to productization.

## Current Baseline

### Verified State

- branch: `master`
- working tree: clean at plan creation time
- export-targeted backend gate passes:
  - `ExportControllerTest` `8/0/0`
  - `ExportServiceTest` `19/0/0`
  - `OdmExportExecutionServiceTest` `5/0/0`
  - `OdmExportGeneratorTest` `21/0/0`
- retained static-mocking gate passes:
  - `ImportServiceTest` `35/0/0`
- architecture gate passes:
  - `ModulithVerificationTest` `1/0/0`

### Open Follow-up Themes

1. export backend is technically solid, but the user-facing export flow is still under-productized
2. verification gates are good, but still fragmented into slice commands
3. Java 26 mock creation is fixed, but ArchUnit / Modulith still emits classfile warnings
4. the new baseline should be promoted into a clearer recurring gauntlet

## Phase 4 Goal

Turn the export system from a stabilized backend capability into a more complete product surface with a stronger daily verification story.

## Workstreams

### Phase 4A: Export UX Productization

**Goal:** make export behavior clear and operable from the SPA, not just correct at the backend.

#### 4A.1 Show ODM Contract Version In UI

Review the frontend export center and add support to:

- display the selected ODM contract version
- choose `OC2_0_COMPAT` vs `OC2_1` intentionally where appropriate
- explain compatibility mode in concise operator-facing copy

#### 4A.2 Improve Export Status Presentation

Review how the UI presents:

- `PENDING`
- `RUNNING`
- `COMPLETED`
- `FAILED`
- `CANCELLED`

Success criteria:

- users can distinguish actionable failed jobs from normal queued jobs
- completed jobs clearly expose download affordances
- contract/version and artifact type are visible without opening raw payloads

#### 4A.3 Tighten Download Experience

Review end-to-end download behavior for:

- filename clarity
- content type correctness
- empty/missing artifact handling
- failed/not-completed job messaging

### Phase 4B: Verification Gate Consolidation

**Goal:** turn the now-scattered post-stabilization checks into a compact, repeatable gate.

#### 4B.1 Define A Standard Daily Gate ✅

Promote this as the minimum engineering gauntlet:

```bash
bash scripts/ci/daily-gauntlet.sh
```

Raw commands (still documented in script):

```bash
git status --short
pnpm -C frontend lint
pnpm -C frontend typecheck
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -pl app -am test -Dtest=ImportServiceTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -pl app -am test -Dtest=ExportServiceTest,OdmExportExecutionServiceTest,OdmExportGeneratorTest,ExportControllerTest -Dsurefire.failIfNoSpecifiedTests=false
```

#### 4B.2 Helper Script ✅

`scripts/ci/daily-gauntlet.sh` created with 6 checks. Script follows existing `scripts/ci/` conventions and is executable.

### Phase 4C: Java 26 Toolchain Hardening

**Goal:** eliminate the next likely Java 26 tooling hotspot before it becomes a blocker.

#### 4C.1 Audit ArchUnit / Modulith Warning Surface ✅

Current state:

- `ModulithVerificationTest` passes (1/0/0)
- ArchUnit 1.1.1 (via Modulith 1.1.4) emits `Unsupported class file major version 70` warnings when importing JDK classes from the Java 26 runtime
- Warnings are non-fatal — ArchUnit falls back to simple import and all architecture rules still verify

Follow-up (deferred):

- Upgrade to Spring Modulith 1.4.1 / ArchUnit 1.4.1 requires Spring Boot 3.2.5 → 3.5.x — too risky for this phase
- ArchUnit 1.4.1 bundles ASM that supports Java 26 class files
- Track as a future Spring Boot upgrade follow-up

#### 4C.2 Preserve Explicit Test Toolchain Ownership

Keep these guardrails in place:

- explicit Mockito versions in the parent
- explicit Mockito deps in `app/pom.xml`
- explicit Surefire `-javaagent` configuration

### Phase 4D: Documentation Promotion

**Goal:** make the completed stabilization phase historical and establish Phase 4 as the export-productization record.

#### 4D.1 Historicalize Stabilization

The stabilization plan should remain as evidence, but no longer as the forward-looking plan.

#### 4D.2 Refresh Index / Roadmap References

Where appropriate, docs should now reflect:

- stabilization is complete
- Phase 4 is the active follow-up plan
- export productization is the current focus area

## Recommended Delivery Order

### Step 1: Phase 4A

Start with UI-facing export improvements.

Why first:

- backend reliability work is already in place
- the highest user-facing value is now at the SPA / download workflow layer

### Step 2: Phase 4B

Consolidate the verification gate while the current test inventory is fresh and trusted.

### Step 3: Phase 4C

Address ArchUnit / Modulith warning debt before a future JDK bump turns warning noise into failure pressure.

### Step 4: Phase 4D

Refresh references once the active plan has started landing.

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Export backend is correct but UI hides contract/version semantics | operator confusion and support overhead | surface contract version and clearer status labels |
| Verification remains fragmented | checks drift or get skipped | define a single daily gate and optionally wrap it in a CI helper |
| ArchUnit warnings are ignored too long | future JDK/tooling bump becomes noisy or blocking | audit and upgrade deliberately while baseline is green |
| Docs continue to point at a completed stabilization phase | planning confusion | mark stabilization historical and Phase 4 active |

## Success Criteria

1. there is a single completed Phase 4 plan document
2. stabilization is clearly marked historical/completed
3. export UX follow-up is defined in concrete terms
4. a standard daily verification gate is documented
5. ArchUnit / Modulith Java 26 warning follow-up is explicitly tracked

## Current Posture

Phase 4A-4D are complete. The deferred ArchUnit/Modulith version upgrade was completed in Phase 5.
