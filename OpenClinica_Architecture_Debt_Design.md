# OpenClinica 架构债务治理设计思路

**目标：** 在不冻结业务交付的前提下，将 1300+ 遗留文件、419 JSP、双认证体系、275MB WAR 等架构债务逐步收敛为可维护、可测试、可合规的现代化系统。

**核心原则：**

1. **Strangler Fig（绞杀者模式）** — 新功能不进旧核心，旧能力逐步被新模块替代。
2. **Anti-Corruption Layer（防腐层）** — 新模块绝不直接依赖遗留 core 的实体、DAO 或事务。
3. **Schema 边界先于服务边界** — 先通过数据库 Schema 隔离确立模块所有权，再考虑独立进程。
4. **前端统一优先于后端拆分** — 用户可见的 JSP → React 迁移比 Java 微服务拆分对业务价值更大。

---

## 一、遗留核心治理：从「引力井」到「冻结层」

### 1.1 现状诊断

`core` 模块是系统最大的架构引力源：736 个文件、166 个 Hibernate 实体、257 个 Bean DTO、147 个 DAO、大量 XML Spring 配置。任何新模块（randomization、export、crf）只要引用 core 中的实体或 DAO，就会被拉回到遗留范式中，Modulith 的边界约束名存实亡。

### 1.2 设计思路：遗留核心「冻结层」

将 `core` + `web` + `ws` 明确定义为 **Legacy Kernel（遗留内核）**，并施加以下约束：

**约束一：代码冻结（Code Freeze）**

- 遗留内核不再接受任何新功能开发，仅允许缺陷修复和安全补丁。
- 新需求必须进入 `app/module/*` 或独立服务，即使该需求看似与旧业务强相关。
- 在源码目录中物理隔离：建议将 `core` 重命名为 `legacy-core`，在 IDE 和构建中明确标注为只读遗产。

**约束二：只出不进（Read-Only Export）**

- 遗留内核可以向新模块暴露数据，但新模块绝不允许向遗留内核写入数据。
- 如果新模块需要修改旧表（如 randomization 结果需要更新 subject 状态），必须通过**异步事件**或**补偿事务**，而不是直接操作旧 DAO。

**约束三：API 化封装（API Wrapper）**

- 在 `app` 模块中建立 `legacy-gateway` 子包，将遗留内核中最常用的查询能力（Study 列表、Subject 详情、CRF 元数据、用户权限）封装为 REST API。
- 新前端不再直接调用 JSP URL 或旧 Servlet，而是调用 `legacy-gateway` 提供的 JSON API。
- 这一步的目标是让 React SPA 在功能上摆脱对 JSP 的直接依赖。

### 1.3 数据所有权重新划分

当前所有模块共享同一个数据库，这是最大的隐性耦合。建议按以下步骤建立 Schema 边界：

**阶段 A：逻辑隔离（Schema-per-Module，立即可行）**

- 为每个 Modulith 模块指定其**唯一拥有的表**。例如：
  - `randomization` 模块拥有 `randomization_scheme`、`randomization_assignment` 等 8 张表。
  - `export` 模块拥有 `export_job` 表。
  - `notification` 模块拥有 `notification_event` 表（如尚未创建）。
- 模块对自己拥有的表拥有**完全的读写控制**，其他模块只能通过模块发布的 API 或事件来访问这些数据。
- 遗留内核的表暂时保持共享状态，但新模块不得在其上新增列或索引；如确有必要，由遗留内核维护者评审后执行。

**阶段 B：物理隔离（Database-per-Module，中长期）**

- 当某个模块的表数据量、访问模式或团队规模足以独立演进时，将其迁移到独立数据库实例。
- 独立数据库后，模块可自由选择技术栈（如 `export` 模块可能需要 ClickHouse 或 Elasticsearch 来支撑大数据量导出，而不用受限于 PostgreSQL 的行式存储）。
- 跨库数据一致性采用 **Saga 模式**（编排式或协同式）处理，配合重试和死信队列。

---

## 二、双前端治理：JSP 的「绞杀」路线图

### 2.1 现状

419 个 JSP 页面与 React SPA 并存，路由通过 Nginx 按 `/legacy/*` 和 `/app/*` 分流。两套前端意味着两套状态管理、两套组件库、两套国际化、两套安全模型。

