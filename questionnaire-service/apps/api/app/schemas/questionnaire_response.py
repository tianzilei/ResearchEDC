import uuid
from datetime import datetime

from pydantic import BaseModel, ConfigDict, field_validator


class ResponseSubmit(BaseModel):
    started_at: datetime | None = None
    submitted_at: datetime | None = None
    response: dict
    device_info: dict | None = None

    @field_validator("response")
    @classmethod
    def response_not_empty(cls, v: dict) -> dict:
        if not v:
            raise ValueError("response cannot be empty")
        return v


class ResponseDraft(BaseModel):
    started_at: datetime | None = None
    response: dict


class ResponseRead(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    assignment_id: uuid.UUID
    subject_id: uuid.UUID
    visit_id: uuid.UUID | None
    questionnaire_version_id: uuid.UUID
    status: str
    started_at: datetime | None
    submitted_at: datetime | None
    raw_response_json: dict
    score_json: dict | None
    total_score: float | None
    device_info: dict | None
    created_at: datetime


class ResponseReview(BaseModel):
    status: str
    reason: str | None = None


class ResponseCorrection(BaseModel):
    correction_data: dict
    reason: str
