import uuid

from sqlalchemy import select, and_

from app.models.questionnaire_response import QuestionnaireResponse
from app.repositories.base import BaseRepository


class ResponseRepo(BaseRepository[QuestionnaireResponse]):
    def __init__(self, session):
        super().__init__(QuestionnaireResponse, session)

    async def get_by_assignment(
        self, assignment_id: uuid.UUID
    ) -> QuestionnaireResponse | None:
        stmt = select(QuestionnaireResponse).where(
            QuestionnaireResponse.assignment_id == assignment_id
        )
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def list_by_study(
        self,
        study_id: uuid.UUID,
        subject_id: uuid.UUID | None = None,
        visit_id: uuid.UUID | None = None,
        status: str | None = None,
        skip: int = 0,
        limit: int = 100,
    ) -> list[QuestionnaireResponse]:
        conditions = [QuestionnaireResponse.id.isnot(None)]
        if subject_id:
            conditions.append(QuestionnaireResponse.subject_id == subject_id)
        if visit_id:
            conditions.append(QuestionnaireResponse.visit_id == visit_id)
        if status:
            conditions.append(QuestionnaireResponse.status == status)
        stmt = (
            select(QuestionnaireResponse)
            .where(and_(*conditions))
            .offset(skip)
            .limit(limit)
            .order_by(QuestionnaireResponse.created_at.desc())
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())
