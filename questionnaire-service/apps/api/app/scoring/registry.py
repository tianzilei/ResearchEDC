from typing import Any

from app.scoring.base import BaseScorer


class ScorerRegistry:
    def __init__(self) -> None:
        self._scorers: dict[str, BaseScorer] = {}

    def register(self, scorer: BaseScorer) -> None:
        self._scorers[scorer.code] = scorer

    def get(self, code: str) -> BaseScorer:
        if code not in self._scorers:
            raise ValueError(f"No scorer registered for questionnaire: {code}")
        return self._scorers[code]

    def get_all_codes(self) -> list[str]:
        return list(self._scorers.keys())

    def score(
        self, code: str, response: dict[str, Any], scoring_schema: dict[str, Any] | None = None
    ) -> dict[str, Any]:
        scorer = self.get(code)
        return scorer.score(response, scoring_schema)


registry = ScorerRegistry()
