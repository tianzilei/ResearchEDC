# ResearchEDF

**版本:** 0.1  
**最后更新:** 2026-05-26  
**JSP 迁移进度:** 225/419 (54%) — 194 页通过 LegacyFrame 向后兼容  
**许可证:** GNU LGPL

ResearchEDC is an independently maintained research electronic data capture and clinical research data management platform derived from OpenClinica v3.x.

该项目专注于研究者发起的临床研究，支持电子病例报告表、受试者管理、研究工作流、数据导出以及未来扩展的随机化、问卷集成、针刺临床试验工作流和神经生理数据元数据管理。

> ⚠️ **免责声明：** 本项目的源代码按"原样"提供，仅供学习和研究参考。**这是一个实验性项目，不适用于实际生产环境。** 任何直接或间接使用本项目代码、构建产物或部署实例所导致的任何损失，包括但不限于数据丢失、系统故障、法律合规风险及其他任何形式的损害，项目贡献者及维护者均不承担任何责任。使用者应自行评估风险并承担全部责任。

---

## 技术栈

### 后端
| 组件 | 版本 |
|------|------|
| **Java** | 21 LTS |
| **Spring Framework** | 6.1.5 |
| **Spring Boot** | 3.2.5 |
| **Spring Modulith** | 1.1.4 |
| **Spring Security** | 6.2.3 |
| **Spring WS** | 4.0.10 |
| **Hibernate ORM** | 6.4.4.Final |
| **Jakarta EE** | 10 (Servlet 6.0, JSP 3.1, JAXB 4.0) |
| **Jackson** | 2.17.0 |
| **Maven** | 3.9.x |
| **Liquibase** | 4.26.0 |

### 前端
| 组件 | 版本 |
|------|------|
| **框架** | React 19 |
| **语言** | TypeScript 5.8 (strict mode) |
| **构建** | Vite 6 + pnpm |
| **UI 组件库** | Ant Design 5 |
| **路由** | React Router 7 |
| **服务端状态** | TanStack Query 5 |
| **测试** | Vitest + React Testing Library |
| **认证** | Spring Security form login (Session Cookie + CSRF) |

**应用服务器:** Tomcat 10.1.x 推荐  
**数据库:** PostgreSQL 14+ / Oracle 19c+

---

## 项目结构

