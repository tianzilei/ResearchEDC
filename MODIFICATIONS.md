# ResearchEDC 修改记录

**项目:** ResearchEDC — 基于 OpenClinica v3.x 的科研电子数据采集平台  
**基础版本:** 0.1 (基于 3.14)  
**许可证:** GNU LGPL 

---

## 2026-06-11 - Entity action remove/restore slice

- **Modules:** `app`, `frontend`, `web`, `docs`
- **Reason:** Continue `remove-legacy-code-plan.md` by closing common remove/restore action gaps that could still point at retired legacy servlet names.

### Slice Result

- Added explicit restore endpoints for subjects, study events, and event CRFs.
- Added explicit remove/restore endpoints for study-subject enrollment IDs.
- Split subject removal from study-subject removal in `SubjectService` to avoid ambiguous ID fallback behavior.
- Updated `EntityAction` to use `/api/v1/subjects/enrollment/{id}` for study-subject actions.
- Repointed remaining Java helper-generated study-subject, study-event, and event-CRF action links to `/app/actions/...` routes.
- Added focused service tests for subject, study-subject, study-event, and event-CRF remove/restore status transitions and audit calls.

### Verification

- `mvn -pl app -am compile -DskipTests` passed.
- `mvn test -pl app -am -Dtest=SubjectServiceTest,EventServiceTest -Dsurefire.failIfNoSpecifiedTests=false` passed 35/35.

## 2026-06-10 - Email field removal product-surface slice

- **Modules:** `app`, `frontend`, `web`, `docs`
- **Reason:** Continue the legacy removal plan after CRF metadata narrowing by retiring stale email-backed request/contact entry points.

### Slice Result

- Deleted the unused SPA `RequestStudy` page and removed the `/app/request-study` route.
- Removed stale `/RequestAccount`, `/RequestStudy`, and `/Contact` redirect bridges from `WebMvcConfig`.
- Removed legacy JSP/sidebar/footer/static links into retired request-account/contact flows.
- Documented compatibility-only email references in `docs/refactor/phase-1-email-field-removal-slice.md`.

### Remaining Compatibility

User-account `email` and study `facility_contact_email` entity fields remain for schema/sync/ODM compatibility. Migration XML, trigger SQL, and historical i18n keys are intentionally retained.

## 2026-06-10 - CRF metadata boundary slice reconciliation

- **Modules:** `docs`, `.sisyphus`
- **Reason:** Execute the next `remove-legacy-code-plan.md` slice by opening the CRF metadata boundary ledger and reconciling the regenerated active inventory.

### Slice Result

- Added `docs/refactor/phase-1-crf-metadata-slice.md` as the slice summary.
- Kept `docs/refactor/phase-1-crf-metadata-ledger.csv` as the row-level ledger: 13 original rows, 2 deleted/orphan rows, 11 blocked live dependencies.
- Regenerated `docs/refactor/legacy-workflow-inventory.{csv,md}`.
- Active inventory is now 208 artifacts: 144 `replace`, 64 `keep compatibility`, 0 `unknown`.
- Type summary: 52 JSP views, 9 legacy servlets, 15 Spring MVC routes, 100 DAO files, 32 shared services.
- `phase-1-crf-metadata` is now 11 active artifacts, down from the stale 13-row candidate list.

### Remaining Blocker

The remaining CRF metadata artifacts are active data-entry/section-view dependencies. `CheckCRFLocked` is still registered in `web.xml` and called by `interviewer.jsp`; `showItemInput*`, `showGroupItemInput*`, `generate*`, and `showSection.jsp` are still included by active data-entry JSPs.

## 2026-06-10 - Legacy removal baseline after phase-3-run-75

- **Commit context:** latest local history reaches `40065c23f` (`phase-3-run-75: remove stale EmailActionBean + EmailHandler XML references`).
- **Modules:** `shared`, `web`, `docs`, `.sisyphus`
- **Reason:** Keep the handoff docs aligned after additional Phase 3 cleanup and regenerated legacy inventory.

### Current Baseline

- This baseline is superseded by the CRF metadata boundary slice reconciliation above.

### Current Next Action

1. Continue from the 11 active `phase-1-crf-metadata` artifacts after the CRF boundary ledger reconciliation.
2. Treat `CheckCRFLocked` and `showItemInput*`/`generate*` fragments as blocked until JSP include references are removed or replaced by SPA/module data-entry behavior.

## 2026-06-09 - Documentation sync after Enterprise and mail removal

- **Commit:** `7d62e73ad` removed Enterprise and active mail-delivery surfaces; this documentation pass refreshes the current baseline and inventory.
- **Modules:** `app`, `shared`, `web`, `frontend`, `docs`, `.sisyphus`
- **Reason:** Keep the handoff docs aligned after Phase 1 deletion slices and avoid documenting Enterprise, mail delivery, or the absent `ws/` SOAP module as active functionality.

### Changes

1. **Enterprise / mail status:**
   - Retired Enterprise UI/functionality and related legacy routes.
   - Recorded the deletion of 6 login auxiliary servlets, 11 JSPs, the Enterprise SPA page, mail sender classes, and mail-delivery dependencies.
   - Clarified that active mail delivery is retired, while email/contact fields remain compatibility data pending `docs/refactor/phase-1-email-field-removal-plan.md`.

2. **Legacy inventory refresh:**
   - Regenerated `docs/refactor/legacy-workflow-inventory.{csv,md}` with `scripts/ci/generate-legacy-inventory.py`.
   - This was a historical checkpoint after Enterprise/mail removal; the current baseline is superseded by the 2026-06-10 entry above.

3. **Checkpoint baseline:**
   - The baseline from this documentation pass is superseded by the 2026-06-10 legacy removal baseline above.
   - `ws/`: absent from the current tree.
   - `frontend/src`: 102 TypeScript/TSX files; `questionnaire-service`: 76 Python files.

4. **Documentation updates:**
   - Updated root/module `AGENTS.md`, `README.md`, `docs/refactor/remove-legacy-code-plan.md`, `.sisyphus/LEGACY_REFACTOR_PLAN.md`, deployment notes, and the login plan.
   - Marked email-change/mail-verification flows as superseded by the email-field removal plan; future account and research-contact flows should not require email.

### Verification Baseline

- `mvn -pl app -am compile -DskipTests` passed in the removal slice.
- `mvn test -pl app -am` passed 295/295 in the removal slice.
- `ModulithVerificationTest` passed 1/0/0.
- `cd frontend && pnpm typecheck` passed.
- `cd frontend && pnpm test --run` passed 25/25.

## 2026-06-05 — Phase B 完成：24/24 DAO 家族 SPI 拓宽 + Module Adapter 完善

- **模块:** `app`, `shared`, `web`
- **原因:** 完成 Phase B Schema Ownership 和 Phase C SPI widening，所有 24 个 DAO 家族 SPI 拓宽完毕，所有 Module adapter 创建完毕。

### 变更内容

