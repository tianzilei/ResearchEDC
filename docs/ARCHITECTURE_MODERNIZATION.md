# OpenClinica 架构现代化设计

**文档版本:** v1.0  
**生成日期:** 2026-05-17  
**适用版本:** 3.18-SNAPSHOT → 现代化重构目标  
**文档状态:** 设计阶段  

---

## 1. 架构愿景

### 1.1 目标架构

将 OpenClinica 从旧式 Java EE (Servlet/JSP/SOAP) 架构迁移到现代化的分层领域驱动架构：

```text
┌─────────────────────────────────────────────────────────────┐
│                     前端层 (Frontend)                        │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  React + TypeScript + Vite                           │  │
│  │  - 单页应用 (SPA)                                     │  │
│  │  - 动态表单渲染                                       │  │
│  │  - 响应式布局                                         │  │
│  └───────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP/REST + JSON
┌───────────────────────────▼─────────────────────────────────┐
│                     API 网关层                              │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Nginx / Spring Cloud Gateway                        │  │
│  │  - 路由分发 (/app, /api/v1, /legacy)                 │  │
│  │  - 负载均衡                                           │  │
│  │  - SSL 终止                                           │  │
│  └───────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                     应用层 (Application)                    │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  Spring Boot 3.5.x                                    │  │
│  │  ├─ REST Controllers (API)                            │  │
│  │  ├─ Application Services (用例编排)                   │  │
│  │  ├─ Security (认证/授权)                              │  │
│  │  └─ Audit (审计拦截)                                  │  │
│  └───────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                     领域层 (Domain)                         │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │  │
│  │  │  Study      │  │  Subject    │  │  Event      │   │  │
│  │  │  Management │  │  Management │  │  Management │   │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘   │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │  │
│  │  │  CRF        │  │  Data       │  │  Rule       │   │  │
│  │  │  Metadata   │  │  Capture    │  │  Engine     │   │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘   │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │  │
│  │  │  Query      │  │  Export     │  │  Audit &    │   │  │
│  │  │  Management │  │  Jobs       │  │  Signature  │   │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘   │  │
│  └───────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────┘
                            │ Repository Port (接口)
┌───────────────────────────▼─────────────────────────────────┐
│                 基础设施层 (Infrastructure)                 │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │  │
│  │  │  JPA/       │  │  Legacy     │  │  File       │   │  │
│  │  │  Hibernate  │  │  Adapter    │  │  Storage    │   │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘   │  │
│  │  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐   │  │
│  │  │  Quartz/    │  │  Email/     │  │  External   │   │  │
│  │  │  Scheduler  │  │  Notification│  │  Services   │   │  │
│  │  └─────────────┘  └─────────────┘  └─────────────┘   │  │
│  └───────────────────────────────────────────────────────┘  │
└───────────────────────────┬─────────────────────────────────┘
                            │
┌───────────────────────────▼─────────────────────────────────┐
│                     数据层 (Data)                           │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  PostgreSQL 16+                                       │  │
│  │  - 原有 OpenClinica 表结构 (保留)                     │  │
│  │  - 新增 ocm_* 现代化表                                │  │
│  │  - Liquibase 迁移管理                                 │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 核心设计原则

| 原则 | 说明 |
|------|------|
| **领域驱动** | 按业务领域划分模块边界，清晰分离关注点 |
| **分层架构** | Controller → Application → Domain → Infrastructure |
| **依赖倒置** | 领域层不依赖基础设施，通过 Port/Adapter 模式解耦 |
| **绞杀者模式** | 新系统逐步替换旧系统，而非大爆炸重写 |
| **审计优先** | 所有敏感操作必须记录审计日志 |
| **向后兼容** | 数据库 schema 保持兼容，不破坏历史数据 |

---

## 2. 技术栈

### 2.1 后端技术栈

| 类别 | 技术选择 | 版本 | 说明 |
|------|----------|------|------|
| **JDK** | OpenJDK | 21 (LTS) | 第一阶段目标，预留升级至 25 |
| **框架** | Spring Boot | 3.5.x | 现代化 Spring 生态 |
| **Web** | Spring MVC | 6.x | REST API |
| **安全** | Spring Security | 6.x | 认证/授权/审计 |
| **数据访问** | Spring Data JPA | 3.x | ORM 封装 |
| **ORM** | Hibernate ORM | 6.x | JPA 实现 |
| **数据库** | PostgreSQL | 16+ | 主数据库，优先支持 |
| **迁移** | Liquibase | 4.x | 保留现有体系 |
| **API 文档** | springdoc-openapi | 2.x | OpenAPI 3 自动生成 |
| **后台任务** | Spring Scheduler | - | 替代 Quartz (可选保留) |
| **DTO 映射** | MapStruct | 1.5+ | Entity/DTO 转换 |
| **校验** | Jakarta Bean Validation | 3.x | 统一校验 |
| **日志** | SLF4J + Logback | - | 结构化日志 |
| **监控** | Spring Actuator + Micrometer | 3.x | 健康检查、指标 |
| **测试** | JUnit 5 + AssertJ + Mockito | 5.x | 单元/集成测试 |
| **DB 测试** | Testcontainers | 1.x | 数据库集成测试 |

### 2.2 前端技术栈

| 类别 | 技术选择 | 版本 | 说明 |
|------|----------|------|------|
| **框架** | React | 19.x | 组件化 UI |
| **语言** | TypeScript | 5.x | 类型安全 |
| **构建** | Vite | 7.x | 快速开发服务器 |
| **UI 组件** | Ant Design | 5.x | 企业级组件库 |
| **状态管理** | TanStack Query | 5.x | 服务端状态管理 |
| **表单** | React Hook Form | 7.x | 高性能表单 |
| **校验** | Zod | 3.x | Schema 校验 |
| **路由** | React Router | 6.x | SPA 路由 |
| **国际化** | i18next | 23.x | 多语言支持 |
| **测试** | Vitest + Testing Library | - | 单元/组件测试 |
| **E2E 测试** | Playwright | 1.x | 端到端测试 |
| **图表** | ECharts / Recharts | 5.x | 数据可视化 |

### 2.3 部署技术栈

| 类别 | 技术选择 | 说明 |
|------|----------|------|
| **容器化** | Docker + Docker Compose | 开发/测试环境 |
| **编排** | Kubernetes (可选) | 生产环境 |
| **反向代理** | Nginx | 路由分发 |
| **对象存储** | MinIO | 文件存储 (可选) |
| **CI/CD** | GitHub Actions / GitLab CI | 自动化构建 |
| **镜像仓库** | GHCR / Harbor | 容器镜像管理 |

---

## 3. 目录结构

### 3.1 推荐项目结构

```text
openclinica-modern/
├── backend/                          # 后端模块
│   ├── pom.xml
│   ├── oc-bootstrap/                 # Spring Boot 启动入口
│   │   └── src/main/java/org/akaza/openclinica/
│   │       └── Application.java
│   │
│   ├── oc-domain/                    # 领域层
│   │   └── src/main/java/org/akaza/openclinica/domain/
│   │       ├── study/                # Study 聚合
│   │       ├── subject/              # Subject 聚合
│   │       ├── event/                # Event 聚合
│   │       ├── crf/                  # CRF 聚合
│   │       ├── data/                 # 数据录入聚合
│   │       ├── rule/                 # 规则引擎
│   │       ├── audit/                # 审计领域
│   │       └── export/               # 导出领域
│   │
│   ├── oc-application/               # 应用层
│   │   └── src/main/java/org/akaza/openclinica/application/
│   │       ├── study/                # Study 用例
│   │       ├── subject/              # Subject 用例
│   │       ├── event/                # Event 用例
│   │       ├── dataentry/            # 数据录入用例
│   │       └── export/               # 导出用例
│   │
│   ├── oc-infrastructure/            # 基础设施层
│   │   └── src/main/java/org/akaza/openclinica/infrastructure/
│   │       ├── persistence/          # JPA 实现
│   │       ├── legacy/               # 旧系统适配器
│   │       ├── file/                 # 文件存储
│   │       ├── mail/                 # 邮件服务
│   │       └── job/                  # 任务调度
│   │
│   ├── oc-api-rest/                  # REST API 层
│   │   └── src/main/java/org/akaza/openclinica/api/
│   │       ├── controller/           # REST Controllers
│   │       ├── dto/                  # 请求/响应 DTO
│   │       ├── mapper/               # MapStruct 映射器
│   │       └── exception/            # 全局异常处理
│   │
│   ├── oc-security/                  # 安全模块
│   │   └── src/main/java/org/akaza/openclinica/security/
│   │       ├── authentication/       # 认证
│   │       ├── authorization/        # 授权
│   │       ├── audit/                # 审计服务
│   │       └── config/               # 安全配置
│   │
│   └── oc-migration/                 # 数据库迁移
│       └── src/main/resources/db/
│           ├── changelog/            # Liquibase 变更
│           ├── checks/               # 迁移检查脚本
│           └── rollback/             # 回滚脚本
│
├── frontend/                         # 前端模块
│   └── web-app/
│       ├── src/
│       │   ├── app/                  # 应用入口
│       │   │   ├── App.tsx
│       │   │   ├── router.tsx
│       │   │   └── providers.tsx
│       │   │
│       │   ├── pages/                # 页面组件
│       │   │   ├── dashboard/
│       │   │   ├── studies/
│       │   │   ├── subjects/
│       │   │   ├── events/
│       │   │   ├── crfs/
│       │   │   ├── data-entry/
│       │   │   ├── exports/
│       │   │   ├── audit/
│       │   │   └── admin/
│       │   │
│       │   ├── features/             # 功能模块
│       │   │   ├── auth/
│       │   │   ├── study-management/
│       │   │   ├── subject-management/
│       │   │   ├── crf-metadata/
│       │   │   ├── data-capture/
│       │   │   ├── query-management/
│       │   │   └── export-jobs/
│       │   │
│       │   ├── entities/             # 实体类型定义
│       │   │   ├── study/
│       │   │   ├── subject/
│       │   │   ├── event/
│       │   │   ├── crf/
│       │   │   └── user/
│       │   │
│       │   ├── shared/               # 共享资源
│       │   │   ├── api/              # API 客户端
│       │   │   ├── ui/               # UI 组件
│       │   │   ├── config/           # 配置
│       │   │   ├── i18n/             # 国际化
│       │   │   ├── hooks/            # 自定义 hooks
│       │   │   └── utils/            # 工具函数
│       │   │
│       │   └── test/                 # 测试
│       │
│       ├── package.json
│       ├── tsconfig.json
│       └── vite.config.ts
│
├── deploy/                           # 部署配置
│   ├── docker-compose.yml            # 开发环境
│   ├── docker-compose.prod.yml       # 生产环境
│   ├── docker-compose.legacy.yml     # 遗留系统
│   ├── Dockerfile.backend
│   ├── Dockerfile.frontend
│   ├── Dockerfile.legacy
│   └── nginx/
│       └── default.conf              # Nginx 路由配置
│
├── docs/                             # 项目文档
│   ├── ARCHITECTURE.md               # 架构设计
│   ├── MODIFICATIONS.md              # 修改记录
│   ├── API_DESIGN.md                 # API 设计
│   ├── SECURITY_MODEL.md             # 安全模型
│   ├── AUDIT_MODEL.md                # 审计模型
│   ├── DATABASE_MIGRATION.md         # 数据库迁移
│   ├── CRF_SCHEMA.md                 # CRF 架构
│   ├── VALIDATION_PLAN.md            # 验证计划
│   ├── RELEASE_PLAN.md               # 发布计划
│   └── adr/                          # 架构决策记录
│       └── 0001-use-incremental-modernization.md
│
└── tools/                            # 开发工具
    ├── db-compare/                   # 数据库对比
    ├── migration-check/              # 迁移检查
    ├── legacy-smoke-test/            # 遗留系统冒烟测试
    └── data-diff/                    # 数据差异分析
