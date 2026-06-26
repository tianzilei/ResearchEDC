import uuid
from datetime import datetime

from sqlalchemy import String, ForeignKey, UniqueConstraint, func
from sqlalchemy.dialects.postgresql import UUID, JSONB
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.core.database import Base


class QuestionnaireVersion(Base):
    __tablename__ = "questionnaire_version"
    __table_args__ = (
        UniqueConstraint("template_id", "version_no", name="uq_template_version"),
    )

    id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True), primary_key=True, default=uuid.uuid4
    )
    template_id: Mapped[uuid.UUID] = mapped_column(
        UUID(as_uuid=True),
        ForeignKey("questionnaire_template.id", ondelete="RESTRICT"),
        nullable=False,
    )
    version_no: Mapped[str] = mapped_column(String(32), nullable=False)
    surveyjs_schema: Mapped[dict] = mapped_column(JSONB, nullable=False)
    validation_schema: Mapped[dict | None] = mapped_column(JSONB, nullable=True)
    scoring_schema: Mapped[dict | None] = mapped_column(JSONB, nullable=True)
    language: Mapped[str] = mapped_column(String(16), nullable=False, default="zh-CN")
    schema_hash: Mapped[str] = mapped_column(String(128), nullable=False)
    status: Mapped[str] = mapped_column(
        String(32), nullable=False, default="draft"
    )
    published_by: Mapped[uuid.UUID | None] = mapped_column(
        UUID(as_uuid=True), nullable=True
    )
    published_at: Mapped[datetime | None] = mapped_column(nullable=True)
    locked_at: Mapped[datetime | None] = mapped_column(nullable=True)
    created_at: Mapped[datetime] = mapped_column(
        server_default=func.now(), nullable=False
    )
    updated_at: Mapped[datetime] = mapped_column(
        server_default=func.now(), onupdate=func.now(), nullable=False
    )

    template: Mapped["QuestionnaireTemplate"] = relationship(
        back_populates="versions"
    )
