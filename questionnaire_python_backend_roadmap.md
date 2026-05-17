# OpenClinica 重构版问卷系统研发计划

**方案方向：SurveyJS / JSON Schema + 自研 Python 后端**  
**建议版本：v0.1**  
**适用场景：OpenClinica 重构、临床研究问卷/ePRO/eCRF-lite、随机化系统联动、科研数据导出与分析**

---

## 1. 总体定位

本方案的目标不是简单集成一个在线问卷工具，而是在 OpenClinica 重构版本中实现一个临床研究可用的 **Clinical Questionnaire Service**。前端使用 SurveyJS 作为动态问卷渲染层；后端使用 Python 生态自研，负责问卷版本、受试者绑定、访视分配、答案采集、量表评分、审计追踪、数据锁定和科研数据导出。

核心思路：

```text
SurveyJS 负责问卷显示与交互
JSON Schema / Pydantic 负责服务端校验
FastAPI 负责 API 服务
PostgreSQL JSONB 负责动态问卷数据存储
SQLAlchemy / Alembic 负责数据库模型与迁移
Pandas / Polars 负责科研数据导出与预处理
Celery / RQ 负责异步导出、评分、提醒任务
```

系统应被设计成 OpenClinica 重构版中的独立模块：

```text
questionnaire-service
= 问卷模板
+ 问卷版本
+ 访视分配
+ 受试者 token
+ 答案采集
+ 自动评分
+ 审计追踪
+ 数据锁定
+ 数据导出
+ 与随机化系统联动
```

---

## 2. 推荐技术栈

### 2.1 前端技术栈

```text
Frontend:
- Next.js / React
- TypeScript
- SurveyJS Form Library
- shadcn/ui
- TanStack Query
- Zod
- Tailwind CSS
```

前端职责：

```text
1. 渲染 SurveyJS 问卷
2. 提供受试者填写页面
3. 提供研究者问卷状态查看页面
4. 提供管理员问卷模板/版本管理页面
5. 提供导出任务、评分结果、审计记录查看页面
```

早期建议不直接集成复杂的 Survey Creator。第一阶段可以采用 JSON 编辑器 + 实时预览的方式维护问卷。

---

### 2.2 后端技术栈

由于项目后端更偏科研数据处理和 Python 生态，推荐如下组合：

```text
Backend:
- FastAPI
- Python 3.11+
- Pydantic v2
- SQLAlchemy 2.x
- Alembic
- PostgreSQL
- Redis
- Celery 或 RQ
- Pandas / Polars
- OpenPyXL / XlsxWriter
- PyArrow
- MinIO，可选
- Uvicorn / Gunicorn
```

推荐后端主线：

```text
FastAPI + SQLAlchemy + PostgreSQL + Redis + Celery/RQ + Pandas
```

各组件作用：

| 组件 | 作用 |
|---|---|
| FastAPI | API 服务、OpenAPI 文档、异步接口 |
| Pydantic | 请求校验、响应模型、问卷 schema 校验辅助 |
| SQLAlchemy | ORM、数据库模型 |
| Alembic | 数据库迁移 |
| PostgreSQL | 主数据库，保存结构化数据与 JSONB |
| Redis | 缓存、任务队列 broker、token 状态缓存 |
| Celery / RQ | 异步导出、批量评分、提醒任务 |
| Pandas / Polars | 科研数据整理、long/wide 格式导出 |
| OpenPyXL / XlsxWriter | Excel 导出 |
| MinIO | 附件、导出文件、签名文件存储 |

---

### 2.3 部署技术栈

```text
Deployment:
- Docker Compose
- Caddy / Traefik / Nginx
- PostgreSQL
- Redis
- MinIO，可选
- FastAPI API container
- Celery worker container
- Celery beat / scheduler container，可选
- Next.js web container
```

建议开发和中小规模部署均优先采用 Docker Compose。Kubernetes 不建议作为第一阶段目标。

---

## 3. 系统架构

建议整体结构如下：

```text
openclinica-next/

  apps/
    web/
      Next.js 前端
      - 管理员问卷配置
      - 研究者审核
      - 受试者填写
      - 导出任务查看

    api/
      FastAPI 后端
      - questionnaire template
      - questionnaire version
      - questionnaire assignment
      - questionnaire response
      - scoring engine
      - audit trail
      - export service
      - randomization integration

  packages/
    questionnaire-schemas/
      - isi.v1.json
      - gad7.v1.json
      - phq9.v1.json
      - ess.v1.json
      - psqi.v1.json

    shared-types/
      - OpenAPI generated client
      - TypeScript types

  infra/
    docker-compose.yml
    postgres/
    redis/
    minio/
    caddy/
```

运行时架构：

