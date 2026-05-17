import uuid
from typing import Any

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.services.assignment_service import AssignmentService
from app.schemas.questionnaire_assignment import AssignmentCreate

router = APIRouter(prefix="/events", tags=["Events"])


@router.post("/randomization-completed", status_code=201)
async def on_randomization_completed(
    payload: dict[str, Any],
    db: AsyncSession = Depends(get_db),
) -> dict[str, Any]:
    study_id = uuid.UUID(payload["study_id"])
    subject_id = uuid.UUID(payload["subject_id"])
    arm_id = uuid.UUID(payload.get("randomization_arm_id", "00000000-0000-0000-0000-000000000000"))
    visit_overrides = payload.get("visit_questionnaire_map", {})
    created_by = uuid.UUID(payload.get("created_by", "00000000-0000-0000-0000-000000000001"))
    service = AssignmentService(db)
    assignments_created = 0
    visit_schedule = _get_visit_schedule(arm_id)
    for visit_name, questionnaire_version_ids in visit_schedule.items():
        visit_id = visit_overrides.get(visit_name)
        for version_id in questionnaire_version_ids:
            try:
                v_id = uuid.UUID(version_id) if isinstance(version_id, str) else version_id
                await service.create(
                    AssignmentCreate(
                        study_id=study_id,
                        subject_id=subject_id,
                        visit_id=uuid.UUID(visit_id) if isinstance(visit_id, str) else visit_id,
                        questionnaire_version_id=v_id,
                        randomization_arm_id=arm_id,
                    ),
                    created_by=created_by,
                )
                assignments_created += 1
            except Exception:
                pass
    return {
        "status": "ok",
        "subject_id": str(subject_id),
        "assignments_created": assignments_created,
    }


@router.post("/visit-started", status_code=201)
async def on_visit_started(
    payload: dict[str, Any],
    db: AsyncSession = Depends(get_db),
) -> dict[str, Any]:
    study_id = uuid.UUID(payload["study_id"])
    subject_id = uuid.UUID(payload["subject_id"])
    visit_id = uuid.UUID(payload.get("visit_id", "00000000-0000-0000-0000-000000000000"))
    questionnaire_version_ids = payload.get("questionnaire_version_ids", [])
    created_by = uuid.UUID(payload.get("created_by", "00000000-0000-0000-0000-000000000001"))
    service = AssignmentService(db)
    count = 0
    for vid in questionnaire_version_ids:
        try:
            await service.create(
                AssignmentCreate(
                    study_id=study_id,
                    subject_id=subject_id,
                    visit_id=visit_id,
                    questionnaire_version_id=uuid.UUID(vid),
                ),
                created_by=created_by,
            )
            count += 1
        except Exception:
            pass
    return {"status": "ok", "assignments_created": count}


def _get_visit_schedule(arm_id: uuid.UUID) -> dict[str, list[str]]:
    return {
        "baseline": [],
        "week_2": [],
        "week_4": [],
        "endpoint": [],
    }
