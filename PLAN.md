# OpenClinica 已知问题与规划

> 核心依赖现代化（Java 21 / Spring 6 / Hibernate 6 / Jakarta EE 10）已完成，进入**系统综合重构**阶段。

---

## 一、当前状态

| 模块 | 文件数 | 编译状态 |
|------|--------|---------|
| core | 736 | ✅ `mvn clean compile` 通过 |
| web | 481 | ✅ `mvn clean compile` 通过 |
| ws | 57 | ✅ `mvn clean compile` 通过 |

**构建命令:** `mvn clean compile -DskipTests`

当前系统仍偏传统 WAR/XML/Servlet 形态，前端陈旧，部署方式传统，模块边界不清，未建立性能基线、测试和可观测性体系。下一阶段应从依赖升级转入综合性重构。

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
Milestone 1: Docker-first 可运行基线 (Phase 2.1–2.5)
  ↓
Milestone 2: Spring Boot 模块化单体
  ↓
Milestone 3: 前端基线工程 (React 19 + TypeScript + Vite)
  ↓
Milestone 4: 新后台壳与基础页面
  ↓
Milestone 5: 模块化单体重构 (Spring Modulith)
  ↓
Milestone 6: 随机化系统 (第一个新架构模块)
  ↓
Milestone 7: 导出中心与审计中心
  ↓
Milestone 8: CRF 元数据与表单引擎
  ↓
Milestone 9: 性能优化与可观测性
  ↓
Milestone 10: 后续升级评估 (Java 25 / SB 4 / K8s)
```

---

## 七、详细里程碑

### Milestone 0：当前迁移版本验收

目标：确认 Spring 6 / Hibernate 6 / Jakarta 迁移版本真实可运行。

- [x] `mvn clean package -DskipTests` ✅ 三模块打包成功 (core=2.3M JAR, web=108M WAR, ws=98M WAR)
- [ ] `mvn test` 测试日志 (需要 PostgreSQL 数据库运行)
- [ ] PostgreSQL schema validate 报告 (需要 PostgreSQL + 旧数据库备份)
- [ ] 核心业务路径手工验证报告 (需要部署到 Tomcat)
- [ ] 初始性能基准报告 (需要部署环境)
- [x] 修正 Tomcat/Jakarta/Servlet 版本表述 ✅ AGENTS.md 已更新 (Java 7→21, Spring 3.2→6.1.5, Hibernate 3.5→6.4.4)
- [x] 检查 Spring XML 配置是否真实可加载 ✅ 12 个 XML 配置已验证，使用 versionless XSD 兼容 Spring 6
- [x] 检查 Hibernate 6 查询兼容性 ✅ 无 `setResultTransformer`/`createSQLQuery`/`createCriteria` 等废弃 API
- [ ] 检查 Spring Security 登录和权限 (需要部署环境)
- [ ] 检查 Enketo/Web service 接口 (需要部署环境)

> **Milestone 0 完成率:** 4/10 — 静态验证完成，动态测试 (测试/部署/性能) 需 Docker 环境支持

> **Milestone 2 完成率:** 10/12 — 核心 Spring Boot 骨架已完成。XML→Java Config 逐步迁移和 Testcontainers 测试用例为后续迭代。

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

- [ ] ~~GitHub Actions~~ → 按需求跳过
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

### Milestone 3：前端基线工程

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

- [ ] Vite + React + TypeScript 项目初始化
- [ ] pnpm workspace 配置
- [ ] ESLint / Prettier / TypeScript strict
- [ ] Ant Design 主题配置
- [ ] React Router 基础路由
- [ ] TanStack Query 基础封装
- [ ] Keycloak 登录接入
- [ ] API client 初版

---

### Milestone 4：新后台壳与基础页面

- [ ] App Layout（顶部栏 + 左侧导航 + 主内容区）
- [ ] Dashboard
- [ ] Study/Site 切换
- [ ] 权限菜单
- [ ] 错误页 / Loading / Skeleton

新旧前端通过 Nginx 路由共存：
```text
/legacy/*  → 旧 OpenClinica JSP
/app/*     → 新 React 前端
/api/*     → Spring Boot API
/auth/*    → Keycloak
```

---

### Milestone 5：模块化单体重构

采用 **Spring Modulith** 约束模块边界，为后续局部服务化打基础。

推荐模块：
| 模块 | 职责 |
|------|------|
| identity | 用户、角色、权限、OIDC 映射 |
| study | Study、Protocol、配置 |
| site | Site、中心、机构 |
| subject | 受试者、筛选、入组 |
| event | Visit、Study Event |
| crf | CRF 模板、版本、字段定义 |
| data-capture | 数据录入、保存、提交 |
| query-management | 数据疑问、质控 |
| randomization | 随机方案、分配、揭盲 |
| audit | 不可变审计日志 |
| export | 数据导出、后台任务 |
| notification | 邮件、通知 |
| integration | Enketo、外部 API |

优先重构：notification → export → audit → randomization → subject → study/site → crf/data-capture

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

### Milestone 7：导出中心与审计中心

- [ ] Export Center（后台任务 + 状态跟踪 + 下载中心）
- [ ] Job 状态表
- [ ] 失败重试
- [ ] Audit Viewer（操作前后 diff + 时间线）

---

### Milestone 8：CRF 元数据与表单引擎

- [ ] CRF 列表 / 版本 / 预览页面
- [ ] `form-engine` package（字段渲染、校验、跳题、审计）
- [ ] 动态字段渲染
- [ ] 草稿保存 / 自动保存
- [ ] 只读 / 冻结 / 锁定状态

---

### Milestone 9：性能优化与可观测性

**推荐组件:**
```
Spring Boot Actuator
OpenTelemetry Java Agent
Prometheus + Grafana
Loki / ELK
```

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

### Milestone 10：后续升级评估

主线稳定后评估：
- Java 25 / Spring Boot 4 / Spring Framework 7
- Jackson 3 / Jakarta EE 11
- Kubernetes / Helm
- GraalVM Native Image
- randomization-service / export-service 独立服务化

---

## 八、推荐技术栈总结

```text
后端:    Java 21 + Spring Boot 3.5.x + Spring Modulith + Hibernate 6.x
数据库:  PostgreSQL 17 + Flyway/Liquibase
认证:    Keycloak / OIDC / JWT
API:     OpenAPI 3 + REST + 统一错误码 + 统一分页
前端:    React 19 + TypeScript + Vite + Ant Design + TanStack Query
部署:    Docker Compose (dev/test/prod)
可观测:  Actuator + OpenTelemetry + Prometheus + Grafana
CI/CD:   GitHub Actions + Docker build + Compose smoke test
测试:    Testcontainers + Vitest + Playwright
```

---

## 九、最小验收标准

进入随机化系统开发前需满足：

- [x] `mvn clean compile` 成功
- [ ] `mvn clean package -DskipTests` 成功
- [ ] Docker Compose 一键启动
- [ ] Tomcat 10.1 正常启动两个 WAR
- [ ] PostgreSQL 旧库 hbm2ddl validate 通过
- [ ] 管理员账号可登录
- [ ] Study / Subject / CRF / 数据录入 / Audit / Export 流程可用
- [ ] 核心 Web Service 可调用
- [ ] 依赖冲突已排查
- [ ] 重大安全漏洞已记录
