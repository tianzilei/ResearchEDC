import uuid
from datetime import datetime, timedelta, timezone

from sqlalchemy.ext.asyncio import AsyncSession

from app.repositories.assignment_repo import AssignmentRepo
from app.core.exceptions import NotFoundError
from app.core.security import generate_public_token, hash_public_token
from app.core.config import settings
from app.schemas.questionnaire_assignment import (
    AssignmentCreate,
    AssignmentBulkCreate,
    AssignmentRead,
)


def _utcnow() -> datetime:
    return datetime.now(timezone.utc).replace(tzinfo=None)


class AssignmentService:
    def __init__(self, session: AsyncSession):
        self.repo = AssignmentRepo(session)

    def _compute_token_expiry(self, due_at: datetime | None) -> datetime:
        now = _utcnow()
        if due_at:
            if due_at.tzinfo is not None:
                due_at = due_at.replace(tzinfo=None)
            return due_at.replace(hour=23, minute=59, second=59, microsecond=0)
        return now + timedelta(hours=settings.public_token_expire_hours)

    async def create(
        self, data: AssignmentCreate, created_by: uuid.UUID
    ) -> AssignmentRead:
        token = generate_public_token()
        token_hash = hash_public_token(token)
        expires_at = self._compute_token_expiry(data.due_at)
        assignment = await self.repo.create(
            study_id=data.study_id,
            subject_id=data.subject_id,
            visit_id=data.visit_id,
            randomization_arm_id=data.randomization_arm_id,
            questionnaire_version_id=data.questionnaire_version_id,
            due_at=data.due_at,
            public_token_hash=token_hash,
            token_expires_at=expires_at,
            created_by=created_by,
        )
        result = AssignmentRead.model_validate(assignment)
        result.has_token = True
        return result

    async def get(self, assignment_id: uuid.UUID) -> AssignmentRead:
        assignment = await self.repo.get(assignment_id)
        if not assignment:
            raise NotFoundError("Assignment", str(assignment_id))
        return AssignmentRead.model_validate(assignment)

    async def list_by_study(
        self,
        study_id: uuid.UUID,
        subject_id: uuid.UUID | None = None,
        status: str | None = None,
        skip: int = 0,
        limit: int = 100,
    ) -> list[AssignmentRead]:
        assignments = await self.repo.list_by_study(
            study_id=study_id,
            subject_id=subject_id,
            status=status,
            skip=skip,
            limit=limit,
        )
        return [AssignmentRead.model_validate(a) for a in assignments]

    async def list_by_subject(
        self,
        subject_id: uuid.UUID,
        status: str | None = None,
        skip: int = 0,
        limit: int = 100,
    ) -> list[AssignmentRead]:
        from app.models.questionnaire_assignment import QuestionnaireAssignment
        filters = {"subject_id": subject_id}
        if status:
            filters["status"] = status
        assignments = await self.repo.list(
            skip=skip,
            limit=limit,
            filters=filters,
            order_by="-created_at",
        )
        return [AssignmentRead.model_validate(a) for a in assignments]

    async def bulk_create(
        self, data: AssignmentBulkCreate, created_by: uuid.UUID
    ) -> list[AssignmentRead]:
        assignments_data = []
        for a in data.assignments:
            token = generate_public_token()
            token_hash = hash_public_token(token)
            assignments_data.append(
                {
                    "study_id": a.study_id,
                    "subject_id": a.subject_id,
                    "visit_id": a.visit_id,
                    "questionnaire_version_id": a.questionnaire_version_id,
                    "due_at": a.due_at,
                    "randomization_arm_id": a.randomization_arm_id,
                    "public_token_hash": token_hash,
                    "token_expires_at": _utcnow()
                    + timedelta(hours=settings.public_token_expire_hours),
                    "created_by": created_by,
                }
            )
        assignments = await self.repo.bulk_create(assignments_data)
        return [AssignmentRead.model_validate(a) for a in assignments]

    async def update_status(
        self, assignment_id: uuid.UUID, status: str
    ) -> AssignmentRead:
        assignment = await self.repo.update(assignment_id, status=status)
        if not assignment:
            raise NotFoundError("Assignment", str(assignment_id))
        return AssignmentRead.model_validate(assignment)
