# OpenClinica 后续现代化与 Legacy Code Refactor 执行计划

> 日期：2026-05-18  
> 目标仓库：GitHub 托管  
> CI/CD 方案：GitHub Actions  
> 项目基线：OpenClinica 3.18-SNAPSHOT，Spring Boot 3.2.5 + Spring Modulith + React 19 + FastAPI + Docker Compose  
> 优先级原则：**优先完成现代化与 legacy code refactor；现代化稳定后，再系统完善验证、审计、可观测性与合规门禁。**

---

## 1. 当前状态判断

当前项目已经完成第一轮基础架构债务治理，具备继续推进 legacy code refactor 的条件。已完成内容包括：

1. 依赖现代化：Commons Lang、Commons Collections、Quartz、Liquibase、Maven 插件等已升级；Javassist 已移除。
2. Spring 配置现代化：11 个 Spring XML 已迁移为 Java `@Configuration`，`@ImportResource` 已清零。
3. 遗留内核冻结：`core/` 已重命名为 `legacy-core/`，并通过 pre-commit 脚本限制非修复性改动。
4. 模块化单体初步建立：`app/` 中已有 12 个 Spring Modulith 模块，包括 study、subject、event、datacapture、crf、identity、randomization、export、audit、notification、legacy 等。
5. 写操作端点已进入 Phase A：Study、Subject、Event、DataCapture、Identity 已具备基础写 API。
6. 认证体系已统一到 Keycloak OIDC/JWT + Session 兼容模式。
7. 前端已从 WAR 内嵌资源中拆出，React/Vite 输出到 `frontend/dist/`，可由 Nginx 独立 serve。
8. 当前基础门禁通过：Maven 编译、Modulith verification、Maven Enforcer、前端 typecheck/lint/build、问卷服务 pytest 均已通过。

但当前仍存在明显 legacy 负担：

1. `legacy-core/` 仍有大量 bean、dao、domain、service、logic、job 代码。
2. `web/` 中仍有大量 SecureController 与 JSP 页面。
3. `ws/` 仍保留遗留 SOAP 服务。
4. 业务数据访问仍在较大程度上依赖 legacy DAO/Bean。
5. Ehcache 2 仍存在遗留 API 使用。
6. 部分模块虽然已有 REST API，但底层仍属于 bridge/adapter 模式，尚未完全转换为现代 application service + repository + DTO 结构。

因此下一阶段的主线不是继续零散升级依赖，而是：

> **以 GitHub Actions 为自动化执行平台，持续推进 legacy code 分层替换、DAO 迁移、JSP 绞杀、SOAP 退役、缓存替换与前端现代化。**

---

## 2. 总体目标

### 2.1 阶段总目标

在后续 6–12 个月内，将当前系统从“现代化外壳 + 遗留内核”推进到“现代化模块化单体 + 少量 legacy fallback”。

### 2.2 优先级排序

本阶段优先级应明确调整为：

| 优先级 | 方向 | 说明 |
|---|---|---|
| P0 | GitHub Actions 基础 CI | 保证每次 refactor 都能自动编译、构建、生成报告 |
| P1 | Legacy DAO/Service refactor | 将业务逻辑从 legacy DAO/Bean 迁移到模块化 application service |
| P1 | JSP 页面绞杀 | 高频业务页面逐步迁移到 React |
| P1 | SOAP 退役或 REST 替代 | 减少遗留 ws 模块维护成本 |
| P2 | Ehcache 2 替换 | 随业务模块迁移自然替换到 Caffeine |
| P2 | 构建与部署现代化 | 进一步收敛 WAR、前端、问卷服务和 Nginx/Docker Compose |
| P3 | 验证、审计、合规门禁 | 在现代化主体稳定后系统加强 |
| P3 | OpenTelemetry、Saga、Outbox | 在模块边界稳定后引入 |

### 2.3 暂不优先处理的事项

以下事项暂缓，不作为当前 refactor 的主线：

1. Database-per-Module 物理隔离。
2. 大规模微服务拆分。
3. GraalVM Native Image。
4. 全量删除 legacy DAO/Bean。
5. 一次性替换全部 JSP。
6. 一开始就做完整 GxP/临床合规级审计系统。

这些事项需要建立在现代化模块基本稳定之后。

---

## 3. GitHub Actions 总体设计

