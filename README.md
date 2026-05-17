# OpenClinica — 电子数据采集系统

**版本:** 3.18-SNAPSHOT  
**最后更新:** 2026-05-17  
**许可证:** GNU LGPL  

OpenClinica 是一个开源的临床试验电子数据采集（EDC）和临床数据管理（CDM）平台。

---

## 技术栈

| 组件 | 版本 |
|------|------|
| **Java** | 21 LTS |
| **Spring Framework** | 6.1.5 |
| **Spring Security** | 6.2.3 |
| **Spring WS** | 4.0.10 |
| **Hibernate ORM** | 6.4.4.Final |
| **Jakarta EE** | 10 (Servlet 6.0, JSP 3.1, JAXB 4.0) |
| **Jackson** | 2.17.0 |
| **Maven** | 3.9.x |
| **Liquibase** | 4.26.0 |

**应用服务器:** Tomcat 10.1.x 推荐  
**数据库:** PostgreSQL 14+ / Oracle 19c+

---

## 项目结构

```
OpenClinica/
├── pom.xml             # Maven 父 POM
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
└── ws/                 # SOAP Web 服务 (57 源文件)
    └── endpoint/       # Spring WS 端点
```

---

## 构建与运行

```bash
# 编译全部模块
mvn clean compile -DskipTests

# 跳过测试打包
mvn clean package -DskipTests

# 部署到 Tomcat 10.1
cp web/target/OpenClinica-web-*.war $CATALINA_HOME/webapps/
cp ws/target/OpenClinica-ws-*.war $CATALINA_HOME/webapps/

# 数据库迁移
mvn liquibase:update -pl core

# 运行测试
mvn test
```

---

## 测试架构

| 层级 | 基类 | 数据库 | 数量 |
|------|------|--------|------|
| 纯单元测试 | `junit.framework.TestCase` | ❌ | 17 (core) + 2 (web) |
| DAO 集成测试 | `HibernateOcDbTestCase` (DBUnit) | ✅ | DBUnit XML 数据集 |
| Service 集成测试 | `HibernateOcDbTestCase` | ✅ | 同 DAO |

测试数据文件: `core/src/test/resources/org/akaza/openclinica/{dao,service}/testdata/`

---

## 核心架构模式

- **DAO 模式:** 所有数据库访问通过 `EntityDAO<K>` 子类
- **Bean 模式:** DTO 继承 `EntityBean` 含审计字段 (id, createdDate, ownerId...)
- **SecureController:** 所有 Servlet 继承此类自动处理会话/权限
- **服务层:** `@Service` + `@Transactional` 封装业务编排

---

## 开发规范

- 禁止绕过 `SecureController` 做会话检查
- 禁止在 Servlet 中直接写 SQL — 使用 DAO 层
- 禁止硬编码文件路径 — 使用 `CoreResources.getField()`
- 禁止修改已发布的 Liquibase 迁移文件
- 包结构: `bean.*` (DTO), `dao.*` (数据访问), `service.*` (业务), `domain.*` (实体)

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

---

## 国际化

支持 6 种语言: en, de, es, fr, pt, zh  
资源文件: `core/src/main/resources/org/akaza/openclinica/i18n/`

---

## 相关文档

- [AGENTS.md](./AGENTS.md) — AI 助手知识库
- [MODIFICATIONS.md](./MODIFICATIONS.md) — 修改记录
- [PLAN.md](./PLAN.md) — 已知问题与规划