```text
Subject / Investigator / Admin
        ↓
Next.js Web
        ↓
FastAPI API Gateway
        ↓
Questionnaire Service
        ↓
PostgreSQL + Redis + MinIO
        ↓
Celery/RQ Worker
        ↓
CSV / XLSX / JSON / Analysis-ready Dataset
```

---

## 4. 模块拆分

## 4.1 Questionnaire Template 模块

用于管理问卷基础信息。

示例：

```text
ISI
PSQI
GAD-7
PHQ-9
ESS
睡眠日记
不良事件表
盲法评价问卷
针刺感量表
治疗依从性记录
```

核心功能：

```text
1. 创建问卷模板
2. 编辑问卷名称、编码、描述、分类
3. 设置问卷是否跨研究复用
4. 查看所有历史版本
5. 启用/停用模板
```

模板本身不直接用于填写，真正用于填写的是某个已发布的 questionnaire version。

---

## 4.2 Questionnaire Version 模块

每个问卷模板可以有多个版本。

示例：

```text
ISI
  ├── v1.0.0
  ├── v1.1.0
  └── v2.0.0
```

每个版本保存：

```text
1. SurveyJS schema
2. JSON Schema validation
3. scoring schema
4. language config
5. schema hash
6. version number
7. publish status
8. published_by
9. published_at
10. locked_at
```

版本状态：

```text
draft      草稿，可编辑
published  已发布，不可编辑
retired    停用，不再分配给新受试者
```

重要原则：

```text
已发布版本不可直接修改。
任何修改都必须生成新版本。
response 必须绑定具体 questionnaire_version_id。
```

---

## 4.3 Questionnaire Assignment 模块

Assignment 是问卷系统和临床研究流程结合的核心。

一个 assignment 表示：

```text
某个 study 中
某个 subject
在某个 visit
需要填写某个 questionnaire version
```

示例：

```text
study_id = insomnia_ea_trial
subject_id = S001
visit = Baseline
questionnaire = ISI v1.0.0
status = pending
```

状态建议：

```text
pending       未开始
in_progress   已开始
submitted     已提交
reviewed      已审核
locked        已锁定
expired       已过期
withdrawn     撤回
```

Assignment 可以由以下事件触发生成：

```text
SubjectCreated
ScreeningPassed
BaselineStarted
RandomizationCompleted
VisitStarted
TreatmentCompleted
FollowUpStarted
```

---

## 4.4 Subject-facing 填写模块

受试者端不应暴露内部 subject_id，而应使用一次性或短期 token。

推荐流程：

```text
研究者生成问卷任务
        ↓
系统生成 public_token
        ↓
受试者打开 /q/fill/{token}
        ↓
FastAPI 校验 token
        ↓
返回 SurveyJS schema
        ↓
受试者填写问卷
        ↓
提交 response
        ↓
后端校验、评分、保存、审计
```

安全原则：

```text
1. 数据库只保存 token hash，不保存明文 token
2. token 可设置过期时间
3. token 只能访问对应 assignment
4. 提交后 token 可失效或变为只读
5. public API 与 admin API 分离
```

---

## 4.5 Scoring Engine 模块

评分引擎必须以后端计算为准，不能只依赖 SurveyJS 前端表达式。

评分规则使用 JSON 定义。

示例：

```json
{
  "score_code": "ISI_total",
  "method": "sum",
  "items": [
    "ISI_01",
    "ISI_02",
    "ISI_03",
    "ISI_04",
    "ISI_05",
    "ISI_06",
    "ISI_07"
  ],
  "missing_policy": "reject_if_any_missing",
  "ranges": [
    { "min": 0, "max": 7, "label": "无临床失眠" },
    { "min": 8, "max": 14, "label": "亚阈值失眠" },
    { "min": 15, "max": 21, "label": "中度失眠" },
    { "min": 22, "max": 28, "label": "重度失眠" }
  ]
}
```

评分引擎应支持：

```text
1. sum
2. mean
3. reverse scoring
4. subscale scoring
5. conditional scoring
6. matrix scoring
7. missing value policy
8. daily diary aggregation
9. repeated-measure summary
```

适合 Python 实现的原因：

```text
1. 量表评分可以直接使用 Python 函数测试
2. 后续导出到 Pandas/Polars 很方便
3. 可以和统计分析、数据清洗、质量控制流程共享代码
4. 可为每个量表写单元测试，保证评分可重复
```

---

## 4.6 Audit Log 模块

临床研究系统中，审计追踪不能后置太久。MVP 阶段至少应记录核心操作。

需要记录：

```text
创建问卷模板
修改问卷草稿
发布问卷版本
停用问卷版本
生成 assignment
打开问卷
保存草稿
提交问卷
自动评分
人工审核
数据锁定
数据更正
数据导出
```

