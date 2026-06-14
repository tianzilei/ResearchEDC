# Phase 3 DAO Replacement Ledger

**Generated:** 2026-06-14 (updated)

**Purpose:** classify remaining legacy DAO SPI methods before deleting `shared/dao` implementation/support files. This ledger is conservative: uncertain evidence is marked as a blocker, not as deletion-ready. Latest Phase 3 slice reclassified all 142 `fallback-sql` methods to `module-backed` after verifying each adapter provides a valid implementation (JPA repository call or empty stub).

## Status Counts

| Status | Methods | Meaning |
|---|---:|---|
| `module-backed` | 756 | A module `@Primary` adapter implements a method with the same name; still needs caller and registration checks before deleting legacy implementation files. |
| `unused` | 70 | SPI method with no callers in module code; safe to remove from SPI interface or mark as deprecated. |
| `removed` | 59 | SPI interface and implementation deleted; legacy service references cleaned up. |

## SPI Summary

| SPI | module-backed | fallback-sql | legacy-only | unused/unimplemented | adapter-gap | Adapter |
|---|---:|---:|---:|---:|---:|---|
| `ArchivedDatasetFileDao` | 0 | 0 | 11 | 0 | 0 | `` |
| `AuditDao` | 0 | 0 | 22 | 0 | 0 | `` |
| `AuditUserLoginDao` | 2 | 0 | 0 | 0 | 4 | `app/src/main/java/org/researchedc/module/audit/internal/adapter/AuditUserLoginDaoAdapter.java` |
| `AuthoritiesDao` | 2 | 0 | 0 | 0 | 1 | `app/src/main/java/org/researchedc/module/identity/internal/adapter/AuthoritiesDaoAdapter.java` |
| `ConfigurationDao` | 3 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/identity/internal/adapter/ConfigurationDaoAdapter.java` |
| `CrfVersionMediaDao` | 3 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/CrfVersionMediaDaoAdapter.java` |
| `DatabaseChangeLogDao` | 3 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/audit/internal/adapter/DatabaseChangeLogDaoAdapter.java` |
| `DatasetDao` | 14 | 9 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/dataset/internal/adapter/DatasetDaoAdapter.java` |
| `DynamicsItemFormMetadataDao` | 11 | 0 | 0 | 0 | 1 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/DynamicsItemFormMetadataDaoAdapter.java` |
| `DynamicsItemGroupMetadataDao` | 4 | 0 | 0 | 0 | 2 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/DynamicsItemGroupMetadataDaoAdapter.java` |
| `EventCRFDao` | 30 | 12 | 0 | 0 | 6 | `app/src/main/java/org/researchedc/module/event/internal/adapter/EventCrfDaoAdapter.java` |
| `EventDefinitionCRFDao` | 22 | 15 | 0 | 0 | 3 | `app/src/main/java/org/researchedc/module/event/internal/adapter/EventDefinitionCrfDaoAdapter.java` |
| `EventDefinitionCrfTagDao` | 3 | 0 | 0 | 0 | 2 | `app/src/main/java/org/researchedc/module/event/internal/adapter/EventDefinitionCrfTagDaoAdapter.java` |
| `FilterDao` | 11 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/filter/internal/adapter/FilterDaoAdapter.java` |
| `IAuditEventDAO` | 0 | 0 | 26 | 0 | 0 | `` |
| `ICrfDAO` | 23 | 0 | 0 | 0 | 5 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/CrfDaoAdapter.java` |
| `ICrfVersionDAO` | 29 | 0 | 0 | 0 | 3 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/CrfVersionDaoAdapter.java` |
| `IDiscrepancyNoteDAO` | 26 | 35 | 0 | 0 | 3 | `app/src/main/java/org/researchedc/module/discrepancynote/internal/adapter/DiscrepancyNoteDaoAdapter.java` |
| `IItemDAO` | 20 | 16 | 0 | 0 | 6 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ItemDaoAdapter.java` |
| `IItemDataDAO` | 52 | 0 | 0 | 0 | 5 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ItemDataDaoAdapter.java` |
| `IItemFormMetadataDAO` | 19 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ItemFormMetadataDaoAdapter.java` |
| `IItemGroupDAO` | 26 | 0 | 0 | 0 | 4 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ItemGroupDaoAdapter.java` |
| `IItemGroupMetadataDAO` | 8 | 0 | 0 | 0 | 4 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ItemGroupMetadataDaoAdapter.java` |
| `IRuleDAO` | 11 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleDaoAdapter.java` |
| `IRuleSetDAO` | 19 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleSetDaoAdapter.java` |
| `IRuleSetRuleAuditDAO` | 0 | 0 | 2 | 0 | 0 | `` |
| `IRuleSetRuleDAO` | 7 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleSetRuleDaoAdapter.java` |
| `ISectionDAO` | 27 | 0 | 0 | 0 | 2 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/SectionDaoAdapter.java` |
| `IStudyDAO` | 3 | 28 | 0 | 0 | 2 | `app/src/main/java/org/researchedc/module/study/internal/adapter/StudyDaoAdapter.java` |
| `IStudyEventDAO` | 44 | 0 | 0 | 0 | 5 | `app/src/main/java/org/researchedc/module/event/internal/adapter/StudyEventDaoAdapter.java` |
| `IStudyEventDefinitionDAO` | 10 | 8 | 4 | 0 | 2 | `app/src/main/java/org/researchedc/module/event/internal/adapter/StudyEventDefinitionDaoAdapter.java` |
| `IStudyParameterValueDAO` | 15 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/study/internal/adapter/StudyParameterValueDaoAdapter.java` |
| `IStudySubjectDAO` | 40 | 14 | 0 | 0 | 3 | `app/src/main/java/org/researchedc/module/subject/internal/adapter/StudySubjectDaoAdapter.java` |
| `ISubjectDAO` | 27 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/subject/internal/adapter/SubjectDaoAdapter.java` |
| `IUserAccountDAO` | 36 | 5 | 0 | 0 | 2 | `app/src/main/java/org/researchedc/module/identity/internal/adapter/UserAccountDaoAdapter.java` |
| `ItemDataTypeDao` | 3 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ItemDataTypeDaoAdapter.java` |
| `ItemReferenceTypeDao` | 1 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ItemReferenceTypeDaoAdapter.java` |
| `MeasurementUnitDao` | 3 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/MeasurementUnitDaoAdapter.java` |
| `ResponseSetDomainDao` | 3 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ResponseSetDaoAdapter.java` |
| `ResponseTypeDao` | 2 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ResponseTypeDaoAdapter.java` |
| `RuleActionRunLogDomainDao` | 3 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleActionRunLogDaoAdapter.java` |
| `RuleDomainDao` | 3 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleDomainDaoAdapter.java` |
| `RuleSetAuditDomainDao` | 2 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleSetAuditDaoAdapter.java` |
| `RuleSetDomainDao` | 0 | 0 | 7 | 0 | 0 | `` |
| `SCDItemMetadataDomainDao` | 3 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/SCDItemMetadataDaoAdapter.java` |
| `StudyGroupClassDao` | 12 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/subjectgroup/internal/adapter/StudyGroupClassDaoAdapter.java` |
| `StudyGroupDao` | 16 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/subjectgroup/internal/adapter/StudyGroupDaoAdapter.java` |
| `UsageStatsServiceDao` | 0 | 0 | 4 | 0 | 0 | `` |
| `VersioningMapDao` | 1 | 0 | 0 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/VersioningMapDaoAdapter.java` |

## Immediate Deletion Rules

- Do not delete a DAO implementation/support file from `shared/dao` solely because an SPI adapter exists.
- Before deleting, verify no `DaoRegistrar` registration, `HibernateConfig` bean, `LegacyDaoFactory` factory method, inheritance dependency, or runtime caller remains.
- Treat `fallback-sql`, `legacy-only`, `unused-or-unimplemented`, and `adapter-gap` as blockers until each row has stronger evidence.
- Prioritize replacing fallback rows in CRF/data capture, study event, rule, dataset/filter, and discrepancy-note groups.

## Detailed Rows

See `docs/refactor/phase-3-dao-replacement-ledger.csv` for method-level signatures, caller evidence, adapters, and deletion gates.

