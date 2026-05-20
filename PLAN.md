# OpenClinica 架构债务治理计划

> **设计依据:** [OpenClinica_Architecture_Debt_Design.md](./docs/refactor/OpenClinica_Architecture_Debt_Design.md)  
> **项目版本:** 3.18-SNAPSHOT  
> **最后更新:** 2026-05-18  
> **核心原则:** Strangler Fig（绞杀者模式）— 新功能不进旧核心，旧能力逐步被新模块替代

---

## 一、当前状态

### 1.1 已完成基线

| 领域 | 完成内容 | 状态 |
|------|---------|------|
| **Java 版本升级** | Java 7 → 21, Spring 3 → 6.1.5, Hibernate 3 → 6.4.4, Jakarta EE 10 | ✅ |
| **Spring Boot 化** | app 模块整合 core + web + ws, Actuator + OpenAPI + Profiles | ✅ |
| **Spring Modulith** | notification 模块已提取, identity 模块已实现, 边界验证测试 | ✅ |
| **前端基线** | React 19 + TypeScript strict + Vite 6 + Ant Design 5 + TanStack Query 5 | ✅ |
| **前端 "Precision Clinical"** | 设计重构: 配色/排版/动效/AppLayout/Dashboard | ✅ |
| **随机化系统** | 3 种算法, 8 表 REST API, 前端 6 页面 | ✅ |
| **导出中心** | 异步任务, 状态跟踪, 下载中心 | ✅ |
| **CRF 元数据 & 表单引擎** | 列表/版本/预览, FormField/DataEntryForm/useAutoSave/FormStatus | ✅ |
| **问卷微服务** | FastAPI + 评分引擎 (ISI/GAD-7/PHQ-9/ESS), 31/31 tests | ✅ |
| **Docker 部署** | 三层 Compose (dev/test/prod), Nginx + TLS + Prometheus + Grafana | ✅ |
| **Maven 构建** | `mvn compile` ✅, `mvn package` ✅, `mvn test` 11/11 ✅ | ✅ |
| **Hibernate 6 兼容** | 修复 9 个实体映射 + 2 个 Liquibase 问题 | ✅ |
| **P0 安全加固** | Actuator 端点封锁, Swagger UI 禁用, entrypoint 默认密码移除 | ✅ |
| **P1 Maven 插件升级** | surefire 2.10 → 3.2.5, resources 2.5 → 3.3.1 | ✅ |
| **P2 构建/Git 优化** | 前端构建产物 .gitignore, JVM 容器化 CATALINA_OPTS | ✅ |
| **Sprint 0: Foundation** | CRF 防腐层构建、legacy-gateway 模块、EntityScan 修复、边界测试 | ✅ (2026-05-18) |
| **Sprint 1: Audit 模块** | 独立 audit_log 表 + JPA 实体 + 事件驱动 + Liquibase 迁移 | ✅ (2026-05-18) |
| **Sprint 2: Study 模块** | study 表桥接实体 + Repository + Service + REST API | ✅ (2026-05-18) |
| **Sprint 3: Subject 模块** | subject + study_subject 桥接实体 + REST API | ✅ (2026-05-18) |
| **Sprint 4: Event 模块** | study_event + event_crf + study_event_definition 桥接实体 + REST API | ✅ (2026-05-18) |
| **Sprint 5: Data Capture 模块** | item_data + response_set + item_group 桥接实体 + REST API | ✅ (2026-05-18) |
| **Identity 模块实现** | user_account + study_user_role 桥接实体 + REST API | ✅ (2026-05-18) |

### 1.2 遗留债务快照

| 指标 | 数值 | 趋势 |
|------|------|------|
| Modulith 模块 | 11 个 (包括 7 个新提取) | ✅ 完成模块化基线 |
| 遗留 Java 文件 (core+web+ws) | ~1300 | 🔴 待绞杀 (Phase C) |
| JSP 页面数 | 419 | 🔴 待绞杀 (Phase G) |
| WAR 体积 | 275 MB | 🔴 需降至 < 150 MB |
| Java 测试覆盖 | 11 tests / 1300+ files | 🔴 需增量建设 |
| 陈旧依赖数 | 10+ (Ehcache 2, Quartz 1.8, Commons Collections 3 等) | 🟡 可分批替换 |
| 旧版 Spring Security OAuth2 | `spring-security-oauth2` 2.0.x | 🔴 需统一到 Keycloak OIDC |
| 测试覆盖率 | < 1% | 🔴 需增量门禁 |
| 前端测试 | 0 测试 (43 源文件) | 🔴 Vitest 框架已就绪 |

---

## 二、架构债务治理策略

### 核心原则（来自设计文档）

1. **Strangler Fig（绞杀者模式）** — 新功能不进旧核心，旧能力逐步被新模块替代
2. **Anti-Corruption Layer（防腐层）** — 新模块绝不直接依赖遗留 core 的实体、DAO 或事务
3. **Schema 边界先于服务边界** — 先通过数据库 Schema 隔离确立模块所有权
4. **前端统一优先于后端拆分** — JSP → React 迁移比 Java 微服务拆分对业务价值更大

