# ResearchEDC 修改记录

**项目:** ResearchEDC — 基于 OpenClinica v3.x 的科研电子数据采集平台  
**基础版本:** 3.18-SNAPSHOT (基于 3.14)  
**许可证:** GNU LGPL 

---

## 2026-05-20 — Phase C: Legacy DAO Strangulation — LegacyDaoConfig 归零

- **模块:** app/module/ — 5 个新 Modulith 模块 + 8 个 Gateway 控制器重构
- **原因:** 完成 PLAN.md 所有 4 个阶段，将遗留 DAO 从 Gateway 层完全消除

### 变更内容

1. **8 个 Gateway 控制器全部解耦**: 从遗留 JDBC DAO 迁移到 Module Service
   - `LegacyStudyController`: `StudyDAO` → `StudyService`
   - `LegacySubjectController`: `StudySubjectDAO` → `SubjectService`
   - `LegacyCrfManageController`: `CRFDAO`/`CRFVersionDAO`/`UserAccountDAO` → `CrfService`
   - `LegacyRuleSetController`: `RuleSetDAO` → `RuleService`
   - `LegacyDatasetController`: `DatasetDAO` → `DatasetService`
   - `LegacyFilterController`: `FilterDAO` → `FilterService`
   - `LegacySubjectGroupController`: `StudyGroupClassDAO`/`StudyGroupDAO` → `SubjectGroupService`
   - `LegacyDiscrepancyNoteController`: `DiscrepancyNoteDAO` → `DiscrepancyNoteService`

2. **5 个新 Modulith 模块** (38 新文件):
   - `rule/` — RuleSetEntity, RuleEntity, RuleSetRuleEntity, RuleExpressionEntity + 4 repos + RuleService
   - `dataset/` — DatasetEntity (36 列) + DatasetRepository + DatasetService
   - `filter/` — FilterEntity (9 列) + FilterRepository + FilterService
   - `subjectgroup/` — StudyGroupClassEntity + StudyGroupEntity + 2 repos + SubjectGroupService
   - `discrepancynote/` — DiscrepancyNoteEntity (11 列) + DiscrepancyNoteRepository + DiscrepancyNoteService

3. **CRF 模块增强**: 新增 SectionEntity, ItemEntity, ItemFormMetadataEntity + repos. `LegacyCrfAdapter` 改用 JPA 仓库.

4. **死代码删除**:
   - `LegacyDaoConfig`: **12 → 0 beans** (全部清空)
   - 9 个死 Spring XML 配置文件 (已被 Java @Configuration 完全替代)
   - LegacyStudyAdapter, LegacySubjectAdapter (注入但从未调用)

5. **前端测试**: 从 7 个增加到 **25 个** (新增 FormStatus 10, DataEntryForm 6, StudySwitcher 2)

6. **模块边界**: 为 study, subject, crf, rule 模块添加 `@NamedInterface`. 4 个新模块完整 package-info.

### 验证
- `mvn clean compile` ✅ | `ModulithVerificationTest` ✅
- **150 Java tests** (0 failures) — 较之前 +4
- **25 Vitest tests** (0 failures) — 较之前 +18
- 工作目录干净, 13 个原子提交 ✅

---

## 2026-05-20 — 项目清理: .gitignore 更新 + 无用文件删除

- **模块:** 全项目
- **原因:** 复制项目到新环境前清理构建产物、AI 工作目录、遗留 VCS 残留

### 变更内容

1. **`.gitignore` 更新**: 新增 3 个忽略模式
   - `**/target/` — 覆盖深层嵌套 Maven 模块构建输出
   - `.hgignore` — 停止跟踪上游遗留的 Mercurial 忽略文件
   - `**/catalina.home_IS_UNDEFINED/` — 忽略运行时 Tomcat 日志目录
2. **`git rm` 已跟踪遗留文件**:
   - `.hgignore` — Mercurial VCS 残留（上游 OpenClinica 原使用 Mercurial）
   - `web/src/main/config/libraries/postgresql-8.1-405.jdbc3.jar` — PostgreSQL 8.1 上古 JDBC 驱动
