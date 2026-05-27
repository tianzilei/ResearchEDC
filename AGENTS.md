# ResearchEDC - PROJECT KNOWLEDGE BASE

**Derived from:** OpenClinica v3.x  
**Generated:** 2026-05-25  
**Branch:** master

## OVERVIEW

ResearchEDC is an independently maintained research electronic data capture (EDC) and clinical data management (CDM) platform derived from OpenClinica v3.x. Built on Java 21 with Spring Framework 6.1.5, Hibernate ORM 6.4.4, and Liquibase migrations. Multi-module Maven project supporting Oracle and PostgreSQL.

New React 19 SPA frontend at `frontend/`, built to `frontend/dist/`. Backend modular monolith with Spring Modulith at `org.researchedc.module.*`. Legacy code consolidated into `shared/` module, with `web/` (484 files + 419 JSP) and `ws/` (75 files) being incrementally strangulated into Modulith modules.

**当前状态:** `mvn clean compile` ✅ | `ModulithVerificationTest` 1/0/0 ✅ | Frontend Vitest 25/25 ✅ | **Questionnaire Service** `pytest` 31/31 ✅ | Bare Deploy ✅ | E2E API ✅ | **ResearchEDC Rename** ✅ | **项目清理** ✅ | **Phase C: LegacyDaoConfig 归零** ✅ | **legacy-core → shared 合并** ✅ | **Java module tests 150+** ✅

✅ **Frontend TypeScript 状态:** `pnpm typecheck` — 0 errors

## STRUCTURE

```
./
├── app/                     # Spring Boot modular monolith entry point (WAR)
│   └── module/              # Spring Modulith modules (17 个, ~250 文件)
│       ├── randomization/   # 随机化系统 (算法 + API, 37 文件)
│       ├── export/          # 导出中心 (异步任务, 9 文件)
│       ├── crf/             # CRF 元数据 (含 LegacyCrfAdapter, 21 文件)
│       ├── notification/    # 通知模块 (事件驱动邮件, 5 文件)
│       ├── legacy/          # 遗留网关 (底层 DAO REST 封装, 25 文件)
│       ├── audit/           # 审计日志 (事件驱动 + 独立表, 16 文件)
│       ├── study/           # 研究管理 (映射 study 表, 19 文件)
│       ├── subject/         # 受试者管理 (映射 subject/study_subject, 19 文件)
│       ├── event/           # 访视管理 (映射 study_event/event_crf, 24 文件)
│       ├── datacapture/     # 数据采集 (映射 item_data/response_set, 14 文件)
│       ├── identity/        # 身份权限 (映射 user_account/study_user_role, 11 文件)
│       ├── dashboard/       # 仪表盘 Bootstrap (用户/研究/站点上下文 + 待办 + 状态, 8 文件)
│       ├── rule/            # 规则引擎 (JPA 实体 + 仓库, 13 文件)
│       ├── dataset/         # 数据集管理 (JPA 实体 + 仓库, 7 文件)
│       ├── filter/          # 过滤器管理 (JPA 实体 + 仓库, 7 文件)
│       ├── subjectgroup/    # 受试者分组 (JPA 实体 + 仓库, 9 文件)
│       └── discrepancynote/ # 差异备注管理 (JPA 实体 + 仓库, 7 文件)
├── shared/                  # 共享领域逻辑与数据访问 — ~770 文件 (取代 legacy-core)
│   ├── bean/                # DTOs (253 文件)
│   ├── dao/                 # 数据访问层 (169 文件, 含 29 SPI 接口)
│   ├── domain/              # Hibernate 实体 (166 文件)
│   ├── service/             # 业务服务 (60 文件)
│   ├── logic/               # 规则引擎 (57 文件)
│   └── ...                  # job, exception, validator, i18n, patterns, config
├── frontend/                # React 19 + TypeScript SPA (pnpm workspace, 94 文件)
├── questionnaire-service/   # Python FastAPI 问卷微服务 (独立部署, 74 文件)
├── web/                     # 遗留 Web UI — ~484 文件 + 419 JSP (待绞杀)
├── ws/                      # 遗留 SOAP — ~75 文件 (待绞杀)
├── deploy/                  # Bare host reverse proxy / observability configs
├── deploy.sh                # Single bare host deploy shell
├── pom.xml                  # Maven parent
├── docs/                    # Documentation
│   └── refactor/            # Refactoring plans & baseline
├── research-edc-bom/        # Maven BOM version management
├── scripts/                 # CI helper scripts
├── shared/                  # 共享模块 (AGENTS.md)
├── AGENTS.md
├── MODIFICATIONS.md
└── .sisyphus/               # AI work plans
```

## WHERE TO LOOK