### 遗留内核治理约束

| 约束 | 内容 |
|------|------|
| **代码冻结** | 遗留内核 (`core` + `web` + `ws`) 不再接受新功能开发，仅允许缺陷修复和安全补丁。新需求必须进入 `app/module/*` |
| **只出不进** | 遗留内核可向新模块暴露数据，新模块不允许直接操作遗留 DAO 或写入遗留表 |
| **API 化封装** | 在 `app` 中建立 `legacy-gateway`，将高频旧功能封装为 REST API |

---

## 三、演进路线图

```
Phase 1: 止血 (0–3 月)     → 安全基线 + 依赖治理 + 测试门禁
Phase 2: 边界建立 (3–6 月)  → Schema 边界 + JSP 首批绞杀 + 认证统一 + 构建解耦
Phase 3: 核心重构 (6–12 月) → 审计总线 + 遗留 DAO 迁移 + Saga + JSP 二批绞杀
Phase 4: 收敛 (12–18 月)    → JSP < 50 + 服务拆分决策 + 全面可观测 + 冷启动优化
```

---

## 四、Phase 1: 止血（0–3 个月）

> 目标：堵住安全缺口 → 建立依赖治理机制 → 增量测试门禁

### 4.1 安全基线（P0 完成项复检）

- [x] 生产 Actuator 端点封锁 (`env`, `beans`, `conditions` 已禁用)
- [x] 生产 Swagger UI 禁用 (`springdoc.swagger-ui.enabled: false`)
- [x] Entrypoint 默认密码移除（`?` 强校验替代 `:-` 弱默认值）
- [x] Dockerfile 默认敏感变量移除（`OC_DB_PASS`、`OC_ADMIN_EMAIL` 无默认值）
- [x] 凭证文件重复暴露消除（`WEBAPP_CLASSES` 复制步骤已移除）
- [x] **OWASP Dependency Check** — 插件已配置（`dependency-check-maven:10.0.4`, CVSS ≥7 阻断），CI 工作流 `backend-modernization.yml` 已集成
- [x] **Trivy 扫描集成** — `.trivy-config.yml` 已创建，`docker-compose-check.yml` CI 含 Trivy scan job，`scripts/scan.sh` 支持 image/filesystem/SBOM 扫描
- [x] **Dependabot 配置** — `.github/dependabot.yml` 已创建（Maven + npm + pip）
- [x] **Maven BOM `research-edc-bom`** — `research-edc-bom/pom.xml` 已创建，继承 Spring Modulith BOM，统一版本管理

### 4.2 遗留内核冻结

- [x] **物理目录重命名**: `core/` → `legacy-core/`，IDE 和构建中明确标注为只读遗产
- [x] **Maven Enforcer `bannedDependencies`**: 禁止新模块 (`app/module/*`) 引入遗留库（Ehcache 2、Quartz 1.8、Commons Collections 3 等）
- [x] **提交规范**: `scripts/pre-commit-legacy-check.sh` — 阻止非修复性 legacy 变更（`ALLOW_LEGACY_CHANGES=1` 可绕过）
- [x] **文档标注**: `legacy-core/AGENTS.md` 已更新冻结策略；`PLAN.md` 已同步

### 4.3 依赖现代化（按替换成本分类治理）

遵循设计文档的分类治理框架，按替换成本将遗留依赖分为四类：

#### 类型 A：可替换且成本可控（Phase 1 立即执行）

- [x] **Commons Lang 2 → 3**: 包名 `org.apache.commons.lang` → `org.apache.commons.lang3`，全局替换（4 JSP + javadoc）
- [x] **Commons Collections 3 → 4**: 包名 `org.apache.commons.collections` → `org.apache.commons.collections4`（`LazyList.decorate()` → `lazyList()`）
- [x] **Javassist 3.8 移除**: Hibernate 6 自带 ByteBuddy，无代码引用，已移除依赖
- [x] **Maven Enforcer 禁用规则**: 配置 `bannedDependencies` 禁止 `commons-lang:commons-lang`, `commons-collections:commons-collections`, `javassist:javassist`
- [x] **Quartz 1.8 → 2.3**: 全量 API 迁移，`JobDetailBean` → `JobDetailImpl`，`StatefulJob` → `@DisallowConcurrentExecution`，50+ 文件
  - 先封装 `JobScheduler` 内部接口，将 Quartz 具体调用隐藏在接口之后
  - 再替换底层实现为 Quartz 2.3 API
  - 审计全部 9 个 Quartz 定时任务对遗留 DAO 的依赖（在 .sisyphus/LEGACY_REFACTOR_PLAN.md 中已标记为高风险）
- [x] **Ehcache 2 → Caffeine**: 已完成迁移（2026-05-20），Hibernate 二级缓存改用 Caffeine，应用级缓存使用 Spring Caffeine CacheManager

#### 类型 C：无现代替代品但可移除（随 JSP/SOAP 绞杀自然退役）

