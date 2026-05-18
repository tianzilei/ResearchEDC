# OpenClinica - PROJECT KNOWLEDGE BASE

**Generated:** 2026-05-18  
**Branch:** master  

## OVERVIEW

OpenClinica is an open-source Electronic Data Capture (EDC) and Clinical Data Management (CDM) platform for clinical trials. Built on Java 21 with Spring Framework 6.1.5, Hibernate ORM 6.4.4, and Liquibase migrations. Multi-module Maven project supporting Oracle and PostgreSQL.

New React 19 SPA frontend at `frontend/`, built to `app/src/main/resources/static/`. Backend modular monolith with Spring Modulith at `org.akaza.openclinica.module.*`. Legacy code at `core/` (737 files), `web/` (482 files + 417 JSP), `ws/` (57 files) is being incrementally strangulated into Modulith modules.

**当前状态:** `mvn clean compile` ✅ | `mvn clean package -DskipTests` ✅ | `ModulithVerificationTest` 1/0/0 ✅ | Frontend TypeScript 0 errors ✅ | ESLint 0 errors ✅ | **Questionnaire Service** `pytest` 31/31 ✅ | Docker Compose ✅ | E2E API ✅

## STRUCTURE

```
./
├── app/                     # Spring Boot modular monolith entry point (WAR)
│   └── module/              # Spring Modulith modules
│       ├── randomization/   # 随机化系统 (算法 + API, 33 文件)
│       ├── export/          # 导出中心 (异步任务, 7 文件)
│       ├── crf/             # CRF 元数据 (含 LegacyCrfAdapter, 7 文件)
│       ├── notification/    # 通知模块 (事件驱动邮件, 5 文件)
│       ├── legacy/          # 遗留网关 (底层 DAO REST 封装, 5 文件)
│       ├── audit/           # 审计日志 (事件驱动 + 独立表, 11 文件)
│       ├── study/           # 研究管理 (映射 study 表, 8 文件)
│       ├── subject/         # 受试者管理 (映射 subject/study_subject, 10 文件)
│       ├── event/           # 访视管理 (映射 study_event/event_crf, 14 文件)
│       ├── datacapture/     # 数据采集 (映射 item_data/response_set, 11 文件)
│       └── identity/        # 身份权限 (映射 user_account/study_user_role, 10 文件)
├── core/                    # 遗留领域逻辑 — ~737 源文件 (待绞杀)
│   ├── bean/                # DTOs (250 文件)
│   ├── dao/                 # 数据访问层 (140 文件)
│   ├── domain/              # Hibernate 实体 (166 文件)
│   ├── service/             # 业务服务 (60 文件)
│   ├── logic/               # 规则引擎 (57 文件)
│   └── ...                  # job, exception, validator, i18n, patterns
├── frontend/                # React 19 + TypeScript SPA (pnpm workspace)
├── questionnaire-service/   # Python FastAPI 问卷微服务 (独立部署)
├── web/                     # 遗留 Web UI — ~482 文件 + 419 JSP (待绞杀)
├── ws/                      # 遗留 SOAP — ~57 文件 (待绞杀)
├── deploy/                  # Docker Compose, Nginx, scripts
├── docker/                  # Dockerfiles
├── pom.xml                  # Maven parent
├── AGENTS.md
├── MODIFICATIONS.md
├── PLAN.md
└── .sisyphus/               # Sisyphus AI work plans
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| **Questionnaire Service (Python)** | `questionnaire-service/apps/api/` | FastAPI + SQLAlchemy + Pydantic v2 |
| Questionnaire models | `questionnaire-service/apps/api/app/models/` | 7 SQLAlchemy ORM models |
| Scoring engine | `questionnaire-service/apps/api/app/scoring/` | ISI/GAD-7/PHQ-9/ESS scorers |
| API routers | `questionnaire-service/apps/api/app/api/v1/routers/` | 8 router modules |
| **Randomization module** | `app/.../module/randomization/` | 3 种算法, 8 实体, REST API |
| **Export module** | `app/.../module/export/` | 异步任务状态机, REST API |
| **CRF module** | `app/.../module/crf/` | CRF 列表/版本/预览, LegacyCrfAdapter |
| **Notification module** | `app/.../module/notification/` | 事件驱动, ApplicationEvent 模式 |
| **Legacy Gateway** | `app/.../module/legacy/` | `/api/legacy/*` DAO REST 封装 |
| **Audit module** | `app/.../module/audit/` | 独立 audit_log 表, 事件驱动, REST API |
| **Study module** | `app/.../module/study/` | 桥接 study 表, REST API |
| **Subject module** | `app/.../module/subject/` | 桥接 subject/study_subject, REST API |
| **Event module** | `app/.../module/event/` | 桥接 study_event/event_crf, REST API |
| **Data Capture module** | `app/.../module/datacapture/` | 桥接 item_data/response_set, REST API |
| **Identity module** | `app/.../module/identity/` | 桥接 user_account/study_user_role, REST API |
| SPA fallback config | `app/.../config/WebMvcConfig.java` | `/app/**` -> React index.html |
| Legacy Hibernate entities | `core/.../domain/datamap/` | ~62 实体, JPA 注解 |
| Legacy DAOs (JDBC) | `core/.../dao/*/` | EntityDAO 子类, SQL digester |
| Legacy DAOs (JPA) | `core/.../dao/hibernate/` | AbstractDomainDao 子类 |
| Web controllers | `web/.../control/**/*.java` | 186 个 SecureController 子类 |
| REST controllers | `web/.../controller/*.java` | Spring @Controller |
| SOAP endpoints | `ws/.../ws/*Endpoint.java` | 7 个 Spring WS 端点 |
| JSP pages | `web/.../webapp/WEB-INF/jsp/**/*.jsp` | 417 页面 |
| Liquibase migrations | `core/.../migration/` | 版本化 schema 变更 |
| i18n strings | `core/.../i18n/*.properties` | 6 种语言 |
| Docker Compose | `deploy/compose/` | dev/test/prod 三层 |

## CONVENTIONS

### Backend
- **Package:** `org.akaza.openclinica.*`
- **Beans:** `*Bean` suffix for legacy DTOs (e.g., `StudyBean`)
- **DAOs:** `*DAO` suffix, extend `EntityDAO<K extends EntityBean>`
- **Servlets:** `*Servlet` suffix, extend `SecureController` or `CoreSecureController`
- **Modules:** `org.akaza.openclinica.module.<name>.*` with `@ApplicationModule`
- **Module entities:** `@Entity(name = "Module<Name>")` to avoid collision with legacy entities
- **Module FKs:** Plain `Integer`/`Long` columns, NOT JPA `@ManyToOne` (follows randomization pattern)
- **Anti-corruption layer:** Legacy DAO access only in `module/<name>/internal/adapter/`
- **Logging:** SLF4J + Logback (configured in `logback-spring.xml`)

### Frontend
- **Framework:** React 19 + TypeScript 5.8 strict + Vite 6
- **UI library:** Ant Design 5 with ConfigProvider theme
- **Routing:** React Router 7, browser router with `/app/*` prefix
- **Data fetching:** TanStack Query 5 via typed `useAppQuery`/`useAppMutation` wrappers
- **API client:** Fetch-based `ApiClient` class (JSON + FormData support)
- **Auth:** Keycloak OIDC via `AuthProvider` context (session token based)
- **Quality:** `pnpm typecheck` (0 errors) | `pnpm lint` (0 errors) | `pnpm build` (no warnings)

## MODULITH MODULES INVENTORY

| Module | Status | Entities | Repos | Services | Controller | DTOs | API Base Path |
|--------|--------|----------|-------|----------|------------|------|---------------|
| `randomization` | ✅ Complete | 8 | 6 | 3 | 1 | 9 | `/api/v1/randomization` |
| `export` | ✅ Complete | 1 | 1 | 1 | 1 | 2 | `/api/v1/exports` |
| `crf` | ✅ Fixed | 0 | 0 | 1 | 1 | 4 | `/api/v1/crfs` |
| `notification` | ✅ Complete | 0 | 0 | 2 | 0 | 0 | event-driven |
| `legacy` | ✅ Built | 0 | 0 | 0 | 2 | 2 | `/api/v1/legacy/*` |
| `audit` | ✅ Extracted | 1 | 1 | 1 | 1 | 1 | `/api/v1/audit` |
| `study` | ✅ Extracted | 1 | 1 | 1 | 1 | 2 | `/api/v1/studies` |
| `subject` | ✅ Extracted | 2 | 2 | 1 | 1 | 2 | `/api/v1/subjects` |
| `event` | ✅ Extracted | 3 | 3 | 1 | 1 | 3 | `/api/v1/events` |
| `datacapture` | ✅ Extracted | 3 | 3 | 1 | 1 | 3 | `/api/v1/data-capture` |
| `identity` | ✅ Built | 2 | 2 | 1 | 1 | 2 | `/api/v1/identity` |

## TESTING ARCHITECTURE

### Backend Tests
Tests live in `core/src/test` (17 files), `web/src/test` (2 files), and `app/src/test`:

| Tier | Base Class | DB Needed | Use Case |
|------|-----------|-----------|----------|
| **Unit** | `junit.framework.TestCase` | ❌ | Pure logic, format conversion, expression parsing |
| **DAO** | `HibernateOcDbTestCase` (DBUnit) | ✅ | CRUD operations, query correctness |
| **Service** | `HibernateOcDbTestCase` | ✅ | Business service integration, rule filtering |
| **Modulith** | JUnit 5 + `ApplicationModules` | ❌ | Module boundary verification |

**Test run prerequisites:**
- `JAVA_HOME` must point to JDK 21
- PostgreSQL must be running on localhost:5432 for `mvn test` (core DBUnit tests)

## ANTI-PATTERNS (THIS PROJECT)

### Legacy Backend
- **NEVER** bypass `SecureController` for session/auth checks
- **DO NOT** write SQL directly in servlets - use DAO layer
- **AVOID** modifying migration files after they've been released
- **NEVER** hardcode file paths - use `CoreResources.getField()`
- **DO NOT** ignore transaction boundaries - use `@Transactional` or `TransactionTemplate`

### Modulith Modules
- **DO NOT** create circular dependencies between modules (enforced by `ModulithVerificationTest`)
- **ALWAYS** use `ApplicationEvents` for cross-module communication
- **NEVER** `@Autowired` beans from other modules directly — use events
- **NEVER** import `core.dao.*`, `core.bean.*`, or `core.domain.*` in module public classes
- **ALWAYS** put legacy DAO access in `module/<name>/internal/adapter/` classes
- **AVOID** adding new code to legacy packages — add to the appropriate module

## COMMANDS

```bash
# === Build & Verify ===
mvn clean compile -DskipTests
mvn clean package -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false

# === Frontend ===
cd frontend && pnpm install && pnpm build && cd ..

# === Questionnaire Service ===
cd questionnaire-service/apps/api
python -m pytest app/tests/ -v
```

## NOTES

- **Database:** Supports Oracle and PostgreSQL
- **Security:** Legacy Spring Security for JSP; Keycloak OIDC for SPA
- **Routing:** `/app/*` -> React SPA, `/legacy/*` -> JSP, `/q/*` -> questionnaire, `/api/*` -> REST
- **Modulith:** Only `org.akaza.openclinica.module.*` is verified; legacy packages are excluded
- **Version:** 3.18-SNAPSHOT

## SUBMODULE REFERENCES

- [core/AGENTS.md](./core/AGENTS.md) - Domain logic and data access
- [web/AGENTS.md](./web/AGENTS.md) - Web UI and controllers
- [ws/AGENTS.md](./ws/AGENTS.md) - SOAP web services
- [LEGACY_REFACTOR_PLAN.md](./.sisyphus/LEGACY_REFACTOR_PLAN.md) - Remaining legacy refactoring roadmap
