# OpenClinica 技术架构文档

## 项目概述

OpenClinica 是一个开源的临床试验电子数据采集（EDC）和临床数据管理（CDM）系统，用于优化临床试验工作流程。

**当前版本:** 3.18-SNAPSHOT  
**最后更新:** 2026-05-17

---

## 测试架构

OpenClinica 采用三层测试体系，覆盖不同粒度的业务逻辑验证：

### 1. 纯单元测试（JUnit）
- **基类:** `junit.framework.TestCase`
- **依赖:** 无 Spring 上下文，无数据库
- **用途:** 测试表达式语法验证（`ExpressionServiceTest`）、日期格式转换（`ItemDataDAOTest`）、权限控制（`SubmitDataServletTest`）
- **Mock 框架:** Mockito（用于 web 层角色/权限桩代码）

### 2. DAO 集成测试（DBUnit）
- **基类:** `HibernateOcDbTestCase`（继承 DBUnit `DataSourceBasedDBTestCase`）
- **依赖:** 完整 Spring 上下文 + 真实数据库连接（通过 `test.properties` 配置）
- **数据准备:** DBUnit XML 文件，按约定路径自动加载：
  ```
  测试类: org.akaza.openclinica.dao.rule.RuleSetDaoTest
  数据文件: org/akaza/openclinica/dao/rule/testdata/RuleSetDaoTest.xml
  ```
- **事务隔离:** `tearDown()` 中回滚未提交事务，关闭 DataSource 连接

### 3. Service 集成测试
- **基类:** `HibernateOcDbTestCase`（同 DAO 测试）
- **用途:** 验证业务编排逻辑，如 `RuleSetService.filterByStatusEqualsAvailable()`
- **Spring Bean 获取:** `getContext().getBean("serviceName")`

### 测试文件统计
| 模块 | 测试类 | DBUnit XML 数据集 |
|------|--------|-------------------|
| `core/` | 17 | 12 |
| `web/` | 2 | 0 |
| **总计** | **19** | **12** |

---

## 技术栈

### 核心技术
| 技术 | 版本 | 用途 |
|------|------|------|
| **Java** | 7 | 编程语言 |
| **Spring Framework** | 3.2.18.RELEASE | IOC容器、事务管理 |
| **Spring Security** | 3.2.10.RELEASE | 安全认证与授权 |
| **Hibernate** | 3.5.1-Final | ORM框架 |
| **Maven** | 3.x | 构建工具 |
| **Liquibase** | 4.3.5 | 数据库迁移 |

### 数据库支持
- **Oracle** - 企业级部署
- **PostgreSQL** - 开源替代方案

### Web技术
- **Servlet/JSP** - 传统Web层
- **Spring MVC** - REST API
- **Spring WS** - SOAP Web服务
- **JSTL** - JSP标签库

### 其他组件
- **Quartz** - 任务调度
- **Logback** - 日志框架
- **SLF4J** - 日志抽象层
- **Jackson** - JSON处理
- **Ehcache** - 缓存

---

## 项目结构

```
OpenClinica/
├── pom.xml                    # Maven父POM
├── README.md                  # 本文件
├── AGENTS.md                  # AI助手知识库
├── core/                      # 核心模块（736个Java文件）
│   ├── src/main/java/org/akaza/openclinica/
│   │   ├── bean/             # DTO/POJO对象
│   │   ├── dao/              # 数据访问层
│   │   ├── domain/           # Hibernate实体类
│   │   ├── service/          # 业务服务层
│   │   ├── logic/            # 业务逻辑（规则引擎）
│   │   ├── job/              # Quartz定时任务
│   │   └── validator/        # 输入验证
│   └── src/main/resources/
│       ├── migration/        # Liquibase迁移脚本
│       └── properties/       # 配置文件
│
├── web/                       # Web模块（481个Java文件，419个JSP页面）
│   ├── src/main/java/org/akaza/openclinica/
│   │   ├── control/          # Servlet控制器
│   │   ├── controller/       # Spring MVC REST控制器
│   │   ├── view/             # 视图辅助类
│   │   └── web/              # Web工具类
│   └── src/main/webapp/
│       └── WEB-INF/jsp/      # JSP页面
│
└── ws/                        # Web服务模块（57个Java文件）
    └── src/main/java/org/akaza/openclinica/
        └── ws/               # SOAP端点
```

---

## 核心架构模式

### 1. DAO模式（数据访问层）
所有数据库访问通过继承 `EntityDAO` 实现：

```java
public class StudyDAO extends EntityDAO<StudyBean> {
    public StudyBean findByPK(int id) {
        HashMap variables = new HashMap();
        variables.put(1, id);
        return (StudyBean) executeFind("findByPK", variables).get(0);
    }
}
```

**特点：**
- 统一的CRUD操作封装
- 支持命名参数查询
- 自动事务管理

### 2. Bean模式（数据传输对象）
所有DTO继承 `EntityBean`，包含审计字段：
- `id` - 主键
- `createdDate` - 创建时间
- `updatedDate` - 更新时间
- `ownerId` - 创建者
- `updaterId` - 更新者

### 3. SecureController模式（Web安全）
所有Servlet必须继承 `SecureController`：

```java
public class MyServlet extends SecureController {
    @Override
    protected void processRequest() throws Exception {
        // 自动验证会话和权限
        // 业务逻辑...
        forwardPage(Page.MY_PAGE);
    }
}
```