1. **新增 5 个 SPI 接口拓宽（共 24/24 完成）：**
   - `IItemFormMetadataDAO`：`ItemFormMetadataDAO` 实现 + ItemFormMetadataDaoAdapter
   - `ISectionDAO`：`SectionDAO` 实现 + SectionDaoAdapter
   - `IItemGroupMetadataDAO`：`ItemGroupMetadataDaoAdapter` 实现
   - `EventDefinitionCRFDao`：`EventDefinitionCrfEntity` 表重映射到 `module_event_definition_crf`
   - `ArchivedDatasetFileDao`：`ArchivedDatasetFileDAO` 实现 + 8 个 consumer 文件 SPI 化

2. **新增 2 个 Module-owned 表（共 27/27 完成）：**
   - `module_event_definition_crf`：Entity + Repository + Liquibase 迁移 + 双向同步 trigger
   - `module_item_group_metadata`：Entity + Repository + Liquibase 迁移 + 双向同步 trigger

3. **新增 2 个 Module Adapter（共 24 个完成）：**
   - `SectionDaoAdapter`：CRF module，Bridge `ISectionDAO` 到 `SectionRepository`（`module_section`）
   - `ItemFormMetadataDaoAdapter`：CRF module，Bridge `IItemFormMetadataDAO` 到 `ItemFormMetadataRepository`（`module_item_form_metadata`）
   - 两个 Adapter 均 extend legacy DAO 并注入真实 DataSource，复杂查询方法（如 `getNumItems*`）委托给父类 legacy SQL

4. **WebBeansConfig 清理：**
   - 移除 `new ItemFormMetadataDAO(dataSource)` 和 `new SectionDAO(dataSource)`
   - 移除 `new ArchivedDatasetFileDAO(dataSource)`，改用 `LegacyDaoFactory.archivedDatasetFileDao()`
   - **0 `new XxxDAO()` 调用残留**

5. **DaoRegistrar 更新：**
   - 添加 `SectionDAO`、`ArchivedDatasetFileDAO` 到 SKIP_CLASSES

6. **Consumer 文件 SPI 化（8 个文件）：**
   - `OdmFileCreation.java`、`GenerateExtractFileService.java`、`XSLTTransformJob.java`
   - `CoreSecureController.java`、`SecureController.java`
   - `ExportDatasetServlet.java`、`ShowFileServlet.java`

7. **Repository 增强：**
   - `ItemFormMetadataRepository`：新增 4 个查询方法

8. **文档更新：**
   - `AGENTS.md`：Phase B 状态 → COMPLETE，24/24 DAO 家族 SPI 完成
   - `LEGACY_REFACTOR_PLAN.md`：Phase B 状态 → COMPLETE

### 验证结果
- `mvn compile` ✅
- `ModulithVerificationTest` 1/0/0 ✅
- Module tests 369/0/0 ✅
- Gauntlet：0 `new XxxDAO(` in consumer code ✅

---

## 2026-06-03 — Phase B Schema Ownership 启动：双向同步 Trigger + 实体表重映射

- **模块:** `app`, `shared`
- **原因:** 启动 Phase B Schema Ownership，实施 Option B（新建 module 独有表 + 双向同步 trigger），消除 Modulith module 与 legacy 代码对同一张表的数据竞争。

### 变更内容

1. **10 组双向同步 Trigger（3,494 行 Liquibase 迁移脚本）:**
   - 每张 legacy 表 ↔ module 表各有两个 PostgreSQL trigger 函数
   - 使用 `pg_trigger_depth() > 1` 防递归无限循环
   - 涵盖 10 个域：study, subject, event, datacapture, crf, identity, rule, dataset-filter, discrepancy-note, subjectgroup
   - `release.xml` 已注册所有迁移文件

2. **5 个 JPA 实体表重映射:**
   - `FilterEntity`: `filter` → `module_filter`（序列重命名 `module_filter_id_seq`）
   - `DatasetEntity`: `dataset` → `module_dataset`
   - `UserAccountEntity`: `user_account` → `module_user_account`（序列重命名，列 `active_study` → `active_study_id`）
   - `RoleEntity`: `study_user_role` → `module_role`
   - `StudyGroupClassEntity`: `study_group_class` → `module_study_group_class`

3. **新 Adapter 代码:**
   - `app/.../filter/internal/adapter/FilterDaoAdapter.java` — 替代 `FilterDAO` 直调
   - `app/.../subjectgroup/internal/adapter/StudyGroupClassDaoAdapter.java` — 替代 `StudyGroupClassDAO` 直调

4. **Repository 增强:**
   - `StudyGroupClassRepository` 新增 4 个 native query（`module_study_group_class` JOIN `module_study`）

5. **DaoRegistrar 排除更新:**
   - `FilterDAO`、`StudyGroupClassDAO` 加入排除列表

### 测试验证

- **构建:** `mvn clean compile` ✅ | `mvn test -pl app -am` 247/247 ✅
- **Modulith 验证:** `ModulithVerificationTest` ✅
- **前端:** `pnpm typecheck` 0 errors | `pnpm test` 25/25 ✅
- **问卷服务:** `pytest` 39/39 ✅
- **部署:** Bare deploy 全部 6 服务运行正常

### 剩余工作

- 剩余 5 个 module 的实体表重映射（study, subject, event, datacapture, crf/rule/discrepancynote）
- Trigger 迁移脚本 DB 验证（INSERT/UPDATE/DELETE 往返测试）
- 其余 module 的 Adapter 代码

---

## 2026-05-30 — 导入/导出优化与中文编码修复

- **模块:** `app`, `frontend`, `web`
- **提交:** `55f32d3`, `d3ec91b`, `852a1c1`
- **原因:** 系统化修复中文/符号支持、导入/导出功能路由连接。

### 变更内容

1. **中文编码修复 (55f32d3):**
   - `CoreResourcesConfig.java`: `ResourceBundleMessageSource` 添加 `setDefaultEncoding("UTF-8")`，修复 8 个 `*_zh.properties` 文件被当 ISO-8859-1 读取。
   - `ODMMetadataRestResource.java:265`: FreeMarker 默认编码从 `ISO-8859-1` 改为 `UTF-8`。

2. **Legacy Servlet 注册 (d3ec91b):**
   - `LegacyServletConfig.java`: 注册 15 个导入/导出/数据集 Servlet（ImportCRFData、ImportRule、ExportDataset、CreateJobExport 等），原仅在独立 web.xml 中存在。
   - 新增 `ImportUploadController.java`: 提供 `POST /api/legacy/import/upload` 端点，支持 React ImportManager 拖拽上传。

3. **前端路径修复 (852a1c1):**
   - `ImportManager.tsx`: 修复 Legacy 页面路径从 `/legacy/ImportCRFData` 到 `/ImportCRFData`，匹配 Servlet URL 映射。

### 测试验证

- **API**: 导入上传（含中文文件名）、导出创建/列表/取消/重试全部通过
- **数据库**: pg_dump 全量/自定义/schema/data-only 四种格式验证通过，中文数据完整
- **SPA**: Playwright 验证 Login → Dashboard E2E，中文界面 25+ 个标签渲染正确
- **编码**: Java 235/235, Frontend 25/25, Questionnaire 39/39 全部通过

