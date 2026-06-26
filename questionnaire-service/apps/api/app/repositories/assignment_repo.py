import uuid
from datetime import datetime

from sqlalchemy import select, and_

from app.models.questionnaire_assignment import QuestionnaireAssignment
from app.repositories.base import BaseRepository


class AssignmentRepo(BaseRepository[QuestionnaireAssignment]):
    def __init__(self, session):
        super().__init__(QuestionnaireAssignment, session)

    async def list_by_study(
        self,
        study_id: uuid.UUID,
        subject_id: uuid.UUID | None = None,
        status: str | None = None,
        skip: int = 0,
        limit: int = 100,
    ) -> list[QuestionnaireAssignment]:
        stmt = select(QuestionnaireAssignment).where(
            QuestionnaireAssignment.study_id == study_id
        )
        if subject_id:
            stmt = stmt.where(QuestionnaireAssignment.subject_id == subject_id)
        if status:
            stmt = stmt.where(QuestionnaireAssignment.status == status)
        stmt = stmt.offset(skip).limit(limit).order_by(QuestionnaireAssignment.created_at.desc())
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

    async def bulk_create(
        self, assignments: list[dict]
    ) -> list[QuestionnaireAssignment]:
        instances = [QuestionnaireAssignment(**data) for data in assignments]
        for instance in instances:
            self.session.add(instance)
        await self.session.flush()
        return instances

    async def get_expired_tokens(
        self,
    ) -> list[QuestionnaireAssignment]:
        stmt = select(QuestionnaireAssignment).where(
            and_(
                QuestionnaireAssignment.token_expires_at.isnot(None),
                QuestionnaireAssignment.token_expires_at < datetime.utcnow(),
                QuestionnaireAssignment.status == "pending",
            )
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

    async def get_by_token_hash(
        self, token_hash: str
    ) -> QuestionnaireAssignment | None:
        stmt = select(QuestionnaireAssignment).where(
            QuestionnaireAssignment.public_token_hash == token_hash
        )
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()
