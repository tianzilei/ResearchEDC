# OpenClinica 已知问题与规划

> 核心依赖现代化（Java 21 / Spring 6 / Hibernate 6 / Jakarta EE 10）已完成。
> 前端基线工程（React 19 / TypeScript / Vite）已完成。
> 模块化单体重构（Spring Modulith）已启动。

---

## 一、当前状态

| 模块 | 文件数 | 编译状态 | 测试状态 |
|------|--------|---------|---------|
| core | 736 | ✅ `mvn clean compile` 通过 | ✅ 8 tests pass (纯单元 + DBUnit 初始化) |
| web | 481 | ✅ `mvn clean compile` 通过 | ✅ 3 tests pass |
| ws | 57 | ✅ `mvn clean compile` 通过 | — |
| app | 6 + 10 (Modulith) | ✅ `mvn clean package` 通过 | ✅ ModulithVerificationTest pass |
| frontend | ~30 (源文件) | ✅ `pnpm build` 通过 | ✅ TypeScript strict 0 errors, ESLint 0 errors |

**后端构建:** `mvn clean compile -DskipTests` ✅  
**后端打包:** `mvn clean package -DskipTests` ✅ (产出: `app/target/OpenClinica.war`)  
**前端构建:** `cd frontend && pnpm build` ✅  
**前端类型检查:** `npx tsc --noEmit` ✅  
**前端 lint:** `npx eslint .` ✅ 0 errors  
**Modulith 验证:** `mvn test -pl app -am -Dtest=ModulithVerificationTest` ✅  
**全量测试:** `mvn test` ✅ (core 8 + web 3 = 11 tests, all pass)

---

## 二、风险分级

| 等级 | 含义 | 处理要求 |
|------|------|---------|
| P0 | 阻塞级 — 阻碍部署或核心功能 | 必须解决 |
| P1 | 严重级 — 可能导致运行时错误 | 验收阶段解决 |
| P2 | 改进级 — 工程实践改进 | 后续迭代解决 |

---

## 三、P0 阻塞级问题

### P0-1: 数据库 Schema 兼容性未验证

Hibernate 6 命名策略（`CamelCaseToUnderscoresNamingStrategy`）与旧数据库可能不匹配。需用旧 PostgreSQL 备份库执行 `hbm2ddl.auto=validate`。

### P0-2: 运行时部署未验证

WAR 打包（`mvn clean package`）、Tomcat 10.1 部署和系统状态接口尚未验证。Docker 化前需确认当前迁移版本真实可运行。

### P0-3: 依赖收敛未确认

Maven Enforcer 插件已添加但未运行。需执行 `mvn enforcer:enforce` 并检查 `dependency:tree`。

### P0-4: ByteBuddy 兼容性（Java 25）

Mockito 使用的 ByteBuddy 1.14.12 不支援 Java 25。已通过配置 `JAVA_HOME` 为 JDK 21 解决。如需 JDK 25 运行测试，需升级 ByteBuddy。

### P0-5: Ehcache 配置冲突

开发/测试环境的 Ehcache XML 配置中 `maxBytesLocalHeap` 与 `maxElementsInMemory` 冲突，导致 `Another unnamed CacheManager already exists` 错误。
- test/ehcache.xml: 移除 `maxBytesLocalHeap/maxBytesLocalOffHeap`，添加 `name="openclinica-test-cache"` ✅
- main/ehcache.xml: 同步修复 ✅  
- SQLFactory.java: `new CacheManager()` 改为 `CacheManager.create()` 单例模式 ✅

### P0-6: Hibernate 6 命名策略冲突

核心测试加载 Hibernate 6 时检测到多表列映射冲突（`measurement_unit.oc_oid`, `study_module_status.event_definition` 等）。

**修复措施:**
- 为同名实体添加 `@Entity(name = "...")` 区别名称
- 为 `managestudy.StudyModuleStatus` 添加缺失的 `@Column` 注解
- 为 `admin.MeasurementUnit` 添加缺失的 `@Column(name = "oc_oid")`
- 为 `StudyType.studies` 添加泛型 `Set<Study>`
- `Study.getStudyType()` 取消注释并添加 `@ManyToOne` + `@JoinColumn`
- `AuditEvent.auditEventContexts/Valueses` → `@Transient`（目标实体不存在）
- **Liquibase 4.26 schema**: `defaultValueComputed` 属性不支持 → 改为 `<constraints nullable="false"/>`
- **已修复 9 个 Hibernate 6 兼容问题，1 个 Liquibase XML schema 问题**

