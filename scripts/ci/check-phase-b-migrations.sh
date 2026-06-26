#!/usr/bin/env bash
set -euo pipefail

MIGRATION_DIR="shared/src/main/resources/migration/3.18"
RELEASE_FILE="${MIGRATION_DIR}/release.xml"

expected_files=(
  "2026-06-03-study-bidirectional-sync-trigger.xml"
  "2026-06-03-subject-bidirectional-sync-trigger.xml"
  "2026-06-03-event-bidirectional-sync-trigger.xml"
  "2026-06-05-event-definition-crf-bidirectional-sync-trigger.xml"
  "2026-06-03-datacapture-bidirectional-sync-trigger.xml"
  "2026-06-05-item-group-metadata-bidirectional-sync-trigger.xml"
  "2026-06-03-crf-bidirectional-sync-trigger.xml"
  "2026-06-03-identity-bidirectional-sync-trigger.xml"
  "2026-06-03-rule-bidirectional-sync-trigger.xml"
  "2026-06-03-dataset-filter-bidirectional-sync-trigger.xml"
  "2026-06-03-discrepancy-note-bidirectional-sync-trigger.xml"
  "2026-06-03-subjectgroup-bidirectional-sync-trigger.xml"
)

echo "=== Phase B migration static checks ==="

failures=0

fail() {
  echo "FAIL: $1"
  failures=$((failures + 1))
}

if [ ! -f "${RELEASE_FILE}" ]; then
  fail "missing ${RELEASE_FILE}"
fi

if command -v xmllint >/dev/null 2>&1; then
  while IFS= read -r xml_file; do
    xmllint --noout "${xml_file}" || fail "XML parse failed: ${xml_file}"
  done < <(find "${MIGRATION_DIR}" -name '*.xml' -type f | sort)
else
  echo "WARN: xmllint not found; XML parse check skipped"
fi

for file in "${expected_files[@]}"; do
  path="${MIGRATION_DIR}/${file}"
  include="migration/3.18/${file}"

  if [ ! -f "${path}" ]; then
    fail "missing expected migration ${path}"
    continue
  fi

  if ! grep -Fq "${include}" "${RELEASE_FILE}"; then
    fail "${file} is not included in release.xml"
  fi

  if ! grep -Fq "pg_trigger_depth() > 1" "${path}"; then
    fail "${file} is missing pg_trigger_depth recursion guard"
  fi

  if ! grep -Fq "CREATE TRIGGER" "${path}"; then
    fail "${file} does not create triggers"
  fi

  if ! grep -Fq "CREATE OR REPLACE FUNCTION" "${path}"; then
    fail "${file} does not create functions"
  fi
done

registered_count="$(grep -cE 'bidirectional-sync-trigger\.xml' "${RELEASE_FILE}" || true)"
expected_count="${#expected_files[@]}"

echo "Expected Phase B trigger migrations: ${expected_count}"
echo "Registered bidirectional trigger migrations: ${registered_count}"

if [ "${registered_count}" -lt "${expected_count}" ]; then
  fail "release.xml registers fewer bidirectional trigger migrations than expected"
fi

if [ "${failures}" -gt 0 ]; then
  echo "Phase B migration static checks failed: ${failures} issue group(s)."
  exit 1
fi

echo "Phase B migration static checks passed."
