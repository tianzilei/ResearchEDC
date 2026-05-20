"""Tests for response submission logic (service level, no DB)."""

import uuid
from unittest.mock import AsyncMock, patch

import pytest

from app.schemas.questionnaire_response import ResponseSubmit
from app.services.response_service import ResponseService


@pytest.mark.asyncio
async def test_expand_answers_simple_values():
    session = AsyncMock()
    service = ResponseService(session)
    response_id = uuid.uuid4()
    raw = {"Q1": 1, "Q2": "text", "Q3": True, "Q4": None, "Q5": {"nested": True}}

    await service._expand_answers(response_id, raw)

    calls = session.add.call_count
    assert calls == 5


@pytest.mark.asyncio
async def test_expand_answers_empty():
    session = AsyncMock()
    service = ResponseService(session)
    await service._expand_answers(uuid.uuid4(), {})
    assert session.add.call_count == 0
