import uuid
from datetime import datetime, timezone

from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.assignment_repo import AssignmentRepo
from app.repositories.questionnaire_version_repo import QuestionnaireVersionRepo
from app.core.exceptions import TokenExpiredError, TokenInvalidError, NotFoundError
from app.core.security import verify_public_token, hash_public_token, generate_public_token
from app.core.config import settings


class PublicTokenInfo:
    def __init__(
        self,
        assignment_id: uuid.UUID,
        subject_id: uuid.UUID,
        study_id: uuid.UUID,
        questionnaire_code: str,
        questionnaire_name: str,
        version_no: str,
        surveyjs_schema: dict,
        status: str,
        due_at: datetime | None,
    ):
        self.assignment_id = assignment_id
        self.subject_id = subject_id
        self.study_id = study_id
        self.questionnaire_code = questionnaire_code
        self.questionnaire_name = questionnaire_name
        self.version_no = version_no
        self.surveyjs_schema = surveyjs_schema
        self.status = status
        self.due_at = due_at


class TokenService:
    def __init__(self, session: AsyncSession):
        self.assignment_repo = AssignmentRepo(session)
        self.version_repo = QuestionnaireVersionRepo(session)

    async def validate_token(self, token: str) -> PublicTokenInfo:
        token_hash = hash_public_token(token)
        assignment = await self.assignment_repo.get_by_token_hash(token_hash)
        if not assignment:
            raise TokenInvalidError()
        if (
            assignment.token_expires_at
            and assignment.token_expires_at.replace(tzinfo=timezone.utc)
            < datetime.now(timezone.utc)
        ):
            raise TokenExpiredError()
        version = await self.version_repo.get(
            assignment.questionnaire_version_id
        )
        if not version:
            raise NotFoundError(
                "QuestionnaireVersion",
                str(assignment.questionnaire_version_id),
            )
        template = version.template
        return PublicTokenInfo(
            assignment_id=assignment.id,
            subject_id=assignment.subject_id,
            study_id=assignment.study_id,
            questionnaire_code=template.code,
            questionnaire_name=template.name,
            version_no=version.version_no,
            surveyjs_schema=version.surveyjs_schema,
            status=assignment.status,
            due_at=assignment.due_at,
        )
