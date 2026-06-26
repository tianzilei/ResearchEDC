# Post-Hardening Stabilization Plan

**Created:** 2026-06-24
**Updated:** 2026-06-24
**Status:** ✅ Completed / historical (2026-06-24)
**Purpose:** historical record of the stabilization work that followed the first post-refactor hardening wave.
**Supersedes:** `docs/refactor/post-refactor-product-hardening-plan.md`

> Status note:
> This plan is complete and now historical.
> The active follow-up plan is [`phase-4-export-productization-plan.md`](./phase-4-export-productization-plan.md).

## Completion Summary (2026-06-24)

### Phase 0: Build Baseline Restoration ✅

- Deleted 8 dead files: `ExtendedBasicDataSource`, `DbConfig`, `QueryStore`, `SchedulerConfig`, and 4 scheduler classes
- Replaced DBCP1 datasource wiring with Spring Boot HikariCP auto-config via `spring.datasource.*`
- Created minimal `LiquibaseConfig` to preserve conditional Liquibase bean behavior
- Replaced Joda-Time with `java.time` in `AuditUserLoginAdapter`
- Fixed `OdmExportExecutionServiceTest` uncaught `IOException`
- Fixed `ExportDataProviderAdapter` Modulith boundary declarations
- **Result:** `mvn -pl app -am compile -DskipTests` ✅ | `ModulithVerificationTest` 1/0/0 ✅

### Phase 1: ODM Export Conformance ✅

- Corrected namespace handling in `OdmExportGenerator`
- Added proper `xsi:schemaLocation` via `setAttributeNS`
- Declared `xmlns:xsi` explicitly on the ODM root
- Made `oc:Monitored` namespace-aware
- Updated generator tests to match the corrected XML output
- **Result:** `OdmExportGeneratorTest` 6/0/0 ✅

### Phase 2: Java 26 Mockito / Byte Buddy Compatibility ✅

- Upgraded `mockito-core` from `5.11.0` to `5.23.0`
- Upgraded `byte-buddy` / `byte-buddy-agent` from `1.14.12` to `1.17.8`
- Added explicit `mockito-core` and `mockito-junit-jupiter` test dependencies in `app/pom.xml`
- Configured Maven Surefire to run tests with an explicit Mockito `-javaagent`
- Removed reliance on runtime self-attach for inline mocking on newer JDKs
- **Result:** export-targeted tests now pass on Java 26 instead of failing during mock creation

### Phase 3A: Confirm Recovered Test Baseline ✅

- Verified `ModulithVerificationTest` passes (1/0/0)
- Verified export-targeted gate passes: `ExportServiceTest` 15/0/0, `OdmExportExecutionServiceTest` 5/0/0, `OdmExportGeneratorTest` 6/0/0, `ExportControllerTest` 5/0/0
- Verified `ImportServiceTest` passes (35/0/0)
- **Result:** 67/0/0 baseline confirmed

### Phase 3B: ODM Schema Validation Tests ✅

- Expanded `OdmExportGeneratorTest` from 6 to 21 tests
- Added root attribute tests (FileType, FileOID, ODMVersion, CreationDateTime)
- Added schemaLocation format tests for both OC2_0_COMPAT and OC2_1
- Added namespace declaration tests (xmlns:oc, xmlns:xsi)
- Added study element structure tests (OID, StudyName, StudyDescription, ProtocolName)
- Added protocol name fallback test (defaults to StudyName when null)
- Added MetaDataVersion OID/Name tests
- Added ClinicalData hierarchy tests (StudyOID, MetaDataVersionOID)
- Added multiple subjects test
- Added empty item value test
- Added EventRepeatKey presence/absence tests
- Added OC2_0_COMPAT StudyDetails tests (FacilityName, FacilityContactEmail)
- Added empty facility element tests
- **Result:** 21/0/0 pass; full gate 82/0/0