GitHub Actions 在本项目中不只是 CI 工具，而是 legacy refactor 的自动化控制面。建议将 workflow 分为三类：

1. **基础现代化工作流**：保障 refactor 不破坏编译和构建。
2. **遗留代码治理工作流**：持续输出 legacy 使用清单，防止新增反向依赖。
3. **后期质量加固工作流**：现代化完成后逐步引入测试、安全、审计和发布门禁。

### 3.1 推荐 workflow 文件结构

```text
.github/
└── workflows/
    ├── ci-modernization.yml
    ├── backend-modernization.yml
    ├── frontend-modernization.yml
    ├── questionnaire-modernization.yml
    ├── docker-compose-check.yml
    ├── legacy-refactor-report.yml
    ├── dependency-modernization.yml
    ├── security-lite.yml
    └── release-later.yml
```

### 3.2 阶段一必须启用的 workflow

第一阶段只启用最关键的 5 个：

| Workflow | 触发条件 | 目标 |
|---|---|---|
| `ci-modernization.yml` | push / pull_request | 聚合后端、前端、Python、Compose 基础检查 |
| `backend-modernization.yml` | 修改 Java/Maven 文件 | 编译 Spring Boot / Modulith / legacy-core |
| `frontend-modernization.yml` | 修改 frontend 文件 | pnpm install、typecheck、lint、build |
| `questionnaire-modernization.yml` | 修改 questionnaire-service 文件 | FastAPI 依赖安装与 pytest |
| `legacy-refactor-report.yml` | push / 手动触发 | 输出 legacy DAO/JSP/SOAP/Ehcache 使用清单 |

### 3.3 阶段二再启用的 workflow

| Workflow | 启用时机 | 目标 |
|---|---|---|
| `dependency-modernization.yml` | Maven BOM 建立后 | 依赖收敛、过期依赖报告、Dependabot PR 辅助 |
| `security-lite.yml` | Docker 构建稳定后 | Trivy、OWASP Dependency Check、pnpm audit、pip-audit |
| `docker-compose-check.yml` | Compose 文件整理后 | 验证 compose config、镜像构建、服务健康检查 |
| `release-later.yml` | 形成稳定 staging 环境后 | 构建镜像、打 tag、上传 artifact |

### 3.4 GitHub Actions 设计原则

1. workflow 不直接写复杂逻辑，复杂命令沉淀到 `scripts/ci/*.sh`。
2. 每个 workflow 只负责一个边界清晰的任务。
3. 优先使用 GitHub-hosted runner，暂不引入 self-hosted runner。
4. 优先使用 cache 加速 Maven、pnpm、pip。
5. 不在第一阶段设置过严的质量门禁，避免阻断 refactor。
6. 对 legacy 使用清单先“报告”，后续再逐步升级为“失败门禁”。

---

## 4. 推荐 GitHub Actions 基础配置

### 4.1 聚合 CI：`.github/workflows/ci-modernization.yml`

```yaml
name: CI Modernization

on:
  push:
    branches: [ master, main, develop ]
  pull_request:
    branches: [ master, main, develop ]
  workflow_dispatch:

jobs:
  backend:
    uses: ./.github/workflows/backend-modernization.yml

  frontend:
    uses: ./.github/workflows/frontend-modernization.yml

  questionnaire:
    uses: ./.github/workflows/questionnaire-modernization.yml

  compose-check:
    uses: ./.github/workflows/docker-compose-check.yml
```

### 4.2 后端 workflow：`.github/workflows/backend-modernization.yml`

```yaml
name: Backend Modernization

on:
  workflow_call:
  workflow_dispatch:
  push:
    paths:
      - 'pom.xml'
      - '**/pom.xml'
      - 'app/**'
      - 'legacy-core/**'
      - 'web/**'
      - 'ws/**'
      - 'scripts/**'

jobs:
  backend-build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: '21'
          cache: maven

      - name: Maven compile
        run: mvn -B clean compile -DskipTests

      - name: Maven verify lightweight
        run: mvn -B verify -DskipITs=true
```

### 4.3 前端 workflow：`.github/workflows/frontend-modernization.yml`