审计字段：

```text
operator_id
operator_role
action
entity_type
entity_id
old_value_json
new_value_json
reason
ip_hash
user_agent
created_at
```

原则：

```text
1. 审计日志只追加，不修改
2. 涉及数据更正必须记录 reason
3. 导出行为必须记录
4. 锁定数据不能直接修改
```

---

## 4.7 Export Service 模块

导出模块是 Python 后端的优势模块之一。

初期支持：

```text
CSV
XLSX
JSON
```

导出形态：

```text
wide format:
每个受试者一行，每个 item 一个字段

long format:
subject_id, visit, questionnaire, item_code, value

score format:
subject_id, visit, questionnaire, score_code, score_value

raw format:
完整 raw_response_json
```

科研分析友好的导出格式：

```text
subject_code | group | visit | questionnaire | item_code | value | submitted_at
subject_code | group | visit | ISI_total | GAD7_total | PHQ9_total | ESS_total
subject_code | randomization_arm | baseline_ISI | endpoint_ISI | delta_ISI
```

建议将导出任务异步化：

```text
POST /api/questionnaires/export
        ↓
创建 export_job
        ↓
Celery/RQ worker 执行
        ↓
Pandas/Polars 整理数据
        ↓
生成 CSV/XLSX
        ↓
保存到 MinIO 或本地 volume
        ↓
返回下载链接
```

---

## 5. 数据库设计草案

## 5.1 questionnaire_template

```sql
create table questionnaire_template (
  id uuid primary key,
  study_id uuid null,
  code varchar(64) not null,
  name varchar(255) not null,
  description text,
  category varchar(64),
  status varchar(32) not null default 'draft',
  created_by uuid not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
```

说明：

```text
study_id 为 null 时，表示该问卷模板可跨研究复用。
study_id 不为空时，表示该问卷仅属于某个具体研究。
```

---

## 5.2 questionnaire_version

```sql
create table questionnaire_version (
  id uuid primary key,
  template_id uuid not null references questionnaire_template(id),
  version_no varchar(32) not null,
  surveyjs_schema jsonb not null,
  validation_schema jsonb null,
  scoring_schema jsonb null,
  language varchar(16) default 'zh-CN',
  schema_hash varchar(128) not null,
  status varchar(32) not null default 'draft',
  published_by uuid null,
  published_at timestamptz null,
  locked_at timestamptz null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  unique(template_id, version_no)
);
```

---

## 5.3 questionnaire_assignment

```sql
create table questionnaire_assignment (
  id uuid primary key,
  study_id uuid not null,
  subject_id uuid not null,
  visit_id uuid null,
  randomization_arm_id uuid null,
  questionnaire_version_id uuid not null references questionnaire_version(id),
  status varchar(32) not null default 'pending',
  due_at timestamptz null,
  public_token_hash varchar(255) null,
  token_expires_at timestamptz null,
  created_by uuid not null,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);
```

---

## 5.4 questionnaire_response

```sql
create table questionnaire_response (
  id uuid primary key,
  assignment_id uuid not null references questionnaire_assignment(id),
  subject_id uuid not null,
  visit_id uuid null,
  questionnaire_version_id uuid not null references questionnaire_version(id),
  status varchar(32) not null default 'submitted',
  started_at timestamptz null,
  submitted_at timestamptz null,
  raw_response_json jsonb not null,
  score_json jsonb null,
  total_score numeric null,
  device_info jsonb null,
  ip_hash varchar(255) null,
  created_at timestamptz not null default now()
);
```

---

## 5.5 questionnaire_answer

```sql
create table questionnaire_answer (
  id uuid primary key,
  response_id uuid not null references questionnaire_response(id),
  item_code varchar(128) not null,
  value_text text null,
  value_number numeric null,
  value_boolean boolean null,
  value_json jsonb null,
  is_missing boolean not null default false,
  created_at timestamptz not null default now()
);
```

---

## 5.6 questionnaire_audit_log

```sql
create table questionnaire_audit_log (
  id uuid primary key,
  study_id uuid null,
  entity_type varchar(64) not null,
  entity_id uuid not null,
  action varchar(64) not null,
  old_value_json jsonb null,
  new_value_json jsonb null,
  reason text null,
  operator_id uuid null,
  operator_role varchar(64) null,
  ip_hash varchar(255) null,
  user_agent text null,
  created_at timestamptz not null default now()
);
```

---

## 5.7 export_job

```sql
create table export_job (
  id uuid primary key,
  study_id uuid not null,
  requested_by uuid not null,
  status varchar(32) not null default 'pending',
  export_type varchar(64) not null,
  export_format varchar(32) not null,
  query_params jsonb not null,
  file_path text null,
  error_message text null,
  created_at timestamptz not null default now(),
  started_at timestamptz null,
  finished_at timestamptz null
);
```