### 2.2 设计思路：按「用户角色 + 高频路径」分批绞杀

不要按技术栈（如"先把所有 JSP 翻译成 React 组件"）来迁移，而要按**业务价值密度**来排序：

**第一批：受试者相关高频页面（数据录入、Query、随机化）**

- 这些页面的用户是 CRC（临床协调员）和 Investigator（研究者），使用频率最高，对 UX 现代化最敏感。
- 新 React 页面需要完整实现：电子签名（eSignature）、审计追踪展示、数据锁定/冻结状态机、双录入比对。这些都是监管核心要求，必须在迁移时同步完成，不能留到"以后再说"。
- 建议为这批页面设计「功能开关」：在 Study 级别配置 "use_new_data_entry_ui: true"，让特定 Study 使用 React 页面，其他 Study 继续使用 JSP，实现金丝雀迁移。

**第二批：运营管理页面（Study 构建、CRF 设计、用户管理）**

- 这些页面的用户是 Data Manager 和系统管理员，使用频率较低，但业务逻辑复杂（如 CRF 字段规则编辑器）。
- CRF Designer 可考虑渐进式策略：保留旧版 Designer 的 iframe 嵌入，但外围导航和 Study 切换使用 React 统一壳。

**第三批：系统管理与报表（日志、配置、遗留报表）**

- 最后迁移的通常是低频管理功能和历史遗留报表。部分功能如果即将被新模块替代（如旧导出被 Export Center 替代），可以直接废弃而不迁移。

### 2.3 技术策略：Hybrid Shell 架构

在迁移完成之前，需要一个过渡架构让用户体验"看起来是统一的"：

**React App Shell + 遗留 iframe/代理内容**

- React 的 `AppLayout`（顶栏 + 侧栏 + 内容区）作为全局外壳。
- 已迁移的页面直接渲染 React 路由组件。
- 未迁移的 JSP 页面通过 `<iframe src="/legacy/xxx">` 嵌入到 React 内容区中。
- iframe 与父页面通过 `postMessage` 进行必要通信（如 Study 切换通知、登录状态同步）。
- 这样用户始终看到统一的品牌、导航和颜色体系，减少"两套系统"的感知。

**遗留 Servlet 的 API 化适配**

- 将 JSP 对应的 Servlet/Controller 中「返回 HTML 的逻辑」与「执行业务的逻辑」分离。
- 业务逻辑层暴露为 JSON API，供 React 调用；HTML 渲染层保持原样供 JSP 使用。
- 当某个 JSP 被绞杀后，其对应的 HTML 渲染层可以直接删除，业务 API 继续保留。

---

## 三、认证体系统一：从「双轨制」到「联邦制」

### 3.1 现状

- 遗留体系：`spring-security-oauth2` 2.0.x + Session + LDAP（可选）。
- 新体系：Keycloak / OIDC + JWT + React SPA。
- 两套体系在同一个 Tomcat 中运行，过滤器链可能相互干扰。

### 3.2 设计思路：OIDC 联邦 + Session Bridge

**目标：** 所有前端（JSP 和 React）和所有后端（Java 和 Python）都通过同一个 Keycloak Realm 进行身份认证和授权。

**遗留 JSP 的 Session Bridge**

- 在 Keycloak 中配置一个 "OpenClinica Legacy" OIDC Client（使用 authorization_code + PKCE 或标准 flow）。
- 部署 Keycloak Tomcat Adapter（或 Spring Security 6 的 OIDC Login 支持）到遗留 `web` 模块。
- 用户访问 `/legacy/*` 时，如果无 Session，重定向到 Keycloak 登录；登录成功后，Keycloak Adapter 将 JWT 中的 claim 映射为传统 Java `UserPrincipal` 和 `GrantedAuthority`，写入 Tomcat Session。
- 这样遗留 JSP 的 `SecureController` 无需重写，它仍然从 Session 中读取用户和权限，但 Session 的创建方式已变为 OIDC 驱动。

**React SPA 的标准 OIDC**

- React 继续使用 Keycloak JavaScript Adapter 或 `react-oidc-context`，通过 JWT Bearer Token 调用后端 API。
- Java 后端（app 模块）统一使用 Spring Security 6 的 `oauth2-resource-server` 验证 JWT。
- 遗留的 `spring-security-oauth2` 2.0.x 依赖及其配置应当被完全移除。

