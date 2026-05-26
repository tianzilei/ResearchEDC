#!/usr/bin/env bash
set -euo pipefail
FAILED=0
echo "============================================"
echo " ResearchEDF CI - Full Modernisation Check"
echo "============================================"

echo ""
echo "[1/5] Backend build..."
bash scripts/ci/backend-build.sh || { echo "FAILED: Backend"; FAILED=1; }

echo ""
echo "[2/5] Frontend build..."
bash scripts/ci/frontend-build.sh || { echo "FAILED: Frontend"; FAILED=1; }

echo ""
echo "[3/5] Questionnaire service..."
bash scripts/ci/questionnaire-test.sh || { echo "FAILED: Questionnaire"; FAILED=1; }

echo ""
echo "[4/5] Docker Compose..."
bash scripts/ci/docker-compose-check.sh || { echo "FAILED: Docker Compose"; FAILED=1; }

echo ""
echo "[5/5] Legacy refactor report..."
bash scripts/ci/generate-legacy-report.sh || { echo "FAILED: Legacy report"; FAILED=1; }

echo ""
if [ "$FAILED" -eq 0 ]; then
  echo "All checks passed."
else
  echo "Some checks FAILED. See above for details."
  exit 1
fi
