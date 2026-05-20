# ResearchEDC 命名迁移与重构修改计划

> 适用场景：将基于 OpenClinica 的 fork / refactor 项目重命名为 **ResearchEDC**，并同步完成 repo、package、模块、Docker、文档、许可证与修改记录调整。

---

## 1. 目标与原则

### 1.1 总体目标

将当前基于 OpenClinica 的改造项目迁移为一个独立命名的科研电子数据采集与临床研究数据管理平台：**ResearchEDC**。

核心目标包括：

1. 降低与 OpenClinica 官方项目之间的名称、品牌、商标和来源混淆风险。
2. 保留原始 LGPL 许可义务，不通过改名规避版权与开源许可要求。
3. 建立清晰的 fork 来源说明、修改记录和重构记录。
4. 将代码结构逐步调整为现代化、模块化、可维护的 ResearchEDC 架构。
5. 为后续集成随机化系统、问卷系统、针刺临床研究模块、神经生理数据模块、Docker 部署和审计追踪功能提供稳定基础。

### 1.2 合规原则

ResearchEDC 的命名迁移应遵循以下原则：

- 不再使用 OpenClinica 作为 repo 名、产品名、Docker 镜像名或 package 主命名空间。
- README、NOTICE、MODIFICATIONS.md 中保留项目来源说明。
- 来源于 OpenClinica 的代码继续保留 GNU LGPL 许可。
- 不删除原始 copyright、license、disclaimer。
- 对修改过的文件、目录、模块和架构变更进行记录。
- 避免使用“official OpenClinica”“certified OpenClinica”“OpenClinica edition”等容易造成官方背书误解的表述。

推荐表述：

```text
ResearchEDC is an independently maintained research data management platform derived from OpenClinica v3.x.
```

不推荐表述：

```text
Official OpenClinica Research Edition
OpenClinica Pro
OpenClinica Clinical Research Platform
```

---

## 2. 推荐命名方案

### 2.1 项目显示名称

```text
ResearchEDC
```

### 2.2 Repo 名称

推荐：

```text
research-edc
```

如果需要突出基础框架属性，也可以使用：

```text
research-edc-core
```

建议优先使用 `research-edc`，因为名称更简洁，适合长期维护。

### 2.3 后端 package 命名

根据机构或实验室情况选择一种：

```text
org.researchedc
```

或：

```text
cn.yourinstitution.researchedc
```

或：

```text
cn.yourlab.researchedc
```

如果尚未确定机构命名空间，建议先采用：

```text
org.researchedc
```

后续如需内部化，可再迁移为机构命名空间。

### 2.4 Docker 镜像命名

推荐：

```text
research-edc/app
research-edc/backend
research-edc/frontend
research-edc/db
```

如果发布到个人或机构 registry：

```text
yourname/research-edc
yourlab/research-edc
yourregistry/research-edc-backend
```

### 2.5 数据库命名

推荐：

```text
research_edc
research_edc_dev
research_edc_test
research_edc_prod
```

### 2.6 服务命名

Docker Compose 服务名建议：

```yaml
services:
  researchedc-backend:
  researchedc-frontend:
  researchedc-db:
  researchedc-redis:
  researchedc-minio:
  researchedc-nginx:
```

---

## 3. 迁移范围

本次命名迁移分为七类：

1. Repository 与文档命名迁移
2. Package / namespace 命名迁移
3. Maven / Gradle / npm / 前端包命名迁移
4. Docker / Compose / 环境变量迁移
5. 数据库 schema、初始化脚本与配置迁移
6. UI 显示名称、页面标题和国际化文本迁移
7. LGPL 合规文档、修改记录和 NOTICE 更新

---

## 4. 阶段化执行计划

## Phase 0：迁移前准备

### 4.1 建立安全分支

建议从当前稳定分支创建迁移分支：

```bash
git checkout main
git pull
git checkout -b refactor/research-edc-rename
```

### 4.2 建立迁移基线 tag

在正式迁移前打 tag，便于回滚与审计：

```bash
git tag pre-researchedc-rename-2026-05
git push origin pre-researchedc-rename-2026-05
```

### 4.3 生成当前 OpenClinica 残留名称清单

在项目根目录执行：