**Python 微服务的 JWT 验证**

- `questionnaire-service` 已使用 `python-jose` 验证 JWT，但应确保它验证的是与 Java 后端相同的 Keycloak Realm 的 token（相同的 JWKS URL、issuer、audience）。
- 建议将 JWT 验证逻辑（包括公钥获取、claim 映射、角色解析）抽象为共享库或配置规范，避免 Java 和 Python 两端对权限模型的理解出现偏差。

**跨系统权限模型对齐**

- 当前系统有 8 种角色 × 18 种权限。建议将这些权限建模为 Keycloak Realm Roles 或 Resource-Based 权限（使用 Keycloak Authorization Services）。
- Java 后端和 Python 后端都从 JWT 的 `realm_access.roles` 或自定义 claim 中解析权限，而不是各自维护独立的权限映射表。

---

## 四、构建与部署解耦：从「巨石 WAR」到「独立制品」

### 4.1 现状

- 275MB WAR 包含了 Java 后端、React 前端构建产物、所有依赖。
- 前端构建嵌入在 Maven 多阶段构建中，前端失败会导致整个 WAR 构建失败。
- 每次前端小改动都需要重新打包整个 Java WAR。

### 4.2 设计思路：制品分离 + 运行时组合

**方案 A：前后端独立部署（推荐）**

- React 前端不再打包进 `app/src/main/resources/static/`，而是作为独立的静态站点部署。
- 构建产出：
  - `openclinica-api.war`（或 JAR）：仅包含 Java 后端（Spring Boot），体积目标 < 150MB。
  - `openclinica-frontend`：一组静态 HTML/JS/CSS 文件，部署到 Nginx 或 CDN。
- Nginx 配置：
  - `/app/*` →  serve `index.html`（React SPA fallback）。
  - `/api/*` → proxy to Java backend。
  - `/legacy/*` → proxy to legacy Tomcat（或保留在 Java WAR 中作为过渡）。
- 这样前端可以独立部署、独立回滚、独立扩容（CDN 缓存），不再受 Java 构建周期约束。

**方案 B：嵌入式但解耦构建（过渡方案）**

- 如果组织短期内无法接受独立部署，至少应在构建流程上解耦：
  - 前端在 CI 中独立构建，产物上传到 Artifactory/S3。
  - Maven 构建时通过 `maven-dependency-plugin` 下载前端产物，而不是在 Dockerfile 中执行 `pnpm build`。
  - 这样前端团队可以在不触发 Java CI 的情况下完成部署。

**遗留 Web/WS 的容器化策略**

- 当前有 `docker/web/Dockerfile`、`docker/ws/Dockerfile`、`docker/app/Dockerfile` 三个镜像。
- 建议收敛为两个运行时单元：
  - `openclinica-app`：Spring Boot WAR（core + web + ws + app + 新模块）。这是唯一需要长期维护的 Java 运行时。
  - `openclinica-legacy-web`：仅在过渡期内保留，用于承载尚未迁移的 JSP 页面。一旦 JSP 全部绞杀，该镜像退役。
- `ws` 模块的 SOAP 服务如果仍有外部消费者调用，应保留在 `openclinica-app` 中，而不是单独部署。SOAP 的消费者迁移到 REST 后，再考虑移除 WS 模块。

---

## 五、依赖治理：建立「安全沙箱」与「退出机制」

### 5.1 遗留依赖的处理策略

对于 Ehcache 2、Quartz 1.8、Commons Collections 3、Castor、JMesa 等陈旧依赖，建议采用分类处理：

**类型 A：可替换且替换成本可控（立即替换）**

- **Commons Lang 2 → Commons Lang 3**：包名从 `org.apache.commons.lang` 变为 `org.apache.commons.lang3`，可以通过全局查找替换完成。Maven Enforcer 可以配置禁止引入 `commons-lang:commons-lang`。
- **Commons Collections 3 → Commons Collections 4**：包名从 `org.apache.commons.collections` 变为 `org.apache.commons.collections4`。注意 `Bag`、`Buffer` 等类在 4 中可能被移除，需评估使用情况。
- **Javassist 3.8**：检查是否被显式引用。Hibernate 6 自带 ByteBuddy，通常不需要独立引入 Javassist。

