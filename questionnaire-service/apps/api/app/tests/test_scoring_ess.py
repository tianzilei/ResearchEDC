import pytest

from app.scoring.ess import ESSScorer


class TestESSScorer:
    def setup_method(self):
        self.scorer = ESSScorer()

    def test_valid_response(self):
        response = {k: 2 for k in ESSScorer.required_items}
        result = self.scorer.score(response)
        assert result["status"] == "valid"
        assert result["total_score"] == 16
        assert result["severity"] == "重度嗜睡"

    def test_minimal_score(self):
        response = {k: 0 for k in ESSScorer.required_items}
        result = self.scorer.score(response)
        assert result["total_score"] == 0
        assert result["severity"] == "正常"

    def test_maximum_score(self):
        response = {k: 3 for k in ESSScorer.required_items}
        result = self.scorer.score(response)
        assert result["total_score"] == 24
        assert result["severity"] == "重度嗜睡"

    def test_missing_items_returns_invalid(self):
        response = {"ESS_01": 1}
        result = self.scorer.score(response)
        assert result["status"] == "invalid"
        assert result["total_score"] is None
