import pytest

from app.scoring.psqi import PSQIScorer


class TestPSQIScorer:
    def setup_method(self):
        self.scorer = PSQIScorer()

    def test_valid_response(self):
        response = {k: 1 for k in PSQIScorer.required_items}
        response["PSQI_01"] = "22:00"
        response["PSQI_03"] = "06:00"
        response["PSQI_04"] = 7
        result = self.scorer.score(response)
        assert result["status"] == "valid"
        assert result["total_score"] is not None
        assert "subscale_scores" in result

    def test_minimal_score(self):
        response = {k: 0 for k in PSQIScorer.required_items}
        response["PSQI_01"] = "22:00"
        response["PSQI_03"] = "06:00"
        response["PSQI_04"] = 8
        result = self.scorer.score(response)
        assert result["status"] == "valid"
        assert result["total_score"] == 0
        assert result["severity"] == "良好睡眠"

    def test_missing_items_returns_invalid(self):
        response = {"PSQI_01": "22:00"}
        result = self.scorer.score(response)
        assert result["status"] == "invalid"
        assert result["total_score"] is None

    def test_severe_score(self):
        response = {k: 3 for k in PSQIScorer.required_items}
        response["PSQI_01"] = "23:00"
        response["PSQI_03"] = "05:00"
        response["PSQI_04"] = 3
        result = self.scorer.score(response)
        assert result["status"] == "valid"
        assert result["total_score"] >= 14
        assert result["severity"] == "重度睡眠障碍"
