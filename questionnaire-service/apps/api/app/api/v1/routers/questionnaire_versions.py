import uuid
from typing import Any

from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.schemas.questionnaire_version import (
    QuestionnaireVersionCreate,
    QuestionnaireVersionRead,
    QuestionnaireVersionUpdate,
    VersionPublishRequest,
)
from app.services.questionnaire_version_service import (
    QuestionnaireVersionService,
)

router = APIRouter(prefix="/questionnaires", tags=["Versions"])


@router.post(
    "/templates/{template_id}/versions",
    response_model=QuestionnaireVersionRead,
    status_code=201,
)
async def create_version(
    template_id: uuid.UUID,
    data: QuestionnaireVersionCreate,
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = QuestionnaireVersionService(db)
    return await service.create(template_id, data)


@router.get(
    "/templates/{template_id}/versions",
    response_model=list[QuestionnaireVersionRead],
)
async def list_versions(
    template_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = QuestionnaireVersionService(db)
    return await service.list_by_template(template_id)


@router.get(
    "/versions/{version_id}",
    response_model=QuestionnaireVersionRead,
)
async def get_version(
    version_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = QuestionnaireVersionService(db)
    return await service.get(version_id)


@router.patch(
    "/versions/{version_id}",
    response_model=QuestionnaireVersionRead,
)
async def update_version(
    version_id: uuid.UUID,
    data: QuestionnaireVersionUpdate,
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = QuestionnaireVersionService(db)
    return await service.update(version_id, data)


@router.post(
    "/versions/{version_id}/publish",
    response_model=QuestionnaireVersionRead,
)
async def publish_version(
    version_id: uuid.UUID,
    data: VersionPublishRequest,
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = QuestionnaireVersionService(db)
    return await service.publish(version_id, data.published_by)


@router.post(
    "/versions/{version_id}/retire",
    response_model=QuestionnaireVersionRead,
)
async def retire_version(
    version_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = QuestionnaireVersionService(db)
    return await service.retire(version_id)