```yaml
name: Frontend Modernization

on:
  workflow_call:
  workflow_dispatch:
  push:
    paths:
      - 'frontend/**'
      - 'pnpm-lock.yaml'
      - 'package.json'

jobs:
  frontend-build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Enable Corepack
        run: corepack enable

      - name: Set up Node.js
        uses: actions/setup-node@v4
        with:
          node-version: '22'
          cache: pnpm
          cache-dependency-path: frontend/pnpm-lock.yaml

      - name: Install dependencies
        working-directory: frontend
        run: pnpm install --frozen-lockfile

      - name: Typecheck
        working-directory: frontend
        run: pnpm typecheck

      - name: Lint
        working-directory: frontend
        run: pnpm lint

      - name: Build
        working-directory: frontend
        run: pnpm build
```

### 4.4 Python 问卷服务 workflow：`.github/workflows/questionnaire-modernization.yml`

```yaml
name: Questionnaire Modernization

on:
  workflow_call:
  workflow_dispatch:
  push:
    paths:
      - 'questionnaire-service/**'

jobs:
  questionnaire-test:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up Python
        uses: actions/setup-python@v5
        with:
          python-version: '3.12'
          cache: pip

      - name: Install dependencies
        working-directory: questionnaire-service
        run: |
          python -m pip install --upgrade pip
          pip install -r requirements.txt

      - name: Run tests
        working-directory: questionnaire-service
        run: pytest app/tests/ -v
```

### 4.5 Compose 检查 workflow：`.github/workflows/docker-compose-check.yml`

```yaml
name: Docker Compose Check

on:
  workflow_call:
  workflow_dispatch:
  push:
    paths:
      - 'deploy/**'
      - 'docker/**'
      - 'Dockerfile'
      - '**/Dockerfile'
      - 'compose*.yml'
      - 'docker-compose*.yml'

jobs:
  compose-check:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Validate compose files
        run: |
          docker compose -f deploy/compose.dev.yml config
```

---

## 5. Legacy Refactor 工作流设计

### 5.1 `legacy-refactor-report.yml` 目标

该 workflow 的目标不是立即阻断合并，而是持续生成 legacy 债务报告。第一阶段先作为 artifact 输出，第二阶段再逐步把部分指标升级为 failure gate。

建议报告内容：

1. legacy DAO import 数量。
2. legacy Bean 使用数量。
3. JSP 文件数量。
4. SecureController 子类数量。
5. SOAP endpoint 数量。
6. Ehcache 2 API 使用位置。
7. `javax.*` 残留使用。
8. 直接访问 legacy 表的模块位置。
9. 新增 legacy 依赖趋势。
10. 每个模块对 `module/legacy` 的调用次数。

### 5.2 示例 workflow

```yaml
name: Legacy Refactor Report

on:
  push:
    branches: [ master, main, develop ]
  pull_request:
    branches: [ master, main, develop ]
  workflow_dispatch:

jobs:
  legacy-report:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Generate legacy report
        run: |
          mkdir -p build/reports/legacy
          bash scripts/ci/generate-legacy-report.sh

      - name: Upload legacy report
        uses: actions/upload-artifact@v4
        with:
          name: legacy-refactor-report
          path: build/reports/legacy/
```

### 5.3 `scripts/ci/generate-legacy-report.sh` 示例

```bash
#!/usr/bin/env bash
set -euo pipefail

REPORT_DIR="build/reports/legacy"
mkdir -p "${REPORT_DIR}"

{
  echo "# Legacy Refactor Report"
  echo
  echo "Generated at: $(date -u)"
  echo

  echo "## Legacy DAO imports"
  grep -R "org.akaza.openclinica.*dao" app web ws legacy-core --include='*.java' || true
  echo

  echo "## Legacy Bean imports"
  grep -R "org.akaza.openclinica.*bean" app web ws legacy-core --include='*.java' || true
  echo

  echo "## JSP files"
  find web -name '*.jsp' | sort || true
  echo

  echo "## SecureController classes"
  grep -R "extends SecureController" web --include='*.java' || true
  echo

  echo "## SOAP classes"
  find ws -name '*.java' | sort || true
  echo

  echo "## Ehcache usage"
  grep -R "net.sf.ehcache\|Ehcache\|CacheManager" app web ws legacy-core --include='*.java' || true
  echo

  echo "## javax residuals"
  grep -R "import javax\." app web ws legacy-core --include='*.java' || true
} > "${REPORT_DIR}/legacy-report.md"
```

---

## 6. Legacy Code Refactor 总体路线

