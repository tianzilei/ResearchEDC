from typing import Any

from app.scoring.base import BaseScorer


class GAD7Scorer(BaseScorer):
    code = "GAD7"

    required_items = [
        "GAD7_01", "GAD7_02", "GAD7_03", "GAD7_04",
        "GAD7_05", "GAD7_06", "GAD7_07",
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
                "score_code": "GAD7_total",
                "status": "invalid",
                "missing_items": missing,
                "total_score": None,
            }
        total = sum(int(response[item]) for item in self.required_items)
        if total <= 4:
            severity = "无焦虑"
        elif total <= 9:
            severity = "轻度焦虑"
        elif total <= 14:
            severity = "中度焦虑"
        else:
            severity = "重度焦虑"
        return {
            "score_code": "GAD7_total",
            "status": "valid",
            "total_score": total,
            "severity": severity,
        }
