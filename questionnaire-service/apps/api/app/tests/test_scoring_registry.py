import pytest

from app.scoring.registry import ScorerRegistry
from app.scoring.isi import ISIScorer
from app.scoring.gad7 import GAD7Scorer
from app.scoring.phq9 import PHQ9Scorer
from app.scoring.ess import ESSScorer


class TestScorerRegistry:
    def setup_method(self):
        self.registry = ScorerRegistry()
        self.registry.register(ISIScorer())
        self.registry.register(GAD7Scorer())

    def test_register_and_get(self):
        scorer = self.registry.get("ISI")
        assert isinstance(scorer, ISIScorer)

    def test_get_nonexistent(self):
        with pytest.raises(ValueError, match="No scorer registered"):
            self.registry.get("UNKNOWN")

    def test_score_through_registry(self):
        response = {f"ISI_0{i}": 1 for i in range(1, 8)}
        result = self.registry.score("ISI", response)
        assert result["status"] == "valid"
        assert result["total_score"] == 7

    def test_get_all_codes(self):
        codes = self.registry.get_all_codes()
        assert "ISI" in codes
        assert "GAD7" in codes

    def test_global_registry_has_all_scorers(self):
        from app.scoring import registry
        codes = registry.get_all_codes()
        assert "ISI" in codes
        assert "GAD7" in codes
        assert "PHQ9" in codes
        assert "ESS" in codes
