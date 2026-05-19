#!/usr/bin/env bash
set -euo pipefail
echo "=== Questionnaire: Install deps ==="
cd questionnaire-service
python -m pip install --upgrade pip
pip install -r requirements.txt -q
echo "=== Questionnaire: Run tests ==="
python -m pytest app/tests/ -v
echo "=== Questionnaire: OK ==="