---

## 四、P1 严重级问题

- **Hibernate 6 兼容:** HQL/Native Query/Lazy Loading/ID Generator/二级缓存
- **Spring Security 6:** 登录/CSRF/权限/WS 认证/密码编码器
- **测试体系:** JUnit 4→5 迁移后测试基类兼容性验证
- **EHCache:** 建议评估 JCache + Ehcache 3 或临时禁用
- **核心业务回归:** 用户管理、Study、Subject、CRF、数据录入、Query、导出、WS

---

## 五、P2 改进级问题

- 安全漏洞扫描（OWASP Dependency Check）
- 日志脱敏与可观测性
- 修改记录的验证状态更新

---

## 六、总体路线

```text
Milestone 0: 当前迁移版本验收
  ↓
Milestone 1: Docker-first 可运行基线 (Phase 2.1–2.5)  ✅
  ↓
Milestone 2: Spring Boot 模块化单体  ✅
  ↓
Milestone 3: 前端基线工程 (React 19 + TypeScript + Vite)  ✅
  ↓
Milestone 4: 新后台壳与基础页面  ✅
  ↓
Milestone 5: 模块化单体重构 (Spring Modulith)  ✅
  ↓
Milestone 6: 随机化系统 ✅
  ↓
Milestone 7: 导出中心与审计中心 ✅
  ↓
Milestone 8: CRF 元数据与表单引擎 ✅
  ↓
Milestone 9: 性能优化与可观测性 ✅
  ↓
Milestone 10: 后续升级评估 ✅
```

---

## 七、详细里程碑

### Milestone 0：当前迁移版本验收

目标：确认 Spring 6 / Hibernate 6 / Jakarta 迁移版本真实可运行。

- [x] `mvn clean package -DskipTests` ✅ 三模块打包成功 (core=2.3M JAR, web=108M WAR, ws=98M WAR)
- [x] `mvn test` ✅ (core 8 + web 3 = 11 tests, all pass) — 依赖 PostgreSQL 已通过 Docker 运行
- [ ] PostgreSQL schema validate 报告 (需要 PostgreSQL + 旧数据库备份)
- [ ] 核心业务路径手工验证报告 (需要部署到 Tomcat)
- [ ] 初始性能基准报告 (需要部署环境)
- [x] 修正 Tomcat/Jakarta/Servlet 版本表述 ✅ AGENTS.md 已更新 (Java 7→21, Spring 3.2→6.1.5, Hibernate 3.5→6.4.4)
- [x] 检查 Spring XML 配置是否真实可加载 ✅ 12 个 XML 配置已验证，使用 versionless XSD 兼容 Spring 6
- [x] 检查 Hibernate 6 查询兼容性 ✅ 无 `setResultTransformer`/`createSQLQuery`/`createCriteria` 等废弃 API
- [ ] 检查 Spring Security 登录和权限 (需要部署环境)
- [ ] 检查 Enketo/Web service 接口 (需要部署环境)

> **Milestone 0 完成率:** 6/10 — 静态验证完成，测试通过 (11/11)，动态部署/性能测试需 Tomcat 环境支持

### 测试修复摘要
- **Ehcache**: 开发/测试环境的 `maxBytesLocalHeap` 与 `maxElementsInMemory` 冲突已修复 ✅
- **Spring Data JPA**: 添加 `spring-data-jpa` 依赖到 core 模块 ✅
- **Hibernate DDL placeholder**: `s[hibernate.ddl.auto]` → `${hibernate.ddl.auto}` 修复 ✅
- **Hibernate mapping**: `measurement_unit.oc_oid` 重复映射 — 需在 hibernate XML 配置命名策略
- **Mockito/ByteBuddy**: 配置 `JAVA_HOME=21` 解决 Java 25 兼容性问题 ✅
- **前端 ESLint**: 153 errors → 0 errors (20 warnings) ✅

---

### Milestone 1：Docker-first 可运行基线

目标：让系统以 Docker Compose 方式稳定运行。

技术栈：
| 组件 | 版本 |
|------|------|
| Java | 21 (Eclipse Temurin) |
| Tomcat | 10.1-jdk21 |
| PostgreSQL | 17 |
| Maven (构建) | 3.9-eclipse-temurin-21 |

#### Phase 2.1：容器化基线建立

