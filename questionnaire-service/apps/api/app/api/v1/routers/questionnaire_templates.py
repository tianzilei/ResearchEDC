import uuid
from typing import Any

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.auth import get_current_user, get_optional_user, require_permission, AuthUser
from app.core.database import get_db
from app.schemas.questionnaire_template import (
    QuestionnaireTemplateCreate,
    QuestionnaireTemplateRead,
    QuestionnaireTemplateUpdate,
)
from app.services.questionnaire_template_service import (
    QuestionnaireTemplateService,
)

router = APIRouter(prefix="/questionnaires/templates", tags=["Templates"])


@router.post("", response_model=QuestionnaireTemplateRead, status_code=201)
async def create_template(
    data: QuestionnaireTemplateCreate,
    user: AuthUser = Depends(require_permission("crf:design")),
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = QuestionnaireTemplateService(db)
    return await service.create(data, user.id)


@router.get("", response_model=list[QuestionnaireTemplateRead])
async def list_templates(
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=100, ge=1, le=500),
    study_id: uuid.UUID | None = None,
    category: str | None = None,
    status: str | None = None,
    user: AuthUser = Depends(get_optional_user),
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = QuestionnaireTemplateService(db)
    return await service.list(
        skip=skip,
        limit=limit,
        study_id=study_id,
        category=category,
        status=status,
    )


@router.get("/{template_id}", response_model=QuestionnaireTemplateRead)
async def get_template(
    template_id: uuid.UUID,
    user: AuthUser = Depends(get_optional_user),
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = QuestionnaireTemplateService(db)
    return await service.get(template_id)


@router.patch("/{template_id}", response_model=QuestionnaireTemplateRead)
async def update_template(
    template_id: uuid.UUID,
    data: QuestionnaireTemplateUpdate,
    user: AuthUser = Depends(require_permission("crf:design")),
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = QuestionnaireTemplateService(db)
    return await service.update(template_id, data)


@router.delete("/{template_id}", status_code=204)
async def delete_template(
    template_id: uuid.UUID,
    user: AuthUser = Depends(require_permission("crf:design")),
    db: AsyncSession = Depends(get_db),
) -> None:
    service = QuestionnaireTemplateService(db)
    await service.delete(template_id)
