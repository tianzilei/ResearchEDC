# ResearchEDC

**版本:** 3.18-SNAPSHOT  
**最后更新:** 2026-05-20  
**许可证:** GNU LGPL

ResearchEDC is an independently maintained research electronic data capture and clinical research data management platform derived from OpenClinica v3.x.

该项目专注于研究者发起的临床研究，支持电子病例报告表、受试者管理、研究工作流、数据导出以及未来扩展的随机化、问卷集成、针刺临床试验工作流和神经生理数据元数据管理。

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
| **认证** | Keycloak / OIDC |

**应用服务器:** Tomcat 10.1.x 推荐  
**数据库:** PostgreSQL 14+ / Oracle 19c+

---

## 项目结构

```
ResearchEDC/
├── pom.xml             # Maven 父 POM
├── frontend/           # React 19 SPA (pnpm workspace, 43 源文件)
│   ├── src/
│   │   ├── api/        # API 客户端 + 类型
│   │   ├── components/ # 通用组件 (StudySwitcher, SkeletonCard, FormField)
│   │   │   ├── form-engine/          # 表单引擎 (FormField, DataEntryForm, FormStatus)
│   │   │   └── questionnaire-builder/ # 问卷可视化 Builder
│   │   ├── hooks/      # TanStack Query 封装 + 权限 hooks + useAutoSave
│   │   ├── layouts/    # AppLayout (Precision Clinical 风格 — 顶栏 + 侧栏 + 内容区)
│   │   ├── pages/      # 页面组件 (Dashboard, CRF, 随机化, 导出, 问卷)
│   │   │   └── questionnaire/  # 问卷相关页面 (7 个页面)
│   │   ├── providers/  # AuthProvider, AppProviders
│   │   ├── router/     # React Router 配置
│   │   ├── styles/     # 设计系统: 全局 CSS (dot-grid 纹理, glass panel, 动效), Ant Design 主题
│   │   └── types/      # TypeScript 类型定义 (用户, 研究, 随机化)
│   ├── vite.config.ts  # Vite 配置 (代理 / API 构建输出)
│   └── package.json
├── questionnaire-service/  # Python FastAPI 问卷微服务 (独立部署)
│   ├── apps/api/            # FastAPI 后端 (models, services, scoring, routers, workers)
│   ├── infra/               # Docker Compose (PostgreSQL, Redis, MinIO)
│   └── packages/            # SurveyJS 问卷 schema (ISI, GAD-7, PHQ-9, ESS)
├── app/                # Spring Boot 模块化单体入口
│   ├── src/main/java/
│   │   └── org/akaza/openclinica/
│   │       ├── config/      # WebMvcConfig (SPA fallback), WebServiceConfig, OpenApiConfig
│   │       └── module/      # Spring Modulith 模块 (12 个)
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
│   │           └── identity/       # 身份权限 (user_account/study_user_role 桥接)
│   └── src/main/resources/
│       ├── application.yml # profile 配置
│       └── static/         # 前端构建产物 (自动生成)
├── legacy-core/        # 遗留领域逻辑 & 数据访问 (736 源文件)
│   ├── dao/            # 数据访问层 (EntityDAO 模式)
│   ├── domain/         # Hibernate 实体 (@Entity)
│   ├── service/        # 业务服务层
│   ├── logic/          # 规则引擎
│   ├── job/            # Quartz 定时任务
│   └── migration/      # Liquibase 迁移脚本
├── web/                # Web UI & REST API (481 源文件, 419 JSP)
│   ├── control/        # Servlet 控制器 (SecureController)
│   ├── controller/     # Spring MVC REST 控制器
│   └── webapp/         # JSP 页面、前端资源
├── ws/                 # SOAP Web 服务 (57 源文件)
│   └── endpoint/       # Spring WS 端点
├── deploy/             # Docker Compose + Nginx 配置
│   ├── compose/        # dev/test/prod 三层 Compose
│   └── nginx/          # 生产级 Nginx 配置
├── .dockerignore       # Docker 构建忽略规则
└── scripts/            # 构建/部署/发布脚本
```