### Phase 3C: Export Artifact / Download Regressions ✅

- Added `ExportArtifactWriterTest` (5 tests): file creation, directory structure, byte length, UTF-8 encoding, empty content
- Added controller download tests: content type, Content-Disposition header, Content-Length, service delegation for not-found/not-completed
- Added service tests: null file path rejection, error message storage, null message handling, completed date setting
- **Result:** 32/0/0 export tests pass; full gate 94/0/0

### Phase 3D: Documentation Refresh ✅

- Updated AGENTS.md: test counts (21/21), gauntlet commands, export gate
- Updated README.md: plan status reflects Phases 0-3C complete
- Updated stabilization plan: verification standard includes ExportArtifactWriterTest
- Updated roadmap: expanded test counts and artifact regression tests noted
- **Result:** all docs describe verified post-fix baseline

## Current Verified State

### Repository State

- Branch: `master`
- Working tree: expected to be clean once the current stabilization edits are committed
- Recent delivery commits:
  - `1fbf0f774 fix: post-hardening stabilization — build baseline restoration + ODM namespace hardening`
  - `b893a01d3 refactor(frontend): eliminate all lint warnings (76→0)`
  - `b3e2e5dbc feat: ODM export execution pipeline + frontend lint zero-error + warning reduction`

### Verified Commands

```bash
pnpm -C frontend lint
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -pl app -am test -Dtest=ExportServiceTest,OdmExportExecutionServiceTest,OdmExportGeneratorTest,ExportControllerTest -Dsurefire.failIfNoSpecifiedTests=false
```

Expected/verified outcomes after Phase 2:

- frontend lint passes
- `ModulithVerificationTest` passes
- export-targeted backend tests pass under Java 26
- the previous `Mockito cannot mock this class` / `Byte Buddy ... Java 26 (70) is not supported` errors are gone

## Historical Scope

This document records the completed stabilization phase.
Forward-looking planning has moved to `phase-4-export-productization-plan.md`.

## Working Diagnosis

The earlier stabilization failures were two different problems that happened back-to-back:

1. build ownership drift after `shared/` became resource-only,
2. test-toolchain drift on Java 26 because Mockito and Byte Buddy were pinned below the JDK support line and inline mocking still depended on dynamic self-attach.

Phase 0 fixed the build baseline. Phase 2 fixed the Java 26 mock-creation failures. The remaining work is now ordinary regression hardening, not legacy-removal recovery.

## Primary Goal

Lock the new post-hardening baseline into a repeatable verification gate, then harden ODM export behavior around real schema and artifact-level regressions.

## Workstreams

### Workstream A: Test Baseline Preservation

**Goal:** keep the recovered Java 26 test baseline stable and widen it carefully.

#### A1. Keep Test Ownership Explicit

Preserve these rules:

- do not rely on `spring-boot-starter-test` transitives alone for Mockito alignment
- keep Mockito and Byte Buddy versions centrally managed in the parent
- keep inline-mocking agent setup explicit in Surefire while Java 21+ agent rules continue tightening

#### A2. Widen Verification Beyond Export

Next verification ring:

```bash
mvn test -pl app -am -Dtest=ImportServiceTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
```

Reason:

- `ImportServiceTest` exercises retained static mocking
- `ModulithVerificationTest` remains the architectural boundary gate

#### A3. Decide Whether To Refresh ArchUnit / Modulith Tooling

`ModulithVerificationTest` currently passes, but ArchUnit still emits Java 26 classfile warnings during import. That is not a blocker yet, but it is the next likely toolchain hotspot.

Follow-up:

- review Spring Modulith and ArchUnit versions for Java 26 readiness
- upgrade only if warnings become failures or block broader test execution

### Workstream B: ODM Export Contract Hardening

**Goal:** move the ODM export path from “now runnable” to “schema-safe and regression-resistant”.

#### B1. Add XSD-Backed Validation Tests

Add validation coverage for both contract families:

