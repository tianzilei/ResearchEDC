from typing import Any

from app.scoring.base import BaseScorer


class ISIScorer(BaseScorer):
    code = "ISI"

    required_items = [
        "ISI_01", "ISI_02", "ISI_03", "ISI_04",
        "ISI_05", "ISI_06", "ISI_07",
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
                "score_code": "ISI_total",
                "status": "invalid",
                "missing_items": missing,
                "total_score": None,
            }
        total = sum(int(response[item]) for item in self.required_items)
        if total <= 7:
            severity = "无临床失眠"
        elif total <= 14:
            severity = "亚阈值失眠"
        elif total <= 21:
            severity = "中度失眠"
        else:
            severity = "重度失眠"
        return {
            "score_code": "ISI_total",
            "status": "valid",
            "total_score": total,
            "severity": severity,
        }
