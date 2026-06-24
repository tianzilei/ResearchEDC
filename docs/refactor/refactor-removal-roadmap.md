# Refactor And Removal Roadmap

**Updated:** 2026-06-24
**Purpose:** single source of truth for the remaining legacy refactor/removal work.

## Current Verified State

- Legacy workflow inventory: `963/963` closed (`100.0%`)
- DAO SPI ledger: `878/878` removed (`100.0%`)
- `shared/dao`: `0` files remain
- `web/`: deleted
- `ws/`: absent
- JSP surface: `0` files
- Remaining `shared/` Java surface: `0` files
- Modulith Java surface: `397` files
- Code balance by file count: `0%` shared legacy / `100%` module modern

This means the workflow-level deletion program is complete. The remaining work is **compatibility strangulation inside `app/` and `shared/`**, not more `web/` or DAO SPI cleanup. With `shared/src/main/java` now at 0 Java files, the strangulation is effectively complete — only resource-only files remain in `shared/`.

## Post-Hardening Stabilization (2026-06-24)

Build stabilization complete. Changes:
- DBCP1 DataSource → Spring Boot HikariCP auto-config
- Quartz scheduler infrastructure removed (5 files + config, dead code)
- Joda-Time → java.time in AuditUserLoginAdapter
- QueryStore removed (zero consumers)
- ODM export namespace handling corrected (oc: prefix, xsi:schemaLocation, xmlns declarations)
- ExportDataProviderAdapter Modulith boundary fixed (allowedDependencies for 5 modules)
- OdmExportExecutionServiceTest IOException fixed

Verified baseline:
```bash
mvn clean compile -DskipTests                          # ✅ BUILD SUCCESS
mvn test -pl app -am -Dtest=ModulithVerificationTest   # ✅ 1/0/0
mvn test -pl app -am -Dtest=OdmExportGeneratorTest     # ✅ 6/0/0
```

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
| `shared/bean` | 0 | Retired; all DTOs migrated to module-owned DTOs |
| `shared/domain` | 0 | Retired; active mappings live in module-owned entities/repositories |
| `shared/core` | 0 | Retired; app-owned config loads retained property resources |
| `shared/i18n` | 0 | Retired; resource files remain, Java helper removed |
| `shared/exception` | 0 | Retired with the final shared core support |

### Highest-Weight Callers

| Caller | Why It Matters |
|---|---|
| (none) | All `shared/bean` DTOs have been retired; module adapters now use module-owned DTOs |

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
- `app/config/DbConfig` and `ExtendedBasicDataSource` have been removed; DataSource is now managed by Spring Boot HikariCP auto-config via `spring.datasource.*` properties. The legacy `QueryStore` (zero consumers) was also removed.
- Retained `datainfo.properties` loading now lives in app-owned `CoreResourcesConfig`, allowing the final `shared/core` and `shared/exception` Java support classes to be retired.
- Retained term/admin bundle lookups now use direct `ResourceBundle` access inside the remaining DTO/term beans, allowing the final `shared/i18n` Java helper to be retired.
- Retired extract post-processing helpers (`Processing*`, `Pdf/Sas/SqlProcessingFunction`, `ScriptRunner`) were removed after zero-caller scans and compile/test verification.
- The unused `shared/core/util/ItemGroupCrvVersionUtil` view-helper residue was removed; no `shared/core/util` Java callers remain.
- Zero-caller shared support residues (`i18n/core/LocaleResolver`, `i18n/util/I18nFormatUtil`, and `exception/OpenClinicaException`) were removed after source/resource scans confirmed no app, test, shared, or resource callers.
- Data-capture attachment storage now resolves `attached_file_location` through module-owned support, allowing the retired `shared/bean/core/Utils` path helper and the final `shared/core/form/StringUtil` caller to be removed.
- Retired no-caller DTO residue for discrepancy-note display, item-group metadata display, and section display paths was removed; `FormDiscrepancyNotes`, `StudyEventBean`, `ItemGroupBean`, and `ItemDataDaoAdapter` no longer keep those legacy bean types alive.
- Form-validation-only controlled vocabulary types (`EntityAction`, `TermType`, and `NumericComparisonOperator`) now live under app-owned `control/form/support`, and the no-caller `Privilege` term plus retired package Javadoc stubs were removed from `shared`.

**Exit Gate**
- `shared/core`, `shared/exception`, and `shared/i18n` stay at `0` Java files.

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
  - `shared.bean.login.*`
- Preserve current upload/validate/commit behavior while replacing legacy DTO plumbing.

