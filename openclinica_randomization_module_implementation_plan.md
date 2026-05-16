# OpenClinica 重构版随机化系统实现计划

**文档版本:** v1.0  
**生成日期:** 2026-05-17  
**目标项目:** OpenClinica 现代化重构版  
**参考项目:** `imi-ms/RandIMI`、`ttscience/unbiased`  
**建议实现形态:** EDC 内嵌随机化模块，边界按未来可独立服务设计  
**推荐第一版范围:** 预生成随机化表 + 分层区组随机化 + 盲法权限 + 审计 + 紧急揭盲  

---

## 1. 设计结论

建议在 OpenClinica 重构版中新增独立模块：

```text
backend/
├── oc-randomization/
│   ├── randomization-domain/
│   ├── randomization-application/
│   ├── randomization-infrastructure/
│   └── randomization-api/
```

第一版不要直接做完整 RTSM/IWRS，不要做药物库存、药盒补货、温控物流、复杂平台试验随机化。更稳妥的路线是先实现：

```text
1. 随机化计划管理
2. 预生成随机化表导入
3. 分层区组随机化执行
4. 受试者随机化状态管理
5. 盲态/非盲权限隔离
6. 完整审计日志
7. 紧急揭盲
8. 随机化平衡报告
```

核心技术判断：

```text
不要把 RandIMI 或 unbiased 原样嵌入。
应参考它们的设计点，然后在 OpenClinica 重构架构中实现自有 oc-randomization 模块。
```

原因：

1. OpenClinica 重构版后端目标是 Java / Spring Boot / PostgreSQL / Liquibase。
2. RandIMI 是 Java 生态，适合参考领域模型、API 设计和多中心随机化思路。
3. unbiased 是 R 生态，适合参考 API 化随机化服务、minimization 算法、审计接口和 eCRF 集成思路。
4. 临床随机化模块必须深度接入 OpenClinica 的 Study、Site、Subject、Event、CRF、权限、审计、导出和电子签名体系。
5. 随机化执行必须支持数据库事务、行级锁、分配隐藏、盲法保护、唯一约束和完整审计。
6. 随机化模块不能只作为“算法函数库”，必须作为“受控临床业务流程”实现。

---

## 2. 参考项目分析

### 2.1 RandIMI 可参考点

RandIMI 是一个面向临床研究的随机化平台，定位是为单中心或多中心研究提供 Web 与 API 接口，将受试者分配到预配置研究臂。其 README 显示它支持 blocked randomization、minimization 和 coin toss 三类随机化算法，并提供 Docker、PostgreSQL、Flyway、Java 17、Tomcat 10 等部署/开发方式。

建议参考：

| RandIMI 设计点 | 是否采用 | 在 OpenClinica 中的实现方式 |
|---|---:|---|
| 多中心研究支持 | 采用 | Study + Site + Stratum 绑定 |
| study arm 预配置 | 采用 | RandomizationArm |
| blocked randomization | 采用 | PermutedBlock / StratifiedPermutedBlock |
| minimization | 第二阶段采用 | Pocock-style Minimization |
| coin toss | 可选 | SimpleRandomization |
| API 用户与普通用户区分 | 采用 | API token / service account |
| API versioning | 采用 | `/api/v1/randomization/**` |
| stratum definition endpoint | 采用 | 查看分层变量定义 |
| subject randomization endpoint | 采用 | `POST /subjects/{subjectId}/randomize` |
| Flyway migration 思路 | 部分采用 | OpenClinica 重构版仍建议统一 Liquibase |
| Tomcat WAR 部署 | 不采用 | Spring Boot executable jar / container |
| Thymeleaf UI | 不采用 | React + TypeScript |
| 独立随机化平台 | 暂不采用 | 第一版作为模块化单体内嵌模块 |

### 2.2 unbiased 可参考点

unbiased 是一个临床试验随机化 API 项目，强调生产可用 REST API、PostgreSQL 集成、eCRF/EDC 集成、审计接口、Docker Compose、测试和代码覆盖。它支持 simple、block 和 adaptive minimization randomization，并提供 study creation、patient randomization、study list、study details、randomization list、audit log 等 API 方向。

建议参考：

| unbiased 设计点 | 是否采用 | 在 OpenClinica 中的实现方式 |
|---|---:|---|
| REST API first | 采用 | OpenAPI + Spring Boot Controller |
| Study creation with randomization parameters | 采用 | RandomizationPlan 创建 |
| Patient randomization endpoint | 采用 | Subject Randomization API |
| Audit log endpoint | 采用 | 接入 oc-audit |
| Randomization list endpoint | 采用但限权 | 仅非盲授权角色可见 |
| PostgreSQL | 采用 | 与 OpenClinica 主库一致 |
| Docker Compose 测试 | 采用 | Testcontainers + docker compose |
| minimization algorithm | 第二阶段采用 | 单独算法实现与验证 |
| R runtime | 不采用 | 用 Java 重写算法或作为测试参考 |
| temporal_tables extension | 不强制采用 | 审计由 oc-audit 统一实现 |
| Sentry | 可选 | 后续接入 observability |

### 2.3 参考后的设计原则

```text
RandIMI 适合参考“临床随机化业务平台”。
unbiased 适合参考“API 化随机化服务与算法接口”。
OpenClinica 重构版应实现“EDC 内嵌随机化模块”，但模块边界要足够清晰，后续可以拆成独立服务。
```

---

## 3. 随机化模块在 OpenClinica 中的位置

### 3.1 与现有重构模块关系

```text
oc-study
  ↓
oc-subject
  ↓
oc-randomization
  ↓
oc-audit
  ↓
oc-security
  ↓
oc-export
```

随机化模块依赖：

```text
必需依赖：
- oc-study
- oc-site
- oc-subject
- oc-security
- oc-audit

弱依赖：
- oc-event
- oc-crf
- oc-rule-engine
- oc-export

不应强依赖：
- 具体 CRF 页面实现
- 旧 JSP/Servlet
- 旧 SOAP endpoint
```

### 3.2 推荐集成方式

第一阶段采用内嵌模块：

```text
OpenClinica Spring Boot Backend
├── Study API
├── Subject API
├── CRF API
├── Audit API
└── Randomization API
```

后续如果需要拆成独立服务，可以改为：

```text
OpenClinica Backend
  ↓ REST / internal API
Randomization Service
  ↓
PostgreSQL / 独立 schema
```

为了未来可拆分，第一天就应避免：

1. 在 subject service 中直接写随机化算法。
2. 在 CRF 保存逻辑中直接分配治疗组。
3. 在随机化表里直接写 OpenClinica 旧 Bean 对象。
4. 随机化模块直接调用 JSP/Servlet。
5. 将真实 treatment arm 泄露给普通 Subject 页面。

---

## 4. 功能范围

### 4.1 MVP 范围

MVP 必须包含：

```text
1. 随机化计划 RandomizationPlan
2. 治疗臂 RandomizationArm
3. 分层变量 StratificationFactor
4. 分层定义 RandomizationStratum
5. 随机化表 AllocationList
6. 分配槽 AllocationSlot
7. 受试者随机化 Assignment
8. 紧急揭盲 EmergencyUnblinding
9. 随机化审计 RandomizationAudit
10. 随机化平衡报告 BalanceReport
```

### 4.2 MVP 支持的随机化方法

第一版建议支持：

```text
PREGENERATED_LIST
STRATIFIED_PERMUTED_BLOCK
SIMPLE_RANDOMIZATION, 可选
```

第二版再支持：

```text
PERMUTED_BLOCK_GENERATOR
MINIMIZATION_POCOCK
MINIMIZATION_WEIGHTED
```

第三版再考虑：

