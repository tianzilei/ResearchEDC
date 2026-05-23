# app/ - Spring Boot Modular Monolith Entry Point

**Module:** Application entry point, configuration, and Modulith modules  
**Files:** ~244 Java files (module/) + config classes, 16 Modulith modules  

> Entry point: `OpenClinicaApplication.java` — Spring Boot WAR packaging.  
> Config classes in `org.researchedc.config.*` handle Hibernate, security (Keycloak OIDC + legacy Spring Security), mail, scheduling, and OpenAPI.

## STRUCTURE

```
app/src/main/java/org/researchedc/
├── OpenClinicaApplication.java   # @SpringBootApplication entry point
├── config/                       # 14 configuration classes
│   ├── SecurityConfig.java       # Keycloak JWT + legacy session filter
│   ├── HibernateConfig.java      # Dual DataSource (legacy + module)
│   ├── WebMvcConfig.java         # SPA fallback to /app/index.html
│   └── ... (DbConfig, MailConfig, SchedulerConfig, OpenApiConfig, etc.)
└── module/                       # 16 Spring Modulith modules
    ├── audit/                    # Audit logging (event-driven, independent table)
    ├── crf/                      # CRF metadata (includes LegacyCrfAdapter)
    ├── datacapture/              # Data collection (item_data / response_set)
    ├── dataset/                  # Dataset management (gateway only)
    ├── discrepancynote/          # Discrepancy note management (gateway only)
    ├── event/                    # Study event / event_crf (CQRS pattern)
    ├── export/                   # Export center (async task state machine)
    ├── filter/                   # Filter management (gateway only)
    ├── identity/                 # User account / study_user_role
    ├── legacy/                   # Legacy DAO REST gateway
    ├── notification/             # Event-driven email notifications
    ├── randomization/            # 3 randomization algorithms, REST API
    ├── rule/                     # Rule engine (gateway only)
    ├── study/                    # Study management (CQRS pattern)
    ├── subject/                  # Subject / study_subject (CQRS pattern)
    └── subjectgroup/             # Subject grouping (gateway only)
```

## CONVENTIONS

- **Package base:** `org.researchedc`
- **Module structure:** `module/<name>/{entity,repository,service,dto,controller}/` — some modules add `event/`, `enums/`, `internal/adapter/`
- **Entities:** `@Entity(name = "Module<Name>")` avoids name collision with legacy `domain/datamap/` entities
- **FKs:** Plain `Integer`/`Long` columns, NOT JPA `@ManyToOne` (follows randomization pattern as reference)
- **Anti-corruption layer:** Legacy DAO access restricted to `module/<name>/internal/adapter/` only
- **Cross-module events:** `ApplicationEvents` from Spring Modulith — never `@Autowired` across module boundaries
- **Tests:** JUnit 5 + `@SpringBootTest` where DB needed, `ModulithVerificationTest` enforces no circular deps

## KEY CONFIGURATIONS

| Config | File | Purpose |
|--------|------|---------|
| Security | `config/SecurityConfig.java` | Keycloak JWT auth + legacy session bridge |
| OIDC Bridge | `config/OidcSessionBridgeSuccessHandler.java` | Session token exchange for JSP |
| Hibernate | `config/HibernateConfig.java` | Dual DataSource: legacy + module entities |
| SPA Routes | `config/WebMvcConfig.java` | `/app/**` → React `index.html` fallback |
| OpenAPI | `config/OpenApiConfig.java` | Swagger UI at `/swagger-ui.html` |

## TESTING

- **ModulithVerificationTest** — Validates no circular module dependencies (no DB needed)
- **Module service tests** — 12 test files under `module/*/service/`, use `@SpringBootTest`
- **LegacyGatewayContractTest** — Contract test for `/api/v1/legacy/*` endpoints
- **TestDataFactory** — Shared test data builder in `testutil/`

## ANTI-PATTERNS

- **NEVER** `@Autowired` beans from another module — use `ApplicationEvents`
- **NEVER** import `shared.dao.*` / `shared.bean.*` / `shared.domain.*` in module public classes
- **NEVER** bypass `LegacyGateway` to call legacy DAOs from module controllers
- **ALWAYS** put legacy access in `internal/adapter/` classes
- **DO NOT** add new code to `shared/` — add new functionality as a module here