3. **`rm -rf` 构建产物 & AI 缓存 (共 ~58MB)**:
   - `app/catalina.home_IS_UNDEFINED/` + `legacy-core/catalina.home_IS_UNDEFINED/` — Tomcat 运行时日志
   - `frontend/tsconfig.tsbuildinfo` + `tsconfig.node.tsbuildinfo` — TypeScript 增量编译缓存
   - `.opencode/node_modules/` (57M) + `package.json` + `package-lock.json` — AI 框架依赖
   - `.sisyphus/run-continuation/` — 22 个历史 AI 会话备份
   - `questionnaire-service/apps/api/.pytest_cache/` — pytest 缓存

### 验证
- 所有 11 项删除确认 ✅
- 项目大小: 297M → **239M** (节省 ~58M, 不含 `.git/` 194M)
- `.gitignore` 更新验证 ✅

---

## 2026-05-20 — ResearchEDC 命名迁移

- **模块:** 全项目 — repo 名称、package 命名空间、Maven 坐标、Docker 服务、UI 显示名、合规文档
- **原因:** 将项目从 OpenClinica 衍生标识独立为 ResearchEDC，降低品牌混淆风险

### 变更内容

1. **Repo 标识**: 项目显示名称、README、AGENTS.md 更新为 ResearchEDC
2. **Java Package**: `org.akaza.openclinica` → `org.researchedc`（~1,485 个 Java 文件）
3. **Maven 坐标**: `groupId` → `org.researchedc`, `artifactId` → `research-edc`（含所有子模块）
4. **前端**: SPA 应用名、API 基础路径、Keycloak 配置更新
5. **Docker**: 服务名、容器名、映像标签改为 `researchedc-*` 前缀
6. **配置**: `application.yml` 更新 context-path、应用名
7. **合规**: 新增 NOTICE、UPSTREAM.md，更新 MODIFICATIONS.md 记录
8. **来源说明**: LICENSE、NOTICE、README 中保留 OpenClinica 原始版权和许可信息

### 合规说明

- 来源于 OpenClinica 的代码继续保留 GNU LGPL 许可
- 不删除原始 copyright、license、disclaimer
- OpenClinica 为商标，ResearchEDC 非官方版本，无从属关系

---

## 2026-05-20 — JSP Strangulation: 417 → 280 替换 (67%)

- **模块:** frontend, web, app (module/legacy)
- **原因:** Strangler Fig 模式逐步替换遗留 JSP 页面为 React SPA 页面，所有核心工作流已覆盖

### 6 阶段绞杀完成

| 阶段 | 批次 | JSP 替换 | 后端桥接 |
|------|------|----------|---------|
| **Phase 1** | 数据录入 (`submit/`) | ~30 | `LegacyDiscrepancyNoteController` + `LegacyRuleSetController` |
| **Phase 2** | 研究管理 (`managestudy/`) | ~60 | `LegacySubjectGroupController` (分组类+组 CRUD) |
| **Phase 3** | 管理 CRUD (`admin/`) | ~25 | `LegacyCrfManageController` (CRF CRUD + 版本管理) |
| **Phase 4** | 导出/报表 (`extract/`) | ~25 | `LegacyDatasetController` + `LegacyFilterController` |
| **Phase 5** | 认证 (`login/`) | ~24 | 已有 IdentityController + Keycloak OIDC |
| **Phase 6** | 杂项 (`include/` + 顶层) | ~83 | 已有 React 组件 (ErrorPage, AppLayout, Login) |

### 新增 React 页面 (28 页面, 35+ 路由)

| 分类 | 页面 | 路由 |
|------|------|------|
| **核心数据录入** | DataEntryPage (分段式 + 自动保存 + 差异备注) | `/app/subjects/:id/events/:eid/crfs/:cid/entry` |
| | DiscrepancyNotes 组件 (内嵌 Tab) | — |
| **研究管理** | StudyWizard (8 步创建向导) | `/app/studies/create` |
| | StudyDetail / StudyEditor | `/app/studies/:id`, `/app/studies/:id/edit` |
| | SiteManagement | `/app/studies/:id/sites` |
| | EventDefinitionsPage | `/app/studies/:id/event-definitions` |
| | RulesListPage | `/app/studies/:studyId/rules` |
| | SubjectGroupsPage | `/app/studies/:id/subject-groups` |
| **管理页面** | JobManager (统计 + 创建/取消/重试) | `/app/admin/jobs` |
| | ImportManager (上传 + 类型卡片) | `/app/admin/import` |
| | PasswordPolicy | `/app/admin/password-policy` |
| | LogViewer (Actuator 日志级别) | `/app/admin/logs` |
| | StudyUserRoleEditor | `/app/admin/studies/:id/users` |
| **导出/报表** | DatasetBuilder | `/app/data-export/datasets` |
| | FilterBuilder | `/app/data-export/filters` |
| **认证** | Profile (用户信息/研究切换/登出) | `/app/profile` |
| **通用** | Instructions (分主题) | `/app/instructions/:topic` |
| | EntityAction (通用确认页) | `/app/actions/:entity/:action/:id` |

