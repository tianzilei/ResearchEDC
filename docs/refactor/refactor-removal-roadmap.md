# Refactor And Removal Roadmap

**Updated:** 2026-06-20
**Purpose:** single source of truth for the remaining legacy refactor/removal work.

## Current Verified State

- Legacy workflow inventory: `963/963` closed (`100.0%`)
- DAO SPI ledger: `878/878` removed (`100.0%`)
- `shared/dao`: `0` files remain
- `web/`: deleted
- `ws/`: absent
- JSP surface: `0` files
- Remaining `shared/` Java surface: `106` files
- Modulith Java surface: `383` files
- Code balance by file count: `22%` shared legacy / `78%` module modern

This means the workflow-level deletion program is complete. The remaining work is **compatibility strangulation inside `app/` and `shared/`**, not more `web/` or DAO SPI cleanup.

## Document Roles

| Document | Role |
|---|---|
| `README.md` | Directory index and maintenance rules |
| `refactor-removal-roadmap.md` | Active roadmap |
| `remove-legacy-code-plan.md` | Baseline and completed-phase evidence |
| `legacy-workflow-inventory.{md,csv}` | Generated proof that workflow inventory is closed |
| `phase-3-dao-replacement-ledger.{md,csv}` | Generated proof that DAO SPI deletion is complete |
| `phase-1-email-field-removal-plan.md` | Active focused follow-up slice |

## Remaining Legacy Surface

### By Package Family

| Surface | Files | Why It Still Exists |
|---|---:|---|
| `shared/bean` | 52 | Legacy DTOs still consumed by module/internal adapters and compatibility workflows |
| `shared/domain` | 46 | Legacy entities and shared mapping model still used by repositories/adapters |
| `shared/core` | 3 | Resource/config/path and utility support still used by import, form, and shared compatibility code |
| `shared/i18n` | 3 | Legacy form/import localization support |
| `shared/exception` | 2 | Compatibility exception types |

### Highest-Weight Callers

| Caller | Why It Matters |
|---|---|
| `app/module/dataimport/internal/adapter/ImportCrfDataAdapter` | Largest remaining consumer of `shared.bean.*`, ODM compatibility types, and `shared` resource/i18n support |
| `app/control/form/Validator` | Major anchor for `shared.core`, `shared.i18n`, and legacy form DTO assumptions |
| `app/control/form/FormDiscrepancyNotes` | Couples discrepancy workflows to legacy form/bean model |
| `app/module/*/internal/adapter/*.java` | 20 module adapters still translate to/from legacy `shared` DTOs/entities |
| `app/module/event/internal/adapter/StudyEventDaoAdapter` | Still carries legacy bean/entity translation logic after observer cleanup |

## Completed Workstreams

These are closed and should not be reopened except to fix regressions:

1. Workflow inventory closure
2. `web/` deletion and JSP removal
3. `ws/` retirement
4. DAO SPI widening and deletion
5. Dead code scavenging and dependency cleanup
6. Phase B schema ownership trigger rollout

## Active Roadmap

### Workstream 1: Shared Support Extraction

**Goal:** eliminate the lowest-risk `shared` support packages first.

**Scope**
- `shared/core/*`
- `shared/i18n/*`
- `shared/exception/*`

**Approach**
- Move runtime support classes into `app/config`, `app/control/form/support`, or module-local infrastructure packages.
- Continue shrinking support-style helpers until no app-owned runtime support remains in `shared/`.

**Current Progress**
- `app/control/form/Validator` now uses app-owned form support for locale/bundle/format behavior.
- `app/module/dataimport/internal/adapter/ImportCrfDataAdapter` now resolves ODM mapping and page-message bundles through module-owned support instead of direct `shared/core` or `shared/i18n` imports.
- `app/module/crf/internal/adapter/*` no longer imports `shared/exception` or `shared/core/util` compatibility helper types.
- `app/config/DbConfig` now uses an app-owned DBCP compatibility wrapper, allowing the old `shared/core/ExtendedBasicDataSource` class to be removed.
- Legacy `CoreResources` Spring initialization now lives in `shared/core`, leaving app-side configuration free of direct `shared/core` imports while preserving shared compatibility static resource setup.
- Retired extract post-processing helpers (`Processing*`, `Pdf/Sas/SqlProcessingFunction`, `ScriptRunner`) were removed after zero-caller scans and compile/test verification.
- The unused `shared/core/util/ItemGroupCrvVersionUtil` view-helper residue was removed; no `shared/core/util` Java callers remain.

