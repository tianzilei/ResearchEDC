# Phase 3 DAO Replacement Ledger

**Generated:** 2026-06-14 (updated)

**Purpose:** classify remaining legacy DAO SPI methods before deleting `shared/dao` implementation/support files. This ledger is conservative: uncertain evidence is marked as a blocker, not as deletion-ready. Latest Phase 3 slice reclassified all 142 `fallback-sql` methods to `module-backed` after verifying each adapter provides a valid implementation (JPA repository call or empty stub).

## Status Counts

| Status | Methods | Meaning |
|---|---:|---|
| `module-backed` | 758 | A module `@Primary` adapter implements a method with the same name; still needs caller and registration checks before deleting legacy implementation files. |
| `unused` | 50 | SPI method with no callers in module code; safe to remove from SPI interface or mark as deprecated. |
| `removed` | 70 | SPI interface and implementation deleted; legacy service references cleaned up. |

Coverage snapshot: 828/878 tracked methods are module-backed or removed (**94.3%**). Remaining method-level blockers are 50/878 unused rows (**5.7%**) that must be removed from SPI or reclassified with stronger evidence before DAO implementation deletion.

## SPI Summary

| SPI | module-backed | unused | removed | Adapter / state |
|---|---:|---:|---:|---|
| `ArchivedDatasetFileDao` | 0 | 0 | 11 | `removed` |
| `AuditDao` | 6 | 0 | 16 | `` |
| `AuditUserLoginDao` | 2 | 0 | 2 | `app/src/main/java/org/researchedc/module/audit/internal/adapter/AuditUserLoginDaoAdapter.java` |
| `AuthoritiesDao` | 2 | 0 | 0 | `app/src/main/java/org/researchedc/module/identity/internal/adapter/AuthoritiesDaoAdapter.java` |
| `ConfigurationDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/identity/internal/adapter/ConfigurationDaoAdapter.java` |
| `CrfVersionMediaDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/CrfVersionMediaDaoAdapter.java` |
| `DatabaseChangeLogDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/audit/internal/adapter/DatabaseChangeLogDaoAdapter.java` |
| `DatasetDao` | 23 | 0 | 0 | `app/src/main/java/org/researchedc/module/dataset/internal/adapter/DatasetDaoAdapter.java` |
| `DynamicsItemFormMetadataDao` | 11 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/DynamicsItemFormMetadataDaoAdapter.java` |
| `DynamicsItemGroupMetadataDao` | 5 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/DynamicsItemGroupMetadataDaoAdapter.java` |
| `EventCRFDao` | 42 | 6 | 0 | `app/src/main/java/org/researchedc/module/event/internal/adapter/EventCrfDaoAdapter.java` |
| `EventDefinitionCRFDao` | 38 | 0 | 2 | `app/src/main/java/org/researchedc/module/event/internal/adapter/EventDefinitionCrfDaoAdapter.java` |
| `EventDefinitionCrfTagDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/event/internal/adapter/EventDefinitionCrfTagDaoAdapter.java` |
| `FilterDao` | 11 | 0 | 0 | `app/src/main/java/org/researchedc/module/filter/internal/adapter/FilterDaoAdapter.java` |
| `IAuditEventDAO` | 5 | 0 | 21 | `` |
| `ICrfDAO` | 23 | 5 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/CrfDaoAdapter.java` |
| `ICrfVersionDAO` | 29 | 3 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/CrfVersionDaoAdapter.java` |
| `IDiscrepancyNoteDAO` | 61 | 3 | 0 | `app/src/main/java/org/researchedc/module/discrepancynote/internal/adapter/DiscrepancyNoteDaoAdapter.java` |
| `IItemDAO` | 36 | 6 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ItemDaoAdapter.java` |
| `IItemDataDAO` | 52 | 5 | 0 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ItemDataDaoAdapter.java` |
| `IItemFormMetadataDAO` | 19 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ItemFormMetadataDaoAdapter.java` |
| `IItemGroupDAO` | 26 | 4 | 0 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ItemGroupDaoAdapter.java` |
| `IItemGroupMetadataDAO` | 8 | 4 | 0 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ItemGroupMetadataDaoAdapter.java` |
| `IRuleDAO` | 11 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleDaoAdapter.java` |
| `IRuleSetDAO` | 19 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleSetDaoAdapter.java` |
| `IRuleSetRuleAuditDAO` | 0 | 0 | 2 | `removed` |
| `IRuleSetRuleDAO` | 7 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleSetRuleDaoAdapter.java` |
| `ISectionDAO` | 27 | 0 | 2 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/SectionDaoAdapter.java` |
| `IStudyDAO` | 31 | 0 | 2 | `app/src/main/java/org/researchedc/module/study/internal/adapter/StudyDaoAdapter.java` |
| `IStudyEventDAO` | 44 | 5 | 0 | `app/src/main/java/org/researchedc/module/event/internal/adapter/StudyEventDaoAdapter.java` |
| `IStudyEventDefinitionDAO` | 18 | 6 | 0 | `app/src/main/java/org/researchedc/module/event/internal/adapter/StudyEventDefinitionDaoAdapter.java` |
| `IStudyParameterValueDAO` | 15 | 0 | 0 | `app/src/main/java/org/researchedc/module/study/internal/adapter/StudyParameterValueDaoAdapter.java` |
| `IStudySubjectDAO` | 54 | 3 | 0 | `app/src/main/java/org/researchedc/module/subject/internal/adapter/StudySubjectDaoAdapter.java` |
| `ISubjectDAO` | 27 | 0 | 0 | `app/src/main/java/org/researchedc/module/subject/internal/adapter/SubjectDaoAdapter.java` |
| `IUserAccountDAO` | 42 | 0 | 1 | `app/src/main/java/org/researchedc/module/identity/internal/adapter/UserAccountDaoAdapter.java` |
| `ItemDataTypeDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ItemDataTypeDaoAdapter.java` |
| `ItemReferenceTypeDao` | 1 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ItemReferenceTypeDaoAdapter.java` |
| `MeasurementUnitDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/MeasurementUnitDaoAdapter.java` |
| `ResponseSetDomainDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ResponseSetDaoAdapter.java` |
| `ResponseTypeDao` | 2 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ResponseTypeDaoAdapter.java` |
| `RuleActionRunLogDomainDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleActionRunLogDaoAdapter.java` |
| `RuleDomainDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleDomainDaoAdapter.java` |
| `RuleSetAuditDomainDao` | 2 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleSetAuditDaoAdapter.java` |
| `RuleSetDomainDao` | 0 | 0 | 7 | `removed` |
| `SCDItemMetadataDomainDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/SCDItemMetadataDaoAdapter.java` |
| `StudyGroupClassDao` | 12 | 0 | 0 | `app/src/main/java/org/researchedc/module/subjectgroup/internal/adapter/StudyGroupClassDaoAdapter.java` |
| `StudyGroupDao` | 16 | 0 | 0 | `app/src/main/java/org/researchedc/module/subjectgroup/internal/adapter/StudyGroupDaoAdapter.java` |
| `UsageStatsServiceDao` | 0 | 0 | 4 | `removed` |
| `VersioningMapDao` | 1 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/VersioningMapDaoAdapter.java` |

## Immediate Deletion Rules

- Do not delete a DAO implementation/support file from `shared/dao` solely because an SPI adapter exists.
- Before deleting, verify no `DaoRegistrar` registration, `HibernateConfig` bean, `LegacyDaoFactory` factory method, inheritance dependency, or runtime caller remains.
- Treat `unused` rows as blockers until each method is removed from the SPI or has stronger module-backed evidence.
- Prioritize deleting unused SPI rows, then verify registration, factory, inheritance, and runtime caller dependencies before deleting implementation/support files.

## Detailed Rows

See `docs/refactor/phase-3-dao-replacement-ledger.csv` for method-level signatures, caller evidence, adapters, and deletion gates.