```text
RESPONSE_ADAPTIVE_RANDOMIZATION
PLATFORM_TRIAL_RANDOMIZATION
MULTI_STAGE_RANDOMIZATION
RTSM_KIT_ASSIGNMENT
```

### 4.3 不纳入 MVP

第一版不建议实现：

```text
1. 药物库存管理
2. 药盒号自动分配
3. 中心补货
4. 温控物流
5. 多阶段平台试验
6. Bayesian response-adaptive randomization
7. 与药房系统直接集成
8. 随机化后自动处方
9. 独立手机端随机化
10. 匿名 survey randomization
```

---

## 5. 关键临床与合规要求

### 5.1 随机化报告要求

系统必须保存并可导出以下信息，以支持 CONSORT/SPIRIT 要求：

```text
1. 随机序列生成方法
2. 随机化类型
3. 限制性随机化细节，例如 blocking
4. 分配隐藏机制
5. 谁生成随机序列
6. 谁入组受试者
7. 谁执行分配
8. 是否分层
9. 分层因素及其取值
10. 随机化执行时间
```

### 5.2 盲法保护要求

系统必须明确区分：

```text
公开信息：
- 受试者是否已随机化
- 随机化编号
- 随机化时间
- 随机化计划名称
- 分层 key，可根据盲法策略决定是否显示

盲态信息：
- blindedLabel
- kitLabel
- groupLabel
- allocationDisplayCode

非盲信息：
- armCode
- armName
- treatmentName
- complete allocation list
- block size
- random seed
- minimization probability
```

普通研究者和数据录入人员不应看到真实 treatment arm。

### 5.3 审计要求

所有随机化相关操作必须写审计：

```text
1. 创建随机化计划
2. 修改随机化计划
3. 导入随机化表
4. 验证随机化表
5. 批准随机化计划
6. 激活随机化计划
7. 锁定随机化计划
8. 执行受试者随机化
9. 查询随机化结果
10. 查看非盲信息
11. 导出随机化表
12. 紧急揭盲
13. 撤销/停用随机化计划
14. 系统错误和重试
```

### 5.4 数据完整性要求

必须满足：

```text
1. 一个受试者在同一随机化计划下只能随机化一次。
2. 一个 allocation slot 只能被使用一次。
3. 已激活计划的核心配置不可随意修改。
4. 已使用随机化表不可删除。
5. 随机化执行必须在数据库事务中完成。
6. 并发随机化必须使用行级锁。
7. 随机化失败不能产生半完成 assignment。
8. 随机化结果不能被普通编辑操作修改。
9. 揭盲必须有原因和审计。
10. 随机化导出必须区分盲态和非盲。
```

---

## 6. 目标架构

### 6.1 后端模块结构

```text
backend/oc-randomization/
├── randomization-domain/
│   ├── model/
│   │   ├── RandomizationPlan.java
│   │   ├── RandomizationArm.java
│   │   ├── StratificationFactor.java
│   │   ├── RandomizationStratum.java
│   │   ├── AllocationList.java
│   │   ├── AllocationSlot.java
│   │   ├── RandomizationAssignment.java
│   │   └── EmergencyUnblinding.java
│   ├── value/
│   │   ├── RandomizationMethod.java
│   │   ├── BlindingMode.java
│   │   ├── PlanStatus.java
│   │   ├── StratumKey.java
│   │   └── AllocationRatio.java
│   ├── service/
│   │   ├── RandomizationPolicy.java
│   │   ├── StratumKeyResolver.java
│   │   └── AllocationConcealmentPolicy.java
│   └── event/
│       ├── RandomizationExecutedEvent.java
│       ├── RandomizationPlanActivatedEvent.java
│       └── EmergencyUnblindingEvent.java
│
├── randomization-application/
│   ├── command/
│   │   ├── CreateRandomizationPlanCommand.java
│   │   ├── ImportAllocationListCommand.java
│   │   ├── ActivateRandomizationPlanCommand.java
│   │   ├── RandomizeSubjectCommand.java
│   │   └── EmergencyUnblindCommand.java
│   ├── query/
│   │   ├── GetRandomizationPlanQuery.java
│   │   ├── GetSubjectAssignmentQuery.java
│   │   ├── GetBalanceReportQuery.java
│   │   └── GetRandomizationAuditQuery.java
│   ├── service/
│   │   ├── RandomizationPlanService.java
│   │   ├── AllocationListImportService.java
│   │   ├── RandomizationExecutionService.java
│   │   ├── EmergencyUnblindingService.java
│   │   └── RandomizationReportService.java
│   └── port/
│       ├── RandomizationPlanRepository.java
│       ├── AllocationSlotRepository.java
│       ├── RandomizationAssignmentRepository.java
│       ├── SubjectGateway.java
│       ├── StudyGateway.java
│       ├── AuditGateway.java
│       └── CryptoGateway.java
│
├── randomization-infrastructure/
│   ├── persistence/
│   │   ├── JpaRandomizationPlanRepository.java
│   │   ├── JpaAllocationSlotRepository.java
│   │   ├── JpaRandomizationAssignmentRepository.java
│   │   └── entity/
│   ├── migration/
│   │   └── liquibase/
│   ├── crypto/
│   │   ├── AesGcmCryptoGateway.java
│   │   └── KeyResolver.java
│   ├── import/
│   │   ├── CsvAllocationListParser.java
│   │   └── XlsxAllocationListParser.java
│   └── algorithm/
│       ├── SimpleRandomizationAlgorithm.java
│       ├── PermutedBlockAlgorithm.java
│       ├── StratifiedPermutedBlockAlgorithm.java
│       └── PocockMinimizationAlgorithm.java
│
└── randomization-api/
    ├── controller/
    │   ├── RandomizationPlanController.java
    │   ├── SubjectRandomizationController.java
    │   ├── RandomizationReportController.java
    │   └── EmergencyUnblindingController.java
    ├── dto/
    └── mapper/
```

### 6.2 前端模块结构

```text
frontend/web-app/src/features/randomization/
├── api/
│   ├── randomizationPlanApi.ts
│   ├── subjectRandomizationApi.ts
│   ├── balanceReportApi.ts
│   └── emergencyUnblindingApi.ts
├── pages/
│   ├── RandomizationPlanListPage.tsx
│   ├── RandomizationPlanDetailPage.tsx
│   ├── AllocationListImportPage.tsx
│   ├── RandomizationBalancePage.tsx
│   ├── SubjectRandomizationPanel.tsx
│   └── EmergencyUnblindingPage.tsx
├── components/
│   ├── PlanStatusBadge.tsx
│   ├── BlindingModeBadge.tsx
│   ├── RandomizationButton.tsx
│   ├── AssignmentResultCard.tsx
│   ├── StratumDefinitionTable.tsx
│   ├── AllocationListPreviewTable.tsx
│   └── BalanceSummaryTable.tsx
├── hooks/
│   ├── useRandomizationPlan.ts
│   ├── useRandomizeSubject.ts
│   └── useEmergencyUnblind.ts
└── types/
    └── randomization.ts
```

---

## 7. 数据模型设计

### 7.1 核心实体

```text
RandomizationPlan
- study 级别的随机化方案
- 定义随机化方法、盲法模式、分组比例、分层因素、状态和版本

RandomizationArm
- 研究臂
- 可以是真实治疗组，也可以映射到盲态 label

StratificationFactor
- 分层因素
- 例如 site、sex、severity、ageGroup

RandomizationStratum
- 一个具体分层组合
- 例如 site=01|sex=female|severity=moderate

AllocationList
- 随机化表批次
- 可以从外部导入，也可以由系统生成

AllocationSlot
- 随机化表中的一个可用分配槽
- 每个 slot 最多使用一次

RandomizationAssignment
- 受试者实际随机化结果
- 绑定 subject、plan、slot、stratum、执行人、时间

EmergencyUnblinding
- 紧急揭盲记录
- 保存原因、审批、真实组别查看记录
```

