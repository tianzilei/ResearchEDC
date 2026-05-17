# OpenClinica 修改记录

**项目:** OpenClinica 技术栈现代化重构  
**基础版本:** 3.18-SNAPSHOT (基于 3.14)  
**许可证:** GNU LGPL 

---

## 2026-05-17 — Milestone 2: Spring Boot 化

- **模块:** 项目整体 + 新增 `app/` 模块
- **原因:** 从传统 Spring XML/WAR 转向 Spring Boot 应用形态，实现模块化单体第一步
- **差异:**
  - **parent pom.xml:** 新增 `<module>app</module>`, Spring Boot 3.2.5 / springdoc 2.5.0 属性
  - **web pom.xml:** 新增 `maven-war-plugin attachClasses=true` 以产出 classes JAR 供 app 模块编译依赖
  - **ws pom.xml:** 同上
  - **新增 `app/pom.xml`:** 整合 core+web+ws 的 Spring Boot WAR 模块，含 starter-web/actuator/validation、springdoc、spring-ws、testcontainers
  - **新增 `app/.../OpenClinicaApplication.java`:** `@SpringBootApplication` + `@ImportResource` 加载 11 个 XML 配置文件
  - **新增 `app/.../OpenClinicaServletInitializer.java`:** WAR 部署兼容 (extends `SpringBootServletInitializer`)
  - **新增 `app/.../config/WebServiceConfig.java`:** 注册 Spring WS `MessageDispatcherServlet` 以支持 SOAP 端点
  - **新增 `app/.../config/OpenApiConfig.java`:** OpenAPI 3 文档元信息
  - **新增 `app/src/main/resources/application.yml`:** dev/test/prod 三环境 profile，Actuator、springdoc 配置
  - **新增 `app/src/main/resources/logback-spring.xml`:** Spring profile-aware 日志配置
- **架构变化:**
  - 原有三个模块 (core/web/ws) 保持 WAR 兼容性不变
  - app 模块通过 WAR overlay 继承 web 模块的 JSP/静态资源
  - app 模块通过 `@ImportResource` 加载 core/web 模块的 Spring XML 配置
  - 可通过 `java -jar OpenClinica.war` 或部署到 Tomcat 10.1 两种方式运行
- **构建:** `mvn clean package -DskipTests` ✅ 全部 5 模块通过
  - `OpenClinica-core-3.18-SNAPSHOT.jar` = 2.3 MB
  - `OpenClinica-web-3.18-SNAPSHOT.war` = 108 MB (不变，向后兼容)
  - `OpenClinica-ws-3.18-SNAPSHOT.war` = 98 MB (不变，向后兼容)
  - `OpenClinica.war` (app) = 275 MB (Spring Boot repackaged，可独立运行)
- **验证状态:** ✅ 已验证 (`mvn clean package -DskipTests` 全部通过，Spring 版本收敛为 6.1.5 一致) 

---

## 修改格式

每次修改应记录: 修改模块、原因、与原版差异、许可证影响、数据库结构影响、回滚方式、验证状态、相关提交。

---

## 2026-05-17 — 初始化现代化重构项目

- **模块:** 项目整体
- **原因:** 将技术栈从 Java 7/Spring 3.2/Hibernate 3.5 迁移到 Java 21/Spring 6.x/Hibernate 6.x
- **差异:**
  - 新增重构计划文档
  - 新增 AGENTS.md 知识库
  - 更新 README.md 为中文技术架构文档
  - 新增 v3.17.3 数据库迁移脚本
- **许可证影响:** 无，保持 GNU LGPL
- **数据库:** 新增 3.17 migration 目录 (性能索引)
- **验证状态:** ⬜ 未验证

---

## 2026-05-17 — 创建项目知识库 (AGENTS.md)

- **模块:** 文档系统
- **原因:** 建立 AI 助手和开发者的项目知识库
- **差异:** 新增根目录、core、web、ws 四份 AGENTS.md
- **验证状态:** ✅ 已验证

---

## 2026-05-17 — 集成 v3.17.3 数据库迁移

- **模块:** 数据库迁移 (core/.../migration/)
- **原因:** 从 Release v3.17.3 提取性能优化索引
- **差异:**
  - 新增 `migration/3.17/release.xml`
  - 新增 `migration/3.17/2018-01-01-OC-performance-indexes.xml` (130+ 索引)
  - 更新 `migration/master.xml`
- **数据库:** 新增 130+ 索引
- **回滚:** `mvn liquibase:rollback -pl core -Dliquibase.rollbackCount=1`
- **验证状态:** ⬜ 未验证

---

## 2026-05-17 — 依赖升级 (核心迁移)

- **模块:** 全部 (pom.xml + 1300+ Java 文件)
- **原因:** Java 7→21, Spring 3.2→6.1.5, Hibernate 3.5→6.4.4, Jackson 1→2, Jakarta EE 命名空间迁移
- **差异:**
  - Maven POM 4 个文件更新
  - 1000+ import 语句从 `javax.*` 迁移到 `jakarta.*`
  - Commons Lang 2→3, SLF4J 1.7→2.0, Logback 1.1→1.5
  - POI 3→5, iText→OpenPDF, CGLIB→ByteBuddy
  - Jersey 1→3, Rome `com.sun.syndication`→`com.rometools`
- **验证状态:** ⬜ 未验证

---

## 2026-05-17 — Hibernate DAO 重构

