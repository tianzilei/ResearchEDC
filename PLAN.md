# OpenClinica 架构债务治理计划

> **设计依据:** [OpenClinica_Architecture_Debt_Design.md](./OpenClinica_Architecture_Debt_Design.md)  
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
| **Spring Modulith** | notification 模块已提取, identity 模块桩, 边界验证测试 | ✅ |
| **前端基线** | React 19 + TypeScript strict + Vite 6 + Ant Design 5 + TanStack Query 5 | ✅ |
| **前端 "Precision Clinical"** | 设计重构: 配色/排版/动效/AppLayout/Dashboard | ✅ |
| **随机化系统** | 3 种算法, 8 表 REST API, 前端 6 页面 | ✅ |
| **导出中心** | 异步任务, 状态跟踪, 下载中心 | ✅ |
| **CRF 元数据 & 表单引擎** | 列表/版本/预览, FormField/DataEntryForm/useAutoSave/FormStatus | ✅ |
| **问卷微服务** | FastAPI + 评分引擎 (ISI/GAD-7/PHQ-9/ESS), 31/31 tests | ✅ |
| **Docker 部署** | 三层 Compose (dev/test/prod), Nginx + TLS + Prometheus + Grafana | ✅ |
| **Maven 构建** | `mvn compile` ✅, `mvn package` ✅, `mvn test` 11/11 ✅ | ✅ |
| **Hibernate 6 兼容** | 修复 9 个实体映射 + 2 个 Liquibase 问题 | ✅ |
| **P0 安全加固** | Actuator 端点封锁, Swagger UI 禁用, entrypoint 默认密码移除 | ✅ (2026-05-18) |
| **P1 Maven 插件升级** | surefire 2.10 → 3.2.5, resources 2.5 → 3.3.1 | ✅ (2026-05-18) |
| **P2 构建/Git 优化** | 前端构建产物 .gitignore, JVM 容器化 CATALINA_OPTS | ✅ (2026-05-18) |

### 1.2 遗留债务快照

| 指标 | 数值 | 趋势 |
|------|------|------|
| 遗留 Java 文件 (core+web+ws) | ~1300 | 🔴 需冻结增长 |
| JSP 页面数 | 419 | 🔴 需绞杀 |
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
- [ ] **OWASP Dependency Check** — 在 CI 中配置每周扫描，创建漏洞工单
- [ ] **Trivy 扫描集成** — 在 `scripts/scan.sh` 基础上，配置 PR 级镜像扫描

### 4.2 遗留内核冻结

- [ ] **物理目录重命名**: `core/` → `legacy-core/`，在 IDE 和构建中明确标注为只读遗产
- [ ] **Maven Enforcer `bannedDependencies`**: 禁止新模块 (`app/module/*`) 引入遗留库（Ehcache 2、Quartz 1.8、Commons Collections 3 等）
- [ ] **提交规范**: 在 `.git/hooks/pre-commit` 或 CI 中检查 legacy 目录是否有非修复性变更
- [ ] **文档标注**: 在 `legacy-core/AGENTS.md` 中标注代码冻结策略

### 4.3 依赖现代化（第一批 — 替换成本低的）

- [ ] **Commons Lang 2 → 3**: 包名 `org.apache.commons.lang` → `org.apache.commons.lang3`，全局替换
- [ ] **Commons Collections 3 → 4**: 包名 `org.apache.commons.collections` → `org.apache.commons.collections4`
- [ ] **Javassist 3.8 移除**: 检查是否被显式引用，Hibernate 6 自带 ByteBuddy
- [ ] **Maven 插件升级**:
  - [x] `maven-surefire-plugin`: 2.10 → 3.2.5
  - [x] `maven-resources-plugin`: 2.5 → 3.3.1
  - [ ] `maven-release-plugin`: 2.3 → 3.0.1
  - [ ] `maven-assembly-plugin`: 2.2.2 → 3.7.1
  - [ ] `cargo-maven2-plugin`: 1.1.4 → 评估是否仍需要
  - [ ] `liquibase-plugin`: 1.9.x → `liquibase-maven-plugin` 4.x
  - [ ] `maven-jaxb2-plugin`: 0.7.5 → 评估是否需要（JAXB 4 已内置）

