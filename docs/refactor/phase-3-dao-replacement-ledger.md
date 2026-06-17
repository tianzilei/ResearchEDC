# Phase 3 DAO Replacement Ledger

**Generated:** 2026-06-14 (updated)

**Purpose:** classify remaining legacy DAO SPI methods before deleting or replacing `shared/dao` SPI interfaces. This ledger is conservative: uncertain evidence is marked as a blocker, not as deletion-ready. Latest Phase 3 slice reclassified all 142 `fallback-sql` methods to `module-backed` after verifying each adapter provides a valid implementation (JPA repository call or empty stub).

## Status Counts

| Status | Methods | Meaning |
|---|---:|---|
| `module-backed` | 720 | A module adapter or service path implements the method; still needs caller migration before deleting legacy SPI interfaces. |
| `unused` | 0 | SPI method with no callers in module code; safe to remove from SPI interface or mark as deprecated. |
| `removed` | 158 | SPI contract deleted or removed from the ledger; legacy service references cleaned up. |

Coverage snapshot: 720/878 tracked methods are module-backed and 158/878 are removed; 878/878 tracked methods are module-backed or removed (**100.0%**). No method-level blockers remain; DAO SPI deletion now depends on migrating callers from legacy SPI names to module-owned ports.

## SPI Summary

