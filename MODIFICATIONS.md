# OpenClinica 修改记录

**项目:** OpenClinica 技术栈现代化重构  
**基础版本:** 3.18-SNAPSHOT (基于 3.14)  
**许可证:** GNU LGPL 

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
