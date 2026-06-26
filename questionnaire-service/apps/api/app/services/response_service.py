import inspect
import uuid
from datetime import datetime, timezone


def _utcnow() -> datetime:
    return datetime.now(timezone.utc).replace(tzinfo=None)

from sqlalchemy.ext.asyncio import AsyncSession

from app.models.questionnaire_assignment import QuestionnaireAssignment
from app.repositories.assignment_repo import AssignmentRepo
from app.repositories.response_repo import ResponseRepo
from app.models.questionnaire_answer import QuestionnaireAnswer
from app.repositories.audit_repo import AuditRepo
from app.core.exceptions import NotFoundError, ValidationError, LockedError
from app.schemas.questionnaire_response import (
    ResponseSubmit,
    ResponseDraft,
    ResponseRead,
    ResponseReview,
    ResponseCorrection,
)


class ResponseService:
    def __init__(self, session: AsyncSession):
        self.assignment_repo = AssignmentRepo(session)
        self.response_repo = ResponseRepo(session)
        self.audit_repo = AuditRepo(session)
        self.session = session

    async def submit(
        self,
        assignment_id: uuid.UUID,
        data: ResponseSubmit,
        ip_hash: str | None = None,
    ) -> ResponseRead:
        assignment = await self.assignment_repo.get(assignment_id)
        if not assignment:
            raise NotFoundError("Assignment", str(assignment_id))
        if assignment.status in ("locked", "submitted", "expired"):
            raise ValidationError(
                f"Cannot submit: assignment status is '{assignment.status}'"
            )
        existing = await self.response_repo.get_by_assignment(assignment_id)
        if existing:
            raise ValidationError("Response already exists for this assignment")
        response = await self.response_repo.create(
            assignment_id=assignment_id,
            subject_id=assignment.subject_id,
            visit_id=assignment.visit_id,
            questionnaire_version_id=assignment.questionnaire_version_id,
            raw_response_json=data.response,
            started_at=data.started_at,
            submitted_at=data.submitted_at or _utcnow(),
            device_info=data.device_info,
            ip_hash=ip_hash,
        )
        await self._expand_answers(response.id, data.response)
        await self.assignment_repo.update(
            assignment_id, status="submitted"
        )
        await self.audit_repo.create(
            entity_type="questionnaire_response",
            entity_id=response.id,
            action="submit",
            new_value_json={"raw_response": data.response},
            study_id=assignment.study_id,
        )
        return ResponseRead.model_validate(response)

    async def save_draft(
        self,
        assignment_id: uuid.UUID,
        data: ResponseDraft,
        ip_hash: str | None = None,
    ) -> ResponseRead:
        assignment = await self.assignment_repo.get(assignment_id)
        if not assignment:
            raise NotFoundError("Assignment", str(assignment_id))
        existing = await self.response_repo.get_by_assignment(assignment_id)
        if existing and existing.status in ("locked", "submitted"):
            raise ValidationError(
                f"Cannot save draft: response status is '{existing.status}'"
            )
        if existing:
            response = await self.response_repo.update(
                existing.id,
                raw_response_json=data.response,
                started_at=data.started_at,
            )
        else:
            response = await self.response_repo.create(
                assignment_id=assignment_id,
                subject_id=assignment.subject_id,
                visit_id=assignment.visit_id,
                questionnaire_version_id=assignment.questionnaire_version_id,
                raw_response_json=data.response,
                started_at=data.started_at,
                status="draft",
                ip_hash=ip_hash,
            )
        await self.assignment_repo.update(
            assignment_id, status="in_progress"
        )
        return ResponseRead.model_validate(response)

    async def get(self, response_id: uuid.UUID) -> ResponseRead:
        response = await self.response_repo.get(response_id)
        if not response:
            raise NotFoundError("Response", str(response_id))
        return ResponseRead.model_validate(response)

    async def list_by_study(
        self,
        study_id: uuid.UUID | None = None,
        subject_id: uuid.UUID | None = None,
        visit_id: uuid.UUID | None = None,
        status: str | None = None,
        skip: int = 0,
        limit: int = 100,
    ) -> list[ResponseRead]:
        responses = await self.response_repo.list_by_study(
            study_id=study_id,
            subject_id=subject_id,
            visit_id=visit_id,
            status=status,
            skip=skip,
            limit=limit,
        )
        return [ResponseRead.model_validate(r) for r in responses]

    async def review(
        self, response_id: uuid.UUID, data: ResponseReview
    ) -> ResponseRead:
        response = await self.response_repo.get(response_id)
        if not response:
            raise NotFoundError("Response", str(response_id))
        if response.status == "locked":
            raise LockedError()
        updated = await self.response_repo.update(
            response_id, status=data.status
        )
        await self.audit_repo.create(
            entity_type="questionnaire_response",
            entity_id=response_id,
            action=data.status,
            reason=data.reason,
        )
        return ResponseRead.model_validate(updated)

    async def lock(self, response_id: uuid.UUID, reason: str | None = None) -> ResponseRead:
        response = await self.response_repo.get(response_id)
        if not response:
            raise NotFoundError("Response", str(response_id))
        updated = await self.response_repo.update(
            response_id, status="locked"
        )
        await self.audit_repo.create(
            entity_type="questionnaire_response",
            entity_id=response_id,
            action="lock",
            reason=reason,
        )
        return ResponseRead.model_validate(updated)

    async def correct(
        self, response_id: uuid.UUID, data: ResponseCorrection
    ) -> ResponseRead:
        response = await self.response_repo.get(response_id)
        if not response:
            raise NotFoundError("Response", str(response_id))
        if response.status != "locked":
            raise ValidationError(
                "Only locked responses can be corrected"
            )
        old_value = response.raw_response_json
        updated = await self.response_repo.update(
            response_id,
            raw_response_json=data.correction_data,
        )
        await self.audit_repo.create(
            entity_type="questionnaire_response",
            entity_id=response_id,
            action="correction",
            old_value_json=old_value,
            new_value_json={"raw_response": data.correction_data},
            reason=data.reason,
        )
        return ResponseRead.model_validate(updated)

    async def _expand_answers(
        self, response_id: uuid.UUID, raw_response: dict
    ) -> None:
        for item_code, value in raw_response.items():
            answer_data = {
                "response_id": response_id,
                "item_code": item_code,
                "is_missing": value is None or value == "",
            }
            if isinstance(value, bool):
                answer_data["value_boolean"] = value
            elif isinstance(value, (int, float)):
                answer_data["value_number"] = float(value)
            elif isinstance(value, dict):
                answer_data["value_json"] = value
            else:
                answer_data["value_text"] = str(value) if value is not None else None
            add_result = self.session.add(QuestionnaireAnswer(**answer_data))
            if inspect.isawaitable(add_result):
                await add_result
        await self.session.flush()
