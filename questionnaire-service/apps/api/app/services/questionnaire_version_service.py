import hashlib
import json
import uuid
from datetime import datetime, timezone


def _utcnow() -> datetime:
    return datetime.now(timezone.utc).replace(tzinfo=None)

from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.questionnaire_version_repo import QuestionnaireVersionRepo
from app.core.exceptions import NotFoundError, DuplicateError, PublishError
from app.schemas.questionnaire_version import (
    QuestionnaireVersionCreate,
    QuestionnaireVersionUpdate,
    QuestionnaireVersionRead,
)


class QuestionnaireVersionService:
    def __init__(self, session: AsyncSession):
        self.repo = QuestionnaireVersionRepo(session)

    def _compute_schema_hash(self, schema: dict) -> str:
        raw = json.dumps(schema, sort_keys=True, ensure_ascii=False)
        return hashlib.sha256(raw.encode()).hexdigest()

    async def create(
        self,
        template_id: uuid.UUID,
        data: QuestionnaireVersionCreate,
    ) -> QuestionnaireVersionRead:
        exists = await self.repo.version_exists(template_id, data.version_no)
        if exists:
            raise DuplicateError(
                f"Version '{data.version_no}' already exists for this template"
            )
        schema_hash = self._compute_schema_hash(data.surveyjs_schema)
        version = await self.repo.create(
            template_id=template_id,
            version_no=data.version_no,
            surveyjs_schema=data.surveyjs_schema,
            validation_schema=data.validation_schema,
            scoring_schema=data.scoring_schema,
            language=data.language,
            schema_hash=schema_hash,
        )
        return QuestionnaireVersionRead.model_validate(version)

    async def get(self, version_id: uuid.UUID) -> QuestionnaireVersionRead:
        version = await self.repo.get(version_id)
        if not version:
            raise NotFoundError("QuestionnaireVersion", str(version_id))
        return QuestionnaireVersionRead.model_validate(version)

    async def list_by_template(
        self, template_id: uuid.UUID
    ) -> list[QuestionnaireVersionRead]:
        versions = await self.repo.list_by_template(template_id)
        return [QuestionnaireVersionRead.model_validate(v) for v in versions]

    async def update(
        self, version_id: uuid.UUID, data: QuestionnaireVersionUpdate
    ) -> QuestionnaireVersionRead:
        version = await self.repo.get(version_id)
        if not version:
            raise NotFoundError("QuestionnaireVersion", str(version_id))
        if version.status != "draft":
            raise PublishError(
                "Only draft versions can be edited"
            )
        update_kwargs = {}
        if data.surveyjs_schema is not None:
            update_kwargs["surveyjs_schema"] = data.surveyjs_schema
            update_kwargs["schema_hash"] = self._compute_schema_hash(
                data.surveyjs_schema
            )
        if data.validation_schema is not None:
            update_kwargs["validation_schema"] = data.validation_schema
        if data.scoring_schema is not None:
            update_kwargs["scoring_schema"] = data.scoring_schema
        updated = await self.repo.update(version_id, **update_kwargs)
        return QuestionnaireVersionRead.model_validate(updated)

    async def publish(
        self, version_id: uuid.UUID, published_by: uuid.UUID
    ) -> QuestionnaireVersionRead:
        version = await self.repo.get(version_id)
        if not version:
            raise NotFoundError("QuestionnaireVersion", str(version_id))
        if version.status != "draft":
            raise PublishError(
                f"Cannot publish version with status '{version.status}'"
            )
        updated = await self.repo.update(
            version_id,
            status="published",
            published_by=published_by,
            published_at=_utcnow(),
        )
        return QuestionnaireVersionRead.model_validate(updated)

    async def retire(self, version_id: uuid.UUID) -> QuestionnaireVersionRead:
        version = await self.repo.get(version_id)
        if not version:
            raise NotFoundError("QuestionnaireVersion", str(version_id))
        if version.status != "published":
            raise PublishError(
                "Only published versions can be retired"
            )
        updated = await self.repo.update(
            version_id, status="retired"
        )
        return QuestionnaireVersionRead.model_validate(updated)
