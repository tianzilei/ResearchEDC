from typing import Any

from app.scoring.registry import registry


class ScoringService:
    def score_response(
        self,
        questionnaire_code: str,
        raw_response: dict[str, Any],
        scoring_schema: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        try:
            result = registry.score(questionnaire_code, raw_response, scoring_schema)
            return result
        except ValueError:
            return {
                "score_code": f"{questionnaire_code}_total",
                "status": "unsupported",
                "total_score": None,
            }
