import pytest

from app.scoring.phq9 import PHQ9Scorer


class TestPHQ9Scorer:
    def setup_method(self):
        self.scorer = PHQ9Scorer()

    def test_valid_response(self):
        response = {k: 1 for k in PHQ9Scorer.required_items}
        result = self.scorer.score(response)
        assert result["status"] == "valid"
        assert result["total_score"] == 9
        assert result["severity"] == "轻度抑郁"

    def test_minimal_score(self):
        response = {k: 0 for k in PHQ9Scorer.required_items}
        result = self.scorer.score(response)
        assert result["total_score"] == 0
        assert result["severity"] == "无抑郁"

    def test_maximum_score(self):
        response = {k: 3 for k in PHQ9Scorer.required_items}
        result = self.scorer.score(response)
        assert result["total_score"] == 27
        assert result["severity"] == "重度抑郁"

    def test_missing_items(self):
        response = {"PHQ9_01": 1}
        result = self.scorer.score(response)
        assert result["status"] == "invalid"
