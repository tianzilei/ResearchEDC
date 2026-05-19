#!/usr/bin/env bash
set -euo pipefail

REPORT_DIR="build/reports/legacy"
REPORT_FILE="${REPORT_DIR}/legacy-report.md"

mkdir -p "${REPORT_DIR}"

echo "=== Legacy Refactor Report ==="
echo "Generating report at ${REPORT_FILE} ..."

LEGACY_DAO_IMPORTS=0
LEGACY_BEAN_IMPORTS=0
JSP_COUNT=0
SECURE_CONTROLLER_COUNT=0
SOAP_JAVA_COUNT=0
EHCACHE_USAGE=0
JAX_COUNT=0
MODULE_LEGACY_IMPORTS=0

DAO_IMPORTS=$(grep -rl --include='*.java' 'org\.akaza\.openclinica\.\(.*\.\)\?dao\.' app/ web/ ws/ core/ 2>/dev/null || true)
LEGACY_DAO_IMPORTS=$(echo "$DAO_IMPORTS" | grep -c . || true)

BEAN_IMPORTS=$(grep -rl --include='*.java' 'org\.akaza\.openclinica\.\(.*\.\)\?bean\.' app/ web/ ws/ core/ 2>/dev/null || true)
LEGACY_BEAN_IMPORTS=$(echo "$BEAN_IMPORTS" | grep -c . || true)

JSP_FILES=$(find web -name '*.jsp' 2>/dev/null || true)
JSP_COUNT=$(echo "$JSP_FILES" | grep -c . || true)

SC_CLASSES=$(grep -rl --include='*.java' 'extends\s\+SecureController\|extends\s\+CoreSecureController' web/ 2>/dev/null || true)
SECURE_CONTROLLER_COUNT=$(echo "$SC_CLASSES" | grep -c . || true)

SOAP_FILES=$(find ws -name '*.java' 2>/dev/null || true)
SOAP_JAVA_COUNT=$(echo "$SOAP_FILES" | grep -c . || true)

EHCACHE_FILES=$(grep -rl --include='*.java' 'net\.sf\.ehcache\|Ehcache\|CacheManager' app/ core/ web/ ws/ 2>/dev/null || true)
EHCACHE_USAGE=$(echo "$EHCACHE_FILES" | grep -c . || true)

JAX_FILES=$(grep -rl --include='*.java' 'import javax\.' app/ core/ web/ ws/ 2>/dev/null || true)
JAX_COUNT=$(echo "$JAX_FILES" | grep -c . || true)

MODULE_IMPORTS=$(grep -rl --include='*.java' 'import org\.akaza\.openclinica\.core\.\|import org\.akaza\.openclinica\.bean\.\|import org\.akaza\.openclinica\.dao\.\|import org\.akaza\.openclinica\.domain\.' app/module/ 2>/dev/null || true)
MODULE_LEGACY_IMPORTS=$(echo "$MODULE_IMPORTS" | grep -c . || true)

cat > "${REPORT_FILE}" <<HEADER
# OpenClinica Legacy Refactor Report

Generated: $(date -u +"%Y-%m-%d %H:%M:%S UTC")

## Summary

| Metric | Count |
|--------|-------|
| Legacy DAO imports (app/web/ws/core) | ${LEGACY_DAO_IMPORTS} |
| Legacy Bean imports (app/web/ws/core) | ${LEGACY_BEAN_IMPORTS} |
| JSP files (web/) | ${JSP_COUNT} |
| SecureController subclasses (web/) | ${SECURE_CONTROLLER_COUNT} |
| SOAP Java files (ws/) | ${SOAP_JAVA_COUNT} |
| Ehcache usage files (app/core/web/ws) | ${EHCACHE_USAGE} |
| javax.* residual import files (app/core/web/ws) | ${JAX_COUNT} |
| Modern module imports from legacy (app/module/) | ${MODULE_LEGACY_IMPORTS} |

---

## 1. Legacy DAO Imports

Files importing legacy DAO classes (${LEGACY_DAO_IMPORTS} files):

HEADER

if [ -n "$DAO_IMPORTS" ] && [ "$LEGACY_DAO_IMPORTS" -gt 0 ]; then
  echo "$DAO_IMPORTS" | while read -r f; do
    echo "- \`${f}\`" >> "${REPORT_FILE}"
  done