**Current Progress**
- ODM mapping resource lookup and page-message bundle resolution now live in `module/dataimport/internal/support`, so `ImportCrfDataAdapter` no longer depends directly on shared support classes while preserving the current import validation flow.
- ODM import POJOs and the Castor mapping now live under `module/dataimport/internal/odm`, so the retained ODM parser no longer keeps `shared.bean.submit.crfdata.*` alive.
- `ImportCrfDataAdapter` now keeps ODM beans only at its retained dataimport-owned compatibility boundary; internal study/subject/event/CRF lookup and event-CRF status flow use module-local records instead of `shared.bean.*` transport types.
- Data-import lookup/event-CRF/item metadata ports now return typed module-owned records instead of positional `Object[]` rows, removing raw row plumbing from `ImportCrfDataAdapter` and its study, subject, event, CRF, and data-capture adapters.
- `ImportCrfDataAdapter` now uses app-owned response-set validation support and local compatibility status ids, so the adapter no longer imports `shared.bean.*` directly for edit-check or stage validation behavior.

**Exit Gate**
- ✅ `ImportCrfDataAdapter` no longer imports `shared.bean.*` packages.

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
- `Validator` now uses app-owned form term/comparison support for `IS_VALID_TERM` and numeric comparison validation, removing the former shared form-validation-only term classes from `shared/bean/core`.
- `Validator` now uses app-owned response-set and status validation support, removing its last direct `shared.bean.*` imports from form validation.

**Exit Gate**
- ✅ `app/control/form/*` no longer depends on `shared/core/*` or `shared/i18n/*`.

### Workstream 4: Adapter DTO Contraction

**Goal:** reduce `shared.bean.*` from a broad compatibility model to a narrowly bounded adapter bridge.

**Status:** ✅ COMPLETE — all `shared/bean` DTOs retired; module adapters use module-owned DTOs.

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

**Current Progress**
- Retained submit DTOs were trimmed for no-caller compatibility residue: `ItemDataBean` no longer carries obsolete `selected`/`auditLog` equality state, `ItemBean` no longer carries retired dataset/definition display fields, `CRFVersionBean` no longer carries unused `date_created`/download/Enketo residue, `ItemGroupBean` no longer carries retired display collections, and `ResponseSetBean` no longer keeps an unused option-index cache.
- `ItemFormMetadataBean` no longer carries no-reader CRF/group/section display residue, conditional-display/highlight fields, or its self-contained legacy equality/hash snapshot; `ItemFormMetadataDaoAdapter` no longer joins CRF, group, and section tables only to populate those retired bean fields.
- Retained study/event DTOs were narrowed further: `StudySubjectBean` no longer carries subject display/group/timezone residue, `StudyEventBean` no longer carries event list/repeat/display/UI residue, and `EventCRFBean` no longer carries display-only names, event ordinal, or obsolete validation/completion residue.
- `StudyEventBean` and `EventCRFBean` no longer carry no-caller legacy stage-display helpers; data-import compatibility now uses its module-local status-to-stage calculation directly.
- `StudyBean` no longer carries no-caller JSP-era study-parameter state, parent-study display names, or redundant genetic/string type accessors; retained compatibility uses the mapped `typeId` contract only.
- `AuditableEntityBean` no longer carries no-caller object owner/updater references or reflective `getId()` extraction; retained audit compatibility uses scalar owner/updater ids only.
- Retained submit/event-definition DTOs no longer carry self-contained no-caller equality/hash snapshots, and `Status`/`EntityBean` no longer carry unused helper/comment residue.
- `StudyBean` no longer carries no-caller deprecated status-id accessors, `Status` no longer keeps typed equality/list-conversion residue, and `ItemGroupBean` no longer redeclares redundant serialization.
- `ItemDataDaoAdapter` no longer carries no-caller legacy DAO compatibility stubs for retired item-data view/date/permission/key/status helper paths.
- `ItemDaoAdapter` no longer carries no-caller legacy CRF/item DAO compatibility stubs for retired parent/group/permission/required-item helper paths, and the orphan `ItemGroupCrfVersionView` helper was removed.
- `StudyEventDefinitionBean` no longer carries retired CRF list/count, lock/populated UI flags, or default-version matrix residue.
- `Status` no longer carries no-caller JSP-era dropdown/list caches or the redundant status-id name map.
- `StudyDaoAdapter` no longer exposes zero-caller legacy DAO stubs for retired sorted search, permission-filtered study lists, limit filtering, parent-child map assembly, or test-only deletion paths.
- `ItemGroupDaoAdapter` no longer exposes zero-caller legacy DAO stubs for retired type registration, sorted/permission-filtered lists, OID generation, test-only deletion, or repeating-group helper paths.
- `StudySubjectDaoAdapter` no longer exposes zero-caller legacy DAO stubs for retired sorted/permission-filtered lists, with-event subject lists, subject-group lookup, or CRF-migration count paths.
- `ItemFormMetadataDaoAdapter` no longer exposes zero-caller legacy DAO stubs for retired type registration, sorted/permission-filtered lists, hidden-item counts, or item/group metadata fallback lookup paths.
- All `shared/bean` DTOs have been retired; module adapters now use module-owned DTOs under `app/module/*/dto/`. The `shared/src/main/java` directory contains 0 Java files.

