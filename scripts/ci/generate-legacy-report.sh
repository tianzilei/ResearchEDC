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

# Post-rename: org.akaza.openclinica → org.researchedc
# Check for remaining old-package references
DAO_IMPORTS=$(grep -rl --include='*.java' 'org\.akaza\.openclinica\.\(.*\.\)\?dao\.' app/ web/ ws/ shared/ 2>/dev/null || true)
LEGACY_DAO_IMPORTS=$(echo "$DAO_IMPORTS" | grep -c . || true)

BEAN_IMPORTS=$(grep -rl --include='*.java' 'org\.akaza\.openclinica\.\(.*\.\)\?bean\.' app/ web/ ws/ shared/ 2>/dev/null || true)
LEGACY_BEAN_IMPORTS=$(echo "$BEAN_IMPORTS" | grep -c . || true)

JSP_FILES=$(find web -name '*.jsp' 2>/dev/null || true)
JSP_COUNT=$(echo "$JSP_FILES" | grep -c . || true)

SC_CLASSES=$(grep -rl --include='*.java' 'extends\s\+SecureController\|extends\s\+CoreSecureController' web/ 2>/dev/null || true)
SECURE_CONTROLLER_COUNT=$(echo "$SC_CLASSES" | grep -c . || true)

SOAP_FILES=$(find ws -name '*.java' 2>/dev/null || true)
SOAP_JAVA_COUNT=$(echo "$SOAP_FILES" | grep -c . || true)

EHCACHE_FILES=$(grep -rl --include='*.java' 'net\.sf\.ehcache\|Ehcache\|CacheManager' app/ web/ ws/ shared/ 2>/dev/null || true)
EHCACHE_USAGE=$(echo "$EHCACHE_FILES" | grep -c . || true)

JAX_FILES=$(grep -rl --include='*.java' 'import javax\.' app/ web/ ws/ shared/ 2>/dev/null || true)
JAX_COUNT=$(echo "$JAX_FILES" | grep -c . || true)

# Check app/module/ for forbidden legacy imports
MODULE_IMPORTS=$(grep -rl --include='*.java' 'import org\.researchedc\.dao\.\|import org\.researchedc\.bean\.\|import org\.researchedc\.domain\.\|import org\.akaza\.openclinica\.' app/module/ 2>/dev/null || true)
MODULE_LEGACY_IMPORTS=$(echo "$MODULE_IMPORTS" | grep -c . || true)

# SPI coverage: count SPI interfaces with @Primary adapters
SPI_COUNT=$(find shared/src/main/java/org/researchedc/dao/spi -name '*.java' -type f | wc -l)
ADAPTER_COUNT=$(find app -path '*/internal/adapter/*DaoAdapter.java' -o -path '*/internal/adapter/*DaoAdapter.java' | wc -l)

# Direct DAO construction gauge
NEW_DAO_COUNT=$(git grep -c 'new \([A-Z][a-zA-Z]*DAO\b\|[A-Z][a-zA-Z]*Dao\b\)(' -- '*.java' ':!app/src/main/java/org/researchedc/config/' ':!shared/src/main/java/org/researchedc/dao/' ':!*Test*.java' ':!app/src/main/java/org/researchedc/module/*/internal/adapter/' 2>/dev/null | tail -1 | cut -d: -f2 || echo 0)

cat > "${REPORT_FILE}" <<HEADER
# ResearchEDC Legacy Refactor Report

Generated: $(date -u +"%Y-%m-%d %H:%M:%S UTC")

## Summary

| Metric | Count |
|--------|-------|
| org.akaza.openclinica.*dao.* residual imports | ${LEGACY_DAO_IMPORTS} |
| org.akaza.openclinica.*bean.* residual imports | ${LEGACY_BEAN_IMPORTS} |
| JSP files (web/) | ${JSP_COUNT} |
| SecureController subclasses (web/) | ${SECURE_CONTROLLER_COUNT} |
| SOAP Java files (ws/) | ${SOAP_JAVA_COUNT} |
| Ehcache usage files | ${EHCACHE_USAGE} |
| javax.* residual import files | ${JAX_COUNT} |
| Module legacy imports (app/module/) | ${MODULE_LEGACY_IMPORTS} |
| SPI interfaces | ${SPI_COUNT} |
| @Primary module adapters | ${ADAPTER_COUNT} |
| Direct new XxxDAO() in consumer code | ${NEW_DAO_COUNT} |

---

## 1. Old Package Imports (org.akaza.openclinica)

Files still importing old-package DAO classes (${LEGACY_DAO_IMPORTS} files):

HEADER

if [ -n "$DAO_IMPORTS" ] && [ "$LEGACY_DAO_IMPORTS" -gt 0 ]; then
  echo "$DAO_IMPORTS" | while read -r f; do
    echo "- \`${f}\`" >> "${REPORT_FILE}"
  done
else
  echo "_No old-package DAO imports found._" >> "${REPORT_FILE}"
fi

cat >> "${REPORT_FILE}" <<SECTION2

---

## 2. Old Package Bean Imports

Files importing old-package Bean classes (${LEGACY_BEAN_IMPORTS} files):

SECTION2

if [ -n "$BEAN_IMPORTS" ] && [ "$LEGACY_BEAN_IMPORTS" -gt 0 ]; then
  echo "$BEAN_IMPORTS" | while read -r f; do
    echo "- \`${f}\`" >> "${REPORT_FILE}"
  done
else
  echo "_No old-package Bean imports found._" >> "${REPORT_FILE}"
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

## 8. Module Legacy Imports

Files in \`app/module/\` importing legacy packages: ${MODULE_LEGACY_IMPORTS}

SECTION8

if [ -n "$MODULE_IMPORTS" ] && [ "$MODULE_LEGACY_IMPORTS" -gt 0 ]; then
  echo "$MODULE_IMPORTS" | while read -r f; do
    echo "- \`${f}\`" >> "${REPORT_FILE}"
  done
else
  echo "_No module legacy imports found._" >> "${REPORT_FILE}"
fi

cat >> "${REPORT_FILE}" <<SECTION9

---

## 9. SPI Coverage

| Metric | Count |
|--------|-------|
| SPI interfaces | ${SPI_COUNT} |
| @Primary module adapters | ${ADAPTER_COUNT} |
| Direct new XxxDAO() in consumer code | ${NEW_DAO_COUNT} |

SECTION9

cat >> "${REPORT_FILE}" <<FOOTER

---

_Report generated by scripts/ci/generate-legacy-report.sh_
FOOTER

echo "=== Legacy Report: OK (${REPORT_FILE}) ==="
