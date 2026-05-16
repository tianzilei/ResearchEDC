# OpenClinica 技术栈现代化重构计划

**文档版本:** v1.0  
**生成日期:** 2026-05-17  
**适用对象:** OpenClinica 3.x / 3.18-SNAPSHOT 代码库现代化改造  
**建议策略:** 绞杀者模式（Strangler Fig Pattern）+ 模块化单体（Modular Monolith）+ 渐进式前后端分离  
**不建议策略:** 一次性全量重写、过早微服务化、直接在旧代码上盲目升级依赖版本  

---

## 1. 背景与目标

当前 OpenClinica 代码库属于典型的旧式 Java 企业应用。根据现有项目技术架构文档，当前系统仍使用 Java 7、Spring Framework 3.2、Spring Security 3.2、Hibernate 3.5、Servlet/JSP、Spring MVC、Spring WS/SOAP、Maven、Liquibase、Quartz、Logback、SLF4J、Jackson 和 Ehcache。项目结构主要由 `core`、`web` 和 `ws` 三个模块组成，其中 `core` 包含核心业务、DAO、Hibernate 实体、规则引擎、Quartz 任务和迁移脚本；`web` 包含 Servlet 控制器、Spring MVC 控制器和 JSP 页面；`ws` 包含 SOAP Web Service 端点。

本计划的目标不是简单“升级依赖”，而是将 OpenClinica 改造成更容易维护、部署、扩展和二次开发的现代临床数据采集系统。核心原则是保留 EDC/CDM 领域语义、审计链路、历史数据兼容性和关键业务规则，同时逐步替换旧技术栈。

### 1.1 主要目标

1. 将运行时从 Java 7 迁移到 Java 21，后续预留 Java 25 兼容路径。
2. 将 Spring 3.x 体系迁移到 Spring Boot 3.5.x 或后续 Spring Boot 4.x。
3. 将 Hibernate 3.5 迁移到 Hibernate ORM 6.x/7.x 或通过 Spring Data JPA 进行封装。
4. 将 Servlet/JSP 页面逐步替换为 React + TypeScript 前端。
5. 将 SOAP 接口逐步替换为 REST API + OpenAPI 文档。
6. 保留并扩展 Liquibase 数据库迁移体系。
7. 优先支持 PostgreSQL，除非确有企业部署需要，否则逐步降低 Oracle 兼容负担。
8. 建立统一权限、审计、电子签名、数据变更历史、导出任务和后台任务体系。
9. 建立自动化测试、回归验证和迁移验证机制。
10. 在 LGPL 许可要求下保留修改说明、许可证声明和衍生修改记录。

### 1.2 非目标

第一阶段不建议做以下事情：

1. 不建议一次性全量重写所有功能。
2. 不建议直接把系统拆成多个微服务。
3. 不建议立即重构全部数据库 schema。
4. 不建议一开始重写 CRF 数据录入、双录入、电子签名和规则引擎。
5. 不建议同时支持过多数据库、认证系统和部署形态。
6. 不建议在没有回归测试的情况下直接替换高风险业务模块。

---

## 2. 当前架构问题诊断

### 2.1 技术栈老化

| 层级 | 当前状态 | 主要问题 | 改造方向 |
|---|---|---|---|
| Java | Java 7 | 已严重过时，安全性、性能、语言特性不足 | Java 21 起步，预留 Java 25 |
| Spring | Spring 3.2 | 与现代安全、Jakarta、Boot 生态脱节 | Spring Boot 3.5.x / 4.x |
| Security | Spring Security 3.2 | 权限模型和安全配置落后 | Spring Security 6/7 |
| ORM | Hibernate 3.5 | JPA 能力不足，查询和缓存体系过旧 | Hibernate 6.x/7.x + Spring Data JPA |
| Web | Servlet/JSP | 前后端耦合，页面维护困难 | REST API + React |
| API | SOAP + 部分 REST | 接口风格旧，集成成本高 | REST + OpenAPI |
| 构建 | Maven 旧式多模块 | 依赖冲突、插件过旧 | Maven Wrapper / Gradle 可选 |
| 部署 | 传统部署 | 环境不一致，难以自动化 | Docker Compose / K8s 可选 |
| 配置 | filters/properties | 配置分散，环境耦合 | Spring Boot Config / env vars |
| 测试 | 可能不足 | 重构风险高 | Characterization tests + Testcontainers |

### 2.2 架构耦合问题

现有架构中，Servlet、DAO、Bean、Service、JSP、权限检查、页面跳转、事务边界和审计操作往往存在较强耦合。典型问题包括：

1. Servlet 同时承担请求解析、权限判断、业务调度、页面跳转。
2. DAO 直接暴露旧式查询和 Bean 对象，领域边界不清。
3. Bean 既承担 DTO 又承担部分领域数据结构，职责混杂。
4. JSP 与后端模型强耦合，前端无法独立演进。
5. SOAP、REST、页面控制器并存，但没有统一 API 契约。
6. 数据录入、规则引擎、审计、电子签名之间存在复杂隐式依赖。
7. 文件路径、配置项、部署配置和运行环境之间耦合较强。
8. 缺少足够的自动化回归测试，导致重构风险集中在数据录入和审计链路。

### 2.3 业务风险等级

| 模块 | 风险等级 | 原因 | 迁移顺序 |
|---|---:|---|---|
| 用户资料读取、菜单、系统配置 | 低 | 只读为主 | 优先 |
| Study 列表、Subject 列表、Event 查询 | 低-中 | 主要是查询 | 优先 |
| Study/Site/Subject 创建与编辑 | 中 | 涉及权限与审计 | 第二批 |
| 数据导出任务 | 中 | 涉及后台任务和文件 | 第二批 |
| CRF 元数据读取 | 中 | 结构复杂但写操作少 | 第二批 |
| CRF 数据录入 | 高 | 涉及数据完整性、校验、审计 | 后置 |
| 双录入 | 高 | 涉及一致性和冲突处理 | 后置 |
| 规则引擎 | 高 | 影响数据校验和自动计算 | 后置 |
| 电子签名 | 高 | 合规性要求高 | 后置 |
| 审计日志 | 极高 | 临床系统核心可信链路 | 优先建基础，后续贯穿 |

---

## 3. 总体重构策略

### 3.1 推荐策略：绞杀者模式

不要在原系统上直接“大爆炸式重写”。推荐策略是保留旧系统可运行，然后在旁边建立现代化模块，通过统一入口逐步接管旧功能。

基本模式如下：

```text
用户请求
  ↓
统一入口 / 网关 / Nginx
  ↓
根据路径路由：
  /legacy/**      → 旧 OpenClinica Web
  /api/v1/**      → 新 Spring Boot API
  /app/**         → 新 React 前端
  /ws/**          → 旧 SOAP，逐步废弃
```

早期阶段，旧系统仍然负责高风险功能，例如 CRF 数据录入和双录入。新系统先接管低风险查询和管理页面。等测试覆盖、审计链路和领域模型稳定后，再逐步替换高风险功能。

### 3.2 模块化单体优先

不建议一开始拆微服务。EDC/CDM 系统的复杂度主要来自数据一致性、审计、权限、规则、数据锁定和临床流程，而不是高并发流量。过早拆服务会增加以下复杂度：