```

---

## 4. 分层架构

### 4.1 后端分层

```text
┌────────────────────────────────────────┐
│  API Controller 层                      │
│  - HTTP 请求处理                        │
│  - 参数校验                             │
│  - DTO 转换                             │
│  - 返回响应                             │
│  ❌ 禁止: 业务逻辑                      │
├────────────────────────────────────────┤
│  Application Service 层                 │
│  - 用例编排                             │
│  - 事务边界                             │
│  - 权限调用                             │
│  - 审计触发                             │
│  ❌ 禁止: 直接写 SQL                   │
├────────────────────────────────────────┤
│  Domain 层                              │
│  - 领域对象                             │
│  - 领域规则                             │
│  - 状态变更                             │
│  ❌ 禁止: 依赖 Spring Web/JPA          │
├────────────────────────────────────────┤
│  Repository Port (接口)                 │
│  - 领域定义的仓储接口                   │
├────────────────────────────────────────┤
│  Infrastructure 层                      │
│  - JPA 实现                             │
│  - 文件存储                             │
│  - 邮件服务                             │
│  - 旧系统适配器                         │
│  ❌ 禁止: 反向污染领域层                │
└────────────────────────────────────────┘
```

### 4.2 各层职责

| 层 | 职责 | 关键技术 | 禁止事项 |
|----|------|----------|----------|
| **Controller** | HTTP 请求处理、参数校验、返回 DTO | Spring MVC, `@RestController`, `@Valid` | 不写业务逻辑 |
| **Application** | 用例编排、事务边界、权限调用、审计触发 | `@Service`, `@Transactional` | 不直接写 SQL |
| **Domain** | 领域对象、领域规则、状态变更、领域事件 | Plain Java, 领域事件 | 不依赖 Spring MVC/JPA |
| **Repository Port** | 仓储接口定义 | Interface, Spring Data JPA | - |
| **Infrastructure** | JPA、文件、邮件、任务、旧系统适配 | `@Repository`, JPA | 不反向污染领域层 |

---

## 5. 领域模块划分

### 5.1 核心领域

```text
Identity (身份与访问)
├── User                          # 用户
├── Role                          # 角色
├── Permission                    # 权限
├── StudyUserRole                 # 研究用户角色
├── Session                       # 会话
└── PasswordPolicy                # 密码策略