**类型 B：可替换但替换成本高（分阶段替换）**

- **Ehcache 2 → Caffeine 或 Ehcache 3**：
  - 如果仅用于 Hibernate 二级缓存，迁移至 Ehcache 3（JCache 实现）是平滑的。
  - 如果用于应用级缓存，Caffeine 是更轻量、性能更好的现代选择。
  - 过渡期间，可以通过自定义 Spring Cache Manager 同时支持两种缓存，按模块逐步切换。
- **Quartz 1.8 → Quartz 2.3**：API 变化较大（如 `JobDetailBean` → `JobDetailImpl`），但配置模型更现代化。建议先封装一个 `JobScheduler` 内部接口，将 Quartz 的具体调用隐藏在接口之后，再替换底层实现。

**类型 C：无现代替代品但可移除（直接移除）**

- **JMesa**：遗留 JSP 表格渲染库。随着 JSP 页面被 React 取代，JMesa 自然失去存在价值。可以在依赖分析中标记为 "仅 legacy-web 使用"，并在 JSP 绞杀完成后移除。
- **Castor / XmlSchema**：如果仅用于旧版 WSDL/SOAP 绑定，可以在 WS 模块退役时一起移除。

**类型 D：深度耦合难以替换（隔离封装）**

- 某些遗留依赖可能已经深度嵌入到 core 的 XML 配置或 DAO 层中。对于这类依赖，在 core 被完全绞杀之前，不宜强行替换。
- 策略是**建立依赖沙箱**：在 `legacy-core` 模块的 `pom.xml` 中将这些依赖显式列出，并添加注释说明 "Legacy Only - Do not use in new modules"。Maven Enforcer 可以配置 `bannedDependencies` 规则，防止新模块 `app/module/*` 意外引入这些旧库。

### 5.2 依赖自动化治理

- **OWASP Dependency Check**：每周在 CI 中运行，生成漏洞报告并自动创建工单。当前 PLAN.md 中标记为 "待运行"，这是高风险状态。
- **Dependabot / Renovate**：自动提交依赖升级 PR。对于 Spring Boot、Hibernate、Jackson 等核心框架，可以配置自动合并补丁版本（patch）升级，手动评审小版本（minor）升级。
- **Maven Bill of Materials (BOM)**：当前项目使用了 Spring Modulith BOM，但缺少一个公司/项目级的 `openclinica-dependencies` BOM。将全项目允许的依赖及其版本集中管理，避免子模块各自引入不同版本。

---

## 六、测试体系重建：从「11 个测试」到「质量门禁」

### 6.1 设计思路：分层测试金字塔

针对 1300+ Java 文件只有 11 个测试的现状，不建议试图一次性补全所有测试，而应建立**增量测试策略**：新代码必须有测试，旧代码在修改时必须补测试。

**第一层：Modulith 模块测试（最优先建设）**

- 每个 `app/module/*` 模块必须包含：
  - **单元测试**：Service 层业务逻辑，使用 JUnit 5 + Mockito，不依赖数据库。
  - **集成测试**：Repository + Service + Controller 端到端，使用 `@DataJpaTest` 或 Testcontainers（PostgreSQL 容器），验证数据库交互和事务边界。
  - **模块边界测试**：使用 Spring Modulith 的 `ApplicationModules` 验证模块间不存在非法依赖。
- 这些测试应当进入 CI 质量门禁：PR 合入前必须通过本模块全部测试。

**第二层：遗留核心回归测试（有限投入）**

- 对遗留 core/web 不建议大规模补单元测试（ROI 太低），但应建立**关键路径的契约测试**：
  - 使用 Spring MockMvc 或 RestAssured，对 `legacy-gateway` 暴露的 REST API 编写测试，确保旧核心在升级 Hibernate/Spring 时不会破坏 API 契约。
  - 使用 DBUnit 或 `@Sql` 脚本，为最关键的 5–10 个 DAO 查询编写集成测试，防止数据库 Schema 变更导致查询失败。

**第三层：跨服务契约测试（Python ↔ Java）**

- Java 后端和 Python 微服务之间应建立**消费者驱动的契约测试（CDC）**。
  - 例如：Python 服务定义了 `randomization-completed` webhook 的 JSON Schema，Java 端作为消费者使用 Pact 或 Spring Cloud Contract 验证其发出的消息符合该 Schema。
  - 这样当 Python 服务升级 API 时，可以提前发现是否会破坏 Java 端的调用。