### 7.2 状态机

#### RandomizationPlanStatus

```text
DRAFT
  ↓
VALIDATED
  ↓
APPROVED
  ↓
ACTIVE
  ↓
LOCKED
  ↓
ARCHIVED
```

状态含义：

| 状态 | 含义 | 可编辑范围 |
|---|---|---|
| DRAFT | 草稿 | 全部可编辑 |
| VALIDATED | 已通过系统校验 | 可小范围编辑，编辑后回到 DRAFT |
| APPROVED | 已由统计/管理员批准 | 不允许修改核心字段 |
| ACTIVE | 已启用，可执行随机化 | 不允许修改核心字段 |
| LOCKED | 已有随机化或手动锁定 | 只允许查看 |
| ARCHIVED | 停用或研究结束 | 只读 |

#### AllocationSlotStatus

```text
AVAILABLE
RESERVED
USED
VOIDED
```

建议第一版只用 `AVAILABLE` 与 `USED`，后续再引入 `RESERVED`。

#### AssignmentStatus

```text
ASSIGNED
UNBLINDED
CANCELLED_WITH_JUSTIFICATION
```

原则上不允许取消随机化。`CANCELLED_WITH_JUSTIFICATION` 只用于极端数据修正场景，必须有管理员权限和完整审计。

### 7.3 表结构建议

#### randomization_plan

```sql
CREATE TABLE rand_randomization_plan (
    id BIGSERIAL PRIMARY KEY,
    study_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    code VARCHAR(100) NOT NULL,
    method VARCHAR(64) NOT NULL,
    blinding_mode VARCHAR(64) NOT NULL,
    allocation_ratio_json JSONB NOT NULL,
    stratification_schema_json JSONB,
    status VARCHAR(64) NOT NULL,
    version INTEGER NOT NULL DEFAULT 1,
    random_seed_encrypted TEXT,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_by BIGINT,
    updated_at TIMESTAMPTZ,
    validated_by BIGINT,
    validated_at TIMESTAMPTZ,
    approved_by BIGINT,
    approved_at TIMESTAMPTZ,
    activated_by BIGINT,
    activated_at TIMESTAMPTZ,
    locked_by BIGINT,
    locked_at TIMESTAMPTZ,
    lock_reason TEXT,
    CONSTRAINT uq_rand_plan_study_code UNIQUE (study_id, code)
);
```

#### randomization_arm

```sql
CREATE TABLE rand_randomization_arm (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES rand_randomization_plan(id),
    arm_code VARCHAR(100) NOT NULL,
    arm_name_encrypted TEXT NOT NULL,
    blinded_label VARCHAR(255),
    display_order INTEGER NOT NULL,
    is_control BOOLEAN NOT NULL DEFAULT FALSE,
    metadata_json JSONB,
    CONSTRAINT uq_rand_arm_code UNIQUE (plan_id, arm_code)
);
```

#### stratification_factor

```sql
CREATE TABLE rand_stratification_factor (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES rand_randomization_plan(id),
    factor_code VARCHAR(100) NOT NULL,
    factor_name VARCHAR(255) NOT NULL,
    source_type VARCHAR(64) NOT NULL,
    source_ref VARCHAR(255),
    data_type VARCHAR(64) NOT NULL,
    allowed_values_json JSONB,
    required BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL,
    CONSTRAINT uq_rand_factor_code UNIQUE (plan_id, factor_code)
);
```

`source_type` 推荐取值：

```text
SUBJECT_FIELD
SITE_FIELD
EVENT_FIELD
CRF_ITEM
MANUAL_INPUT
DERIVED
```

#### randomization_stratum

```sql
CREATE TABLE rand_randomization_stratum (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES rand_randomization_plan(id),
    stratum_key VARCHAR(500) NOT NULL,
    stratum_values_json JSONB NOT NULL,
    planned_size INTEGER,
    status VARCHAR(64) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uq_rand_stratum_key UNIQUE (plan_id, stratum_key)
);
```

#### allocation_list

```sql
CREATE TABLE rand_allocation_list (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES rand_randomization_plan(id),
    source_type VARCHAR(64) NOT NULL,
    file_object_id BIGINT,
    checksum_sha256 VARCHAR(128),
    imported_by BIGINT NOT NULL,
    imported_at TIMESTAMPTZ NOT NULL,
    generated_by_algorithm VARCHAR(128),
    generator_version VARCHAR(128),
    validation_status VARCHAR(64) NOT NULL,
    validation_report_json JSONB,
    row_count INTEGER NOT NULL,
    metadata_json JSONB
);
```

`source_type` 推荐取值：

```text
IMPORTED_CSV
IMPORTED_XLSX
SYSTEM_GENERATED
MIGRATED_LEGACY
```

#### allocation_slot

```sql
CREATE TABLE rand_allocation_slot (
    id BIGSERIAL PRIMARY KEY,
    plan_id BIGINT NOT NULL REFERENCES rand_randomization_plan(id),
    allocation_list_id BIGINT NOT NULL REFERENCES rand_allocation_list(id),
    stratum_key VARCHAR(500) NOT NULL,
    sequence_no INTEGER NOT NULL,
    block_no INTEGER,
    arm_code_encrypted TEXT NOT NULL,
    blinded_label VARCHAR(255),
    kit_code_encrypted TEXT,
    status VARCHAR(64) NOT NULL DEFAULT 'AVAILABLE',
    used_by_subject_id BIGINT,
    used_by_assignment_id BIGINT,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_rand_slot_sequence UNIQUE (plan_id, stratum_key, sequence_no),
    CONSTRAINT uq_rand_slot_assignment UNIQUE (used_by_assignment_id)
);
```

建议索引：

```sql
CREATE INDEX idx_rand_slot_next
ON rand_allocation_slot(plan_id, stratum_key, status, sequence_no);

CREATE INDEX idx_rand_slot_subject
ON rand_allocation_slot(used_by_subject_id);
```

#### randomization_assignment

```sql
CREATE TABLE rand_randomization_assignment (
    id BIGSERIAL PRIMARY KEY,
    study_id BIGINT NOT NULL,
    site_id BIGINT,
    subject_id BIGINT NOT NULL,
    plan_id BIGINT NOT NULL REFERENCES rand_randomization_plan(id),
    allocation_slot_id BIGINT NOT NULL REFERENCES rand_allocation_slot(id),
    randomization_no VARCHAR(100) NOT NULL,
    stratum_key VARCHAR(500) NOT NULL,
    stratum_values_json JSONB NOT NULL,
    blinded_label VARCHAR(255),
    arm_code_encrypted TEXT NOT NULL,
    randomized_by BIGINT NOT NULL,
    randomized_at TIMESTAMPTZ NOT NULL,
    eligibility_snapshot_json JSONB,
    request_id VARCHAR(128) NOT NULL,
    status VARCHAR(64) NOT NULL,
    CONSTRAINT uq_rand_assignment_subject UNIQUE (plan_id, subject_id),
    CONSTRAINT uq_rand_assignment_slot UNIQUE (allocation_slot_id),
    CONSTRAINT uq_rand_assignment_no UNIQUE (plan_id, randomization_no)
);
```

#### emergency_unblinding

```sql
CREATE TABLE rand_emergency_unblinding (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL REFERENCES rand_randomization_assignment(id),
    requested_by BIGINT NOT NULL,
    requested_at TIMESTAMPTZ NOT NULL,
    reason TEXT NOT NULL,
    approved_by BIGINT,
    approved_at TIMESTAMPTZ,
    revealed_arm_code_encrypted TEXT NOT NULL,
    revealed_to_user_id BIGINT NOT NULL,
    request_id VARCHAR(128) NOT NULL,
    status VARCHAR(64) NOT NULL,
    audit_event_id BIGINT
);
```