Study Management (研究管理)
├── Study                         # 研究
├── Site                          # 站点
├── StudyConfiguration            # 研究配置
├── StudyStatus                   # 研究状态
└── StudyEventDefinition          # 研究事件定义

Subject Management (受试者管理)
├── Subject                       # 受试者
├── StudySubject                  # 研究受试者
├── Enrollment                    # 入组
├── SubjectStatus                 # 受试者状态
└── SubjectIdentifier             # 受试者标识

Event Management (事件管理)
├── StudyEvent                    # 研究事件
├── EventStatus                   # 事件状态
├── EventSchedule                 # 事件计划
└── EventOccurrence               # 事件发生

CRF Metadata (CRF 元数据)
├── CRF                           # 病例报告表
├── CRFVersion                    # CRF 版本
├── ItemGroup                     # 项目组
├── Item                          # 项目
├── ItemOption                    # 项目选项
├── ItemValidation                # 项目校验
└── CRFLayout                     # CRF 布局

Data Capture (数据采集)
├── EventCRF                      # 事件 CRF
├── ItemData                      # 项目数据
├── DataEntrySession              # 数据录入会话
├── DataEntryStatus               # 数据录入状态
├── DoubleDataEntry               # 双录入
└── DataLock                      # 数据锁定

Query Management (查询管理)
├── DiscrepancyNote               # 差异备注
├── Query                         # 查询
├── QueryStatus                   # 查询状态
├── QueryThread                   # 查询线程
└── QueryResponse                 # 查询响应