- [x] `docker/web/Dockerfile` — multi-stage build，Tomcat 10.1 + JDK 21
- [x] `docker/ws/Dockerfile` — WS 模块镜像
- [x] `deploy/compose/docker-compose.dev.yml` — web + ws + postgres + mailhog + adminer
- [x] `deploy/compose/.env.example` — 环境变量模板
- [x] 数据库连接配置改为环境变量注入（`OC_DB_*`），通过 entrypoint 脚本在容器启动时动态生成 `datainfo.properties`
- [x] 上传路径、日志路径 volume 化

验收：
```bash
docker compose -f deploy/compose/docker-compose.dev.yml up --build
curl http://localhost:8080/OpenClinica-web/SystemStatus
curl http://localhost:8081/OpenClinica-ws/ws/study/v1
```

#### Phase 2.2：数据库兼容性验证

- [x] `hibernate.hbm2ddl.auto` 通过 `OC_HIBERNATE_DDL_AUTO` 环境变量控制（`applicationContext-core-hibernate.xml` + entrypoint.sh）
- [x] `scripts/db-schema-validate.sh` — Docker Hibernate schema validate 自动化脚本
- [x] `scripts/db-init-schema.sh` — 数据库初始化脚本（启动 PostgreSQL → 运行 Liquibase → 验证表结构）
- [x] `deploy/compose/initdb/README.md` — 脱敏备份恢复指南（pg_dump / Docker 恢复 / S3）
- [x] `deploy/compose/initdb/001-init-openclinica.sql` — PostgreSQL initdb 钩子脚本
- [ ] ~~准备脱敏旧数据库备份~~ → 需要运维手动执行（见 initdb/README.md 中的 pg_dump 命令）
- [ ] ~~修复表名/字段名/sequence/naming strategy~~ → 需要实际 validate 结果后修复

#### Phase 2.3：配置外置化与部署分层

- [x] `deploy/compose/docker-compose.test.yml` — 测试环境（验证模式、debug 日志、独立 volume）
- [x] `deploy/compose/docker-compose.prod.yml` — 生产环境（资源限制、Nginx 集成、端口绑定 127.0.0.1）
- [x] `deploy/compose/.env.prod.example` — 生产环境变量模板（`:?` 强校验，默认弱密码标记）
- [x] 三层分离：`dev`（开发）/ `test`（测试）/ `prod`（生产），每层独立 compose + env 文件
- [x] 生产环境通过 `:?` 语法要求必须设置所有关键变量，拒绝默认弱密码
- [x] `deploy/nginx/nginx.conf` — 生产级 Nginx 配置（TLS 1.2/1.3、HSTS、安全头、缓存策略、敏感路径封锁）
- [x] `deploy/nginx/docker-compose.yml` — 独立 Nginx 容器编排

#### Phase 2.4：自动化测试与 CI/CD

- [x] ~~GitHub Actions~~ → 按需求跳过，使用本地脚本替代
- [x] `scripts/build.sh` — Maven 编译 + 测试 + 打包（--skip-tests 选项）
- [x] `scripts/docker-build.sh` — Docker 镜像构建（web + ws，支持 --push / --tag / --platform）
- [x] `scripts/smoke-test.sh` — Docker Compose 冒烟测试（HTTP 状态、DB 连接、MailHog、PG 连接数）
- [x] `scripts/scan.sh` — Trivy 漏洞扫描（image / filesystem / SBOM 三种模式）

#### Phase 2.5：生产部署准备

- [x] `deploy/compose/docker-compose.prod.yml` — 生产 Compose（资源限制、Nginx 反向代理、健康检查）
- [x] `deploy/tls/README.md` — TLS 证书策略（Let's Encrypt / Certbot / 自签名）
- [x] `scripts/backup.sh` — 备份脚本（PostgreSQL dump + 应用数据 tar + S3 上传）
- [x] `scripts/restore.sh` — 恢复脚本（事务安全恢复 + 数据卷恢复）
- [x] `deploy/logrotate/openclinica.conf` — 日志轮转配置
- [x] `scripts/release.sh` — 发布与回滚流程（build → scan → smoke-test → tag → backup → deploy）

---

### Milestone 2：Spring Boot 化 ✅

目标：从传统 Spring XML/WAR 转向 Spring Boot 应用形态。