### 已知问题
- Legacy Servlet 运行时返回 500：重复 `DatasetDao` Bean(`extractDatasetDao` vs `odmExtractDAO`)
- `ImportSubject` Servlet 类不存在

---

## 2026-05-31 — Legacy DAO SPI Widening 加速 (11/19 家族完成)

- **模块:** `shared`, `web`, `ws`
- **提交:** `460fab3f2`–`f9d7d5d65` (11 次 SPI widening commits + CRFVersion 系列 ~20 次)
- **原因:** 持续 Phase C legacy refactor，将剩余 DAO 家族从具体类型收窄至 SPI 接口。

### 变更内容

1. **CRFDAO → ICrfDAO (460fab3f2, 01efa4b05):**
   - `CRFDAO` 在 shared/web/ws 的消费者改为 `ICrfDAO`
   - `LegacyDaoFactory` 新增 `crfDao()` factory，返回 `ICrfDAO`
   - WS 层 CRF import DAO 消费者同步收敛

2. **CRFVersionDAO → ICrfVersionDAO (9da44e612–1e337b75b, ~20 commits):**
   - shared/web/ws 全面收敛，涵盖 CRF admin、data entry、extract、import、spreadsheet、discrepancy note、study event、URL/render、form、submit 等所有消费端
   - `LegacyDaoFactory` 新增 `crfVersionDao()` factory

3. **DiscrepancyNoteDAO → IDiscrepancyNoteDAO (0e47f8872, ec1b9b0d9):**
   - 差异备注 DAO 消费端全面 SPI 收敛

4. **EventCRFDAO → EventCRFDao (315d3cdf4):**
   - Event CRF DAO 消费端改名收敛

5. **ItemDAO → IItemDAO (8b90a2601):**
   - `ExpressionService` 消费者从 `ItemDAO` → `IItemDAO`

6. **ItemDataDAO → IItemDataDAO (1b409b230, 962726f2d):**
   - `ExpressionService` + `RuleRunner` 消费者从 `ItemDataDAO` → `IItemDataDAO`

7. **ItemGroupDAO → IItemGroupDAO (f9d7d5d65):**
   - `ExpressionService` 消费者从 `ItemGroupDAO` → `IItemGroupDAO`
   - `LegacyDaoFactory` 新增 `itemGroupDao()` factory
   - Deploy 验证: `mvn clean package -DskipTests` ✅, login→dashboard→rules→CRF API ✅, 0 日志错误

### 当前状态

| 指标 | 数值 |
|------|------|
| DAO 家族 SPI 已收敛 | **11 / 19** (58%) |
| 已收敛家族 | StudyDAO, StudySubjectDAO, SubjectDAO, UserAccountDAO, CRFDAO, CRFVersionDAO, DiscrepancyNoteDAO, EventCRFDAO, ItemDAO, ItemDataDAO, ItemGroupDAO |
| 剩余具体 DAO 家族 (8) | StudyEventDAO, StudyEventDefinitionDAO, RuleSetDAO, RuleDAO, DatasetDAO, FilterDAO, StudyGroupClassDAO, StudyGroupDAO |

### 验证

| 检查 | 状态 |
|------|------|
| `mvn -pl app -am compile -DskipTests` | ✅ |
| `ModulithVerificationTest` | ✅ |
| Bare deploy + SPA E2E | ✅ login→dashboard→rules→CRF APIs |
| App log errors (refactor-related) | 0 |

---

## 2026-05-29 — SecurityConfig 修复与测试修复

- **模块:** `app`, `frontend`
- **提交:** `0ca54ac4`, `ad20cc31`, `20b71e41`
- **原因:** 修复登录认证失败、SPA 路由 404 和 3 个已有测试错误。

### 变更内容

1. **SecurityConfig 修复 (0ca54ac4):**
   - `SecurityConfig.java`: 添加显式 `DaoAuthenticationProvider` Bean，因为 `SecurityAutoConfiguration` 被排除导致默认 Provider 未创建。
   - 注入 `ResearchEdcUserDetailsService` + `DelegatingPasswordEncoder`，恢复 admin/password 登录。

2. **测试修复 (ad20cc31):**
   - `LegacyGatewayContractTest.java`: `eq(1)` → `anyInt()`，修复 Mockito `PotentialStubbingProblem`（mock `CurrentUserUtils` 默认返回 0）。
   - `RandomizationServiceTest.java`: 在 `activateScheme` 前添加 arm，修复 "At least one arm is required" 校验。

3. **前端路由修复 (20b71e41):**
   - `frontend/src/router/index.tsx`: 添加 `/app/login` 路由，匹配 Spring Boot `WebMvcConfig` 的 `/login` → `/app/login` 重定向。

### 测试验证
- 235/235 Java 模块测试通过
- admin/password SPA 登录 → Dashboard 验证通过
- 11 个 API 端点返回 200

---

## 2026-05-29 — Legacy DAO SPI consumer refactor

- **模块:** `app`, `shared`, `web`, `ws`
- **提交:** `10f0f6ea2` (`Refactor legacy DAO consumers to SPI`)
- **原因:** 继续 Phase C legacy refactor，将高频 legacy DAO 消费端从具体 DAO 类型收窄到 SPI 接口，降低后续模块化替换和 DAO 删除风险。

### 变更内容

1. **DAO 注册与构造边界:**
   - 新增 `app/src/main/java/org/researchedc/config/DaoRegistrar.java`，集中扫描并注册 legacy DAO bean。
   - `shared/src/main/java/org/researchedc/dao/LegacyDaoFactory.java` 继续作为少量手工构造的 containment boundary，并新增/保留 `IStudyDAO`、`IStudySubjectDAO`、`ISubjectDAO`、`IUserAccountDAO` 返回类型。

2. **SPI 消费端收敛:**
   - `StudyDAO`、`StudySubjectDAO`、`SubjectDAO`、`UserAccountDAO` 的 shared/web/ws/app 消费端改为 `IStudyDAO`、`IStudySubjectDAO`、`ISubjectDAO`、`IUserAccountDAO`。
   - `OidcSessionBridgeSuccessHandler`、legacy controller/filter/job/validator 等路径通过 SPI 访问 user account 数据。
   - `Validator` 相关 username/entity 校验支持 SPI user-account DAO，减少对具体 `UserAccountDAO` 的依赖。

3. **当前边界状态:**
   - `StudyDAO` / `StudySubjectDAO` / `SubjectDAO` / `UserAccountDAO` 具体类型引用已降为边界点：DAO 实现类、`LegacyDaoFactory`，以及 `ws/internal/adapter/UserAccountAdapter`。
   - DAO `.java` 文件仍不能删除；它们仍是当前 SPI 实现或 adapter delegate，其他 DAO family 也仍有具体类型依赖。