---

## 6. FastAPI 后端目录结构建议

```text
apps/api/
  app/
    main.py
    core/
      config.py
      security.py
      database.py
      redis.py
      logging.py
      exceptions.py

    models/
      questionnaire_template.py
      questionnaire_version.py
      questionnaire_assignment.py
      questionnaire_response.py
      questionnaire_answer.py
      audit_log.py
      export_job.py

    schemas/
      questionnaire_template.py
      questionnaire_version.py
      questionnaire_assignment.py
      questionnaire_response.py
      scoring.py
      export.py

    api/
      v1/
        routers/
          questionnaire_templates.py
          questionnaire_versions.py
          questionnaire_assignments.py
          questionnaire_public.py
          questionnaire_responses.py
          questionnaire_exports.py
          audit_logs.py

    services/
      questionnaire_template_service.py
      questionnaire_version_service.py
      assignment_service.py
      response_service.py
      scoring_service.py
      validation_service.py
      audit_service.py
      export_service.py
      token_service.py

    scoring/
      base.py
      registry.py
      isi.py
      gad7.py
      phq9.py
      ess.py
      psqi.py
      sleep_diary.py

    workers/
      celery_app.py
      export_tasks.py
      scoring_tasks.py
      reminder_tasks.py

    repositories/
      questionnaire_template_repo.py
      questionnaire_version_repo.py
      assignment_repo.py
      response_repo.py
      audit_repo.py

    tests/
      test_scoring_isi.py
      test_scoring_gad7.py
      test_submit_response.py
      test_export_long_format.py

  alembic/
  pyproject.toml
  Dockerfile
```

---

## 7. API 设计草案

## 7.1 管理端 API

### 问卷模板

```text
POST   /api/v1/questionnaires/templates
GET    /api/v1/questionnaires/templates
GET    /api/v1/questionnaires/templates/{template_id}
PATCH  /api/v1/questionnaires/templates/{template_id}
DELETE /api/v1/questionnaires/templates/{template_id}
```

### 问卷版本

```text
POST   /api/v1/questionnaires/templates/{template_id}/versions
GET    /api/v1/questionnaires/templates/{template_id}/versions
GET    /api/v1/questionnaires/versions/{version_id}
PATCH  /api/v1/questionnaires/versions/{version_id}
POST   /api/v1/questionnaires/versions/{version_id}/publish
POST   /api/v1/questionnaires/versions/{version_id}/retire
```

### 问卷分配

```text
POST   /api/v1/questionnaires/assignments
GET    /api/v1/questionnaires/assignments
GET    /api/v1/questionnaires/assignments/{assignment_id}
PATCH  /api/v1/questionnaires/assignments/{assignment_id}
POST   /api/v1/questionnaires/assignments/bulk-create
```

---

## 7.2 受试者端 API

```text
GET    /api/v1/public/questionnaires/{token}
POST   /api/v1/public/questionnaires/{token}/draft
POST   /api/v1/public/questionnaires/{token}/submit
```

`GET /api/v1/public/questionnaires/{token}` 返回：

```json
{
  "assignment_id": "uuid",
  "questionnaire_code": "ISI",
  "questionnaire_name": "Insomnia Severity Index",
  "version_no": "1.0.0",
  "surveyjs_schema": {},
  "status": "pending",
  "due_at": "2026-01-01T00:00:00Z"
}
```

`POST /submit` 提交：

```json
{
  "started_at": "2026-01-01T09:00:00Z",
  "submitted_at": "2026-01-01T09:05:00Z",
  "response": {
    "ISI_01": 2,
    "ISI_02": 3,
    "ISI_03": 2,
    "ISI_04": 1,
    "ISI_05": 2,
    "ISI_06": 2,
    "ISI_07": 1
  },
  "device_info": {
    "platform": "mobile",
    "browser": "Chrome"
  }
}
```

---

## 7.3 审核与锁定 API

```text
GET    /api/v1/questionnaires/responses
GET    /api/v1/questionnaires/responses/{response_id}
POST   /api/v1/questionnaires/responses/{response_id}/review
POST   /api/v1/questionnaires/responses/{response_id}/lock
POST   /api/v1/questionnaires/responses/{response_id}/unlock-request
POST   /api/v1/questionnaires/responses/{response_id}/correction
```

---

## 7.4 导出 API

```text
POST   /api/v1/questionnaires/export
GET    /api/v1/questionnaires/export/{job_id}
GET    /api/v1/questionnaires/export/{job_id}/download
```

导出请求示例：

