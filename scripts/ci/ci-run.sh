#!/usr/bin/env bash
set -euo pipefail
FAILED=0
echo "============================================"
echo " ResearchEDC CI - Full Modernisation Check"
echo "============================================"

echo ""
echo "[1/4] Backend build..."
bash scripts/ci/backend-build.sh || { echo "FAILED: Backend"; FAILED=1; }

echo ""
echo "[2/4] Frontend build..."
bash scripts/ci/frontend-build.sh || { echo "FAILED: Frontend"; FAILED=1; }

echo ""
echo "[3/4] Questionnaire service..."
bash scripts/ci/questionnaire-test.sh || { echo "FAILED: Questionnaire"; FAILED=1; }

echo ""
echo "[4/4] Architecture guardrails..."
bash scripts/ci/check-architecture-guardrails.sh || { echo "FAILED: Architecture guardrails"; FAILED=1; }

echo ""
if [ "$FAILED" -eq 0 ]; then
  echo "All checks passed."
else
  echo "Some checks FAILED. See above for details."
  exit 1
fi
