from typing import Any

from app.scoring.base import BaseScorer


class PHQ9Scorer(BaseScorer):
    code = "PHQ9"

    required_items = [
        "PHQ9_01", "PHQ9_02", "PHQ9_03", "PHQ9_04",
        "PHQ9_05", "PHQ9_06", "PHQ9_07", "PHQ9_08", "PHQ9_09",
    ]

    def score(
        self,
        response: dict[str, Any],
        scoring_schema: dict[str, Any] | None = None,
    ) -> dict[str, Any]:
        missing = [
            item for item in self.required_items
            if item not in response or response[item] is None
        ]
        if missing:
            return {
                "score_code": "PHQ9_total",
                "status": "invalid",
                "missing_items": missing,
                "total_score": None,
            }
        total = sum(int(response[item]) for item in self.required_items)
        if total <= 4:
            severity = "无抑郁"
        elif total <= 9:
            severity = "轻度抑郁"
        elif total <= 14:
            severity = "中度抑郁"
        elif total <= 19:
            severity = "中重度抑郁"
        else:
            severity = "重度抑郁"
        return {
            "score_code": "PHQ9_total",
            "status": "valid",
            "total_score": total,
            "severity": severity,
        }