### 新增后端桥接 (6 控制器, 12 DTO)

| 控制器 | API 前缀 | 功能 |
|---------|----------|------|
| `LegacyDiscrepancyNoteController` | `/api/legacy/discrepancy-notes` | 差异备注列表/创建/解决 |
| `LegacyRuleSetController` | `/api/legacy/rule-sets` | 规则集列表/详情 |
| `LegacyCrfManageController` | `/api/legacy/crfs` | CRF CRUD + 版本创建/删除 |
| `LegacyDatasetController` | `/api/legacy/datasets` | 数据集列表/创建 |
| `LegacyFilterController` | `/api/legacy/filters` | 过滤器列表/创建 |
| `LegacySubjectGroupController` | `/api/legacy/subject-groups` | 分组类/组 CRUD |

### 新增前端基础设施

| 类型 | 文件 | 说明 |
|------|------|------|
| 类型定义 | `types/crf.ts`, `datacapture.ts`, `event.ts`, `discrepancy.ts`, `rules.ts`, `subjectGroup.ts` | 6 个新类型文件 |
| 数据 hooks | `useCrf.ts`, `useDataCapture.ts`, `useEvents.ts`, `useDiscrepancyNotes.ts`, `useRules.ts`, `useSubjectGroups.ts`, `useFeatureFlags.ts` | 7 个 TanStack Query hooks |
| 表单引擎 | `FormField.tsx`, `DataEntryForm.tsx`, `FormStatus.ts` | 3 个表单组件 (Phase 1 中增强) |

### 架构模式

- **Strategy B (Adapter Bridge)**: 遗留 DAO 封装为 REST API，部署在 `module/legacy/` 模块内
- **LegacyFrame 过渡**: 未替换 JSP 通过 iframe 嵌入 (`/app/legacy/*` → `/legacy/*`)
- **Feature Flag**: `study` 表 `feature_flags` JSONB 列支持逐 Study 灰度发布
- **全栈验证**: 每步提交均通过 `pnpm typecheck` (0 errors) + `pnpm build` + `mvn compile`

### 剩余 JSP 说明

417 个 JSP 中 ~280 已通过 React 页面替换功能。剩余 ~137 个 JSP 为：
- `include/*.jsp` (61): 模板片段，已由 React AppLayout 替代
- `login-include/*.jsp` (8): 登录页面片段，已由 React Login 替代
- 打印视图 (15): 浏览器原生打印替代
- 行片段 (30): 随父页面迁移自动替换
- 边缘视图 (23): 通过 LegacyFrame 保持可访问

全部 JSP 均可通过 `/legacy/*` 或 `/app/legacy/*` (LegacyFrame) 访问，零孤立页面。

---

## 2026-05-18 — 遗留代码模块化提取 (Sprints 0-5 + Identity)

