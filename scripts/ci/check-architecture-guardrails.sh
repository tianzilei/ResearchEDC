#!/usr/bin/env bash
set -euo pipefail

echo "=== Architecture guardrails ==="

failures=0
warnings=0

fail_if_matches() {
  local label="$1"
  local output="$2"

  if [ -n "${output}" ]; then
    echo "FAIL: ${label}"
    echo "${output}"
    failures=$((failures + 1))
  else
    echo "PASS: ${label}"
  fi
}

warn_if_matches() {
  local label="$1"
  local output="$2"

  if [ -n "${output}" ]; then
    echo "WARN: ${label}"
    echo "${output}"
    warnings=$((warnings + 1))
  else
    echo "PASS: ${label}"
  fi
}

shared_java="$(find shared/src/main/java -name '*.java' 2>/dev/null || true)"
fail_if_matches "shared/src/main/java must remain resource-only" "${shared_java}"

frontend_legacy_callers="$(find frontend/src -type f \( -name '*.ts' -o -name '*.tsx' \) -print0 \
  | xargs -0 grep -n '/api/legacy/' 2>/dev/null || true)"
fail_if_matches "frontend must not call /api/legacy/*" "${frontend_legacy_callers}"

study_zero_placeholders="$(find frontend/src -type f \( -name '*.ts' -o -name '*.tsx' \) -print0 \
  | xargs -0 grep -En 'studyId[[:space:]]*[:=][[:space:]]*0|studyId[^A-Za-z0-9_]+0|studyId.*\?\?.*0' 2>/dev/null || true)"
warn_if_matches "frontend studyId=0-style fallbacks remain; prefer enabled guards with undefined study ids" "${study_zero_placeholders}"

page_raw_fetch="$(find frontend/src/pages frontend/src/components frontend/src/hooks -type f \( -name '*.ts' -o -name '*.tsx' \) -print0 \
  | xargs -0 grep -En '(^|[^A-Za-z0-9_])fetch\(' 2>/dev/null || true)"
allowed_raw_fetch="$(printf '%s\n' "${page_raw_fetch}" \
  | grep -Ev 'ImportManager|ExportCenter|api/client|AuthProvider|FormData|attachments|download|Blob' || true)"
warn_if_matches "page/component/hooks raw fetch remains; prefer apiClient or typed API modules for ordinary JSON APIs" "${allowed_raw_fetch}"

if [ "${failures}" -gt 0 ]; then
  echo "Architecture guardrails failed: ${failures} issue group(s), ${warnings} warning group(s)."
  exit 1
fi

echo "Architecture guardrails passed with ${warnings} warning group(s)."