```
ResearchEDC/
├── pom.xml             # Maven 父 POM (5 子模块: bom, shared, web, ws, app)
├── frontend/           # React 19 SPA (pnpm workspace, 28 pages)
│   ├── src/
│   │   ├── api/        # API 客户端 + 类型
│   │   ├── components/ # 通用组件 + form-engine + DiscrepancyNotes
│   │   │   ├── form-engine/          # 表单引擎 (DataEntryForm, FormField, FormStatus)
│   │   │   └── questionnaire-builder/ # 问卷可视化 Builder
│   │   ├── hooks/      # 15+ TanStack Query hooks (CRF, events, data capture, rules, etc.)
│   │   ├── layouts/    # AppLayout (顶栏 + 侧栏 + 内容区)
│   │   ├── pages/      # 28 页面组件 (admin/ crf/ datacapture/ events/ export/ questionnaire/
│   │   │   │           #   randomization/ rules/ studies/ subject/)
│   │   ├── providers/  # AuthProvider (Keycloak OIDC) + AppProviders
│   │   ├── router/     # React Router 7 配置 (30+ routes)
│   │   ├── styles/     # 设计系统: dot-grid 纹理, glass panel, 动效, Ant Design 主题
│   │   └── types/      # 10+ TypeScript 类型文件 (study, crf, event, datacapture, rules, user)
│   ├── vite.config.ts  # Vite 6 配置 (API 代理 / 构建输出)
│   └── package.json
├── questionnaire-service/  # Python FastAPI 问卷微服务 (独立部署)
│   ├── apps/api/            # FastAPI 后端 (models, services, scoring, routers, workers)
│   ├── infra/               # Docker Compose (PostgreSQL, Redis, MinIO)
│   └── packages/            # SurveyJS 问卷 schema (ISI, GAD-7, PHQ-9, ESS)
├── app/                # Spring Boot 模块化单体入口
│   ├── src/main/java/
│   │   └── org/researchedc/
│   │       ├── config/      # WebMvcConfig (SPA fallback), WebServiceConfig, OpenApiConfig
│   │       └── module/      # Spring Modulith 模块 (16 个, 244 文件)
│   │           ├── randomization/  # 随机化系统 (算法 + API)
│   │           ├── export/         # 导出中心 (异步任务)
│   │           ├── crf/            # CRF 元数据 (REST API)
│   │           ├── notification/   # 通知模块 (事件驱动邮件)
│   │           ├── legacy/         # 遗留网关 (底层 DAO REST 封装)
│   │           ├── audit/          # 审计日志 (事件驱动 + 独立表)
│   │           ├── study/          # 研究管理 (study 表桥接)
│   │           ├── subject/        # 受试者管理 (subject/study_subject 桥接)
│   │           ├── event/          # 访视管理 (study_event/event_crf 桥接)
│   │           ├── datacapture/    # 数据采集 (item_data/response_set 桥接)
│   │           ├── identity/       # 身份权限 (user_account/study_user_role 桥接)
│   │           ├── rule/           # 规则引擎 (JPA 实体 + 仓库)
│   │           ├── dataset/        # 数据集管理 (JPA 实体 + 仓库)
│   │           ├── filter/         # 过滤器管理 (JPA 实体 + 仓库)
│   │           ├── subjectgroup/   # 受试者分组 (JPA 实体 + 仓库)
│   │           └── discrepancynote/# 差异备注管理 (JPA 实体 + 仓库)
│   └── src/main/resources/
│       ├── application.yml # profile 配置
│       └── static/         # 前端构建产物 (自动生成)
├── shared/              # 共享领域逻辑 & 数据访问 (770 源文件, 取代 legacy-core)
│   ├── dao/             # 数据访问层 (169 文件: hibernate 67, spi 29, 各子域 DAOs)
│   ├── domain/          # Hibernate 实体 (166 文件, 含 datamap/ 62 实体)
│   ├── service/         # 业务服务层 (60 文件)
│   ├── bean/            # DTOs (253 文件)
│   ├── logic/           # 规则引擎 (57 文件)
│   ├── job/             # Quartz 定时任务 (9 文件)
│   └── migration/       # Liquibase 迁移脚本 (193 文件)
├── web/                # Web UI & REST API (484 源文件, 419 JSP — ~225 replaced via React)
│   ├── control/        # Servlet 控制器 (SecureController)
│   ├── controller/     # Spring MVC REST 控制器
│   └── webapp/         # JSP 页面 (剩余 194 页通过 LegacyFrame iframe 访问)
├── ws/                 # SOAP Web 服务 (75 源文件)
│   └── endpoint/       # Spring WS 端点
├── deploy/             # Docker Compose + Nginx 配置
│   ├── compose/        # dev/test/prod 三层 Compose
│   └── nginx/          # 生产级 Nginx 配置
├── research-edc-bom/   # Maven BOM 版本管理
|── scripts/            # 构建/部署/发布脚本
├── .dockerignore       # Docker 构建忽略规则
├── Makefile            # 常用开发/部署命令
└── .github/workflows/  # CI 工作流 (5 个: backend, frontend, docker, legacy-refactor)
```

---

## 路由架构

```
/legacy/*       → 旧 OpenClinica JSP (194 页向后兼容)
/app/*          → 新 React SPA (28 页面, 30+ 路由, index.html fallback)
/app/studies/create  → 研究创建 8 步向导
/app/studies/:id          → 研究详情/编辑/站点/事件定义/规则/分组
/app/subjects/:id         → 受试者详情/访视/数据录入
/app/admin/*              → 管理页面 (用户/审计/系统/CRF/任务)
/app/data-export/*        → 导出中心 + 数据集
/q/*            → 问卷受试者填写页 (独立于 AppLayout)
/api/v1/*       → Spring Boot Modulith REST API (study/subject/event/datacapture/...)
/api/legacy/*   → Legacy 网关桥接 API (notes/rules/crfs/datasets/groups)
/login          → React SPA 登录页 (Spring Security form login)
/api/v1/auth/*  → 认证 API (login, logout, me)
/actuator/*     → Spring Boot Actuator
```

---

## 构建与运行

