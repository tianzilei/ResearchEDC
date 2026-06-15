# Next Refactor And Removal Plan

**Updated:** 2026-06-15

## Current State

| Surface | Before | After | Removed | % |
|---------|--------|-------|---------|---|
| shared/ files | 793 | 278 | 515 | 64.9% |
| shared/ lines | ~80,000 | 33,338 | ~46,662 | ~58% |
| dao/ files | 186 | 75 | 111 | 59.7% |
| web/ | 480 | 0 | 480 | 100% |
| ws/ | 75 | 0 | 75 | 100% |
| SPI methods | 878 | 878 covered | 120 removed | 100% |
| Module files | — | 404 | — | — |

**Code balance:** 278 legacy / 404 modern = 41% legacy / 59% modern (files), 58% legacy / 42% modern (lines)

## Status

Dead code is exhausted. All remaining shared files have active callers.

- ✅ Dead file deletion: complete (515 files removed)
- ✅ Dead method removal: complete (~130 dead methods removed)
- ✅ Dead @OneToMany cleanup: complete (46 fields removed from 10 entities)
- ✅ Dead @ManyToOne fields: cannot remove (Hibernate requires owning-side FK mappings)
- ✅ SPI method coverage: 100% (878/878)
- ✅ LegacyDaoFactory: eliminated
- ✅ EntityDAO infrastructure: deleted

## Remaining Surface (278 files)

| Category | Files | Status |
|----------|-------|--------|
| Bean/DTOs | 73 | Active — used by module adapters |
| Domain entities | 95 | Active — JPA @Entity mappings, used by module repositories |
| DAO (SPI + filter + infra) | 75 | Active — SPI interfaces implemented by module adapters |
| Jobs | 4 | Active — Quartz infrastructure |
| Core | 4 | Active — CoreResources, StringUtil, etc. |
| Other | 27 | Active — i18n, patterns, exceptions |

## Next Steps (requires deeper refactoring)

1. **Replace adapter-delegated behavior with module-owned implementations** — the 75 DAO files and 73 beans are the legacy surface that module adapters depend on. Reducing further requires migrating adapter logic to use module repositories directly.
2. **Migrate remaining shared services to module-owned services** — 4 active service files remain.
3. **Remove JSTL/JMesa dependencies** — after all JSP-era rendering code is confirmed dead.

## Verification

```bash
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
bash scripts/ci/check-legacy-guardrails.sh
cd frontend && pnpm typecheck
```
