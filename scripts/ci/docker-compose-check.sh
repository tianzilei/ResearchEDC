#!/usr/bin/env bash
set -euo pipefail
echo "=== Docker Compose: Validate ==="
docker compose -f deploy/compose.dev.yml config
echo "=== Docker Compose: OK ==="