1. 分布式事务。
2. 跨服务权限传播。
3. 审计链路跨服务拼接。
4. CRF 数据一致性验证困难。
5. 本地开发和测试成本提高。
6. 数据迁移和版本兼容更复杂。

因此建议先采用模块化单体。模块边界稳定后，再选择性拆分导出、文件、通知、任务调度等外围模块。

### 3.3 迁移优先级

优先级排序：

1. 可运行性和容器化。
2. 构建系统和依赖治理。
3. 只读 API。
4. 前端壳和导航。
5. 权限、用户、角色。
6. Study/Site/Subject/Event 管理。
7. CRF 元数据读取。
8. 审计日志和变更记录。
9. 数据导出和后台任务。
10. CRF 数据录入。
11. 双录入。
12. 规则引擎。
13. 电子签名。
14. SOAP 废弃。
15. 旧 JSP/Servlet 清理。

---

## 4. 目标技术栈

### 4.1 后端技术栈

| 类别 | 推荐选择 | 说明 |
|---|---|---|
| JDK | Java 21 | 稳定、生态兼容性好，适合作为第一阶段目标 |
| JDK 预留 | Java 25 | 最新 LTS，可作为后续升级目标 |
| 框架 | Spring Boot 3.5.x | 迁移阻力小于 Boot 4，适合作为第一阶段主线 |
| 后续框架 | Spring Boot 4.x | 等 Jakarta EE 11、Jackson 3 适配完成后再评估 |
| 安全 | Spring Security 6.x | 替代旧 Spring Security 3.2 |
| ORM | Spring Data JPA + Hibernate ORM 6.x | 先稳定迁移，后续再评估 Hibernate 7 |
| 数据库 | PostgreSQL 16+ | 优先支持开源部署 |
| 迁移 | Liquibase | 保留原迁移体系，不修改已发布迁移 |
| API 文档 | springdoc-openapi | 生成 OpenAPI 3 文档 |
| 后台任务 | Spring Scheduler / Quartz | 可保留 Quartz，也可逐步封装 |
| 映射 | MapStruct | Entity/DTO/API Model 映射 |
| 校验 | Jakarta Bean Validation | 替换散落式校验 |
| 测试 | JUnit 5、AssertJ、Mockito、Testcontainers | 单元、集成、数据库测试 |
| 日志 | SLF4J + Logback | 保留但规范化结构化日志 |
| 监控 | Spring Actuator + Micrometer | 健康检查、指标、追踪 |

### 4.2 前端技术栈

| 类别 | 推荐选择 | 说明 |
|---|---|---|
| 前端框架 | React | 生态成熟，适合后台系统 |
| 语言 | TypeScript | 强类型，适合复杂表单 |
| 构建 | Vite | 快速、轻量 |
| UI | Ant Design 或 shadcn/ui | AntD 更适合企业表格表单，shadcn 更灵活 |
| 请求 | TanStack Query | 缓存、请求状态、分页 |
| 表单 | React Hook Form | 复杂表单性能好 |
| 校验 | Zod | 与 TypeScript 配合好 |
| 路由 | React Router | 标准 SPA 路由 |
| 国际化 | i18next | 替换 JSP 资源绑定 |
| 测试 | Vitest + Testing Library + Playwright | 单元、组件、端到端 |
| 图表 | ECharts / Recharts | 数据概览和报表 |

### 4.3 部署技术栈

| 类别 | 推荐选择 | 说明 |
|---|---|---|
| 本地部署 | Docker Compose | 开发和测试统一环境 |
| 反向代理 | Nginx / Caddy | 路由 legacy、新前端、新 API |
| 数据库 | PostgreSQL | 主数据库 |
| 对象存储 | MinIO，可选 | CRF 附件、导出文件、审计附件 |
| 认证 | 内置认证起步，Keycloak 可选 | 先保证兼容旧权限，再考虑统一身份 |
| CI/CD | GitHub Actions / GitLab CI | 构建、测试、镜像 |
| 镜像仓库 | GHCR / Harbor | 内部部署可选 Harbor |

---

## 5. 目标架构设计

### 5.1 推荐目录结构

```text
openclinica-modern/
├── backend/
│   ├── pom.xml
│   ├── oc-bootstrap/
│   │   └── Spring Boot 启动入口
│   ├── oc-domain/
│   │   └── 领域模型、值对象、领域服务、领域事件
│   ├── oc-application/
│   │   └── 用例服务、命令、查询、事务边界
│   ├── oc-infrastructure/
│   │   └── JPA、文件、邮件、任务、外部系统适配
│   ├── oc-api-rest/
│   │   └── REST Controller、DTO、OpenAPI
│   ├── oc-security/
│   │   └── 用户、角色、权限、会话、安全注解
│   ├── oc-audit/
│   │   └── 审计、电子签名、数据变更历史
│   ├── oc-export/
│   │   └── ODM、CSV、Excel、后台导出任务
│   ├── oc-randomization/
│   │   └── 随机化计划、分层区组、盲法、紧急揭盲
│   ├── oc-rule-engine/
│   │   └── 规则表达式、校验、自动计算
│   ├── oc-legacy-adapter/
│   │   └── 旧 DAO/Bean/Servlet 兼容适配层
│   └── oc-migration/
│       └── Liquibase changelog、数据校验脚本
│
├── frontend/
│   └── web-app/
│       ├── src/
│       │   ├── app/
│       │   ├── pages/
│       │   ├── features/
│       │   ├── entities/
│       │   ├── shared/
│       │   └── i18n/
│       ├── package.json
│       └── vite.config.ts
│
├── deploy/
│   ├── docker-compose.yml
│   ├── docker-compose.legacy.yml
│   ├── Dockerfile.backend
│   ├── Dockerfile.frontend
│   ├── nginx/
│   └── postgres/
│
├── docs/
│   ├── ARCHITECTURE_MODERNIZATION.md
│   ├── MODIFICATIONS.md
│   ├── API_DESIGN.md
│   ├── DATABASE_MIGRATION.md
│   ├── VALIDATION_PLAN.md
│   ├── SECURITY_MODEL.md
│   ├── AUDIT_MODEL.md
│   └── RELEASE_PLAN.md
│
└── tools/
    ├── db-compare/
    ├── migration-check/
    ├── legacy-smoke-test/
    └── data-diff/
```

### 5.2 后端分层

```text
Controller 层
  ↓
Application Service 层
  ↓
Domain 层
  ↓
Repository Port
  ↓
Infrastructure Adapter
  ↓
Database / File / External System
```

各层职责：

| 层 | 职责 | 禁止事项 |
|---|---|---|
| API Controller | HTTP 请求、参数校验、返回 DTO | 不写业务逻辑 |
| Application Service | 用例编排、事务边界、权限调用、审计触发 | 不直接写 SQL |
| Domain | 领域对象、领域规则、状态变更 | 不依赖 Spring MVC/JPA |
| Infrastructure | JPA、文件、邮件、任务、旧系统适配 | 不反向污染领域层 |
| Security | 认证、授权、数据权限 | 不散落在 Controller 中 |
| Audit | 记录敏感操作和数据变更 | 不被业务模块绕过 |

### 5.3 领域模块划分

