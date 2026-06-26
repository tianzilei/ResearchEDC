from app.scoring.registry import registry
from app.scoring.isi import ISIScorer
from app.scoring.gad7 import GAD7Scorer
from app.scoring.phq9 import PHQ9Scorer
from app.scoring.ess import ESSScorer

registry.register(ISIScorer())
registry.register(GAD7Scorer())
registry.register(PHQ9Scorer())
registry.register(ESSScorer())

__all__ = ["registry", "ISIScorer", "GAD7Scorer", "PHQ9Scorer", "ESSScorer"]