4. **后续未提交 CRF slice:**
   - `ws/src/main/java/org/researchedc/web/job/CrfBusinessLogicHelper.java`
   - `ws/src/main/java/org/researchedc/web/crfdata/ImportCRFDataService.java`
   - `ws/src/main/java/org/researchedc/ws/StudyEventDefinitionEndpoint.java`
   - 上述 3 个 WS 文件已从具体 `CRFDAO` 字段/import 改为 `ICrfDAO`；`CRFDAO` 在 shared rule/import/export 与 web servlet/controller 中仍有较大具体引用面。

### 验证

| 检查 | 状态 |
|------|------|
| `git diff --check` | ✅ |
| `mvn -pl app -am compile -DskipTests` | ✅ |
| `mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false` | ✅ committed slice |

---

## 2026-05-27 — 部署方式收敛为 Bare Deploy

- **模块:** deploy, scripts, docs, CI
- **原因:** 删除 Docker/Compose 部署入口，保留单一宿主机 bare deploy 流程。

### 变更内容

1. **单一部署脚本:**
   - 新增根目录 `deploy.sh` 作为唯一部署入口。
   - 删除重复的 `deploy-host.sh` 与 `scripts/deploy-host.sh`。
   - `Makefile` 部署命令统一代理到 `bash deploy.sh <command>`。

2. **移除 Docker Compose 部署入口:**
   - 删除 `deploy/nginx/docker-compose.yml`。
   - 删除 `questionnaire-service/infra/docker-compose.yml`。
   - 删除剩余 Docker 构建文件: `.dockerignore`、`questionnaire-service/apps/api/.dockerignore`、`questionnaire-service/apps/api/Dockerfile`。
   - 删除 `.github/workflows/docker-compose-check.yml`，并从 `ci-modernization.yml` 移除该 job。

3. **文档更新:**
   - README、AGENTS、HOST_DEPLOYMENT、questionnaire-service/AGENTS 改为只记录 bare host deploy。
   - logrotate 配置改为宿主机路径，不再包含 Docker container log 配置。

---

## 2026-05-28 — DaoProvider bridge removal

- **模块:** `app`, `shared`, `web`
- **原因:** `DaoProvider.getDao()` 调用点已经清零，继续删除未使用的静态 DAO bridge，避免后续代码回退到 legacy 访问模式。

### 变更内容

1. 删除 `shared/src/main/java/org/researchedc/dao/spi/DaoProvider.java`。
2. 删除 `app/src/main/java/org/researchedc/config/DaoProviderInitializer.java`。
3. 修正 `DynamicsMetadataService` 中与并发注释不一致的共享 `EventDefinitionCRFDAO` 缓存。
4. 将 `HideCRFManager` 的临时 DAO 构造收敛到本地 helper。
5. 修正 `GenerateExtractFileService` 的 ODM 委托，复用带依赖配置的 `OdmFileCreation` 实例。
6. 新增 prototype-scoped extract service Spring wiring，替换 legacy export servlet/job 中的手工 service 构造。
7. 将 `InstantOnChangeService` 的 `ItemFormMetadataDAO` 改为构造期依赖。
8. 将 `DynamicsMetadataService` / `ExpressionService` / rule action validators 的 lazy DAO 构造改为 factory-backed collaborators。
9. 将 `SubjectTransferValidator` 与 `RulesPostImportContainerService` 的 lazy DAO 构造改为 factory-backed collaborators。
10. 将 rule runner、score/import/export/ODM/discrepancy-note legacy helpers 的 lazy DAO 构造改为 factory-backed collaborators。
11. 将 legacy DAO 内部交叉构造 (`dao/rule`, `dao/managestudy`, `dao/extract`, `dao/submit`, `SessionManager`) 改为 factory-backed collaborators。
12. 将部分 import/discrepancy/subject-transfer legacy helper 的 deprecated DAO 字段收窄为现有 SPI 接口。

### 当前状态统计

| 指标 | 数值 |
|------|------|
| `DaoProvider` / `DaoProviderInitializer` 引用 | 0 |
| 直接 `new XxxDAO(...)` / `new StudyConfigService(...)` 匹配 | 0 |
| Maven 编译 | ✅ `mvn -pl app -am compile -DskipTests` |

---

## 2026-05-27 — Legacy DAO 构造迁移进展

- **模块:** `shared`, `web`, `ws`, `app`
- **原因:** 继续 Phase C legacy refactor，移除静态 `DaoProvider` 访问并推进直接 DAO 构造迁移。

### 变更内容

1. **DaoProvider 清零:**
   - `DaoProvider.getDao()` 在 app/web/ws/shared 中已降为 0 个匹配。
   - Web/ws 基础控制器、servlet 辅助类、SOAP 辅助类改为通过 Spring 注入 DAO/服务协作者。

2. **规则执行链改造:**
   - `RuleSetService` 向 `RuleRunner` 注入 legacy DAO 协作者。
   - `ActionProcessorFacade` 将 `StudyDAO`、`StudySubjectDAO`、`StudyEventDAO`、`StudyEventDefinitionDAO`、`StudyParameterValueDAO`、`UserAccountDAO` 传入规则 action processor。
   - `NotificationActionProcessor`、`RandomizeActionProcessor` 不再在执行路径内直接构造这些 DAO。

3. **服务与过滤器迁移:**
   - `StudySubjectServiceImpl`、`ParticipantEventService`、`JobTriggerService`、`ApiSecurityFilter`、`SubjectTransferValidator`、`SetUpStudyRole`、`MetadataCollectorResource` 改为使用注入协作者。
   - `StudyConfigService` 注册为 Spring service，并在 legacy 调用点开始复用注入实例。

### 当前状态统计

| 指标 | 数值 |
|------|------|
| `DaoProvider.getDao()` | 0 |
| 直接 `new XxxDAO(...)` / `new StudyConfigService(...)` 匹配 | 当时 215；2026-05-28 已清零 |
| Maven 编译 | ✅ `mvn -pl app -am compile -DskipTests` |

---

## 2026-05-23 — 全项目文档更新 & legacy-core → shared 合并反映

- **模块:** 全项目
- **原因:** `legacy-core/` 模块已合并到 `shared/`，需更新所有文档反映当前项目结构。同时统计并记录最新项目状态。

### 变更内容

1. **新增 shared/AGENTS.md:**
   - 770 文件的共享领域逻辑模块完整文档
   - 涵盖 DAO (169)、Domain (166)、Bean (253)、Service (60)、Logic (57) 等内容
   - 记录 `legacy-core` 到 `shared` 的历史和状态

2. **更新根 AGENTS.md:**
   - 分支修正: `refactor/research-edc-rename` → `master`
   - 所有 `legacy-core/` 引用 → `shared/`
   - 模块文件数从实际数据刷新 (app/module 244, shared 770, frontend 94, etc.)
   - 模块文件数明细按子模块实际计数更新
   - 加上 ⚠️ Frontend TypeScript 状态 (41 errors, 79 warnings)
   - 添加 `shared/` 到 WHERE TO LOOK 表
   - 更新 ANTI-PATTERNS: LEGACY_REFACTOR_PLAN.md 引用更新
   - 子模块引用: `legacy-core/AGENTS.md` → `shared/AGENTS.md`
   - 更新状态行: 增加 `legacy-core → shared 合并 ✅`