Rules Engine (规则引擎)
├── RuleSet                       # 规则集
├── Rule                          # 规则
├── Expression                    # 表达式
├── RuleTarget                    # 规则目标
├── RuleAction                    # 规则动作
└── ValidationResult              # 校验结果

Audit & Signature (审计与签名)
├── AuditLog                      # 审计日志
├── AuditEvent                    # 审计事件
├── DataChangeHistory             # 数据变更历史
├── ElectronicSignature           # 电子签名
└── SignatureReason               # 签名原因

Export (数据导出)
├── Dataset                       # 数据集
├── ExportJob                     # 导出任务
├── ODMExport                     # ODM 导出
├── CSVExport                     # CSV 导出
└── ExportFile                    # 导出文件

Administration (系统管理)
├── SystemConfig                  # 系统配置
├── CodeList                      # 代码列表
├── Localization                  # 本地化
└── NotificationTemplate          # 通知模板
```

---

## 6. API 设计

### 6.1 API 风格

- **协议:** REST + JSON
- **版本:** v1 (路径前缀 `/api/v1/`)
- **文档:** OpenAPI 3 (自动生成)

### 6.2 基础路径

```text
/api/v1/auth                     # 认证
/api/v1/users                    # 用户管理
/api/v1/studies                  # 研究管理
/api/v1/sites                    # 站点管理
/api/v1/subjects                 # 受试者管理
/api/v1/events                   # 事件管理
/api/v1/crfs                     # CRF 管理
/api/v1/data-entry               # 数据录入
/api/v1/queries                  # 查询管理
/api/v1/rules                    # 规则引擎
/api/v1/exports                  # 数据导出
/api/v1/audit                    # 审计日志
/api/v1/admin                    # 系统管理
```

### 6.3 响应规范

**成功响应:**
```json
{
  "data": { },
  "meta": {
    "requestId": "req-uuid-123",
    "timestamp": "2026-05-17T12:00:00Z"
  }
}
```

**分页响应:**
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

**错误响应:**
```json
{
  "code": "SUBJECT_NOT_FOUND",
  "message": "Study subject was not found.",
  "field": "subjectId",
  "details": { }
}
```

### 6.4 第一批 API (Phase 3)

**只读 API (低风险):**
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

---

## 7. 安全模型

### 7.1 权限维度

```text
系统权限
├── 系统管理员
├── 用户管理
└── 配置管理

