import uuid
from datetime import datetime

from sqlalchemy import String, ForeignKey, Numeric, func
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.orm import Mapped, mapped_column

from app.core.database import Base


class QuestionnaireResponse(Base):
    __tablename__ = "questionnaire_response"

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    assignment_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("questionnaire_assignment.id", ondelete="RESTRICT"),
        nullable=False,
        index=True,
    )
    subject_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), nullable=False, index=True
    )
    visit_id: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True), nullable=True
    )
    questionnaire_version_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("questionnaire_version.id", ondelete="RESTRICT"),
        nullable=False,
    )
    status: Mapped[str] = mapped_column(
        String(32), nullable=False, default="submitted"
    )
    started_at: Mapped[datetime | None] = mapped_column(nullable=True)
    submitted_at: Mapped[datetime | None] = mapped_column(nullable=True)
    raw_response_json: Mapped[dict] = mapped_column(JSONB, nullable=False)
    score_json: Mapped[dict | None] = mapped_column(JSONB, nullable=True)
    total_score: Mapped[float | None] = mapped_column(Numeric(10, 2), nullable=True)
    device_info: Mapped[dict | None] = mapped_column(JSONB, nullable=True)
    ip_hash: Mapped[str | None] = mapped_column(String(255), nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        server_default=func.now(), nullable=False
    )