- [ ] **JMesa**: 遗留 JSP 表格渲染库（`web/pom.xml` + `legacy-core/pom.xml` 引用），标记为 "仅 legacy-web 使用"
  - 随 JSP 页面被 React 取代后直接移除，无需替换
- [ ] **Castor / XmlSchema**: 用于旧版 WSDL/SOAP 绑定（`ws/pom.xml` + `web/pom.xml` 引用）
  - 随 WS 模块退役时一并移除

#### 类型 D：深度耦合难以替换（建立依赖沙箱隔离）

- [x] **遗留依赖沙箱**: `legacy-core/pom.xml` 已列出所有遗留依赖
- [x] **Maven Enforcer `bannedDependencies`**: 禁止 `commons-lang:commons-lang`, `commons-collections:commons-collections`, `javassist:javassist`
- [x] **依赖影响范围审计**: 完成（2026-05-20），每个遗留依赖的实际使用范围如下：
  - **JMesa** (`jmesa:2.5.2`) — JSP 表格渲染库，用于 `web/` 的 JSP 页面（menu.jsp, viewAllSubjectSDVform.jsp）和 `legacy-core` 的 `ViewNotesFilterCriteria/SortCriteria` 服务类。**依赖方**: `legacy-core`, `web`。**移除条件**: 随 JSP 绞杀自然退役（Phase G）。
  - **Castor** (`castor-xml:1.4.1`) — XML 编组/解组，用于 ODM 导出（`MetaDataReportBean` 中的 Castor `Marshaller`）和规则处理（`OidHandler`, `EmailHandler`, `EmptySpaceHandler`）。**依赖方**: `legacy-core`, `web`, `ws`。**移除条件**: 随 SOAP 退役和 ODM 导出迁移移除。
  - **OpenClinica ODM** (`openclinica-odm:2.2`) — 内部 ODM 数据模型库，`bean/odmbeans/` 包大量使用。**依赖方**: `legacy-core`。**移除条件**: 随核心导出/导入模块迁移。
  - **Sitemesh** (`sitemesh`) — JSP 页面装饰框架，`web/WEB-INF/sitemesh.xml` 和 `ws/WEB-INF/sitemesh.xml` 配置。**依赖方**: `web`, `ws`。**移除条件**: 随 JSP 绞杀。
  - **Rome/RSS** (`rome:2.1.0`, `rome-fetcher`) — RSS 订阅解析，仅用于 `CoreResources` 中 RSS URL 配置和 `SystemController` 展示。**依赖方**: `web`, `ws`。**低影响**。
  - **MVEL** (`mvel2`) — 表达式语言，仅遗留注释引用 `//import org.mvel2.MVEL`。**可安全移除**（需确认无运行时反射引用）。
  - **Commons BeanUtils**, **Commons Validator**, **Commons Digester3** — 通用工具库，各模块广泛使用。**低风险**, 按需逐步替换。
  - **Apache POI / JExcel** — Excel 导出，用于遗留报表功能。**依赖方**: `legacy-core`, `web`, `ws`。**移除条件**: 随导出模块迁移。

#### Maven 插件升级

- [x] `maven-surefire-plugin`: 2.10 → 3.2.5
- [x] `maven-resources-plugin`: 2.5 → 3.3.1
- [x] `maven-release-plugin`: 2.3 → 3.0.1
- [x] `maven-assembly-plugin`: 2.2.2 → 3.7.1
- [x] `cargo-maven2-plugin`: 1.1.4 → 1.10.13（tomcat6x → tomcat10x）
- [x] `liquibase-plugin`: 1.9.x → `liquibase-maven-plugin` 4.28.0
- [x] `maven-jaxb2-plugin`: 0.7.5 → **评估完成，已移除**（父 POM 中的旧配置为死代码，WS 模块使用 `org.jvnet.jaxb:jaxb-maven-plugin:4.0.6` 替代）

### 4.4 增量测试门禁（分层测试金字塔）

遵循设计文档的四层测试策略，按优先级增量建设：

#### 第一层：Modulith 模块测试（Phase 1–2 最优先）

- [ ] **Modulith 模块测试规范**: 每个 `app/module/*` 必须包含：
  - 单元测试：Service 层业务逻辑，JUnit 5 + Mockito，不依赖数据库
  - 集成测试：Repository + Service + Controller，`@DataJpaTest` 或 Testcontainers
  - 模块边界测试：Spring Modulith `ApplicationModules` 验证无非法依赖 ✅（现有 `ModulithVerificationTest`）
- [x] **Maven Failsafe 配置**: 已完成（failsafe `3.2.5` 在父 POM `<pluginManagement>` 中，`skipITs=true` 默认跳过，`verify` 阶段可启用）
- [ ] **测试数据工厂**: 引入 `instancio` 或自定义 `TestDataFactory`，避免冗长的对象构造代码
- [x] **Testcontainers 标准化**: 依赖已添加（`testcontainers:1.19.8` 在 BOM 和父 POM 中），待编写测试用例

#### 第二层：遗留核心回归测试（Phase 2 有限投入）