研究权限
├── Study 级别访问
├── Study 设计
├── 数据查看
└── 数据导出

站点权限
└── Site 级别数据访问

受试者权限
├── Subject 查看
├── Subject 创建
└── Subject 更新

CRF 权限
├── 数据录入
├── 二次录入
├── 查看
└── 锁定

查询权限
├── 创建 query
├── 回复 query
└── 关闭 query

审计权限
└── 查看审计日志

签名权限
├── 电子签名
└── 撤销签名
```

### 7.2 权限实现

使用 Spring Security method security:

```java
@PreAuthorize("@permissionService.canViewStudy(authentication, #studyId)")
public StudyDetailDto getStudy(Long studyId) {
    // ...
}
```

---

## 8. 审计模型

### 8.1 审计原则

所有敏感操作必须记录:
- **who:** 操作者
- **when:** 操作时间
- **where:** IP、User-Agent、会话
- **what:** 操作对象
- **action:** 创建、修改、删除、签名、锁定、导出
- **before:** 修改前值
- **after:** 修改后值
- **reason:** 修改原因
- **study/site:** 数据归属
- **signature:** 是否涉及电子签名
- **requestId:** 请求链路 ID

### 8.2 审计表设计

```sql
-- 现代化审计事件表
CREATE TABLE ocm_audit_event (
    id                  BIGSERIAL PRIMARY KEY,
    request_id          VARCHAR(64),
    actor_user_id       BIGINT,
    actor_username      VARCHAR(255),
    action              VARCHAR(50),
    entity_type         VARCHAR(100),
    entity_id           BIGINT,
    study_id            BIGINT,
    site_id             BIGINT,
    before_json         JSONB,
    after_json          JSONB,
    reason              TEXT,
    ip_address          VARCHAR(45),
    user_agent          TEXT,
    created_at          TIMESTAMPTZ DEFAULT NOW()
);