```bash
grep -RIn "OpenClinica\|openclinica\|OPENCLINICA" . \
  --exclude-dir=.git \
  --exclude-dir=target \
  --exclude-dir=node_modules \
  --exclude-dir=build \
  > rename_audit_openclinica_refs.txt
```

该文件用于判断哪些地方需要替换，哪些地方需要保留为来源说明。

### 4.4 分类处理引用

将搜索结果分成三类：

| 类型 | 示例 | 处理方式 |
|---|---|---|
| 品牌/产品名 | 页面标题、系统名称、Docker 镜像名 | 替换为 ResearchEDC |
| package / namespace | `org.openclinica...` | 逐步迁移为 `org.researchedc...` |
| 来源/许可证说明 | LICENSE、NOTICE、README 来源段落 | 保留 OpenClinica 字样 |

---

## Phase 1：Repo 与文档命名迁移

### 5.1 修改 repo 名称

推荐 GitHub repo 名：

```text
research-edc
```

如果保留 fork 关系，建议在 repo description 中写：

```text
A customized research EDC platform derived from OpenClinica v3.x.
```

### 5.2 更新 README 标题

README 开头建议改为：

```markdown
# ResearchEDC

ResearchEDC is an independently maintained research electronic data capture and clinical research data management platform derived from OpenClinica v3.x.

This project contains substantial refactoring, package renaming, deployment modernization, and study-specific extensions. Files derived from the original OpenClinica source code remain licensed under the GNU Lesser General Public License (LGPL), version 2.1 or later.
```

### 5.3 增加来源说明

README 中建议加入：

```markdown
## Origin and License

ResearchEDC is derived from OpenClinica v3.x. OpenClinica is distributed under the GNU Lesser General Public License (LGPL), version 2.1 or later.

This project has been renamed to avoid confusion with the official OpenClinica project. The renaming does not alter the license obligations for code derived from OpenClinica.

See `MODIFICATIONS.md` for a summary of changes and `NOTICE` for attribution information.
```

### 5.4 增加非官方声明

```markdown
## Trademark Notice

OpenClinica is a trademark of its respective owner. ResearchEDC is an independent modified project and is not an official OpenClinica release. This project is not affiliated with, endorsed by, or sponsored by OpenClinica.
```

---

## Phase 2：Package / Namespace 命名迁移

### 6.1 推荐迁移目标

原始命名空间可能类似：

```text
org.akaza.openclinica
org.openclinica
```

目标命名空间推荐：

```text
org.researchedc
```

如需保留机构属性：

```text
cn.yourinstitution.researchedc
```

### 6.2 迁移策略

不建议一次性手动全局替换所有文本。应分层迁移：

1. 先迁移 Java package 和 import。
2. 再迁移配置文件中的 classpath、bean name、component scan。
3. 再迁移测试代码。
4. 最后迁移文档、脚本、Docker 和 UI 文本。

### 6.3 Java package 迁移步骤

以 Maven / Java 项目为例：

```bash
# 示例：将 org.openclinica 替换为 org.researchedc
find src -type f -name "*.java" -print0 | xargs -0 sed -i 's/org\.openclinica/org.researchedc/g'
```

如果原始 package 是 `org.akaza.openclinica`：

```bash
find src -type f -name "*.java" -print0 | xargs -0 sed -i 's/org\.akaza\.openclinica/org.researchedc/g'
```

然后移动目录结构：

```bash
mkdir -p src/main/java/org/researchedc
mkdir -p src/test/java/org/researchedc
```

使用 IDE 的 refactor 功能会更安全，特别是大型 Java 项目中涉及 XML、注解扫描、Spring bean、JSP 引用和反射字符串时。

### 6.4 Spring / XML / 配置扫描调整

需要检查：

```text
applicationContext.xml
web.xml
spring-security.xml
persistence.xml
hibernate.cfg.xml
application.properties
application.yml
```

重点搜索：

```text
org.openclinica
org.akaza.openclinica
openclinica
```

替换为：

```text
org.researchedc
researchedc
```

### 6.5 反射与字符串类名检查

Java 项目中可能存在字符串形式的 class name，例如：

```java
Class.forName("org.openclinica.xxx.SomeClass")
```