```text
Identity
- User
- Role
- Permission
- StudyUserRole
- Session
- PasswordPolicy

Study Management
- Study
- Site
- StudyConfiguration
- StudyStatus
- StudyEventDefinition

Subject Management
- Subject
- StudySubject
- Enrollment
- SubjectStatus
- SubjectIdentifier

Event Management
- StudyEvent
- EventStatus
- EventSchedule
- EventOccurrence

CRF Metadata
- CRF
- CRFVersion
- ItemGroup
- Item
- ItemOption
- ItemValidation
- CRFLayout

Data Capture
- EventCRF
- ItemData
- DataEntrySession
- DataEntryStatus
- DoubleDataEntry
- DataLock

Query Management
- DiscrepancyNote
- Query
- QueryStatus
- QueryThread
- QueryResponse

Rules Engine
- RuleSet
- Rule
- Expression
- RuleTarget
- RuleAction
- ValidationResult

Audit & Signature
- AuditLog
- AuditEvent
- DataChangeHistory
- ElectronicSignature
- SignatureReason

Export
- Dataset
- ExportJob
- ODMExport
- CSVExport
- ExportFile

Administration
- SystemConfig
- CodeList
- Localization
- NotificationTemplate

Randomization
- RandomizationPlan
- RandomizationArm
- StratificationFactor
- RandomizationStratum
- AllocationList
- AllocationSlot
- RandomizationAssignment
- EmergencyUnblinding
```

---

## 6. 数据库迁移策略

### 6.1 总原则

1. 保留原数据库可读性。
2. 不修改已发布的 Liquibase migration。
3. 新增 migration 必须向前兼容。
4. 所有结构变更必须有 rollback 或补救脚本。
5. 所有关键表变更必须有数据校验 SQL。
6. 临床数据和审计数据不允许无记录修改。
7. 第一阶段避免重建核心表，只做兼容层和新增表。

### 6.2 数据库策略分阶段

#### 阶段 A：兼容原 schema

保留原表结构，新系统通过 JPA 或 SQL adapter 读取旧表。此阶段主要目标是读功能和低风险写功能。

```text
新 API → Repository Port → Legacy Repository Adapter → 旧 OpenClinica 表
```

#### 阶段 B：建立 Read Model

针对前端列表、分页、搜索、统计建立只读视图或物化视图。

示例：

```sql
CREATE VIEW v_study_subject_summary AS
SELECT
    ss.study_subject_id,
    ss.label,
    s.name AS study_name,
    ss.status_id,
    ss.created_date,
    ss.updated_date
FROM study_subject ss
JOIN study s ON ss.study_id = s.study_id;
```

#### 阶段 C：新增现代化表

对于新功能，例如 API token、导出任务、审计扩展、前端偏好设置，可以新增 `ocm_` 前缀表。

示例：

```text
ocm_export_job
ocm_audit_event
ocm_file_object
ocm_user_preference
ocm_api_token
ocm_migration_marker
```

#### 阶段 D：核心表重构

只有在 API、测试、审计、数据校验成熟后，才考虑核心表重构。核心表包括：

```text
study
study_subject
study_event
crf
crf_version
item
item_data
event_crf
audit_log_event
```

### 6.3 数据校验机制

每个 migration 必须配套：

1. 前置检查 SQL。
2. 后置检查 SQL。
3. 行数对比。
4. 外键完整性检查。
5. 关键状态枚举检查。
6. 审计记录完整性检查。
7. 空值和非法值检查。
8. 导出结果抽样对比。

建议目录：

```text
backend/oc-migration/
├── src/main/resources/db/changelog/
├── src/main/resources/db/checks/pre/
├── src/main/resources/db/checks/post/
└── src/main/resources/db/rollback/
```

### 6.4 PostgreSQL 优化方向

1. 统一使用 `timestamptz` 处理时间。
2. 所有业务主键保留旧 ID，新增系统可考虑 UUID。
3. 对分页查询建立组合索引。
4. 对审计日志按时间和实体类型索引。
5. 对导出任务建立状态索引。
6. 对 Subject、Study、Event 常用过滤条件建立索引。
7. 避免在第一阶段引入复杂分区，除非审计日志规模很大。
8. 对大文本、JSON 配置可评估 `jsonb`，但不要替代强约束临床数据表。

---

## 7. API 设计计划

### 7.1 API 风格

采用 REST + JSON + OpenAPI 3。

基础路径：

```text
/api/v1/auth
/api/v1/users
/api/v1/studies
/api/v1/sites
/api/v1/subjects
/api/v1/events
/api/v1/crfs
/api/v1/data-entry
/api/v1/queries
/api/v1/rules
/api/v1/exports
/api/v1/audit
/api/v1/admin
```

### 7.2 API 响应规范

统一响应结构：

```json
{
  "data": {},
  "meta": {
    "requestId": "string",
    "timestamp": "2026-05-17T12:00:00Z"
  },
  "errors": []
}
```

分页结构：

```json
{
  "data": [],
  "page": {
    "number": 1,
    "size": 20,
    "totalElements": 1024,
    "totalPages": 52
  }
}
```

错误结构：

```json
{
  "code": "SUBJECT_NOT_FOUND",
  "message": "Study subject was not found.",
  "field": "subjectId",
  "details": {}
}
```

### 7.3 第一批 API

第一批只做低风险读功能：

```text
GET /api/v1/me
GET /api/v1/studies
GET /api/v1/studies/{studyId}
GET /api/v1/studies/{studyId}/sites
GET /api/v1/studies/{studyId}/subjects
GET /api/v1/subjects/{subjectId}
GET /api/v1/subjects/{subjectId}/events
GET /api/v1/events/{eventId}
GET /api/v1/crfs
GET /api/v1/crfs/{crfId}
GET /api/v1/crfs/{crfId}/versions
GET /api/v1/audit/entities/{entityType}/{entityId}
```

### 7.4 第二批 API

第二批增加中风险写功能：

```text
POST /api/v1/studies
PATCH /api/v1/studies/{studyId}
POST /api/v1/sites
PATCH /api/v1/sites/{siteId}
POST /api/v1/subjects
PATCH /api/v1/subjects/{subjectId}
POST /api/v1/events
PATCH /api/v1/events/{eventId}
POST /api/v1/exports
GET /api/v1/exports/{jobId}
GET /api/v1/exports/{jobId}/download
```

### 7.5 第三批 API

第三批处理高风险功能：

```text
GET /api/v1/data-entry/sessions/{sessionId}
POST /api/v1/data-entry/sessions
PATCH /api/v1/data-entry/sessions/{sessionId}/items/{itemId}
POST /api/v1/data-entry/sessions/{sessionId}/validate
POST /api/v1/data-entry/sessions/{sessionId}/complete
POST /api/v1/data-entry/sessions/{sessionId}/sign
POST /api/v1/queries
PATCH /api/v1/queries/{queryId}
POST /api/v1/rules/evaluate
```

### 7.6 API 兼容原则

1. 所有 API 都必须有 OpenAPI 文档。
2. 所有写操作必须生成审计记录。
3. 所有敏感写操作必须检查 Study/Site 级别权限。
4. 所有状态变更必须显式建模，不允许直接更新状态字段。
5. 所有导出接口必须支持异步任务。
6. 所有删除操作优先使用软删除或状态变更。
7. API 不直接暴露旧 Bean 对象。
8. DTO 与领域模型必须分离。