**Exit Gate**
- No production code outside `shared/` imports `org.researchedc.core.*`, `i18n.*`, or `exception.*`.

### Workstream 2: Data Import Compatibility Strangulation

**Goal:** turn import compatibility into a module-owned model instead of a `shared.bean.*` dependency hub.

**Primary Target**
- `app/src/main/java/org/researchedc/module/dataimport/internal/adapter/ImportCrfDataAdapter.java`

**Approach**
- Introduce module-owned import DTOs/records for validation, preview, and commit flows.
- Isolate ODM parsing/mapping behind dataimport-owned types.
- Shrink or eliminate imports from:
  - `shared.bean.managestudy.*`
  - `shared.bean.submit.*`
  - `shared.bean.submit.crfdata.*`
  - `shared.bean.login.*`
- Preserve current upload/validate/commit behavior while replacing legacy DTO plumbing.

**Current Progress**
- ODM mapping resource lookup and page-message bundle resolution now live in `module/dataimport/internal/support`, so `ImportCrfDataAdapter` no longer depends directly on shared support classes while preserving the current import validation flow.
- `ImportCrfDataAdapter` now keeps ODM beans only at its retained external compatibility boundary; internal study/subject/event/CRF lookup and event-CRF status flow use module-local records instead of `shared.bean.*` transport types.

**Exit Gate**
- `ImportCrfDataAdapter` no longer imports `shared.bean.*` packages except where an explicitly retained external compatibility contract still requires it.

### Workstream 3: Form Validation Compatibility Isolation

**Goal:** detach app-hosted validation/discrepancy code from the old shared utility and DTO stack.

**Primary Targets**
- `app/src/main/java/org/researchedc/control/form/Validator.java`
- `app/src/main/java/org/researchedc/control/form/FormDiscrepancyNotes.java`

**Approach**
- Introduce app-owned validation support types for locale, formatting, and field-state handling.
- Move shared localization and formatting dependencies behind app-owned wrappers.
- Replace shared bean assumptions where practical with narrower app/module contracts.

**Current Progress**
- `app/control/form/Validator` now resolves locale, bundles, date formats, and string/date checks through app-owned `control/form/support` classes instead of direct `shared/core` or `shared/i18n` imports.

**Exit Gate**
- `app/control/form/*` no longer depends directly on `shared/core/*` or `shared/i18n/*`.

### Workstream 4: Adapter DTO Contraction

**Goal:** reduce `shared.bean.*` from a broad compatibility model to a narrowly bounded adapter bridge.

**Primary Targets**
- `app/module/study/internal/adapter/StudyDaoAdapter`
- `app/module/subject/internal/adapter/StudySubjectDaoAdapter`
- `app/module/event/internal/adapter/StudyEventDaoAdapter`
- `app/module/event/internal/adapter/EventCrfDaoAdapter`
- `app/module/datacapture/internal/adapter/ItemDataDaoAdapter`
- `app/module/crf/internal/adapter/*`

**Approach**
- Add module-owned internal mappers/records.
- Keep legacy DTO conversion at the narrowest possible edge.
- Prefer repository/entity-to-module DTO conversion over legacy bean propagation.

**Exit Gate**
- No module/internal adapter exposes more legacy DTO types than strictly required by a retained compatibility contract.

### Workstream 5: Shared Domain Reduction

**Goal:** shrink `shared/domain` only after adapter and compatibility callers stop needing it.

**Priority Order**
1. `domain/technicaladmin`
2. `domain/crfdata`
3. `domain/rule`
4. `domain/datamap`

**Approach**
- Re-home technical/admin support types first.
- Audit each domain package for remaining active imports.
- Convert retained behavior to module-owned entities or module-local bridge types where feasible.
- Do not delete JPA mappings that are still needed by repositories, import compatibility, or schema sync code.