---

## 8. 随机化方法设计

### 8.1 方法枚举

```java
public enum RandomizationMethod {
    PREGENERATED_LIST,
    SIMPLE_RANDOMIZATION,
    PERMUTED_BLOCK,
    STRATIFIED_PERMUTED_BLOCK,
    MINIMIZATION_POCOCK,
    MINIMIZATION_WEIGHTED
}
```

### 8.2 第一版推荐执行模式

生产随机化执行时，推荐统一走 allocation slot：

```text
无论 allocation list 是外部导入还是系统生成，执行随机化时都只做：
1. 找到当前 stratum 下第一条可用 slot
2. 锁定该 slot
3. 标记为 used
4. 创建 assignment
5. 写 audit
6. 返回盲态结果
```

这样有几个好处：

1. 运行时逻辑简单。
2. 容易审计。
3. 容易并发控制。
4. 容易复现。
5. 可以隐藏完整分配表。
6. 对预生成表和系统生成表都兼容。

### 8.3 PREGENERATED_LIST

这是第一版最推荐实现的生产模式。

流程：

```text
1. 统计师在系统外生成随机化表
2. 系统管理员导入 CSV/XLSX
3. 系统做格式校验和平衡校验
4. 统计/管理员批准
5. 激活计划
6. 执行随机化时按 stratum + sequence_no 顺序取下一条 slot
```

导入文件推荐字段：

```csv
plan_code,stratum_key,sequence_no,block_no,arm_code,blinded_label,kit_code
EA_INSOMNIA_001,site=01|sex=F|severity=M,1,1,A,Group X,K001
EA_INSOMNIA_001,site=01|sex=F|severity=M,2,1,B,Group Y,K002
```

其中 `arm_code` 和 `kit_code` 导入后加密存储。

### 8.4 SIMPLE_RANDOMIZATION

适合早期 POC 或小型开放标签研究。不建议作为严肃多中心试验的默认模式。

算法：

```text
根据 allocation ratio 计算每个 arm 的权重；
使用 SecureRandom 生成随机数；
按权重区间分配 arm；
创建 allocation slot 或直接 assignment。
```

建议即使 simple randomization 也生成 slot：

```text
RandomizeSubjectCommand
  → SimpleRandomizationAlgorithm.generateOne()
  → create virtual slot
  → assignment
```

但生产模式更建议预先生成一定数量的 slot。

### 8.5 PERMUTED_BLOCK

适合保持组间人数相对平衡。注意区组大小不能暴露给执行随机化者。

算法输入：

```text
arms: A, B
ratio: 1:1
blockSizes: [4, 6, 8]
totalSize: 120
seed: encrypted
```

输出：

```text
block_no, sequence_no, arm_code
1,1,A
1,2,B
1,3,B
1,4,A
...
```

安全要求：

```text
1. block size 不显示给普通用户
2. 支持 variable block size
3. seed 加密保存
4. 分配表激活后不可修改
5. 完整分配表仅非盲授权角色可导出
```

### 8.6 STRATIFIED_PERMUTED_BLOCK

适合多中心、按性别/严重程度等因素分层。

输入：

```text
stratificationFactors:
- site
- sex
- severity

arms:
- EA
- Control

ratio:
- 1:1

blockSizes:
- 4, 6, 8

plannedSizePerStratum:
- 可手动设置，也可按总样本估计
```

执行时：

```text
1. 从 subject/site/crf 解析 stratification values
2. 生成 stratum_key
3. 在对应 stratum 下取下一条 allocation slot
```

### 8.7 MINIMIZATION_POCOCK

第二阶段实现。适合样本量较小、分层因素多、希望动态平衡的研究。

输入：

```text
arms
allocationRatio
covariates
covariateWeights
randomComponentProbability
imbalanceFunction
```

基本思路：

```text
1. 获取当前所有 assignment
2. 对候选 arm 逐一模拟加入当前 subject
3. 计算每个 arm 下的总不平衡得分
4. 选取最小不平衡 arm
5. 按 biased probability 分配，保留随机成分
```

建议：

```text
MVP 不上线 minimization。
先将 minimization 作为算法实验模块。
完成 golden master 测试和统计验证后再进入生产。
```

---

## 9. 随机化执行流程

### 9.1 标准执行流程

```text
用户点击 Randomize
  ↓
检查权限 RANDOMIZATION_EXECUTE
  ↓
检查 Study/Site/Subject 状态
  ↓
检查 subject 是否已随机化
  ↓
解析/校验分层变量
  ↓
检查 RandomizationPlan 是否 ACTIVE
  ↓
开启数据库事务
  ↓
再次检查 subject 是否已随机化
  ↓
锁定当前 stratum 下下一条 AVAILABLE allocation_slot
  ↓
更新 slot 为 USED
  ↓
创建 RandomizationAssignment
  ↓
更新 Subject randomization status
  ↓
写审计事件
  ↓
提交事务
  ↓
根据用户权限返回盲态或非盲结果
```

### 9.2 并发控制 SQL

PostgreSQL 推荐使用：

```sql
SELECT id
FROM rand_allocation_slot
WHERE plan_id = :planId
  AND stratum_key = :stratumKey
  AND status = 'AVAILABLE'
ORDER BY sequence_no ASC
LIMIT 1
FOR UPDATE SKIP LOCKED;
```

然后：

```sql
UPDATE rand_allocation_slot
SET status = 'USED',
    used_by_subject_id = :subjectId,
    used_at = now()
WHERE id = :slotId
  AND status = 'AVAILABLE';
```

如果更新行数不是 1：

```text
1. 事务回滚
2. 记录系统审计
3. 返回 409 或重试一次
```

### 9.3 幂等设计

如果用户重复点击随机化，应该返回已有结果，而不是重新随机。

建议：

```text
Idempotency-Key: 前端生成并随请求发送
```

或以后端唯一约束为准：

```text
unique(plan_id, subject_id)
```

行为：

```text
1. 如果 subject 已有 assignment，返回已有 assignment
2. 如果正在进行随机化，返回 409 RANDOMIZATION_IN_PROGRESS
3. 不允许创建第二个 assignment
```

### 9.4 错误码

```text
RANDOMIZATION_PLAN_NOT_ACTIVE
SUBJECT_ALREADY_RANDOMIZED
SUBJECT_NOT_ELIGIBLE
STRATIFICATION_VALUE_MISSING
STRATIFICATION_VALUE_INVALID
STRATUM_NOT_FOUND
ALLOCATION_LIST_EXHAUSTED
NO_PERMISSION_TO_RANDOMIZE
NO_PERMISSION_TO_VIEW_UNBLINDED
EMERGENCY_UNBLINDING_REASON_REQUIRED
RANDOMIZATION_CONCURRENCY_CONFLICT
```

---

## 10. API 设计

### 10.1 Randomization Plan API

```text
POST   /api/v1/studies/{studyId}/randomization/plans
GET    /api/v1/studies/{studyId}/randomization/plans
GET    /api/v1/randomization/plans/{planId}
PATCH  /api/v1/randomization/plans/{planId}
POST   /api/v1/randomization/plans/{planId}/validate
POST   /api/v1/randomization/plans/{planId}/approve
POST   /api/v1/randomization/plans/{planId}/activate
POST   /api/v1/randomization/plans/{planId}/lock
POST   /api/v1/randomization/plans/{planId}/archive
```

创建计划请求：

