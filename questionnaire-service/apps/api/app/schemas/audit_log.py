import uuid
from datetime import datetime

from pydantic import BaseModel, ConfigDict


class AuditLogCreate(BaseModel):
    study_id: uuid.UUID | None = None
    entity_type: str
    entity_id: uuid.UUID
    action: str
    old_value_json: dict | None = None
    new_value_json: dict | None = None
    reason: str | None = None
    operator_id: uuid.UUID | None = None
    operator_role: str | None = None
    ip_hash: str | None = None
    user_agent: str | None = None


class AuditLogRead(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    study_id: uuid.UUID | None
    entity_type: str
    entity_id: uuid.UUID
    action: str
    old_value_json: dict | None
    new_value_json: dict | None
    reason: str | None
    operator_id: uuid.UUID | None
    operator_role: str | None
    ip_hash: str | None
    user_agent: str | None
    created_at: datetime
