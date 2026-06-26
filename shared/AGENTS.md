# shared/ - Shared Domain Logic & Data Access

**Module:** Resource-only shared module (i18n, migrations, properties)
**Files:** 0 Java files; resource files only
**Package:** `org.researchedc.*`

> Formerly `legacy-core/`. Consolidated into `shared/` module. All Java code has been retired —
> DAO SPIs were deleted, domain entities were removed, and all `shared/bean` DTOs have been
> migrated to module-owned DTOs under `app/module/*/dto/`. The `shared/` module now contains
> only resource files: i18n property bundles, Liquibase migrations, and ODM/XSD templates.

## STRUCTURE

```
shared/src/main/java/org/researchedc/
# (empty — 0 Java files remain)

shared/src/main/resources/
├── migration/    # Liquibase schema migrations (210 XML files)
├── i18n/         # Internationalization property bundles (6 languages)
└── properties/   # ODM/XSD/XSLT/CRF-template compatibility resources (27 top-level files)
```

## KEY COMPONENTS

| Area | Files | Description |
|------|-------|-------------|
| **Java files** | 0 | All Java code retired; module adapters now use module-owned DTOs and repositories |
| **DAO (SPI)** | 0 | Deleted; compatibility data access now uses module-owned ports/repositories |
| **Domain Entities** | 0 | Retired; active mappings live in module-owned entities and repositories |
| **DTO Beans** | 0 | All migrated to module-owned DTOs under `app/module/*/dto/` |
| **Core/Exception Support** | 0 | Retired; app-owned config loads retained properties |
| **i18n Java Support** | 0 | Retired; term beans use standard `ResourceBundle` directly |
| **Liquibase Migrations** | 210 | Versioned schema changes from OpenClinica 3.x through 3.18 |
| **Legacy DAO XML** | 0 | Retired `properties/*_dao.xml` SQL maps were removed; active query loading uses `classpath:queries/<db>/**/*.properties` |
| **Quartz Jobs** | 0 | Moved to app-owned scheduler support |

## CONVENTIONS

- **DAOs:** do not add shared DAO SPI files; module implementations live under `app/module/*/internal/adapter/` and module repositories.
- **Entities:** do not add shared Hibernate entities; use module-owned `@Entity(name = "Module<Name>")` mappings.
- **Beans:** do not add `*Bean` DTOs here; use module-owned DTOs under `app/module/*/dto/`.
- **Package:** All classes in `org.researchedc.*` (migrated from `org.akaza.openclinica`)

## TESTING

The `shared/` module has no dedicated test directory. Tests for shared functionality exist in:
- `app/src/test/` — 54 Java test files covering Modulith module, legacy-bridge, and compatibility paths

Legacy `legacy-core/src/test/` (17 files, DBUnit-based DAO/Service tests) was archived during the
`legacy-core` → `shared/` consolidation. These tests require PostgreSQL and have commented-out
test methods awaiting reactivation.

## MODULE STATUS

| Aspect | Status |
|--------|--------|
| Package rename | ✅ `org.akaza.openclinica` → `org.researchedc` |
| SPI interfaces | ✅ Deleted; caller migration to module-owned ports complete |
| Domain entities | ✅ Removed; 0 Java files remain in `shared/domain` |
| DTO beans | ✅ Removed; all migrated to module-owned DTOs under `app/module/*/dto/` |
| Liquibase migrations | ✅ 210 XML files, versioned from 3.x through 3.18 |
| Strangulation target | ✅ COMPLETE — `shared/src/main/java` contains 0 Java files; resource-only module |
| DAO deletion | ✅ `DaoProvider`, direct `new XxxDAO(...)` / `new StudyConfigService(...)`, LegacyDaoFactory, EntityDAO infrastructure, and shared DAO SPI files are removed. Phase 3 ledger: 878/878 rows removed; 0 unused, fallback-SQL, legacy-only, or adapter-gap rows remain. |

## ANTI-PATTERNS

- **NEVER** add new code to `shared/` — add new functionality to `app/module/<name>/`
- **NEVER** import `shared/` DAOs or entities directly from Modulith modules — use adapter pattern
- **AVOID** modifying Liquibase migrations after release — add new migration files
- **DO NOT** bypass service layer — call `@Service` beans from controllers, not DAOs directly