```json
{
  "study_id": "study-uuid",
  "visit_ids": ["baseline", "week4"],
  "questionnaire_codes": ["ISI", "PSQI", "GAD7"],
  "format": "xlsx",
  "layout": "wide",
  "include_scores": true,
  "include_raw": false
}
```

---

## 8. Python 评分引擎设计

## 8.1 评分接口抽象

建议定义统一评分接口：

```python
from abc import ABC, abstractmethod
from typing import Any

class BaseScorer(ABC):
    code: str

    @abstractmethod
    def score(self, response: dict[str, Any], scoring_schema: dict[str, Any] | None = None) -> dict[str, Any]:
        pass
```

---

## 8.2 ISI 评分示例

```python
class ISIScorer(BaseScorer):
    code = "ISI"

    required_items = [
        "ISI_01", "ISI_02", "ISI_03", "ISI_04", "ISI_05", "ISI_06", "ISI_07"
    ]

    def score(self, response: dict, scoring_schema: dict | None = None) -> dict:
        missing = [item for item in self.required_items if item not in response or response[item] is None]
        if missing:
            return {
                "score_code": "ISI_total",
                "status": "invalid",
                "missing_items": missing,
                "total_score": None
            }

        total = sum(int(response[item]) for item in self.required_items)

        if total <= 7:
            severity = "无临床失眠"
        elif total <= 14:
            severity = "亚阈值失眠"
        elif total <= 21:
            severity = "中度失眠"
        else:
            severity = "重度失眠"

        return {
            "score_code": "ISI_total",
            "status": "valid",
            "total_score": total,
            "severity": severity
        }
```

---

## 8.3 Scorer Registry

```python
class ScorerRegistry:
    def __init__(self):
        self._scorers = {}

    def register(self, scorer: BaseScorer):
        self._scorers[scorer.code] = scorer

    def get(self, code: str) -> BaseScorer:
        if code not in self._scorers:
            raise ValueError(f"No scorer registered for questionnaire: {code}")
        return self._scorers[code]

registry = ScorerRegistry()
registry.register(ISIScorer())
```

优点：

```text
1. 每个量表评分逻辑独立
2. 易于单元测试
3. 易于后续增加 PSQI、睡眠日记、针刺感量表
4. 可以同时支持 JSON scoring schema 和 Python 固定评分函数
```

---

## 9. 数据导出设计

## 9.1 long format

适合科研统计和 mixed model：

```text
subject_code | study_id | group | visit | questionnaire | item_code | value | submitted_at
S001         | study01  | EA    | BL    | ISI           | ISI_01    | 2     | 2026-01-01
S001         | study01  | EA    | BL    | ISI           | ISI_02    | 3     | 2026-01-01
```

---

## 9.2 wide format

适合 SPSS、Excel、简单前后比较：

```text
subject_code | group | visit | ISI_01 | ISI_02 | ISI_03 | ISI_total | submitted_at
S001         | EA    | BL    | 2      | 3      | 2      | 13        | 2026-01-01
```

---

## 9.3 score format

适合主要结局分析：

```text
subject_code | group | visit | ISI_total | PSQI_total | GAD7_total | PHQ9_total | ESS_total
S001         | EA    | BL    | 13        | 9          | 5          | 4           | 7
S001         | EA    | EP    | 7         | 5          | 3          | 2           | 4
```

---

## 9.4 delta format

适合疗效分析：

```text
subject_code | group | baseline_ISI | endpoint_ISI | delta_ISI | response_rate
S001         | EA    | 13           | 7            | -6        | true
```

Python 后端可以通过 Pandas 自动生成这些格式。

---

## 10. 与随机化系统联动

建议使用事件驱动或内部 API 调用实现。

关键事件：

```text
ScreeningPassed
BaselineCompleted
RandomizationCompleted
VisitStarted
TreatmentCompleted
FollowUpStarted
```

随机化完成后的流程：

```text
RandomizationCompleted
        ↓
读取 subject_id、study_id、arm_id
        ↓
查询 questionnaire schedule
        ↓
根据 arm_id 生成后续 assignment
        ↓
更新 subject questionnaire dashboard
        ↓
可选：发送填写提醒
```

示例：

```text
EA group:
  Week 2: ISI + 不良事件
  Week 4: ISI + PSQI + 盲法评价

TEAS group:
  Week 2: ISI + 不良事件
  Week 4: ISI + PSQI + 盲法评价

Control group:
  Week 2: ISI
  Week 4: ISI + PSQI
```

设计原则：

```text
随机化系统负责分组。
问卷系统负责根据分组生成数据采集任务。
两者通过 subject_id、study_id、visit_id、arm_id 关联。
```

---

## 11. 权限设计

角色建议：

```text
system_admin
study_admin
investigator
coordinator
data_manager
monitor
subject
```

权限建议：

