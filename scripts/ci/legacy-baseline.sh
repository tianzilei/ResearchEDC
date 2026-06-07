#!/usr/bin/env bash
set -euo pipefail

REPORT_DIR="${1:-build/reports/legacy}"
MARKDOWN_FILE="${REPORT_DIR}/legacy-baseline.md"
JSON_FILE="${REPORT_DIR}/legacy-baseline.json"

mkdir -p "${REPORT_DIR}"

count_files() {
  local path="$1"
  shift
  if [ -d "${path}" ]; then
    find "${path}" "$@" -type f 2>/dev/null | wc -l | tr -d ' '
  else
    echo 0
  fi
}

count_rg() {
  local pattern="$1"
  shift
  (rg -n "${pattern}" "$@" 2>/dev/null || true) | wc -l | tr -d ' '
}

SHARED_JAVA_COUNT="$(count_files shared/src/main/java/org/researchedc -name '*.java')"
SHARED_DAO_COUNT="$(count_files shared/src/main/java/org/researchedc/dao -name '*.java')"
WEB_JAVA_COUNT="$(count_files web/src/main/java -name '*.java')"
WEB_JSP_COUNT="$(count_files web/src/main/webapp -name '*.jsp')"
SECURE_CONTROLLER_COUNT="$(count_rg 'extends\s+(SecureController|CoreSecureController)' web/src/main/java -g '*.java')"
WS_JAVA_COUNT="$(count_files ws/src/main/java -name '*.java')"
DAO_PROVIDER_COUNT="$(count_rg 'DaoProvider\.getDao' app shared web ws -g '*.java')"
DIRECT_DAO_COUNT="$((git grep -n 'new \([A-Z][a-zA-Z]*DAO\b\|CrfDao\|RuleDao\|StudyDao\|SubjectDao\|DatasetDao\|FilterDao\|StudyGroupDao\|DiscrepancyNoteDao\|EventCrfDao\|ItemDao\|ItemDataDao\|ItemGroupDao\|SectionDao\|ItemFormMetadataDao\|ItemGroupMetadataDao\)(' -- '*.java' ':!app/src/main/java/org/researchedc/config/' ':!shared/src/main/java/org/researchedc/dao/' ':!*Test*.java' ':!*adapter/Item*' ':!*adapter/Section*' ':!*adapter/Event*' ':!*adapter/Crf*' ':!*adapter/Rule*' ':!*adapter/Study*' ':!*adapter/Subject*' ':!*adapter/User*' ':!*adapter/Filter*' ':!*adapter/Dataset*' ':!*adapter/Discrepancy*' 2>/dev/null || true) | wc -l | tr -d ' ')"
PUBLIC_MODULE_LEGACY_IMPORTS="$(count_rg 'import org\.researchedc\.(dao|bean|domain)\.|import org\.akaza\.openclinica\.' app/src/main/java/org/researchedc/module -g '*.java' -g '!**/internal/adapter/**')"
SOAP_ENDPOINT_COUNT="$(count_rg '@Endpoint' ws/src/main/java -g '*.java')"

cat > "${MARKDOWN_FILE}" <<EOF
# Legacy Baseline

Generated: $(date -u +"%Y-%m-%d %H:%M:%S UTC")

| Metric | Count |
|--------|-------|
| shared Java files | ${SHARED_JAVA_COUNT} |
| shared DAO/SPI/implementation files | ${SHARED_DAO_COUNT} |
| web Java files | ${WEB_JAVA_COUNT} |
| JSP files | ${WEB_JSP_COUNT} |
| SecureController/CoreSecureController subclass matches | ${SECURE_CONTROLLER_COUNT} |
| ws Java files | ${WS_JAVA_COUNT} |
| SOAP endpoint annotation matches | ${SOAP_ENDPOINT_COUNT} |
| DaoProvider.getDao matches | ${DAO_PROVIDER_COUNT} |
| direct new XxxDAO() consumer matches | ${DIRECT_DAO_COUNT} |
| public module legacy imports | ${PUBLIC_MODULE_LEGACY_IMPORTS} |

EOF

cat > "${JSON_FILE}" <<EOF
{
  "generatedUtc": "$(date -u +"%Y-%m-%dT%H:%M:%SZ")",
  "sharedJavaFiles": ${SHARED_JAVA_COUNT},
  "sharedDaoFiles": ${SHARED_DAO_COUNT},
  "webJavaFiles": ${WEB_JAVA_COUNT},
  "jspFiles": ${WEB_JSP_COUNT},
  "secureControllerSubclassMatches": ${SECURE_CONTROLLER_COUNT},
  "wsJavaFiles": ${WS_JAVA_COUNT},
  "soapEndpointAnnotationMatches": ${SOAP_ENDPOINT_COUNT},
  "daoProviderGetDaoMatches": ${DAO_PROVIDER_COUNT},
  "directDaoConstructionConsumerMatches": ${DIRECT_DAO_COUNT},
  "publicModuleLegacyImports": ${PUBLIC_MODULE_LEGACY_IMPORTS}
}
EOF

cat "${MARKDOWN_FILE}"
echo "Baseline written to ${MARKDOWN_FILE} and ${JSON_FILE}"