---

## 8. 权限与安全重构

### 8.1 权限模型

建议从传统页面权限升级为多维权限模型：

```text
系统权限：系统管理员、用户管理、配置管理
研究权限：Study 级别访问、设计、数据查看、数据导出
站点权限：Site 级别数据访问
受试者权限：Subject 查看、创建、更新
CRF 权限：数据录入、二次录入、查看、锁定
查询权限：创建 query、回复 query、关闭 query
审计权限：查看审计日志
签名权限：电子签名、撤销签名
```

### 8.2 权限判断位置

不建议在 Controller 中散落权限判断。建议使用：

1. Spring Security method security。
2. 自定义权限服务。
3. 数据权限过滤器。
4. 审计切面。
5. 状态机校验。

示例：

```java
@PreAuthorize("@permissionService.canViewStudy(authentication, #studyId)")
public StudyDetailDto getStudy(Long studyId) {
    ...
}
```

### 8.3 认证策略

第一阶段可保留内置账号体系，逐步迁移为现代认证结构：

```text
Phase 1: 旧用户表 + Spring Security Adapter
Phase 2: 新 Session / Token 表
Phase 3: 可选接入 Keycloak / OIDC
Phase 4: 支持 SSO、MFA、API Token
```

### 8.4 密码与会话

应实现：

1. BCrypt/Argon2 密码哈希。
2. 密码复杂度策略。
3. 登录失败锁定。
4. 会话超时。
5. Remember-me 禁用或严格控制。
6. CSRF 防护。
7. XSS 防护。
8. 安全响应头。
9. 审计登录、登出、失败登录。
10. 管理员重置密码审计。

---

## 9. 审计与电子签名重构

### 9.1 审计原则

临床数据系统中，审计不是附属功能，而是核心可信链路。所有敏感操作必须记录：

```text
who: 操作者
when: 操作时间
where: IP、User-Agent、会话
what: 操作对象
action: 创建、修改、删除、签名、锁定、导出
before: 修改前值
after: 修改后值
reason: 修改原因
study/site: 数据归属
signature: 是否涉及电子签名
requestId: 请求链路 ID
```

### 9.2 审计实现方式

建议采用组合方式：

1. 应用层显式审计：关键业务状态变更。
2. JPA Entity Listener：普通字段变更。
3. 数据库触发器：高风险表补充兜底。
4. 请求链路 ID：便于追踪。
5. 审计事件表：统一检索和导出。

### 9.3 审计数据模型

建议新增现代化审计表，不破坏旧表：

```text
ocm_audit_event
- id
- request_id
- actor_user_id
- actor_username
- action
- entity_type
- entity_id
- study_id
- site_id
- before_json
- after_json
- reason
- ip_address
- user_agent
- created_at

ocm_electronic_signature
- id
- entity_type
- entity_id
- signer_user_id
- signature_meaning
- signature_reason
- signed_at
- request_id
- status
```

### 9.4 电子签名

电子签名功能迁移必须后置，并需要单独测试：

1. 签名前二次认证。
2. 签名含义明确，例如确认数据完整、确认审核完成。
3. 签名后数据锁定。
4. 修改签名后数据必须触发签名失效或重新签名。
5. 签名记录不可删除。
6. 签名导出必须可追溯。
7. 签名必须进入审计日志。

---

## 10. 前端重构计划

### 10.1 前端总体策略

JSP 不应一次性替换。建议建立新 React 前端壳，逐页迁移。

初期路由：

```text
/app/login
/app/dashboard
/app/studies
/app/studies/:studyId
/app/studies/:studyId/subjects
/app/subjects/:subjectId
/app/subjects/:subjectId/events
/app/crfs
/app/exports
/app/audit
/app/admin
```

旧页面继续通过：

```text
/legacy/*
```

访问。

### 10.2 前端目录设计

```text
frontend/web-app/src/
├── app/
│   ├── App.tsx
│   ├── router.tsx
│   ├── providers.tsx
│   └── layout/
├── pages/
│   ├── dashboard/
│   ├── studies/
│   ├── subjects/
│   ├── events/
│   ├── crfs/
│   ├── data-entry/
│   ├── exports/
│   ├── audit/
│   └── admin/
├── features/
│   ├── auth/
│   ├── study-management/
│   ├── subject-management/
│   ├── crf-metadata/
│   ├── data-capture/
│   ├── query-management/
│   └── export-jobs/
├── entities/
│   ├── study/
│   ├── subject/
│   ├── event/
│   ├── crf/
│   └── user/
├── shared/
│   ├── api/
│   ├── ui/
│   ├── config/
│   ├── i18n/
│   ├── hooks/
│   └── utils/
└── test/
```

### 10.3 UI 迁移顺序

| 顺序 | 页面 | 风险 | 说明 |
|---:|---|---:|---|
| 1 | 登录后 Dashboard | 低 | 前端壳验证 |
| 2 | Study 列表 | 低 | 只读 |
| 3 | Subject 列表 | 低 | 分页、搜索 |
| 4 | Event 列表 | 低-中 | 状态展示 |
| 5 | CRF 元数据查看 | 中 | 结构复杂 |
| 6 | Audit 查看 | 中 | 权限敏感 |
| 7 | Export 任务 | 中 | 异步任务 |
| 8 | Study/Site/Subject 编辑 | 中 | 写操作 |
| 9 | Query 管理 | 中-高 | 流程状态 |
| 10 | CRF 数据录入 | 高 | 最后迁移 |
| 11 | 双录入 | 高 | 需要完整回归 |
| 12 | 电子签名 | 极高 | 需要单独验证 |

### 10.4 表单策略

CRF 和临床数据录入是最复杂部分，建议分层：

```text
CRF Metadata Renderer
  ↓
Form Schema Adapter
  ↓
React Form Runtime
  ↓
Validation Engine
  ↓
Data Entry API
  ↓
Audit + Rule Engine
```

不要直接在 React 中硬编码所有 CRF 结构。应从后端返回表单 schema，由前端动态渲染。

---

## 11. CRF 与数据录入迁移计划

### 11.1 迁移难点

CRF 数据录入涉及：

1. CRF Version。
2. ItemGroup。
3. Item。
4. ItemOption。
5. ItemData。
6. 数据类型校验。
7. 逻辑跳转。
8. 必填校验。
9. 范围校验。
10. 规则引擎。
11. Query/Discrepancy Note。
12. 保存草稿。
13. 提交完成。
14. 双录入。
15. 电子签名。
16. 数据锁定。
17. 审计日志。

因此不能早期重写。

### 11.2 分阶段迁移

#### 阶段 1：CRF 元数据只读 API

目标：

```text
GET /api/v1/crfs
GET /api/v1/crfs/{crfId}
GET /api/v1/crfs/{crfId}/versions
GET /api/v1/crf-versions/{versionId}/schema
```

输出统一 schema：

```json
{
  "crfId": 1,
  "versionId": 2,
  "name": "Baseline CRF",
  "sections": [
    {
      "id": "section-1",
      "label": "Demographics",
      "items": [
        {
          "id": "age",
          "type": "integer",
          "label": "Age",
          "required": true,
          "constraints": {
            "min": 18,
            "max": 80
          }
        }
      ]
    }
  ]
}
```