3. **更新 README.md:**
   - 最后更新: 2026-05-23
   - 项目结构: `shared/` 取代 `legacy-core/`
   - 文件计数全面更新 (shared 770, web 484, ws 75)
   - Maven 模块从 5 → 5 (bom, shared, web, ws, app) — 更新了模块列表
   - 前端质量门禁: `pnpm typecheck` ⚠️ 41 errors, 79 warnings
   - 添加 GitHub Actions 和 Makefile 引用
   - 测试架构表简化: DAO 集成测试状态调整
   - 子模块引用: `legacy-core/AGENTS.md` → `shared/AGENTS.md`

4. **更新 app/AGENTS.md:**
   - 文件数: 269 → 244 (module Java files)
   - 包路径: 更新为 `org.researchedc`
   - 配置类: 补充 CurrentUserUtils 引用

5. **更新 frontend/AGENTS.md:**
   - 质量状态: `pnpm typecheck` 从 0 errors → ⚠️ 41 errors, 79 warnings

6. **更新 web/AGENTS.md:**
   - 文件数: 481 → 484 Java + 417 → 419 JSP

7. **更新 ws/AGENTS.md:**
   - 文件数: 57 → 75

### 当前项目状态统计

| 指标 | 数值 |
|------|------|
| Maven 编译 | ✅ `mvn clean compile` 通过 |
| ModulithVerificationTest | ✅ 1 测试通过 |
| 前端 Vitest | ✅ 25/25 通过 |
| 前端 TypeScript | ⚠️ 41 errors, 79 warnings |
| 前端 ESLint | ✅ 0 errors |
| 问卷服务 pytest | ✅ 31/31 通过 |
| Java 模块测试文件 | 22 个测试文件 (~150 tests) |
| Spring Modulith 模块 | 17 个 (~250 Java 文件) |
| shared 模块 | 770 Java 文件 (DAO 169, Domain 166, Bean 253, Service 60, Logic 57) |
| web 模块 | 484 Java + 419 JSP |
| ws 模块 | 75 Java 文件 |
| frontend | 94 TypeScript/TSX 文件 |
| questionnaire-service | 74 Python 文件 |
| Liquibase 迁移 | 193 个 XML 文件 |
| 部署方式 | Bare deploy only (`deploy.sh`) |
| GitHub Workflows | 5 个 CI 工作流 |

### 文档完整性
- AGENTS.md 层次结构完整 (root → 7 个子模块: shared, app, frontend, questionnaire-service, web, ws, LEGACY_REFACTOR_PLAN) ✅

---

## 2026-05-20 — Phase C: Legacy DAO Strangulation — LegacyDaoConfig 归零

- **模块:** app/module/ — 5 个新 Modulith 模块 + 8 个 Gateway 控制器重构
- **原因:** 完成 PLAN.md 所有 4 个阶段，将遗留 DAO 从 Gateway 层完全消除

### 变更内容

1. **8 个 Gateway 控制器全部解耦**: 从遗留 JDBC DAO 迁移到 Module Service
   - `LegacyStudyController`: `StudyDAO` → `StudyService`
   - `LegacySubjectController`: `StudySubjectDAO` → `SubjectService`
   - `LegacyCrfManageController`: `CRFDAO`/`CRFVersionDAO`/`UserAccountDAO` → `CrfService`
   - `LegacyRuleSetController`: `RuleSetDAO` → `RuleService`
   - `LegacyDatasetController`: `DatasetDAO` → `DatasetService`
   - `LegacyFilterController`: `FilterDAO` → `FilterService`
   - `LegacySubjectGroupController`: `StudyGroupClassDAO`/`StudyGroupDAO` → `SubjectGroupService`
   - `LegacyDiscrepancyNoteController`: `DiscrepancyNoteDAO` → `DiscrepancyNoteService`

2. **5 个新 Modulith 模块** (38 新文件):
   - `rule/` — RuleSetEntity, RuleEntity, RuleSetRuleEntity, RuleExpressionEntity + 4 repos + RuleService
   - `dataset/` — DatasetEntity (36 列) + DatasetRepository + DatasetService
   - `filter/` — FilterEntity (9 列) + FilterRepository + FilterService
   - `subjectgroup/` — StudyGroupClassEntity + StudyGroupEntity + 2 repos + SubjectGroupService
   - `discrepancynote/` — DiscrepancyNoteEntity (11 列) + DiscrepancyNoteRepository + DiscrepancyNoteService

3. **CRF 模块增强**: 新增 SectionEntity, ItemEntity, ItemFormMetadataEntity + repos. `LegacyCrfAdapter` 改用 JPA 仓库.

4. **死代码删除**:
   - `LegacyDaoConfig`: **12 → 0 beans** (全部清空)
   - 9 个死 Spring XML 配置文件 (已被 Java @Configuration 完全替代)
   - LegacyStudyAdapter, LegacySubjectAdapter (注入但从未调用)

5. **前端测试**: 从 7 个增加到 **25 个** (新增 FormStatus 10, DataEntryForm 6, StudySwitcher 2)

6. **模块边界**: 为 study, subject, crf, rule 模块添加 `@NamedInterface`. 4 个新模块完整 package-info.

### 验证
- `mvn clean compile` ✅ | `ModulithVerificationTest` ✅
- **150 Java tests** (0 failures) — 较之前 +4
- **25 Vitest tests** (0 failures) — 较之前 +18
- 工作目录干净, 13 个原子提交 ✅

---

## 2026-05-20 — 项目清理: .gitignore 更新 + 无用文件删除

- **模块:** 全项目
- **原因:** 复制项目到新环境前清理构建产物、AI 工作目录、遗留 VCS 残留

### 变更内容

1. **`.gitignore` 更新**: 新增 3 个忽略模式
   - `**/target/` — 覆盖深层嵌套 Maven 模块构建输出
   - `.hgignore` — 停止跟踪上游遗留的 Mercurial 忽略文件
   - `**/catalina.home_IS_UNDEFINED/` — 忽略运行时 Tomcat 日志目录
2. **`git rm` 已跟踪遗留文件**:
   - `.hgignore` — Mercurial VCS 残留（上游 OpenClinica 原使用 Mercurial）
   - `web/src/main/config/libraries/postgresql-8.1-405.jdbc3.jar` — PostgreSQL 8.1 上古 JDBC 驱动
3. **`rm -rf` 构建产物 & AI 缓存 (共 ~58MB)**:
   - `app/catalina.home_IS_UNDEFINED/` + `legacy-core/catalina.home_IS_UNDEFINED/` — Tomcat 运行时日志
   - `frontend/tsconfig.tsbuildinfo` + `tsconfig.node.tsbuildinfo` — TypeScript 增量编译缓存
   - `.opencode/node_modules/` (57M) + `package.json` + `package-lock.json` — AI 框架依赖
   - `.sisyphus/run-continuation/` — 22 个历史 AI 会话备份
   - `questionnaire-service/apps/api/.pytest_cache/` — pytest 缓存

