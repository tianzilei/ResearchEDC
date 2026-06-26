from typing import Any

from fastapi import APIRouter, Depends, Request
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.database import get_db
from app.schemas.questionnaire_response import ResponseSubmit, ResponseDraft
from app.services.token_service import TokenService
from app.services.response_service import ResponseService

router = APIRouter(prefix="/public/questionnaires", tags=["Public"])


@router.get("/{token}")
async def get_questionnaire_by_token(
    token: str,
    db: AsyncSession = Depends(get_db),
) -> dict[str, Any]:
    token_service = TokenService(db)
    info = await token_service.validate_token(token)
    return {
        "assignment_id": str(info.assignment_id),
        "questionnaire_code": info.questionnaire_code,
        "questionnaire_name": info.questionnaire_name,
        "version_no": info.version_no,
        "surveyjs_schema": info.surveyjs_schema,
        "status": info.status,
        "due_at": info.due_at.isoformat() if info.due_at else None,
    }


@router.post("/{token}/draft")
async def save_draft(
    token: str,
    data: ResponseDraft,
    request: Request,
    db: AsyncSession = Depends(get_db),
) -> dict[str, Any]:
    token_service = TokenService(db)
    info = await token_service.validate_token(token)
    response_service = ResponseService(db)
    ip_hash = _get_ip_hash(request)
    response = await response_service.save_draft(
        info.assignment_id, data, ip_hash=ip_hash
    )
    return {"response_id": str(response.id), "status": "draft_saved"}


@router.post("/{token}/submit")
async def submit_response(
    token: str,
    data: ResponseSubmit,
    request: Request,
    db: AsyncSession = Depends(get_db),
) -> dict[str, Any]:
    token_service = TokenService(db)
    info = await token_service.validate_token(token)
    response_service = ResponseService(db)
    ip_hash = _get_ip_hash(request)
    response = await response_service.submit(
        info.assignment_id, data, ip_hash=ip_hash
    )
    from app.scoring.scoring_service import ScoringService
    scoring = ScoringService()
    score_result = scoring.score_response(
        info.questionnaire_code, data.response
    )
    if score_result.get("status") == "valid":
        _save_score(db, response.id, score_result)
    return {
        "response_id": str(response.id),
        "status": "submitted",
        "score": score_result,
    }


def _get_ip_hash(request: Request) -> str | None:
    forwarded = request.headers.get("X-Forwarded-For")
    if forwarded:
        return forwarded.split(",")[0].strip()
    client = request.client
    if client:
        return client.host
    return None


async def _save_score(
    db: AsyncSession,
    response_id,
    score_result: dict[str, Any],
) -> None:
    from app.repositories.response_repo import ResponseRepo
    repo = ResponseRepo(db)
    await repo.update(
        response_id,
        score_json=score_result,
        total_score=score_result.get("total_score"),
    )
