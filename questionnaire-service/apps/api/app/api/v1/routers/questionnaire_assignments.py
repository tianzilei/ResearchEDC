import uuid
from typing import Any

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.schemas.questionnaire_assignment import (
    AssignmentCreate,
    AssignmentBulkCreate,
    AssignmentRead,
    AssignmentStatusUpdate,
)
from app.services.assignment_service import AssignmentService

router = APIRouter(prefix="/questionnaires/assignments", tags=["Assignments"])


@router.post("", response_model=AssignmentRead, status_code=201)
async def create_assignment(
    data: AssignmentCreate,
    created_by: uuid.UUID = Query(default=None),
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = AssignmentService(db)
    return await service.create(data, created_by or uuid.uuid4())


@router.post("/bulk-create", response_model=list[AssignmentRead])
async def bulk_create_assignments(
    data: AssignmentBulkCreate,
    created_by: uuid.UUID = Query(default=None),
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = AssignmentService(db)
    return await service.bulk_create(data, created_by or uuid.uuid4())


@router.get("", response_model=list[AssignmentRead])
async def list_assignments(
    study_id: uuid.UUID,
    subject_id: uuid.UUID | None = None,
    status: str | None = None,
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=100, ge=1, le=500),
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = AssignmentService(db)
    return await service.list_by_study(
        study_id=study_id,
        subject_id=subject_id,
        status=status,
        skip=skip,
        limit=limit,
    )


@router.get("/by-subject/{subject_id}", response_model=list[AssignmentRead])
async def list_assignments_by_subject(
    subject_id: uuid.UUID,
    status: str | None = None,
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=100, ge=1, le=500),
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = AssignmentService(db)
    return await service.list_by_subject(
        subject_id=subject_id,
        status=status,
        skip=skip,
        limit=limit,
    )


@router.get("/{assignment_id}", response_model=AssignmentRead)
async def get_assignment(
    assignment_id: uuid.UUID,
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = AssignmentService(db)
    return await service.get(assignment_id)


@router.patch("/{assignment_id}", response_model=AssignmentRead)
async def update_assignment_status(
    assignment_id: uuid.UUID,
    data: AssignmentStatusUpdate,
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = AssignmentService(db)
    return await service.update_status(assignment_id, data.status)
