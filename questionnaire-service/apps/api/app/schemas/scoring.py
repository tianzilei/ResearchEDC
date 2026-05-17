from pydantic import BaseModel


class ScoreResult(BaseModel):
    score_code: str
    status: str
    total_score: float | None = None
    severity: str | None = None
    missing_items: list[str] | None = None
    subscale_scores: dict[str, float] | None = None