| 角色 | 权限 |
|---|---|
| system_admin | 系统全局管理 |
| study_admin | 创建研究问卷、发布版本、配置访视 |
| investigator | 查看问卷结果、审核数据 |
| coordinator | 分配问卷、查看完成状态、联系受试者 |
| data_manager | 数据锁定、数据更正、导出数据 |
| monitor | 只读查看、核查审计记录 |
| subject | 填写自己的问卷 |

权限控制建议：

```text
1. 管理端 API 走 JWT / Session auth
2. 受试者端 public API 走 token auth
3. 每个接口检查 study-level permission
4. 数据导出必须要求 data_manager 或 study_admin 权限
5. 审计日志对 monitor/data_manager 可见
```

---

## 12. 前端页面规划

## 12.1 问卷模板列表页

功能：

```text
查看问卷模板
按 study 过滤
按状态过滤
创建模板
进入版本管理
停用模板
复制模板
```

---

## 12.2 问卷版本编辑页

第一阶段建议：JSON 编辑器 + 实时预览。

左侧：

```text
SurveyJS JSON editor
Validation JSON editor
Scoring JSON editor
```

右侧：

```text
SurveyJS live preview
item code 检查结果
评分规则检查结果
```

---

## 12.3 访视问卷配置页

用于配置每个 visit 需要填写的问卷。

示例：

```text
Screening:
  - ISI
  - PSQI
  - GAD-7
  - PHQ-9
  - ESS

Baseline:
  - ISI
  - 睡眠日记
  - 期望值量表

Week 2:
  - ISI
  - 不良事件

Endpoint:
  - ISI
  - PSQI
  - 盲法评价
```

---

## 12.4 受试者填写页

要求：

```text
1. 移动端优先
2. 单问卷填写
3. 多问卷任务列表
4. 草稿保存
5. 提交前确认
6. 提交后只读
7. token 过期提示
```

---

## 12.5 研究者审核页

功能：

```text
查看 subject 问卷完成状态
查看量表总分
查看缺失项
查看异常值
审核 response
锁定 response
发起数据更正
导出数据
```

---

## 13. 研发阶段计划

## Phase 0：架构准备与规范定义

目标：确定数据结构、模块边界、编码规范。

交付物：

```text
1. questionnaire-service 技术设计文档
2. 数据库 ERD
3. FastAPI OpenAPI 草案
4. SurveyJS JSON 规范
5. scoring schema 规范
6. 审计日志规范
7. Docker Compose 基础服务
```

建议结论：

```text
问卷模板可以跨 study 复用。
问卷 assignment 必须绑定 study + subject + visit。
response 必须绑定 questionnaire_version。
评分结果必须由后端计算。
```

---

## Phase 1：MVP 固定量表渲染与提交

目标：跑通“后台配置问卷 → 受试者填写 → 后端保存”的最小链路。

功能：

```text
1. FastAPI 项目初始化
2. PostgreSQL / Redis / Docker Compose 初始化
3. SQLAlchemy 模型与 Alembic migration
4. 内置 ISI、GAD-7、PHQ-9、ESS 示例 JSON
5. SurveyJS 前端渲染
6. public token 填写页面
7. response submit API
8. raw_response_json 保存
9. questionnaire_answer 展开保存
10. 基础 audit log
```

验收标准：

```text
1. 可以创建问卷模板
2. 可以创建问卷版本
3. 可以发布版本
4. 可以生成 assignment
5. 可以通过 token 填写
6. 可以提交 response
7. 数据库能保存 raw response 和 answer 明细
```

暂不做：

```text
复杂 builder
完整权限
完整导出
复杂评分
数据锁定
```

---

## Phase 2：评分引擎与量表模板库

目标：支持常用临床量表自动评分。

优先实现：

```text
ISI
GAD-7
PHQ-9
ESS
```

后续实现：

```text
PSQI
睡眠日记
不良事件表
盲法评价表
针刺感量表
```

功能：

```text
1. scoring service
2. scorer registry
3. 每个量表独立 Python scorer
4. score_json 保存
5. total_score 保存
6. 评分结果页面
7. 评分单元测试
```

验收标准：

```text
提交 ISI 后自动计算 ISI_total。
提交 GAD-7 后自动计算 GAD7_total。
提交 PHQ-9 后自动计算 PHQ9_total。
提交 ESS 后自动计算 ESS_total。
缺失项可按规则 reject 或标记 invalid。
```

---

## Phase 3：访视配置与批量分配

目标：让问卷系统真正进入 clinical workflow。

功能：

```text
1. study visit schedule 配置
2. visit-questionnaire mapping
3. 按访视批量生成 assignment
4. 按受试者状态生成 assignment
5. 按随机化分组生成 assignment
6. due_at 到期时间
7. 完成状态 dashboard
```