这些内容不会被 IDE 自动完全处理，需要 grep 检查：

```bash
grep -RIn "org\.openclinica\|org\.akaza\.openclinica" src config . \
  --exclude-dir=.git \
  --exclude-dir=target
```

---

## Phase 3：Build 配置迁移

### 7.1 Maven 项目

检查并修改：

```text
pom.xml
*/pom.xml
```

推荐 groupId / artifactId：

```xml
<groupId>org.researchedc</groupId>
<artifactId>research-edc</artifactId>
<name>ResearchEDC</name>
<description>A research electronic data capture platform derived from OpenClinica.</description>
```

如果是多模块项目：

```xml
<modules>
  <module>researchedc-core</module>
  <module>researchedc-web</module>
  <module>researchedc-api</module>
  <module>researchedc-export</module>
</modules>
```

### 7.2 Gradle 项目

检查：

```text
settings.gradle
build.gradle
gradle.properties
```

示例：

```gradle
rootProject.name = 'research-edc'

group = 'org.researchedc'
version = '0.1.0-refactor'
```

### 7.3 前端 package

如果后续前端独立化，推荐：

```json
{
  "name": "research-edc-frontend",
  "description": "Frontend for ResearchEDC"
}
```

---

## Phase 4：Docker 与部署命名迁移

### 8.1 Dockerfile Label

Dockerfile 中建议加入：

```dockerfile
LABEL org.opencontainers.image.title="ResearchEDC"
LABEL org.opencontainers.image.description="A research EDC platform derived from OpenClinica v3.x"
LABEL org.opencontainers.image.licenses="LGPL-2.1-or-later"
```

### 8.2 docker-compose.yml 服务名

推荐结构：

```yaml
services:
  researchedc-backend:
    image: researchedc/backend:dev
    container_name: researchedc-backend

  researchedc-db:
    image: postgres:16
    container_name: researchedc-db

  researchedc-nginx:
    image: nginx:stable
    container_name: researchedc-nginx
```

### 8.3 环境变量命名

原先可能存在：

```text
OPENCLINICA_DB_HOST
OPENCLINICA_DB_NAME
OPENCLINICA_HOME
```

建议迁移为：

```text
RESEARCHEDC_DB_HOST
RESEARCHEDC_DB_NAME
RESEARCHEDC_HOME
RESEARCHEDC_PROFILE
RESEARCHEDC_SECRET_KEY
```

### 8.4 数据卷命名

```yaml
volumes:
  researchedc-db-data:
  researchedc-app-data:
  researchedc-logs:
```

---

## Phase 5：数据库与配置迁移

### 9.1 数据库名称

开发环境：

```text
research_edc_dev
```

测试环境：

```text
research_edc_test
```

生产环境：

```text
research_edc_prod
```

### 9.2 数据库表名是否立即修改

不建议第一阶段大规模修改数据库表名。

原因：

1. OpenClinica 的旧代码可能存在大量 SQL、ORM、JSP、导出逻辑和报表逻辑依赖旧表名。
2. 表名重命名会显著增加迁移风险。
3. 与 package / repo 命名相比，数据库结构迁移更容易破坏历史数据兼容性。

建议策略：

- 第一阶段：只修改数据库名称、连接配置、初始化脚本名称。
- 第二阶段：建立数据库迁移工具，例如 Flyway 或 Liquibase。
- 第三阶段：再决定是否逐步重命名表、字段、索引和约束。

### 9.3 数据迁移工具

推荐优先选择：

```text
Flyway
```

迁移脚本目录：

```text
src/main/resources/db/migration
```

示例：

```text
V001__baseline_openclinica_schema.sql
V002__research_edc_config_rename.sql
V003__add_randomization_module.sql
V004__add_survey_module.sql
```

---

## Phase 6：UI 与系统显示名称迁移

### 10.1 页面标题

将页面标题中的 OpenClinica 替换为 ResearchEDC。

搜索：

```bash
grep -RIn "OpenClinica\|openclinica" src/main/webapp frontend public . \
  --exclude-dir=.git \
  --exclude-dir=node_modules \
  --exclude-dir=target
```

### 10.2 登录页与页脚

建议页脚写法：

```text
ResearchEDC — derived from OpenClinica v3.x. Not an official OpenClinica release.
```

