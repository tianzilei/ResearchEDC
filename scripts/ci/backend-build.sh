#!/usr/bin/env bash
set -euo pipefail
echo "=== Backend: Maven compile ==="
mvn -B clean compile -DskipTests
echo "=== Backend: Modulith verification ==="
mvn test -pl app -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
echo "=== Backend: Module tests ==="
mvn test -pl app -am
echo "=== Backend: OK ==="