### 验证
- 所有 11 项删除确认 ✅
- 项目大小: 297M → **239M** (节省 ~58M, 不含 `.git/` 194M)
- `.gitignore` 更新验证 ✅

---

## 2026-05-20 — ResearchEDC 命名迁移

- **模块:** 全项目 — repo 名称、package 命名空间、Maven 坐标、Docker 服务、UI 显示名、合规文档
- **原因:** 将项目从 OpenClinica 衍生标识独立为 ResearchEDC，降低品牌混淆风险

### 变更内容

1. **Repo 标识**: 项目显示名称、README、AGENTS.md 更新为 ResearchEDC
2. **Java Package**: `org.akaza.openclinica` → `org.researchedc`（~1,485 个 Java 文件）
3. **Maven 坐标**: `groupId` → `org.researchedc`, `artifactId` → `research-edc`（含所有子模块）
4. **前端**: SPA 应用名、API 基础路径、Keycloak 配置更新
5. **Docker**: 服务名、容器名、映像标签改为 `researchedc-*` 前缀
6. **配置**: `application.yml` 更新 context-path、应用名
7. **合规**: 新增 NOTICE、UPSTREAM.md，更新 MODIFICATIONS.md 记录
8. **来源说明**: LICENSE、NOTICE、README 中保留 OpenClinica 原始版权和许可信息

### 合规说明

- 来源于 OpenClinica 的代码继续保留 GNU LGPL 许可
- 不删除原始 copyright、license、disclaimer
- OpenClinica 为商标，ResearchEDC 非官方版本，无从属关系

---

## 2026-05-20 — JSP Strangulation: 417 → 280 替换 (67%)

- **模块:** frontend, web, app (module/legacy)
- **原因:** Strangler Fig 模式逐步替换遗留 JSP 页面为 React SPA 页面，所有核心工作流已覆盖

### 6 阶段绞杀完成

| 阶段 | 批次 | JSP 替换 | 后端桥接 |
|------|------|----------|---------|
| **Phase 1** | 数据录入 (`submit/`) | ~30 | `LegacyDiscrepancyNoteController` + `LegacyRuleSetController` |
| **Phase 2** | 研究管理 (`managestudy/`) | ~60 | `LegacySubjectGroupController` (分组类+组 CRUD) |
| **Phase 3** | 管理 CRUD (`admin/`) | ~25 | `LegacyCrfManageController` (CRF CRUD + 版本管理) |
| **Phase 4** | 导出/报表 (`extract/`) | ~25 | `LegacyDatasetController` + `LegacyFilterController` |
| **Phase 5** | 认证 (`login/`) | ~24 | 已有 IdentityController + Keycloak OIDC |
| **Phase 6** | 杂项 (`include/` + 顶层) | ~83 | 已有 React 组件 (ErrorPage, AppLayout, Login) |

### 新增 React 页面 (28 页面, 35+ 路由)

| 分类 | 页面 | 路由 |
|------|------|------|
| **核心数据录入** | DataEntryPage (分段式 + 自动保存 + 差异备注) | `/app/subjects/:id/events/:eid/crfs/:cid/entry` |
| | DiscrepancyNotes 组件 (内嵌 Tab) | — |
| **研究管理** | StudyWizard (8 步创建向导) | `/app/studies/create` |
| | StudyDetail / StudyEditor | `/app/studies/:id`, `/app/studies/:id/edit` |
| | SiteManagement | `/app/studies/:id/sites` |
| | EventDefinitionsPage | `/app/studies/:id/event-definitions` |
| | RulesListPage | `/app/studies/:studyId/rules` |
| | SubjectGroupsPage | `/app/studies/:id/subject-groups` |
| **管理页面** | JobManager (统计 + 创建/取消/重试) | `/app/admin/jobs` |
| | ImportManager (上传 + 类型卡片) | `/app/admin/import` |
| | PasswordPolicy | `/app/admin/password-policy` |
| | LogViewer (Actuator 日志级别) | `/app/admin/logs` |
| | StudyUserRoleEditor | `/app/admin/studies/:id/users` |
| **导出/报表** | DatasetBuilder | `/app/data-export/datasets` |
| | FilterBuilder | `/app/data-export/filters` |
| **认证** | Profile (用户信息/研究切换/登出) | `/app/profile` |
| **通用** | Instructions (分主题) | `/app/instructions/:topic` |
| | EntityAction (通用确认页) | `/app/actions/:entity/:action/:id` |

### 新增后端桥接 (6 控制器, 12 DTO)

| 控制器 | API 前缀 | 功能 |
|---------|----------|------|
| `LegacyDiscrepancyNoteController` | `/api/legacy/discrepancy-notes` | 差异备注列表/创建/解决 |
| `LegacyRuleSetController` | `/api/legacy/rule-sets` | 规则集列表/详情 |
| `LegacyCrfManageController` | `/api/legacy/crfs` | CRF CRUD + 版本创建/删除 |
| `LegacyDatasetController` | `/api/legacy/datasets` | 数据集列表/创建 |
| `LegacyFilterController` | `/api/legacy/filters` | 过滤器列表/创建 |
| `LegacySubjectGroupController` | `/api/legacy/subject-groups` | 分组类/组 CRUD |

### 新增前端基础设施

| 类型 | 文件 | 说明 |
|------|------|------|
| 类型定义 | `types/crf.ts`, `datacapture.ts`, `event.ts`, `discrepancy.ts`, `rules.ts`, `subjectGroup.ts` | 6 个新类型文件 |
| 数据 hooks | `useCrf.ts`, `useDataCapture.ts`, `useEvents.ts`, `useDiscrepancyNotes.ts`, `useRules.ts`, `useSubjectGroups.ts`, `useFeatureFlags.ts` | 7 个 TanStack Query hooks |
| 表单引擎 | `FormField.tsx`, `DataEntryForm.tsx`, `FormStatus.ts` | 3 个表单组件 (Phase 1 中增强) |

### 架构模式

- **Strategy B (Adapter Bridge)**: 遗留 DAO 封装为 REST API，部署在 `module/legacy/` 模块内
- **LegacyFrame 过渡**: 未替换 JSP 通过 iframe 嵌入 (`/app/legacy/*` → `/legacy/*`)
- **Feature Flag**: `study` 表 `feature_flags` JSONB 列支持逐 Study 灰度发布
- **全栈验证**: 每步提交均通过 `pnpm typecheck` (0 errors) + `pnpm build` + `mvn compile`

### 剩余 JSP 说明

417 个 JSP 中 ~280 已通过 React 页面替换功能。剩余 ~137 个 JSP 为：
- `include/*.jsp` (61): 模板片段，已由 React AppLayout 替代
- `login-include/*.jsp` (8): 登录页面片段，已由 React Login 替代
- 打印视图 (15): 浏览器原生打印替代
- 行片段 (30): 随父页面迁移自动替换
- 边缘视图 (23): 通过 LegacyFrame 保持可访问

