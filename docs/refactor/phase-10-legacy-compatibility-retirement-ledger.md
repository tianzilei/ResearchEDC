# Phase 10 Legacy Compatibility Retirement Ledger

**Created:** 2026-06-25
**Status:** Initial classification
**Purpose:** classify remaining `module/legacy` REST endpoints after frontend callers were migrated to module-native `/api/v1/*` APIs.

## Current State

Frontend scan:

```bash
rg -n "/api/legacy" frontend/src -g "*.ts" -g "*.tsx"
```

Result: no active frontend callers.

The remaining `/api/legacy/*` endpoints are therefore compatibility endpoints, not current SPA product dependencies.
They should be removed only after an explicit external-compatibility decision.

## Classifications

| Classification | Meaning |
|---|---|
| `external-compat-retain` | Keep for potential external integrations or backward-compatible API clients |
| `deprecated` | Keep temporarily, document replacement, plan removal |
| `ready-to-remove` | No frontend caller and no external compatibility reason identified |

## Endpoint Ledger

| Controller | Legacy Path | Module-Native Replacement | Current Frontend Callers | Classification | Deletion Gate |
|---|---|---|---:|---|---|
| `LegacyStudyController` | `/api/legacy/studies` | `/api/v1/studies` | 0 | `external-compat-retain` | external compatibility decision + remove/update `LegacyGatewayContractTest` coverage |
| `LegacySubjectController` | `/api/legacy/subjects` | `/api/v1/subjects` | 0 | `external-compat-retain` | external compatibility decision + replacement contract documented |
| `LegacyCrfManageController` | `/api/legacy/crfs` | `/api/v1/crfs/manage` | 0 | `deprecated` | verify no external admin clients; remove contract tests; delete controller/DTOs |
| `LegacyDatasetController` | `/api/legacy/datasets` | `/api/v1/datasets` | 0 | `retired` | 2026-06-25: deleted, no external callers found |
| `LegacyFilterController` | `/api/legacy/filters` | `/api/v1/filters` | 0 | `retired` | 2026-06-25: deleted, no external callers found |
| `LegacySubjectGroupController` | `/api/legacy/subject-groups` | `/api/v1/subject-groups` | 0 | `deprecated` | verify no external subject-group clients; remove contract tests; delete controller/DTOs |
| `LegacyDiscrepancyNoteController` | `/api/legacy/discrepancy-notes` | `/api/v1/discrepancy-notes` | 0 | `deprecated` | verify no external discrepancy-note clients; remove contract tests; delete controller/DTOs |
| `LegacyRuleSetController` | `/api/legacy/rule-sets` | `/api/v1/rules/rule-sets` | 0 | `deprecated` | verify no external rule-set clients; remove contract tests; delete controller/DTOs |
| `LegacyRuleController` | `/api/legacy/rules` | `/api/v1/rules` | 0 | `deprecated` | verify no external rule clients; remove contract tests; delete controller/DTOs |
| `ImportUploadController` | `/api/legacy/import/upload` | `/api/v1/imports/upload` | 0 | `deprecated` | verify no external import upload clients; remove contract tests; delete controller |

## Recommended Retirement Order

1. Dataset/filter compatibility endpoints
2. Subject group compatibility endpoints
3. Discrepancy-note compatibility endpoints
4. Rule/rule-set compatibility endpoints
5. CRF admin compatibility endpoints
6. Import upload compatibility endpoint
7. Study/subject compatibility endpoints

Study and subject are retained last because they are more likely to be used by external integrations.

## Required Removal Procedure

For each controller:

1. confirm frontend callers remain zero
2. make an explicit external compatibility decision
3. remove or update `LegacyGatewayContractTest` coverage
4. delete controller and legacy DTOs that become unused
5. run:

```bash
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
```

6. run frontend grep guardrail:

```bash
rg -n "/api/legacy" frontend/src -g "*.ts" -g "*.tsx"
```

Expected result: no matches.