#### 阶段 2：CRF 预览

前端可以渲染 CRF，但不能保存数据。

#### 阶段 3：草稿保存

仅允许保存未完成状态，并进行完整审计。

#### 阶段 4：完整数据录入

支持校验、提交、状态变更。

#### 阶段 5：双录入

支持第二录入者、差异比较、冲突解决。

#### 阶段 6：电子签名和锁定

支持签名、锁定、签名失效、重新签名。

### 11.3 数据录入状态机

建议显式建模状态：

```text
NOT_STARTED
IN_PROGRESS
INITIAL_COMPLETE
DOUBLE_ENTRY_REQUIRED
DOUBLE_ENTRY_IN_PROGRESS
DOUBLE_ENTRY_COMPLETE
QUERIES_OPEN
READY_FOR_REVIEW
SIGNED
LOCKED
```

所有状态变更必须通过状态机，不允许直接更新状态字段。

---

## 12. 规则引擎迁移计划

### 12.1 当前问题

旧规则引擎位于 `logic/rule/`，通常与旧 Bean、DAO、表达式服务和数据录入流程耦合较深。重构时应避免一开始替换规则表达式语义，否则可能导致历史规则执行结果变化。

### 12.2 迁移策略

1. 第一阶段保留旧规则引擎。
2. 为旧规则引擎包一层 adapter。
3. 建立规则执行输入/输出标准结构。
4. 对历史规则建立 golden master 测试。
5. 逐步替换表达式解析和执行器。
6. 保留旧规则兼容模式。
7. 新规则引擎支持版本化。

### 12.3 规则执行模型

```text
RuleEvaluationRequest
- studyId
- subjectId
- eventId
- crfVersionId
- itemData
- context

RuleEvaluationResult
- errors
- warnings
- calculatedValues
- generatedQueries
- hiddenItems
- requiredItems
```

### 12.4 规则测试

必须建立规则回归样本：

```text
rules-test-cases/
├── required-field/
├── range-check/
├── cross-field-validation/
├── calculated-value/
├── query-generation/
└── skip-logic/
```

每个样本包括：

```text
input.json
expected-output.json
legacy-output.json
```

---

## 13. 数据导出与 ODM 迁移计划

### 13.1 导出原则

数据导出是临床系统核心功能，应支持：

1. ODM XML。
2. CSV。
3. Excel。
4. 按 Study/Site/Subject/Event/CRF 过滤。
5. 异步任务。
6. 导出审计。
7. 导出文件有效期。
8. 导出权限控制。

### 13.2 导出任务模型

```text
POST /api/v1/exports
GET /api/v1/exports/{jobId}
GET /api/v1/exports/{jobId}/download
DELETE /api/v1/exports/{jobId}
```

任务状态：

```text
PENDING
RUNNING
COMPLETED
FAILED
CANCELLED
EXPIRED
```

### 13.3 导出一致性测试

每个导出格式都应有：

1. 旧系统导出结果。
2. 新系统导出结果。
3. 字段级 diff。
4. 行数 diff。
5. 编码校验。
6. 日期格式校验。
7. ODM schema 校验。
8. 脱敏策略校验。

---

## 14. SOAP 接口处理策略

### 14.1 不建议立即删除 SOAP

旧系统中 `ws` 模块提供 Study、StudySubject、Event 等 SOAP 端点。考虑到外部系统可能依赖 SOAP，第一阶段不应直接移除。

### 14.2 推荐路径

```text
Phase 1: 保留 SOAP
Phase 2: 为同等功能提供 REST API
Phase 3: 编写 SOAP-to-REST 对照表
Phase 4: 标记 SOAP deprecated
Phase 5: 统计 SOAP 调用量
Phase 6: 提供迁移指南
Phase 7: 在主版本升级时移除或单独打包
```

### 14.3 SOAP-to-REST 映射文档

建议新增：

```text
docs/SOAP_DEPRECATION_PLAN.md
```

内容包括：

```text
旧 SOAP Endpoint
对应 REST Endpoint
请求字段映射
响应字段映射
权限差异
错误码差异
迁移示例
废弃日期
```

---

## 15. 构建与依赖治理

### 15.1 Maven 现代化

建议保留 Maven，但引入 Maven Wrapper：

```bash
./mvnw clean verify
```

父 POM 管理：

```xml
<dependencyManagement>
    <!-- Spring Boot BOM -->
    <!-- Testcontainers BOM -->
    <!-- Jackson BOM, if needed -->
</dependencyManagement>
```

### 15.2 构建阶段

```text
validate
compile
test
integration-test
verify
package
docker-build
```

### 15.3 依赖治理规则

1. 不直接引入无人维护依赖。
2. 所有依赖必须通过 BOM 或版本集中管理。
3. 安全漏洞依赖必须自动扫描。
4. 禁止业务模块随意引入 Web 层依赖。
5. 禁止领域模块依赖 infrastructure。
6. 对 LGPL、Apache、MIT、GPL 依赖进行许可证检查。
7. 对旧依赖保留 `legacy` profile，不污染新模块。

### 15.4 推荐 CI 流程

```yaml
stages:
  - lint
  - build
  - unit-test
  - integration-test
  - frontend-test
  - e2e-test
  - dependency-scan
  - docker-build
  - migration-check
```

---

## 16. 部署与运行环境

### 16.1 本地开发环境

推荐：

```text
Docker Compose
- postgres
- backend
- frontend
- legacy-openclinica
- nginx
- minio optional
- mailhog optional
```

示例服务：

```yaml
services:
  postgres:
    image: postgres:16
  backend:
    build: ./backend
  frontend:
    build: ./frontend/web-app
  nginx:
    image: nginx:alpine
  minio:
    image: minio/minio
```

### 16.2 路由策略

```text
/app/*       → React 前端
/api/v1/*    → 新 Spring Boot API
/legacy/*    → 旧 OpenClinica Web
/ws/*        → 旧 SOAP
```

### 16.3 配置策略

采用环境变量和 Spring profile：

```text
SPRING_PROFILES_ACTIVE=dev
DB_HOST=postgres
DB_PORT=5432
DB_NAME=openclinica
DB_USERNAME=clinica
DB_PASSWORD=...
FILE_STORAGE_ROOT=/data/openclinica
AUDIT_ENABLED=true
```

### 16.4 健康检查

必须提供：

```text
GET /actuator/health
GET /actuator/health/readiness
GET /actuator/health/liveness
GET /actuator/metrics
```

---

## 17. 测试与验证计划

### 17.1 测试金字塔

```text
E2E 测试：少量关键流程
API 集成测试：主要业务接口
Service 测试：用例逻辑
Domain 测试：领域规则
Repository 测试：数据库访问
Migration 测试：数据库迁移
Golden Master 测试：旧系统行为对照
```

### 17.2 Characterization Tests

在重构旧代码前，先给旧行为建立 characterization tests。重点覆盖：

1. Study 创建和编辑。
2. Subject 创建和状态变更。
3. Event 创建和状态变更。
4. CRF 元数据读取。
5. 数据录入保存。
6. 数据录入完成。
7. 双录入差异处理。
8. 规则引擎执行。
9. Query 创建和关闭。
10. ODM 导出。
11. 审计记录生成。
12. 电子签名。

