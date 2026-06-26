"""initial migration

Revision ID: 0001
Revises:
Create Date: 2026-05-17
"""

from typing import Sequence, Union

from alembic import op
import sqlalchemy as sa
from sqlalchemy.dialects.postgresql import UUID, JSONB

revision: str = "0001"
down_revision: Union[str, None] = None
branch_labels: Union[str, Sequence[str], None] = None
depends_on: Union[str, Sequence[str], None] = None


def upgrade() -> None:
    op.create_table(
        "questionnaire_template",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("study_id", UUID(as_uuid=True), nullable=True),
        sa.Column("code", sa.String(64), nullable=False, index=True),
        sa.Column("name", sa.String(255), nullable=False),
        sa.Column("description", sa.Text, nullable=True),
        sa.Column("category", sa.String(64), nullable=True),
        sa.Column("status", sa.String(32), nullable=False, server_default="active"),
        sa.Column("created_by", UUID(as_uuid=True), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
    )

    op.create_table(
        "questionnaire_version",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("template_id", UUID(as_uuid=True), sa.ForeignKey("questionnaire_template.id", ondelete="RESTRICT"), nullable=False),
        sa.Column("version_no", sa.String(32), nullable=False),
        sa.Column("surveyjs_schema", JSONB, nullable=False),
        sa.Column("validation_schema", JSONB, nullable=True),
        sa.Column("scoring_schema", JSONB, nullable=True),
        sa.Column("language", sa.String(16), nullable=False, server_default="zh-CN"),
        sa.Column("schema_hash", sa.String(128), nullable=False),
        sa.Column("status", sa.String(32), nullable=False, server_default="draft"),
        sa.Column("published_by", UUID(as_uuid=True), nullable=True),
        sa.Column("published_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("locked_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.UniqueConstraint("template_id", "version_no", name="uq_template_version"),
    )

    op.create_table(
        "questionnaire_assignment",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("study_id", UUID(as_uuid=True), nullable=False, index=True),
        sa.Column("subject_id", UUID(as_uuid=True), nullable=False, index=True),
        sa.Column("visit_id", UUID(as_uuid=True), nullable=True),
        sa.Column("randomization_arm_id", UUID(as_uuid=True), nullable=True),
        sa.Column("questionnaire_version_id", UUID(as_uuid=True), sa.ForeignKey("questionnaire_version.id", ondelete="RESTRICT"), nullable=False),
        sa.Column("status", sa.String(32), nullable=False, server_default="pending"),
        sa.Column("due_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("public_token_hash", sa.String(255), nullable=True),
        sa.Column("token_expires_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("created_by", UUID(as_uuid=True), nullable=False),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("updated_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
    )

    op.create_table(
        "questionnaire_response",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("assignment_id", UUID(as_uuid=True), sa.ForeignKey("questionnaire_assignment.id", ondelete="RESTRICT"), nullable=False, index=True),
        sa.Column("subject_id", UUID(as_uuid=True), nullable=False, index=True),
        sa.Column("visit_id", UUID(as_uuid=True), nullable=True),
        sa.Column("questionnaire_version_id", UUID(as_uuid=True), sa.ForeignKey("questionnaire_version.id", ondelete="RESTRICT"), nullable=False),
        sa.Column("status", sa.String(32), nullable=False, server_default="submitted"),
        sa.Column("started_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("submitted_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("raw_response_json", JSONB, nullable=False),
        sa.Column("score_json", JSONB, nullable=True),
        sa.Column("total_score", sa.Numeric(10, 2), nullable=True),
        sa.Column("device_info", JSONB, nullable=True),
        sa.Column("ip_hash", sa.String(255), nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
    )

    op.create_table(
        "questionnaire_answer",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("response_id", UUID(as_uuid=True), sa.ForeignKey("questionnaire_response.id", ondelete="CASCADE"), nullable=False, index=True),
        sa.Column("item_code", sa.String(128), nullable=False),
        sa.Column("value_text", sa.Text, nullable=True),
        sa.Column("value_number", sa.Numeric(14, 4), nullable=True),
        sa.Column("value_boolean", sa.Boolean, nullable=True),
        sa.Column("value_json", JSONB, nullable=True),
        sa.Column("is_missing", sa.Boolean, nullable=False, server_default=sa.text("false")),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
    )

    op.create_table(
        "questionnaire_audit_log",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("study_id", UUID(as_uuid=True), nullable=True),
        sa.Column("entity_type", sa.String(64), nullable=False, index=True),
        sa.Column("entity_id", UUID(as_uuid=True), nullable=False, index=True),
        sa.Column("action", sa.String(64), nullable=False),
        sa.Column("old_value_json", JSONB, nullable=True),
        sa.Column("new_value_json", JSONB, nullable=True),
        sa.Column("reason", sa.Text, nullable=True),
        sa.Column("operator_id", UUID(as_uuid=True), nullable=True),
        sa.Column("operator_role", sa.String(64), nullable=True),
        sa.Column("ip_hash", sa.String(255), nullable=True),
        sa.Column("user_agent", sa.Text, nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
    )

    op.create_table(
        "export_job",
        sa.Column("id", UUID(as_uuid=True), primary_key=True, server_default=sa.text("gen_random_uuid()")),
        sa.Column("study_id", UUID(as_uuid=True), nullable=False, index=True),
        sa.Column("requested_by", UUID(as_uuid=True), nullable=False),
        sa.Column("status", sa.String(32), nullable=False, server_default="pending"),
        sa.Column("export_type", sa.String(64), nullable=False),
        sa.Column("export_format", sa.String(32), nullable=False),
        sa.Column("query_params", JSONB, nullable=False),
        sa.Column("file_path", sa.Text, nullable=True),
        sa.Column("error_message", sa.Text, nullable=True),
        sa.Column("created_at", sa.DateTime(timezone=True), server_default=sa.func.now(), nullable=False),
        sa.Column("started_at", sa.DateTime(timezone=True), nullable=True),
        sa.Column("finished_at", sa.DateTime(timezone=True), nullable=True),
    )


def downgrade() -> None:
    op.drop_table("export_job")
    op.drop_table("questionnaire_audit_log")
    op.drop_table("questionnaire_answer")
    op.drop_table("questionnaire_response")
    op.drop_table("questionnaire_assignment")
    op.drop_table("questionnaire_version")
    op.drop_table("questionnaire_template")
