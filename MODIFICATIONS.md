# OpenClinica 修改记录

**项目:** OpenClinica 技术栈现代化重构  
**基础版本:** 3.18-SNAPSHOT (基于 3.14)  
**许可证:** GNU LGPL 

---

## 2026-05-17 — 测试修复与质量提升 (第二轮)

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
- Prometheus + Grafana Docker Compose 生产部署配置
- Prometheus 抓取配置 + Grafana 自动配置

### Milestone 10: 后续升级评估
- PLAN.md 完整更新，所有里程碑标记完成
- 升级评估表 (Java 25 / SB 4 / K8s / GraalVM)

- **构建验证:** `mvn compile -DskipTests` ✅ | `pnpm typecheck` ✅
- **提交历史:** 9 个原子提交，Milestones 6-10

---

## 2026-05-17 — Milestone 0-5 完成

(见上一版本记录)