### 17.3 Golden Master 测试

对于旧系统复杂输出，例如 ODM 导出、规则执行、CRF schema，应采用 golden master 测试。

目录：

```text
test-fixtures/
├── studies/
├── subjects/
├── crfs/
├── item-data/
├── rules/
├── exports/
└── audit/
```

### 17.4 集成测试

使用 Testcontainers 启动 PostgreSQL：

```text
Repository tests
Migration tests
API tests
Export tests
Audit tests
```

### 17.5 前端测试

```text
Vitest: 工具函数和组件逻辑
Testing Library: 表单组件
Playwright: 登录、Study 列表、Subject 查看、导出任务
```

### 17.6 验收标准

每个被替换模块必须满足：

1. 新旧系统功能对照完成。
2. 数据库写入结果一致。
3. 审计日志完整。
4. 权限行为一致或更严格。
5. 导出结果一致或差异有说明。
6. E2E 测试通过。
7. 回滚路径明确。
8. 文档更新完成。

---

## 18. 合规与质量控制

### 18.1 临床系统特别要求

EDC/CDM 系统不同于普通后台管理系统。以下要求必须贯穿设计：

1. 数据可追溯。
2. 修改有原因。
3. 关键操作有审计。
4. 签名不可伪造。
5. 导出可重复。
6. 权限最小化。
7. 数据锁定后不能随意修改。
8. 查询关闭过程可追踪。
9. 历史 CRF 版本可重现。
10. 用户行为可回溯。

### 18.2 验证文档

建议建立以下文档：

```text
docs/VALIDATION_PLAN.md
docs/REQUIREMENTS_TRACEABILITY_MATRIX.md
docs/TEST_PLAN.md
docs/TEST_SUMMARY_REPORT.md
docs/CHANGE_CONTROL.md
docs/RISK_ASSESSMENT.md
docs/DEPLOYMENT_QUALIFICATION.md
```

### 18.3 变更控制

每个高风险变更需要记录：

```text
变更编号
变更原因
影响模块
风险等级
测试范围
回滚方案
审批人
实施日期
验证结果
```

---

## 19. 详细阶段计划

### Phase 0：盘点与基线建立

**目标:** 明确现有系统行为，建立可运行基线。

**任务:**

1. 固定当前 Git commit。
2. 记录当前构建方式。
3. 建立本地可运行环境。
4. 导出当前数据库结构。
5. 记录默认配置。
6. 统计 Java 文件、JSP、Servlet、DAO、Service、SOAP endpoint。
7. 识别核心业务流程。
8. 建立 smoke test。
9. 新增 `MODIFICATIONS.md`。
10. 新增 `ARCHITECTURE_MODERNIZATION.md`。

**产出:**

```text
docs/CURRENT_STATE_ASSESSMENT.md
docs/MODIFICATIONS.md
docs/ARCHITECTURE_MODERNIZATION.md
tools/legacy-smoke-test/
```

**验收标准:**

1. 旧系统可在开发环境启动。
2. 可完成登录。
3. 可访问 Study 列表。
4. 可访问 Subject 页面。
5. 可执行一次数据库 migration。
6. 可生成当前依赖清单。

---

### Phase 1：容器化与配置外部化

**目标:** 让旧系统和新系统未来能在同一开发环境中运行。

**任务:**

1. 编写 `docker-compose.yml`。
2. 增加 PostgreSQL 服务。
3. 配置 Nginx 路由。
4. 外部化数据库配置。
5. 外部化文件路径配置。
6. 增加健康检查脚本。
7. 增加本地初始化脚本。
8. 增加 `.env.example`。
9. 增加开发文档。

**产出:**

```text
deploy/docker-compose.yml
deploy/nginx/default.conf
.env.example
docs/LOCAL_DEVELOPMENT.md
```

**验收标准:**

1. 一条命令启动开发环境。
2. 数据库可初始化。
3. 旧系统页面可访问。
4. 新前端壳可访问。
5. 新 API 健康检查可访问。

---

### Phase 2：新后端骨架

**目标:** 建立 Spring Boot 新后端，不替换旧业务。

**任务:**

1. 建立 `backend` 多模块项目。
2. 增加 Spring Boot 启动模块。
3. 增加 `/actuator/health`。
4. 增加统一错误处理。
5. 增加 OpenAPI。
6. 增加日志 requestId。
7. 增加基本安全配置。
8. 增加数据库连接。
9. 增加 Testcontainers。
10. 增加 CI 构建。

**产出:**

```text
backend/oc-bootstrap
backend/oc-api-rest
backend/oc-domain
backend/oc-application
backend/oc-infrastructure
```

**验收标准:**

1. 新后端可启动。
2. OpenAPI 可访问。
3. 数据库连接成功。
4. 单元测试和集成测试通过。
5. 镜像可构建。

---

### Phase 3：Legacy Adapter 与只读 API

**目标:** 新系统通过适配层读取旧数据库。

**任务:**

1. 建立 Legacy Repository Adapter。
2. 映射 Study。
3. 映射 Site。
4. 映射 Subject。
5. 映射 Event。
6. 映射 CRF metadata。
7. 增加只读 API。
8. 增加分页和搜索。
9. 增加权限初步过滤。
10. 增加 API 测试。

**第一批 API:**

```text
GET /api/v1/me
GET /api/v1/studies
GET /api/v1/studies/{id}
GET /api/v1/studies/{id}/subjects
GET /api/v1/subjects/{id}
GET /api/v1/subjects/{id}/events
GET /api/v1/crfs
GET /api/v1/crfs/{id}/versions
```

**验收标准:**

1. API 返回数据与旧页面一致。
2. 分页总数一致。
3. 权限过滤不弱于旧系统。
4. OpenAPI 文档完整。
5. 只读 API 不修改任何业务表。

---

### Phase 4：React 前端壳和低风险页面迁移

**目标:** 建立新 UI 框架，迁移只读页面。

**任务:**

1. 建立 React + TypeScript + Vite 项目。
2. 增加登录态处理。
3. 增加布局和导航。
4. 增加 Study 列表。
5. 增加 Study 详情。
6. 增加 Subject 列表。
7. 增加 Subject 详情。
8. 增加 Event 列表。
9. 增加 CRF metadata 查看。
10. 增加 Playwright 测试。

**验收标准:**

1. 新前端能通过 Nginx 访问。
2. Study/Subject/Event 数据与旧系统一致。
3. 页面权限处理正确。
4. 刷新页面路由正常。
5. E2E 登录和列表访问通过。

---

### Phase 5：权限、用户和审计基础设施

**目标:** 建立统一安全与审计基础。

**任务:**

1. 建立用户认证 adapter。
2. 建立角色权限模型。
3. 建立 Study/Site 数据权限服务。
4. 建立审计事件表。
5. 建立审计服务。
6. 建立请求链路 ID。
7. 建立登录审计。
8. 建立敏感查询审计。
9. 建立审计查询 API。
10. 建立审计前端页面。

**验收标准:**

1. 登录、失败登录、退出均记录审计。
2. Study/Site 权限过滤正确。
3. 审计记录不可通过普通 API 删除。
4. 审计页面仅授权用户可访问。
5. 审计记录包含 requestId、用户、时间、动作、实体。

---

