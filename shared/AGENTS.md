# shared/ - Shared Domain Logic & Data Access

**Module:** Legacy compatibility DTO and term beans
**Files:** 15 Java files
**Package:** `org.researchedc.*`

> Formerly `legacy-core/`. Consolidated into `shared/` module with `@Repository`/`@Service` annotations
> and package rename from `org.akaza.openclinica` to `org.researchedc`. This module is the target of
> the Strangler Fig pattern — new functionality goes into `app/module/` Modulith modules.

## STRUCTURE

```
shared/src/main/java/org/researchedc/
└── bean/         # DTOs and term beans — 15 Java files

shared/src/main/resources/
├── migration/    # Liquibase schema migrations (210 XML files)
└── properties/   # ODM/XSD/XSLT/CRF-template compatibility resources (27 top-level files)
```

## KEY COMPONENTS

| Area | Files | Description |
|------|-------|-------------|
| **DAO (SPI)** | 0 | Deleted; compatibility data access now uses module-owned ports/repositories |
| **Domain Entities** | 0 | Retired; active mappings live in module-owned entities and repositories |
| **DTO Beans** | 15 | `EntityBean` subclasses — data transfer objects for compatibility paths |
| **Core/Exception Support** | 0 | Retired; app-owned config loads retained properties |
| **i18n Java Support** | 0 | Retired; term beans use standard `ResourceBundle` directly |
| **Liquibase Migrations** | 210 | Versioned schema changes from OpenClinica 3.x through 3.18 |
| **Legacy DAO XML** | 0 | Retired `properties/*_dao.xml` SQL maps were removed; active query loading uses `classpath:queries/<db>/**/*.properties` |
| **Quartz Jobs** | 0 | Moved to app-owned scheduler support |

## CONVENTIONS

- **Beans:** `*Bean` suffix for DTOs, extend `EntityBean<K>` with audit fields (id, createdDate, ownerId...)
- **DAOs:** do not add shared DAO SPI files; module implementations live under `app/module/*/internal/adapter/` and module repositories.
- **Entities:** do not add shared Hibernate entities; use module-owned `@Entity(name = "Module<Name>")` mappings.
- **DAO access:** Prefer Spring-injected DAO/SPI collaborators in managed beans; keep remaining legacy manual construction isolated behind local adapters/helpers until each path is strangulated.
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
| Annotations | ✅ `@Repository`/`@Service` applied to all DAOs and services |
| SPI interfaces | ✅ Deleted; caller migration to module-owned ports complete |
| Liquibase migrations | ✅ 210 XML files, versioned from 3.x through 3.18 |
| Strangulation target | 🔶 Active — remaining shared DTO/term beans should keep shrinking; new code goes to `app/module/` |
| DAO deletion | ✅ `DaoProvider`, direct `new XxxDAO(...)` / `new StudyConfigService(...)`, LegacyDaoFactory, EntityDAO infrastructure, and shared DAO SPI files are removed. Phase 3 ledger: 878/878 rows removed; 0 unused, fallback-SQL, legacy-only, or adapter-gap rows remain. |

## ANTI-PATTERNS

- **NEVER** add new code to `shared/` — add new functionality to `app/module/<name>/`
- **NEVER** import `shared/` DAOs or entities directly from Modulith modules — use adapter pattern
- **AVOID** modifying Liquibase migrations after release — add new migration files
- **DO NOT** bypass service layer — call `@Service` beans from controllers, not DAOs directly