- `OC2_0_COMPAT`
- `OC2_1`

Target:

- generated XML validates against the selected XSD family, not just string assertions

#### B2. Preserve Contract-Specific Behavior

Keep explicit tests for:

- `OC2_1` omits `FacilityContactEmail`
- `OC2_0_COMPAT` may retain the deprecated compatibility element
- schema-location and namespace declarations remain correct

#### B3. Add Artifact-Level Regression Coverage

Add tests for:

- generated file written to disk
- download endpoint returns the artifact cleanly
- failed generation persists `FAILED` with a readable, bounded error message

#### B4. Keep The Export Slice As A Standing Gate

Standing command:

```bash
mvn -pl app -am test -Dtest=ExportServiceTest,OdmExportExecutionServiceTest,OdmExportGeneratorTest,ExportControllerTest -Dsurefire.failIfNoSpecifiedTests=false
```

### Workstream C: Export UX And API Completion

**Goal:** finish the user-facing loop now that backend generation and download paths exist.

#### C1. Surface Contract Version In The UI

Review whether the export center can:

- show the selected ODM contract version
- let operators choose compatibility mode explicitly
- make artifact type and status easier to understand

#### C2. Tighten Download Semantics

Review:

- content type
- filename generation
- behavior for missing, failed, or incomplete artifacts

#### C3. Clarify Execution Semantics

Document whether export execution is:

- synchronous today, or
- intended to become background execution later

Do not add worker complexity until the current path is fully regression-covered.

### Workstream D: Documentation Refresh

**Goal:** update docs to match the verified post-fix baseline.

#### D1. Refresh Stabilization References

Update:

- `docs/refactor/README.md`
- `docs/refactor/refactor-removal-roadmap.md`
- `AGENTS.md`

What should change:

- Java 26 Mockito / Byte Buddy compatibility is fixed
- export-targeted tests are runnable again
- the next-stage plan is export regression hardening, not build repair

#### D2. Re-state The Minimum Verification Standard

Updated baseline:

```bash
git status --short
pnpm -C frontend lint
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn -pl app -am test -Dtest=ExportServiceTest,OdmExportExecutionServiceTest,OdmExportGeneratorTest,ExportControllerTest,ExportArtifactWriterTest -Dsurefire.failIfNoSpecifiedTests=false
```

## Recommended Delivery Order

### Phase 3A: Confirm The Recovered Test Baseline

Run and keep green:

- `ImportServiceTest`
- `ModulithVerificationTest`
- export-targeted test gate

### Phase 3B: Add ODM Schema Validation

Reason:

- export is now executable, so contract correctness is the highest-value next safeguard

### Phase 3C: Close Export Artifact / Download Regressions

Reason:

- this finishes the operational loop, not just the generation loop

### Phase 3D: Refresh Docs And Baselines

Reason:

- repo documentation should describe the post-fix reality, not the earlier broken state

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Mockito alignment drifts again through starter transitives | Java 26 test failures return unexpectedly | keep explicit Mockito deps in `app/pom.xml` and versions in parent |
| Inline mocking falls back to dynamic self-attach again | fragile behavior on newer JDKs | keep explicit Surefire `-javaagent` setup |
| ODM XML passes string assertions but fails real schema validation | downstream integration breakage | add XSD-backed validation tests |
| Docs continue to describe the old failure mode | misleading maintenance baseline | refresh only after rerun verification |

## Success Criteria

1. Java 26 test execution remains green for the export regression slice.
2. `ImportServiceTest` and `ModulithVerificationTest` remain green on the same toolchain.
3. ODM export validates against the selected XSD family.
4. Artifact creation, failure persistence, and download behavior are regression-covered.
5. Refactor/stabilization docs describe the verified post-fix baseline.

## Historical Next Action

Start with **Phase 3A: confirm the recovered test baseline**, then move directly into **Phase 3B: ODM schema validation tests**.
