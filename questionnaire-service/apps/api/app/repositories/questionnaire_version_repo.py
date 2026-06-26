import uuid

from sqlalchemy import select, and_

from app.models.questionnaire_version import QuestionnaireVersion
from app.repositories.base import BaseRepository


class QuestionnaireVersionRepo(BaseRepository[QuestionnaireVersion]):
    def __init__(self, session):
        super().__init__(QuestionnaireVersion, session)

    async def list_by_template(
        self, template_id: uuid.UUID
    ) -> list[QuestionnaireVersion]:
        stmt = (
            select(QuestionnaireVersion)
            .where(QuestionnaireVersion.template_id == template_id)
            .order_by(QuestionnaireVersion.created_at.desc())
        )
        result = await self.session.execute(stmt)
        return list(result.scalars().all())

    async def get_latest_by_template(
        self, template_id: uuid.UUID
    ) -> QuestionnaireVersion | None:
        stmt = (
            select(QuestionnaireVersion)
            .where(QuestionnaireVersion.template_id == template_id)
            .order_by(QuestionnaireVersion.created_at.desc())
            .limit(1)
        )
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def get_published(
        self, template_id: uuid.UUID
    ) -> QuestionnaireVersion | None:
        stmt = (
            select(QuestionnaireVersion)
            .where(
                and_(
                    QuestionnaireVersion.template_id == template_id,
                    QuestionnaireVersion.status == "published",
                )
            )
            .order_by(QuestionnaireVersion.published_at.desc())
            .limit(1)
        )
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none()

    async def version_exists(
        self, template_id: uuid.UUID, version_no: str
    ) -> bool:
        stmt = select(QuestionnaireVersion).where(
            and_(
                QuestionnaireVersion.template_id == template_id,
                QuestionnaireVersion.version_no == version_no,
            )
        )
        result = await self.session.execute(stmt)
        return result.scalar_one_or_none() is not None