-- 电子签名表
CREATE TABLE ocm_electronic_signature (
    id                      BIGSERIAL PRIMARY KEY,
    entity_type             VARCHAR(100),
    entity_id               BIGINT,
    signer_user_id          BIGINT,
    signature_meaning       VARCHAR(255),
    signature_reason        TEXT,
    signed_at               TIMESTAMPTZ,
    request_id              VARCHAR(64),
    status                  VARCHAR(50)
);
```

---

## 9. 数据库策略

### 9.1 总原则

1. 保留原数据库可读性
2. 不修改已发布的 Liquibase migration
3. 新增 migration 必须向前兼容
4. 所有结构变更必须有 rollback
5. 关键表变更必须有数据校验 SQL

### 9.2 分阶段策略

**阶段 A: 兼容原 schema**
- 保留原表结构
- 新系统通过 JPA adapter 读取旧表

**阶段 B: 建立 Read Model**
- 针对列表、分页、搜索建立只读视图

**阶段 C: 新增现代化表**
- 新功能使用 `ocm_` 前缀表
- 例如: `ocm_export_job`, `ocm_audit_event`

**阶段 D: 核心表重构 (最后阶段)**
- 只有在 API、测试、审计成熟后才考虑

### 9.3 PostgreSQL 优化

1. 统一使用 `timestamptz`
2. 业务主键保留旧 ID，新系统可用 UUID
3. 分页查询建立组合索引
4. 审计日志按时间和实体类型索引
5. 导出任务建立状态索引

---

## 10. 前端架构

### 10.1 总体策略

- JSP 不一次性替换
- 建立新 React 前端壳
- 逐页迁移
- 初期通过路由 `/legacy/*` 访问旧页面

### 10.2 前端路由

```text
/app/login                    # 登录
/app/dashboard               # 仪表板
/app/studies                 # 研究列表
/app/studies/:studyId        # 研究详情
/app/studies/:studyId/subjects # 研究受试者
/app/subjects/:subjectId     # 受试者详情
/app/subjects/:subjectId/events # 受试者事件
/app/crfs                    # CRF 列表
/app/exports                 # 导出任务
/app/audit                   # 审计日志
/app/admin                   # 系统管理

/legacy/*                    # 旧系统页面
```

### 10.3 UI 迁移顺序

| 顺序 | 页面 | 风险 | 说明 |
|-----:|------|------|------|
| 1 | Dashboard | 低 | 前端壳验证 |
| 2 | Study 列表 | 低 | 只读 |
| 3 | Subject 列表 | 低 | 分页、搜索 |
| 4 | Event 列表 | 低-中 | 状态展示 |
| 5 | CRF 元数据查看 | 中 | 结构复杂 |
| 6 | Audit 查看 | 中 | 权限敏感 |
| 7 | Export 任务 | 中 | 异步任务 |
| 8 | Study/Site/Subject 编辑 | 中 | 写操作 |
| 9 | Query 管理 | 中-高 | 流程状态 |
| 10 | **CRF 数据录入** | **高** | 最后迁移 |
| 11 | **双录入** | **高** | 需要回归 |
| 12 | **电子签名** | **极高** | 需要验证 |

---

## 11. 里程碑路线图

| 里程碑 | 时间 | 目标 |
|--------|------|------|
| M0 | 第 1-2 周 | 盘点、基线、文档、旧系统可运行 |
| M1 | 第 3-5 周 | Docker Compose、配置外部化、新后端骨架 |
| M2 | 第 6-9 周 | 只读 API、OpenAPI、Legacy Adapter |
| M3 | 第 10-13 周 | React 前端壳、Study/Subject/Event 只读页面 |
| M4 | 第 14-18 周 | 权限、用户、审计基础 |
| M5 | 第 19-25 周 | Study/Site/Subject/Event 写功能 |
| M6 | 第 26-32 周 | 导出任务、ODM/CSV、后台任务 |
| M7 | 第 33-40 周 | CRF schema、动态表单预览 |
| M8 | 第 41-52 周 | **CRF 数据录入迁移** |
| M9 | 第 53-64 周 | **双录入、Query、电子签名** |
| M10 | 第 65 周+ | SOAP 废弃、旧页面清理 |

---

## 12. 参考文档

- [openclinica_modernization_refactor_plan.md](../openclinica_modernization_refactor_plan.md) - 完整重构计划
- [MODIFICATIONS.md](./MODIFICATIONS.md) - 修改记录
- [AGENTS.md](../AGENTS.md) - 项目知识库
- [README.md](../README.md) - 技术架构文档

---

## 13. 许可证

本项目基于 OpenClinica 开源代码进行现代化重构。根据 GNU LGPL 许可证要求：

1. 本项目的修改部分遵循 LGPL 许可证
2. 所有修改均在 [MODIFICATIONS.md](./MODIFICATIONS.md) 中记录
3. 原始代码版权归属 OpenClinica, LLC
4. 修改代码版权归属本项目的贡献者
