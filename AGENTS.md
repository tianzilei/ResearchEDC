# ResearchEDC - PROJECT KNOWLEDGE BASE

**Derived from:** OpenClinica v3.x
**Generated:** 2026-05-25
**Updated:** 2026-06-17
**Branch:** master

## OVERVIEW

ResearchEDC is an independently maintained research electronic data capture (EDC) and clinical data management (CDM) platform derived from OpenClinica v3.x. Built on Java 21 with Spring Framework 6.1.5, Hibernate ORM 6.4.4, and Liquibase migrations. Multi-module Maven project supporting Oracle and PostgreSQL.

New React 19 SPA frontend at `frontend/`, built to `frontend/dist/`. Backend modular monolith with Spring Modulith at `org.researchedc.module.*`. `legacy-core/` has been consolidated into `shared/`, but legacy code has **not** been fully removed. Current legacy surface: `shared/` (241 Java files, including 39 DAO SPI files). `web/` has been **completely removed** — its 93 dead servlet/view/helper files were deleted and needed import/validation classes were migrated to `app/`, with later dead leftovers removed. The legacy `ws/` SOAP module is absent from the current tree. Enterprise UI/functionality and active mail-delivery code paths were retired on 2026-06-09; email/contact fields remain as compatibility data pending `docs/refactor/phase-1-email-field-removal-plan.md`.


**当前状态:** `mvn clean compile` ✅ | `ModulithVerificationTest` 1/0/0 ✅ | **Refactor progress 96.0%** ✅ | **Phase 3 DAO ledger 720/878 module-backed; 878/878 covered/removed (100%)** ✅ | Frontend Vitest 25/25 ✅ | **Questionnaire Service** `pytest` 39/39 ✅ | Bare Deploy ✅ | E2E SPA ✅ | **Java module tests 432/432** ✅ | **中文/符号支持** ✅ | **导入/导出优化** ✅ | **Legacy Servlet 注册** ✅ | **ResearchEDC Rename** ✅ | **项目清理** ✅ | **Phase C: SPI widening 24/24** ✅ | **legacy-core → shared 合并** ✅ | **Phase B: Schema ownership ✅ COMPLETE (12 triggers, 27 entities remapped, 24 adapters)** | **Phase II: @SuppressWarnings 消除 ✅ COMPLETE (168→72, -96, 57%, 27 non-deferred all genuine, 45 deferred TableFactory)** | **web/ module DELETED ✅** | **Phase 3 legacy-only: 0 remaining ✅** | **LegacyDaoFactory ELIMINATED ✅** | **EntityDAO infrastructure DELETED ✅** | **Dead code cleanup: -515 files, -46,662 lines ✅**

✅ **Frontend TypeScript 状态:** `pnpm typecheck` — 0 errors
✅ **中文编码:** 全栈 UTF-8，Legacy JSP i18n 修复，ODM 导出修复，SPA `lang="zh-CN"`

## STRUCTURE