```bash
# === 完整构建 (前端 + 后端) ===
cd frontend && pnpm install && pnpm build && cd ..
mvn clean compile -DskipTests

# === 仅后端 ===
mvn clean compile -DskipTests

# === 仅前端开发 ===
cd frontend
pnpm dev  # localhost:5173

# === Modulith 模块验证 ===
mvn test -pl app -am -Dtest=ModulithVerificationTest

# === 部署 ===
mvn clean package -DskipTests
cp app/target/OpenClinica.war $CATALINA_HOME/webapps/

# === Docker (含 Maven cache mount 加速) ===
docker compose -f deploy/compose/docker-compose.dev.yml up --build

# === 问卷服务 (Python FastAPI) ===
cd questionnaire-service/infra
docker compose up -d                         # 启动 PostgreSQL + Redis + MinIO
cd ../apps/api
PYTHONPATH="$PWD" alembic upgrade head        # 数据库迁移
PYTHONPATH="$PWD" uvicorn app.main:app        # 启动 API (localhost:8000)
python -m pytest app/tests/ -v               # 运行 31 个测试
```

---

## 测试架构

| 层级 | 基类 | 数据库 | 当前状态 |
|------|------|--------|---------|
| 模块单元测试 | JUnit 5 + Mockito `@SpringBootTest` | ❌ | ✅ ~150 tests pass (app) |
| Modulith 验证 | JUnit 5 + `ApplicationModules` | ❌ | ✅ 1 test pass |
| Servlet 单元测试 | `junit.framework.TestCase` + Mockito | ❌ | ✅ 3 tests pass (web) |
| DAO 集成测试 | `HibernateOcDbTestCase` (DBUnit) | ✅ | ⚠️ 待启用 (shared 模块) |
| **前端测试** | Vitest + React Testing Library | ❌ | **✅ 25 tests pass** |
| **问卷服务单元测试** | **pytest** | ❌ | **✅ 31 tests pass** |
| **问卷服务 E2E** | **curl + pytest** | ✅ | **✅ Docker Compose + API** |

### 运行测试
```bash
# Java 全量测试 (需要 PostgreSQL Docker)
mvn test

# Modulith 模块验证 (无需数据库)
mvn test -pl app -am -Dtest=ModulithVerificationTest

# 启动 PostgreSQL Docker
docker run -d --name oc-test-pg -e POSTGRES_USER=clinica \
  -e POSTGRES_PASSWORD=clinica \
  -e POSTGRES_DB=openclinica-TEST-3.12 \
  -p 5432:5432 postgres:17-alpine

# 问卷服务测试 (Python)
cd questionnaire-service/apps/api
python -m pytest app/tests/ -v  # 31 tests
```

**注意:** `JAVA_HOME` 需指向 JDK 21 (Homebrew 默认 JDK 25 会导致 Mockito/ByteBuddy 不兼容)

### 前端质量门禁
| 检查 | 命令 | 状态 |
|------|------|------|
| TypeScript strict | `pnpm typecheck` | ⚠️ **41 errors, 79 warnings** |
| ESLint | `pnpm lint` | ✅ 0 errors |
| 构建 | `pnpm build` | ✅ |
| 测试 | `pnpm test --run` | ✅ **25/25 tests pass** |

### 设计系统 (2026-05-18 "Precision Clinical" 重构)
- **配色**: Jade teal (`#099A87`) 主色 + warm brass (`#D4A854`) 点缀 + deep slate (`#0F1A2E`) 基底 + warm paper (`#F8F5F0`) 表面色
- **排版**: Sora (标题) + DM Sans (正文) Google Fonts
- **动效**: 页面进场 `fadeInUp` 动画、卡片 hover 上浮、统计卡片交错进场
- **背景**: 全局 dot-grid 图纸纹理 (radial-gradient)
- **组件**: AppLayout 精修 (brass 边框 header, max-width 内容区), Dashboard 重设计 (问候头像、彩色统计卡片、活动时间线、SVG 环形图、快捷操作), ErrorPage/NotFound 定制页面

测试数据文件: `shared/src/test/resources/org/researchedc/{dao,service}/testdata/`

---

## 核心架构模式

- **DAO 模式:** 所有数据库访问通过 `EntityDAO<K>` 子类或 SPI 接口
- **Bean 模式:** DTO 继承 `EntityBean` 含审计字段 (id, createdDate, ownerId...)
- **SecureController:** 所有 Servlet 继承此类自动处理会话/权限
- **服务层:** `@Service` + `@Transactional` 封装业务编排
- **Modulith 模块:** `@ApplicationModule` 定义模块边界，`ApplicationEvents` 跨模块通信
- **前端权限矩阵:** 8 种角色 × 18 种权限，通过 JWT token claims 驱动菜单显示

