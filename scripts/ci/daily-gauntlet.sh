#!/usr/bin/env bash
set -euo pipefail

echo "=== Daily Gauntlet ==="
echo "Minimum engineering verification gate for ResearchEDC."
echo ""

failures=0

run_check() {
  local label="$1"
  shift
  echo "--- ${label} ---"
  if "$@"; then
    echo "PASS: ${label}"
  else
    echo "FAIL: ${label}"
    failures=$((failures + 1))
  fi
  echo ""
}

# 1. Working tree status
run_check "Git status" git status --short

# 2. Frontend lint
run_check "Frontend lint" pnpm -C frontend lint

# 3. Frontend typecheck
run_check "Frontend typecheck" pnpm -C frontend typecheck

# 4. Modulith architecture gate
run_check "Modulith verification" mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false -q

# 5. Import service tests
run_check "Import service tests" mvn -pl app -am test -Dtest=ImportServiceTest -Dsurefire.failIfNoSpecifiedTests=false -q

# 6. Export backend gate
run_check "Export backend tests" mvn -pl app -am test -Dtest=ExportServiceTest,OdmExportExecutionServiceTest,OdmExportGeneratorTest,ExportControllerTest -Dsurefire.failIfNoSpecifiedTests=false -q

echo "========================="
if [ "${failures}" -gt 0 ]; then
  echo "GAUNTLET FAILED: ${failures} check(s) failed."
  exit 1
fi
echo "GAUNTLET PASSED: all checks clean."
