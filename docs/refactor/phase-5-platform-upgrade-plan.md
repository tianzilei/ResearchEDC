# Phase 5 Platform Upgrade Plan

**Created:** 2026-06-24
**Updated:** 2026-06-24
**Status:** Complete (2026-06-24)
**Purpose:** historical record for the completed platform/toolchain upgrade after export productization.
**Predecessor:** `docs/refactor/phase-4-export-productization-plan.md`

## Why Phase 5 Existed

The repo had moved past the earlier refactor and stabilization phases:

- legacy workflow closure was complete
- `shared/src/main/java` was at `0` Java files
- export stabilization and productization had landed
- daily verification had a documented gauntlet
- Java 26 Mockito / Byte Buddy compatibility had been repaired

The main unresolved engineering debt was no longer feature completeness or legacy removal.
It was platform alignment.

## Original Trigger

### Starting Toolchain State

- Spring Boot: `3.2.5`
- Spring Modulith: `1.1.4`
- ArchUnit: `1.1.1` transitively
- Java runtime in local verification: Java 26

### Starting Pain Point

`ModulithVerificationTest` passed, but ArchUnit emitted `Unsupported class file major version 70` warnings while importing JDK classes from the Java 26 runtime.

Starting behavior:

- warnings were non-fatal
- fallback import still allowed the rules to verify
- the baseline was usable, but not clean

## Completed Outcome

- Spring Boot upgraded to `3.5.2`
- Spring Modulith upgraded to `1.4.1`
- ArchUnit upgraded transitively to `1.4.1`
- Spring Framework upgraded to `6.2.8`
- Spring Security upgraded to `6.5.1`
- Mockito `5.23.0` and Byte Buddy `1.17.8` kept as explicit test-tooling ownership
- Verification gates remained green after the upgrade

## Workstreams

### Phase 5A: Upgrade Path Assessment

Evaluated three strategies:

1. targeted ArchUnit override only
2. Spring Modulith minor/major upgrade
3. Spring Boot upgrade with aligned Modulith refresh

Decision: use the aligned stack upgrade to avoid an unsupported partial dependency mix.

### Phase 5B: Verification Gate Protection

Mandatory gates remained:

```bash
bash scripts/ci/daily-gauntlet.sh
```

Focused platform gates:

```bash
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -pl app -am test -Dtest=ImportServiceTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -pl app -am test -Dtest=ExportServiceTest,OdmExportExecutionServiceTest,OdmExportGeneratorTest,ExportControllerTest -Dsurefire.failIfNoSpecifiedTests=false
```

Exit gates:

- architecture verification still passes
- export regression gate still passes
- import/static-mocking gate still passes
- no new frontend or backend baseline regressions are introduced

### Phase 5C: ArchUnit / Modulith Warning Reduction

The stack was upgraded to the aligned Spring Boot / Spring Modulith / ArchUnit baseline.
Non-fatal JDK import warnings were materially reduced; remaining warning behavior, if observed under newer JDKs, should be handled as new post-upgrade toolchain work rather than legacy-refactor work.

### Phase 5D: Documentation Re-baselining

Phase 5 is now a completed platform-upgrade record.
The refactor roadmap remains historical/baseline rather than forward-looking.

## Success Criteria

1. there is a single completed platform-upgrade record
2. the refactor roadmap is clearly historical/baseline rather than forward-looking
3. the dependency upgrade path is explicitly evaluated
4. architecture verification remains green throughout
5. Java 26 ArchUnit / Modulith warning debt is removed or reduced to a clearly documented residual risk

## Current Posture

Phase 5 is complete. The next engineering slice should be opened as a new focused plan only when there is concrete post-upgrade work, such as dependency convergence, verification hardening, or product feature delivery.