```json
{
  "code": "EA_INSOMNIA_001",
  "name": "Electroacupuncture insomnia trial randomization",
  "method": "PREGENERATED_LIST",
  "blindingMode": "DOUBLE_BLIND",
  "allocationRatio": {
    "EA": 1,
    "CONTROL": 1
  },
  "arms": [
    {
      "armCode": "EA",
      "armName": "Electroacupuncture",
      "blindedLabel": "Group A",
      "isControl": false
    },
    {
      "armCode": "CONTROL",
      "armName": "Control",
      "blindedLabel": "Group B",
      "isControl": true
    }
  ],
  "stratificationFactors": [
    {
      "factorCode": "site",
      "factorName": "Study site",
      "sourceType": "SITE_FIELD",
      "sourceRef": "site_code",
      "dataType": "STRING",
      "required": true
    },
    {
      "factorCode": "severity",
      "factorName": "Baseline severity",
      "sourceType": "CRF_ITEM",
      "sourceRef": "baseline_isi_category",
      "dataType": "ENUM",
      "allowedValues": ["mild", "moderate", "severe"],
      "required": true
    }
  ]
}
```

### 10.2 Allocation List API

```text
POST /api/v1/randomization/plans/{planId}/allocation-lists/import
GET  /api/v1/randomization/plans/{planId}/allocation-lists
GET  /api/v1/randomization/allocation-lists/{listId}/validation-report
GET  /api/v1/randomization/allocation-lists/{listId}/preview
GET  /api/v1/randomization/allocation-lists/{listId}/download
```

导入接口要求：

```text
Content-Type: multipart/form-data
file: allocation_list.csv
```

导入校验返回：

```json
{
  "allocationListId": 301,
  "status": "VALIDATED",
  "rowCount": 240,
  "strataCount": 12,
  "warnings": [
    {
      "code": "SMALL_STRATUM_SIZE",
      "message": "Stratum site=01|severity=severe has only 6 slots."
    }
  ],
  "errors": []
}
```

### 10.3 Subject Randomization API

```text
POST /api/v1/subjects/{subjectId}/randomize
GET  /api/v1/subjects/{subjectId}/randomization
GET  /api/v1/studies/{studyId}/randomization/assignments
```

请求：

```json
{
  "planId": 1001,
  "stratificationValues": {
    "site": "site_01",
    "severity": "moderate"
  },
  "eligibilitySnapshot": {
    "eventId": 2001,
    "eventCrfId": 3001,
    "eligibilityConfirmed": true,
    "confirmedBy": "investigator"
  },
  "confirmation": true
}
```

双盲返回：

```json
{
  "assignmentId": 90001,
  "subjectId": 501,
  "randomizationNo": "R-000123",
  "blindedLabel": "Group A",
  "randomizedAt": "2026-05-17T10:30:00Z",
  "stratumKey": "site=site_01|severity=moderate",
  "status": "ASSIGNED"
}
```

非盲返回：

```json
{
  "assignmentId": 90001,
  "subjectId": 501,
  "randomizationNo": "R-000123",
  "armCode": "EA",
  "armName": "Electroacupuncture",
  "blindedLabel": "Group A",
  "randomizedAt": "2026-05-17T10:30:00Z",
  "stratumKey": "site=site_01|severity=moderate",
  "status": "ASSIGNED"
}
```

### 10.4 Emergency Unblinding API

```text
POST /api/v1/randomization/assignments/{assignmentId}/emergency-unblind
GET  /api/v1/randomization/assignments/{assignmentId}/unblinding
GET  /api/v1/studies/{studyId}/randomization/unblinding-events
```

请求：

```json
{
  "reason": "Medical emergency requiring knowledge of assigned treatment.",
  "confirm": true
}
```

返回：

```json
{
  "unblindingId": 7001,
  "assignmentId": 90001,
  "revealedArmCode": "EA",
  "revealedArmName": "Electroacupuncture",
  "unblindedAt": "2026-05-17T11:00:00Z",
  "unblindedBy": 101,
  "auditEventId": 88001
}
```

### 10.5 Report API

```text
GET /api/v1/studies/{studyId}/randomization/balance
GET /api/v1/studies/{studyId}/randomization/balance/by-site
GET /api/v1/studies/{studyId}/randomization/balance/by-stratum
GET /api/v1/studies/{studyId}/randomization/audit
```

平衡报告示例：

```json
{
  "studyId": 1,
  "planId": 1001,
  "totalRandomized": 84,
  "byArm": [
    { "armCode": "EA", "count": 42 },
    { "armCode": "CONTROL", "count": 42 }
  ],
  "byStratum": [
    {
      "stratumKey": "site=01|severity=moderate",
      "total": 20,
      "byArm": [
        { "armCode": "EA", "count": 10 },
        { "armCode": "CONTROL", "count": 10 }
      ]
    }
  ]
}
```

---

## 11. 权限与盲法设计

### 11.1 权限项

```text
RANDOMIZATION_PLAN_VIEW
RANDOMIZATION_PLAN_CREATE
RANDOMIZATION_PLAN_EDIT
RANDOMIZATION_PLAN_VALIDATE
RANDOMIZATION_PLAN_APPROVE
RANDOMIZATION_PLAN_ACTIVATE
RANDOMIZATION_PLAN_LOCK
RANDOMIZATION_PLAN_ARCHIVE

RANDOMIZATION_ALLOCATION_IMPORT
RANDOMIZATION_ALLOCATION_PREVIEW_BLINDED
RANDOMIZATION_ALLOCATION_PREVIEW_UNBLINDED
RANDOMIZATION_ALLOCATION_EXPORT_BLINDED
RANDOMIZATION_ALLOCATION_EXPORT_UNBLINDED

RANDOMIZATION_EXECUTE
RANDOMIZATION_VIEW_ASSIGNMENT_BLINDED
RANDOMIZATION_VIEW_ASSIGNMENT_UNBLINDED
RANDOMIZATION_VIEW_BALANCE_BLINDED
RANDOMIZATION_VIEW_BALANCE_UNBLINDED

RANDOMIZATION_EMERGENCY_UNBLIND
RANDOMIZATION_EMERGENCY_UNBLIND_APPROVE
RANDOMIZATION_AUDIT_VIEW
```

### 11.2 角色建议

| 角色 | 权限 |
|---|---|
| System Admin | 技术配置，不默认查看非盲分组 |
| Study Manager | 查看计划、查看盲态报告、管理流程 |
| Statistician | 创建/导入/验证随机化表，可查看非盲信息 |
| Randomization Manager | 批准、激活、锁定计划 |
| Site Investigator | 执行随机化、查看盲态结果 |
| Data Manager | 查看随机化状态和盲态报告 |
| Pharmacist / Unblinded Staff | 查看非盲分组或 kit 信息 |
| Emergency Unblinder | 紧急揭盲 |
| Auditor | 查看审计日志 |

### 11.3 盲法矩阵

| 信息类型 | Investigator | Data Manager | Statistician | Pharmacist | Auditor |
|---|---:|---:|---:|---:|---:|
| 是否已随机化 | 可见 | 可见 | 可见 | 可见 | 可见 |
| 随机化编号 | 可见 | 可见 | 可见 | 可见 | 可见 |
| blindedLabel | 可见 | 可见 | 可见 | 可见 | 可见 |
| armCode | 不可见 | 不可见 | 可见 | 可见 | 可按审计权限查看 |
| armName | 不可见 | 不可见 | 可见 | 可见 | 可按审计权限查看 |
| block size | 不可见 | 不可见 | 可见 | 不一定 | 不一定 |
| full allocation list | 不可见 | 不可见 | 可见 | 受限 | 受限 |
| random seed | 不可见 | 不可见 | 极少数可见 | 不可见 | 不建议直接可见 |

---

## 12. 与 OpenClinica 业务流程集成

### 12.1 Study Management

