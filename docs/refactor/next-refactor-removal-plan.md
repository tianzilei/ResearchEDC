# Next Refactor And Removal Plan

**Created:** 2026-06-11
**Updated:** 2026-06-15
**Status source:** current tree analysis

## Current Status

Legacy removal is **not complete** but major structural blockers are eliminated.

Completed:

- Phase 0 inventory and guardrails ✅
- Phase B schema ownership ✅ (12 triggers, 27 entities)
- Phase 1 web/JSP/servlet deletion ✅ (web/ absent)
- Phase 2 SOAP retirement ✅ (ws/ absent)
- Phase 3 DAO SPI method cleanup ✅ (878/878 methods, 100% coverage)
- LegacyDaoFactory elimination ✅
- EntityDAO/AuditableEntityDAO infrastructure deletion ✅
- ExtractBean deletion ✅
- Rule engine dead code deletion ✅ (52 files, 12,461 lines)
- ODM export/scoring/discrepancy dead code deletion ✅ (36 files, 14,294 lines)

Current metrics:

| Surface | Before | Current | Removed |
|---------|--------|---------|---------|
| `shared/` Java files | 793 | 395 | 398 (50.2%) |
| `shared/dao/` files | 186 | 76 | 110 (59.1%) |
| `web/` files | 480 | 0 | 480 (100%) |
| `ws/` files | 75 | 0 | 75 (100%) |
| SPI methods | 878 | 878 covered | 120 removed, 758 module-backed |
| Module files | — | 404 | — |
| Shared lines | — | 45,394 | — |
| Module lines | — | 27,471 | — |

## Remaining Work

### 1. Shared dead code scan

Status: **active**

Goal: find and delete dead shared/ files that have no callers from app/module code.

The 395 remaining shared files break down as:

- 167 beans/DTOs — some used by adapters, some potentially dead
- 112 domain entities — JPA mappings, some used by adapters
- 76 DAO files — SPI interfaces + filter/sort + core infrastructure (structurally required)
- 40 services, logic, validators, i18n, jobs, exceptions

Actions:

- Scan each shared/service/ file for callers from app/
- Scan each shared/logic/ file for callers from app/
- Scan each shared/bean/ file for callers from app/
- Scan each shared/domain/ file for callers from app/
- Delete dead files, update plan

Exit gate:

- Every remaining shared/ file has at least one active caller from app/ or is structurally required (SPI interface, entity, filter/sort)

### 2. Shared line reduction

Goal: reduce shared/ from 45,394 lines toward module-owned code.

Actions:

- For shared/services with active callers: consider migrating to module-owned services
- For shared/beans with active callers: consider replacing with module DTOs
- For shared/domain entities: keep until module entities fully replace them

### 3. Done Definition

Legacy code removal is complete only when all are true:

- `web/` directory is deleted ✅
- `ws/` remains absent ✅
- `shared/dao` contains only SPI interfaces, filter/sort, and core infrastructure ✅ (76 files remain, all structurally required)
- Legacy-only beans/services/jobs/utilities in `shared/` have no callers and are deleted ⬜
- Legacy-only dependencies are removed from Maven ⬜
- Full backend, frontend, and E2E verification passes ✅

## Verification Commands

```bash
git status --short
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
bash scripts/ci/check-legacy-guardrails.sh
cd frontend && pnpm typecheck
```