**第四层：前端测试（React）**

- 组件测试：使用 Vitest + React Testing Library，覆盖通用组件（`FormField`、`DataEntryForm`、`StudySwitcher`）的渲染和交互。
- 页面测试：使用 Playwright 编写关键用户旅程的 E2E 测试（登录 → 选择 Study → 打开 CRF → 录入数据 → 保存 → 查看审计日志）。
- E2E 测试应在 CI 中针对 Docker Compose 全栈运行，确保前端、后端、数据库的集成正确。

### 6.2 测试基础设施

- **Testcontainers 标准化**：所有需要数据库的测试统一使用 Testcontainers 的 PostgreSQL 模块，而不是依赖外部数据库或 H2（H2 的 SQL 方言与 PostgreSQL 差异可能导致测试通过但生产失败）。
- **Maven Surefire / Failsafe 升级**：将 `maven-surefire-plugin` 升级至 3.2+，`maven-failsafe-plugin` 用于集成测试阶段。遗留 JUnit 4 测试通过 `junit-vintage-engine` 在 JUnit 5 平台上运行。
- **测试数据工厂**：引入 `instancio` 或自定义的 `TestDataFactory`，避免在测试中编写大量冗长的对象构造代码。

---

## 七、合规与审计架构：从「模块各自为政」到「系统级审计总线」

### 7.1 现状

随机化模块有自己的审计日志表，但遗留核心的审计依赖 Hibernate Envers 或手工插入，没有统一模型。在受监管环境中，审计追踪的缺失或不一致可能导致 FDA 审查失败。

### 7.2 设计思路：统一审计事件总线

**核心概念：审计事件（AuditEvent）作为一等公民**

- 定义一个系统级的 `AuditEvent` 领域模型，包含：who（用户 ID）、when（UTC 时间戳）、what（操作类型：CREATE/UPDATE/DELETE/SIGN/EXPORT）、where（模块：randomization/crf/export）、which（业务实体 ID）、before（变更前快照）、after（变更后快照）、why（电子签名含义，如 "I attest that this data is accurate"）。
- 该模型独立于任何业务模块，存储在独立的 `audit_log` 表中（或独立的数据库实例）。

**事件采集机制**

- **新模块**：通过 Spring Modulith 的 `ApplicationEvents` 发布业务事件（如 `RandomizationCompleted`、`ExportJobStarted`），同时由独立的 `AuditEventHandler` 监听这些事件并生成审计记录。
- **遗留核心**：由于无法全面改造为事件驱动，可以采用以下过渡方案：
  - 在数据库层面使用 PostgreSQL 触发器或逻辑解码（logical decoding）捕获 DML 变更，转化为审计事件。
  - 或在 `EntityDAO` 基类中通过 AOP/拦截器拦截所有 `create`、`update`、`execute` 操作，统一生成审计事件。
- **电子签名**：esignature 动作应当生成一种特殊的审计事件 `ELECTRONIC_SIGNATURE`，包含签名含义、签名时重新认证的身份凭证哈希、以及被签名记录的不可变引用。

**审计存储策略**

- **写优化路径**：审计事件先写入高速消息队列（Redis Stream / RabbitMQ / Kafka），由后台消费者异步批量写入 `audit_log` 表。这样主业务事务不受审计写入延迟影响。
- **不可变存储**：`audit_log` 表应当由数据库级约束保证不可变（如只插入、不更新、不删除），并由独立的数据库用户权限控制，即使应用层被攻破，审计记录也无法被篡改。
- **归档策略**：在线审计表保留最近 2 年数据，历史数据通过只读压缩表或对象存储（MinIO/S3）长期保存，满足 10–25 年的临床试验数据保留法规要求。

---

## 八、数据一致性：跨模块事务的 Saga 与补偿设计

### 8.1 问题场景

当 randomization 模块完成一次随机分配后，需要：

1. 在 randomization 库中记录分配结果。
2. 在 Java 遗留核心中更新 Subject 的随机化状态。
3. 向 Python 问卷服务发送 webhook，触发后续问卷推送。

这三个操作涉及两个数据库和一个外部 HTTP 调用，不能使用传统 ACID 事务。

