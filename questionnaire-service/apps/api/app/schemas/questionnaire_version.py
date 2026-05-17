import uuid
from datetime import datetime

from pydantic import BaseModel, ConfigDict


class QuestionnaireVersionCreate(BaseModel):
    version_no: str
    surveyjs_schema: dict
    validation_schema: dict | None = None
    scoring_schema: dict | None = None
    language: str = "zh-CN"


class QuestionnaireVersionUpdate(BaseModel):
    surveyjs_schema: dict | None = None
    validation_schema: dict | None = None
    scoring_schema: dict | None = None


class QuestionnaireVersionRead(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    template_id: uuid.UUID
    version_no: str
    surveyjs_schema: dict
    validation_schema: dict | None
    scoring_schema: dict | None
    language: str
    schema_hash: str
    status: str
    published_by: uuid.UUID | None
    published_at: datetime | None
    locked_at: datetime | None
    created_at: datetime
    updated_at: datetime


class VersionPublishRequest(BaseModel):
    published_by: uuid.UUID


class VersionRetireRequest(BaseModel):
    pass