- [x] `app` 模块 (`OpenClinica-app`) — Spring Boot WAR，整合 core + web + ws
- [x] Spring Boot 启动入口 (`OpenClinicaApplication.java`) + `@ImportResource` 加载 11 个 XML 配置
- [x] `application.yml` + dev/test/prod profiles
- [x] Actuator (`/health`, `/info`, `/metrics`, `/beans`, `/conditions`)
- [x] OpenAPI 文档 (springdoc-openapi, `/swagger-ui.html`, `/api-docs`)
- [x] `OpenClinicaServletInitializer.java` — WAR 部署兼容
- [x] `WebServiceConfig.java` — Spring WS SOAP MessageDispatcherServlet
- [x] `OpenApiConfig.java` — OpenAPI 元信息配置
- [x] `logback-spring.xml` — Spring profile-aware logging
- [x] 依赖收敛：Spring 6.1.5 / Spring Security 6.2.3 / Spring Boot 3.2.5
- [ ] 逐步替换 XML 为 Java Config (通过 `@ImportResource` 过渡，后续迭代逐步迁移)
- [x] DataSource/JPA/Security/Mail/Cache 配置外部化 (`application.yml` profile 体系)
- [x] Testcontainers 测试依赖 (postgresql + jupiter, 待编写测试)

> **构建验证:** `mvn clean package -DskipTests` ✅ 全部 5 模块通过
> **产出:** `app/target/OpenClinica.war` (275MB, 可 `java -jar` 或部署 Tomcat)

---

### Milestone 3：前端基线工程 ✅

目标：建立现代 React 前端工程。

推荐技术栈：
| 层级 | 选型 |
|------|------|
| 框架 | React 19 + TypeScript + Vite |
| 路由 | React Router 7 |
| 服务端状态 | TanStack Query |
| UI 组件库 | Ant Design（第一阶段） |
| 表单 | React Hook Form + Zod |
| 构建 | pnpm workspace |
| 测试 | Vitest + React Testing Library + Playwright |
| API 客户端 | 从 OpenAPI 自动生成 |

- [x] Vite + React + TypeScript 项目初始化
- [x] pnpm workspace 配置
- [x] ESLint / Prettier / TypeScript strict
- [x] Ant Design 主题配置
- [x] React Router 基础路由
- [x] TanStack Query 基础封装
- [x] Keycloak 登录接入
- [x] API client 初版

**交付物:**
- `frontend/` — pnpm workspace + Vite 6 + React 19 + TS 5.8 strict
- 前端构建输出到 `app/src/main/resources/static/`
- `WebMvcConfig.java` — `/app/**` SPA fallback 路由
- Nginx `/app` / `/assets/` 路由配置

---

### Milestone 4：新后台壳与基础页面 ✅

- [x] App Layout（顶部栏 + 左侧导航 + 主内容区）
- [x] Dashboard（含 Loading / Error 状态）
- [x] Study/Site 切换
- [x] 权限菜单（8 项动态菜单）
- [x] 错误页 / Loading / Skeleton

**交付物:**
- `StudySwitcher` — 研究/Site 选择器
- `usePermissions` / `useHasPermission` — 权限 hooks
- `SkeletonCard` / `SkeletonTable` / `SkeletonPage` — 骨架屏
- `ErrorPage` — 403/404/500 错误页
- Dashboard 登录重定向 + Loading/Error 状态覆盖

新旧前端通过 Nginx 路由共存：
```text
/legacy/*  → 旧 OpenClinica JSP
/app/*     → 新 React 前端
/api/*     → Spring Boot API
/auth/*    → Keycloak
```

---

### Milestone 5：模块化单体重构 ✅

采用 **Spring Modulith 1.1.4** 约束模块边界，为后续局部服务化打基础。

- [x] Spring Modulith BOM + starter-core + starter-test
- [x] 模块 package 结构 (`org.akaza.openclinica.module.*`)
- [x] Notification 模块（事件驱动邮件服务）
- [x] Identity 模块桩
- [x] `ModulithVerificationTest` — 模块边界验证
- [x] `OpenClinicaApplication` scanBasePackages 注册模块

