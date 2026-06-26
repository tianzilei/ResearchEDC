import pytest

from app.scoring.gad7 import GAD7Scorer


class TestGAD7Scorer:
    def setup_method(self):
        self.scorer = GAD7Scorer()

    def test_valid_response(self):
        response = {k: 2 for k in GAD7Scorer.required_items}
        result = self.scorer.score(response)
        assert result["status"] == "valid"
        assert result["total_score"] == 14
        assert result["severity"] == "中度焦虑"

    def test_minimal_score(self):
        response = {k: 0 for k in GAD7Scorer.required_items}
        result = self.scorer.score(response)
        assert result["total_score"] == 0
        assert result["severity"] == "无焦虑"

    def test_maximum_score(self):
        response = {k: 3 for k in GAD7Scorer.required_items}
        result = self.scorer.score(response)
        assert result["total_score"] == 21
        assert result["severity"] == "重度焦虑"

    def test_missing_items_returns_invalid(self):
        response = {"GAD7_01": 1}
        result = self.scorer.score(response)
        assert result["status"] == "invalid"
        assert result["total_score"] is None
