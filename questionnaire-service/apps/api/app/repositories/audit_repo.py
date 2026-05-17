import uuid
from datetime import datetime

from sqlalchemy import select, and_

from app.models.audit_log import QuestionnaireAuditLog
from app.repositories.base import BaseRepository


class AuditRepo(BaseRepository[QuestionnaireAuditLog]):
    def __init__(self, session):
        super().__init__(QuestionnaireAuditLog, session)

    async def list_by_entity(
        self,
        entity_type: str,
        entity_id: uuid.UUID,
        skip: int = 0,
        limit: int = 50,
    ) -> list[QuestionnaireAuditLog]:
        stmt = (
            select(QuestionnaireAuditLog)
            .where(
                and_(
                    QuestionnaireAuditLog.entity_type == entity_type,
                    QuestionnaireAuditLog.entity_id == entity_id,
                )
            )
            .offset(skip)
            .limit(limit)
            .order_by(QuestionnaireAuditLog.created_at.desc())
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

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
    ) -> list[QuestionnaireAuditLog]:
        conditions = []
        if study_id:
            conditions.append(QuestionnaireAuditLog.study_id == study_id)
        if entity_type:
            conditions.append(
                QuestionnaireAuditLog.entity_type == entity_type
            )
        if action:
            conditions.append(QuestionnaireAuditLog.action == action)
        if operator_id:
            conditions.append(
                QuestionnaireAuditLog.operator_id == operator_id
            )
        if date_from:
            conditions.append(
                QuestionnaireAuditLog.created_at >= date_from
            )
        if date_to:
            conditions.append(
                QuestionnaireAuditLog.created_at <= date_to
            )
        stmt = (
            select(QuestionnaireAuditLog)
            .where(and_(*conditions) if conditions else True)
            .offset(skip)
            .limit(limit)
            .order_by(QuestionnaireAuditLog.created_at.desc())
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())