- [ ] **关键路径契约测试**: 对 `legacy-gateway` 暴露的 REST API 编写 Spring MockMvc 测试
- [ ] **DAO 回归测试**: 为 5–10 个最关键的 DAO 查询编写集成测试（`@Sql` + Testcontainers）
  - 目的：防止 Hibernate/Spring 升级破坏核心查询

#### 第三层：跨服务契约测试（Phase 3）

- [ ] **Python ↔ Java CDC（Consumer-Driven Contract）**: Java 后端与 Python 微服务之间建立契约测试
  - 示例：Python 服务定义 `randomization-completed` webhook 的 JSON Schema
  - Java 端使用 Pact 或 Spring Cloud Contract 验证发出的消息符合该 Schema
  - 目的：Python 服务升级 API 时提前发现是否破坏 Java 端调用

#### 第四层：前端测试（Phase 2–4）

- [ ] **组件测试**: Vitest + React Testing Library，覆盖 `FormField`/`DataEntryForm`/`StudySwitcher` 等通用组件
- [ ] **E2E 测试**: Playwright 编写关键用户旅程（登录 → 选 Study → 打开 CRF → 录入 → 保存 → 审计日志）
- [ ] **CI 全栈集成**: E2E 测试在 CI 中针对 Docker Compose 全栈运行

### 4.5 Phase 1 验收标准

- [x] OWASP Dependency Check 已配置（CI 工作流集成，suppressions 文件已创建），首个完整运行需 CI 触发
- [x] 遗留内核冻结策略文档化并执行（`core` → `legacy-core` 目录重命名 + AGENTS.md 更新）
- [x] Commons Lang/Collections 迁移完成，编译通过
- [x] Quartz 1.8 → 2.3 迁移完成，编译通过
- [x] Maven Enforcer `bannedDependencies` 规则生效
- [ ] 至少 1 个 Modulith 模块达到 "must have tests" 标准
- [x] Ehcache 2 → Caffeine 已迁移
- [x] Maven BOM (`research-edc-bom`) 已创建
- [x] maven-jaxb2-plugin 0.7.5 dead config 已移除
- [x] 前端/后端构建解耦完成（前端独立 `pnpm build` → `frontend/dist/`）
- [x] CI 工作流 6 个已配置（含 OWASP + Trivy + Modulith 验证）

---

## 五、Phase 2: 边界建立（3–6 个月）

> 目标：Schema 边界正式划定 → 高频 JSP 开始绞杀 → 认证统一 → 构建解耦

### 5.1 Schema 边界（Schema-per-Module）

**设计原则:** Schema 边界先于服务边界 — 先通过数据库 Schema 隔离确立模块所有权，再考虑独立进程。

**阶段 A：逻辑隔离（Schema-per-Module，Phase 2 执行）**

- [ ] **表所有权声明**: 为每个 Modulith 模块指定唯一拥有的表
  - `randomization` → `randomization_scheme`, `randomization_assignment` 等 8 表
  - `export` → `export_job` 表
  - `notification` → `notification_event` 表
  - 后续模块同理
- [ ] **模块 API 契约**: 模块间数据访问必须通过 API 或事件，禁止跨模块直接 DAO
- [ ] **遗留表变更控制**: 新模块不得在遗留表上新增列或索引，确有必要时评审后执行

**阶段 B：物理隔离（Database-per-Module，Phase 3–4 评估）**

当某个模块的表数据量、访问模式或团队规模足以独立演进时，评估迁移到独立数据库实例：

- [ ] **独立数据库候选评估**: 基于以下指标判断模块是否适合物理隔离
  - 数据增长率（如 export 模块可能需要列式存储替代 PostgreSQL 行存）
  - 独立扩缩容需求（高 QPS 模块可以独立分片）
  - 团队自主权（模块团队可独立选择技术栈）
  - 监管合规（审计日志等需要独立存储的模块）
- [ ] **跨库一致性方案**: 独立数据库后采用 Saga 模式（编排式或协同式）处理跨库事务
  - 配合重试和死信队列处理分布式故障
  - 跨库查询通过 API 聚合层（Backend for Frontend）处理
- [ ] **数据同步与迁移工具**: 评估 Debezium / pglogical 等 CDC 工具的适用性
- [ ] **遗留核心数据库解耦路线图**: core 表 → study 模块独立 → subject 模块独立 → ...（逐步剥离）

### 5.2 JSP 绞杀 — 第一批（受试者高频页面）

#### 5.2.1 功能开关（Feature Flag）

- [x] **功能开关设计**: 已完成 — `study` 表新增 `feature_flags` JSONB 列，支持 Study 级别配置 `use_new_subject_ui: true` 等金丝雀迁移
  - 特定 Study 使用 React 新页面，其他 Study 继续使用 JSP
  - 开关通过 `GET/PUT /api/v1/studies/{id}/feature-flags` API 暴露
  - 前端 `useFeatureFlags` hook 根据开关值动态渲染
  - 新页面首次上线默认对所有 Study 关闭，逐步开放