### 8.2 设计思路：协同式 Saga（Choreography Saga）

- **随机化模块**完成本地事务后，发布 `RandomizationCompleted` 领域事件到事件总线（Spring Modulith 的 `ApplicationEvents` 或外部消息队列）。
- **遗留核心适配器**监听该事件，更新 Subject 状态。如果更新失败，发布 `RandomizationSubjectUpdateFailed` 补偿事件。
- **问卷服务 webhook 适配器**监听该事件，调用 Python API。如果调用失败，记录到重试队列（使用 `tenacity` 或 Celery 的 retry）。
- **随机化模块**监听补偿事件，执行业务补偿（如撤销分配、标记为待人工处理）。

这种设计的优点是模块间无直接调用，通过事件松耦合；缺点是事件流的可观测性要求高，需要引入 Saga 编排可视化工具（如 Camunda、Temporal 或自研的 Saga 状态机）。

对于不需要即时一致性的场景（如导出任务完成后的邮件通知），可以使用**最大努力投递 + 死信队列**模式，降低系统复杂度。

---

## 九、演进路线图建议

### Phase 1：止血（0–3 个月）

- 执行 P0 安全加固（Actuator、Swagger UI、配置加密）。
- 冻结遗留内核代码，建立「遗留层」目录和文档公约。
- 完成 Maven 依赖分析，移除未使用依赖，建立 `bannedDependencies` 清单。
- 恢复关键路径集成测试（使用 Testcontainers）。

### Phase 2：边界建立（3–6 个月）

- 为所有 Modulith 模块划定数据库表所有权，建立 Schema 边界。
- 在 `app` 中完成 `legacy-gateway` API 封装，让 React SPA 可以调用所有高频旧功能。
- 启动 JSP 第一批绞杀（数据录入相关页面），同步实现 eSignature 和审计。
- 认证体系统一：Keycloak OIDC 覆盖 JSP（Session Bridge）和 React（JWT）。
- 前后端构建解耦：前端独立部署到 Nginx，WAR 体积降至 150MB 以下。

### Phase 3：核心重构（6–12 个月）

- 建立统一审计事件总线，覆盖新模块和遗留核心关键操作。
- 将高频遗留 DAO/Service 逐步迁移到 Modulith 模块，按「Subject → Study/Site → CRF → Data Capture」顺序推进。
- 引入 Saga 模式处理跨模块事务。
- 前端完成第二批绞杀（Study 构建、CRF 设计）。

### Phase 4：收敛（12–18 个月）

- 遗留 JSP 页面 < 50 个，评估是否全部废弃或保留为只读归档。
- 评估 `randomization-service`、`export-service` 是否需要拆分为独立进程（基于实际负载和团队规模）。
- 完成 OWASP 依赖安全扫描常态化，测试覆盖率 > 60%（新模块 > 80%）。
- 引入 GraalVM Native Image 或 CDS 预热，将冷启动时间降至 30 秒以内。

---

## 十、关键决策清单

| 决策点 | 选项 A | 选项 B | 建议 |
|--------|--------|--------|------|
| JSP 迁移策略 | 一次性重写全部 419 个 | 按业务价值分批绞杀 | **B** — 按受试者高频路径优先 |
| 前端部署模式 | 继续嵌入 WAR | 独立 Nginx 静态部署 | **B** — 解耦构建与部署周期 |
| 缓存方案 | 升级 Ehcache 3 | 迁移至 Caffeine | **Caffeine** — 更轻量、无 XML 配置 |
| 审计存储 | 每个模块自建审计表 | 统一审计事件总线 + 独立库 | **统一总线** — 满足监管一致性要求 |
| 跨服务事务 | 分布式 XA/2PC | Saga + 补偿 | **Saga** — 与 Spring Modulith 事件模型契合 |
| Python 服务数据库 | 继续独立 PostgreSQL | 收敛到主库（不同 Schema） | **独立但加协调** — 保持技术自主权，但建立 Schema 变更契约 |
| 容器编排 | 维持 Docker Compose | 引入 Kubernetes | **先维持 Compose** — 除非出现多环境、多地域部署需求 |

---

*以上设计思路均为架构级建议，不涉及具体代码实现。实施时应根据团队规模、监管时间表和业务优先级进行裁剪。*