Study 下新增 Randomization tab：

```text
Study Detail
├── Overview
├── Sites
├── Subjects
├── CRFs
├── Randomization
│   ├── Plans
│   ├── Allocation Lists
│   ├── Assignments
│   ├── Balance
│   └── Audit
└── Export
```

### 12.2 Subject Management

Subject 详情页新增随机化状态：

```text
Subject Detail
├── Demographics
├── Events
├── CRFs
├── Queries
├── Randomization
│   ├── Not randomized / Randomized
│   ├── Randomization No
│   ├── Randomized At
│   ├── Blinded Label
│   └── Randomize Button
└── Audit
```

### 12.3 CRF 集成

随机化需要读取 CRF 字段时，不应直接依赖 CRF 内部表结构。建议定义接口：

```java
public interface RandomizationEligibilityProvider {
    EligibilityResult checkEligibility(Long studyId, Long subjectId, Long planId);

    Map<String, String> resolveStratificationValues(
        Long studyId,
        Long subjectId,
        Long planId
    );
}
```

第一版可从 Subject/Site/Event 字段解析分层值。CRF 重构完成后再支持：

```text
sourceType = CRF_ITEM
sourceRef = baseline.eligibility.isi_category
```

### 12.4 Audit 集成

随机化模块不独立造一套审计系统，而应调用 `oc-audit`：

```java
auditGateway.record(AuditEvent.builder()
    .eventType("RANDOMIZATION_EXECUTED")
    .studyId(studyId)
    .subjectId(subjectId)
    .entityType("RandomizationAssignment")
    .entityId(assignmentId)
    .beforeJson(null)
    .afterJson(maskedAssignmentJson)
    .reason("Subject randomized")
    .requestId(requestId)
    .build());
```

注意：

```text
盲态审计视图和非盲审计视图应分开。
审计表可以保存真实 arm 的加密值，但默认审计查询不返回。
```

### 12.5 Export 集成

导出分两类：

```text
盲态导出：
- subject_id
- randomization_no
- randomized_at
- blinded_label
- stratum_key

非盲导出：
- subject_id
- randomization_no
- randomized_at
- arm_code
- arm_name
- blinded_label
- stratum_key
```

非盲导出必须要求：

```text
1. RANDOMIZATION_ALLOCATION_EXPORT_UNBLINDED 权限
2. 导出原因
3. 审计记录
4. 可选二次认证
```

---

## 13. 前端页面计划

### 13.1 RandomizationPlanListPage

功能：

```text
1. 显示当前 Study 下所有随机化计划
2. 显示状态、方法、盲法模式、创建人、激活时间
3. 支持创建计划
4. 支持进入详情
5. 支持状态过滤
```

### 13.2 RandomizationPlanDetailPage

功能：

```text
1. 查看计划基本信息
2. 查看 arms
3. 查看 stratification factors
4. 查看 allocation list 状态
5. 执行 validate / approve / activate / lock
6. 查看变更历史
```

### 13.3 AllocationListImportPage

功能：

```text
1. 上传 CSV/XLSX
2. 预览前 20 行
3. 显示校验错误
4. 显示各分层行数
5. 显示各治疗臂分配数
6. 显示 checksum
7. 导入后不可直接激活，必须先 validate
```

### 13.4 SubjectRandomizationPanel

功能：

```text
1. 显示是否符合随机化条件
2. 显示缺失分层字段
3. 显示随机化按钮
4. 点击前二次确认
5. 随机化后显示结果
6. 支持打印/保存随机化确认单
```

### 13.5 EmergencyUnblindingPage

功能：

```text
1. 仅授权用户可见
2. 显示严肃警告
3. 必须填写原因
4. 可要求二次输入密码
5. 显示真实 arm
6. 生成审计
7. 标记 assignment 为 UNBLINDED
```

---

## 14. 导入校验规则

### 14.1 格式校验

```text
1. 必须包含 plan_code
2. 必须包含 stratum_key
3. 必须包含 sequence_no
4. 必须包含 arm_code
5. arm_code 必须属于计划定义的 arms
6. sequence_no 在同一 stratum 下唯一
7. stratum_key 格式必须可解析
8. blinded_label 不能为空，除非 open-label
9. 文件 checksum 记录
10. 空行和重复行报告
```

### 14.2 业务校验

```text
1. allocation ratio 是否接近计划比例
2. 每个 stratum 是否至少有 N 条 slot
3. 每个 stratum 下各 arm 是否平衡
4. block_no 是否连续，可选
5. sequence_no 是否连续
6. 是否有未知 stratum
7. 是否有未知 arm
8. 是否与已激活计划冲突
9. 是否已有 assignment，如果已有则禁止替换
10. 是否满足计划样本量
```

### 14.3 校验报告结构

```json
{
  "status": "VALIDATED_WITH_WARNINGS",
  "summary": {
    "rowCount": 240,
    "strataCount": 12,
    "armCount": 2
  },
  "errors": [],
  "warnings": [
    {
      "row": null,
      "code": "UNBALANCED_STRATUM",
      "message": "Stratum site=03 has EA=8 and CONTROL=4."
    }
  ],
  "byStratum": [
    {
      "stratumKey": "site=01|severity=moderate",
      "total": 20,
      "byArm": {
        "EA": 10,
        "CONTROL": 10
      }
    }
  ]
}
```

---

## 15. Java 代码骨架

### 15.1 Algorithm Interface

```java
public interface RandomizationAlgorithm {

    RandomizationMethod method();

    AllocationListDraft generate(RandomizationPlan plan,
                                 RandomizationGenerationRequest request);

    AlgorithmValidationResult validateConfiguration(RandomizationPlan plan);
}
```

### 15.2 Execution Service

```java
@Service
@RequiredArgsConstructor
public class RandomizationExecutionService {

    private final RandomizationPlanRepository planRepository;
    private final AllocationSlotRepository slotRepository;
    private final RandomizationAssignmentRepository assignmentRepository;
    private final SubjectGateway subjectGateway;
    private final PermissionGateway permissionGateway;
    private final AuditGateway auditGateway;
    private final CryptoGateway cryptoGateway;
    private final StratumKeyResolver stratumKeyResolver;

    @Transactional
    public RandomizationResult randomize(RandomizeSubjectCommand command) {
        permissionGateway.require(command.actor(), "RANDOMIZATION_EXECUTE", command.studyId());

        RandomizationPlan plan = planRepository.getActivePlan(command.planId());
        subjectGateway.requireRandomizable(command.studyId(), command.subjectId());

        assignmentRepository.findByPlanIdAndSubjectId(command.planId(), command.subjectId())
            .ifPresent(existing -> {
                throw new SubjectAlreadyRandomizedException(existing.getId());
            });

        StratumKey stratumKey = stratumKeyResolver.resolve(
            plan,
            command.subjectId(),
            command.stratificationValues()
        );

        AllocationSlot slot = slotRepository.lockNextAvailableSlot(plan.getId(), stratumKey)
            .orElseThrow(() -> new AllocationListExhaustedException(
                plan.getId(),
                stratumKey.value()
            ));

        RandomizationAssignment assignment = RandomizationAssignment.create(
            command.studyId(),
            command.siteId(),
            command.subjectId(),
            plan.getId(),
            slot.getId(),
            command.actor().userId(),
            stratumKey,
            command.eligibilitySnapshot(),
            command.requestId()
        );

        slot.markUsed(command.subjectId(), assignment.getId());
        slotRepository.save(slot);
        assignmentRepository.save(assignment);

        auditGateway.recordRandomizationExecuted(assignment.maskedForAudit());

        return RandomizationResult.from(assignment, command.viewerPermissions(), cryptoGateway);
    }
}
```

### 15.3 Repository Lock Method

