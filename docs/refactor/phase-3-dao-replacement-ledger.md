# Phase 3 DAO Replacement Ledger

**Generated:** 2026-06-14 (updated)

**Purpose:** preserve the final replacement/deletion record for legacy DAO SPI methods after deleting the `shared/dao` SPI surface. The ledger is conservative: adapter evidence is retained for auditability, but the checked-in SPI contracts are gone.

## Status Counts

| Status | Methods | Meaning |
|---|---:|---|
| `module-backed` | 0 | No SPI contracts remain waiting on caller migration. |
| `unused` | 0 | SPI method with no callers in module code; safe to remove from SPI interface or mark as deprecated. |
| `removed` | 878 | SPI contract deleted or removed from the ledger; legacy service references cleaned up. |

Coverage snapshot: 878/878 tracked methods are removed (**100.0%**). No method-level blockers remain and no DAO SPI interface remains under `shared/dao`.

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
| `EventCRFDao` | 0 | 0 | 48 | `removed` |
| `EventDefinitionCRFDao` | 0 | 0 | 40 | `removed` |
| `EventDefinitionCrfTagDao` | 0 | 0 | 3 | `removed` |
| `FilterDao` | 0 | 0 | 11 | `removed` |
| `IAuditEventDAO` | 0 | 0 | 26 | `removed` |
| `ICrfDAO` | 0 | 0 | 28 | `removed` |
| `ICrfVersionDAO` | 0 | 0 | 32 | `removed` |
| `IDiscrepancyNoteDAO` | 0 | 0 | 64 | `removed` |
| `IItemDAO` | 0 | 0 | 42 | `removed` |
| `IItemDataDAO` | 0 | 0 | 57 | `removed` |
| `IItemFormMetadataDAO` | 0 | 0 | 19 | `removed` |
| `IItemGroupDAO` | 0 | 0 | 30 | `removed` |
| `IItemGroupMetadataDAO` | 0 | 0 | 12 | `removed` |
| `IRuleDAO` | 0 | 0 | 11 | `removed` |
| `IRuleSetDAO` | 0 | 0 | 19 | `removed` |
| `IRuleSetRuleAuditDAO` | 0 | 0 | 2 | `removed` |
| `IRuleSetRuleDAO` | 0 | 0 | 7 | `removed` |
| `ISectionDAO` | 0 | 0 | 29 | `removed` |
| `IStudyDAO` | 0 | 0 | 33 | `removed` |
| `IStudyEventDAO` | 0 | 0 | 49 | `removed` |
| `IStudyEventDefinitionDAO` | 0 | 0 | 24 | `removed` |
| `IStudyParameterValueDAO` | 0 | 0 | 15 | `removed` |
| `IStudySubjectDAO` | 0 | 0 | 57 | `removed` |
| `ISubjectDAO` | 0 | 0 | 27 | `removed` |
| `IUserAccountDAO` | 0 | 0 | 43 | `removed` |
| `ItemDataTypeDao` | 0 | 0 | 3 | `removed` |
| `ItemReferenceTypeDao` | 0 | 0 | 1 | `removed` |
| `MeasurementUnitDao` | 0 | 0 | 3 | `removed` |
| `ResponseSetDomainDao` | 0 | 0 | 3 | `removed` |
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

## Final Deletion Rules

- No `shared/dao` SPI interface remains in the current tree.
- Future compatibility work must use module-owned ports and repositories, not reintroduced shared DAO SPI names.
- Adapter methods retained in `app/module/*/internal/adapter` are compatibility helpers, not shared DAO contracts.

## Detailed Rows

See `docs/refactor/phase-3-dao-replacement-ledger.csv` for method-level signatures, caller evidence, adapters, and deletion gates.