### 6.1 Refactor 总原则

1. 不直接修改大量 legacy 代码，而是通过 adapter、防腐层、模块迁移逐步替换。
2. 不追求一次性删除 legacy-core，而是按业务闭环减少对 legacy 的调用。
3. 新代码禁止继续向 legacy DAO/Bean 扩散。
4. 每完成一个业务模块迁移，都要减少对应 JSP/SOAP/DAO 的使用入口。
5. 优先迁移高频业务链路，而不是低频后台管理页面。
6. 保持数据库 schema 初期兼容，避免过早进行物理拆分。

### 6.2 推荐迁移顺序

按业务价值与依赖关系，建议顺序为：

```text
Subject → Study/Site → Event → CRF Metadata → Data Capture → Query → Randomization → Questionnaire → Export → Identity/Admin → SOAP retirement
```

但考虑你已经实现 randomization 和 questionnaire 的部分现代能力，实际执行时可以采用两条线并行：

```text
主线 A：Subject → Event → Data Capture
主线 B：Randomization → Questionnaire → Export
```

这两条线最终汇合为完整临床研究业务闭环。

---

## 7. 分阶段执行计划

## Phase 0：GitHub Actions 与 Refactor 基线建立

周期：1–2 周  
目标：建立自动化 refactor 基线，让每次修改都有可追踪的构建结果和 legacy 债务报告。

### 任务 0.1：新增 GitHub Actions 基础工作流

交付物：

```text
.github/workflows/ci-modernization.yml
.github/workflows/backend-modernization.yml
.github/workflows/frontend-modernization.yml
.github/workflows/questionnaire-modernization.yml
.github/workflows/docker-compose-check.yml
.github/workflows/legacy-refactor-report.yml
```

验收标准：

1. push 和 pull request 可自动触发。
2. 后端、前端、问卷服务、Docker Compose 均可独立检查。
3. legacy report 可作为 artifact 下载。
4. workflow 失败时能明确定位到 backend/frontend/questionnaire/compose 其中一个区域。

### 任务 0.2：整理 CI 脚本

交付物：

```text
scripts/ci/backend-build.sh
scripts/ci/frontend-build.sh
scripts/ci/questionnaire-test.sh
scripts/ci/docker-compose-check.sh
scripts/ci/generate-legacy-report.sh
```

验收标准：

1. 本地执行脚本与 GitHub Actions 执行逻辑一致。
2. 脚本使用 `set -euo pipefail`。
3. 复杂逻辑不散落在 YAML 中。

### 任务 0.3：建立 legacy 基线指标

交付物：

```text
docs/refactor/legacy-baseline.md
build/reports/legacy/legacy-report.md
```

指标包括：

1. legacy-core Java 文件总数。
2. DAO 文件数量。
3. Bean 文件数量。
4. domain entity 数量。
5. service/logic/job 数量。
6. JSP 文件数量。
7. SecureController 数量。
8. SOAP Java 文件数量。
9. Ehcache 使用位置。
10. 新模块中直接引用 legacy 的位置。

验收标准：

1. 可以明确知道当前 legacy code 的规模。
2. 后续每个阶段都能对比 legacy 债务是否减少。

---

## Phase 1：构建体系与 Maven 结构继续现代化

周期：2–4 周  
目标：让 Maven 结构、依赖管理、构建输出更适合长期 refactor。

### 任务 1.1：建立 Maven BOM

目标：创建 `openclinica-dependencies` 或在 parent pom 中集中管理版本。

建议管理：

1. Spring Boot 相关版本尽量由 Spring Boot BOM 接管。
2. Spring Modulith 版本集中管理。
3. Testcontainers 版本集中管理。
4. Jackson、Liquibase、Quartz、Caffeine、MapStruct、Lombok、OpenAPI 版本集中管理。
5. 禁止模块 pom 中随意声明版本号。

交付物：

```text
pom.xml
openclinica-dependencies/pom.xml 或 dependencyManagement section
```

验收标准：

1. `mvn -B dependency:tree` 无明显版本冲突。
2. Maven Enforcer 增加 dependency convergence 检查。
3. 新增依赖必须进入 BOM 或 parent 管理。

### 任务 1.2：收敛 Maven profile

目标：减少历史 profile、Tomcat/Cargo 旧式部署配置、遗留 assembly 配置的影响。

建议保留：

