#!/bin/bash
# =============================================================================
# ResearchEDC — Vulnerability Scanning
#
# Scans Docker images for known CVEs using Trivy.
# Install Trivy: https://trivy.dev/latest/getting-started/installation/
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "${SCRIPT_DIR}")"

SCAN_TYPE="${1:-image}"  # image, filesystem, or sbom
SEVERITY="${SEVERITY:-CRITICAL,HIGH}"
TAG="${TAG:-3.18-SNAPSHOT}"
EXIT_ON_FINDING="${EXIT_ON_FINDING:-false}"

echo "=== ResearchEDC Vulnerability Scan ==="
echo "  Scanner: Trivy ($(trivy --version 2>&1 | head -1))"
echo "  Type:    ${SCAN_TYPE}"
echo "  Severity: ${SEVERITY}"
echo ""

# Check Trivy is installed
if ! command -v trivy &> /dev/null; then
    echo "ERROR: Trivy not found."
    echo "  Install: brew install trivy"
    echo "  Or: https://trivy.dev/latest/getting-started/installation/"
    exit 1
fi

scan_image() {
    local image="$1"
    local severity="${2:-${SEVERITY}}"

    echo "--- Scanning: ${image} ---"
    trivy image \
        --severity "${severity}" \
        --no-progress \
        --exit-code 0 \
        --ignore-unfixed \
        --format table \
        "${image}" 2>/dev/null || true
    echo ""
}

scan_filesystem() {
    local path="$1"
    echo "--- Scanning filesystem: ${path} ---"
    trivy filesystem \
        --severity "${SEVERITY}" \
        --no-progress \
        --exit-code 0 \
        --ignore-unfixed \
        --format table \
        "${path}" 2>/dev/null || true
    echo ""
}

case "${SCAN_TYPE}" in
    image)
        echo "Scanning Docker images..."
        scan_image "researchedc-web:${TAG}"
        scan_image "researchedc-ws:${TAG}"
        scan_image "tomcat:10.1-jdk21-temurin"
        scan_image "postgres:17-alpine"
        scan_image "nginx:1.27-alpine"
        ;;

    filesystem)
        echo "Scanning project filesystem..."
        scan_filesystem "${PROJECT_DIR}/legacy-core"
        scan_filesystem "${PROJECT_DIR}/web"
        scan_filesystem "${PROJECT_DIR}/ws"
        ;;

    sbom)
        mkdir -p "${PROJECT_DIR}/target/sbom"
        echo "Generating SBOM for researchedc-web:${TAG}..."
        trivy image --format cyclonedx --output "${PROJECT_DIR}/target/sbom/web-${TAG}.cdx.json" "researchedc-web:${TAG}"
        echo "  SBOM: target/sbom/web-${TAG}.cdx.json"

        echo "Generating SBOM for researchedc-ws:${TAG}..."
        trivy image --format cyclonedx --output "${PROJECT_DIR}/target/sbom/ws-${TAG}.cdx.json" "researchedc-ws:${TAG}"
        echo "  SBOM: target/sbom/ws-${TAG}.cdx.json"
        ;;

    *)
        echo "Usage: $0 [image|filesystem|sbom]"
        exit 1
        ;;
esac

echo "=== Scan Complete ==="
echo ""
echo "Review findings and address HIGH/CRITICAL CVEs before production deployment."
echo ""
echo "To scan with HTML report:"
echo "  trivy image --format html --output report.html researchedc-web:${TAG}"