---

## 路由架构

```
/legacy/*  → 旧 OpenClinica JSP (向后兼容)
/app/*     → 新 React SPA (前端路由, index.html fallback)
/q/*       → 问卷受试者填写页 (独立于 AppLayout)
/api/*     → Spring Boot REST API
/auth/*    → Keycloak / OIDC
/actuator/* → Spring Boot Actuator
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
| 纯单元测试 | `junit.framework.TestCase` | ❌ | ✅ 8 tests pass (core) |
| Mockito 测试 | `junit.framework.TestCase` | ❌ | ✅ 3 tests pass (web) |
| DAO 集成测试 | `HibernateOcDbTestCase` (DBUnit) | ✅ | ⚠️ 待启用 (测试方法被注释) |
| Service 集成测试 | `HibernateOcDbTestCase` | ✅ | ⚠️ 同上 |
| Modulith 验证 | JUnit 5 + `ApplicationModules` | ❌ | ✅ 1 test pass |
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
| TypeScript strict | `pnpm typecheck` | ✅ 0 errors |
| ESLint | `pnpm lint` | ✅ 0 errors |
| 构建 | `pnpm build` | ✅ |
| 测试 | `pnpm test` | ⏳ Vitest 待编写 |

### 设计系统 (2026-05-18 "Precision Clinical" 重构)
- **配色**: Jade teal (`#099A87`) 主色 + warm brass (`#D4A854`) 点缀 + deep slate (`#0F1A2E`) 基底 + warm paper (`#F8F5F0`) 表面色
- **排版**: Sora (标题) + DM Sans (正文) Google Fonts
- **动效**: 页面进场 `fadeInUp` 动画、卡片 hover 上浮、统计卡片交错进场
- **背景**: 全局 dot-grid 图纸纹理 (radial-gradient)
- **组件**: AppLayout 精修 (brass 边框 header, max-width 内容区), Dashboard 重设计 (问候头像、彩色统计卡片、活动时间线、SVG 环形图、快捷操作), ErrorPage/NotFound 定制页面

测试数据文件: `legacy-core/src/test/resources/org/akaza/openclinica/{dao,service}/testdata/`

---

## 核心架构模式

- **DAO 模式:** 所有数据库访问通过 `EntityDAO<K>` 子类
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
- 新代码应写入 `module/<name>/` 而非遗留包

---

## 关键模块

| 模块 | 位置 | 功能 |
|------|------|------|
| 研究管理 (遗留) | `control/managestudy/` | 研究/Site/Subject 管理 (186 Servlet) |
| 数据录入 (遗留) | `control/submit/` | CRF 数据录入、双录入、电子签名 |
| 规则引擎 (遗留) | `logic/rule/` | 表达式验证、自动计算 |
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
| 通知模块 | `module/notification/` | 事件驱动邮件 |
| **问卷服务** | `questionnaire-service/apps/api/` | **FastAPI + SQLAlchemy + 评分引擎** |
| 前台 SPA | `frontend/src/` | React 19 管理界面 (8 页面) |

### 前端页面一览

#### Java 后台页面
| 页面 | 路由 | 功能 |
|------|------|------|
| Dashboard | `/app/dashboard` | 登录后首页, 研究概览统计 |
| CRF 列表 | `/app/crfs` | CRF 库浏览 |
| CRF 预览 | `/app/crfs/:versionId` | 版本详情 + 分段结构 |
| 导出中心 | `/app/data-export` | 创建/跟踪/取消导出任务 |
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
资源文件: `legacy-core/src/main/resources/org/akaza/openclinica/i18n/`

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

- [AGENTS.md](./AGENTS.md) — AI 助手知识库
- [MODIFICATIONS.md](./MODIFICATIONS.md) — 修改记录
- [PLAN.md](./PLAN.md) — 已知问题与规划
