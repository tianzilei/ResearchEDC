from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.core.config import settings
from app.api.v1.routers import (
    questionnaire_templates,
    questionnaire_versions,
    questionnaire_assignments,
    questionnaire_public,
    questionnaire_responses,
    questionnaire_exports,
    audit_logs,
    events_webhook,
)

app = FastAPI(
    title="ResearchEDF Questionnaire Service",
    description="ResearchEDF Questionnaire Service API",
    version="0.1.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.cors_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(questionnaire_templates.router, prefix="/api/v1")
app.include_router(questionnaire_versions.router, prefix="/api/v1")
app.include_router(questionnaire_assignments.router, prefix="/api/v1")
app.include_router(questionnaire_public.router, prefix="/api/v1")
app.include_router(questionnaire_responses.router, prefix="/api/v1")
app.include_router(questionnaire_exports.router, prefix="/api/v1")
app.include_router(audit_logs.router, prefix="/api/v1")
app.include_router(events_webhook.router, prefix="/api/v1")


@app.get("/health")
async def health() -> dict:
    return {"status": "ok", "service": "questionnaire-service", "version": "0.1.0"}
