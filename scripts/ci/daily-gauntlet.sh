#!/usr/bin/env bash
set -euo pipefail

echo "=== Daily Gauntlet ==="
echo "Minimum engineering verification gate for ResearchEDC."
echo ""

failures=0
warnings=0

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

run_guardrail() {
  local label="$1"
  shift
  echo "--- ${label} ---"
  if "$@"; then
    echo "PASS: ${label}"
  else
    echo "WARN: ${label}"
    warnings=$((warnings + 1))
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

# === Guardrails ===
echo "=== Architecture Guardrails ==="

# 7. No shared Java files
run_guardrail "No shared/src/main/java files" test "$(find shared/src/main/java -name '*.java' 2>/dev/null | wc -l)" -eq 0

# 8. No new legacy callers
run_guardrail "No /api/legacy/* in frontend" test "$(grep -r '/api/legacy/' frontend/src --include='*.ts' --include='*.tsx' -l 2>/dev/null | wc -l)" -eq 0

# 9. No userId=0 placeholders
run_guardrail "No userId=0 in frontend" test "$(grep -r 'userId=0' frontend/src --include='*.ts' --include='*.tsx' -l 2>/dev/null | wc -l)" -eq 0

# 10. No unsafe Promise<unknown> casts
run_guardrail "No unsafe Promise<unknown> casts" test "$(grep -r 'Promise<unknown>' frontend/src --include='*.ts' --include='*.tsx' -l 2>/dev/null | wc -l)" -eq 0

# 11. No studyId=0 fallbacks (allowed in hooks with enabled guards)
run_guardrail "No studyId=0 in page components" test "$(grep -r 'studyId.*=.*0' frontend/src/pages --include='*.ts' --include='*.tsx' -l 2>/dev/null | wc -l)" -eq 0

echo ""
echo "========================="
if [ "${failures}" -gt 0 ]; then
  echo "GAUNTLET FAILED: ${failures} check(s) failed."
  exit 1
fi
if [ "${warnings}" -gt 0 ]; then
  echo "GAUNTLET PASSED with ${warnings} warning(s)."
else
  echo "GAUNTLET PASSED: all checks clean."
fi
