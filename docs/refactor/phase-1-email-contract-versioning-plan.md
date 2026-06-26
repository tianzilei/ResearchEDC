# Phase 1 Email Contract Versioning Plan

**Created:** 2026-06-24
**Status:** Proposed execution plan for Phase 1 email-field cleanup Slice E3.
**Primary parent plan:** `docs/refactor/phase-1-email-field-removal-plan.md`

## Purpose

Define the concrete next step for E3 now that product code, Java mappings, and runtime rule/email storage have already been cleaned up.

The remaining work is no longer "remove more Java email code". It is to version the retained ODM/XSD compatibility contract so that:

1. the current legacy-compatible ODM contract remains stable for downstream consumers, and
2. a new default contract can remove `OpenClinica:FacilityContactEmail` without making a silent breaking change.

## Current Verified State

### Completed

- Product-side user/study email fields are no longer exposed in active Java APIs, frontend forms, or questionnaire auth contracts.
- Runtime rule schema no longer exposes `EmailAction` or `EmailActionType`.
- Forward migration `migration/3.18/2026-06-21-retire-email-storage.xml` removes:
  - `dc_send_email_event`
  - `rule_action.email_to`
  - `rule_action.email_subject`
- PostgreSQL write-boundary triggers force retained compatibility columns to empty strings:
  - `user_account.email`
  - `module_user_account.email`
  - `study.facility_contact_email`
  - `module_study.facility_contact_email`

### Remaining Compatibility Surface

- `shared/src/main/resources/properties/OpenClinica-ODM1-3-0-OC2-0-foundation.xsd`
  still defines `FacilityContactEmail` as a deprecated compatibility element.
- Historical pre-3.18 Liquibase files still document original email-era columns/tables.
- Existing tests intentionally lock the current compatibility contract:
  - `app/src/test/java/org/researchedc/compatibility/RetiredEmailSchemaCleanupTest.java`

### Important Constraint

The existing `OC2-0` ODM contract cannot safely remove `FacilityContactEmail` in place.
Doing so would create an undocumented breaking schema change for downstream validators/importers/export consumers.

## Decision

**Do not remove `FacilityContactEmail` from the existing `OC2-0` schema.**

Instead:

1. keep `OC2-0` as the compatibility schema,
2. introduce a new ODM schema version for the email-free contract, and
3. move default exports to the new version only through explicit version selection logic.

## Target End State

### Compatibility Contract

- `OC2-0` remains available and unchanged except for deprecation annotations/documentation.
- Existing compatibility test continues to assert that `FacilityContactEmail` exists in `OC2-0`.

### New Default Contract

- A new ODM schema version is added that removes `FacilityContactEmail`.
- New exports default to the new schema version unless a caller explicitly requests compatibility mode.

### Product Behavior

- No active product flow writes or depends on study-contact email.
- Export code never emits user-provided study-contact email values.
- Compatibility mode, if retained, emits either no value or a neutral empty value according to the selected schema contract.

## Naming And Versioning Strategy

### Recommended Version

Add a new schema family using an incremented compatibility version, for example:

- `OpenClinica-ODM1-3-0-OC2-1.xsd`
- `OpenClinica-ToODM1-3-0-OC2-1.xsd`
- `OpenClinica-ODM1-3-0-OC2-1-foundation.xsd`

### Versioning Rule

- `OC2-0` = legacy-compatible, retains deprecated `FacilityContactEmail`
- `OC2-1` = email-free contract, intended new default

This keeps downstream behavior explicit and avoids retroactive mutation of a published schema filename.

## Work Packages

### WP1: Freeze And Document The Current Compatibility Contract ✅

**Goal:** explicitly declare `OC2-0` as the stable legacy contract.

**Changes**

- Keep the current `FacilityContactEmail` deprecation annotation in:
  - `shared/src/main/resources/properties/OpenClinica-ODM1-3-0-OC2-0-foundation.xsd`
- Expand `RetiredEmailSchemaCleanupTest` wording so it is clear that:
  - `OC2-0` is intentionally retained for downstream compatibility
  - removal must happen in a new schema version, not in place
- Update `phase-1-email-field-removal-plan.md` to point to this versioning plan as the E3 execution detail.

**Exit check**

- Tests and docs describe `OC2-0` as compatibility-only, not as the future default contract.

### WP2: Add A New Email-Free ODM Schema Family ✅

**Goal:** create the first contract version that fully removes `FacilityContactEmail`.

**Changes**

- Copy and version:
  - `OpenClinica-ODM1-3-0-OC2-0.xsd`
  - `OpenClinica-ToODM1-3-0-OC2-0.xsd`
  - `OpenClinica-ODM1-3-0-OC2-0-foundation.xsd`
- Create `OC2-1` equivalents.
- In `OC2-1-foundation.xsd`, remove:
  - `FacilityContactEmail`
- Keep surrounding `FacilityInformation` structure intact:
  - `FacilityContactName`
  - `FacilityContactDegree`
  - `FacilityContactPhone`
- Ensure imports/includes point to the new versioned filenames.

**Design rule**

- The new schema should remove only the retired email element in this slice.
- Avoid bundling unrelated schema cleanup into `OC2-1`.

**Exit check**

- `OC2-0` and `OC2-1` can coexist in `shared/src/main/resources/properties/`.

### WP3: Introduce Explicit ODM Contract Selection ✅

**Goal:** make schema selection explicit in code and product behavior.

**Changes**

