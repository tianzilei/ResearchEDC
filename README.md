# OpenClinica — 电子数据采集系统

**版本:** 3.18-SNAPSHOT  
**最后更新:** 2026-05-17  
**许可证:** GNU LGPL  

OpenClinica 是一个开源的临床试验电子数据采集（EDC）和临床数据管理（CDM）平台。

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
OpenClinica/
├── pom.xml             # Maven 父 POM
├── frontend/           # React 19 SPA (pnpm workspace)
│   ├── src/
│   │   ├── api/        # API 客户端 + 类型
│   │   ├── components/ # 通用组件 (StudySwitcher, SkeletonCard)
│   │   ├── hooks/      # TanStack Query 封装 + 权限 hooks
│   │   ├── layouts/    # AppLayout (顶栏 + 侧栏 + 内容区)
│   │   ├── pages/      # 页面组件
│   │   ├── providers/  # AuthProvider, AppProviders
│   │   ├── router/     # React Router 配置
│   │   ├── styles/     # Ant Design 主题
│   │   └── types/      # TypeScript 类型定义
│   ├── vite.config.ts  # Vite 配置 (代理 / API 构建输出)
│   └── package.json
├── app/                # Spring Boot 模块化单体入口
│   ├── src/main/java/
│   │   └── org/akaza/openclinica/
│   │       ├── config/      # WebMvcConfig (SPA fallback), WebServiceConfig, OpenApiConfig
│   │       └── module/      # Spring Modulith 模块
│   │           ├── notification/  # 通知模块 (已提取)
│   │           └── identity/      # 身份模块桩
│   └── src/main/resources/
│       ├── application.yml # profile 配置
│       └── static/         # 前端构建产物 (自动生成)
├── core/               # 领域逻辑 & 数据访问 (736 源文件)
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
├── docker/             # multi-stage Dockerfiles
└── scripts/            # 构建/部署/发布脚本
```

---

## 路由架构

```
/legacy/*  → 旧 OpenClinica JSP (向后兼容)
/app/*     → 新 React SPA (前端路由, index.html fallback)
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

# === Docker ===
docker compose -f deploy/compose/docker-compose.dev.yml up --build
```

---

## 测试架构

| 层级 | 基类 | 数据库 | 数量 |
|------|------|--------|------|
| 纯单元测试 | `junit.framework.TestCase` | ❌ | 17 (core) + 2 (web) |
| DAO 集成测试 | `HibernateOcDbTestCase` (DBUnit) | ✅ | DBUnit XML 数据集 |
| Service 集成测试 | `HibernateOcDbTestCase` | ✅ | 同 DAO |
| Modulith 验证 | JUnit 5 + `ApplicationModules` | ❌ | 1 (app) |

测试数据文件: `core/src/test/resources/org/akaza/openclinica/{dao,service}/testdata/`

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
| 研究管理 | `control/managestudy/` | 研究/Site/Subject 管理 |
| 数据录入 | `control/submit/` | CRF 数据录入、双录入、电子签名 |
| 规则引擎 | `logic/rule/` | 表达式验证、自动计算 |
| 数据提取 | `control/extract/` | ODM 导出、报告生成 |
| SOAP API | `ws/` | 研究/Subject/CRUD |
| REST API | `controller/` | Spring MVC JSON API |
| 通知模块 | `module/notification/` | Spring Modulith 模块 (事件驱动邮件) |
| 前台 SPA | `frontend/src/` | React 19 管理界面 |

---

## 国际化

支持 6 种语言: en, de, es, fr, pt, zh  
资源文件: `core/src/main/resources/org/akaza/openclinica/i18n/`

---

## 相关文档

- [AGENTS.md](./AGENTS.md) — AI 助手知识库
- [MODIFICATIONS.md](./MODIFICATIONS.md) — 修改记录
- [PLAN.md](./PLAN.md) — 已知问题与规划
