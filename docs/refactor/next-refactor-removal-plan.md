# Next Refactor And Removal Plan

**Updated:** 2026-06-16

## Current State

| Surface | Before | After | Removed | % |
|---------|--------|-------|---------|---|
| shared/ files | 793 | 273 | 520 | 65.6% |
| shared/ lines | ~80,000 | 33,338 | ~46,662 | ~58% |
| dao/ files | 186 | 71 | 115 | 61.8% |
| web/ | 480 | 0 | 480 | 100% |
| ws/ | 75 | 0 | 75 | 100% |
| SPI methods | 878 | 878 covered | 121 removed | 100% |
| Module files | — | 404 | — | — |

**Code balance:** 273 legacy / 404 modern = 40% legacy / 60% modern (files), 58% legacy / 42% modern (lines)

## Status

Dead code is exhausted. All remaining shared files have active callers.

- ✅ Dead file deletion: complete (515 files removed)
- ✅ Dead method removal: complete (~130 dead methods removed)
- ✅ Dead @OneToMany cleanup: complete (46 fields removed from 10 entities)
- ✅ Dead @ManyToOne fields: cannot remove (Hibernate requires owning-side FK mappings)
- ✅ SPI method coverage: 100% (878/878)
- ✅ LegacyDaoFactory: eliminated
- ✅ EntityDAO infrastructure: deleted

## Remaining Surface (273 files)

| Category | Files | Status |
|----------|-------|--------|
| Bean/DTOs | 82 | Active — used by module adapters |
| Domain entities | 103 | Active — JPA @Entity mappings, used by module repositories |
| DAO (SPI + filter + infra) | 71 | Active — SPI interfaces implemented by module adapters |
| Jobs | 4 | Active — Quartz infrastructure |
| Core | 4 | Active — CoreResources, StringUtil, etc. |
| Other | 13 | Active — i18n, patterns, exceptions |

## Next Steps (requires deeper refactoring)

1. **Replace adapter-delegated behavior with module-owned implementations** — the 71 DAO files and 82 beans are the legacy surface that module adapters depend on. Reducing further requires migrating adapter logic to use module repositories directly.
2. **Migrate remaining shared support code to module-owned services** — no `shared/service` package remains; further reduction requires proving compatibility support classes unused.
3. **Remove JSTL/JMesa dependencies** — after all JSP-era rendering code is confirmed dead.

## Verification

```bash
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
bash scripts/ci/check-legacy-guardrails.sh
cd frontend && pnpm typecheck
```
