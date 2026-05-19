#!/usr/bin/env bash
set -euo pipefail
echo "=== Backend: Maven compile ==="
mvn -B clean compile -DskipTests
echo "=== Backend: Maven verify ==="
mvn -B verify -DskipITs=true
echo "=== Backend: OK ==="
