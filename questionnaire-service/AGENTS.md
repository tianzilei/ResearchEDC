# questionnaire-service/ - Python FastAPI Microservice

**Module:** Standalone questionnaire scoring and management microservice  
**Files:** ~76 Python files  

> Python FastAPI application providing questionnaire CRUD, assignment management, scoring (ISI/GAD-7/PHQ-9/ESS/PSQI), and export. Deployed by the root bare host `deploy.sh` script.

## STRUCTURE

```
questionnaire-service/
├── apps/api/app/
│   ├── main.py              # FastAPI app entry point
│   ├── api/v1/routers/      # 9 API router modules
│   ├── core/                # Core utilities (config, db session)
│   ├── models/              # 7 SQLAlchemy ORM models
│   ├── schemas/             # Pydantic v2 request/response schemas
│   ├── repositories/        # Data access layer
│   ├── services/            # Business logic
│   ├── scoring/             # Scoring engine (7 scorers)
│   ├── workers/             # Background task workers
│   └── tests/               # pytest test suite (39 tests)
├── infra/                   # Infrastructure notes/configs
└── packages/                # Shared Python packages
```

## API ROUTERS

| Router | Path | Purpose |
|--------|------|---------|
| `questionnaire_templates.py` | `/api/v1/questionnaire-templates` | CRUD for questionnaire definitions |
| `questionnaire_versions.py` | `/api/v1/questionnaire-versions` | Version management |
| `questionnaire_assignments.py` | `/api/v1/assignments` | Subject-questionnaire assignments |
| `questionnaire_responses.py` | `/api/v1/responses` | Response submission & retrieval |
| `questionnaire_public.py` | `/api/v1/public` | Unauthenticated questionnaire access |
| `questionnaire_exports.py` | `/api/v1/exports` | Data export endpoints |
| `scoring.py` | `/api/v1/scoring` | Score calculation endpoints |
| `audit_logs.py` | `/api/v1/audit-logs` | Audit trail |
| `events_webhook.py` | `/api/v1/webhooks` | Webhook notifications |

## SCORING ENGINE

```
scoring/
├── base.py         # Abstract scorer base class
├── registry.py     # Scorer registry (maps questionnaire types to scorers)
├── scoring_service.py  # Orchestration layer
├── isi.py          # Insomnia Severity Index
├── gad7.py         # Generalized Anxiety Disorder 7
├── phq9.py         # Patient Health Questionnaire 9
├── ess.py          # Epworth Sleepiness Scale
└── psqi.py         # Pittsburgh Sleep Quality Index
```

## MODELS (SQLAlchemy)

7 models: `QuestionnaireTemplate`, `QuestionnaireVersion`, `QuestionnaireAssignment`, `QuestionnaireResponse`, `QuestionnaireAnswer`, `ExportJob`, `AuditLog`

## CONVENTIONS

- **Framework:** FastAPI + SQLAlchemy 2.0 + Pydantic v2
- **Architecture:** Router → Service → Repository → Model with Pydantic schemas for I/O
- **Scoring:** Strategy pattern via `BaseScorer` with registry
- **Tests:** pytest with 39 passing tests
- **DB:** PostgreSQL via SQLAlchemy async session
- **Deployment:** Bare host service started by root `deploy.sh`, served at `/q/*` behind the host reverse proxy

## ANTI-PATTERNS

- **NEVER** put business logic in routers — use services layer
- **ALWAYS** use Pydantic schemas for request/response validation
- **AVOID** raw SQL — prefer SQLAlchemy ORM queries
- **DO NOT** couple scoring logic to API layer — scorers should be testable independently
