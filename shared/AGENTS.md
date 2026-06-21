# shared/ - Shared Domain Logic & Data Access

**Module:** Legacy domain logic, data access, entities, and business services
**Files:** 65 Java files
**Package:** `org.researchedc.*`

> Formerly `legacy-core/`. Consolidated into `shared/` module with `@Repository`/`@Service` annotations
> and package rename from `org.akaza.openclinica` to `org.researchedc`. This module is the target of
> the Strangler Fig pattern — new functionality goes into `app/module/` Modulith modules.

## STRUCTURE

```
shared/src/main/java/org/researchedc/
├── bean/         # DTOs — 38 Java files
├── core/         # Core resources/configuration — 2 Java files
├── domain/       # Hibernate/domain entities — 23 Java files
├── exception/    # Custom exceptions — 1 Java file
├── i18n/         # Internationalization utilities + 22 .properties files
└── other support # Logging, validation, and compatibility helpers — 0 Java files

shared/src/main/resources/
├── migration/    # Liquibase schema migrations (208 XML files)
└── properties/   # ODM/XSD/XSLT/CRF-template compatibility resources (27 top-level files)
```

## KEY COMPONENTS

| Area | Files | Description |
|------|-------|-------------|
| **DAO (SPI)** | 0 | Deleted; compatibility data access now uses module-owned ports/repositories |
| **Domain Entities** | 23 | Hibernate `@Entity` classes mapping to database tables (`datamap/` has 16 Java files); former duplicate admin/managestudy, technical admin, retired crfdata, retired rule, old domain base interfaces, DN/subjectgroup/discrepancy-note/study-user-role map, and unused datamap mappings have been removed |
| **DTO Beans** | 38 | `EntityBean` subclasses — data transfer objects for compatibility paths |
| **Liquibase Migrations** | 209 | Versioned schema changes from OpenClinica 3.x through 3.18 |
| **Legacy DAO XML** | 0 | Retired `properties/*_dao.xml` SQL maps were removed; active query loading uses `classpath:queries/<db>/**/*.properties` |
| **Quartz Jobs** | 0 | Moved to app-owned scheduler support |

## CONVENTIONS

- **Beans:** `*Bean` suffix for DTOs, extend `EntityBean<K>` with audit fields (id, createdDate, ownerId...)
- **DAOs:** do not add shared DAO SPI files; module implementations live under `app/module/*/internal/adapter/` and module repositories.
- **Entities:** Hibernate `@Entity` classes in `domain/datamap/` (16 Java files)
- **DAO access:** Prefer Spring-injected DAO/SPI collaborators in managed beans; keep remaining legacy manual construction isolated behind local adapters/helpers until each path is strangulated.
- **Package:** All classes in `org.researchedc.*` (migrated from `org.akaza.openclinica`)

## TESTING

The `shared/` module has no dedicated test directory. Tests for shared functionality exist in:
- `app/src/test/` — 55 Java test files covering Modulith module, legacy-bridge, and compatibility paths

Legacy `legacy-core/src/test/` (17 files, DBUnit-based DAO/Service tests) was archived during the
`legacy-core` → `shared/` consolidation. These tests require PostgreSQL and have commented-out
test methods awaiting reactivation.

## MODULE STATUS

| Aspect | Status |
|--------|--------|
| Package rename | ✅ `org.akaza.openclinica` → `org.researchedc` |
| Annotations | ✅ `@Repository`/`@Service` applied to all DAOs and services |
| SPI interfaces | ✅ Deleted; caller migration to module-owned ports complete |
| Liquibase migrations | ✅ 208 XML files, versioned from 3.x through 3.18 |
| Strangulation target | 🔶 Active — new code goes to `app/module/` |
| DAO deletion | ✅ `DaoProvider`, direct `new XxxDAO(...)` / `new StudyConfigService(...)`, LegacyDaoFactory, EntityDAO infrastructure, and shared DAO SPI files are removed. Phase 3 ledger: 878/878 rows removed; 0 unused, fallback-SQL, legacy-only, or adapter-gap rows remain. |

## ANTI-PATTERNS

- **NEVER** add new code to `shared/` — add new functionality to `app/module/<name>/`
- **NEVER** import `shared/` DAOs or entities directly from Modulith modules — use adapter pattern
- **AVOID** modifying Liquibase migrations after release — add new migration files
- **DO NOT** bypass service layer — call `@Service` beans from controllers, not DAOs directly