#### 5.2.2 Hybrid Shell 过渡架构

在迁移完成之前，需要一个过渡架构让用户体验"看起来是统一的"：

- [x] **React App Shell（全局外壳）**: `LegacyFrame` 组件已完成
  - 已迁移的页面直接渲染 React 路由组件
  - 未迁移的 JSP 页面通过 `<iframe src="/legacy/xxx">` 嵌入 React 内容区（`/app/legacy/*` → `/legacy/*`）
  - iframe 使用 sandbox 隔离（`allow-same-origin allow-scripts allow-forms`）
  - 支持 loading 骨架屏、错误重试、超时检测
  - 用户在过渡期间始终看到统一的品牌、导航和颜色体系

#### 5.2.3 遗留 Servlet 的 API 化适配

- [ ] **业务逻辑与渲染分离**: 将 JSP 对应 Servlet/Controller 中「返回 HTML 的逻辑」与「执行业务的逻辑」拆分
  - 业务逻辑层暴露为 JSON API，供 React 调用
  - HTML 渲染层保持原样，继续供 JSP 使用
  - 当某个 JSP 被绞杀后，HTML 渲染层直接删除，业务 API 继续保留
  - 优先级: `submit/*` (70 JSP) → `managestudy/*` (120 JSP) → `admin/*` (69 JSP)

#### 5.2.4 页面迁移清单

- [ ] **数据录入页面迁移**: React 实现，完整包含：
  - [ ] 电子签名（eSignature）工作流
  - [ ] 审计追踪展示（数据变更历史 + 签名记录）
  - [ ] 数据锁定/冻结状态机（草稿 → 提交 → 锁定 → 冻结）
  - [ ] 双录入比对（两名录入员独立录入 → 差异高亮 → 裁决）
- [ ] **Query 管理页面迁移**: 质疑创建/回复/关闭工作流
- [x] **受试者管理页面迁移**: React 实现（SubjectList + SubjectDetail），支持创建/查看/搜索受试者，包括 Study enrollment
- [ ] **访视管理页面迁移**: Event 模块 REST API 已就绪，待 React UI
- [ ] **研究管理页面迁移**: Study 模块 REST API 已就绪，待 React UI（当前通过 LegacyFrame 访问 JSP）
- [x] **`legacy-gateway` API 封装**: 已完成 — `LegacyStudyController`, `LegacySubjectController` 提供高频查询 JSON API

### 5.3 认证体系统一（OIDC + Session Bridge）

- [x] **Keycloak OIDC 集成**: 双 FilterChain（API JWT + Web OIDC/Form Login）
- [x] **OidcSessionBridgeSuccessHandler**: Keycloak 用户映射到 legacy `userBean` session
- [x] **KeycloakJwtAuthenticationConverter**: realm_access.roles → Spring Security authorities
- [x] **Spring Security 6 Resource Server**: `/api/**` JWT Bearer token 验证
- [x] **旧版 `spring-security-oauth2` 移除**: 2.0.19.RELEASE 删除（死依赖，仅注释引用）
- [ ] **Python JWT 验证对齐**: `questionnaire-service` 使用与 Java 相同的 Keycloak JWKS URL + issuer + audience
- [ ] **权限模型统一**: 8 种角色 × 18 种权限建模为 Keycloak Realm Roles

### 5.4 构建与部署解耦

**设计目标**：从 275MB 巨石 WAR → 独立制品，前端可独立部署和回滚。

#### 方案 A：前后端独立部署（推荐 ✅ 已实施）

- [x] **前端独立部署**: `vite.config.ts` 输出目录改为 `frontend/dist/`，不再打包进 WAR
  - `openclinica-api.war`：仅 Java 后端，前端静态文件由 Nginx 独立 serve
  - `openclinica-frontend`：`pnpm build` 输出到 `frontend/dist/`，部署到 Nginx
- [ ] **Nginx 路由配置**（需部署团队配合）:
  - `/app/*` → `root /opt/openclinica/frontend/dist; try_files $uri /app/index.html`
  - `/api/*` → `proxy_pass http://localhost:8080/OpenClinica/api`
  - `/legacy/*` → `proxy_pass http://localhost:8080/OpenClinica/legacy`
  - `/actuator/*` → `proxy_pass http://localhost:8080/OpenClinica/actuator`
- [x] **构建命令解耦**: `pnpm build` 不再写入 `app/src/main/resources/static/`
  - 开发时: `pnpm dev`（Vite dev server on :5173, proxy /api → :8080）
  - 生产构建: `pnpm build` → `frontend/dist/`
  - Java 构建: `mvn clean package -DskipTests`（无前端依赖）
- [x] **Nginx 路由配置**: `deploy/nginx/nginx.conf` 已创建（含 `/app` SPA fallback、`/assets/` 静态缓存、`/ws/` SOAP、security headers），当前仍通过 proxy_pass 到 Java 后端，需按方案 A 切换为直接 serve 前端静态文件

#### 方案 B：嵌入式但解耦构建（过渡方案）

