import uuid
from datetime import datetime, timezone


def _utcnow() -> datetime:
    return _utcnow().replace(tzinfo=None)


from app.workers.celery_app import celery_app
from app.repositories.base import BaseRepository
from app.models.export_job import ExportJob
from app.models.questionnaire_response import QuestionnaireResponse
from app.services.export_service import (
    export_long,
    export_wide,
    export_score,
    save_to_file,
)


@celery_app.task(bind=True, max_retries=3)
def run_export(self, job_id: str) -> dict:
    from app.core.database import get_session_factory
    import asyncio

    async def _run():
        async with get_session_factory()() as session:
            repo = BaseRepository(ExportJob, session)
            job = await repo.get(uuid.UUID(job_id))
            if not job:
                raise ValueError(f"Export job not found: {job_id}")
            await repo.update(
                uuid.UUID(job_id),
                status="running",
                started_at=_utcnow(),
            )
            try:
                params = job.query_params
                layout = params.get("layout", "wide")
                export_format = params.get("export_format", "xlsx")
                q_codes = params.get("questionnaire_codes", [])
                responses = await _fetch_responses(session, q_codes)
                if layout == "long":
                    buffer = export_long(responses, export_format)
                elif layout == "score":
                    buffer = export_score(responses, export_format)
                else:
                    buffer = export_wide(responses, export_format)
                file_path = f"/tmp/exports/{job_id}.{export_format}"
                save_to_file(buffer, file_path)
                await repo.update(
                    uuid.UUID(job_id),
                    status="completed",
                    finished_at=_utcnow(),
                    file_path=file_path,
                )
            except Exception as exc:
                await repo.update(
                    uuid.UUID(job_id),
                    status="failed",
                    error_message=str(exc),
                    finished_at=_utcnow(),
                )
                raise

    asyncio.run(_run())
    return {"status": "completed"}


async def _fetch_responses(session, q_codes: list[str]) -> list[dict]:
    from sqlalchemy import select

    stmt = select(QuestionnaireResponse).limit(1000)
    result = await session.execute(stmt)
    responses = list(result.scalars().all())
    rows = []
    for r in responses:
        rows.append(
            {
                "response_id": str(r.id),
                "subject_id": str(r.subject_id),
                "assignment_id": str(r.assignment_id),
                "raw_response_json": r.raw_response_json,
                "score_json": r.score_json,
                "total_score": float(r.total_score) if r.total_score else None,
                "submitted_at": r.submitted_at,
            }
        )
    return rows