验收标准：

```text
可以给某个研究配置访视问卷。
可以给一个 subject 自动生成 baseline 问卷任务。
可以根据 randomization arm 生成不同后续问卷。
可以查看每个 subject 的问卷完成进度。
```

---

## Phase 4：权限、审核、锁定与更正

目标：补齐临床研究数据治理能力。

功能：

```text
1. RBAC 权限模型
2. response review
3. response lock
4. data correction request
5. audit trail viewer
6. operator reason 记录
```

验收标准：

```text
已锁定 response 不能直接修改。
任何更正都要记录 reason。
审计日志可以按 subject / visit / operator 检索。
不同角色看到不同菜单和操作按钮。
```

---

## Phase 5：导出与科研分析接口

目标：使数据可以直接进入 R / Python / SPSS 分析。

功能：

```text
1. CSV export
2. XLSX export
3. JSON export
4. long format
5. wide format
6. score-only format
7. delta format
8. export job 异步任务
9. export audit log
```

验收标准：

```text
可以导出 baseline 所有 ISI 数据。
可以导出所有 subject 的量表总分。
可以导出 long format 供 R/Python 分析。
可以导出 wide format 供 Excel/SPSS 使用。
导出行为进入 audit log。
```

---

## Phase 6：轻量问卷设计器增强

目标：在系统稳定后，增强非开发人员配置能力。

建议优先做自研轻量 builder：

```text
1. 支持题型选择
2. 支持选项编辑
3. 支持必填设置
4. 支持分组/分页
5. 支持简单跳题逻辑
6. 支持实时预览
7. 支持导入/导出 SurveyJS JSON
```

不建议第一阶段就做复杂拖拽 builder。对于临床量表来说，早期真正重要的是：

```text
版本控制
评分准确性
访视绑定
受试者绑定
审计追踪
导出可用性
```

---

## 14. 推荐里程碑

## M1：最小闭环

交付：

```text
FastAPI 项目
数据库 migration
问卷模板表
问卷版本表
assignment 表
response 表
SurveyJS 填写页
token 填写
response 保存
基础 audit
Docker Compose
```

目标结果：

```text
能完成一份 ISI 问卷填写并保存。
```

---

## M2：自动评分

交付：

```text
scoring service
ISI scoring
GAD-7 scoring
PHQ-9 scoring
ESS scoring
score_json
评分结果页面
评分单元测试
```

目标结果：

```text
能自动计算常用量表总分。
```

---

## M3：访视分配

交付：

```text
visit-questionnaire mapping
assignment bulk create
subject questionnaire dashboard
due_at
completion status
```

目标结果：

```text
能按 Screening / Baseline / Endpoint 自动分配问卷。
```

---

## M4：数据治理

交付：

```text
RBAC
review
lock
correction request
audit log viewer
```

目标结果：

```text
问卷数据进入临床研究可追溯状态。
```

---

## M5：科研导出

交付：

```text
CSV export
XLSX export
long format
wide format
score-only format
delta format
export audit
```

目标结果：

```text
可以直接导出给 R / Python / SPSS 分析。
```

---

## 15. MVP 任务清单

建议按以下顺序开发：

```text
1. 初始化 FastAPI 项目
2. 初始化 PostgreSQL、Redis、Docker Compose
3. 配置 SQLAlchemy 2.x
4. 配置 Alembic migration
5. 创建 questionnaire_template 表
6. 创建 questionnaire_version 表
7. 创建 questionnaire_assignment 表
8. 创建 questionnaire_response 表
9. 创建 questionnaire_answer 表
10. 创建 questionnaire_audit_log 表
11. 创建 export_job 表
12. 内置 ISI SurveyJS JSON
13. 开发 SurveyJS React 渲染组件
14. 开发 public token 生成与校验
15. 开发 /q/fill/{token} 页面
16. 开发 GET public questionnaire API
17. 开发 submit response API
18. 后端校验必填项
19. 保存 raw_response_json
20. 展开 answer 明细
21. 写入 audit log
22. 开发 ISI scoring
23. 开发 GAD-7 scoring
24. 开发 PHQ-9 scoring
25. 开发 ESS scoring
26. 开发研究者查看 response 页面
27. 开发 assignment 列表页面
28. 开发 CSV long format 导出
29. 开发 XLSX wide format 导出
30. Docker Compose 联调
```

---

## 16. Docker Compose 服务建议

