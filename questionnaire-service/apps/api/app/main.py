import logging
from contextlib import asynccontextmanager

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

logger = logging.getLogger(__name__)
_minio_connected = False


async def _init_minio() -> bool:
    global _minio_connected
    try:
        from minio import Minio

        client = Minio(
            settings.minio_endpoint,
            access_key=settings.minio_access_key,
            secret_key=settings.minio_secret_key,
            secure=settings.minio_secure,
        )
        if client.bucket_exists(settings.minio_bucket):
            logger.info("MinIO connected — bucket '%s' ready", settings.minio_bucket)
        else:
            client.make_bucket(settings.minio_bucket)
            logger.info("MinIO connected — bucket '%s' created", settings.minio_bucket)
        _minio_connected = True
        return True
    except ImportError:
        logger.warning("MinIO package not installed — using local storage fallback")
        return False
    except Exception as exc:
        logger.warning("MinIO unavailable (%s) — using local storage fallback", exc)
        return False


@asynccontextmanager
async def lifespan(app: FastAPI):
    await _init_minio()
    yield


app = FastAPI(
    title="ResearchEDC Questionnaire Service",
    description="ResearchEDC Questionnaire Service API",
    version="0.1.0",
    docs_url="/docs",
    redoc_url="/redoc",
    lifespan=lifespan,
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
    return {
        "status": "ok",
        "service": "questionnaire-service",
        "version": "0.1.0",
        "minio": "connected" if _minio_connected else "unavailable",
    }