如果内部使用，也可以简化为：

```text
ResearchEDC Clinical Research Data Platform
```

但 README / NOTICE 中仍应保留完整来源说明。

### 10.3 国际化文件

检查：

```text
messages.properties
messages_zh_CN.properties
messages_en_US.properties
*.json
*.yml
```

推荐中文名称：

```text
ResearchEDC 科研电子数据采集平台
```

或：

```text
ResearchEDC 临床研究数据管理平台
```

---

## Phase 7：许可证与合规文档更新

### 11.1 保留 LICENSE

保留原 LGPL 许可文件。建议文件名：

```text
LICENSE
COPYING.LESSER
```

如果项目中已经存在原始 LGPL 文本，不要删除。

### 11.2 新增或更新 NOTICE

推荐 `NOTICE` 内容：

```text
ResearchEDC includes modified code derived from OpenClinica v3.x.

Original project: OpenClinica
Original license: GNU Lesser General Public License (LGPL), version 2.1 or later
Original source: https://github.com/OpenClinica/OpenClinica

This modified distribution has been renamed to ResearchEDC to avoid confusion with the official OpenClinica project. The renaming does not alter the license obligations for code derived from OpenClinica.

Files derived from OpenClinica remain subject to the GNU LGPL. See MODIFICATIONS.md for a summary of changes.

OpenClinica is a trademark of its respective owner. ResearchEDC is not an official OpenClinica release and is not affiliated with or endorsed by OpenClinica.
```

### 11.3 更新 MODIFICATIONS.md

在 `MODIFICATIONS.md` 中新增一节：

```markdown
## Package and Repository Renaming

Date: 2026-05-19  
Modified by: [Your Name / Institution]

The project repository, application name, package namespace, Docker service names, and user-facing product name were renamed from OpenClinica-derived identifiers to ResearchEDC-related identifiers.

Purpose:

- To avoid confusion with the official OpenClinica project
- To establish an independent project identity
- To support long-term technical refactoring and research-specific customization

This renaming does not alter the license obligations for code derived from OpenClinica. Such files remain licensed under the GNU LGPL, version 2.1 or later.
```

### 11.4 新增 THIRD_PARTY_NOTICES.md

用于记录新增依赖：

```markdown
# Third-Party Notices

This file records third-party dependencies added during the ResearchEDC refactoring process.

| Component | Version | License | Purpose |
|---|---:|---|---|
| Spring Boot | TBD | Apache-2.0 | Backend modernization |
| PostgreSQL Driver | TBD | BSD-2-Clause | Database connectivity |
| React | TBD | MIT | Frontend modernization |
| SurveyJS | TBD | MIT / Commercial depending on usage | Survey module |
| Flyway | TBD | Apache-2.0 | Database migration |
```

---

## Phase 8：Git 历史与 fork 记录策略

### 12.1 是否保留 Git 历史

建议保留 Git 历史，至少在内部主仓库中保留。

优点：

- 能证明项目来源和修改过程。
- 便于审计 LGPL 合规。
- 便于回滚和定位问题。

如果后续创建干净的新 repo，也建议保留：

```text
UPSTREAM.md
MODIFICATIONS.md
NOTICE
```

### 12.2 UPSTREAM.md 示例

```markdown
# Upstream Source

ResearchEDC was originally derived from OpenClinica v3.x.

- Upstream project: OpenClinica
- Upstream repository: https://github.com/OpenClinica/OpenClinica
- Upstream license: GNU LGPL, version 2.1 or later
- Initial fork/import date: [YYYY-MM-DD]
- ResearchEDC rename date: [YYYY-MM-DD]

This repository contains substantial modifications and refactoring. See `MODIFICATIONS.md` for details.
```

---

## Phase 9：测试与验证

### 13.1 编译验证

Maven：

```bash
mvn clean test
mvn clean package -DskipTests
```

Gradle：

```bash
./gradlew clean test
./gradlew build
```

### 13.2 静态搜索验证

确认不应出现的 OpenClinica 品牌引用：