```text
dev
ci
staging
prod
legacy-compatible
```

验收标准：

1. CI 使用固定 profile。
2. 本地开发使用 dev profile。
3. legacy 兼容逻辑显式声明，不再隐式混入主构建。

### 任务 1.3：构建产物现代化评估

当前 `app/` 是 Spring Boot modular monolith，但仍是 WAR。建议不要立即切换到 executable JAR，因为 JSP 与 legacy web 仍在。建议分两步：

第一步：保持 WAR，但清理 WAR 内嵌前端静态资源。  
第二步：JSP 大幅减少后，再评估切换为 executable JAR + 独立 React/Nginx。

交付物：

```text
docs/architecture/packaging-strategy.md
```

验收标准：

1. 明确 WAR 保留原因。
2. 明确切换 executable JAR 的前置条件。
3. 不在当前阶段强行切换导致 JSP 破坏。

---

## Phase 2：模块 Application Service 与 Legacy Adapter 重构

周期：1–3 个月  
目标：把新模块从“REST bridge”推进到“现代 application service + repository + adapter”。

### 任务 2.1：统一模块内部结构

建议每个核心模块采用以下结构：

```text
module/subject/
├── api/
│   ├── SubjectController.java
│   └── dto/
├── application/
│   ├── SubjectApplicationService.java
│   └── command/
├── domain/
│   ├── SubjectPolicy.java
│   └── SubjectDomainService.java
├── infrastructure/
│   ├── SubjectRepository.java
│   └── LegacySubjectAdapter.java
└── event/
    └── SubjectEnrolledEvent.java
```

先不强制所有模块完全 DDD 化，但必须做到：

1. Controller 不直接访问 DAO。
2. Controller 不直接访问 Hibernate Session。
3. Controller 不直接拼装复杂业务逻辑。
4. legacy DAO 只能被 adapter 调用。
5. application service 是事务边界。

### 任务 2.2：Subject 模块优先迁移

Subject 是后续随机化、访视、数据采集、问卷的核心依赖，应优先现代化。

改造内容：

1. `POST /api/v1/subjects` 改为调用 `SubjectApplicationService.createSubject()`。
2. `POST /api/v1/subjects/enroll` 改为调用 `SubjectApplicationService.enrollSubject()`。
3. 抽离 legacy `SubjectDAO` 访问到 `LegacySubjectAdapter`。
4. 定义现代 DTO：`SubjectCreateRequest`、`SubjectResponse`、`SubjectEnrollRequest`。
5. 明确 study subject 与 subject 的关系。
6. 为后续 randomization 预留 subject enrollment 状态字段。

交付物：

```text
app/src/main/java/.../module/subject/application/
app/src/main/java/.../module/subject/infrastructure/
app/src/main/java/.../module/subject/api/dto/
docs/refactor/subject-refactor.md
```

验收标准：

1. Subject API 不再直接暴露 legacy Bean。
2. Subject controller 无 legacy DAO import。
3. Subject 模块保留 legacy adapter，但 adapter 是唯一 legacy 访问点。
4. React 前端可基于新 DTO 调用。

### 任务 2.3：Study/Site 模块迁移

Study/Site 是所有权限和业务上下文的根，应在 Subject 后处理。

改造内容：

1. 统一 `studyId`、`siteId`、`studySubjectId` 命名。
2. `StudyApplicationService` 承担 study 创建、更新、状态变更。
3. `SiteApplicationService` 可独立或作为 Study 子能力。
4. legacy study DAO 进入 `LegacyStudyAdapter`。
5. 定义 study context API，供前端 AppShell 使用。

验收标准：

1. 前端能从现代 API 获取当前用户可访问 study/site 列表。
2. 后续页面不再依赖 JSP session 中的 study context。
3. Study controller 不直接使用 legacy Bean。

### 任务 2.4：Event 模块迁移

Event 是数据采集前置条件，应在 Study/Subject 后处理。

改造内容：

1. `EventApplicationService.createEvent()`。
2. `EventApplicationService.completeEvent()`。
3. 统一 event definition、study event、event CRF 的 DTO 表达。
4. 分离 event schedule 与 event occurrence。
5. 为 React event 页面提供列表、详情、完成操作 API。

验收标准：

1. Event 创建与完成不再直接出现在 Controller 中。
2. Event API 可支撑 React 页面替换 JSP。
3. DataCapture 可以通过现代 Event API 获取上下文。

