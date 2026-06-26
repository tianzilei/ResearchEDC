import uuid
from datetime import datetime

from pydantic import BaseModel, ConfigDict


class AssignmentCreate(BaseModel):
    study_id: uuid.UUID
    subject_id: uuid.UUID
    visit_id: uuid.UUID | None = None
    questionnaire_version_id: uuid.UUID
    due_at: datetime | None = None
    randomization_arm_id: uuid.UUID | None = None


class AssignmentBulkCreate(BaseModel):
    assignments: list[AssignmentCreate]


class AssignmentRead(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    study_id: uuid.UUID
    subject_id: uuid.UUID
    visit_id: uuid.UUID | None
    randomization_arm_id: uuid.UUID | None
    questionnaire_version_id: uuid.UUID
    status: str
    due_at: datetime | None
    has_token: bool = False
    token_expires_at: datetime | None
    created_by: uuid.UUID
    created_at: datetime
    updated_at: datetime


class AssignmentStatusUpdate(BaseModel):
    status: str
