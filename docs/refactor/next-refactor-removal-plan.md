# Next Refactor And Removal Plan

**Updated:** 2026-06-19

## Current State

| Surface | Before | After | Removed | % |
|---------|--------|-------|---------|---|
| shared/ files | 793 | 202 | 591 | 74.5% |
| shared/ lines | ~80,000 | 28,385 | ~51,615 | ~65% |
| dao/ files | 186 | 0 | 186 | 100% |
| web/ | 480 | 0 | 480 | 100% |
| ws/ | 75 | 0 | 75 | 100% |
| SPI methods | 878 | 878 removed | 878 removed | 100% |
| Module files | - | 377 | - | - |

**Code balance:** 202 shared legacy / 377 module modern = 35% legacy / 65% modern (files), roughly 51% legacy / 49% modern (lines)

## Status

Dead code is exhausted. All remaining shared files have active callers.

- ✅ Dead file deletion: complete (515 files removed)
- ✅ Dead method removal: complete (~130 dead methods removed)
- ✅ Dead @OneToMany cleanup: complete (46 fields removed from 10 entities)
- ✅ Dead @ManyToOne fields: cannot remove (Hibernate requires owning-side FK mappings)
- ✅ SPI method coverage: 100% (878/878)
- ✅ DAO SPI surface: deleted (0 files under `shared/dao`)
- ✅ LegacyDaoFactory: eliminated
- ✅ EntityDAO infrastructure: deleted

## Remaining Surface (202 files)

| Category | Files | Status |
|----------|-------|--------|
| Bean/DTOs | 81 | Active — used by module adapters |
| Domain entities | 103 | Active — JPA @Entity mappings, used by module repositories |
| DAO (SPI) | 0 | Deleted — use module-owned ports and repositories |
| Jobs | 4 | Active — Quartz infrastructure |
| Core | 5 | Active — CoreResources, StringUtil, etc. |
| i18n | 3 | Active — locale and resource bundle utilities |
| Exceptions | 2 | Active — compatibility exceptions |

## Next Steps (requires deeper refactoring)

The latest slices migrated the remaining import CRF data callers from legacy SPI names to data-import-owned ports (`ImportStudyLookupPort`, `ImportCrfVersionPort`, `ImportItemPort`, `ImportItemGroupPort`, `ImportItemFormMetadataPort`, `ImportItemDataPort`, `ImportResponseSetPort`, and `ImportEventCrfPort`) and then deleted the final shared DAO SPI files after repo-wide scans confirmed no remaining callers. Scans now show zero legacy DAO/SPI imports under app/shared Java sources, zero Java classes left under `app/src/main/java/org/researchedc/web`, zero files left under `shared/src/main/java/org/researchedc/dao`, no Boot servlet-compatibility registration class remaining, no active app-side OAuth2/Keycloak wiring, and no checked-in JSP-table dependency surface.

1. **Keep DAO SPI deleted** — new compatibility behavior must use module-owned ports and repositories, not shared DAO SPI names.
2. **Migrate remaining shared support code to module-owned services** — no `shared/service` package remains; further reduction requires proving compatibility support classes unused.
3. **Migrate remaining shared support code to module-owned services** — with JSP-era dependency cleanup complete, the remaining reductions are support-code and caller-migration driven.

## Verification

```bash
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
bash scripts/ci/check-legacy-guardrails.sh
cd frontend && pnpm typecheck
```