### 任务 2.5：CRF Metadata 模块迁移

CRF 是 OpenClinica 的复杂核心，不建议一次性重写。应先做 metadata read model。

改造策略：

1. 先只做 CRF 元数据读取，不做 CRF designer 全量重写。
2. 建立 CRF schema DTO。
3. 统一 section、item group、item、response set 的 JSON 表达。
4. 为 DataCapture 页面提供现代 metadata API。
5. CRF designer 留到 JSP 第二批绞杀。

验收标准：

1. React DataCapture 页面可以通过 REST 获取 CRF metadata。
2. DataCapture 不再解析 JSP 或 legacy form 输出。
3. CRF 元数据 DTO 与 legacy Bean 解耦。

### 任务 2.6：DataCapture 模块迁移

DataCapture 是高频页面，应作为第一批 JSP 绞杀目标。

改造内容：

1. `DataCaptureApplicationService.saveItemData()`。
2. `DataCaptureApplicationService.saveBatch()`。
3. 引入 item value validation pipeline。
4. 分离保存草稿与提交完成。
5. 批量保存支持幂等 key，但幂等机制可在后续完善阶段加强。
6. 暂不一次性重写 query management，但保存时保留 query hook。

验收标准：

1. React 页面可以完成最小数据录入闭环。
2. 批量保存 API 与 CRF metadata API 对齐。
3. Controller 不直接访问 item_data DAO。
4. 原 JSP 数据录入页面可以保留 fallback。

---

## Phase 3：JSP 绞杀与 React 页面替换

周期：2–5 个月  
目标：从高频业务页面开始，把遗留 JSP 逐步替换为 React 页面。

### 任务 3.1：建立 React Hybrid Shell

Hybrid Shell 作为过渡层，统一承载 React 页面和 legacy JSP iframe。

功能：

1. 登录态读取。
2. 当前 study/site context。
3. 左侧导航。
4. 权限判断。
5. React route。
6. Legacy iframe route。
7. 面包屑。
8. 错误页。
9. Loading 状态。

目录建议：

```text
frontend/src/app/
├── AppShell.tsx
├── StudyContextProvider.tsx
├── PermissionGuard.tsx
├── LegacyFrame.tsx
└── routes.tsx
```

验收标准：

1. 用户登录后默认进入 React Shell。
2. 未迁移页面通过 iframe 打开 JSP。
3. 已迁移页面使用 React route。
4. 前端不再依赖 WAR 内嵌静态资源。

### 任务 3.2：第一批替换页面

第一批页面应优先选择高频且已具备 API 支撑的页面。

顺序建议：

| 顺序 | 页面 | 原因 |
|---|---|---|
| 1 | Subject list/detail/create/enroll | 业务入口，依赖清晰 |
| 2 | Randomization assignment | 已有随机化模块，现代化价值高 |
| 3 | Event list/create/complete | 数据采集前置流程 |
| 4 | DataCapture minimal entry | 核心业务价值最高 |
| 5 | Questionnaire assignment/submit/score | 已有 FastAPI 服务，可形成现代闭环 |
| 6 | Export center | 相对独立，适合异步任务化 |

验收标准：

1. 以上页面可以从 React 入口完成主要操作。
2. JSP fallback 保留但不作为默认入口。
3. 每替换一个页面，legacy report 中对应 JSP 访问入口减少。

### 任务 3.3：前端 API 类型生成

建议基于 OpenAPI 生成 TypeScript 类型。

实现建议：

```text
frontend/src/api/generated/
```

命令示例：

```bash
pnpm openapi-typescript http://localhost:8080/v3/api-docs -o src/api/generated/openapi.d.ts
```

验收标准：

1. 前端不手写后端 DTO 类型。
2. DTO 变更能在 typecheck 阶段暴露。
3. React hooks 基于生成类型封装。

---

## Phase 4：SOAP 退役与 REST 替代

周期：2–4 个月，可与 Phase 3 并行  
目标：逐步减少 `ws/` 模块负担。

### 任务 4.1：SOAP endpoint 清单化

先不要直接删除 SOAP。应先生成清单：

1. endpoint 名称。
2. 对应业务能力。
3. 当前是否仍被外部系统调用。
4. 是否有 REST 替代。
5. 是否可以废弃。
6. 废弃风险。