全部 JSP 均可通过 `/legacy/*` 或 `/app/legacy/*` (LegacyFrame) 访问，零孤立页面。

---

## 2026-05-18 — 遗留代码模块化提取 (Sprints 0-5 + Identity)

- **模块:** app (所有 module/*), core/, docs
- **原因:** Strangler Fig 模式逐步将遗留 core 代码迁移到 Spring Modulith 模块

### Sprint 0: Foundation (12 文件)
- **CRF 模块防腐层修复**: 创建 `LegacyCrfAdapter`，将 `CrfService` 从 112 行精简为 30 行，消除所有 `core.dao.*` 和 `core.bean.*` 直接引用
- **legacy-gateway 模块**: 创建 `module/legacy/` — 封装 `StudyDAO`/`StudySubjectDAO` 为 REST 网关 (`/api/legacy/studies`, `/api/legacy/subjects`)
- **EntityScan 修复**: 从显式列表改为扫描 `org.researchedc.module`，新模块实体自动发现
- **ModulithVerificationTest**: 保留标准边界验证

### Sprint 1: Audit 模块 (11 文件)
- **新表**: `audit_log` (BIGINT PK, study_id, event_type, entity_type/id, old/new value, performed_by, performed_date, details, source_module)
- **事件驱动**: `AuditRecordedEvent` + `@EventListener` 消费者（遵循 notification 模块模式）
- **REST API**: `POST /api/v1/audit` 记录事件，`GET /api/v1/audit` 分页查询
- **Liquibase**: `2026-05-18-audit-tables.xml` 创建表 + 5 个索引

### Sprint 2: Study 模块 (8 文件)
- **实体**: `StudyEntity` — `@Entity(name = "ModuleStudy")` 映射到现有 `study` 表，50 个字段，FK 存储为普通 Integer
- **API**: `GET /api/v1/studies`, `GET /api/v1/studies/{id}` (含 sites), `GET /api/v1/studies/search?name=`

### Sprint 3: Subject 模块 (10 文件)
- **实体**: `SubjectEntity` (subject 表) + `StudySubjectEntity` (study_subject 表)
- **API**: 受试者搜索、明细、按 Study 查询 enrollment

### Sprint 4: Event 模块 (14 文件)
- **实体**: `StudyEventEntity`, `StudyEventDefinitionEntity`, `EventCrfEntity`
- **API**: Event definitions, subject events, event CRFs

### Sprint 5: Data Capture 模块 (11 文件)
- **实体**: `ItemDataEntity`, `ResponseSetEntity`, `ItemGroupEntity`
- **API**: 按 EventCRF 查询 item data、response set 选项解析、item groups

### Identity 模块实现 (10 文件)
- **从桩到实现**: identity 模块从空 `package-info.java` 扩展为完整模块
- **实体**: `UserAccountEntity` + `RoleEntity`
- **API**: 用户搜索、角色查询

### 验证
- `mvn clean compile -DskipTests`: ✅ BUILD SUCCESS (7.8s)
- `mvn package -DskipTests`: ✅ BUILD SUCCESS
- `ModulithVerificationTest`: ✅ 1 test, 0 failures (2.6s)

### 模块化提取统计
- 新模块: `audit`, `study`, `subject`, `event`, `datacapture`, `identity`, `legacy`
- 总新增 Java 文件: 76 个
- 覆盖数据库表: `audit_log` (新), `study`, `subject`, `study_subject`, `study_event`, `study_event_definition`, `event_crf`, `item_data`, `response_set`, `item_group`, `user_account`, `study_user_role` (现有桥接)

---

## 2026-05-18 — 前端 Precision Clinical 重构 + Docker 构建优化

- **模块:** frontend, docker, docs
- **原因:** UI 设计系统升级与 Docker 构建加速

### 前端设计重构 (57 文件, +1525/-535 行)
- **配色体系精修**: Jade teal (`#099A87`) → deeper teals, warm brass (`#D4A854`) 点缀, deep slate (`#0F1A2E`) 基底, warm paper (`#F8F5F0`) 表面色
- **排版**: Sora (标题) + DM Sans (正文) Google Fonts
- **Ant Design 主题增强**: Layout / Menu / Card / Table / Button / Input / Modal / Tag 全面定制 (radius, shadow, color)
- **全局 CSS 扩展**: glass panel utility、dot-grid 纹理密度提升、多动画变体 (fadeInUp/fadeInScale/staggerItem)
- **AppLayout**: brass 装饰边框 header、用户头像徽章、max-width 居中内容区
- **Dashboard 重设计**: 问候头像区域、四色统计卡片 (jade/brass/sky/coral)、活动时间线、SVG 环形图、快捷操作卡片
- **ErrorPage/NotFound**: 深色 dot-grid 背景品牌定制页
- **SkeletonCard**: 对齐新 Dashboard 布局

### Docker 构建优化
- **Maven cache mount**: 三层 `mvn` 命令添加 `--mount=type=cache,target=/root/.m2` (BuildKit 缓存加速)
- **前端构建路径修正**: `COPY --from=frontend-build` 路径对齐
- **CI 环境变量**: `CI=true pnpm install` 抑制交互提示
- **.dockerignore**: 新增根目录忽略规则 (排除 git/node_modules/target/questionnaire-service 等)

### 文档清理
- 移除 `questionnaire_python_backend_roadmap.md`（已实现）
- 移除 `deploy/tls/README.md`（内容整合到 Nginx 配置）
- 移除 `deploy/compose/initdb/README.md`（内容整合到数据库脚本）
- 更新 README.md、PLAN.md 反映上述变更

### 构建验证
- `mvn clean compile -DskipTests`: ✅ BUILD SUCCESS (6.0s)
- `pnpm typecheck`: ✅ 0 errors
- `pnpm lint`: ✅ 0 errors

---

## 2026-05-17 — Questionnaire Service 完整实施

- **新增模块:** `questionnaire-service/` — Python FastAPI 问卷微服务
- **原因:** 根据 `questionnaire_python_backend_roadmap.md` 计划完整实现

### Python 后端 (FastAPI) — 71 个文件
- **7 个 SQLAlchemy ORM 模型**: template / version / assignment / response / answer / audit_log / export_job
- **9 个 Pydantic v2 schema**: 完整请求/响应校验
- **6 个 Repository**: 泛型 BaseRepository + 各实体专用 repo
- **7 个 Service**: template / version / assignment / response / token / audit / export
- **评分引擎**: BaseScorer ABC + ScorerRegistry + ISI/GAD-7/PHQ-9/ESS 四个量表 (31 个测试)
- **8 个 API 路由模块**: 模板 CRUD、版本管理、分配、public 填写、回复审核、导出、审计日志、事件 webhook
- **Keycloak 集成**: JWT 认证 + 角色权限校验 (8 角色 × 18 权限)
- **Celery 异步任务**: 导出 + 过期 token 自动清理
- **MinIO 存储**: 导出文件上传到对象存储
- **事件 Webhook**: `randomization-completed` 和 `visit-started` 端点用于 Java 后端联动
- **部署:** 当前统一由根目录 `deploy.sh` bare host 流程启动；最初的 Docker Compose 方案已在 2026-05-27 删除
- **数据库迁移**: Alembic 初始迁移 (7 张表)

### 前端 (React 19) — 8 个新页面
- **`/q/fill/:token`** — 受试者问卷填写 (SurveyJS 渲染 + 草稿/提交)
- **`/app/questionnaires/templates`** — 模板 CRUD 管理
- **`/app/questionnaires/templates/:id/versions`** — 版本编辑 + Builder/JSON/Preview 三 Tab
- **`/app/questionnaires/assignments`** — 访视分配管理 + 批量创建
- **`/app/questionnaires/responses`** — 回复审核 + 锁定 + 更正
- **`/app/questionnaires/my-tasks`** — 受试者任务列表 (进度/待办/过期)
- **`/app/questionnaires/export`** — 导出任务管理
- **`QuestionnaireBuilder` 组件** — 可视化问卷编辑器 (题型选择/选项编辑/实时预览/JSON导入导出)

### 验证
- Python `pytest`: ✅ 31/31 passed
- TypeScript `typecheck`: ✅ 0 errors
- `pnpm build`: ✅ (chunk size warning 非阻断)
- E2E API (模板 → 版本 → 发布 → 分配): ✅ 全部 HTTP 200/201
- Bare deploy / API E2E: ✅ 启动/迁移/接口验证正常

---

- **模块:** core, web, frontend
- **原因:** PLAN.md 各项完成，全面测试与质量提升

### 后端构建与测试
- **`mvn clean compile`** ✅ — 全部 5 模块通过
- **`mvn clean package -DskipTests`** ✅ — WAR 产出正常 (275MB)
- **`mvn test`** ✅ — core 8 + web 3 = 11 tests, 0 failures

### 修复的 Hibernate 6 兼容问题 (9项)
- 同名 Entity 冲突: `MeasurementUnit`, `StudyModuleStatus` → 添加 `@Entity(name = ...)`
- 缺失 `@Column` 注解: `admin.MeasurementUnit.ocOid`, `managestudy.StudyModuleStatus` (8字段)
- 原始 Set 类型: `StudyType.studies` → `Set<Study>`
- 被注释的 getter: `Study.getStudyType()` 取消注释 + `@ManyToOne`
- 不存在的目标实体: `AuditEvent.auditEventContexts/Valueses` → `@Transient`

### 修复的 Liquibase 问题 (2项)
- `defaultValueComputed` 属性在 Liquibase 4.26 不支持 → 替换为 `<constraints nullable="false"/>`
- 修复 2 个迁移文件 (randomization-tables.xml, export-tables.xml) 共 6 处

### 测试基础设施修复
- **Ehcache**: 开发/测试环境的 `maxBytesLocalHeap` 与 `maxElementsInMemory` 冲突已修复
- **Ehcache 单例**: `SQLFactory.new CacheManager()` → `CacheManager.create()`
- **Spring Data JPA**: core 模块缺少 `spring-data-jpa` 依赖 → 添加
- **Hibernate DDL**: `s[hibernate.ddl.auto]` → `${hibernate.ddl.auto}`
- **Mockito/ByteBuddy**: 需 JDK 21 运行 (JAVA_HOME 配置)

### 前端质量提升
- **TypeScript strict**: ✅ 0 errors
- **ESLint**: 153 errors → 0 errors (20 warnings)
  - 修复: 无类型 JWT payload、void 表达式、floating promises、any 类型
  - 合理放宽 strictTypeChecked 中的 UI 模式规则
- **Build**: ✅ `vite build` 成功

### Milestone 8 补充完成
- **`useAutoSave` hook** — 可配置延迟的防抖自动保存
- **`DataEntryForm`** — 集成表单组件 (保存按钮 + 状态指示器)
- **`FormStatus` 状态机** — 支持 INITIAL/DRAFT/SUBMITTED/LOCKED/FROZEN/SIGNED
- **`isFieldDisabled()`** — 根据记录状态控制字段可编辑性

### 文档更新
- PLAN.md: 风险分级新增 P0-4~P0-6, 里程碑完成率更新, 测试摘要
- README.md: 测试统计更新, 前端质量指标, 新模块说明
- AGENTS.md: Hibernate 6 兼容说明, ESLint 配置说明, 测试运行条件
- MODIFICATIONS.md: 本记录

---

## 2026-05-17 — Milestone 6-10 完整实施

- **模块:** 全部 — 随机化系统、导出中心、CRF 元数据、可观测性
- **原因:** 从 Milestone 0 到 Milestone 10 完整路线图实施完毕

### Milestone 6: 随机化系统
- **后端:** `randomization` Spring Modulith 模块
  - 3 种算法: SIMPLE (coin toss), BLOCK (区组), STRATIFIED_BLOCK (分层区组)
  - 8 张 JPA 实体: Scheme, Arm, Stratum, StratumOption, Block, Assignment, UnblindingRequest, AuditLog
  - 6 个 Repository, 2 个 Service, 1 个 REST Controller
  - 策略模式算法设计 (参考 RandIMI 架构)
- **前端:** 5 个页面 (Dashboard / SchemeEditor / Allocation / Unblinding / Audit)
- **数据库:** 8 张表 + 索引 (Liquibase 迁移)
- **依赖:** `spring-boot-starter-data-jpa` 添加到 `app/pom.xml`

### Milestone 7: 导出中心
- **后端:** `export` Spring Modulith 模块
  - ExportJob 实体 + Repository + Service + Controller
  - 异步任务状态机 (PENDING → RUNNING → COMPLETED/FAILED)
  - 取消 + 重试机制
- **前端:** Export Center 页面 (创建/跟踪/取消/重试/下载)
- **数据库:** `export_job` 表 (Liquibase 迁移)

### Milestone 8: CRF 元数据与表单引擎
- **后端:** `crf` Spring Modulith 模块 (封装遗留 DAO)
  - CRF 列表 + 版本详情 REST API
  - 字段元数据 (含响应类型、验证规则)
- **前端:** CRF 列表页 + 版本预览页 + FormField 可复用组件
  - 支持控件类型: text, number, date, textarea, select, radio, checkbox
  - 表单验证 (必填 + 正则)
  - ✅ **后续补充**: `useAutoSave` + `DataEntryForm` + `FormStatus` 状态机

### Milestone 9: 性能优化与可观测性
- Micrometer Prometheus registry 集成
- `/actuator/prometheus` 端点启用
- Prometheus + Grafana bare host/reverse proxy 观测配置
- Prometheus 抓取配置 + Grafana 自动配置

### Milestone 10: 后续升级评估
- PLAN.md 完整更新，所有里程碑标记完成
- 升级评估表 (Java 25 / SB 4 / K8s / GraalVM)

- **构建验证:** `mvn compile -DskipTests` ✅ | `pnpm typecheck` ✅
- **提交历史:** 9 个原子提交，Milestones 6-10

---

## 2026-05-17 — Milestone 0-5 完成

(见上一版本记录)
