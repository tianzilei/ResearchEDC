# Next Refactor And Removal Plan

**Updated:** 2026-06-17

## Current State

| Surface | Before | After | Removed | % |
|---------|--------|-------|---------|---|
| shared/ files | 793 | 241 | 552 | 69.6% |
| shared/ lines | ~80,000 | 31,139 | ~48,861 | ~61% |
| dao/ files | 186 | 39 | 147 | 79.0% |
| web/ | 480 | 0 | 480 | 100% |
| ws/ | 75 | 0 | 75 | 100% |
| SPI methods | 878 | 878 covered | 158 removed | 100% |
| Module files | — | 392 | — | — |

**Code balance:** 241 legacy / 392 modern = 38% legacy / 62% modern (files), 58% legacy / 42% modern (lines)

## Status

Dead code is exhausted. All remaining shared files have active callers.

- ✅ Dead file deletion: complete (515 files removed)
- ✅ Dead method removal: complete (~130 dead methods removed)
- ✅ Dead @OneToMany cleanup: complete (46 fields removed from 10 entities)
- ✅ Dead @ManyToOne fields: cannot remove (Hibernate requires owning-side FK mappings)
- ✅ SPI method coverage: 100% (878/878)
- ✅ LegacyDaoFactory: eliminated
- ✅ EntityDAO infrastructure: deleted

## Remaining Surface (241 files)

| Category | Files | Status |
|----------|-------|--------|
| Bean/DTOs | 81 | Active — used by module adapters |
| Domain entities | 103 | Active — JPA @Entity mappings, used by module repositories |
| DAO (SPI) | 39 | Active — SPI interfaces implemented by module adapters |
| Jobs | 4 | Active — Quartz infrastructure |
| Core | 4 | Active — CoreResources, StringUtil, etc. |
| Other | 13 | Active — i18n, patterns, exceptions |

## Next Steps (requires deeper refactoring)

Current active slice: migrate remaining module callers from legacy SPI names to module-owned ports. The latest slices moved database changelog list reads onto `DatabaseChangeLogPort.findChangeLogs()`, moved audit-user event reads out of `IAuditEventDAO`/`IUserAccountDAO` injection, moved study-subject event audit reads out of seven legacy DAO SPI injections into audit module query paths, removed dead DAO-backed `Validator` branches that no longer had call sites, simplified the import bridge so `ImportCrfDataAdapter` no longer carries a dead `DataSource` dependency after the preview-only `ImportCRFDataService` helpers were deleted, moved import commit item shaping fully into `ImportCrfDataAdapter`, moved study metadata/OID validation there as well, and finally absorbed the remaining event/status compatibility logic so `ImportCRFDataService` could be deleted. Follow-up cleanup then removed the last dead app-hosted compatibility leftovers: the final `app/src/main/java/org/researchedc/web/*` holdouts (`ImportHelper`, `OpenClinicaLdapAuthoritiesPopulator`), the obsolete `DaoRegistrar`, the orphaned `ImportCRFInfo`, and the now-no-op `LegacyServletConfig`. Scans now show zero non-adapter legacy DAO/SPI imports under `app/src/main/java`, zero Java classes left under `app/src/main/java/org/researchedc/web`, zero concrete DAO classes left under `shared/src/main/java/org/researchedc/dao`, and no Boot servlet-compatibility registration class remaining; the remaining DAO SPI files stay blocked on caller migration, not method-level replacement coverage.

1. **Replace legacy SPI callers with module-owned ports** — the 39 DAO SPI files and 81 beans are the legacy surface that module adapters and compatibility paths still depend on. Reducing further requires migrating callers to module ports and repositories directly.
2. **Migrate remaining shared support code to module-owned services** — no `shared/service` package remains; further reduction requires proving compatibility support classes unused.
3. **Remove JSTL/JMesa dependencies** — after all JSP-era rendering code is confirmed dead.

## Verification

```bash
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
bash scripts/ci/check-legacy-guardrails.sh
cd frontend && pnpm typecheck
```