交付物：

```text
docs/refactor/soap-retirement-inventory.md
```

### 任务 4.2：REST 替代策略

分类处理：

| SOAP 类型 | 处理方式 |
|---|---|
| Study/Subject/Event 查询 | 用现有 REST API 替代 |
| 数据录入 | 用 DataCapture REST API 替代 |
| 导出 | 用 Export REST API 替代 |
| 用户/角色 | 用 Identity REST API 替代 |
| 无使用记录 | 标记 deprecated，后续删除 |

### 任务 4.3：兼容期策略

1. SOAP 保留只读或兼容模式。
2. 新功能禁止加入 SOAP。
3. 文档标记 deprecated。
4. GitHub Actions report 中持续统计 SOAP 代码量。

验收标准：

1. 每个 SOAP endpoint 都有处理结论。
2. 至少 50% SOAP 能力有 REST 替代。
3. 新增业务能力只允许 REST/API 模块实现。

---

## Phase 5：Ehcache 2 → Caffeine 渐进替换

周期：1–2 个月  
目标：随着业务迁移自然替换遗留缓存，而不是全局硬替换。

### 任务 5.1：缓存使用清单

通过 legacy report 输出：

1. `net.sf.ehcache` import。
2. `CacheManager` 使用位置。
3. cache name。
4. 缓存对象类型。
5. 是否属于高频路径。

### 任务 5.2：引入 Spring Cache + Caffeine

建议在新模块中统一使用：

```java
@Cacheable
@CacheEvict
@CachePut
```

配置放入：

```text
app/src/main/java/.../config/CacheConfig.java
```

迁移策略：

1. 新模块只允许使用 Spring Cache abstraction。
2. 不允许新代码直接 import Ehcache。
3. legacy 模块暂时保留 Ehcache。
4. 被迁移的业务路径同步替换缓存实现。

验收标准：

1. 新模块无 Ehcache import。
2. Caffeine 可支持 CRF metadata、study context、permission read model 缓存。
3. Ehcache 使用点随 legacy code 减少。

---

## Phase 6：现代化完成后的验证、审计与质量门禁

周期：现代化主线稳定后启动  
目标：在主要 legacy refactor 完成后，系统增强验证、审计、安全和可观测性。

这一阶段不作为当前最优先事项，但需要预留设计空间。

### 任务 6.1：测试门禁增强

增强顺序：

1. 单元测试。
2. Spring Modulith 模块测试。
3. Testcontainers PostgreSQL 集成测试。
4. Keycloak JWT 测试。
5. 前端组件测试。
6. Playwright E2E。

建议最先覆盖业务链路：

```text
Subject create → enroll → randomization → event create → data capture → questionnaire submit → export
```

### 任务 6.2：审计系统增强

现代化稳定后，再将 audit 从“事件记录模块”提升为“全局合规基础设施”。

覆盖：

1. Study 变更。
2. Subject 变更。
3. Randomization 分配。
4. DataCapture 保存。
5. Questionnaire 提交。
6. Export 下载。
7. 用户和角色变更。

### 任务 6.3：安全门禁增强

GitHub Actions 中逐步加入：

1. OWASP Dependency Check。
2. Trivy image scan。
3. pnpm audit。
4. pip-audit。
5. CodeQL。
6. Secret scanning。
7. SBOM 生成。

### 任务 6.4：可观测性增强

在 Java、React、FastAPI 三端统一：

1. request id。
2. trace id。
3. structured logging。
4. OpenTelemetry。
5. Prometheus metrics。
6. Grafana dashboard。

---

## 8. 分支与 PR 策略

### 8.1 分支建议

```text
master 或 main：稳定主分支
develop：现代化集成分支
refactor/subject-module
refactor/study-module
refactor/event-module
refactor/datacapture-module
refactor/react-shell
refactor/soap-retirement
refactor/cache-caffeine
```

### 8.2 PR 规则

第一阶段建议不要设置过度严格门禁，只要求：

1. Maven compile 通过。
2. 前端 typecheck/lint/build 通过。
3. Python pytest 通过。
4. Docker compose config 通过。
5. legacy report 成功生成。

第二阶段再增加：

1. Modulith verification 必须通过。
2. 禁止新增 legacy DAO import。
3. 禁止新增 JSP 页面。
4. 禁止新 SOAP endpoint。
5. 禁止新代码使用 Ehcache API。