如果短期内无法独立部署，可在 CI 中单独构建前端后注入 WAR：

- [ ] **前端 CI 独立构建**: `pnpm build` → 产物打包为 artifact
- [ ] **Maven 下载产物**: 通过 `maven-dependency-plugin` 复制到 `app/src/main/resources/static/`
- [ ] **WAR 内嵌备选**: 保留 `WebMvcConfig` SPA fallback 作为独立部署未就绪时的回退方案

#### 制品管理

- [ ] **WAR 体积优化**: 通过 `mvn dependency:analyze` 移除未使用依赖（当前约 275MB → 目标 < 150 MB）
- [ ] **遗留镜像收敛**: 收敛为两个运行时单元（需部署团队配合）
  - `openclinica-app`：Spring Boot WAR（core + web + ws + app + 新模块）
  - `openclinica-legacy-web`：过渡期保留，承载未迁移的 JSP 页面；JSP 全部绞杀后退役
- [ ] **WS 模块保留**: SOAP 服务仍有外部消费者时保留在 `openclinica-app`，迁移到 REST 后移除

### 5.5 Phase 2 验收标准

- [ ] Schema 边界文档化，模块间禁止直接 DAO 访问
- [ ] 至少 20% 的 JSP 页面有 React 替代（按业务价值权重计算）
- [x] React SPA 和新 Java API 完全通过 Keycloak OIDC JWT 认证（前后端均使用 Keycloak OIDC）
- [x] 前后端构建解耦完成（前端 `pnpm build` → `frontend/dist/`，Java 构建无前端依赖），WAR 体积优化进行中
- [x] `legacy-gateway` API 封装已建立（`/api/v1/legacy/*`），覆盖高频查询

---

## 六、Phase 3: 核心重构（6–12 个月）

> 目标：审计合规 → 遗留核心能力迁移 → 跨模块事务 Saga → JSP 二批绞杀

### 6.1 统一审计事件总线

**核心概念：审计事件（AuditEvent）作为一等公民**

- [ ] **`AuditEvent` 领域模型定义**:
  - `who`（用户 ID + 认证类型）
  - `when`（UTC 时间戳，精度到毫秒）
  - `what`（操作类型：CREATE / UPDATE / DELETE / SIGN / EXPORT / LOCK / UNLOCK）
  - `where`（模块：randomization / crf / export / subject / datacapture）
  - `which`（业务实体 ID + 实体类型）
  - `before`（变更前快照，JSON）
  - `after`（变更后快照，JSON）
  - `why`（电子签名含义，如 "I attest that this data is accurate"）
  - `traceId`（分布式追踪 ID，关联跨模块操作链）

**事件采集机制**

- [ ] **新模块审计集成**: 通过 Spring Modulith `ApplicationEvents` 发布业务事件 → `AuditEventHandler` 异步生成审计记录
  - 事件示例：`RandomizationCompleted` → `AuditEvent(who, what=CREATE, where=randomization, ...)`
  - `ExportJobStarted` → `AuditEvent(who, what=EXPORT, where=export, ...)`
- [ ] **遗留核心审计过渡**:
  - 方案 A: PostgreSQL 触发器 / 逻辑解码（logical decoding）捕获 DML 变更
  - 方案 B: `EntityDAO` 基类中通过 AOP 拦截所有 `create`/`update`/`execute` 操作
- [ ] **电子签名（ELETRONIC_SIGNATURE）特殊处理**:
  - 签名含义文本（reason）
  - 签名时重新认证的身份凭证哈希（credential_hash）
  - 被签名记录的不可变引用（signed_record_ref）

**审计存储策略**

- [ ] **写优化路径**: 审计事件先写入高速消息队列（Redis Stream / RabbitMQ / Kafka）
  - 后台消费者异步批量写入 `audit_log` 表
  - 主业务事务不受审计写入延迟影响（即 fire-and-forget，不阻塞业务响应）
- [ ] **不可变存储**: `audit_log` 表由数据库级约束保证不可篡改
  - 仅 INSERT，不允许 UPDATE / DELETE
  - 由独立的数据库用户权限控制（即使应用层被攻破，审计记录也无法被篡改）
- [ ] **审计归档**: 在线审计表保留最近 2 年数据
  - 历史数据通过只读压缩表或对象存储（MinIO/S3）长期保存
  - 满足 10–25 年的临床试验数据保留法规要求

### 6.2 遗留 DAO/Service 迁移

按「Subject → Study/Site → CRF → Data Capture」优先级逐步迁移：

- [ ] **Subject 模块**（`app/module/subject/`）
  - [ ] 实体 + Repository + Service + Controller
  - [ ] 从 `legacy-core` 的 `SubjectDAO`、`StudySubjectDAO` 迁移查询
  - [ ] 依赖 `legacy-gateway` 做读操作的过渡代理
- [ ] **Study/Site 模块**（`app/module/study/` + `app/module/site/`）
- [ ] **Event 模块**（`app/module/event/`）
- [ ] **Data Capture 模块**（`app/module/data-capture/`）