**Exit Gate**
- ✅ No module/internal adapter exposes more legacy DTO types than strictly required by a retained compatibility contract.

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
- Remaining `domain/crfdata` SCD/instant-on-change compatibility models were moved behind CRF module-local adapter records, completing removal of `shared/domain/crfdata`.
- Retired Hibernate-era `domain/rule` mappings were removed after scans confirmed active rule workflows use module-owned `module_rule*` repositories and no Java callers still import `org.researchedc.domain.rule.*`.
- Retired `shared/bean/rule` DTOs and obsolete rule Castor mapping/template resources (`mapping*.xml`, `rules.xsd`, `rules_template*.xml`) were removed; `CoreResources` no longer copies retired rule import templates.
- Retired `shared/src/main/resources/properties/*_dao.xml` SQL mapping resources were removed after confirming active query loading uses `classpath:queries/<db>/**/*.properties` and no code loads the old XML files.
- Retired extract compatibility DTO/support classes (`ExtractPropertyBean` plus old post-processing function types) were removed; `CoreResources` no longer retains the unused extract-property static field.
- Retired dataset/filter extract DTOs (`DatasetBean`, `FilterBean`, `FilterObjectBean`) and their unused `DatasetItemStatus` enum-like term were removed after source/test scans showed no active app or module callers.
- Orphaned audit and study-parameter DTO beans (`bean/admin/Audit*`, `bean/service/StudyParameter*`) were removed after full import/instantiation scans confirmed no active callers.
- Retired data-entry/import display DTOs (`DisplayItemBean*`, `EventDefinitionCRFBean`, `SubjectBean`) and unused subject-group DTO/term types (`StudyGroup*Bean`, `GroupClassType`) were removed after source and resource scans confirmed no active app/shared callers; module-owned CRF, subject, and subject-group entities remain the runtime paths.
- ODM import DTOs were narrowed to fields mapped by `cd_odm_mapping.xml`; unused audit/discrepancy/measurement-unit companion beans under `bean/odmbeans` were removed after import parser tests passed.
- Duplicate unreferenced JPA mappings for `measurement_unit` and `study_module_status` under `domain/admin` and `domain/managestudy` were removed.
- Unused `domain/datamap` mappings for retired DAO/SPI surfaces (`CrfVersionMedia`, `EventDefinitionCrfTag`, `MeasurementUnit`, `Tag`, and `VersioningMap` plus its embedded id) were removed after live Java caller scans found no remaining repositories, adapters, or entity relationships requiring them.
- Discrepancy-note DN link-table mappings (`Dn*Map` and `Dn*MapId`) were removed after confirming the only remaining references were uncalled Hibernate-era reverse collections on `DiscrepancyNote`; the active discrepancy-note module uses module-owned entities/repositories.
- Retired datamap `EventCrf` and `CompletionStatus` mappings were removed after confirming no app/shared code imports them; `ItemData` now keeps the `event_crf_id` column as a scalar compatibility field.
- Legacy subject-group datamap graph (`GroupClassTypes`, `StudyGroupClass`, `StudyGroup`, `SubjectGroupMap`) and unused `StudyModuleStatus` were removed after scans confirmed no live Java callers; active subject-group behavior uses module-owned `module_study_group*` entities/repositories.
- Retired datamap lookup mappings for `ItemReferenceType`, `ItemDataType`, and `ResponseType` were removed after converting retained compatibility entities to scalar id columns.
- Retired datamap `StudyParameter` and `StudyParameterValue` mappings were removed after scans confirmed they were only referenced by an unused `Study` constructor field path; active study configuration uses module-owned adapters/repositories.
- Retired datamap `ItemGroupMetadata` mapping was removed after scans confirmed no direct Java callers; active item-group metadata access uses the module-owned `module_item_group_metadata` entity/repository while compatibility DTO paths retain `ItemGroupMetadataBean`.
- Zero-caller shared domain residues `AuthoritiesBean`, `AuditableMutableDomainObject`, and `AbstractAuditableMutableDomainObject` were removed after source scans confirmed only historical ledger text referenced them.
- The `SourceDataVerification` enum was removed with its only Java caller, the retired `EventDefinitionCRFBean`; ODM XSD schema attributes remain resource-only and do not depend on the Java enum.
- Retired compatibility term residues `NullValue` and `EventCRFStatus` were removed after scans confirmed no active Java callers; ODM import `FormDataBean.EventCRFStatus` remains a string field mapped by `cd_odm_mapping.xml`.
- Retired discrepancy-note datamap graph (`DiscrepancyNote`, `DiscrepancyNoteType`, and `ResolutionStatus`) was removed after package-qualified scans confirmed no active Java callers outside the self-contained old mapping cycle; active discrepancy-note behavior uses module-owned scalar fields and `module_discrepancy_note` entities.
- Retired study-user-role datamap composite mapping (`StudyUserRole` and `StudyUserRoleId`) was removed after scans showed only dead reverse fields in `Study`/`UserAccount` referenced it; active identity behavior uses module-owned `module_study_user_role` / `RoleEntity`.
- Retired shared domain base infrastructure (`DomainObject`, `MutableDomainObject`, `CompositeIdDomainObject`, `AbstractMutableDomainObject`, and package-level generator declarations) was removed after scans confirmed no active callers or subclasses; retained datamap entities have class-local identifiers/generators and now use `DataMapDomainObject` only as a serializable marker base.
- Retired event/subject datamap mappings (`EventDefinitionCrf`, `StudyEvent`, and `StudySubject`) were removed after scans showed only stale imports and reverse collections referenced them; active event, subject, and event-definition CRF behavior uses module-owned entities/repositories.
- The final retired shared domain graph was removed after `ResponseSetDaoAdapter` was narrowed to the module-owned `ImportResponseSetPort`, `EventCRFBean` dropped its no-caller next-generation status field, and app/Hibernate entity scanning was narrowed to `org.researchedc.module`; `shared/src/main/java/org/researchedc/domain` now has `0` Java files.