**Current Progress**
- Unused `domain/technicaladmin/ConfigurationBean` was removed after repository-wide caller scans confirmed no production references and no module repository binding.
- `DatabaseChangeLogBean` and its composite key were replaced by audit-module owned read-only JPA entities for the changelog API, removing another `shared/domain/technicaladmin` mapping from the shared surface.
- `AuditUserLoginBean` and `LoginStatus` were replaced by audit-module owned read-only login audit entities/enums, completing removal of `shared/domain/technicaladmin`.
- Verified package state: `shared/src/main/java/org/researchedc/domain/technicaladmin` has `0` Java files remaining.
- Unused `domain/crfdata/DynamicsItemFormMetadataBean` and `DynamicsItemGroupMetadataBean` mappings were removed after scans showed no active production callers, repositories, or retained runtime query paths.
- Retired Hibernate-era `domain/rule` mappings were removed after scans confirmed active rule workflows use module-owned `module_rule*` repositories and no Java callers still import `org.researchedc.domain.rule.*`.
- Retired `shared/bean/rule` DTOs and obsolete rule Castor mapping/template resources (`mapping*.xml`, `rules.xsd`, `rules_template*.xml`) were removed; `CoreResources` no longer copies retired rule import templates.
- Retired `shared/src/main/resources/properties/*_dao.xml` SQL mapping resources were removed after confirming active query loading uses `classpath:queries/<db>/**/*.properties` and no code loads the old XML files.
- Retired extract compatibility DTO/support classes (`ExtractPropertyBean` plus old post-processing function types) were removed; `CoreResources` no longer retains the unused extract-property static field.
- Retired dataset/filter extract DTOs (`DatasetBean`, `FilterBean`, `FilterObjectBean`) and their unused `DatasetItemStatus` enum-like term were removed after source/test scans showed no active app or module callers.
- Orphaned audit and study-parameter DTO beans (`bean/admin/Audit*`, `bean/service/StudyParameter*`) were removed after full import/instantiation scans confirmed no active callers.
- ODM import DTOs were narrowed to fields mapped by `cd_odm_mapping.xml`; unused audit/discrepancy/measurement-unit companion beans under `bean/odmbeans` were removed after import parser tests passed.
- Duplicate unreferenced JPA mappings for `measurement_unit` and `study_module_status` under `domain/admin` and `domain/managestudy` were removed.
- Unused `domain/datamap` mappings for retired DAO/SPI surfaces (`CrfVersionMedia`, `EventDefinitionCrfTag`, `MeasurementUnit`, `Tag`, and `VersioningMap` plus its embedded id) were removed after live Java caller scans found no remaining repositories, adapters, or entity relationships requiring them.
- Discrepancy-note DN link-table mappings (`Dn*Map` and `Dn*MapId`) were removed after confirming the only remaining references were uncalled Hibernate-era reverse collections on `DiscrepancyNote`; the active discrepancy-note module uses module-owned entities/repositories.
- Retired datamap `EventCrf` and `CompletionStatus` mappings were removed after confirming no app/shared code imports them; `ItemData` now keeps the `event_crf_id` column as a scalar compatibility field.
- Legacy subject-group datamap graph (`GroupClassTypes`, `StudyGroupClass`, `StudyGroup`, `SubjectGroupMap`) and unused `StudyModuleStatus` were removed after scans confirmed no live Java callers; active subject-group behavior uses module-owned `module_study_group*` entities/repositories.
- Retired datamap lookup mappings for `ItemReferenceType`, `ItemDataType`, and `ResponseType` were removed after converting retained compatibility entities to scalar id columns.

**Exit Gate**
- Each removed `shared/domain` file has zero production callers and zero repository/runtime mapping requirements.

### Workstream 6: Product Contract Cleanup

**Goal:** finish post-legacy product/schema cleanups that were intentionally deferred.

**Active Follow-Up**
- `docs/refactor/phase-1-email-field-removal-plan.md`

**Potential Additional Follow-Ups**
- import/export compatibility contract tightening
- OpenRosa compatibility boundary review
- residual legacy REST gateway reduction

**Exit Gate**
- Deferred compatibility fields and contracts are either removed, versioned, or explicitly retained.

## Non-Goals

- Reintroducing DAO SPI interfaces
- Recreating `web/` or JSP compatibility layers
- Deleting `shared/domain` entities just because they look legacy
- Modifying released Liquibase migrations retroactively

## Guardrails

1. New compatibility code must use module-owned ports, repositories, and app/module-local support types.
2. Do not add new code to `shared/` unless it is a temporary extraction waypoint with a documented removal reason.
3. Keep `legacy-workflow-inventory.{md,csv}` and `phase-3-dao-replacement-ledger.{md,csv}` as evidence, not planning surfaces.
4. Prefer one active roadmap and small focused follow-up plans over multiple competing master plans.

## Verification

Run after each roadmap slice:

```bash
git status --short
bash scripts/ci/check-legacy-guardrails.sh
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
cd frontend && pnpm typecheck
```

Add targeted backend/frontend tests whenever a compatibility boundary is narrowed.
