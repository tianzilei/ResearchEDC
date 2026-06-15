# Next Refactor And Removal Plan

**Created:** 2026-06-11
**Updated:** 2026-06-15

## Current Status

| Surface | Before | Current | Removed | % Removed |
|---------|--------|---------|---------|-----------|
| `shared/` Java files | 793 | 297 | 496 | **62.6%** |
| `shared/` lines | ~80,000+ | 37,212 | ~43,000+ | **~54%** |
| `shared/dao/` files | 186 | 76 | 110 | **59.1%** |
| `web/` files | 480 | 0 | 480 | **100%** |
| `ws/` files | 75 | 0 | 75 | **100%** |
| SPI methods | 878 | 878 covered | 120 removed | **100%** |
| Module files | — | 404 | — | — |
| Module lines | — | 27,471 | — | — |

**Code balance:** 297 legacy / 404 modern files = **42% legacy / 58% modern**

## Completed This Session

- ✅ 28 unused SPI methods removed from 6 DAO families
- ✅ 41 unused SPI methods removed from 16 DAO families
- ✅ 52 dead rule engine files deleted (12,461 lines)
- ✅ QueryDAO deleted
- ✅ 36 dead ODM/scoring/discrepancy files deleted (14,294 lines)
- ✅ LegacyDaoFactory, EntityDAO, ExtractBean deleted (7,468 lines)
- ✅ 94 dead shared/ files deleted (8,446 lines)
- ✅ SPI method coverage: 100% (878/878)

## Remaining Work

### Remaining shared/ breakdown (297 files)

| Category | Files | Status |
|----------|-------|--------|
| Bean/DTOs | 88 | Most have active callers from adapters |
| Domain entities | 112 | JPA mappings, used by adapters |
| DAO (SPI + filter + infra) | 76 | Structurally required by adapter pattern |
| Services | 4 | Active callers from app/ |
| Jobs | 4 | Active Quartz infrastructure |
| Core | 4 | Active (CoreResources, StringUtil, etc.) |
| Other (i18n, patterns, exceptions) | 9 | Active |

### Next actions

1. **Dead bean scan** — check remaining 88 beans for any more dead files
2. **Dead service scan** — check remaining 4 services for dead files
3. **Line reduction** — remove dead methods/fields from alive beans
4. **Done definition** — all remaining shared/ files have active callers

## Verification Commands

```bash
git status --short
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
bash scripts/ci/check-legacy-guardrails.sh
cd frontend && pnpm typecheck
```
