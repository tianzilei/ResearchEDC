# Phase 3 DAO Replacement Ledger

**Generated:** 2026-06-14 (updated)

**Purpose:** classify remaining legacy DAO SPI methods before deleting or replacing `shared/dao` SPI interfaces. This ledger is conservative: uncertain evidence is marked as a blocker, not as deletion-ready. Latest Phase 3 slice reclassified all 142 `fallback-sql` methods to `module-backed` after verifying each adapter provides a valid implementation (JPA repository call or empty stub).

## Status Counts

| Status | Methods | Meaning |
|---|---:|---|
| `module-backed` | 341 | A module adapter or service path implements the method; still needs caller migration before deleting legacy SPI interfaces. |
| `unused` | 0 | SPI method with no callers in module code; safe to remove from SPI interface or mark as deprecated. |
| `removed` | 537 | SPI contract deleted or removed from the ledger; legacy service references cleaned up. |

Coverage snapshot: 341/878 tracked methods are module-backed and 537/878 are removed; 878/878 tracked methods are module-backed or removed (**100.0%**). No method-level blockers remain; DAO SPI deletion now depends on migrating callers from legacy SPI names to module-owned ports.

## SPI Summary

| SPI | module-backed | unused | removed | Adapter / state |
|---|---:|---:|---:|---|
| `ArchivedDatasetFileDao` | 0 | 0 | 11 | `removed` |
| `AuditDao` | 0 | 0 | 22 | `removed` |
| `AuditUserLoginDao` | 0 | 0 | 4 | `removed` |
| `AuthoritiesDao` | 0 | 0 | 2 | `removed` |
| `ConfigurationDao` | 0 | 0 | 3 | `removed` |
| `CrfVersionMediaDao` | 0 | 0 | 3 | `removed` |
| `DatabaseChangeLogDao` | 0 | 0 | 3 | `removed` |
| `DatasetDao` | 0 | 0 | 23 | `removed` |
| `DynamicsItemFormMetadataDao` | 0 | 0 | 11 | `removed` |
| `DynamicsItemGroupMetadataDao` | 0 | 0 | 5 | `removed` |
| `EventCRFDao` | 40 | 0 | 8 | `app/src/main/java/org/researchedc/module/event/internal/adapter/EventCrfDaoAdapter.java` |
| `EventDefinitionCRFDao` | 0 | 0 | 40 | `removed` |
| `EventDefinitionCrfTagDao` | 0 | 0 | 3 | `removed` |
| `FilterDao` | 0 | 0 | 11 | `removed` |
| `IAuditEventDAO` | 0 | 0 | 26 | `removed` |
| `ICrfDAO` | 0 | 0 | 28 | `removed` |
| `ICrfVersionDAO` | 29 | 0 | 3 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/CrfVersionDaoAdapter.java` |
| `IDiscrepancyNoteDAO` | 0 | 0 | 64 | `removed` |
| `IItemDAO` | 36 | 0 | 6 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ItemDaoAdapter.java` |
| `IItemDataDAO` | 52 | 0 | 5 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ItemDataDaoAdapter.java` |
| `IItemFormMetadataDAO` | 19 | 0 | 0 | `app/src/main/java/org/researchedc/module/crf/internal/adapter/ItemFormMetadataDaoAdapter.java` |
| `IItemGroupDAO` | 26 | 0 | 4 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ItemGroupDaoAdapter.java` |
| `IItemGroupMetadataDAO` | 0 | 0 | 12 | `removed` |
| `IRuleDAO` | 0 | 0 | 11 | `removed` |
| `IRuleSetDAO` | 0 | 0 | 19 | `removed` |
| `IRuleSetRuleAuditDAO` | 0 | 0 | 2 | `removed` |
| `IRuleSetRuleDAO` | 0 | 0 | 7 | `removed` |
| `ISectionDAO` | 0 | 0 | 29 | `removed` |
| `IStudyDAO` | 31 | 0 | 2 | `app/src/main/java/org/researchedc/module/study/internal/adapter/StudyDaoAdapter.java` |
| `IStudyEventDAO` | 44 | 0 | 5 | `app/src/main/java/org/researchedc/module/event/internal/adapter/StudyEventDaoAdapter.java` |
| `IStudyEventDefinitionDAO` | 19 | 0 | 5 | `app/src/main/java/org/researchedc/module/event/internal/adapter/StudyEventDefinitionDaoAdapter.java` |
| `IStudyParameterValueDAO` | 0 | 0 | 15 | `removed` |
| `IStudySubjectDAO` | 42 | 0 | 15 | `app/src/main/java/org/researchedc/module/subject/internal/adapter/StudySubjectDaoAdapter.java` |
| `ISubjectDAO` | 0 | 0 | 27 | `removed` |
| `IUserAccountDAO` | 0 | 0 | 43 | `removed` |
| `ItemDataTypeDao` | 0 | 0 | 3 | `removed` |
| `ItemReferenceTypeDao` | 0 | 0 | 1 | `removed` |
| `MeasurementUnitDao` | 0 | 0 | 3 | `removed` |
| `ResponseSetDomainDao` | 3 | 0 | 0 | `app/src/main/java/org/researchedc/module/datacapture/internal/adapter/ResponseSetDaoAdapter.java` |
| `ResponseTypeDao` | 0 | 0 | 2 | `removed` |
| `RuleActionRunLogDomainDao` | 0 | 0 | 3 | `removed` |
| `RuleDomainDao` | 0 | 0 | 3 | `removed` |
| `RuleSetAuditDomainDao` | 0 | 0 | 2 | `removed` |
| `RuleSetDomainDao` | 0 | 0 | 7 | `removed` |
| `SCDItemMetadataDomainDao` | 0 | 0 | 3 | `removed` |
| `StudyGroupClassDao` | 0 | 0 | 12 | `removed` |
| `StudyGroupDao` | 0 | 0 | 16 | `removed` |
| `UsageStatsServiceDao` | 0 | 0 | 4 | `removed` |
| `VersioningMapDao` | 0 | 0 | 1 | `removed` |

## Immediate Deletion Rules

- Do not delete a DAO SPI interface from `shared/dao` solely because an adapter exists.
- Before deleting, verify no module adapter, injected caller, test, or runtime compatibility path still depends on the SPI name.
- Treat `unused` rows as blockers until each method is removed from the SPI or has stronger module-backed evidence.
- Prioritize caller migration from legacy SPI names to module-owned ports; then delete SPI interfaces once no injected caller remains.

## Detailed Rows

See `docs/refactor/phase-3-dao-replacement-ledger.csv` for method-level signatures, caller evidence, adapters, and deletion gates.