**Exit Gate**
- `shared/domain` stays at `0` Java files; new persistence mappings belong in module-owned entity packages.

### Workstream 6: Product Contract Cleanup

**Goal:** finish post-legacy product/schema cleanups that were intentionally deferred.

**Active Follow-Up**
- `docs/refactor/phase-1-email-field-removal-plan.md`
- `docs/refactor/phase-1-email-contract-versioning-plan.md`

**Current Progress**
- Email-field cleanup now includes a forward migration that retires historical mail-event/rule-email storage and PostgreSQL write-boundary triggers that keep retained user/study compatibility email columns inert.
- Runtime rule XSD no longer exposes the retired `EmailAction` contract; retained rule compatibility now covers non-email actions only.
- App/shared Java entities no longer map or expose retired user-account email or study facility-contact-email compatibility fields; retained surfaces are now database/ODM compatibility only.
- ODM contract versioning: OC2-0 frozen as compatibility-only (deprecated `FacilityContactEmail` retained), OC2-1 email-free schema family introduced. `OdmContractVersion` enum and `OdmSchemaResourceResolver` added to export module. Guardrail tests verify both schema families. ExportJob entity now carries `odm_contract_version` column defaulting to `OC2_1`.
- ODM export execution pipeline: `OdmExportGenerator` builds ODM 1.3.0 XML, `ExportArtifactWriter` persists files, `OdmExportExecutionService` orchestrates state transitions (PENDING → RUNNING → COMPLETED/FAILED). `ExportDataProviderAdapter` bridges study/subject/event/CRF/item data from module repositories. Download endpoint at `GET /api/v1/exports/{id}/download`. Frontend lint restored to 0 errors.

**Potential Additional Follow-Ups**
- ~~import/export compatibility contract tightening~~ ✅ — import/export modules have 0 shared Java dependencies
- ~~OpenRosa compatibility boundary review~~ ✅ — clean active product module, 0 shared/legacy references
- ~~residual legacy REST gateway reduction~~ ✅ — gateway actively used by SPA frontend (20+ frontend API calls), cannot remove

**Exit Gate**
- Deferred compatibility fields and contracts are either removed, versioned, or explicitly retained.

## Non-Goals

- Reintroducing DAO SPI interfaces
- Recreating `web/` or JSP compatibility layers
- Reintroducing shared-domain Hibernate mappings
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