### Phase 6：中风险写功能迁移

**目标:** 迁移 Study/Site/Subject/Event 的基础写操作。

**任务:**

1. Study 创建和编辑。
2. Site 创建和编辑。
3. Subject 创建和编辑。
4. Event 创建和状态更新。
5. 所有写操作接入审计。
6. 所有写操作接入权限。
7. 所有写操作接入 Bean Validation。
8. 编写新旧行为对照测试。
9. 编写前端表单。
10. 编写回滚策略。

**验收标准:**

1. 数据库写入结果与旧系统一致或差异有文档说明。
2. 审计记录完整。
3. 权限不弱于旧系统。
4. 前端表单校验和后端校验一致。
5. 回归测试通过。

---

### Phase 7：导出与后台任务迁移

**目标:** 建立现代导出任务体系。

**任务:**

1. 建立 ExportJob 表。
2. 建立异步任务执行器。
3. 实现 CSV 导出。
4. 实现 ODM 导出 adapter。
5. 实现导出任务状态 API。
6. 实现导出文件下载。
7. 实现导出审计。
8. 实现导出权限。
9. 实现导出结果 diff 测试。
10. 实现前端导出任务页面。

**验收标准:**

1. 导出结果与旧系统一致。
2. 导出任务可追踪。
3. 失败任务有错误信息。
4. 下载操作有审计。
5. 无权限用户不能下载导出文件。

---

### Phase 8：CRF 元数据和表单渲染

**目标:** 新前端能够稳定渲染 CRF。

**任务:**

1. 建立 CRF schema API。
2. 映射 Section、ItemGroup、Item、Option。
3. 建立前端动态表单渲染器。
4. 支持只读预览。
5. 支持基本字段类型。
6. 支持多语言 label。
7. 支持必填、范围、枚举校验展示。
8. 建立 CRF schema golden master 测试。
9. 建立样例 CRF 测试集。
10. 建立 UI 截图回归。

**验收标准:**

1. 同一 CRF 在新旧系统结构一致。
2. 字段顺序一致。
3. 字段类型一致。
4. 必填和选项展示一致。
5. 只读预览不修改数据。

---

### Phase 9：CRF 数据录入迁移

**目标:** 替换核心数据录入流程。

**前置条件:**

1. 审计基础设施完成。
2. 权限服务完成。
3. CRF schema 稳定。
4. 规则引擎 adapter 完成。
5. golden master 测试完成。
6. 数据录入状态机完成。

**任务:**

1. 建立 DataEntrySession。
2. 支持草稿保存。
3. 支持字段级校验。
4. 支持规则引擎调用。
5. 支持 ItemData 写入。
6. 支持 Query 触发。
7. 支持完成提交。
8. 支持状态变更。
9. 支持数据锁定检查。
10. 支持完整审计。
11. 编写 E2E 测试。
12. 小范围试运行。

**验收标准:**

1. 新旧数据录入结果一致。
2. 审计记录字段级完整。
3. 校验规则输出一致。
4. Query 生成一致。
5. 完成状态一致。
6. 锁定数据不可修改。
7. 异常中断不会产生不一致数据。

---

### Phase 10：双录入、Query 和电子签名

**目标:** 完成高风险临床流程迁移。

**任务:**

1. 双录入流程。
2. 双录入差异比较。
3. 差异解决。
4. Query 创建。
5. Query 回复。
6. Query 关闭。
7. 电子签名。
8. 签名后锁定。
9. 修改后签名失效。
10. 签名审计。
11. 完整合规验证。

**验收标准:**

1. 双录入差异结果准确。
2. Query 状态机正确。
3. 签名前必须二次认证。
4. 签名记录不可删除。
5. 签名后数据锁定。
6. 修改数据后签名处理逻辑正确。
7. 所有操作均有审计。

---

### Phase 11：SOAP 废弃和旧页面清理

**目标:** 降低旧代码依赖。

**任务:**

1. 统计 SOAP 使用情况。
2. 提供 REST 替代接口。
3. 编写 SOAP 迁移文档。
4. 标记 SOAP deprecated。
5. 下线已替换 JSP 页面。
6. 删除无用 Servlet。
7. 清理旧 DAO。
8. 清理旧配置。
9. 清理旧依赖。
10. 更新许可证和修改说明。

**验收标准:**

1. 所有被删除功能已有替代。
2. 外部调用方已迁移。
3. 回归测试通过。
4. 文档更新完成。
5. 构建依赖减少。

---

## 20. 里程碑路线图

以下周期只是工程规划参考，具体取决于团队规模、测试覆盖和历史数据复杂度。

| 里程碑 | 时间范围 | 目标 |
|---|---:|---|
| M0 | 第 1-2 周 | 盘点、基线、文档、旧系统可运行 |
| M1 | 第 3-5 周 | Docker Compose、配置外部化、新后端骨架 |
| M2 | 第 6-9 周 | 只读 API、OpenAPI、Legacy Adapter |
| M3 | 第 10-13 周 | React 前端壳、Study/Subject/Event 只读页面 |
| M4 | 第 14-18 周 | 权限、用户、审计基础 |
| M5 | 第 19-25 周 | Study/Site/Subject/Event 写功能 |
| M6 | 第 26-32 周 | 导出任务、ODM/CSV、后台任务 |
| M7 | 第 33-40 周 | CRF schema、动态表单预览 |
| M8 | 第 41-52 周 | CRF 数据录入迁移 |
| M9 | 第 53-64 周 | 双录入、Query、电子签名 |
| M10 | 第 65 周以后 | SOAP 废弃、旧页面清理、可选微服务拆分 |

---

## 21. 团队分工建议

### 21.1 最小团队

```text
1 名后端负责人
1 名前端负责人
1 名数据库/DevOps 负责人
1 名测试/验证负责人
1 名业务/临床数据管理顾问
```

### 21.2 职责划分

| 角色 | 职责 |
|---|---|
| 架构负责人 | 模块边界、技术栈、迁移策略、代码审查 |
| 后端工程师 | API、领域模型、JPA、权限、审计 |
| 前端工程师 | React、表单、页面迁移、前端测试 |
| 数据库工程师 | migration、索引、数据校验、性能 |
| 测试工程师 | 回归测试、E2E、golden master |
| 业务专家 | CRF、数据录入、规则、审计、导出验收 |
| DevOps | Docker、CI/CD、环境、监控 |

---

## 22. 风险清单与缓解措施

| 风险 | 等级 | 表现 | 缓解措施 |
|---|---:|---|---|
| 旧系统无法稳定构建 | 高 | 依赖过旧、环境不一致 | 先容器化和固定依赖 |
| 数据录入行为变化 | 极高 | 临床数据不一致 | golden master + 小范围迁移 |
| 审计缺失 | 极高 | 合规风险 | 审计优先，写操作统一接入 |
| 权限变弱 | 高 | 越权访问 | 权限测试和数据权限过滤 |
| 数据库迁移失败 | 高 | 历史数据不兼容 | pre/post check + rollback |
| 前后端字段不一致 | 中 | 页面错误、保存失败 | OpenAPI + DTO 生成 |
| SOAP 外部依赖未知 | 中 | 外部系统中断 | 调用统计 + 迁移窗口 |
| 规则引擎结果不一致 | 高 | 校验和 query 变化 | 保留旧引擎 adapter |
| CRF 渲染不一致 | 高 | 数据录入错误 | schema diff + UI 回归 |
| 性能下降 | 中 | 列表和导出变慢 | 索引、分页、异步导出 |