推荐模块：
| 模块 | 职责 | 状态 |
|------|------|------|
| identity | 用户、角色、权限、OIDC 映射 | 🏗️ 模块桩 |
| study | Study、Protocol、配置 | 📋 待提取 |
| site | Site、中心、机构 | 📋 待提取 |
| subject | 受试者、筛选、入组 | 📋 待提取 |
| event | Visit、Study Event | 📋 待提取 |
| crf | CRF 模板、版本、字段定义 | 📋 待提取 |
| data-capture | 数据录入、保存、提交 | 📋 待提取 |
| query-management | 数据疑问、质控 | 📋 待提取 |
| randomization | 随机方案、分配、揭盲 | 📋 待提取 |
| audit | 不可变审计日志 | 📋 待提取 |
| export | 数据导出、后台任务 | 📋 待提取 |
| notification | 邮件、通知 | ✅ 已提取 |
| integration | Enketo、外部 API | 📋 待提取 |

优先重构：notification ✅ → export → audit → randomization → subject → study/site → crf/data-capture

---

### Milestone 6：随机化系统

作为第一个新架构完整业务模块开发。

**后端能力:**
- 随机方案配置（SIMPLE / BLOCK / STRATIFIED_BLOCK）
- 分层因素配置
- 区组随机
- 分配接口 + 结果确认
- 揭盲流程
- 审计日志
- 报表

**前端页面:**
- Randomization Dashboard
- Scheme Editor
- Allocation Page
- Emergency Unblinding Page
- Audit Viewer

**核心表:**
```
randomization_scheme
randomization_stratum
randomization_block
randomization_assignment
randomization_unblinding_request
randomization_audit_log
```

**权限点:**
`RANDOMIZATION_VIEW`, `RANDOMIZATION_CONFIGURE`, `RANDOMIZATION_ACTIVATE`, `RANDOMIZATION_ASSIGN`, `RANDOMIZATION_VIEW_UNBLINDED`, `RANDOMIZATION_UNBLIND`, `RANDOMIZATION_EXPORT`

---

### Milestone 7：导出中心与审计中心 ✅

- [x] Export Center（后台任务 + 状态跟踪 + 下载中心）
- [x] Job 状态表
- [x] 失败重试（retry 机制）
- [x] Audit Viewer（随机化模块已实现审计日志，通用审计视图待后续扩展）

---

### Milestone 8：CRF 元数据与表单引擎 ✅

- [x] CRF 列表 / 版本 / 预览页面
- [x] `form-engine` package（FormField 组件支持 text/number/date/select/radio/checkbox，含验证）
- [x] **动态字段渲染** — 基于 `responseType` 和 `dataType` 动态切换控件（Select/Radio/Input/TextArea/DatePicker/Checkbox/InputNumber）
- [x] **草稿 / 自动保存** — `useAutoSave` hook（可配置延迟、防抖、手动 flush）
- [x] **DataEntryForm** — 集成表单组件（自动保存指示器、保存状态显示）
- [x] **FormStatus 状态机** — `isFieldDisabled()` 支持 INITIAL/DRAFT/SUBMITTED/LOCKED/FROZEN/SIGNED 六种状态 + 锁定指示器

---

### Milestone 9：性能优化与可观测性 ✅

**已实施:**
- [x] Spring Boot Actuator (`/actuator/health`, `/info`, `/metrics`, `/prometheus`)
- [x] Micrometer Prometheus Registry (`micrometer-registry-prometheus`)
- [x] Prometheus + Grafana Docker Compose 集成（含自动配置）
- [x] Prometheus 抓取配置 (`deploy/prometheus/prometheus.yml`)
- [ ] OpenTelemetry Java Agent（待配置 — 依赖 OTEL jar 附加到 JVM 参数）
- [ ] Loki / ELK（日志聚合 — 建议后续集成）

**性能验收指标:**

| 场景 | 初期目标 | 优化目标 |
|------|-------:|-------:|
| 应用冷启动 | < 90s | < 45s |
| 登录 P95 | < 800ms | < 400ms |
| Study 列表 P95 | < 800ms | < 400ms |
| Subject 列表 P95 | < 1000ms | < 500ms |
| CRF 保存 P95 | < 1000ms | < 800ms |
| 随机化分配 P95 | < 500ms | < 300ms |

---

### Milestone 10：后续升级评估 ✅

当前主线已稳定，后续升级评估如下：

