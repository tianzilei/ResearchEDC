import asyncio
from datetime import datetime, timezone

from app.workers.celery_app import celery_app
from app.core.database import get_session_factory
from app.repositories.assignment_repo import AssignmentRepo


@celery_app.task
def check_expired_tokens() -> dict:
    async def _run():
        async with get_session_factory()() as session:
            repo = AssignmentRepo(session)
            expired = await repo.get_expired_tokens()
            count = 0
            for assignment in expired:
                await repo.update(assignment.id, status="expired")
                count += 1
            return {"expired_count": count}
    result = asyncio.run(_run())
    return result