---

## 23. 代码规范建议

### 23.1 后端规范

1. Controller 不写业务逻辑。
2. Application Service 是事务边界。
3. Domain 不依赖 Spring Web。
4. Repository interface 放在 application 或 domain 边界。
5. Infrastructure 实现 Repository。
6. DTO 与 Entity 分离。
7. 所有写操作返回明确结果。
8. 所有异常转换为统一错误码。
9. 所有敏感操作有审计。
10. 所有 public API 有测试。

### 23.2 前端规范

1. 页面组件不直接写请求逻辑。
2. API 调用集中在 `shared/api`。
3. 表单 schema 和校验集中管理。
4. 权限判断有统一 hook。
5. 表格分页统一组件。
6. 错误提示统一处理。
7. i18n 文案不硬编码。
8. 高风险表单必须有 E2E 测试。

### 23.3 Git 规范

分支：

```text
main
develop
feature/*
refactor/*
migration/*
hotfix/*
release/*
```

提交信息：

```text
feat(study): add read-only study API
refactor(legacy): introduce study repository adapter
test(export): add ODM golden master test
docs(license): update MODIFICATIONS.md
```

---

## 24. 文档计划

必须维护以下文档：

```text
docs/CURRENT_STATE_ASSESSMENT.md
docs/ARCHITECTURE_MODERNIZATION.md
docs/MODIFICATIONS.md
docs/API_DESIGN.md
docs/SECURITY_MODEL.md
docs/AUDIT_MODEL.md
docs/DATABASE_MIGRATION.md
docs/CRF_SCHEMA_DESIGN.md
docs/RULE_ENGINE_MIGRATION.md
docs/EXPORT_DESIGN.md
docs/SOAP_DEPRECATION_PLAN.md
docs/VALIDATION_PLAN.md
docs/TEST_PLAN.md
docs/RELEASE_PLAN.md
```

其中 `MODIFICATIONS.md` 应持续记录：

1. 修改日期。
2. 修改模块。
3. 修改原因。
4. 与原 OpenClinica 的差异。
5. 是否影响许可证声明。
6. 是否影响数据库结构。
7. 是否影响临床数据或审计数据。
8. 回滚方式。

---

## 25. 第一批可以立即执行的任务清单

### 25.1 第 1 周

```text
[ ] 固定当前 Git commit
[ ] 确认许可证文件和 NOTICE
[ ] 新增 docs/MODIFICATIONS.md
[ ] 新增 docs/ARCHITECTURE_MODERNIZATION.md
[ ] 统计 Servlet、JSP、DAO、Service、SOAP endpoint 数量
[ ] 导出当前数据库 schema
[ ] 建立本地启动说明
[ ] 记录当前构建失败点
[ ] 建立最小 smoke test
```

### 25.2 第 2 周

```text
[ ] 编写 Dockerfile.legacy
[ ] 编写 docker-compose.yml
[ ] 启动 PostgreSQL
[ ] 外部化数据库配置
[ ] 配置 Nginx 路由
[ ] 建立 /legacy 路由
[ ] 建立 /app 路由占位
[ ] 建立 /api/v1/health 占位
[ ] 增加 .env.example
```

### 25.3 第 3-4 周

```text
[ ] 新建 backend 多模块项目
[ ] 引入 Spring Boot
[ ] 引入 OpenAPI
[ ] 引入 Testcontainers
[ ] 建立统一错误处理
[ ] 建立 requestId filter
[ ] 建立数据库连接
[ ] 编写 Study 只读 API POC
[ ] 编写 StudyRepository legacy adapter
[ ] 编写 Study API 集成测试
```

---

## 26. 推荐决策记录 ADR

建议用 ADR 管理关键架构决策。

目录：

```text
docs/adr/
├── 0001-use-incremental-modernization.md
├── 0002-use-modular-monolith-before-microservices.md
├── 0003-use-java-21-as-baseline.md
├── 0004-use-spring-boot-for-new-backend.md
├── 0005-use-react-typescript-for-frontend.md
├── 0006-keep-liquibase-and-do-not-edit-old-changelogs.md
├── 0007-prioritize-postgresql.md
├── 0008-preserve-legacy-soap-during-transition.md
├── 0009-audit-first-write-operations.md
└── 0010-use-golden-master-tests-for-crf-and-export.md
```

ADR 模板：

```markdown
# ADR-0001: Decision title

## Status

Accepted

## Context

Describe the problem.

## Decision

Describe the decision.

## Consequences

Describe positive and negative consequences.

## Alternatives Considered

Describe alternatives.
```

---

## 27. 参考目标版本建议

由于 OpenClinica 旧代码跨度较大，建议不要一次性追最新版本。推荐使用“稳态版本 + 预留升级路径”。

### 27.1 第一阶段推荐

```text
Java: 21
Spring Boot: 3.5.x
Spring Security: 6.x
Hibernate ORM: 6.x
PostgreSQL: 16 或 17
React: 19.x
TypeScript: 5.x
Vite: 7.x
Node.js: 22 LTS 或 24 LTS
```

### 27.2 第二阶段评估

```text
Java: 25
Spring Boot: 4.x
Spring Security: 7.x
Hibernate ORM: 7.x
PostgreSQL: 18
```

### 27.3 为什么不第一天就上全部最新

1. 旧项目从 Java 7 跨越过大。
2. Spring Boot 4 涉及 Jakarta EE 11 和 Jackson 3 等变更。
3. Hibernate 7 与旧 Hibernate 3.5 差异巨大。
4. 先保证可运行、可测试、可迁移，比追最新版本更重要。
5. 临床系统重构的第一目标是稳定和可验证，而不是技术激进。

---

## 28. 最终目标形态

重构完成后，系统应具备以下形态：

```text
一个现代化 EDC/CDM 平台
- 后端：Spring Boot 模块化单体
- 前端：React + TypeScript
- 数据库：PostgreSQL
- API：REST + OpenAPI
- 权限：RBAC + Study/Site 数据权限
- 审计：统一审计事件
- 签名：可追溯电子签名
- 导出：异步 ODM/CSV/Excel
- 部署：Docker Compose 起步，可扩展到 Kubernetes
- 测试：单元、集成、E2E、golden master
- 文档：架构、API、迁移、验证、修改说明完整
```

---

## 29. 结论

OpenClinica 的现代化不应被理解为单纯“升级 Java 和 Spring”。真正重要的是把旧式 Servlet/JSP/DAO/SOAP 架构逐步迁移到清晰的领域模型、应用服务、REST API、现代前端、统一权限和审计体系上。

建议按以下路线执行：

```text
先稳住旧系统
再建立新后端骨架
先迁移只读 API
再迁移低风险前端页面
然后建立权限和审计
再迁移中风险写功能
最后迁移 CRF、规则引擎、双录入和电子签名
```

这样可以在不破坏历史数据和核心临床流程的前提下，逐步完成技术栈现代化，并为后续二次开发、临床试验定制、AI 辅助质控、自动化数据核查和现代部署提供稳定基础。
