import json

from app.services.export_service import export_long, export_wide, export_score


class TestExportFormats:
    def test_export_long_format(self):
        responses = [
            {
                "subject_id": "S001",
                "raw_response_json": {"ISI_01": 2, "ISI_02": 3},
                "score_json": None,
                "total_score": None,
                "submitted_at": None,
            }
        ]
        buffer = export_long(responses, "json")
        data = json.loads(buffer.getvalue().decode())
        assert len(data) == 2
        assert data[0]["subject_id"] == "S001"
        assert data[0]["item_code"] == "ISI_01"

    def test_export_wide_format(self):
        responses = [
            {
                "subject_id": "S001",
                "raw_response_json": {"ISI_01": 2, "ISI_02": 3},
                "score_json": {"total_score": 5},
                "total_score": 5.0,
                "submitted_at": None,
            }
        ]
        buffer = export_wide(responses, "json")
        data = json.loads(buffer.getvalue().decode())
        assert len(data) == 1
        assert data[0]["ISI_01"] == 2
        assert data[0]["total_score"] == 5

    def test_export_score_format(self):
        responses = [
            {
                "subject_id": "S001",
                "raw_response_json": {},
                "score_json": {
                    "score_code": "ISI_total",
                    "total_score": 13,
                    "severity": "亚阈值失眠",
                },
                "total_score": 13.0,
                "submitted_at": None,
            }
        ]
        buffer = export_score(responses, "json")
        data = json.loads(buffer.getvalue().decode())
        assert len(data) == 1
        assert data[0]["score_code"] == "ISI_total"
        assert data[0]["total_score"] == 13

    def test_export_empty(self):
        buffer = export_long([], "json")
        data = json.loads(buffer.getvalue().decode())
        assert data == []

    def test_export_csv_format(self):
        responses = [
            {
                "subject_id": "S001",
                "raw_response_json": {"ISI_01": 2},
                "score_json": None,
                "total_score": None,
                "submitted_at": None,
            }
        ]
        buffer = export_long(responses, "csv")
        content = buffer.getvalue().decode("utf-8-sig")
        assert "subject_id" in content
        assert "ISI_01" in content