```bash
grep -RIn "OpenClinica\|openclinica\|OPENCLINICA" . \
  --exclude-dir=.git \
  --exclude-dir=target \
  --exclude-dir=node_modules \
  --exclude=LICENSE \
  --exclude=COPYING.LESSER \
  --exclude=NOTICE \
  --exclude=README.md \
  --exclude=MODIFICATIONS.md \
  --exclude=UPSTREAM.md
```

保留出现的位置应主要集中在：

```text
README.md
NOTICE
MODIFICATIONS.md
UPSTREAM.md
LICENSE / COPYING.LESSER
```

### 13.3 启动验证

Docker Compose：

```bash
docker compose up -d --build
```

检查：

```bash
docker compose ps
docker compose logs -f researchedc-backend
docker compose logs -f researchedc-db
```

### 13.4 功能验证清单

| 模块 | 验证内容 | 状态 |
|---|---|---|
| 登录 | 管理员和普通用户登录 | 待验证 |
| 研究项目 | 创建 study / site | 待验证 |
| 受试者 | 筛选、入组、编号 | 待验证 |
| CRF | 创建、填写、保存、锁定 | 待验证 |
| 导出 | CSV / ODM / 数据字典导出 | 待验证 |
| 权限 | 角色访问控制 | 待验证 |
| 审计 | 操作日志、修改记录 | 待验证 |
| Docker | 首次部署、重启、备份恢复 | 待验证 |

---

## Phase 10：提交策略

建议不要把所有改动压成一个 commit。推荐拆分：

```text
chore: rename project to ResearchEDC
chore: update README, NOTICE, and upstream attribution
refactor: rename Java package namespace to org.researchedc
chore: update Maven coordinates for ResearchEDC
chore: update Docker service and image names
refactor: update UI display name and i18n labels
chore: add modification record for ResearchEDC rename
```

每个 commit 应可独立 review，避免后续无法定位问题。

---

## 14. 推荐目录结构

中期目标结构：

```text
research-edc/
├── LICENSE
├── COPYING.LESSER
├── NOTICE
├── README.md
├── MODIFICATIONS.md
├── UPSTREAM.md
├── THIRD_PARTY_NOTICES.md
├── CHANGELOG.md
├── docker-compose.yml
├── Dockerfile
├── docs/
│   ├── architecture.md
│   ├── deployment.md
│   ├── lgpl-compliance.md
│   ├── migration-from-openclinica.md
│   └── study-workflow.md
├── scripts/
│   ├── backup.sh
│   ├── restore.sh
│   └── rename-audit.sh
├── config/
│   ├── researchedc.env.example
│   └── nginx/
├── backend/
│   └── src/main/java/org/researchedc/
├── frontend/
│   └── package.json
├── db/
│   └── migration/
└── modules/
    ├── randomization/
    ├── survey/
    ├── acupuncture-study/
    ├── neurophysiology-metadata/
    └── export/
```

---

## 15. 后续模块命名建议

ResearchEDC 作为主项目名，功能模块建议采用以下命名：

| 功能 | 模块名 | package 示例 |
|---|---|---|
| 核心 EDC | `researchedc-core` | `org.researchedc.core` |
| Web/API | `researchedc-api` | `org.researchedc.api` |
| 受试者管理 | `researchedc-subject` | `org.researchedc.subject` |
| CRF 管理 | `researchedc-crf` | `org.researchedc.crf` |
| 随机化系统 | `researchedc-randomization` | `org.researchedc.randomization` |
| 问卷系统 | `researchedc-survey` | `org.researchedc.survey` |
| 针刺研究扩展 | `researchedc-acupuncture` | `org.researchedc.acupuncture` |
| 神经生理数据 | `researchedc-neurodata` | `org.researchedc.neurodata` |
| 数据导出 | `researchedc-export` | `org.researchedc.export` |
| 审计追踪 | `researchedc-audit` | `org.researchedc.audit` |

---

## 16. 风险与控制措施