| SPI | module-backed | unused | removed | Adapter / state |
|---|---:|---:|---:|---|
| `ArchivedDatasetFileDao` | 0 | 0 | 11 | `removed` |
| `AuditDao` | 6 | 0 | 16 | `` |
| `AuditUserLoginDao` | 0 | 0 | 4 | `removed` |
| `AuthoritiesDao` | 2 | 0 | 0 | `app/src/main/java/org/researchedc/module/identity/internal/adapter/AuthoritiesDaoAdapter.java` |
| `ConfigurationDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/identity/internal/adapter/ConfigurationDaoAdapter.java` |
| `CrfVersionMediaDao` | 0 | 0 | 3 | `removed` |
| `DatabaseChangeLogDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/audit/internal/adapter/DatabaseChangeLogDaoAdapter.java` |
| `DatasetDao` | 23 | 0 | 0 | `app/src/main/java/org/researchedc/module/dataset/internal/adapter/DatasetDaoAdapter.java` |
| `DynamicsItemFormMetadataDao` | 11 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/DynamicsItemFormMetadataDaoAdapter.java` |
| `DynamicsItemGroupMetadataDao` | 5 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/DynamicsItemGroupMetadataDaoAdapter.java` |
| `EventCRFDao` | 40 | 0 | 8 | `app/src/main/java/org/researchedc/module/event/internal/adapter/EventCrfDaoAdapter.java` |
| `EventDefinitionCRFDao` | 38 | 0 | 2 | `app/src/main/java/org/researchedc/module/event/internal/adapter/EventDefinitionCrfDaoAdapter.java` |
| `EventDefinitionCrfTagDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/event/internal/adapter/EventDefinitionCrfTagDaoAdapter.java` |
| `FilterDao` | 11 | 0 | 0 | `app/src/main/java/org/researchedc/module/filter/internal/adapter/FilterDaoAdapter.java` |
| `IAuditEventDAO` | 5 | 0 | 21 | `` |
| `ICrfDAO` | 23 | 0 | 5 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/CrfDaoAdapter.java` |
| `ICrfVersionDAO` | 29 | 0 | 3 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/CrfVersionDaoAdapter.java` |
| `IDiscrepancyNoteDAO` | 53 | 0 | 11 | `app/src/main/java/org/researchedc/module/discrepancynote/internal/adapter/DiscrepancyNoteDaoAdapter.java` |
| `IItemDAO` | 36 | 0 | 6 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ItemDaoAdapter.java` |
| `IItemDataDAO` | 52 | 0 | 5 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ItemDataDaoAdapter.java` |
| `IItemFormMetadataDAO` | 19 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ItemFormMetadataDaoAdapter.java` |
| `IItemGroupDAO` | 26 | 0 | 4 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ItemGroupDaoAdapter.java` |
| `IItemGroupMetadataDAO` | 8 | 0 | 4 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ItemGroupMetadataDaoAdapter.java` |
| `IRuleDAO` | 11 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleDaoAdapter.java` |
| `IRuleSetDAO` | 19 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleSetDaoAdapter.java` |
| `IRuleSetRuleAuditDAO` | 0 | 0 | 2 | `removed` |
| `IRuleSetRuleDAO` | 5 | 0 | 2 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleSetRuleDaoAdapter.java` |
| `ISectionDAO` | 27 | 0 | 2 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/SectionDaoAdapter.java` |
| `IStudyDAO` | 31 | 0 | 2 | `app/src/main/java/org/researchedc/module/study/internal/adapter/StudyDaoAdapter.java` |
| `IStudyEventDAO` | 44 | 0 | 5 | `app/src/main/java/org/researchedc/module/event/internal/adapter/StudyEventDaoAdapter.java` |
| `IStudyEventDefinitionDAO` | 19 | 0 | 5 | `app/src/main/java/org/researchedc/module/event/internal/adapter/StudyEventDefinitionDaoAdapter.java` |
| `IStudyParameterValueDAO` | 15 | 0 | 0 | `app/src/main/java/org/researchedc/module/study/internal/adapter/StudyParameterValueDaoAdapter.java` |
| `IStudySubjectDAO` | 42 | 0 | 15 | `app/src/main/java/org/researchedc/module/subject/internal/adapter/StudySubjectDaoAdapter.java` |
| `ISubjectDAO` | 25 | 0 | 2 | `app/src/main/java/org/researchedc/module/subject/internal/adapter/SubjectDaoAdapter.java` |
| `IUserAccountDAO` | 42 | 0 | 1 | `app/src/main/java/org/researchedc/module/identity/internal/adapter/UserAccountDaoAdapter.java` |
| `ItemDataTypeDao` | 0 | 0 | 3 | `removed` |
| `ItemReferenceTypeDao` | 0 | 0 | 1 | `removed` |
| `MeasurementUnitDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/MeasurementUnitDaoAdapter.java` |
| `ResponseSetDomainDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ResponseSetDaoAdapter.java` |
| `ResponseTypeDao` | 0 | 0 | 2 | `removed` |
| `RuleActionRunLogDomainDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleActionRunLogDaoAdapter.java` |
| `RuleDomainDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleDomainDaoAdapter.java` |
| `RuleSetAuditDomainDao` | 2 | 0 | 0 | `app/src/main/java/org/researchedc/module/rule/internal/adapter/RuleSetAuditDaoAdapter.java` |
| `RuleSetDomainDao` | 0 | 0 | 7 | `removed` |
| `SCDItemMetadataDomainDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/SCDItemMetadataDaoAdapter.java` |
| `StudyGroupClassDao` | 12 | 0 | 0 | `app/src/main/java/org/researchedc/module/subjectgroup/internal/adapter/StudyGroupClassDaoAdapter.java` |
| `StudyGroupDao` | 16 | 0 | 0 | `app/src/main/java/org/researchedc/module/subjectgroup/internal/adapter/StudyGroupDaoAdapter.java` |
| `UsageStatsServiceDao` | 0 | 0 | 4 | `removed` |
| `VersioningMapDao` | 0 | 0 | 1 | `removed` |

## Immediate Deletion Rules

- Do not delete a DAO SPI interface from `shared/dao` solely because an adapter exists.
- Before deleting, verify no module adapter, injected caller, test, or runtime compatibility path still depends on the SPI name.
- Treat `unused` rows as blockers until each method is removed from the SPI or has stronger module-backed evidence.
- Prioritize caller migration from legacy SPI names to module-owned ports; then delete SPI interfaces once no injected caller remains.

## Detailed Rows

See `docs/refactor/phase-3-dao-replacement-ledger.csv` for method-level signatures, caller evidence, adapters, and deletion gates.

