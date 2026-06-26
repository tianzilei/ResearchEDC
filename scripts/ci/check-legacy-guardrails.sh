#!/usr/bin/env bash
set -euo pipefail

echo "=== Legacy guardrails ==="

failures=0

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

dao_provider_matches="$(rg -n 'DaoProvider\.getDao' app shared -g '*.java' 2>/dev/null || true)"
fail_if_matches "DaoProvider.getDao must remain removed" "${dao_provider_matches}"

direct_dao_matches="$(git grep -n 'new \([A-Z][a-zA-Z]*DAO\b\|CrfDao\|RuleDao\|StudyDao\|SubjectDao\|DatasetDao\|FilterDao\|StudyGroupDao\|DiscrepancyNoteDao\|EventCrfDao\|ItemDao\|ItemDataDao\|ItemGroupDao\|SectionDao\|ItemFormMetadataDao\|ItemGroupMetadataDao\)(' -- '*.java' ':!app/src/main/java/org/researchedc/config/' ':!*Test*.java' ':!*adapter/Item*' ':!*adapter/Section*' ':!*adapter/Event*' ':!*adapter/Crf*' ':!*adapter/Rule*' ':!*adapter/Study*' ':!*adapter/Subject*' ':!*adapter/User*' ':!*adapter/Filter*' ':!*adapter/Dataset*' ':!*adapter/Discrepancy*' 2>/dev/null || true)"
fail_if_matches "direct legacy DAO construction in consumer code must remain 0" "${direct_dao_matches}"

public_module_imports="$(rg -n 'import org\.researchedc\.(dao|bean|domain)\.|import org\.akaza\.openclinica\.' app/src/main/java/org/researchedc/module -g '*.java' -g '!**/internal/adapter/**' 2>/dev/null || true)"
fail_if_matches "module public code must not import legacy dao/bean/domain packages" "${public_module_imports}"

retired_import_jobs="$(rg -n 'class\s+(ImportSpringJob|ExampleSpringJob)\b|ImportSpringJob|ExampleSpringJob' app shared -g '*.java' 2>/dev/null || true)"
fail_if_matches "retired import Quartz jobs must remain absent" "${retired_import_jobs}"

retired_rule_xml_import="$(rg -n 'rulesPostImportContainerService|RulesPostImportContainerService|RuleImport|rules_template' app/src/main frontend/src -g '*.java' -g '*.ts' -g '*.tsx' 2>/dev/null || true)"
fail_if_matches "retired rule XML import path must remain absent from app/frontend wiring" "${retired_rule_xml_import}"

if [ "${failures}" -gt 0 ]; then
  echo "Legacy guardrails failed: ${failures} issue group(s)."
  exit 1
fi

echo "Legacy guardrails passed."
