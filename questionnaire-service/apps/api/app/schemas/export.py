import uuid
from datetime import datetime

from pydantic import BaseModel, ConfigDict


class ExportRequest(BaseModel):
    study_id: uuid.UUID
    visit_ids: list[str] | None = None
    questionnaire_codes: list[str]
    export_format: str = "xlsx"
    layout: str = "wide"
    include_scores: bool = True
    include_raw: bool = False


class ExportJobRead(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: uuid.UUID
    study_id: uuid.UUID
    requested_by: uuid.UUID
    status: str
    export_type: str
    export_format: str
    query_params: dict
    file_path: str | None
    error_message: str | None
    created_at: datetime
    started_at: datetime | None
    finished_at: datetime | None
