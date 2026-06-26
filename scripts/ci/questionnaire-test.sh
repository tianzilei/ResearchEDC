#!/usr/bin/env bash
set -euo pipefail
echo "=== Questionnaire: Install deps ==="
cd questionnaire-service/apps/api
rm -rf .venv
uv sync --extra dev
echo "=== Questionnaire: Run tests ==="
.venv/bin/python -m pytest app/tests/ -v
echo "=== Questionnaire: OK ==="