第三阶段再增加：

1. Testcontainers 集成测试。
2. 安全扫描。
3. OpenAPI breaking change 检查。
4. 审计覆盖检查。

---

## 9. Legacy Refactor 指标体系

建议每两周统计一次。

| 指标 | 初始状态 | 目标 |
|---|---:|---:|
| legacy-core Java 文件数 | 记录基线 | 持续下降 |
| legacy DAO 文件数 | 记录基线 | 优先下降 |
| legacy Bean 暴露到 API 的数量 | 记录基线 | 清零 |
| SecureController 数量 | 记录基线 | 逐步下降 |
| JSP 文件数量 | 记录基线 | 逐步下降 |
| SOAP endpoint 数量 | 记录基线 | 大幅下降 |
| Ehcache import 数量 | 记录基线 | 清零 |
| 新模块直接 import legacy DAO 数量 | 记录基线 | 清零 |
| React 替换页面数量 | 记录基线 | 持续上升 |
| REST 替代 SOAP 能力数量 | 记录基线 | 持续上升 |

---

## 10. 推荐里程碑

### Milestone 1：GitHub Actions 基线完成

时间：第 1–2 周

交付：

1. 基础 workflow 可运行。
2. Maven/pnpm/pytest/compose 检查通过。
3. legacy report 可下载。
4. CI 脚本本地与远端一致。

### Milestone 2：Subject/Study/Event 模块完成现代化骨架

时间：第 1–2 个月

交付：

1. SubjectApplicationService。
2. StudyApplicationService。
3. EventApplicationService。
4. Legacy adapter 收口。
5. Controller 不再直接访问 legacy DAO。
6. React Shell 可读取 study/site context。

### Milestone 3：第一批 React 页面替换

时间：第 2–4 个月

交付：

1. React AppShell。
2. Subject 页面。
3. Randomization 页面。
4. Event 页面。
5. DataCapture 最小页面。
6. JSP fallback 保留。

### Milestone 4：CRF Metadata 与 DataCapture 闭环

时间：第 3–6 个月

交付：

1. CRF metadata REST API。
2. DataCapture batch save API 稳定。
3. React 数据录入页面可用。
4. CRF designer 暂不强行重写。

### Milestone 5：SOAP 与 Ehcache 明显收缩

时间：第 5–8 个月

交付：

1. SOAP endpoint 清单。
2. 高优先级 SOAP REST 替代。
3. 新模块全面禁止 Ehcache。
4. Caffeine 支撑新模块缓存。

### Milestone 6：验证、审计、安全门禁增强

时间：第 8–12 个月

交付：

1. Testcontainers 集成测试。
2. 审计事件统一。
3. 安全扫描 workflow。
4. OpenTelemetry 初步接入。
5. OpenAPI breaking change 检查。

---

## 11. 当前最建议立即执行的 10 个任务

1. 新增 `.github/workflows/backend-modernization.yml`。
2. 新增 `.github/workflows/frontend-modernization.yml`。
3. 新增 `.github/workflows/questionnaire-modernization.yml`。
4. 新增 `.github/workflows/legacy-refactor-report.yml`。
5. 编写 `scripts/ci/generate-legacy-report.sh`。
6. 生成 `docs/refactor/legacy-baseline.md`。
7. 重构 Subject 模块为 Controller → ApplicationService → Adapter 结构。
8. 建立 React AppShell 与 LegacyFrame。
9. 设计 Subject/Study/Event 的现代 DTO。
10. 形成 `docs/refactor/subject-refactor.md` 作为第一个模块迁移样板。

---

## 12. 最终建议

本项目当前已经完成基础现代化，下一步不应把主要精力放在审计、Testcontainers、OpenTelemetry 等后期增强项上。更合理的路径是：

```text
GitHub Actions 自动化基线
→ legacy report 持续量化
→ Subject/Study/Event/DataCapture 模块 refactor
→ React Shell + 高频 JSP 页面替换
→ SOAP/Ehcache 收缩
→ 现代化主体稳定
→ 再系统补充验证、审计、安全和可观测性
```

一句话概括：

> **先用 GitHub Actions 保障 refactor 可持续推进；先完成 legacy code 的现代化替换；再逐步把系统提升到高验证、高审计、高可观测的临床研究平台。**