- Introduce an app-owned contract selector type, for example:
  - `org.researchedc.module.export.enums.OdmContractVersion`
  - values such as `OC2_0_COMPAT`, `OC2_1`
- If export request DTOs are extended, add an optional version field to:
  - `app/src/main/java/org/researchedc/module/export/dto/CreateExportJobRequest.java`
  - `app/src/main/java/org/researchedc/module/export/dto/ExportJobDTO.java`
  - `app/src/main/java/org/researchedc/module/export/entity/ExportJob.java`
- If full export generation is not yet implemented, still add the enum and persistence field only when there is a clear near-term caller.
- Otherwise, capture the selection seam in code comments and tests until the generator lands.

**Default behavior**

- Existing persisted jobs without a version keep compatibility semantics.
- New jobs should eventually default to `OC2_1`.
- Compatibility mode must be explicit rather than accidental.

**Exit check**

- There is one named application concept for ODM contract versioning.
- No code relies on filename literals scattered across the export pipeline.

### WP4: Bind Export Output To The Selected Contract ✅

**Goal:** make actual export payloads honor the selected schema family.

**Changes**

- Add a schema resource resolver, for example:
  - `OdmSchemaResourceResolver`
  - maps `OdmContractVersion` to the correct XSD filenames
- Update export generation code to:
  - emit the matching schema references
  - validate or serialize against the selected contract family
- If the export generator is not yet implemented in the current module, create:
  - a small abstraction point now
  - a tracked TODO only at the seam where generation will plug in

**Important rule**

- `OC2-1` exports must not emit `FacilityContactEmail`.
- `OC2-0` compatibility exports may structurally allow the element, but product data must still remain empty/inert.

**Exit check**

- Schema version selection changes output contract deterministically.

### WP5: Add Guardrail Tests ✅

**Goal:** prevent future drift or silent contract regression.

**Required tests**

- Keep and update:
  - `RetiredEmailSchemaCleanupTest`
- Add:
  - test asserting `OC2-0` still contains deprecated `FacilityContactEmail`
  - test asserting `OC2-1` does **not** contain `FacilityContactEmail`
  - test asserting `rules-ODM.xsd` still omits `EmailAction`
  - if export selection code is added, test that `OdmContractVersion` resolves to the correct schema files
  - if export generation exists, test that `OC2-1` output omits the email element entirely

**Nice-to-have tests**

- resource smoke test that all referenced versioned XSD files exist on the classpath
- backward-compatibility test that old version filenames still resolve unchanged

### WP6: Documentation And Rollout Notes ✅

**Goal:** make the compatibility story explicit for maintainers and downstream integrators.

**Changes**

- Update:
  - `docs/refactor/phase-1-email-field-removal-plan.md`
  - `docs/refactor/refactor-removal-roadmap.md`
  - `docs/refactor/README.md`
- Record:
  - which schema version is compatibility-only
  - which schema version is the intended default
  - what downstream users should do if they still validate against `OC2-0`

**Release note requirement**

- Call out schema versioning as a contract change, not just an internal cleanup.

## Proposed Delivery Order

### Slice A: Contract Scaffolding ✅

1. Freeze `OC2-0` as compatibility-only in docs/tests.
2. Add `OC2-1` schema files.
3. Add tests that distinguish the two versions.

**Why first**

This creates the contract boundary without touching runtime export behavior.

### Slice B: Export Selection Plumbing ✅

1. Add `OdmContractVersion` abstraction.
2. Add schema resource resolver.
3. Add request/job wiring if there is already a real export pipeline consumer.

**Why second**

This prevents schema filenames from becoming ad hoc implementation detail.

### Slice C: Default Contract Cutover ✅

1. Move new exports to default to `OC2-1`.
2. Keep explicit opt-in compatibility mode for `OC2-0`.
3. Add release/documentation guidance for downstream adopters.

**Why last**

This is the first externally visible behavior change and should only happen after the versioned contract exists and is test-covered.

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Removing `FacilityContactEmail` in place from `OC2-0` | Silent breaking change for downstream validators | Never mutate existing `OC2-0`; create `OC2-1` |
| Export code hardcodes schema filenames in multiple places | Partial or inconsistent cutover | Centralize schema resolution behind one app-owned selector |
| Future cleanup accidentally removes compatibility assets | Downstream breakage | Add resource existence and schema-content tests |
| New version bundles unrelated schema edits | Harder downstream adoption | Restrict `OC2-1` delta to email-contract removal in this slice |

## Out Of Scope

- Dropping historical database columns immediately.
- Rewriting pre-3.18 Liquibase history.
- Removing generic `Email` constructs from base CDISC ODM schemas not owned by ResearchEDC.
- Broad ODM redesign unrelated to retired email contact fields.

## Success Criteria ✅

1. `OC2-0` remains stable and explicitly documented as compatibility-only. ✅
2. A new versioned ODM contract exists without `FacilityContactEmail`. ✅
3. Tests prove the distinction between compatibility and new-default contracts. ✅
4. Export code has a clear schema version selection seam. ✅
5. Future removal work no longer depends on mutating a legacy schema file in place. ✅

## Recommended Immediate Next Slice

**All slices completed.** The email contract versioning plan is fully executed.

Remaining work: when the ODM export generator is implemented, bind `OdmSchemaResourceResolver` to the generation pipeline so `OC2-1` output omits `FacilityContactEmail` and `OC2-0` output retains it as deprecated/inert.
