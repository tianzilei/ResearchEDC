# Phase 5 Platform Upgrade Plan

**Created:** 2026-06-24
**Updated:** 2026-06-24
**Status:** ✅ Complete (2026-06-24)
**Purpose:** define the next phase after export productization, focused on platform/toolchain upgrade work needed to reduce Java 26 warning debt and prepare the repo for a cleaner long-term baseline.
**Predecessor:** `docs/refactor/phase-4-export-productization-plan.md`

## Why Phase 5 Exists

The repo is now in a different state than the earlier refactor and stabilization phases:

- legacy workflow closure is complete
- `shared/src/main/java` is at `0` Java files
- export stabilization and productization have landed
- daily verification has a documented gauntlet
- Java 26 Mockito / Byte Buddy compatibility has been repaired

The main unresolved engineering debt is no longer feature completeness or legacy removal.
It is platform alignment.

## Current Trigger

### Verified Toolchain State

- Spring Boot: `3.2.5`
- Spring Modulith: `1.1.4`
- ArchUnit: `1.1.1` transitively
- Java runtime in local verification: Java 26

### Known Pain Point

`ModulithVerificationTest` passes, but ArchUnit still emits `Unsupported class file major version 70` warnings while importing JDK classes from the Java 26 runtime.

Current behavior:

- warnings are non-fatal
- fallback import still allows the rules to verify
- the baseline is usable, but not clean

This is acceptable short-term, but it is the next likely build/tooling hotspot.

## Phase 5 Goal

Move the project onto a cleaner platform baseline where architecture verification and test tooling align better with the active Java runtime, without destabilizing the now-green application and export gates.

## Workstreams

### Phase 5A: Upgrade Path Assessment

**Goal:** determine the safest realistic upgrade path before touching runtime libraries.

#### 5A.1 Map The Dependency Chain

Document the current stack relationship:

- Spring Boot `3.2.5`
- Spring Modulith `1.1.4`
- ArchUnit `1.1.1`
- Mockito `5.23.0`
- Byte Buddy `1.17.8`

Output:

- identify which versions are pinned directly
- identify which versions are transitive
- identify the minimum compatible stack that removes Java 26 ArchUnit warnings

#### 5A.2 Compare Candidate Upgrade Strategies

Evaluate at least these paths:

1. targeted ArchUnit override only
2. Spring Modulith minor/major upgrade
3. Spring Boot upgrade with aligned Modulith refresh

Decision criteria:

- warning removal value
- blast radius
- test breakage risk
- config/API migration effort

### Phase 5B: Verification Gate Protection

**Goal:** keep the current green baseline measurable throughout upgrade work.

#### 5B.1 Treat These As Mandatory Gates

```bash
bash scripts/ci/daily-gauntlet.sh
```

And during platform work specifically:

```bash
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -pl app -am test -Dtest=ImportServiceTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -pl app -am test -Dtest=ExportServiceTest,OdmExportExecutionServiceTest,OdmExportGeneratorTest,ExportControllerTest -Dsurefire.failIfNoSpecifiedTests=false
```

#### 5B.2 Define Upgrade Exit Gates

Phase 5 should not be called complete unless:

- architecture verification still passes
- export regression gate still passes
- import/static-mocking gate still passes
- no new frontend or backend baseline regressions are introduced

### Phase 5C: ArchUnit / Modulith Warning Reduction

**Goal:** eliminate or materially reduce Java 26 classfile warnings in architecture verification.

#### 5C.1 Preferred Outcome

Preferred end state:

- `ModulithVerificationTest` passes
- ArchUnit no longer emits major-version fallback warnings for the active JDK

#### 5C.2 Acceptable Interim Outcome

If full cleanup requires a larger Spring Boot jump than Phase 5 can safely absorb, acceptable interim outcome is:

- warning source fully documented
- upgrade blockers explicitly recorded
- next viable aligned version set identified

### Phase 5D: Documentation Re-baselining

**Goal:** promote the repo from “recently stabilized” to “platform-upgrade in progress”.

Update as work lands:

- `docs/refactor/README.md`
- `docs/refactor/refactor-removal-roadmap.md`
- `AGENTS.md`

What should shift:

- roadmap remains historical/baseline
- Phase 5 becomes the active platform plan
- Java 26 warning debt is tracked as active engineering work instead of a deferred note buried in Phase 4

## Recommended Delivery Order

### Step 1: Phase 5A

Assess the upgrade path first.

Why:

- the current baseline is green
- unnecessary version churn would create avoidable risk

### Step 2: Phase 5B

Lock the verification gates before changing framework versions.

### Step 3: Phase 5C

Execute the smallest upgrade that removes or reduces the ArchUnit / Modulith warning surface.

### Step 4: Phase 5D

Refresh documentation after the chosen path is verified.

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Upgrading Spring Boot to clear one warning causes broader runtime/config regressions | large blast radius | assess targeted override and aligned version options before changing the stack |
| ArchUnit warning cleanup is attempted without strong gates | regressions hide behind framework churn | keep daily gauntlet and focused module tests mandatory |
| Platform work reopens already-stable export/test paths | trust loss in recent stabilization work | treat export/import/modulith gates as hard exit criteria |
| Docs continue to frame the repo as primarily in refactor mode | planning confusion | mark roadmap historical and make Phase 5 the active plan |

## Success Criteria

1. there is a single active platform-upgrade plan
2. the refactor roadmap is clearly historical/baseline rather than forward-looking
3. the dependency upgrade path is explicitly evaluated
4. architecture verification remains green throughout
5. Java 26 ArchUnit / Modulith warning debt is either removed or reduced to a clearly documented blocker

## Immediate Next Action

Start with **Phase 5A.1: map the current Spring Boot / Modulith / ArchUnit dependency chain and identify the lowest-risk candidate upgrade path**.
