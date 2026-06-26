import uuid

from sqlalchemy import select, or_

from app.models.questionnaire_template import QuestionnaireTemplate
from app.repositories.base import BaseRepository


class QuestionnaireTemplateRepo(BaseRepository[QuestionnaireTemplate]):
    def __init__(self, session):
        super().__init__(QuestionnaireTemplate, session)

    async def search(
        self,
        query: str | None = None,
        category: str | None = None,
        status: str | None = None,
        study_id: uuid.UUID | None = None,
        skip: int = 0,
        limit: int = 100,
    ) -> list[QuestionnaireTemplate]:
        stmt = select(QuestionnaireTemplate)
        if query:
            stmt = stmt.where(
                or_(
                    QuestionnaireTemplate.code.ilike(f"%{query}%"),
                    QuestionnaireTemplate.name.ilike(f"%{query}%"),
                )
            )
        if category:
            stmt = stmt.where(QuestionnaireTemplate.category == category)
        if status:
            stmt = stmt.where(QuestionnaireTemplate.status == status)
        if study_id:
            stmt = stmt.where(
                or_(
                    QuestionnaireTemplate.study_id == study_id,
                    QuestionnaireTemplate.study_id.is_(None),
                )
            )
        stmt = stmt.offset(skip).limit(limit).order_by(QuestionnaireTemplate.updated_at.desc())
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

    async def get_by_code(self, code: str) -> QuestionnaireTemplate | None:
        stmt = select(QuestionnaireTemplate).where(
            QuestionnaireTemplate.code == code
        )
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()