```java
public interface AllocationSlotRepository {
    Optional<AllocationSlot> lockNextAvailableSlot(Long planId, StratumKey stratumKey);
}
```

JPA native query 示例：

```java
@Query(value = """
    SELECT *
    FROM rand_allocation_slot
    WHERE plan_id = :planId
      AND stratum_key = :stratumKey
      AND status = 'AVAILABLE'
    ORDER BY sequence_no ASC
    LIMIT 1
    FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
Optional<AllocationSlotEntity> lockNextAvailableSlot(
    @Param("planId") Long planId,
    @Param("stratumKey") String stratumKey
);
```

### 15.4 StratumKeyResolver

```java
public class StratumKeyResolver {

    public StratumKey resolve(RandomizationPlan plan,
                              Long subjectId,
                              Map<String, String> manualValues) {
        Map<String, String> values = new TreeMap<>();

        for (StratificationFactor factor : plan.getStratificationFactors()) {
            String value = resolveOneFactor(factor, subjectId, manualValues);
            factor.validate(value);
            values.put(factor.getFactorCode(), value);
        }

        return StratumKey.from(values);
    }
}
```

输出格式建议固定排序：

```text
severity=moderate|site=site_01|sex=female
```

不要用用户输入顺序，否则同一分层可能生成多个 key。

---

## 16. 测试计划

### 16.1 单元测试

```text
RandomizationPlanStatusTest
AllocationRatioTest
StratumKeyResolverTest
AllocationListParserTest
AllocationListValidationTest
SimpleRandomizationAlgorithmTest
PermutedBlockAlgorithmTest
StratifiedPermutedBlockAlgorithmTest
PocockMinimizationAlgorithmTest
BlindingPolicyTest
```

### 16.2 集成测试

使用 Testcontainers + PostgreSQL：

```text
1. 并发随机化测试
2. allocation slot 只能使用一次
3. subject 只能随机化一次
4. 导入 allocation list 后验证
5. 计划激活后不可修改核心字段
6. 紧急揭盲写审计
7. 非盲字段权限过滤
8. 盲态导出不包含 armCode
9. 非盲导出必须有审计
10. 事务失败后不产生半完成 assignment
```

### 16.3 并发测试

模拟：

```text
100 个线程同时对 100 个 subject 执行随机化
检查：
- assignment 数量 = 100
- allocation_slot used 数量 = 100
- 无重复 slot
- 无重复 subject
- 各 stratum 内顺序正确
```

### 16.4 Golden Master 测试

对算法生成结果建立固定测试集：

```text
fixtures/randomization/
├── simple/
├── permuted-block/
├── stratified-block/
└── minimization/
```

每个测试目录：

```text
input-plan.json
input-subjects.json
expected-allocation-list.json
expected-balance-report.json
```

### 16.5 前端 E2E

Playwright 流程：

```text
1. 创建随机化计划
2. 上传 allocation list
3. 查看校验报告
4. 批准计划
5. 激活计划
6. 创建 subject
7. 执行随机化
8. 验证 Subject 页面显示随机化结果
9. 使用无非盲权限账号无法查看 armCode
10. 使用授权账号紧急揭盲
11. 查看审计日志
```

---

## 17. 实施阶段计划

### Phase R0：需求与协议映射

目标：明确随机化需求，不写代码。

任务：

```text
[ ] 确认研究类型：开放、单盲、双盲
[ ] 确认治疗臂数量
[ ] 确认分配比例
[ ] 确认是否多中心
[ ] 确认分层因素
[ ] 确认是否需要紧急揭盲
[ ] 确认随机化时点
[ ] 确认随机化前置条件
[ ] 确认是否写回 CRF
[ ] 确认导出需求
[ ] 确认非盲角色
[ ] 编写 Randomization Requirements
```

产出：

```text
docs/randomization/REQUIREMENTS.md
docs/randomization/BLINDING_MODEL.md
docs/randomization/RISK_ASSESSMENT.md
```

验收：

```text
1. 可以从方案中明确描述随机化流程
2. 统计人员确认方法
3. 数据管理员确认字段来源
4. 研究者确认操作流程
```

---

### Phase R1：领域模型与数据库

目标：建立模块骨架和数据库表。

任务：

```text
[ ] 新建 oc-randomization 模块
[ ] 定义 domain model
[ ] 定义 status enum
[ ] 定义 permission enum
[ ] 编写 Liquibase changelog
[ ] 建立 JPA entity
[ ] 建立 repository
[ ] 建立基础单元测试
[ ] 建立数据库集成测试
```

验收：

```text
1. 所有表可通过 Liquibase 创建
2. 唯一约束生效
3. JPA repository 可读写
4. Testcontainers 测试通过
```

---

### Phase R2：计划管理 API

目标：支持创建、查看、验证、批准、激活计划。

任务：

```text
[ ] CreateRandomizationPlan
[ ] UpdateRandomizationPlan
[ ] ValidateRandomizationPlan
[ ] ApproveRandomizationPlan
[ ] ActivateRandomizationPlan
[ ] LockRandomizationPlan
[ ] Plan detail API
[ ] Plan list API
[ ] 权限控制
[ ] 审计记录
```

验收：

```text
1. DRAFT → VALIDATED → APPROVED → ACTIVE 流程可跑通
2. ACTIVE 后核心字段不可改
3. 所有操作写审计
4. OpenAPI 文档完整
```

---

### Phase R3：Allocation List 导入

目标：支持外部随机化表导入和校验。

任务：

```text
[ ] CSV parser
[ ] XLSX parser
[ ] checksum 计算
[ ] arm_code 加密存储
[ ] kit_code 加密存储
[ ] stratum_key 校验
[ ] sequence_no 校验
[ ] balance report
[ ] validation report API
[ ] import audit
```

验收：

```text
1. 正确文件可导入
2. 错误文件可返回明确错误
3. 重复 sequence_no 被拒绝
4. 未定义 arm_code 被拒绝
5. 真实 arm 加密存储
6. 普通用户无法预览非盲信息
```

---

### Phase R4：执行随机化

目标：受试者随机化 API 可稳定运行。

任务：

```text
[ ] RandomizeSubjectCommand
[ ] EligibilityProvider
[ ] StratumKeyResolver
[ ] lockNextAvailableSlot
[ ] create assignment
[ ] update slot
[ ] audit
[ ] idempotency
[ ] permission filtering
[ ] concurrency test
```

验收：

```text
1. subject 只能随机化一次
2. slot 只能使用一次
3. 并发测试无重复分配
4. 双盲用户只看到 blindedLabel
5. 非盲用户可按权限看到 arm
6. 没有可用 slot 时返回明确错误
```

---

### Phase R5：前端管理页面

目标：研究管理者可通过 UI 管理随机化。

任务：

```text
[ ] Plan list page
[ ] Plan detail page
[ ] Create plan form
[ ] Allocation import page
[ ] Validation report page
[ ] Balance report page
[ ] Subject randomization panel
[ ] Permission-based rendering
[ ] E2E test
```

验收：

```text
1. Study 页面可进入 Randomization tab
2. 可创建计划
3. 可上传 allocation list
4. 可激活计划
5. Subject 页面可执行随机化
6. 页面不泄露非盲信息
```

---

### Phase R6：紧急揭盲

目标：支持可审计的紧急揭盲流程。

任务：

```text
[ ] EmergencyUnblindCommand
[ ] 权限控制
[ ] 二次确认
[ ] 可选二次认证
[ ] 必填原因
[ ] reveal arm
[ ] update assignment status
[ ] audit
[ ] notification optional
```

验收：

```text
1. 无权限用户不能揭盲
2. 揭盲必须填写原因
3. 揭盲后写 audit
4. 真实 arm 只显示一次或可按权限再次查看
5. assignment 标记为 UNBLINDED
```