| Task | Location | Notes |
|------|----------|-------|
| **Questionnaire Service (Python)** | `questionnaire-service/apps/api/` | FastAPI + SQLAlchemy + Pydantic v2 |
| Questionnaire models | `questionnaire-service/apps/api/app/models/` | 7 SQLAlchemy ORM models |
| Scoring engine | `questionnaire-service/apps/api/app/scoring/` | ISI/GAD-7/PHQ-9/ESS/PSQI scorers |
| API routers | `questionnaire-service/apps/api/app/api/v1/routers/` | 9 router modules |
| **Randomization module** | `app/.../module/randomization/` | 3 种算法, 8 实体, REST API |
| **Export module** | `app/.../module/export/` | 异步任务状态机, REST API |
| **Dashboard module** | `app/.../module/dashboard/` | 引导 (用户/研究/站点上下文 + 模块列表), 待办, 状态, 最近活动 |
| **CRF module** | `app/.../module/crf/` | CRF 列表/版本/预览, LegacyCrfAdapter |
| **Notification module** | `app/.../module/notification/` | 事件驱动, ApplicationEvent 模式 |
| **Legacy Gateway** | `app/.../module/legacy/` | `/api/legacy/*` DAO REST 封装 |
| **Audit module** | `app/.../module/audit/` | 独立 audit_log 表, 事件驱动, REST API |
| **Study module** | `app/.../module/study/` | 桥接 study 表, REST API |
| **Subject module** | `app/.../module/subject/` | 桥接 subject/study_subject, REST API |
| **Event module** | `app/.../module/event/` | 桥接 study_event/event_crf, REST API |
| **Data Capture module** | `app/.../module/datacapture/` | 桥接 item_data/response_set, REST API |
| **Identity module** | `app/.../module/identity/` | 桥接 user_account/study_user_role, REST API |
| **Rule module** | `app/.../module/rule/` | 规则集/规则/表达式 JPA 实体 (gateway only) |
| **Dataset module** | `app/.../module/dataset/` | 数据集 JPA 实体 (gateway only) |
| **Filter module** | `app/.../module/filter/` | 过滤器 JPA 实体 (gateway only) |
| **SubjectGroup module** | `app/.../module/subjectgroup/` | 分组类/组 JPA 实体 (gateway only) |
| **DiscrepancyNote module** | `app/.../module/discrepancynote/` | 差异备注 JPA 实体 (gateway only) |
| **Shared (legacy) logic** | `shared/src/main/java/org/researchedc/` | DAO/domain/service/bean/logic |
| Legacy DAOs (JPA) | `shared/.../dao/hibernate/` | AbstractDomainDao 子类 (67 文件) |
| Legacy DAO SPI interfaces | `shared/.../dao/spi/` | 29 个接口 (IStudyDAO, ISubjectDAO, ...) |
| Legacy Hibernate entities | `shared/.../domain/datamap/` | ~62 实体, JPA 注解 |
| Web controllers | `web/.../control/**/*.java` | 186 个 SecureController 子类 |
| REST controllers | `web/.../controller/*.java` | Spring @Controller |
| SOAP endpoints | `ws/.../ws/*Endpoint.java` | 7 个 Spring WS 端点 |
| JSP pages | `web/.../webapp/WEB-INF/jsp/**/*.jsp` | 419 页面 |
| Liquibase migrations | `shared/.../migration/` | 193 个版本化 schema 变更 |
| i18n strings | `shared/.../i18n/*.properties` | 6 种语言 |
| Bare deploy | `deploy.sh` | single host deployment entry point |
| SPA fallback config | `app/.../config/WebMvcConfig.java` | `/app/**` -> React index.html |

## CONVENTIONS

### Backend
- **Package:** `org.researchedc.*`
- **Beans:** `*Bean` suffix for legacy DTOs (e.g., `StudyBean`)
- **DAOs:** `*DAO` suffix, extend `EntityDAO<K extends EntityBean>`; SPI interfaces prefixed with `I`
- **Servlets:** `*Servlet` suffix, extend `SecureController` or `CoreSecureController`
- **Modules:** `org.researchedc.module.<name>.*` with `@ApplicationModule`
- **Module entities:** `@Entity(name = "Module<Name>")` to avoid collision with shared entities
- **Module FKs:** Plain `Integer`/`Long` columns, NOT JPA `@ManyToOne` (follows randomization pattern)
- **Anti-corruption layer:** Legacy DAO access only in `module/<name>/internal/adapter/`
- **Logging:** SLF4J + Logback (configured in `logback-spring.xml`)

### Frontend
- **Framework:** React 19 + TypeScript 5.8 strict + Vite 6
- **UI library:** Ant Design 5 with ConfigProvider theme
- **Routing:** React Router 7, browser router with `/app/*` prefix
- **Data fetching:** TanStack Query 5 via typed `useAppQuery`/`useAppMutation` wrappers
- **API client:** Fetch-based `ApiClient` class (JSON + FormData support, `credentials: same-origin`, CSRF token injection)
- **Auth:** Spring Security form login with server-side Session (HttpOnly cookie + CookieCsrfTokenRepository)
- **Quality:** `pnpm typecheck` (0 errors) | `pnpm lint` (3 errors, 77 warnings) | `pnpm test` (25/25 ✅)

## MODULITH MODULES INVENTORY

