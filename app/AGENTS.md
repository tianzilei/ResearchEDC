# app/ - Spring Boot Modular Monolith Entry Point

**Module:** Application entry point, configuration, and Modulith modules  
**Files:** 417 Java files total, including 391 under `module/`; 17 Modulith modules

> Entry point: `OpenClinicaApplication.java` — Spring Boot WAR packaging.  
> Config classes in `org.researchedc.config.*` handle Hibernate, security (Spring Security form login + CSRF), scheduling, and OpenAPI.

## STRUCTURE

```
app/src/main/java/org/researchedc/
├── OpenClinicaApplication.java   # @SpringBootApplication entry point
├── config/                       # 9 configuration classes
│   ├── SecurityConfig.java       # Spring Security form login, CSRF, session management
│   ├── HibernateConfig.java      # Dual DataSource (legacy + module)
│   ├── WebMvcConfig.java         # SPA fallback to /app/index.html
│   └── ... (DbConfig, SchedulerConfig, OpenApiConfig, etc.)
└── module/                       # 17 Spring Modulith modules
    ├── audit/                    # Audit logging (event-driven, independent table)
    ├── crf/                      # CRF metadata (includes LegacyCrfAdapter)
    ├── dashboard/                # Dashboard bootstrap, tasks, status, recent activity
    ├── datacapture/              # Data collection (item_data / response_set)
    ├── dataset/                  # Dataset management (gateway only)
    ├── discrepancynote/          # Discrepancy note management (gateway only)
    ├── event/                    # Study event / event_crf (CQRS pattern)
    ├── export/                   # Export center (async task state machine)
    ├── filter/                   # Filter management (gateway only)
    ├── identity/                 # User account / study_user_role
    ├── legacy/                   # Legacy DAO REST gateway
    ├── openrosa/                 # OpenRosa REST/XML compatibility API
    ├── randomization/            # 3 randomization algorithms, REST API
    ├── rule/                     # Rule engine (gateway only)
    ├── study/                    # Study management (CQRS pattern)
    ├── subject/                  # Subject / study_subject (CQRS pattern)
    └── subjectgroup/             # Subject grouping (gateway only)
```

## CONVENTIONS

- **Package base:** `org.researchedc`
- **Module structure:** `module/<name>/{entity,repository,service,dto,controller}/` — some modules add `event/`, `enums/`, `internal/adapter/`
- **Entities:** `@Entity(name = "Module<Name>")` keeps module mappings explicit and stable
- **FKs:** Plain `Integer`/`Long` columns, NOT JPA `@ManyToOne` (follows randomization pattern as reference)
- **Anti-corruption layer:** Legacy DAO access restricted to `module/<name>/internal/adapter/` only
- **Legacy removal:** adapters are temporary containment boundaries, not proof that legacy code is removed. Keep public module APIs free of `shared/` types and retire adapters only after repository-backed behavior and tests replace the delegated legacy methods.
- **Cross-module events:** `ApplicationEvents` from Spring Modulith — never `@Autowired` across module boundaries
- **Tests:** JUnit 5 + `@SpringBootTest` where DB needed, `ModulithVerificationTest` enforces no circular deps

## KEY CONFIGURATIONS

| Config | File | Purpose |
|--------|------|---------|
| Security | `config/SecurityConfig.java` | Spring Security form login, CSRF, session management |
| Hibernate | `config/HibernateConfig.java` | Module-owned JPA entities and Hibernate `SessionFactory` |
| SPA Routes | `config/WebMvcConfig.java` | `/app/**` → React `index.html` fallback |
| OpenAPI | `config/OpenApiConfig.java` | Swagger UI at `/swagger-ui.html` |

## TESTING

- **ModulithVerificationTest** — Validates no circular module dependencies (no DB needed)
- **Module tests** — 55 Java test files under `app/src/test`, covering service, controller, adapter, and boundary behavior
- **LegacyGatewayContractTest** — Contract test for `/api/v1/legacy/*` endpoints
- **TestDataFactory** — Shared test data builder in `testutil/`

## ANTI-PATTERNS

- **NEVER** `@Autowired` beans from another module — use `ApplicationEvents`
- **NEVER** import `shared.dao.*` / `shared.bean.*` / `shared.domain.*` in module public classes
- **NEVER** bypass `LegacyGateway` to call legacy DAOs from module controllers
- **ALWAYS** put legacy access in `internal/adapter/` classes
- **DO NOT** add new code to `shared/` — add new functionality as a module here
