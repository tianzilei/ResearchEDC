from abc import ABC, abstractmethod
from typing import Any


class BaseScorer(ABC):
    code: str

    @abstractmethod
    def score(
        self,
        response: dict[str, Any],
        scoring_schema: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        pass