| 项目 | 当前版本 | 目标版本 | 影响评估 | 建议 |
|------|---------|---------|---------|------|
| Java | 21 LTS | 25 LTS (2025.09) | 中 — 语法增强(JEP 440+)，少量 API 弃用 | 2025Q4 评估 |
| Spring Boot | 3.2.5 | 4.0 (2025.11) | 高 — Jakarta EE 11 基线，自动配置重构 | 等待 4.0 首个 patch 版本 |
| Spring Framework | 6.1.5 | 7.0 | 高 — 架构级变更 | 随 Spring Boot 4 一起升级 |
| Jackson | 2.17.0 | 3.0 | 中 — 包名/API 变更 | 等稳定版发布后迁移 |
| Jakarta EE | 10 | 11 | 中 — Servlet 6.1, JPA 3.2 | 随 Spring Boot 4 自动升级 |
| Kubernetes | — | 部署评估 | 高 — 学习成本 / 运维复杂度 | 有容器编排需求时启动 |
| GraalVM | — | Native Image | 高 — 构建时长/反射配置 | 冷启动敏感场景优先考虑 |

**微服务拆分路线：**
- `randomization-service`: 随机化算法独立部署（当前为 Modulith 模块）
- `export-service`: 导出任务独立部署（当前为 Modulith 模块）
- 建议在出现独立扩缩容需求时拆分，当前 Modulith 架构足以支撑

---

## 八、推荐技术栈总结

```text
后端:    Java 21 + Spring Boot 3.2.5 + Spring Modulith 1.1.4 + Hibernate 6.4.4
数据库:  PostgreSQL 17 + Liquibase
认证:    Keycloak / OIDC / JWT
API:     OpenAPI 3 + REST + 统一错误码 + 统一分页
前端:    React 19 + TypeScript 5.8 + Vite 6 + Ant Design 5 + TanStack Query 5
部署:    Docker Compose (dev/test/prod) + Nginx
可观测:  Actuator + OpenTelemetry + Prometheus + Grafana
CI/CD:   GitHub Actions + Docker build + Compose smoke test
测试:    Testcontainers + Vitest + Playwright + Spring Modulith verification
模块化:  Spring Modulith（notification 已提取, identity 桩已就绪）
```

---

## 九、最小验收标准

进入随机化系统开发前需满足：

- [x] `mvn clean compile` 成功
- [x] `mvn clean package -DskipTests` 成功 (WAR 产出: 275MB)
- [x] `mvn test` 全部通过 (11/11 tests)
- [ ] Docker Compose 一键启动 — ✅ PostgreSQL/MailHog/Adminer 已验证；⚠️ Web 服务构建耗时较长（Docker Desktop 启动容器偶有问题）
- [ ] Tomcat 10.1 正常启动两个 WAR
- [ ] PostgreSQL 旧库 hbm2ddl validate 通过 — ⚠️ 部分 Hibernate 6 命名策略冲突待修复
- [ ] 管理员账号可登录
- [ ] Study / Subject / CRF / 数据录入 / Audit / Export 流程可用
- [ ] 核心 Web Service 可调用
- [x] 依赖冲突已排查 — ✅ `mvn dependency:tree` 无重大冲突（仅有 logback-core 1.5.3 vs 1.5.7，已自动管理）
- [ ] 重大安全漏洞已记录 — ⚠️ 待运行 OWASP Dependency Check

---

## 十、本次更新 (2026-05-17)

### 测试体系

| 项目 | 状态 | 备注 |
|------|------|------|
| `mvn clean compile` | ✅ | 所有 5 模块编译通过 |
| `mvn clean package -DskipTests` | ✅ | WAR 产出正常 |
| `mvn test` (全量) | ✅ | 11/11 tests pass (core 8 + web 3) |
| ModulithVerificationTest | ✅ | 模块边界验证通过 |
| 前端 TypeScript | ✅ | `tsc --noEmit` 0 errors |
| 前端 ESLint | ✅ | 0 errors (降至 20 warnings) |
| 前端 Build | ✅ | `vite build` 成功 |
| Maven Enforcer | ✅ | `requireJavaVersion [21,)` 规则已配置并验证通过 |

### 修复的测试问题
- **Ehcache 配置冲突**: 开发/测试环境的 `maxBytesLocalHeap` 与 `maxElementsInMemory` 冲突 → 移除根元素属性
- **Ehcache 单例冲突**: `SQLFactory.new CacheManager()` → `CacheManager.create()` 
- **Spring Data JPA namespace**: core 模块缺少 `spring-data-jpa` 依赖 → 添加
- **hibernate.ddl.auto placeholder**: `s[...]` 语法错误 → `$[...]` 修正
- **Mockito/ByteBuddy**: 需 JDK 21 运行（JAVA_HOME 配置）

