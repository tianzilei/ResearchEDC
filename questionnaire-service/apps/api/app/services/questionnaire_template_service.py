import uuid

from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.questionnaire_template_repo import QuestionnaireTemplateRepo
from app.core.exceptions import NotFoundError, DuplicateError
from app.schemas.questionnaire_template import (
    QuestionnaireTemplateCreate,
    QuestionnaireTemplateUpdate,
    QuestionnaireTemplateRead,
)


class QuestionnaireTemplateService:
    def __init__(self, session: AsyncSession):
        self.repo = QuestionnaireTemplateRepo(session)

    async def create(
        self, data: QuestionnaireTemplateCreate, created_by: uuid.UUID
    ) -> QuestionnaireTemplateRead:
        existing = await self.repo.get_by_code(data.code)
        if existing:
            raise DuplicateError(f"Template with code '{data.code}' already exists")
        template = await self.repo.create(
            code=data.code,
            name=data.name,
            description=data.description,
            category=data.category,
            study_id=data.study_id,
            created_by=created_by,
        )
        return QuestionnaireTemplateRead.model_validate(template)

    async def get(self, template_id: uuid.UUID) -> QuestionnaireTemplateRead:
        template = await self.repo.get(template_id)
        if not template:
            raise NotFoundError("QuestionnaireTemplate", str(template_id))
        return QuestionnaireTemplateRead.model_validate(template)

    async def list(
        self,
        skip: int = 0,
        limit: int = 100,
        study_id: uuid.UUID | None = None,
        category: str | None = None,
        status: str | None = None,
    ) -> list[QuestionnaireTemplateRead]:
        filters = {}
        if study_id:
            filters["study_id"] = study_id
        if category:
            filters["category"] = category
        if status:
            filters["status"] = status
        templates = await self.repo.list(
            skip=skip, limit=limit, filters=filters, order_by="-updated_at"
        )
        return [
            QuestionnaireTemplateRead.model_validate(t) for t in templates
        ]

    async def update(
        self, template_id: uuid.UUID, data: QuestionnaireTemplateUpdate
    ) -> QuestionnaireTemplateRead:
        template = await self.repo.update(
            template_id,
            name=data.name,
            description=data.description,
            category=data.category,
            status=data.status,
        )
        if not template:
            raise NotFoundError("QuestionnaireTemplate", str(template_id))
        return QuestionnaireTemplateRead.model_validate(template)

    async def delete(self, template_id: uuid.UUID) -> None:
        deleted = await self.repo.delete(template_id)
        if not deleted:
            raise NotFoundError("QuestionnaireTemplate", str(template_id))
