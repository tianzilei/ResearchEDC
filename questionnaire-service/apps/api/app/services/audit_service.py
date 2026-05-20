import uuid
from datetime import datetime

from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.audit_repo import AuditRepo
from app.schemas.audit_log import AuditLogCreate, AuditLogRead


class AuditService:
    def __init__(self, session: AsyncSession):
        self.repo = AuditRepo(session)

    async def create(self, data: AuditLogCreate) -> AuditLogRead:
        entry = await self.repo.create(
            study_id=data.study_id,
            entity_type=data.entity_type,
            entity_id=data.entity_id,
            action=data.action,
            old_value_json=data.old_value_json,
            new_value_json=data.new_value_json,
            reason=data.reason,
            operator_id=data.operator_id,
            operator_role=data.operator_role,
            ip_hash=data.ip_hash,
            user_agent=data.user_agent,
        )
        return AuditLogRead.model_validate(entry)

    async def list_by_entity(
        self,
        entity_type: str,
        entity_id: uuid.UUID,
        skip: int = 0,
        limit: int = 50,
    ) -> list[AuditLogRead]:
        entries = await self.repo.list_by_entity(
            entity_type=entity_type,
            entity_id=entity_id,
            skip=skip,
            limit=limit,
        )
        return [AuditLogRead.model_validate(e) for e in entries]

    async def search(
        self,
        study_id: uuid.UUID | None = None,
        entity_type: str | None = None,
        action: str | None = None,
        operator_id: uuid.UUID | None = None,
        date_from: datetime | None = None,
        date_to: datetime | None = None,
        skip: int = 0,
        limit: int = 50,
    ) -> list[AuditLogRead]:
        entries = await self.repo.search(
            study_id=study_id,
            entity_type=entity_type,
            action=action,
            operator_id=operator_id,
            date_from=date_from,
            date_to=date_to,
            skip=skip,
            limit=limit,
        )
        return [AuditLogRead.model_validate(e) for e in entries]