### 6.3 缓存方案迁移

- [x] **Ehcache 2 → Caffeine** ✅（已于 Phase 1 完成，2026-05-20）
  - Hibernate 二级缓存已切换到 Caffeine
  - 应用级缓存使用 Caffeine + Spring Cache Abstraction
  - Ehcache XML 配置文件已移除

### 6.4 跨模块 Saga 模式

**设计原则：协同式 Saga（Choreography Saga）** — 模块间无直接调用，通过事件松耦合。

**核心流程（以 randomization 为例）**：
1. 随机化模块完成本地事务 → 发布 `RandomizationCompleted` 领域事件
2. 遗留核心适配器监听事件 → 更新 Subject 状态 → 失败时发布 `RandomizationSubjectUpdateFailed` 补偿事件
3. 问卷服务 webhook 适配器监听事件 → 调用 Python API → 失败时进入重试队列
4. 随机化模块监听补偿事件 → 执行业务补偿（撤销分配、标记待人工处理）

- [ ] **补偿事件定义**: `RandomizationSubjectUpdateFailed`、`QuestionnaireWebhookFailed`
- [ ] **Saga 可观测性**: 事件流可视化（Camunda/Temporal/自研 Saga 状态机）

**两种一致性模式的权衡**：

- [ ] **强一致性路径（同步 Saga）**: randomization → subject update 需要相对即时的一致性
  - 使用 Spring Retry + 补偿事务
  - 重试耗尽后进入人工处理队列
- [ ] **最大努力投递（Best-Effort Delivery）**: 非关键路径（如通知邮件、审计写入）
  - 使用异步消息队列 + 死信队列（DLQ）
  - 不阻塞主业务响应，失败后自动重试 3 次，仍失败则进入 DLQ 人工处理
  - 显著降低系统复杂度，适用于不需要即时一致性的场景

### 6.5 JSP 绞杀 — 第二批（运营管理页面）

- [ ] **Study 构建页面迁移**（Protocol 配置、Site 管理）
- [ ] **CRF Designer 渐进式迁移**
  - [ ] 第一阶段: iframe 嵌入旧 Designer，React 外壳
  - [ ] 第二阶段: 基于 SurveyJS 或自定义 Builder 重写
- [ ] **用户管理页面迁移**

### 6.6 Phase 3 验收标准

- [ ] 统一审计总线覆盖 70% 以上的数据变更事件
- [ ] 至少 3 个遗留 DAO/Service 模块迁移到 Modulith
- [x] Ehcache 2 依赖移除，Caffeine 上线 ✅（Phase 1 已提前完成）
- [ ] Saga 模式覆盖 randomization 全流程
- [ ] 50% JSP 页面已完成 React 替代

---

## 七、Phase 4: 收敛（12–18 个月）

> 目标：遗留 JSP < 50 → 服务拆分决策 → 全面可观测性 → 冷启动优化

### 7.1 JSP 绞杀 — 第三批（系统管理 & 遗留报表）

- [ ] 系统配置/审计日志查看页面迁移
- [ ] 遗留报表评估（保留、废弃或用新模块替代）
- [ ] 老旧功能退役（如旧导出 → Export Center 替代后废弃）

### 7.2 服务拆分决策

- [ ] **`randomization-service` 独立部署评估**: 基于实际 QPS 和团队规模
- [ ] **`export-service` 独立部署评估**: 是否有独立扩缩容需求
- [ ] **数据库物理隔离评估**: 需要独立数据库的模块（如 export 可能需要列式存储）

### 7.3 全面可观测性

- [ ] **OpenTelemetry Java Agent 部署**: 跨前端 → Java → Python 链路追踪
- [ ] **日志聚合**: Loki / ELK 部署 + 结构化日志（JSON 格式）
- [ ] **业务黄金指标仪表盘**: CRF 保存数、随机化延迟、导出队列深度、问卷完成率
- [ ] **SLO + 告警**: 为关键业务流程配置 SLO 和 PagerDuty/钉钉告警

### 7.4 冷启动 & 性能优化

- [ ] **GraalVM Native Image 评估**: 冷启动 < 30s 目标
- [ ] **CDS/AppCDS 预热**: 在不迁移 GraalVM 的情况下加速 JVM 启动
- [ ] **性能基线报告**: 对照 Milestone 9 指标 (登录 P95 < 400ms, CRF 保存 < 800ms)

### 7.5 Phase 4 验收标准

- [ ] 遗留 JSP 页面 < 50，评估是否全部废弃或保留为只读归档
- [ ] 测试覆盖率 > 60%（新模块 > 80%）
- [ ] OWASP 扫描常态化（每次 PR 自动检查）
- [ ] 冷启动 < 30s
- [ ] 所有模块通过 Modulith 边界验证

---

## 八、关键决策清单

