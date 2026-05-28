from typing import Any

from app.scoring.base import BaseScorer


class PSQIScorer(BaseScorer):
    code = "PSQI"

    required_items = [
        "PSQI_01", "PSQI_02", "PSQI_03", "PSQI_04",
        "PSQI_05a", "PSQI_05b", "PSQI_05c", "PSQI_05d", "PSQI_05e",
        "PSQI_05f", "PSQI_05g", "PSQI_05h", "PSQI_05i", "PSQI_05j",
        "PSQI_06", "PSQI_07", "PSQI_08", "PSQI_09",
    ]

    _component_mapping: dict[str, list[str]] = {
        "subjective_quality": ["PSQI_06"],
        "sleep_latency": ["PSQI_02", "PSQI_05a"],
        "sleep_duration": ["PSQI_04"],
        "sleep_efficiency": ["PSQI_01", "PSQI_03"],
        "sleep_disturbances": [
            "PSQI_05b", "PSQI_05c", "PSQI_05d", "PSQI_05e",
            "PSQI_05f", "PSQI_05g", "PSQI_05h", "PSQI_05i", "PSQI_05j",
        ],
        "sleep_medication": ["PSQI_07"],
        "daytime_dysfunction": ["PSQI_08", "PSQI_09"],
    }

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
                "score_code": "PSQI_total",
                "status": "invalid",
                "missing_items": missing,
                "total_score": None,
            }

        subscale_scores: dict[str, int] = {}
        for component, items in self._component_mapping.items():
            if component == "sleep_efficiency":
                subscale_scores[component] = _score_sleep_efficiency(response)
                continue
            if component == "sleep_duration":
                subscale_scores[component] = _score_sleep_duration(response)
                continue

            component_total = 0
            for item in items:
                val = int(response.get(item, 0))
                component_total += val
            if component == "sleep_latency":
                raw = component_total
                if raw == 0:
                    subscale_scores[component] = 0
                elif raw <= 2:
                    subscale_scores[component] = 1
                elif raw <= 4:
                    subscale_scores[component] = 2
                else:
                    subscale_scores[component] = 3
            else:
                raw = component_total
                if raw == 0:
                    subscale_scores[component] = 0
                elif raw <= 2:
                    subscale_scores[component] = 1
                elif raw <= 4:
                    subscale_scores[component] = 2
                else:
                    subscale_scores[component] = 3

        total_score = sum(subscale_scores.values())
        if total_score <= 5:
            severity = "良好睡眠"
        elif total_score <= 10:
            severity = "轻度睡眠障碍"
        elif total_score <= 15:
            severity = "中度睡眠障碍"
        else:
            severity = "重度睡眠障碍"

        return {
            "score_code": "PSQI_total",
            "status": "valid",
            "total_score": total_score,
            "severity": severity,
            "subscale_scores": subscale_scores,
        }


def _score_sleep_efficiency(response: dict[str, Any]) -> int:
    raw_bedtime = _parse_time_to_minutes(str(response.get("PSQI_01", "0")))
    raw_waketime = _parse_time_to_minutes(str(response.get("PSQI_03", "0")))
    if raw_waketime <= raw_bedtime:
        raw_waketime += 24 * 60

    hours_in_bed = max(0, (raw_waketime - raw_bedtime) / 60.0)
    hours_sleep = float(response.get("PSQI_04", 0))
    efficiency = (hours_sleep / hours_in_bed) * 100 if hours_in_bed > 0 else 0
    if efficiency >= 85:
        return 0
    if efficiency >= 75:
        return 1
    if efficiency >= 65:
        return 2
    return 3


def _score_sleep_duration(response: dict[str, Any]) -> int:
    hours_sleep = float(response.get("PSQI_04", 0))
    if hours_sleep >= 7:
        return 0
    if hours_sleep >= 6:
        return 1
    if hours_sleep >= 5:
        return 2
    return 3


def _parse_time_to_minutes(time_str: str) -> float:
    try:
        parts = time_str.strip().split(":")
        if len(parts) == 2:
            return int(parts[0]) * 60 + int(parts[1])
        return float(time_str) * 60
    except (ValueError, TypeError):
        return 0