else
  echo "_No legacy DAO imports found._" >> "${REPORT_FILE}"
fi

cat >> "${REPORT_FILE}" <<SECTION2

---

## 2. Legacy Bean Imports

Files importing legacy Bean classes (${LEGACY_BEAN_IMPORTS} files):

SECTION2

if [ -n "$BEAN_IMPORTS" ] && [ "$LEGACY_BEAN_IMPORTS" -gt 0 ]; then
  echo "$BEAN_IMPORTS" | while read -r f; do
    echo "- \`${f}\`" >> "${REPORT_FILE}"
  done
else
  echo "_No legacy Bean imports found._" >> "${REPORT_FILE}"
fi

cat >> "${REPORT_FILE}" <<SECTION3

---

## 3. JSP Files

Total JSP files: ${JSP_COUNT}

SECTION3

if [ -n "$JSP_FILES" ] && [ "$JSP_COUNT" -gt 0 ]; then
  echo "$JSP_FILES" | while read -r f; do
    echo "- \`${f}\`" >> "${REPORT_FILE}"
  done
else
  echo "_No JSP files found._" >> "${REPORT_FILE}"
fi

cat >> "${REPORT_FILE}" <<SECTION4

---

## 4. SecureController Subclasses

Total SecureController subclasses: ${SECURE_CONTROLLER_COUNT}

SECTION4

if [ -n "$SC_CLASSES" ] && [ "$SECURE_CONTROLLER_COUNT" -gt 0 ]; then
  echo "$SC_CLASSES" | while read -r f; do
    echo "- \`${f}\`" >> "${REPORT_FILE}"
  done
else
  echo "_No SecureController subclasses found._" >> "${REPORT_FILE}"
fi

cat >> "${REPORT_FILE}" <<SECTION5

---

## 5. SOAP Java Files

Total SOAP Java files: ${SOAP_JAVA_COUNT}

SECTION5

if [ -n "$SOAP_FILES" ] && [ "$SOAP_JAVA_COUNT" -gt 0 ]; then
  echo "$SOAP_FILES" | while read -r f; do
    echo "- \`${f}\`" >> "${REPORT_FILE}"
  done
else
  echo "_No SOAP Java files found._" >> "${REPORT_FILE}"
fi

cat >> "${REPORT_FILE}" <<SECTION6

---

## 6. Ehcache Usage

Files with Ehcache references: ${EHCACHE_USAGE}

SECTION6

if [ -n "$EHCACHE_FILES" ] && [ "$EHCACHE_USAGE" -gt 0 ]; then
  echo "$EHCACHE_FILES" | while read -r f; do
    echo "- \`${f}\`" >> "${REPORT_FILE}"
  done
else
  echo "_No Ehcache usage found._" >> "${REPORT_FILE}"
fi

cat >> "${REPORT_FILE}" <<SECTION7

---

## 7. javax.* Residual Imports

Files with javax.* imports: ${JAX_COUNT}

SECTION7

if [ -n "$JAX_FILES" ] && [ "$JAX_COUNT" -gt 0 ]; then
  echo "$JAX_FILES" | while read -r f; do
    echo "- \`${f}\`" >> "${REPORT_FILE}"
  done
else
  echo "_No javax.* residual imports found._" >> "${REPORT_FILE}"
fi

cat >> "${REPORT_FILE}" <<SECTION8

---

## 8. Modern Module Imports from Legacy

Files in \`app/module/\` importing legacy core/bean/dao/domain packages: ${MODULE_LEGACY_IMPORTS}

SECTION8

if [ -n "$MODULE_IMPORTS" ] && [ "$MODULE_LEGACY_IMPORTS" -gt 0 ]; then
  echo "$MODULE_IMPORTS" | while read -r f; do
    echo "- \`${f}\`" >> "${REPORT_FILE}"
  done
else
  echo "_No modern module imports from legacy packages._" >> "${REPORT_FILE}"
fi

cat >> "${REPORT_FILE}" <<FOOTER

---

_Report generated by scripts/ci/generate-legacy-report.sh_
FOOTER

echo "=== Legacy Report: OK (${REPORT_FILE}) ==="