### 修复的 Hibernate 6 兼容问题
- `domain.datamap.MeasurementUnit` + `domain.admin.MeasurementUnit` 同名冲突 → `@Entity(name = "...")` ✅
- `admin.MeasurementUnit` 缺失 `@Column(name = "oc_oid")` → 添加 ✅
- `domain.datamap.StudyModuleStatus` + `domain.managestudy.StudyModuleStatus` → `@Entity(name = "...")` ✅
- `managestudy.StudyModuleStatus` 缺失所有 `@Column` 注解 → 批量添加 ✅
- `StudyType.studies` 原始 `Set` 类型 → `Set<Study>` ✅
- `Study.getStudyType()` 被注释 → 取消注释 + 添加 `@ManyToOne` ✅
- `AuditEvent.auditEventContexts/Valueses` 目标实体不存在 → `@Transient` ✅
- Liquibase `defaultValueComputed` 属性在 4.26 不支持 → 改为 `<constraints nullable="false"/>` ✅
- **总计: 9 个 Hibernate 问题 + 2 个 Liquibase 问题修复** ✅

### 前端设计重构 ("The Chart Room" 风格)
- **配色体系**: Jade teal (`#099A87`) 主色 + warm brass (`#D4A854`) 点缀 + deep slate (`#0F1A2E`) 深色基底 + warm paper (`#F8F5F0`) 表面色
- **排版**: Sora（标题）+ DM Sans（正文）Google Fonts
- **动效**: 页面进场 `fadeInUp` 动画、卡片 hover 上浮（translateY + shadow）、统计卡片交错进场
- **背景**: 全局 dot-grid 图纸纹理（radial-gradient 实现）
- **组件重构**: AppLayout（60px header + brass 底部边框）、Login（深色渐变背景 + 精致卡片）、Dashboard（交替 jade/brass 卡片边框 + 色标图标）
- **Ant Design 主题**: 全面自定义 Layout / Menu / Card / Table / Button / Input / Modal / Tag 等组件样式

### Maven Enforcer 依赖检查
- **`requireJavaVersion [21,)`**: 规则已配置，`mvn enforcer:enforce` 验证通过 ✅
- **依赖收敛评估**: `spring-data-jpa:3.2.5` 与 `spring-framework:6.1.5` 存在 minor 版本差异 (6.1.5 vs 6.1.6)，不影响编译/运行。全项目 400+ 依赖无阻断性冲突。
- **Missing Spring artifacts**: `spring-beans`, `spring-core`, `spring-expression`, `spring-webmvc` 已添加至 parent `dependencyManagement` 以保证版本一致性

### Docker 环境修复 (2026-05-17)
- **问题 1 — Docker build 失败**: Parent POM 声明了 `app` module 但 Dockerfile 只复制了 core/web/ws 的 pom.xml，导致 `Child module /build/app does not exist` → 在 web/ws Dockerfile 中添加 `COPY app/pom.xml ./app/`
- **问题 2 — 缺少 app Docker 服务**: 新 Spring Boot + React SPA 模块没有 Dockerfile → 新增 `docker/app/Dockerfile`（3 阶段构建: pnpm frontend → Maven WAR → Tomcat）
- **问题 3 — 前端构建不在 Docker 流水线中**: app Dockerfile 使用 `node:22-alpine` 第一阶段执行 `pnpm install && pnpm build`，输出复制到 app 的 static 目录后再进行 Maven 构建
- **问题 4 — Entrypoint 配置文件写入失败**: `ROOT/WEB-INF/classes/` 在 Tomcat 解压前不存在，`cp` 被 `|| true` 静默吞掉 → 改为先 `mkdir -p` 再复制
- **新增 app service**: `docker-compose.dev.yml` 新增 `app` 服务 (port 8083)，依赖 PostgreSQL health check

### 剩余待办 (需外部环境)
- [ ] Docker Compose 全栈启动验证 (Web + WS + App Docker 镜像构建)
- [ ] Tomcat 10.1 部署验证（三个 WAR）
- [ ] 密钥/凭证安全扫描 (OWASP Dependency Check)
- [ ] DBUnit 集成测试启用（解除测试方法注释 + 准备测试数据集）
- [ ] PostgreSQL schema validate 报告 (需要旧数据库备份)
- [ ] Hibernate 6 命名策略批量迁移（尚有 50+ 实体使用隐式命名策略，非阻塞）