| 风险 | 说明 | 控制措施 |
|---|---|---|
| 误删许可证信息 | 重构时删除上游 LGPL / copyright | 保留 LICENSE、NOTICE、MODIFICATIONS.md |
| 商标混淆 | 使用 OpenClinica 作为产品名 | 改名为 ResearchEDC，并加非官方声明 |
| 全局替换破坏代码 | 直接 sed 替换导致 XML / 反射 / SQL 出错 | 分阶段迁移，使用 IDE refactor + grep 校验 |
| 数据库兼容性破坏 | 重命名表和字段导致旧逻辑失效 | 第一阶段不改表名，只改配置和数据库名称 |
| Docker 部署失败 | 环境变量和服务名不同步 | 建立 `.env.example` 和 compose 验证清单 |
| LGPL 合规不完整 | 分发镜像但未提供源码 | 在 release 中附源码链接和合规说明 |
| 第三方依赖许可证冲突 | 新增依赖许可证不兼容 | 建立 THIRD_PARTY_NOTICES.md |

---

## 17. 最小可执行清单

如果只想先完成安全命名迁移，最低限度需要完成：

1. repo 改名为 `research-edc`。
2. README 标题改为 `ResearchEDC`。
3. README 增加来源说明和非官方声明。
4. 保留 LGPL LICENSE。
5. 新增或更新 NOTICE。
6. 更新 MODIFICATIONS.md，记录 ResearchEDC 命名迁移。
7. Docker 镜像名和 compose 服务名改为 `researchedc-*`。
8. UI 显示名从 OpenClinica 改为 ResearchEDC。
9. package 命名分阶段迁移为 `org.researchedc`。
10. 使用 grep 确认 OpenClinica 只保留在来源、许可证和修改说明文档中。

---

## 18. 建议执行顺序

推荐顺序：

```text
1. 文档与合规记录先行
2. Repo / README / NOTICE / MODIFICATIONS.md 命名迁移
3. Docker / 环境变量 / 部署名称迁移
4. UI 显示名称迁移
5. Build 配置迁移
6. Java package / namespace 迁移
7. 测试与修复
8. 模块化重构
9. 随机化、问卷、针刺研究、神经生理数据模块开发
```

不要一开始就同时改 package、数据库、前端、Docker 和业务逻辑。命名迁移应与业务重构分开提交。

---

## 19. 推荐第一批 Issue

```text
[Rename] Rename repository and product identity to ResearchEDC
[Compliance] Add NOTICE, UPSTREAM.md, and update MODIFICATIONS.md
[Docs] Update README with origin and LGPL license notes
[Docker] Rename Docker services and environment variables
[UI] Replace user-facing OpenClinica labels with ResearchEDC
[Build] Rename Maven/Gradle coordinates
[Refactor] Rename Java package namespace to org.researchedc
[Test] Add post-rename smoke tests
[Audit] Generate OpenClinica reference audit report
```

---

## 20. README 推荐开头

```markdown
# ResearchEDC

ResearchEDC is an independently maintained research electronic data capture and clinical research data management platform derived from OpenClinica v3.x.

The project is designed for investigator-initiated clinical studies, including support for electronic case report forms, subject management, study workflows, data export, and future extensions such as randomization, survey integration, acupuncture clinical trial workflows, and neurophysiological data metadata management.

## Origin and License

ResearchEDC is derived from OpenClinica v3.x. OpenClinica is distributed under the GNU Lesser General Public License (LGPL), version 2.1 or later.

This project has been renamed to avoid confusion with the official OpenClinica project. The renaming does not alter the license obligations for code derived from OpenClinica. Files derived from OpenClinica remain licensed under the GNU LGPL, version 2.1 or later.

See `NOTICE`, `UPSTREAM.md`, and `MODIFICATIONS.md` for attribution and modification records.

## Trademark Notice

OpenClinica is a trademark of its respective owner. ResearchEDC is not an official OpenClinica release and is not affiliated with, endorsed by, or sponsored by OpenClinica.
```

---

## 21. 结论

将项目重命名为 **ResearchEDC** 是合理的技术与合规选择。

该名称具有以下优点：

1. 与 OpenClinica 官方品牌保持距离，降低商标和来源混淆风险。
2. 保持科研 EDC 平台定位，不局限于针刺或单一研究类型。
3. 适合后续扩展随机化、问卷、审计、CRF、神经生理数据和临床研究数据导出模块。
4. 便于建立独立 repo、package、Docker 镜像和长期维护路线。

关键注意点是：**改名只用于避免品牌和商标混淆，不改变 OpenClinica 衍生代码的 LGPL 许可义务。**
