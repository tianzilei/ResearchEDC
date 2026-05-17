# OpenClinica - PROJECT KNOWLEDGE BASE

**Generated:** 2026-05-17  
**Branch:** master  

## OVERVIEW

OpenClinica is an open-source Electronic Data Capture (EDC) and Clinical Data Management (CDM) platform for clinical trials. Built on Java 21 with Spring Framework 6.1.5, Hibernate ORM 6.4.4, and Liquibase migrations. Multi-module Maven project supporting Oracle and PostgreSQL.

New React 19 SPA frontend at `frontend/`, built to `app/src/main/resources/static/`. Backend modular monolith with Spring Modulith at `org.akaza.openclinica.module.*`.

**当前状态:** `mvn clean compile` ✅ | `mvn clean package -DskipTests` ✅ | `mvn test` 11/11 pass ✅ | Frontend TypeScript 0 errors ✅ | ESLint 0 errors ✅

## STRUCTURE

```
./
├── app/            # Spring Boot modular monolith entry point (WAR)
│   └── module/     # Spring Modulith modules (randomization, export, crf, notification, identity)
├── core/           # Domain logic, DAOs, services, Hibernate entities
├── frontend/       # React 19 + TypeScript SPA (pnpm workspace)
├── web/            # Web UI (JSP), servlets, controllers, REST endpoints  
├── ws/             # SOAP web services, study/subject/event endpoints
├── deploy/         # Docker Compose, Nginx, scripts
├── docker/         # Dockerfiles
├── scripts/        # Build, deploy, release scripts
├── pom.xml         # Maven parent - Spring 6.1.5, Hibernate 6.4.4, Java 21
└── AGENTS.md
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| Spring Modulith modules | `app/src/main/java/org/akaza/openclinica/module/` | `@ApplicationModule` annotated packages |
| SPA fallback config | `app/src/main/java/org/akaza/openclinica/config/WebMvcConfig.java` | `/app/**` → React index.html |
| App config | `app/src/main/resources/application.yml` | Spring Boot profiles |
| Frontend source | `frontend/src/` | React 19 + TS + Ant Design |
| Frontend build output | `app/src/main/resources/static/` | Auto-built by `pnpm build` |
| Database entities | `core/src/main/java/org/akaza/openclinica/domain/datamap/` | Hibernate annotations |
| Data access (SQL) | `core/src/main/java/org/akaza/openclinica/dao/**/*.java` | EntityDAO pattern |
| Web controllers | `web/src/main/java/org/akaza/openclinica/control/**/*.java` | Extends SecureController |
| REST endpoints | `web/src/main/java/org/akaza/openclinica/controller/*.java` | Spring @Controller |
| SOAP services | `ws/src/main/java/org/akaza/openclinica/ws/*Endpoint.java` | Spring WS |
| JSP pages | `web/src/main/webapp/WEB-INF/jsp/**/*.jsp` | Legacy UI layer |
| Migrations | `core/src/main/resources/migration/` | Liquibase changelog |
| i18n strings | `core/src/main/resources/org/akaza/openclinica/i18n/*.properties` | 6 languages |
| Properties config | `*/src/main/filters/datainfo.properties` | DB connection, paths |
| Docker Compose | `deploy/compose/` | dev/test/prod stacks |
| Nginx config | `deploy/nginx/nginx.conf` | Production reverse proxy |

## CONVENTIONS

### Backend
- **Package:** `org.akaza.openclinica.*`
- **Beans:** `*Bean` suffix for DTOs (e.g., `StudyBean`)
- **DAOs:** `*DAO` suffix, extend `EntityDAO<K extends EntityBean>`
- **Servlets:** `*Servlet` suffix, extend `SecureController` or `CoreSecureController`
- **Modules:** `org.akaza.openclinica.module.<name>.*` with `@ApplicationModule`
- **Logging:** SLF4J + Logback (configured in `logback-spring.xml`)

### Frontend
- **Framework:** React 19 + TypeScript 5.8 strict + Vite 6
- **UI library:** Ant Design 5 with ConfigProvider theme
- **Routing:** React Router 7, browser router with `/app/*` prefix
- **Data fetching:** TanStack Query 5 via typed `useAppQuery`/`useAppMutation` wrappers
- **API client:** Fetch-based `ApiClient` class (JSON + FormData support)
- **Auth:** Keycloak OIDC via `AuthProvider` context (session token based)
- **State:** Server state via TanStack Query; session state via React context
- **Permissions:** `usePermissions` hook derives from JWT roles via `ROLE_PERMISSIONS` matrix
- **Form engine:** `FormField` + `DataEntryForm` + `FormStatus` (auto-save via `useAutoSave`)
- **Build:** `pnpm build` → `app/src/main/resources/static/`
- **Dev server:** `pnpm dev` on port 5173, proxies `/api` `/actuator` `/auth` to localhost:8080
- **Quality:** `pnpm typecheck` (0 errors) | `pnpm lint` (0 errors) | `pnpm build` (no warnings)

## TESTING ARCHITECTURE

### Backend Tests
Tests live in `core/src/test` (17 files), `web/src/test` (2 files), and `app/src/test`:

| Tier | Base Class | DB Needed | Use Case |
|------|-----------|-----------|----------|
| **Unit** | `junit.framework.TestCase` | ❌ | Pure logic, format conversion, expression parsing |
| **DAO** | `HibernateOcDbTestCase` (extends DBUnit `DataSourceBasedDBTestCase`) | ✅ | CRUD operations, query correctness |
| **Service** | `HibernateOcDbTestCase` | ✅ | Business service integration, rule filtering |
| **Modulith** | JUnit 5 + `ApplicationModules` | ❌ | Module boundary verification |

**Key patterns:**
- DAO/Service tests load full Spring context: all `applicationContext-*.xml` via `ClassPathXmlApplicationContext`
- Test data follows convention: `{package}/testdata/{ClassName}.xml` — DBUnit FlatXmlDataSet auto-loaded per test class
- Pure unit tests (like `SubmitDataServletTest`) use Mockito for role/permission mocking
- `test.properties` configures DB connection (Oracle/PostgreSQL)
- Base classes `HibernateOcDbTestCase` and `OcDbTestCase` in `core/.../templates/`
- `ModulithVerificationTest` — verifies `org.akaza.openclinica.module.*` package boundaries via ArchUnit

### Frontend Tests (planned)
- Vitest + jsdom for unit and component tests
- Playwright for E2E tests

**Test run prerequisites:**
- `JAVA_HOME` must point to JDK 21 (Java 25 breaks Mockito/ByteBuddy)
- PostgreSQL must be running on localhost:5432 for `mvn test` (core DBUnit tests)
- Start test DB: `docker run -d --name oc-test-pg -e POSTGRES_USER=clinica -e POSTGRES_PASSWORD=clinica -e POSTGRES_DB=openclinica-TEST-3.12 -p 5432:5432 postgres:17-alpine`

## ANTI-PATTERNS (THIS PROJECT)

### Legacy Backend
- **NEVER** bypass `SecureController` for session/auth checks
- **DO NOT** write SQL directly in servlets - use DAO layer
- **AVOID** modifying migration files after they've been released
- **NEVER** hardcode file paths - use `CoreResources.getField()`
- **DO NOT** ignore transaction boundaries - use `@Transactional` or `TransactionTemplate`
- **AVOID** Java 21+ preview features (project targets Java 21 LTS compatibility)

### Frontend
- **NEVER** use `as any` or `@ts-expect-error` — strict mode enforced
- **DO NOT** bypass the API client — always use `apiClient.get/post/put/delete`
- **NEVER** import from `react-router-dom` directly in pages — use `useNavigate` from hooks
- **DO NOT** mix TanStack Query with raw `useState` for server state
- **AVOID** Ant Design `Provider` nesting — use `AppProviders` composition

### Modulith Modules
- **DO NOT** create circular dependencies between modules (enforced by `ModulithVerificationTest`)
- **ALWAYS** use `ApplicationEvents` for cross-module communication
- **NEVER** `@Autowired` beans from other modules directly — use events
- **AVOID** adding new code to legacy packages — add to the appropriate module

## UNIQUE STYLES

- **DAO Pattern:** All DB access through EntityDAO subclasses with `execute()`/`executeFind()`
- **Bean Pattern:** All data transfer objects extend `EntityBean` with audit fields
- **Servlet Security:** All web controllers extend `SecureController` which handles authz
- **ODM Export:** Clinical data exports use CDISC ODM XML format
- **Rules Engine:** Expression-based edit checks in `core/.../rule/expression/`
- **Modulith Modules:** `@ApplicationModule` with `allowedDependencies` for boundary enforcement
- **Frontend Permission Matrix:** `ROLE_PERMISSIONS` maps 8 study roles to 18 permissions

## COMMANDS

```bash
# === Backend ===
# Compile all modules
mvn clean compile -DskipTests

# Package with tests
mvn clean package

# Package without tests
mvn clean package -DskipTests

# Run Modulith verification only
mvn test -pl app -am -Dtest=ModulithVerificationTest -DfailIfNoTests=false

# Run all tests (requires PostgreSQL)
mvn test

# === Frontend ===
cd frontend
pnpm install           # Install dependencies
pnpm dev               # Dev server at localhost:5173
pnpm build             # Production build → app/src/main/resources/static/
pnpm lint              # ESLint check
pnpm typecheck         # TypeScript check

# === Full Pipeline ===
# Build everything (frontend + backend)
cd frontend && pnpm build && cd ..
mvn clean compile -DskipTests

# === Docker ===
docker compose -f deploy/compose/docker-compose.dev.yml up --build
```

## NOTES

- **Database:** Supports Oracle and PostgreSQL - test queries exist in `core/src/main/resources/queries/`
- **Security:** Spring Security 3.2 with LDAP support, role-based access control
- **Routing:** New frontend at `/app/*`, legacy JSP at `/legacy/*` via Nginx
- **Modulith:** Only `org.akaza.openclinica.module.*` is verified; legacy packages are excluded
- **Scheduling:** Quartz jobs for data export and imports
- **Version:** Currently 3.18-SNAPSHOT (pom.xml)

## SUBMODULE REFERENCES

- [core/AGENTS.md](./core/AGENTS.md) - Domain logic and data access
- [web/AGENTS.md](./web/AGENTS.md) - Web UI and controllers
- [ws/AGENTS.md](./ws/AGENTS.md) - SOAP web services
