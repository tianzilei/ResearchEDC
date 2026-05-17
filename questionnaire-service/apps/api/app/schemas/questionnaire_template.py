import uuid
from datetime import datetime

from pydantic import BaseModel, ConfigDict


class QuestionnaireTemplateCreate(BaseModel):
    code: str
    name: str
    description: str | None = None
    category: str | None = None
    study_id: uuid.UUID | None = None


class QuestionnaireTemplateUpdate(BaseModel):
    name: str | None = None
    description: str | None = None
    category: str | None = None
    status: str | None = None


class QuestionnaireTemplateRead(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    study_id: uuid.UUID | None
    code: str
    name: str
    description: str | None
    category: str | None
    status: str
    created_by: uuid.UUID
    created_at: datetime
    updated_at: datetime