### 4.4 增量测试门禁

- [ ] **Modulith 模块测试规范**: 每个 `app/module/*` 必须包含单元测试 + 集成测试 + 边界测试
- [ ] **Testcontainers 标准化**: 统一使用 PostgreSQL 容器，替代废弃的 DBUnit
- [ ] **关键路径契约测试**: 对 `legacy-gateway` 暴露的 REST API 编写 Spring MockMvc 测试
- [ ] **留核心回归测试**: 为 5–10 个最关键的 DAO 查询编写集成测试（`@Sql` + Testcontainers）
- [ ] **Maven Failsafe 配置**: 分离单元测试（surefire）和集成测试（failsafe）

### 4.5 Phase 1 验收标准

- [ ] OWASP Dependency Check 首次运行完成，高危漏洞已记录
- [ ] 遗留内核冻结策略文档化并执行
- [ ] Commons Lang/Collections 迁移完成，编译通过
- [ ] Maven Enforcer `bannedDependencies` 规则生效
- [ ] 至少 1 个 Modulith 模块达到 "must have tests" 标准

---

## 五、Phase 2: 边界建立（3–6 个月）

> 目标：Schema 边界正式划定 → 高频 JSP 开始绞杀 → 认证统一 → 构建解耦

### 5.1 Schema 边界（Schema-per-Module）

- [ ] **表所有权声明**: 为每个 Modulith 模块指定唯一拥有的表
  - `randomization` → `randomization_scheme`, `randomization_assignment` 等 8 表
  - `export` → `export_job` 表
  - `notification` → `notification_event` 表
  - 后续模块同理
- [ ] **模块 API 契约**: 模块间数据访问必须通过 API 或事件，禁止跨模块直接 DAO
- [ ] **遗留表变更控制**: 新模块不得在遗留表上新增列或索引，确有必要时评审后执行

### 5.2 JSP 绞杀 — 第一批（受试者高频页面）

- [ ] **功能开关设计**: 在 Study 级别配置 `use_new_data_entry_ui: true`，支持金丝雀迁移
- [ ] **数据录入页面迁移**: React 实现，完整包含：
  - [ ] 电子签名（eSignature）工作流
  - [ ] 审计追踪展示
  - [ ] 数据锁定/冻结状态机
  - [ ] 双录入比对
- [ ] **Query 管理页面迁移**
- [ ] **随机化相关页面迁移**（React 已实现，验证完整性）
- [ ] **Hybrid Shell 架构**: React AppLayout 嵌入遗留 JSP iframe（`postMessage` 通信）
- [ ] `legacy-gateway` API 封装: 高频查询（Study、Subject、CRF 元数据）JSON API

### 5.3 认证体系统一（OIDC + Session Bridge）

- [ ] **Keycloak Tomcat Adapter 部署**: JSP 页面通过 OIDC authorization_code flow 登录
- [ ] **SecureController 适配**: JWT claim → `UserPrincipal` + `GrantedAuthority` 写入 Tomcat Session
- [ ] **React SPA 标准 OIDC**: 继续使用 Keycloak JS Adapter / `react-oidc-context`
- [ ] **Spring Security 6 Resource Server**: Java API 统一验证 JWT Bearer Token
- [ ] **旧版 `spring-security-oauth2` 移除**: 确认无引用后删除依赖
- [ ] **Python JWT 验证对齐**: `questionnaire-service` 使用与 Java 相同的 Keycloak JWKS URL + issuer + audience
- [ ] **权限模型统一**: 8 种角色 × 18 种权限建模为 Keycloak Realm Roles

### 5.4 构建与部署解耦