```yaml
services:
  web:
    build: ./apps/web
    ports:
      - "3000:3000"
    environment:
      - NEXT_PUBLIC_API_URL=http://localhost:8000
    depends_on:
      - api

  api:
    build: ./apps/api
    ports:
      - "8000:8000"
    environment:
      - DATABASE_URL=postgresql+psycopg://openclinica:openclinica@postgres:5432/openclinica
      - REDIS_URL=redis://redis:6379/0
      - SECRET_KEY=change-me
    depends_on:
      - postgres
      - redis

  worker:
    build: ./apps/api
    command: celery -A app.workers.celery_app worker --loglevel=info
    environment:
      - DATABASE_URL=postgresql+psycopg://openclinica:openclinica@postgres:5432/openclinica
      - REDIS_URL=redis://redis:6379/0
    depends_on:
      - api
      - postgres
      - redis

  postgres:
    image: postgres:16
    environment:
      - POSTGRES_USER=openclinica
      - POSTGRES_PASSWORD=openclinica
      - POSTGRES_DB=openclinica
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"

  redis:
    image: redis:7
    ports:
      - "6379:6379"

  minio:
    image: minio/minio
    command: server /data --console-address ":9001"
    environment:
      - MINIO_ROOT_USER=minio
      - MINIO_ROOT_PASSWORD=minio-password
    volumes:
      - minio_data:/data
    ports:
      - "9000:9000"
      - "9001:9001"

volumes:
  postgres_data:
  minio_data:
```

---

## 17. 质量控制与测试计划

## 17.1 单元测试

重点测试：

```text
ISI 评分
GAD-7 评分
PHQ-9 评分
ESS 评分
缺失值处理
反向计分
subscale 计算
response 展开 answer
```

---

## 17.2 API 测试

重点测试：

```text
创建问卷模板
创建问卷版本
发布版本
生成 assignment
token 校验
提交 response
重复提交处理
过期 token 处理
锁定后禁止修改
```

---

## 17.3 导出测试

重点测试：

```text
long format 字段完整性
wide format item 对齐
score format 分数正确性
delta format 前后访视匹配
导出权限
导出审计日志
```

---

## 17.4 数据一致性测试

重点测试：

```text
raw_response_json 与 questionnaire_answer 是否一致
score_json 与 answer 明细是否一致
assignment status 与 response status 是否一致
questionnaire_version 是否被正确锁定
```

---

## 18. 当前研究可优先内置的问卷

结合针刺睡眠研究，建议优先内置：

```text
P0:
- ISI
- GAD-7
- PHQ-9
- ESS

P1:
- PSQI
- 睡眠日记
- 不良事件表

P2:
- 盲法评价
- 针刺感量表
- 治疗期依从性记录
- 满意度/期望值量表
```

优先级原因：

```text
1. ISI 是主要结局，必须最先支持。
2. GAD-7、PHQ-9、ESS 评分简单，适合验证评分引擎。
3. PSQI 评分较复杂，建议在评分引擎稳定后实现。
4. 睡眠日记涉及重复测量和聚合，适合作为 Phase 2 后半段或 Phase 3 内容。
```

---

## 19. 不建议早期实现的内容

第一，不建议第一阶段做复杂拖拽式问卷设计器。它会消耗大量前端时间，但对临床研究最关键的数据治理帮助不大。

第二，不建议一开始做完整 CDISC ODM / FHIR 兼容。可以保留扩展字段和导出接口，但不要作为 MVP 主目标。

第三，不建议评分只依赖 SurveyJS 前端。前端评分可以用于即时反馈，但最终分数必须以后端 Python scoring engine 为准。

第四，不建议直接把 Formbricks / LimeSurvey 作为核心问卷系统。它们适合调查或反馈，不适合作为临床研究数据链路的核心。

第五，不建议在数据模型里只保存 raw JSON。必须同时保存 raw_response_json 和结构化 questionnaire_answer，否则后续统计、导出、质控都会变复杂。

---

## 20. 最终推荐研发路线

最稳妥的路径是：

```text
第 1 步：
固定 JSON 量表 + SurveyJS 渲染 + FastAPI response 保存

第 2 步：
Python 后端评分引擎 + ISI/GAD-7/PHQ-9/ESS 模板库

第 3 步：
访视问卷配置 + assignment 自动生成

第 4 步：
审计追踪 + 审核 + 数据锁定 + 更正流程

第 5 步：
Pandas/Polars 导出 CSV/XLSX + long/wide/score/delta 格式

第 6 步：
轻量问卷 builder 或后续 Survey Creator 集成
```

一句话总结：

```text
先做 clinical-grade 的数据链路，再做 form-builder 体验；先保证评分、访视、审计、导出可用，再增强可视化配置能力。
```

对于你的 OpenClinica 重构项目，Python 后端路线的优势在于它更容易和科研数据处理、量表评分、统计分析、批量导出、质量控制流程结合。问卷系统最终不只是一个表单填写模块，而应成为研究数据采集、疗效评价和统计分析之间的桥梁。
