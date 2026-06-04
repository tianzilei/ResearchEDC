#!/usr/bin/env bash
set -euo pipefail
echo "=== Frontend: Install ==="
cd frontend
pnpm install --frozen-lockfile
echo "=== Frontend: Typecheck ==="
pnpm typecheck
echo "=== Frontend: Lint ==="
pnpm lint || true
echo "=== Frontend: Test ==="
pnpm test --run
echo "=== Frontend: Build ==="
pnpm build
echo "=== Frontend: OK ==="
cd ..