---

## 开发规范

- 禁止绕过 `SecureController` 做会话检查
- 禁止在 Servlet 中直接写 SQL — 使用 DAO 层
- 禁止硬编码文件路径 — 使用 `CoreResources.getField()`
- 禁止修改已发布的 Liquibase 迁移文件
- 禁止 `as any` / `@ts-expect-error` — 前端强制 strict 模式
- 包结构: `bean.*` (DTO), `dao.*` (数据访问), `service.*` (业务), `domain.*` (实体)
- 新代码应写入 `module/<name>/` 而非 `shared/`

---

## 关键模块

| 模块 | 位置 | 功能 |
|------|------|------|
| 共享领域逻辑 | `shared/` | DAO、Domain、Service、规则引擎 (770 文件) |
| 研究管理 (遗留) | `web/.../control/managestudy/` | 研究/Site/Subject 管理 (186 Servlet) |
| 数据录入 (遗留) | `web/.../control/submit/` | CRF 数据录入、双录入、电子签名 |
| 规则引擎 (遗留) | `shared/.../logic/` | 表达式验证、自动计算 |
| SOAP API (遗留) | `ws/` | 研究/Subject/CRUD |
| **随机化系统** | `module/randomization/` | 3 种算法, 8 表 REST API |
| **导出中心** | `module/export/` | 异步任务, 取消/重试 |
| **CRF 元数据** | `module/crf/` | CRF 列表/版本/预览, REST API |
| **Legacy 网关** | `module/legacy/` | 遗留 DAO REST 封装 |
| **审计模块** | `module/audit/` | 事件驱动审计, 独立表 |
| **研究模块** | `module/study/` | study 表桥接 REST API |
| **受试者模块** | `module/subject/` | subject/study_subject 桥接 |
| **访视模块** | `module/event/` | study_event/event_crf 桥接 |
| **数据采集模块** | `module/datacapture/` | item_data/response_set 桥接 |
| **身份模块** | `module/identity/` | user_account/study_user_role 桥接 |
| **规则模块** | `module/rule/` | 规则集/规则/表达式 JPA 实体 + 仓库 |
| **数据集模块** | `module/dataset/` | 数据集 JPA 实体 + 仓库 |
| **过滤器模块** | `module/filter/` | 过滤器 JPA 实体 + 仓库 |
| **受试者分组模块** | `module/subjectgroup/` | 分组类/分组 JPA 实体 + 仓库 |
| **差异备注模块** | `module/discrepancynote/` | 差异备注 JPA 实体 + 仓库 |
| 通知模块 | `module/notification/` | 事件驱动邮件 |
| **问卷服务** | `questionnaire-service/apps/api/` | **FastAPI + SQLAlchemy + 评分引擎** |
| 前台 SPA | `frontend/src/` | React 19 管理界面 (28 页面, ~225 JSP 已替换) |

### 前端页面一览

#### 核心页面
| 页面 | 路由 | 功能 |
|------|------|------|
| Dashboard | `/app/dashboard` | 登录后首页, 研究概览统计 |
| 研究列表 | `/app/studies` | 研究/站点浏览, 快速创建 |
| 研究创建向导 | `/app/studies/create` | **8 步向导**: 协议→赞助→设计→条件→招募→机构→联系→确认 |
| 研究详情 | `/app/studies/:id` | 协议信息/设计/机构概览, 快捷操作 |
| 研究编辑 | `/app/studies/:id/edit` | 编辑 40+ 研究字段 |
| 站点管理 | `/app/studies/:id/sites` | 站点创建/列表/状态管理 |
| 事件定义 | `/app/studies/:id/event-definitions` | 研究事件类型 CRUD |
| 受试者列表 | `/app/subjects` | 受试者检索/创建/入组 |
| 受试者详情 | `/app/subjects/:id` | 档案/入组信息/访视列表 |
| 访视列表 | `/app/subjects/:id/events` | 访视计划/完成/状态 |
| **数据录入** | `/app/subjects/:id/events/:eid/crfs/:cid/entry` | **CRF 分段录入: 自动保存, 差异备注, 事件完成** |
| 规则列表 | `/app/studies/:studyId/rules` | 规则集列表/详情 |
| 受试者分组 | `/app/studies/:id/subject-groups` | 分组类/组管理和创建 |

