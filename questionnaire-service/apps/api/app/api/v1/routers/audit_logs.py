import uuid
from datetime import datetime
from typing import Any

from fastapi import APIRouter, Depends, Query
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.schemas.audit_log import AuditLogRead
from app.services.audit_service import AuditService

router = APIRouter(prefix="/audit-logs", tags=["Audit"])


@router.get("/entity/{entity_type}/{entity_id}", response_model=list[AuditLogRead])
async def get_entity_audit_logs(
    entity_type: str,
    entity_id: uuid.UUID,
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=50, ge=1, le=200),
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = AuditService(db)
    return await service.list_by_entity(
        entity_type=entity_type,
        entity_id=entity_id,
        skip=skip,
        limit=limit,
    )


@router.get("/search", response_model=list[AuditLogRead])
async def search_audit_logs(
    study_id: uuid.UUID | None = None,
    entity_type: str | None = None,
    action: str | None = None,
    operator_id: uuid.UUID | None = None,
    date_from: datetime | None = None,
    date_to: datetime | None = None,
    skip: int = Query(default=0, ge=0),
    limit: int = Query(default=50, ge=1, le=200),
    db: AsyncSession = Depends(get_db),
) -> Any:
    service = AuditService(db)
    return await service.search(
        study_id=study_id,
        entity_type=entity_type,
        action=action,
        operator_id=operator_id,
        date_from=date_from,
        date_to=date_to,
        skip=skip,
        limit=limit,
    )
