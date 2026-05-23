# shared/ - Shared Domain Logic & Data Access

**Module:** Legacy domain logic, data access, entities, and business services  
**Files:** ~770 Java files  
**Package:** `org.researchedc.*`

> Formerly `legacy-core/`. Consolidated into `shared/` module with `@Repository`/`@Service` annotations
> and package rename from `org.akaza.openclinica` to `org.researchedc`. This module is the target of
> the Strangler Fig pattern — new functionality goes into `app/module/` Modulith modules.

## STRUCTURE

```
shared/src/main/java/org/researchedc/
├── bean/         # DTOs — 253 files (managestudy, extract, login, admin, oid, core, rule,
│                 #             service, odmbeans, submit, masking)
├── config/       # CoreResourcesConfig.java — resource bundle accessor
├── core/         # Core utilities — formatters, file access, resource helpers (7 files)
├── dao/          # Data access layer — 169 files
│   ├── hibernate/ # AbstractDomainDao subclasses (67 files, JPA-based)
│   ├── spi/       # DAO SPI interfaces (29 files)
│   ├── managestudy/ # Study/subject DAOs
│   ├── submit/      # Data entry DAOs
│   └── ...          # rule, admin, extract, core, cache, ws, login, logic, service
├── domain/       # Hibernate entities — 166 files (datamap, rule, crfdata, xform, user, etc.)
├── exception/    # Custom exceptions (7 files)
├── i18n/         # Internationalization utilities + 22 .properties files (6 languages)
├── job/          # Quartz scheduled jobs (9 files)
├── log/          # Logging utilities (13 files)
├── logic/        # Business logic — 57 files (rule engine, ODM export, scoring, masking)
├── patterns/     # Observer pattern infrastructure (9 files)
├── service/      # Business services — 60 files (managestudy, extract, crfdata, rule, pmanage, user, subject)
└── validator/    # Validation logic (6 files)

shared/src/main/resources/
├── migration/    # Liquibase schema migrations (193 XML files)
└── *.properties  # i18n resource bundles (22 files)
```

## KEY COMPONENTS

| Area | Files | Description |
|------|-------|-------------|
| **DAO (Hibernate)** | 67 | `AbstractDomainDao` subclasses — JPA-based CRUD for all legacy entities |
| **DAO (SPI)** | 29 | Interface definitions for dependency injection (`IStudyDAO`, `ISubjectDAO`, etc.) |
| **Domain Entities** | 166 | Hibernate `@Entity` classes mapping to database tables (`datamap/` has 62) |
| **DTO Beans** | 253 | `EntityBean` subclasses — data transfer objects for legacy servlets |
| **Business Services** | 60 | `@Service` classes — study, subject, event, CRF, extract, rule, CRF data operations |
| **Rule Engine** | 57 | Expression parsing, validation actions, rule runner, scoring functions |
| **Liquibase Migrations** | 193 | Versioned schema changes from OpenClinica 3.x through 3.18 |
| **Quartz Jobs** | 9 | Scheduled tasks (data import, export, cleanup) |

## CONVENTIONS

- **Beans:** `*Bean` suffix for DTOs, extend `EntityBean<K>` with audit fields (id, createdDate, ownerId...)
- **DAOs:** `*DAO` suffix, extend `EntityDAO<K extends EntityBean>` or `AbstractDomainDao`
- **Services:** `@Service` + `@Transactional` for business logic orchestration
- **Entities:** Hibernate `@Entity` + XML mapping files in `domain/datamap/` (~62 entities)
- **DAO access:** Use `DaoProvider.getDao(XxxDAO.class)` in web/ws code; `@Autowired` in Spring beans
- **Package:** All classes in `org.researchedc.*` (migrated from `org.akaza.openclinica`)

## TESTING

The `shared/` module has no dedicated test directory. Tests for shared functionality exist in:
- `app/src/test/` — 20 Java test files (Modulith module service tests)
- `web/src/test/` — 2 unit tests (Mockito-based servlet tests)

Legacy `legacy-core/src/test/` (17 files, DBUnit-based DAO/Service tests) was archived during the
`legacy-core` → `shared/` consolidation. These tests require PostgreSQL and have commented-out
test methods awaiting reactivation.

## MODULE STATUS

| Aspect | Status |
|--------|--------|
| Package rename | ✅ `org.akaza.openclinica` → `org.researchedc` |
| Annotations | ✅ `@Repository`/`@Service` applied to all DAOs and services |
| SPI interfaces | ✅ 29 interfaces for DI (replaces direct DAO class references) |
| Liquibase migrations | ✅ ~193 XML files, versioned from 3.x through 3.18 |
| Strangulation target | 🔶 Active — new code goes to `app/module/` |
| DAO deletion blocked | 🔶 ~1,100 `DaoProvider.getDao()` call sites in web/ws still reference concrete DAO classes |

## ANTI-PATTERNS

- **NEVER** add new code to `shared/` — add new functionality to `app/module/<name>/`
- **NEVER** import `shared/` DAOs or entities directly from Modulith modules — use adapter pattern
- **AVOID** modifying Liquibase migrations after release — add new migration files
- **DO NOT** bypass service layer — call `@Service` beans from controllers, not DAOs directly
