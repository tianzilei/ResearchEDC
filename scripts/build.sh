#!/bin/bash
# =============================================================================
# OpenClinica — Maven Build Script
#
# Compiles all modules, runs tests (if DB available), and packages WARs.
# Use --skip-tests to skip test execution.
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "${SCRIPT_DIR}")"
MVNW="${PROJECT_DIR}/mvnw"
MAVEN="${MVNW:-mvn}"

cd "${PROJECT_DIR}"

SKIP_TESTS=false
SKIP_ENFORCE=false
PROFILE=""

while [[ $# -gt 0 ]]; do
    case "$1" in
        --skip-tests) SKIP_TESTS=true; shift ;;
        --skip-enforce) SKIP_ENFORCE=true; shift ;;
        --profile) PROFILE="-P$2"; shift 2 ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

MAVEN_OPTS="${MAVEN_OPTS:--Xmx2g}"

echo "=== OpenClinica Build ==="
echo "  Java:    $(java -version 2>&1 | head -1)"
echo "  Maven:   $(${MAVEN} --version 2>&1 | head -1)"
echo "  Tests:   $([ "${SKIP_TESTS}" = true ] && echo 'SKIPPED' || echo 'ENABLED')"
echo "  Profile: ${PROFILE:-default}"
echo ""

# Validate Java version
JAVA_VER=$(java -version 2>&1 | sed -n 's/.*version "\([0-9]*\)\..*/\1/p')
if [ "${JAVA_VER}" -lt 21 ]; then
    echo "ERROR: Java 21+ required (found Java ${JAVA_VER})"
    exit 1
fi

# Enforce dependency convergence
if [ "${SKIP_ENFORCE}" = false ]; then
    echo "Step 1: Checking dependency convergence..."
    ${MAVEN} enforcer:enforce ${PROFILE} || {
        echo "WARNING: Dependency convergence check failed."
        echo "  Run 'mvn dependency:tree' to inspect conflicts."
        echo "  Use --skip-enforce to bypass."
        exit 1
    }
    echo ""
fi

# Compile
echo "Step 2: Compiling all modules..."
${MAVEN} clean compile ${PROFILE} -DskipTests
echo ""

# Run tests (unless skipped)
if [ "${SKIP_TESTS}" = false ]; then
    echo "Step 3: Running tests..."
    echo "  Note: DAO/Service integration tests require a PostgreSQL database."
    echo "  Run 'bash scripts/db-init-schema.sh' first to set up the test DB."
    echo ""
    ${MAVEN} test ${PROFILE} || {
        echo "WARNING: Some tests failed. Check surefire reports for details."
        echo "  Reports: ${PROJECT_DIR}/**/target/surefire-reports/"
    }
else
    echo "Step 3: Skipping tests (--skip-tests)"
fi
echo ""

# Package
echo "Step 4: Packaging WARs..."
${MAVEN} package -DskipTests ${PROFILE}
echo ""

echo "=== Build Complete ==="
echo "  core JAR: $(find core/target -name '*.jar' -not -name '*-sources*' -not -name '*-javadoc*' 2>/dev/null | head -1)"
echo "  web WAR:  $(find web/target -name '*.war' 2>/dev/null | head -1)"
echo "  ws WAR:   $(find ws/target -name '*.war' 2>/dev/null | head -1)"
echo ""
echo "Next: bash scripts/docker-build.sh"