| 决策点 | 选项 A | 选项 B | 建议 |
|--------|--------|--------|------|
| JSP 迁移策略 | 一次性重写全部 419 个 | 按业务价值分批绞杀 | **B** — 按受试者高频路径优先 |
| 前端部署模式 | 继续嵌入 WAR | 独立 Nginx 静态部署 | **B** — 解耦构建与部署周期 |
| 缓存方案 | 升级 Ehcache 3 | 迁移至 Caffeine | **Caffeine** — 更轻量、无 XML 配置 |
| 审计存储 | 每个模块自建审计表 | 统一审计事件总线 + 独立库 | **统一总线** — 满足监管一致性 |
| 跨服务事务 | 分布式 XA/2PC | Saga + 补偿 | **Saga** — 与 Spring Modulith 事件模型契合 |
| Python 数据库 | 继续独立 PostgreSQL | 收敛到主库（不同 Schema） | **独立但加协调** — 保持技术自主权 |
| 容器编排 | 维持 Docker Compose | 引入 Kubernetes | **先维持 Compose** — 除非出现多环境需求 |

---

## 九、风险登记册（P0/P1/P2）

映射自 [OpenClinica_Review.md](./docs/refactor/OpenClinica_Review.md)，按处理阶段分组：

| ID | 风险 | 等级 | 处理阶段 | 说明 |
|----|------|------|---------|------|
| R01 | Actuator 敏感端点暴露 | P0 | ✅ Phase 1 已修复 | `env`/`beans`/`conditions` 生产禁用 |
| R02 | Swagger UI Try-it-out | P0 | ✅ Phase 1 已修复 | 生产 profile 禁用 |
| R03 | 默认弱密码 | P0 | ✅ Phase 1 已修复 | entrypoint + Dockerfile 默认值移除 |
| R04 | OWASP 漏洞扫描缺失 | P1 | Phase 1 | 配置每周 CI 扫描 |
| R05 | Ehcache 2 配置冲突 | P1 | Phase 1 | 已修复开发/测试环境冲突；Caffeine 迁移在 Phase 3 |
| R06 | WAR 275MB | P1 | Phase 2 | 依赖分析 + 前端解耦 |
| R07 | 认证双轨制 | P1 | Phase 2 | Keycloak OIDC 统一 |
| R08 | JSP 419 页未迁移 | P1 | Phase 2–4 | 分三批绞杀 |
| R09 | 前端测试缺失 | P2 | Phase 1 | Vitest 框架就绪，待编写 |
| R10 | 日志无脱敏 | P2 | Phase 2 | 需配置 logback 脱敏策略 |
| R11 | 审计日志不统一 | P2 | Phase 3 | 统一审计总线 |
| R12 | 跨服务事务无 Saga | P2 | Phase 3 | 随机化 + Subject + webhook |
| R13 | OpenTelemetry 未集成 | P2 | Phase 4 | 链路追踪 |
| R14 | 日志聚合缺失 | P2 | Phase 4 | Loki/ELK |

---

## 十、技术栈基线

```text
后端:    Java 21 + Spring Boot 3.2.5 + Spring Modulith 1.1.4 + Hibernate 6.4.4
数据库:  PostgreSQL 17 + Liquibase (Java) / Alembic (Python)
认证:    Keycloak / OIDC / JWT (统一目标)
API:     OpenAPI 3 + REST + 统一错误码 + 统一分页
前端:    React 19 + TypeScript 5.8 + Vite 6 + Ant Design 5 + TanStack Query 5
部署:    Docker Compose (dev/test/prod) + Nginx
可观测:  Actuator + Prometheus + Grafana → Phase 4: + OpenTelemetry + Loki
CI/CD:   GitHub Actions + Docker build + Compose smoke test
测试:    JUnit 5 + Mockito + Testcontainers + Vitest + Playwright + Modulith verification
缓存:    Ehcache 2 (过渡) → Caffeine (Phase 3)
```

---

## 十一、附录：最小验收标准

跨阶段始终适用的门禁：

- [x] `mvn clean compile` ✅
- [x] `mvn clean package -DskipTests` ✅
- [x] `mvn test` (11/11) ✅
- [x] ModulithVerificationTest ✅
- [x] 前端 `tsc --noEmit` 0 errors ✅
- [x] 前端 `eslint .` 0 errors ✅
- [x] 问卷服务 `pytest` 31/31 ✅
- [ ] Docker Compose 全栈一键启动
- [ ] 数据库 Schema validate 通过（旧备份验证）
- [ ] 核心业务路径人工验证（登录 → Study → Subject → CRF → 保存 → 导出）
- [ ] OWASP Dependency Check 无高危漏洞
- [ ] Maven Enforcer 所有规则通过

---

> **计划维护:** 本 PLAN.md 随 [docs/refactor/OpenClinica_Architecture_Debt_Design.md](./docs/refactor/OpenClinica_Architecture_Debt_Design.md) 更新而同步修订。  
> **阶段切换条件:** 每个阶段完成后，对照其验收标准逐项检查，全部通过后方可进入下一阶段。  
> **实际操作计划:** 详细的代码绞杀步骤见 [.sisyphus/LEGACY_REFACTOR_PLAN.md](./.sisyphus/LEGACY_REFACTOR_PLAN.md)