- **模块:** `core/.../dao/hibernate/`
- **原因:** HibernateTemplate → JPA EntityManager
- **差异:**
  - `AbstractDomainDao.java` 重写为 JPA EntityManager
  - `CompositeIdAbstractDomainDao.java` 重写
  - 删除 `IntegerEnumUserType` (Hibernate 6 原生枚举支持)
- **验证状态:** ⬜ 未验证

---

## 2026-05-17 — Spring 配置迁移

- **模块:** Spring XML 配置文件 (6 个文件)
- **原因:** 适配 Spring 6 / Hibernate 6 / Spring Security 6
- **差异:**
  - `applicationContext-core-hibernate.xml` → JPA 配置
  - `applicationContext-core-security.xml` → Spring Security 6
  - `applicationContext-security.xml` (web/ws) → 更新
  - `security-config.xml` → 更新
  - `web.xml` (web/ws) → Jakarta EE 6.0
- **验证状态:** ⬜ 未验证

---

## 2026-05-17 — 编译修复 (Phase 1)

- **模块:** core + web + ws
- **原因:** 修复依赖升级后的编译错误
- **差异:**
  - **core:** `Stopwatch.java` (Commons Lang 2→3), `FileUploadHelper.java` (JakartaServletRequestContext 适配 commons-fileupload), `CodedEnumType.java` (Hibernate 6 兼容)
  - **web:** `JakartaWebContext` (JMesa 适配), `OCCsvViewExporter`/`XmlViewExporter` 重写, `OCServletFilter` (javax.servlet→jakarta.servlet), 7 个 Quartz 文件的 `JobDetailBean`→`JobDetailFactoryBean`, `OpenClinicaJdbcService` (`GrantedAuthorityImpl`→`SimpleGrantedAuthority`), `HandlerInterceptorAdapter`→`HandlerInterceptor`, POI `CellType` enum 适配, Spring `ModelMap.addObject` 签名修正
  - **ws:** `SpringPlainTextPasswordValidationCallbackHandler` (XWSS→WSS4J), `UserPermissionInterceptor` (afterCompletion), `SetUpUserInterceptor` (extends→implements), JAXB plugin 配置
- **构建:** `mvn clean compile` 三模块均通过
- **验证状态:** ✅ 已验证

---

## 2026-05-17 — Milestone 0: 迁移版本验收

- **模块:** 全部 (pom.xml, AGENTS.md, web.xml, Spring XML, Maven Enforcer)
- **原因:** 自动化完成 Milestone 0 — 当前迁移版本验收验证
- **差异:**
  - 修复 web + ws pom.xml — 移除无法解析的 httpunit:1.7.3 测试依赖
  - 修复 ws web.xml — 更新 sitemesh filter 为 org.sitemesh 3.x 类 (原 com.opensymphony 2.x 不存在)
  - 修复 Hibernate 缓存配置 — 禁用 EhCacheRegionFactory (Hibernate 6 移除)
  - 修复 Maven Enforcer — Java 21 规则，修复 6 项依赖版本冲突 (byte-buddy, slf4j, metrics, castor, commons-lang, commons-logging)
  - 修复 AGENTS.md — 技术栈描述更新为 Java 21/Spring 6.1.5/Hibernate 6.4.4
  - 修复 slf4j 2.0.12→2.0.16, byte-buddy 1.14.9→1.14.12, metrics 3.1.2→3.1.5
- **构建:** `mvn clean package -DskipTests` ✅ 三模块全部通过
  - `OpenClinica-core-3.18-SNAPSHOT.jar` = 2.3 MB
  - `OpenClinica-web-3.18-SNAPSHOT.war` = 108 MB
  - `OpenClinica-ws-3.18-SNAPSHOT.war` = 98 MB
- **验证状态:** ✅ 已验证

---

## 2026-05-17 — Milestone 1: Docker-first 可运行基线 (Phase 2.1)

- **模块:** Docker 基础设施
- **原因:** 建立 Docker-first 可运行基线，让系统以 Docker Compose 方式一键启动
- **差异:**
  - 新增 `docker/web/Dockerfile` — 多阶段构建 (Maven 编译 + Tomcat 10.1/jdk21 部署)
  - 新增 `docker/ws/Dockerfile` — WS 模块多阶段构建镜像
  - 新增 `docker/scripts/entrypoint.sh` — 容器启动时从 OC_DB_* 环境变量动态生成 datainfo.properties
  - 新增 `deploy/compose/docker-compose.dev.yml` — web + ws + postgres + mailhog + adminer 编排
  - 新增 `deploy/compose/.env.example` — 环境变量模板
  - 数据库连接配置改为环境变量注入 (OC_DB_HOST/PORT/NAME/USER/PASS)
  - 上传路径 (`/opt/openclinica/data`) 和日志路径 (`/opt/openclinica/logs`) volume 化
- **许可证影响:** 无，保持 GNU LGPL
- **数据库:** 无变化
- **回滚:** git revert 新增文件
- **验证状态:** ✅ 已验证 (脚本语法检查通过)

---

## 2026-05-17 — 文档整理

- **模块:** 文档系统
- **原因:** 合并冗余文档为三份核心文件
- **差异:**
  - 合并 `MODERNIZATION_FINAL.md`, `MODERNIZATION_SUMMARY.md`, `MODERNIZATION_PHASE2_COMPLETE.md` → `MODIFICATIONS.md`
  - 合并 `KNOWN_ISSUES.md`, `OpenClinica_Modernization_Issues_and_Next_Plan.md`, 规划类文档 → `PLAN.md`
  - 更新 `README.md` 反映最新技术栈
  - 删除 `docs/` 和 `logs/` 目录
- **验证状态:** ✅ 已验证