#### 管理页面
| 页面 | 路由 | 功能 |
|------|------|------|
| 管理首页 | `/app/admin` | 管理导航 (用户/审计/系统/CRF/任务) |
| 用户管理 | `/app/admin/users` | 用户列表/创建/角色分配 |
| 审计日志 | `/app/admin/audit-log` | 全局审计日志, 模块筛选 |
| 系统配置 | `/app/admin/system` | 健康检查/版本/组件状态 |
| CRF 库 | `/app/admin/crf-library` | CRF 浏览/版本查看/创建/编辑 |
| **任务管理** | `/app/admin/jobs` | **导出任务状态查看/刷新** |

#### 数据功能页面
| 页面 | 路由 | 功能 |
|------|------|------|
| 导出中心 | `/app/data-export` | 创建/跟踪/取消导出任务 |
| **数据集** | `/app/data-export/datasets` | **数据集列表/创建** |
| CRF 列表 | `/app/crfs` | CRF 库浏览 |
| CRF 预览 | `/app/crfs/:versionId` | 版本详情 + 分段结构 |

#### 随机化页面
| 页面 | 路由 | 功能 |
|------|------|------|
| 随机化 | `/app/randomization` | Scheme Dashboard + 创建 |
| Scheme 详情 | `/app/randomization/schemes/:id` | 详情/激活/关闭 |
| 分配 | `/app/randomization/schemes/:id/allocate` | 受试者随机分配 |
| 揭盲 | `/app/randomization/schemes/:id/unblinding` | 揭盲请求/审核 |
| 审计日志 | `/app/randomization/schemes/:id/audit` | 操作审计追踪 |

#### 问卷服务页面
| 页面 | 路由 | 功能 |
|------|------|------|
| 受试者填写 | `/q/fill/:token` | SurveyJS 渲染, 草稿/提交, 自动评分 |
| 模板管理 | `/app/questionnaires/templates` | 问卷模板 CRUD |
| 版本编辑 | `/app/questionnaires/templates/:id/versions` | 可视化 Builder / JSON / 预览 |
| 分配管理 | `/app/questionnaires/assignments` | 访视分配, 批量创建 |
| 回复审核 | `/app/questionnaires/responses` | 查看/审核/锁定/更正 |
| 我的问卷 | `/app/questionnaires/my-tasks` | 受试者任务列表, 进度 |
| 导出管理 | `/app/questionnaires/export` | CSV/XLSX/JSON 导出任务 |

## 国际化

支持 6 种语言: en, de, es, fr, pt, zh  
资源文件: `shared/src/main/resources/org/researchedc/i18n/`

---

## Origin and License

ResearchEDC is independently maintained research electronic data capture and clinical data management platform derived from OpenClinica v3.x.

- **Upstream project:** [OpenClinica](https://github.com/OpenClinica/OpenClinica)
- **Upstream license:** GNU LGPL, version 2.1 or later
- **Initial fork/import date:** 2023 (approximate)
- **ResearchEDC rename date:** 2026-05-20

This repository contains substantial modifications and refactoring. The renaming does not alter the license obligations for code derived from OpenClinica — files derived from OpenClinica remain licensed under the GNU LGPL, version 2.1 or later.

### Trademark Notice

OpenClinica is a trademark of its respective owner. ResearchEDC is not an official OpenClinica release and is not affiliated with, endorsed by, or sponsored by OpenClinica.

### 相关文档

- [AGENTS.md](./AGENTS.md) — AI 助手知识库 (含 app, frontend, questionnaire-service 等子模块)
- [shared/AGENTS.md](./shared/AGENTS.md) — 共享领域逻辑与数据访问
- [app/AGENTS.md](./app/AGENTS.md) — Spring Boot 配置与 Modulith 模块
- [frontend/AGENTS.md](./frontend/AGENTS.md) — React 19 SPA 前后端交互
- [questionnaire-service/AGENTS.md](./questionnaire-service/AGENTS.md) — Python FastAPI 微服务
- [web/AGENTS.md](./web/AGENTS.md) — Web UI 与控制器
- [ws/AGENTS.md](./ws/AGENTS.md) — SOAP Web 服务
- [MODIFICATIONS.md](./MODIFICATIONS.md) — 修改记录