```
./
├── app/                     # Spring Boot modular monolith entry point (WAR)
│   └── module/              # Spring Modulith modules (17 modules, 354 Java files)
│       ├── randomization/   # 随机化系统 (算法 + API, 37 文件)
│       ├── export/          # 导出中心 (异步任务, 9 文件)
│       ├── crf/             # CRF 元数据 (含 LegacyCrfAdapter, 21 文件)
│       ├── legacy/          # 遗留网关 (底层 DAO REST 封装, compatibility only)
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
├── shared/                  # 共享领域逻辑与数据访问 — 241 Java files (replaces legacy-core, still legacy-heavy)
│   ├── bean/                # DTOs (81 Java files)
│   ├── dao/                 # 数据访问层 (39 SPI interfaces)
│   ├── domain/              # Hibernate 实体 (103 Java files)
│   ├── job/                 # Quartz infrastructure (4 Java files)
│   ├── core/                # Core resources/utilities (4 Java files)
│   └── ...                  # job, exception, validator, i18n, patterns, config
├── frontend/                # React 19 + TypeScript SPA (pnpm workspace, 102 src TS/TSX files)
├── questionnaire-service/   # Python FastAPI 问卷微服务 (独立部署, 76 Python files)
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
| **Legacy Gateway** | `app/.../module/legacy/` | `/api/legacy/*` DAO REST 封装 + 导入上传端点 |
| **Audit module** | `app/.../module/audit/` | 独立 audit_log 表, 事件驱动, REST API |
| **Study module** | `app/.../module/study/` | 桥接 study 表, REST API |
| **Subject module** | `app/.../module/subject/` | 桥接 subject/study_subject, REST API |
| **Event module** | `app/.../module/event/` | 桥接 study_event/event_crf, REST API |
| **Data Capture module** | `app/.../module/datacapture/` | 桥接 item_data/response_set, REST API |
| **Legacy servlet registration** | retired | No Boot servlet compatibility registration class remains in the current tree |
| **Security config** | `app/.../config/SecurityConfig.java` | DaoAuthenticationProvider + form login |
| **Encoding config** | `app/.../config/CoreResourcesConfig.java` | MessageSource UTF-8 + ODM FreeMarker UTF-8 |
| **Identity module** | `app/.../module/identity/` | 桥接 user_account/study_user_role, REST API |
| **Rule module** | `app/.../module/rule/` | 规则集/规则/表达式 JPA 实体 (gateway only) |
| **Dataset module** | `app/.../module/dataset/` | 数据集 JPA 实体 (gateway only) |
| **Filter module** | `app/.../module/filter/` | 过滤器 JPA 实体 (gateway only) |
| **SubjectGroup module** | `app/.../module/subjectgroup/` | 分组类/组 JPA 实体 (gateway only) |
| **DiscrepancyNote module** | `app/.../module/discrepancynote/` | 差异备注 JPA 实体 (gateway only) |
| **Shared (legacy) logic** | `shared/src/main/java/org/researchedc/` | DAO/domain/service/bean/logic |
| Legacy DAOs | `shared/.../dao/` | 39 DAO SPI Java files; deletion blocked by caller migration |
| Legacy DAO SPI interfaces | `shared/.../dao/spi/` | 39 个接口 (IStudyDAO, ISubjectDAO, ...) |
| Legacy Hibernate entities | `shared/.../domain/datamap/` | ~62 实体, JPA 注解 |
| Import/validation classes | `app/.../control/form/` | Validator, DiscrepancyValidator, FormDiscrepancyNotes (migrated from web/) |
| Liquibase migrations | `shared/.../migration/` | 208 个版本化 schema XML |
| i18n strings | `shared/.../i18n/*.properties` | 6 种语言 |
| Bare deploy | `deploy.sh` | single host deployment entry point |
| Legacy removal plan | `docs/refactor/remove-legacy-code-plan.md` | Current baseline, phases, and deletion gates |
| SPA fallback config | `app/.../config/WebMvcConfig.java` | `/app/**` -> React index.html |

## CONVENTIONS

### Backend
- **Package:** `org.researchedc.*`
- **Beans:** `*Bean` suffix for legacy DTOs (e.g., `StudyBean`)
- **DAOs:** remaining `shared/dao` files are SPI/port interfaces; new data access belongs in module repositories and module-owned ports.
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
| `legacy` | ✅ Built | 0 | 0 | 0 | 9 | 15 | `/api/v1/legacy/*` |
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
| `openrosa` | ✅ Built | 0 | 0 | 5 | 1 | 4 | `/api/v1/openrosa` |

## TESTING ARCHITECTURE

### Backend Tests
Tests live in `app/src/test` (20 files):

| Tier | Base Class | DB Needed | Use Case |
|------|-----------|-----------|----------|
| **Modulith Unit** | JUnit 5 + Mockito | ❌ | Module service tests (20 files, 235 tests) |
| **Servlet Unit** | `junit.framework.TestCase` + Mockito | ❌ | Servlet authorization logic (3 tests) |
| **Modulith Verification** | JUnit 5 + `ApplicationModules` | ❌ | Module boundary verification (1 test) |

**Test run prerequisites:**
- `JAVA_HOME` must point to JDK 21
- PostgreSQL must be running on localhost:5432 for `mvn test` (DAO/Service integration tests)

### E2E Testing
- **Playwright MCP**: Browser automation for SPA login/dashboard/navigation verification
- **API Testing**: 11 Modulith REST endpoint families verified via curl
- **Chinese Encoding**: Full-stack UTF-8 verified — API input/output, DB storage, SPA rendering, pg_dump
- **Import/Export**: REST API (`/api/v1/exports`), file upload (`/api/legacy/import/upload`), PostgreSQL dump verified

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
- **legacy-core → shared:** `legacy-core/` was removed on 2026-05-23. Its code was consolidated into `shared/` with `@Repository`/`@Service` annotations and package rename to `org.researchedc`. This was a module consolidation, **not** full legacy code removal.
- **Frontend TypeScript:** ✅ `pnpm typecheck` — 0 errors (strict mode).
- **DAO constructor baseline:** `DaoProvider` bridge has been removed. Direct legacy DAO/`StudyConfigService` construction (`new XxxDAO(...)` / `new StudyConfigService(...)`) is now 0 matches across `shared/`; `ws/` is absent. All 24 target DAO families are SPI-widened; deletion is gated on migrating callers from legacy SPI names to module-owned ports.

### Legacy DAO Refactor Handoff (2026-06-05)

- **Status:** Phase C SPI widening **COMPLETE** — all **24 DAO families** are SPI-widened. The remaining checked-in DAO surface is SPI-only: 39 Java files under `shared/dao/spi`. `web/` and `ws/` are absent, `LegacyDaoFactory` and `EntityDAO` infrastructure are deleted, and remaining deletion is blocked by callers that still depend on legacy SPI names.
- **Phase B adapters:** 24 `@Primary @Component` adapter classes in `app/module/*/internal/adapter/` bridge all SPI interfaces to module-owned repositories (27 `module_*` tables). A few complex analytical methods (e.g., `getNumItems*`, `findNext`) delegate to parent legacy DAO SQL.
- **DAO families SPI-widened (24 of 24):**
  - ✅ **StudyDAO / StudySubjectDAO / SubjectDAO / UserAccountDAO** — concrete implementations deleted; remaining callers use SPI names and need module-owned port migration.
  - ✅ **CRFDAO → ICrfDAO** — `460fab3f2`, `01efa4b05`
  - ✅ **CRFVersionDAO → ICrfVersionDAO** — ~20 commits (`9da44e612`–`1e337b75b`)
  - ✅ **DiscrepancyNoteDAO → IDiscrepancyNoteDAO** — `0e47f8872`, `ec1b9b0d9`
  - ✅ **EventCRFDAO → EventCRFDao** — `315d3cdf4`
  - ✅ **ItemDAO → IItemDAO** — `8b90a2601`
  - ✅ **ItemDataDAO → IItemDataDAO** — `1b409b230`, `962726f2d`
  - ✅ **ItemGroupDAO → IItemGroupDAO** — `f9d7d5d65`
  - ✅ **ItemFormMetadataDAO → IItemFormMetadataDAO** — `46879ad29`, `3987e59d1`
  - ✅ **ItemGroupMetadataDAO → IItemGroupMetadataDAO** — `46879ad29`
  - ✅ **SectionDAO → ISectionDAO** — `46879ad29`, `3987e59d1`
  - ✅ **StudyEventDAO → IStudyEventDAO** — ~10 commits (`6896446c1`–`df4a832e5`); 45 web/ + 15 shared/ consumer files all SPI-typed
  - ✅ **StudyEventDefinitionDAO → IStudyEventDefinitionDAO** — ~10 commits (`868a4f6fa`–`968391b3b`); 40+ web/ + 15+ shared/ consumer files all SPI-typed
  - ✅ **EventDefinitionCRFDAO → EventDefinitionCRFDao** — `46879ad29`
  - ✅ **RuleSetDAO → IRuleSetDAO** — `cf22f06d2`, `579cbfab0`; 4 web/ + 4 shared/ consumers all SPI-typed
  - ✅ **RuleDAO → IRuleDAO** — `62595dd32`; 1 web/ + 3 shared/ consumers all SPI-typed
  - ✅ **DatasetDAO → DatasetDao** — `d374b275c`; web/ has commented code only, shared/ uses SPI
  - ✅ **FilterDAO → FilterDao** — web/ has commented code only, all consumers use SPI
  - ✅ **StudyGroupClassDAO → StudyGroupClassDao** — 4 shared/ consumers all SPI-typed
  - ✅ **StudyGroupDAO → StudyGroupDao** — 3 shared/ consumers all SPI-typed
  - ✅ **ArchivedDatasetFileDAO → ArchivedDatasetFileDao** — `58278d68b`; 8 consumer files converted
- **Refactor progress snapshot (2026-06-17):** active workflow inventory is 924/963 closed (**96.0%**), Phase 3 DAO method coverage is 878/878 module-backed or removed (**100%**), DAO method blockers are 0/878 unused rows (**0%**), shared/ reduced from 793 to 241 files (**69.6%**), DAO-surface deletion is 147/186 files (**79.0%**), LegacyDaoFactory eliminated, EntityDAO infrastructure deleted, dead code cleanup complete.
- **Phase 3 ledger status (2026-06-17):** `docs/refactor/phase-3-dao-replacement-ledger.{md,csv}` tracks 878 SPI methods: 720 `module-backed`, 0 `fallback-sql`, 0 `legacy-only`, 0 `adapter-gap`, 0 `unused`, and 158 `removed`. Deletion is now gated by migrating callers from legacy SPI names to module-owned ports.
- **Remaining work:** `shared/dao` is now SPI-only with 39 DAO SPI Java files. Remaining deletion is blocked by callers that still depend on legacy SPI names instead of module-owned ports. Latest caller-migration slices moved audit database-changelog, audit-user-event, and study-subject event audit reads off legacy SPI injection.
- **Gauntlet commands:**
  - `git status --short`
  - `mvn -pl app -am compile -DskipTests && mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false`
  - `mvn test -pl app -am 2>&1 | grep "Tests run:"`
- **Refactor rule of thumb:** do not modify released migrations, do not bypass `SecureController`, do not add new legacy code to `shared/`, and verify the SPI covers every invoked method before widening.

### Phase B Schema Ownership Handoff (2026-06-07)

- **Status:** Phase B schema ownership is **COMPLETE**. 27 entities mapped to `module_*` tables, 12 bidirectional sync triggers registered, 24 SPI adapters bridging to module-owned repositories.
- **10 bidirectional sync trigger migration files** created (3,494 lines total): study, subject, event, datacapture, crf, identity, rule, dataset-filter, discrepancy-note, subjectgroup. Each has two PostgreSQL trigger functions with `pg_trigger_depth() > 1` recursion guard. Registered in `release.xml`.
- **5 JPA entities remapped** to module-owned tables: `FilterEntity` (`module_filter`), `DatasetEntity` (`module_dataset`), `UserAccountEntity` (`module_user_account`), `RoleEntity` (`module_role`), `StudyGroupClassEntity` (`module_study_group_class`). Sequences renamed to match (e.g., `module_filter_id_seq`).
- **New adapter code:** `FilterDaoAdapter.java` + `StudyGroupClassDaoAdapter.java` with unit tests.
- **Historical note:** the former `DaoRegistrar` exclusion list was updated during Phase B for `FilterDAO` and `StudyGroupClassDAO`; `DaoRegistrar` itself has since been removed after the concrete `shared/src/main/java/org/researchedc/dao` classes disappeared.
- **StudyGroupClassRepository** enhanced with 4 native SQL queries (`findByStudyOrChildStudy`, `findByStudyOrChildStudyAndStatus`).
- **PostgreSQL validation:** completed on disposable databases. `shared/src/main/resources/migration/master.xml` applied cleanly, `scripts/ci/check-phase-b-postgres.sh` verified 54 sync functions, 54 sync triggers, representative bidirectional insert/update/delete sync for `study`, `filter`, and `discrepancy_note`, and repeated update convergence without recursion loops.
- **Discrepancy-note migration fix:** legacy `discrepancy_note` has no `entity_id`; Phase B copy/sync now stores `NULL AS entity_id` module-side and omits that column when writing back to legacy.
- **Remaining Phase B work:** none. Next work is Phase 0 legacy route/workflow inventory and Phase 1 vertical JSP/servlet deletion slices in `docs/refactor/remove-legacy-code-plan.md`.
- **Verification:** `bash scripts/ci/check-phase-b-migrations.sh` ✅ | full Liquibase PostgreSQL update ✅ | `scripts/ci/check-phase-b-postgres.sh` ✅ | commit `0963eec2c`

## SUBMODULE REFERENCES

- [shared/AGENTS.md](./shared/AGENTS.md) — Shared domain logic and data access
- [app/AGENTS.md](./app/AGENTS.md) — Spring Boot entry point, config, and Modulith modules
- [frontend/AGENTS.md](./frontend/AGENTS.md) — React 19 SPA (TypeScript, Vite, Ant Design)
- [questionnaire-service/AGENTS.md](./questionnaire-service/AGENTS.md) — Python FastAPI microservice
- [LEGACY_REFACTOR_PLAN.md](./.sisyphus/LEGACY_REFACTOR_PLAN.md) — Remaining legacy refactoring roadmap
- [Remove Legacy Code Plan](./docs/refactor/remove-legacy-code-plan.md) — Explicit deletion plan and measurable gates
- [Next Refactor And Removal Plan](./docs/refactor/next-refactor-removal-plan.md) — Current legacy-removal status, import hardening, and Phase 3 DAO deletion queue