---

### Phase R7：导出与报告

目标：支持盲态/非盲导出和平衡报告。

任务：

```text
[ ] Blinded assignment export
[ ] Unblinded assignment export
[ ] Allocation list export
[ ] Balance by arm
[ ] Balance by site
[ ] Balance by stratum
[ ] Export audit
[ ] Export reason
```

验收：

```text
1. 盲态导出不含 armCode
2. 非盲导出要求特殊权限
3. 非盲导出写审计
4. 平衡报告数据与 assignment 一致
```

---

### Phase R8：系统内生成随机化表

目标：支持系统生成 permuted block / stratified block allocation list。

任务：

```text
[ ] SecureRandom seed management
[ ] PermutedBlockAlgorithm
[ ] StratifiedPermutedBlockAlgorithm
[ ] variable block size
[ ] algorithm metadata
[ ] seed encryption
[ ] generated list validation
[ ] golden master tests
```

验收：

```text
1. 生成结果可复现
2. seed 不对普通用户显示
3. block size 不对随机化执行者显示
4. 生成的 allocation list 仍走同一激活流程
```

---

### Phase R9：Minimization

目标：实现 Pocock-style minimization。

前置条件：

```text
1. 基础随机化稳定
2. 审计稳定
3. 平衡报告稳定
4. 算法测试集准备完成
```

任务：

```text
[ ] 定义 covariates
[ ] 定义 covariate weights
[ ] 定义 imbalance function
[ ] 定义 random component probability
[ ] 实现 PocockMinimizationAlgorithm
[ ] 记录每次决策的 imbalance score
[ ] 保存 algorithm trace
[ ] 测试与 unbiased 思路对照
```

验收：

```text
1. 每次 minimization 决策可追溯
2. 决策过程保存为 algorithm_trace_json
3. 平衡结果符合预期
4. 结果不泄露给盲态角色
```

---

## 18. 版本路线图

| 版本 | 范围 | 建议状态 |
|---|---|---|
| v0.1 | domain + DB schema | 开发 |
| v0.2 | plan management API | 开发 |
| v0.3 | allocation list import | 开发 |
| v0.4 | subject randomization API | 内部测试 |
| v0.5 | React UI + audit | 内部测试 |
| v0.6 | emergency unblinding | 内部测试 |
| v0.7 | blinded/unblinded export | 验证 |
| v1.0 | 预生成表 + 分层区组 + 盲法 + 审计 | 可用于受控试点 |
| v1.1 | 系统生成 block list | 可选 |
| v1.2 | minimization | 可选 |
| v2.0 | 独立服务化 / RTSM 扩展 | 长期 |

---

## 19. 验收标准

v1.0 必须满足：

```text
[ ] 一个 active plan 可以执行随机化
[ ] 受试者不能重复随机化
[ ] allocation slot 不能重复使用
[ ] 支持分层
[ ] 支持导入随机化表
[ ] 支持盲态结果返回
[ ] 支持非盲授权查看
[ ] 支持紧急揭盲
[ ] 所有关键操作写审计
[ ] 支持随机化平衡报告
[ ] 支持盲态和非盲导出
[ ] 支持并发随机化测试
[ ] 支持 OpenAPI 文档
[ ] 支持权限测试
[ ] 支持数据库回滚或补救脚本
```

---

## 20. 风险与缓解

| 风险 | 等级 | 表现 | 缓解 |
|---|---:|---|---|
| 分配可预测 | 高 | 固定小区组、区组大小泄露 | variable block size、隐藏 block size |
| 盲法破坏 | 极高 | 普通角色看到真实 arm | 字段加密、权限矩阵、审计 |
| 并发重复分配 | 极高 | 两个 subject 使用同一 slot | FOR UPDATE SKIP LOCKED + unique constraint |
| subject 重复随机化 | 极高 | 多次点击生成多个 assignment | unique(plan_id, subject_id) + 幂等 |
| 随机化表导入错误 | 高 | arm/stratum/sequence 错误 | 严格 import validation |
| 审计缺失 | 高 | 操作不可追溯 | 所有 command 统一 audit |
| 非盲导出泄露 | 高 | 普通导出含 armCode | 导出分级 + 权限 + reason |
| CRF 字段不稳定 | 中 | 分层字段来源变更 | EligibilityProvider 抽象 |
| minimization 不可解释 | 中 | 动态算法难复现 | 保存 algorithm trace |
| 许可证不清 | 中 | 复制代码引发许可问题 | 只参考架构，不复制代码 |

---

## 21. 许可证建议

### 21.1 RandIMI

RandIMI 显示为 Apache-2.0 license。Apache-2.0 与 LGPL 项目通常可以兼容使用，但如果复制其代码，需要保留版权声明、许可证和 NOTICE 要求。更稳妥做法是：

```text
只参考架构和算法思想，不直接复制源码。
```

### 21.2 unbiased

GitHub 页面显示存在 MIT license 文件，但也显示 Unknown license。由于许可证展示存在不一致，建议：

```text
1. 不直接复制源码。
2. 只参考 API 结构和算法描述。
3. 如果未来要复用 R 代码，先人工确认 LICENSE 文件。
```

### 21.3 OpenClinica 重构版

建议在 `MODIFICATIONS.md` 中增加：

```markdown
## Randomization Module

Added a new randomization module inspired by public clinical trial randomization projects such as RandIMI and unbiased. No source code was copied directly. The implementation provides OpenClinica-integrated randomization plan management, allocation list import, subject randomization, blinding control, emergency unblinding, and audit logging.
```

---

## 22. 推荐第一批开发任务

```text
[ ] docs/randomization/REQUIREMENTS.md
[ ] docs/randomization/BLINDING_MODEL.md
[ ] docs/randomization/API_DESIGN.md
[ ] docs/randomization/DATABASE_SCHEMA.md
[ ] backend/oc-randomization 模块骨架
[ ] Liquibase changelog
[ ] RandomizationPlan entity
[ ] RandomizationArm entity
[ ] StratificationFactor entity
[ ] AllocationList entity
[ ] AllocationSlot entity
[ ] RandomizationAssignment entity
[ ] Plan management API
[ ] Allocation list CSV parser
[ ] Allocation list validation service
[ ] Randomize subject service
[ ] Row-level locking repository
[ ] Audit integration
[ ] Permission constants
[ ] React Randomization tab
[ ] Playwright E2E 最小流程
```

---

## 23. 最终建议

第一版不要追求算法复杂度。随机化系统真正的难点不是“生成随机数”，而是：

```text
1. 分配隐藏
2. 盲法保护
3. 事务一致性
4. 并发安全
5. 权限隔离
6. 完整审计
7. 可验证导出
8. 与 Subject/CRF/Event 流程一致
```

因此推荐优先级：

```text
第一优先级：
预生成 allocation list + 分层区组 + 行级锁 + 审计 + 盲法

第二优先级：
系统内生成随机化表

第三优先级：
minimization

第四优先级：
RTSM / 药物供应 / 平台试验
```

这样既能参考 RandIMI 和 unbiased 的优点，又能让随机化模块自然嵌入 OpenClinica 的现代化重构架构中。

---

## 24. 参考来源

1. RandIMI GitHub: https://github.com/imi-ms/RandIMI  
2. RandIMI Wiki API: https://github.com/imi-ms/RandIMI/wiki/API  
3. unbiased GitHub: https://github.com/ttscience/unbiased  
4. ICH E6(R3) Data Governance summary: https://ichgcp.net/4-data-governance-investigator-and-sponsor-ich-e6-r3  
5. CONSORT 2010 checklist: https://legacyfileshare.elsevier.com/promis_misc/CONSORT-2010-Checklist.pdf  