### 4. 服务层模式
业务逻辑封装在Service中：

```java
@Service
@Transactional
public class StudyServiceImpl implements StudyService {
    @Autowired
    private StudyDAO studyDao;
    
    public StudyBean createStudy(StudyBean study) {
        // 业务逻辑...
        return studyDao.create(study);
    }
}
```

---

## 开发规范

### 命名约定
| 类型 | 命名规则 | 示例 |
|------|----------|------|
| DTO | `*Bean` | `StudyBean`, `SubjectBean` |
| DAO | `*DAO` | `StudyDAO`, `SubjectDAO` |
| Servlet | `*Servlet` | `CreateStudyServlet` |
| Service | `*Service` / `*ServiceImpl` | `StudyService` |
| Controller | `*Controller` | `StudyController` |

### 包结构
- `org.akaza.openclinica.bean.*` - 数据对象
- `org.akaza.openclinica.dao.*` - 数据访问
- `org.akaza.openclinica.service.*` - 业务服务
- `org.akaza.openclinica.domain.*` - Hibernate实体
- `org.akaza.openclinica.logic.*` - 业务逻辑

---

## 关键功能模块

### 1. 研究管理（Study Management）
- **位置:** `control/managestudy/`
- **功能:** 创建/编辑研究、站点管理、受试者管理
- **核心类:** `CreateStudyServlet`, `ViewStudySubjectServlet`

### 2. 数据录入（Data Entry）
- **位置:** `control/submit/`
- **功能:** CRF数据录入、双录入验证、电子签名
- **核心类:** `DataEntryServlet`, `DoubleDataEntryServlet`

### 3. 规则引擎（Rules Engine）
- **位置:** `logic/rule/`
- **功能:** 表达式验证、自动计算、数据校验规则
- **核心类:** `ExpressionService`, `RuleSetService`

### 4. 数据提取（Data Extraction）
- **位置:** `control/extract/`
- **功能:** ODM导出、数据集创建、报告生成
- **格式:** CDISC ODM XML

### 5. Web服务（SOAP）
- **位置:** `ws/`
- **功能:** 研究/受试者/事件的CRUD操作
- **端点:** `StudyEndpoint`, `StudySubjectEndpoint`, `EventEndpoint`

---

## 构建与部署

### 常用Maven命令

```bash
# 编译整个项目（跳过测试）
mvn clean install -DskipTests

# 使用特定配置构建
mvn clean install -Dconfig.id=integration

# 运行单元测试
mvn test

# 执行数据库迁移
mvn liquibase:update -pl core

# 构建发布包
mvn clean package assembly:single
```

### 配置文件

配置文件位于各模块的 `src/main/filters/`：
- `default.properties` - 默认配置
- `datainfo.properties` - 数据库连接
- `integration.properties` - 集成环境
- `dev-*.properties` - 开发者环境

**重要配置项：**
```properties
# 数据库连接
db.url=jdbc:postgresql://localhost:5432/openclinica
db.username=clinica
db.password=password

# 文件存储路径
crf.file.extension=.xls
crf.file.directory=/path/to/crfs
```

---

## 安全注意事项

### ⚠️ 禁止事项
1. **永远不要**绕过 `SecureController` 进行会话检查
2. **永远不要**在Servlet中直接写SQL - 使用DAO层
3. **永远不要**硬编码文件路径 - 使用 `CoreResources.getField()`
4. **永远不要**修改已发布的迁移文件

### 最佳实践
- 所有控制器必须继承 `SecureController`
- 使用 `@Transactional` 注解管理事务
- 敏感操作需要记录审计日志
- 文件上传需验证类型和大小

---

## 国际化（i18n）

支持6种语言：
- 英语（en）
- 德语（de）
- 西班牙语（es）
- 法语（fr）
- 葡萄牙语（pt）
- 中文（zh）

**资源文件位置：**
- `core/src/main/resources/org/akaza/openclinica/i18n/`
- `web/src/main/resources/org/akaza/openclinica/i18n/`

---

## 数据库迁移

使用Liquibase管理数据库变更：

```bash
# 更新数据库到最新版本
mvn liquibase:update -pl core

# 回滚到特定版本
mvn liquibase:rollback -Dliquibase.rollbackTag=version_3_0 -pl core
```

**迁移脚本位置：** `core/src/main/resources/migration/`

---

## 相关文档

- [AGENTS.md](./AGENTS.md) - AI助手知识库（测试架构、编码约定）
- [core/AGENTS.md](./core/AGENTS.md) - 核心模块详情（DAO/Service 测试模式）
- [web/AGENTS.md](./web/AGENTS.md) - Web模块详情（Mockito 测试模式）
- [ws/AGENTS.md](./ws/AGENTS.md) - Web服务模块详情
- [MODERNIZATION_FINAL.md](./MODERNIZATION_FINAL.md) - 现代化改造方案

---

## 许可协议

[GNU LGPL License](https://www.openclinica.com/gnu-lgpl-open-source-license)

---

## 联系方式

- **官方文档:** https://docs.openclinica.com
- **问题报告:** https://jira.openclinica.com
- **社区论坛:** http://forums.openclinica.com
- **GitHub:** https://github.com/OpenClinica/OpenClinica
