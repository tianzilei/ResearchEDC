import uuid
from typing import Any

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.schemas.questionnaire_response import (
    ResponseRead,
    ResponseReview,
    ResponseCorrection,
)
from app.services.response_service import ResponseService

router = APIRouter(prefix="/questionnaires/responses", tags=["Responses"])


@router.get("", response_model=list[ResponseRead])
async def list_responses(
    study_id: uuid.UUID | None = None,
    subject_id: uuid.UUID | None = None,
    visit_id: uuid.UUID | None = None,
    status: str | None = None,
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=100, ge=1, le=500),
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = ResponseService(db)
    return await service.list_by_study(
        study_id=study_id,
        subject_id=subject_id,
        visit_id=visit_id,
        status=status,
        skip=skip,
        limit=limit,
    )


@router.get("/{response_id}", response_model=ResponseRead)
async def get_response(
    response_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = ResponseService(db)
    return await service.get(response_id)


@router.post("/{response_id}/review", response_model=ResponseRead)
async def review_response(
    response_id: uuid.UUID,
    data: ResponseReview,
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = ResponseService(db)
    return await service.review(response_id, data)


@router.post("/{response_id}/lock", response_model=ResponseRead)
async def lock_response(
    response_id: uuid.UUID,
    reason: str | None = None,
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = ResponseService(db)
    return await service.lock(response_id, reason)


@router.post("/{response_id}/correction", response_model=ResponseRead)
async def correct_response(
    response_id: uuid.UUID,
    data: ResponseCorrection,
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = ResponseService(db)
    return await service.correct(response_id, data)