| Module | Status | Entities | Repos | Services | Controller | DTOs | API Base Path |
|--------|--------|----------|-------|----------|------------|------|---------------|
| `randomization` | ✅ Complete | 8 | 6 | 3 | 1 | 9 | `/api/v1/randomization` |
| `export` | ✅ Complete | 1 | 1 | 1 | 1 | 2 | `/api/v1/exports` |
| `crf` | ✅ Complete | 6 | 6 | 1 | 1 | 4 | `/api/v1/crfs` |
| `notification` | ✅ Complete | 0 | 0 | 2 | 0 | 0 | event-driven |
| `legacy` | ✅ Built | 0 | 0 | 0 | 2 | 2 | `/api/v1/legacy/*` |
| `audit` | ✅ Extracted | 1 | 1 | 1 | 1 | 1 | `/api/v1/audit` |
| `study` | ✅ Extracted | 1 | 1 | 1 | 1 | 2 | `/api/v1/studies` |
| `subject` | ✅ Extracted | 2 | 2 | 1 | 1 | 2 | `/api/v1/subjects` |
| `dashboard` | ✅ Complete | 0 | 0 | 1 | 1 | 4 | `/api/v1/dashboard` |
| `event` | ✅ Extracted | 3 | 3 | 1 | 1 | 3 | `/api/v1/events` |
| `datacapture` | ✅ Extracted | 3 | 3 | 1 | 1 | 3 | `/api/v1/data-capture` |
| `identity` | ✅ Built | 2 | 2 | 1 | 1 | 2 | `/api/v1/identity` |
| `rule` | ✅ Built | 4 | 4 | 1 | 0 | 0 | (gateway only) |
| `dataset` | ✅ Built | 1 | 1 | 1 | 0 | 0 | (gateway only) |
| `filter` | ✅ Built | 1 | 1 | 1 | 0 | 0 | (gateway only) |
| `subjectgroup` | ✅ Built | 2 | 2 | 1 | 0 | 0 | (gateway only) |
| `discrepancynote` | ✅ Built | 1 | 1 | 1 | 0 | 0 | (gateway only) |

## TESTING ARCHITECTURE

### Backend Tests
Tests live in `app/src/test` (20 files), `web/src/test` (2 files):

| Tier | Base Class | DB Needed | Use Case |
|------|-----------|-----------|----------|
| **Modulith Unit** | JUnit 5 + Mockito | ❌ | Module service tests (20 files, ~150 tests) |
| **Servlet Unit** | `junit.framework.TestCase` + Mockito | ❌ | Servlet authorization logic (3 tests) |
| **Modulith Verification** | JUnit 5 + `ApplicationModules` | ❌ | Module boundary verification (1 test) |

**Test run prerequisites:**
- `JAVA_HOME` must point to JDK 21
- PostgreSQL must be running on localhost:5432 for `mvn test` (DAO/Service integration tests)

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
- **NEVER** import `shared.dao.*`, `shared.bean.*`, or `shared.domain.*` in module public classes
- **ALWAYS** put legacy DAO access in `module/<name>/internal/adapter/` classes
- **AVOID** adding new code to `shared/` — add to the appropriate module

## COMMANDS

```bash
# === Build & Verify ===
mvn clean compile -DskipTests
mvn clean package -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false

# === Frontend ===
cd frontend && pnpm install && pnpm build && cd ..
pnpm typecheck  # 0 errors
pnpm test --run

# === Questionnaire Service ===
cd questionnaire-service/apps/api
python -m pytest app/tests/ -v
```

## NOTES

- **Database:** Supports Oracle and PostgreSQL
- **Security:** Spring Security form login (Session Cookie + CSRF); Legacy Spring Security for JSP
- **Routing:** `/app/*` -> React SPA, `/legacy/*` -> JSP, `/q/*` -> questionnaire, `/api/*` -> REST
- **Modulith:** Only `org.researchedc.module.*` is verified; `shared/` packages are excluded
- **Version:** 0.1
- **legacy-core → shared:** `legacy-core/` was removed on 2026-05-23. All code consolidated into `shared/` module with `@Repository`/`@Service` annotations and package rename to `org.researchedc`.
- **Frontend TypeScript:** ✅ `pnpm typecheck` — 0 errors (strict mode).
- **DAO deletion blocked:** `DaoProvider.getDao()` call sites are now 0. Remaining blocker is direct legacy DAO/`StudyConfigService` construction (`new XxxDAO(...)` / `new StudyConfigService(...)`) across legacy services, DAO internals, ODM/export logic, validators, and a few web/ws helpers (215 matches as of 2026-05-27).

## SUBMODULE REFERENCES

- [shared/AGENTS.md](./shared/AGENTS.md) — Shared domain logic and data access
- [web/AGENTS.md](./web/AGENTS.md) — Web UI and controllers
- [ws/AGENTS.md](./ws/AGENTS.md) — SOAP web services
- [app/AGENTS.md](./app/AGENTS.md) — Spring Boot entry point, config, and Modulith modules
- [frontend/AGENTS.md](./frontend/AGENTS.md) — React 19 SPA (TypeScript, Vite, Ant Design)
- [questionnaire-service/AGENTS.md](./questionnaire-service/AGENTS.md) — Python FastAPI microservice
- [LEGACY_REFACTOR_PLAN.md](./.sisyphus/LEGACY_REFACTOR_PLAN.md) — Remaining legacy refactoring roadmap