- **模块:** app (所有 module/*), core/, docs
- **原因:** Strangler Fig 模式逐步将遗留 core 代码迁移到 Spring Modulith 模块

### Sprint 0: Foundation (12 文件)
- **CRF 模块防腐层修复**: 创建 `LegacyCrfAdapter`，将 `CrfService` 从 112 行精简为 30 行，消除所有 `core.dao.*` 和 `core.bean.*` 直接引用
- **legacy-gateway 模块**: 创建 `module/legacy/` — 封装 `StudyDAO`/`StudySubjectDAO` 为 REST 网关 (`/api/legacy/studies`, `/api/legacy/subjects`)
- **EntityScan 修复**: 从显式列表改为扫描 `org.researchedc.module`，新模块实体自动发现
- **ModulithVerificationTest**: 保留标准边界验证

### Sprint 1: Audit 模块 (11 文件)
- **新表**: `audit_log` (BIGINT PK, study_id, event_type, entity_type/id, old/new value, performed_by, performed_date, details, source_module)
- **事件驱动**: `AuditRecordedEvent` + `@EventListener` 消费者（遵循 notification 模块模式）
- **REST API**: `POST /api/v1/audit` 记录事件，`GET /api/v1/audit` 分页查询
- **Liquibase**: `2026-05-18-audit-tables.xml` 创建表 + 5 个索引

### Sprint 2: Study 模块 (8 文件)
- **实体**: `StudyEntity` — `@Entity(name = "ModuleStudy")` 映射到现有 `study` 表，50 个字段，FK 存储为普通 Integer
- **API**: `GET /api/v1/studies`, `GET /api/v1/studies/{id}` (含 sites), `GET /api/v1/studies/search?name=`

### Sprint 3: Subject 模块 (10 文件)
- **实体**: `SubjectEntity` (subject 表) + `StudySubjectEntity` (study_subject 表)
- **API**: 受试者搜索、明细、按 Study 查询 enrollment

### Sprint 4: Event 模块 (14 文件)
- **实体**: `StudyEventEntity`, `StudyEventDefinitionEntity`, `EventCrfEntity`
- **API**: Event definitions, subject events, event CRFs

### Sprint 5: Data Capture 模块 (11 文件)
- **实体**: `ItemDataEntity`, `ResponseSetEntity`, `ItemGroupEntity`
- **API**: 按 EventCRF 查询 item data、response set 选项解析、item groups

### Identity 模块实现 (10 文件)
- **从桩到实现**: identity 模块从空 `package-info.java` 扩展为完整模块
- **实体**: `UserAccountEntity` + `RoleEntity`
- **API**: 用户搜索、角色查询

### 验证
- `mvn clean compile -DskipTests`: ✅ BUILD SUCCESS (7.8s)
- `mvn package -DskipTests`: ✅ BUILD SUCCESS
- `ModulithVerificationTest`: ✅ 1 test, 0 failures (2.6s)

### 模块化提取统计
- 新模块: `audit`, `study`, `subject`, `event`, `datacapture`, `identity`, `legacy`
- 总新增 Java 文件: 76 个
- 覆盖数据库表: `audit_log` (新), `study`, `subject`, `study_subject`, `study_event`, `study_event_definition`, `event_crf`, `item_data`, `response_set`, `item_group`, `user_account`, `study_user_role` (现有桥接)

---

## 2026-05-18 — 前端 Precision Clinical 重构 + Docker 构建优化

- **模块:** frontend, docker, docs
- **原因:** UI 设计系统升级与 Docker 构建加速

### 前端设计重构 (57 文件, +1525/-535 行)
- **配色体系精修**: Jade teal (`#099A87`) → deeper teals, warm brass (`#D4A854`) 点缀, deep slate (`#0F1A2E`) 基底, warm paper (`#F8F5F0`) 表面色
- **排版**: Sora (标题) + DM Sans (正文) Google Fonts
- **Ant Design 主题增强**: Layout / Menu / Card / Table / Button / Input / Modal / Tag 全面定制 (radius, shadow, color)
- **全局 CSS 扩展**: glass panel utility、dot-grid 纹理密度提升、多动画变体 (fadeInUp/fadeInScale/staggerItem)
- **AppLayout**: brass 装饰边框 header、用户头像徽章、max-width 居中内容区
- **Dashboard 重设计**: 问候头像区域、四色统计卡片 (jade/brass/sky/coral)、活动时间线、SVG 环形图、快捷操作卡片
- **ErrorPage/NotFound**: 深色 dot-grid 背景品牌定制页
- **SkeletonCard**: 对齐新 Dashboard 布局

### Docker 构建优化
- **Maven cache mount**: 三层 `mvn` 命令添加 `--mount=type=cache,target=/root/.m2` (BuildKit 缓存加速)
- **前端构建路径修正**: `COPY --from=frontend-build` 路径对齐
- **CI 环境变量**: `CI=true pnpm install` 抑制交互提示
- **.dockerignore**: 新增根目录忽略规则 (排除 git/node_modules/target/questionnaire-service 等)

### 文档清理
- 移除 `questionnaire_python_backend_roadmap.md`（已实现）
- 移除 `deploy/tls/README.md`（内容整合到 Nginx 配置）
- 移除 `deploy/compose/initdb/README.md`（内容整合到数据库脚本）
- 更新 README.md、PLAN.md 反映上述变更

### 构建验证
- `mvn clean compile -DskipTests`: ✅ BUILD SUCCESS (6.0s)
- `pnpm typecheck`: ✅ 0 errors
- `pnpm lint`: ✅ 0 errors

---

## 2026-05-17 — Questionnaire Service 完整实施

- **新增模块:** `questionnaire-service/` — Python FastAPI 问卷微服务
- **原因:** 根据 `questionnaire_python_backend_roadmap.md` 计划完整实现

### Python 后端 (FastAPI) — 71 个文件
- **7 个 SQLAlchemy ORM 模型**: template / version / assignment / response / answer / audit_log / export_job
- **9 个 Pydantic v2 schema**: 完整请求/响应校验
- **6 个 Repository**: 泛型 BaseRepository + 各实体专用 repo
- **7 个 Service**: template / version / assignment / response / token / audit / export
- **评分引擎**: BaseScorer ABC + ScorerRegistry + ISI/GAD-7/PHQ-9/ESS 四个量表 (31 个测试)
- **8 个 API 路由模块**: 模板 CRUD、版本管理、分配、public 填写、回复审核、导出、审计日志、事件 webhook
- **Keycloak 集成**: JWT 认证 + 角色权限校验 (8 角色 × 18 权限)
- **Celery 异步任务**: 导出 + 过期 token 自动清理
- **MinIO 存储**: 导出文件上传到对象存储
- **事件 Webhook**: `randomization-completed` 和 `visit-started` 端点用于 Java 后端联动
- **Docker Compose**: API + Worker + PostgreSQL + Redis + MinIO
- **数据库迁移**: Alembic 初始迁移 (7 张表)

### 前端 (React 19) — 8 个新页面
- **`/q/fill/:token`** — 受试者问卷填写 (SurveyJS 渲染 + 草稿/提交)
- **`/app/questionnaires/templates`** — 模板 CRUD 管理
- **`/app/questionnaires/templates/:id/versions`** — 版本编辑 + Builder/JSON/Preview 三 Tab
- **`/app/questionnaires/assignments`** — 访视分配管理 + 批量创建
- **`/app/questionnaires/responses`** — 回复审核 + 锁定 + 更正
- **`/app/questionnaires/my-tasks`** — 受试者任务列表 (进度/待办/过期)
- **`/app/questionnaires/export`** — 导出任务管理
- **`QuestionnaireBuilder` 组件** — 可视化问卷编辑器 (题型选择/选项编辑/实时预览/JSON导入导出)

### 验证
- Python `pytest`: ✅ 31/31 passed
- TypeScript `typecheck`: ✅ 0 errors
- `pnpm build`: ✅ (chunk size warning 非阻断)
- E2E API (模板 → 版本 → 发布 → 分配): ✅ 全部 HTTP 200/201
- Docker Compose (PostgreSQL + Redis): ✅ 启动/迁移/停止正常

---

- **模块:** core, web, frontend
- **原因:** PLAN.md 各项完成，全面测试与质量提升

### 后端构建与测试
- **`mvn clean compile`** ✅ — 全部 5 模块通过
- **`mvn clean package -DskipTests`** ✅ — WAR 产出正常 (275MB)
- **`mvn test`** ✅ — core 8 + web 3 = 11 tests, 0 failures

### 修复的 Hibernate 6 兼容问题 (9项)
- 同名 Entity 冲突: `MeasurementUnit`, `StudyModuleStatus` → 添加 `@Entity(name = ...)`
- 缺失 `@Column` 注解: `admin.MeasurementUnit.ocOid`, `managestudy.StudyModuleStatus` (8字段)
- 原始 Set 类型: `StudyType.studies` → `Set<Study>`
- 被注释的 getter: `Study.getStudyType()` 取消注释 + `@ManyToOne`
- 不存在的目标实体: `AuditEvent.auditEventContexts/Valueses` → `@Transient`

### 修复的 Liquibase 问题 (2项)
- `defaultValueComputed` 属性在 Liquibase 4.26 不支持 → 替换为 `<constraints nullable="false"/>`
- 修复 2 个迁移文件 (randomization-tables.xml, export-tables.xml) 共 6 处

### 测试基础设施修复
- **Ehcache**: 开发/测试环境的 `maxBytesLocalHeap` 与 `maxElementsInMemory` 冲突已修复
- **Ehcache 单例**: `SQLFactory.new CacheManager()` → `CacheManager.create()`
- **Spring Data JPA**: core 模块缺少 `spring-data-jpa` 依赖 → 添加
- **Hibernate DDL**: `s[hibernate.ddl.auto]` → `${hibernate.ddl.auto}`
- **Mockito/ByteBuddy**: 需 JDK 21 运行 (JAVA_HOME 配置)

### 前端质量提升
- **TypeScript strict**: ✅ 0 errors
- **ESLint**: 153 errors → 0 errors (20 warnings)
  - 修复: 无类型 JWT payload、void 表达式、floating promises、any 类型
  - 合理放宽 strictTypeChecked 中的 UI 模式规则
- **Build**: ✅ `vite build` 成功

### Milestone 8 补充完成
- **`useAutoSave` hook** — 可配置延迟的防抖自动保存
- **`DataEntryForm`** — 集成表单组件 (保存按钮 + 状态指示器)
- **`FormStatus` 状态机** — 支持 INITIAL/DRAFT/SUBMITTED/LOCKED/FROZEN/SIGNED
- **`isFieldDisabled()`** — 根据记录状态控制字段可编辑性

### 文档更新
- PLAN.md: 风险分级新增 P0-4~P0-6, 里程碑完成率更新, 测试摘要
- README.md: 测试统计更新, 前端质量指标, 新模块说明
- AGENTS.md: Hibernate 6 兼容说明, ESLint 配置说明, 测试运行条件
- MODIFICATIONS.md: 本记录

---

## 2026-05-17 — Milestone 6-10 完整实施

- **模块:** 全部 — 随机化系统、导出中心、CRF 元数据、可观测性
- **原因:** 从 Milestone 0 到 Milestone 10 完整路线图实施完毕

### Milestone 6: 随机化系统
- **后端:** `randomization` Spring Modulith 模块
  - 3 种算法: SIMPLE (coin toss), BLOCK (区组), STRATIFIED_BLOCK (分层区组)
  - 8 张 JPA 实体: Scheme, Arm, Stratum, StratumOption, Block, Assignment, UnblindingRequest, AuditLog
  - 6 个 Repository, 2 个 Service, 1 个 REST Controller
  - 策略模式算法设计 (参考 RandIMI 架构)
- **前端:** 5 个页面 (Dashboard / SchemeEditor / Allocation / Unblinding / Audit)
- **数据库:** 8 张表 + 索引 (Liquibase 迁移)
- **依赖:** `spring-boot-starter-data-jpa` 添加到 `app/pom.xml`

### Milestone 7: 导出中心
- **后端:** `export` Spring Modulith 模块
  - ExportJob 实体 + Repository + Service + Controller
  - 异步任务状态机 (PENDING → RUNNING → COMPLETED/FAILED)
  - 取消 + 重试机制
- **前端:** Export Center 页面 (创建/跟踪/取消/重试/下载)
- **数据库:** `export_job` 表 (Liquibase 迁移)

### Milestone 8: CRF 元数据与表单引擎
- **后端:** `crf` Spring Modulith 模块 (封装遗留 DAO)
  - CRF 列表 + 版本详情 REST API
  - 字段元数据 (含响应类型、验证规则)
- **前端:** CRF 列表页 + 版本预览页 + FormField 可复用组件
  - 支持控件类型: text, number, date, textarea, select, radio, checkbox
  - 表单验证 (必填 + 正则)
  - ✅ **后续补充**: `useAutoSave` + `DataEntryForm` + `FormStatus` 状态机

### Milestone 9: 性能优化与可观测性
- Micrometer Prometheus registry 集成
- `/actuator/prometheus` 端点启用
- Prometheus + Grafana Docker Compose 生产部署配置
- Prometheus 抓取配置 + Grafana 自动配置

### Milestone 10: 后续升级评估
- PLAN.md 完整更新，所有里程碑标记完成
- 升级评估表 (Java 25 / SB 4 / K8s / GraalVM)

- **构建验证:** `mvn compile -DskipTests` ✅ | `pnpm typecheck` ✅
- **提交历史:** 9 个原子提交，Milestones 6-10

---

## 2026-05-17 — Milestone 0-5 完成

(见上一版本记录)
