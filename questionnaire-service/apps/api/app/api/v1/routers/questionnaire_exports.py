import uuid
from typing import Any

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.schemas.export import ExportRequest, ExportJobRead
from app.models.export_job import ExportJob
from app.repositories.base import BaseRepository
from app.core.exceptions import NotFoundError

router = APIRouter(prefix="/questionnaires/export", tags=["Export"])


@router.post("", response_model=ExportJobRead, status_code=201)
async def create_export_job(
    data: ExportRequest,
    requested_by: uuid.UUID | None = None,
    db: AsyncSession = Depends(get_db),
) -> Any:
    repo = BaseRepository(ExportJob, db)
    job = await repo.create(
        study_id=data.study_id,
        requested_by=requested_by or uuid.uuid4(),
        export_type=data.layout,
        export_format=data.export_format,
        query_params=data.model_dump(),
    )
    return ExportJobRead.model_validate(job)


@router.get("/{job_id}", response_model=ExportJobRead)
async def get_export_job(
    job_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
) -> Any:
    repo = BaseRepository(ExportJob, db)
    job = await repo.get(job_id)
    if not job:
        raise NotFoundError("ExportJob", str(job_id))
    return ExportJobRead.model_validate(job)


@router.get("/{job_id}/download")
async def download_export(
    job_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
) -> dict[str, Any]:
    repo = BaseRepository(ExportJob, db)
    job = await repo.get(job_id)
    if not job:
        raise NotFoundError("ExportJob", str(job_id))
    if job.status != "completed":
        return {"status": job.status, "message": "Export not ready yet"}
    if not job.file_path:
        return {"status": "error", "message": "No file available"}
    return {
        "status": "completed",
        "file_path": job.file_path,
        "format": job.export_format,
    }
