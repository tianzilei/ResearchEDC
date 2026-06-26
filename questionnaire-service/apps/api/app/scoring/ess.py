from typing import Any

from app.scoring.base import BaseScorer


class ESSScorer(BaseScorer):
    code = "ESS"

    required_items = [
        "ESS_01", "ESS_02", "ESS_03", "ESS_04",
        "ESS_05", "ESS_06", "ESS_07", "ESS_08",
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
                "score_code": "ESS_total",
                "status": "invalid",
                "missing_items": missing,
                "total_score": None,
            }
        total = sum(int(response[item]) for item in self.required_items)
        if total <= 10:
            severity = "正常"
        elif total <= 12:
            severity = "边缘嗜睡"
        elif total <= 15:
            severity = "中度嗜睡"
        else:
            severity = "重度嗜睡"
        return {
            "score_code": "ESS_total",
            "status": "valid",
            "total_score": total,
            "severity": severity,
        }