- [ ] **方案评估**: 选择「前后端独立部署」(推荐) 或「嵌入式但解耦构建」(过渡)
- [ ] **前端独立部署**: Nginx 直接 serve `frontend/dist/`，不再打包进 WAR
- [ ] **WAR 体积优化**: 通过 `mvn dependency:analyze` 移除未使用依赖，目标 < 150 MB
- [ ] **遗留镜像收敛**: `openclinica-app` (主运行时) + `openclinica-legacy-web` (过渡期)

### 5.5 Phase 2 验收标准

- [ ] Schema 边界文档化，模块间禁止直接 DAO 访问
- [ ] 至少 20% 的 JSP 页面有 React 替代（按业务价值权重计算）
- [ ] React SPA 和新 Java API 完全通过 Keycloak OIDC JWT 认证
- [ ] 前后端构建解耦完成，WAR < 150 MB
- [ ] `legacy-gateway` API 封装覆盖 Top 10 高频查询

---

## 六、Phase 3: 核心重构（6–12 个月）

> 目标：审计合规 → 遗留核心能力迁移 → 跨模块事务 Saga → JSP 二批绞杀

### 6.1 统一审计事件总线

- [ ] **`AuditEvent` 领域模型定义**: who/when/what/where/which/before/after/why
- [ ] **审计独立存储**: 独立的 `audit_log` 表（只插入、不更新、不删除）
- [ ] **新模块审计集成**: 通过 Spring Modulith `ApplicationEvents` 发布业务事件 → `AuditEventHandler` 生成审计记录
- [ ] **遗留核心审计过渡**:
  - 方案 A: PostgreSQL 触发器 / 逻辑解码捕获 DML 变更
  - 方案 B: `EntityDAO` 基类 AOP 拦截
- [ ] **电子签名审计事件**: `ELECTRONIC_SIGNATURE` 类型，包含签名含义和身份凭证哈希
- [ ] **审计归档**: 在线 2 年 + 只读压缩存储 10–25 年

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

- [ ] **Ehcache 2 → Caffeine**
  - [ ] Hibernate 二级缓存: 切换为 Caffeine 或 Ehcache 3 (JCache)
  - [ ] 应用级缓存: Caffeine + Spring Cache Abstraction
  - [ ] 过渡期: 自定义 `CacheManager` 同时支持两种缓存，按模块切换

### 6.4 跨模块 Saga 模式

- [ ] **Saga 编排设计**: 为 randomization + subject update + questionnaire webhook 设计协同式 Saga
- [ ] **补偿事件定义**: `RandomizationSubjectUpdateFailed`、`QuestionnaireWebhookFailed`
- [ ] **重试 + 死信队列**: 使用 Spring Retry + DLQ 处理临时故障
- [ ] **Saga 可观测性**: 事件流可视化（Camunda/Temporal/自研）

### 6.5 JSP 绞杀 — 第二批（运营管理页面）

- [ ] **Study 构建页面迁移**（Protocol 配置、Site 管理）
- [ ] **CRF Designer 渐进式迁移**
  - [ ] 第一阶段: iframe 嵌入旧 Designer，React 外壳
  - [ ] 第二阶段: 基于 SurveyJS 或自定义 Builder 重写
- [ ] **用户管理页面迁移**

### 6.6 Phase 3 验收标准

- [ ] 统一审计总线覆盖 70% 以上的数据变更事件
- [ ] 至少 3 个遗留 DAO/Service 模块迁移到 Modulith
- [ ] Ehcache 2 依赖移除，Caffeine 上线
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

映射自 [OpenClinica_Review.md](./OpenClinica_Review.md)，按处理阶段分组：

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

> **计划维护:** 本 PLAN.md 随 OpenClinica_Architecture_Debt_Design.md 更新而同步修订。  
> **阶段切换条件:** 每个阶段完成后，对照其验收标准逐项检查，全部通过后方可进入下一阶段。
