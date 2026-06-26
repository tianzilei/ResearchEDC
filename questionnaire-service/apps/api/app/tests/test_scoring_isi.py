import pytest

from app.scoring.isi import ISIScorer


class TestISIScorer:
    def setup_method(self):
        self.scorer = ISIScorer()

    def test_valid_response(self):
        response = {
            "ISI_01": 2, "ISI_02": 3, "ISI_03": 2,
            "ISI_04": 1, "ISI_05": 2, "ISI_06": 2, "ISI_07": 1,
        }
        result = self.scorer.score(response)
        assert result["status"] == "valid"
        assert result["total_score"] == 13
        assert result["severity"] == "亚阈值失眠"

    def test_minimum_score(self):
        response = {k: 0 for k in ISIScorer.required_items}
        result = self.scorer.score(response)
        assert result["status"] == "valid"
        assert result["total_score"] == 0
        assert result["severity"] == "无临床失眠"

    def test_maximum_score(self):
        response = {k: 4 for k in ISIScorer.required_items}
        result = self.scorer.score(response)
        assert result["status"] == "valid"
        assert result["total_score"] == 28
        assert result["severity"] == "重度失眠"

    def test_missing_items(self):
        response = {"ISI_01": 2, "ISI_02": 3}
        result = self.scorer.score(response)
        assert result["status"] == "invalid"
        assert result["total_score"] is None
        assert "ISI_03" in result["missing_items"]

    def test_empty_response(self):
        result = self.scorer.score({})
        assert result["status"] == "invalid"
        assert len(result["missing_items"]) == 7

    def test_boundary_severity_7(self):
        response = {k: 1 for k in ISIScorer.required_items}
        result = self.scorer.score(response)
        assert result["total_score"] == 7
        assert result["severity"] == "无临床失眠"

    def test_boundary_severity_8(self):
        response = {"ISI_01": 2, "ISI_02": 1, "ISI_03": 1,
                    "ISI_04": 1, "ISI_05": 1, "ISI_06": 1, "ISI_07": 1}
        result = self.scorer.score(response)
        assert result["total_score"] == 8
        assert result["severity"] == "亚阈值失眠"

    def test_boundary_severity_14(self):
        response = {k: 2 for k in ISIScorer.required_items}
        result = self.scorer.score(response)
        assert result["total_score"] == 14
        assert result["severity"] == "亚阈值失眠"

    def test_boundary_severity_15(self):
        response = {"ISI_01": 3, "ISI_02": 2, "ISI_03": 2,
                    "ISI_04": 2, "ISI_05": 2, "ISI_06": 2, "ISI_07": 2}
        result = self.scorer.score(response)
        assert result["total_score"] == 15
        assert result["severity"] == "中度失眠"

    def test_boundary_severity_21(self):
        response = {k: 3 for k in ISIScorer.required_items}
        result = self.scorer.score(response)
        assert result["total_score"] == 21
        assert result["severity"] == "中度失眠"

    def test_boundary_severity_22(self):
        response = {"ISI_01": 4, "ISI_02": 3, "ISI_03": 3,
                    "ISI_04": 3, "ISI_05": 3, "ISI_06": 3, "ISI_07": 3}
        result = self.scorer.score(response)
        assert result["total_score"] == 22
        assert result["severity"] == "重度失眠"
