#!/bin/bash
# =============================================================================
# ResearchEDC — Docker Image Build
#
# Builds Docker images for web and ws modules.
# Uses Maven multi-stage builds — no pre-built WAR needed.
#
# Usage:
#   bash scripts/docker-build.sh [--push] [--tag TAG]
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "${SCRIPT_DIR}")"

PUSH=false
TAG="${TAG:-3.18-SNAPSHOT}"
PLATFORM="${PLATFORM:-linux/amd64}"

while [[ $# -gt 0 ]]; do
    case "$1" in
        --push) PUSH=true; shift ;;
        --tag) TAG="$2"; shift 2 ;;
        --platform) PLATFORM="$2"; shift 2 ;;
        *) echo "Unknown option: $1"; exit 1 ;;
    esac
done

echo "=== ResearchEDC Docker Build ==="
echo "  Tag:      ${TAG}"
echo "  Platform: ${PLATFORM}"
echo "  Push:     $([ "${PUSH}" = true ] && echo 'YES' || echo 'no')"
echo ""

# Build web image
echo "Building researchedc-web:${TAG}..."
DOCKER_BUILDKIT=1 docker build \
    --platform "${PLATFORM}" \
    -t "researchedc-web:${TAG}" \
    -f "${PROJECT_DIR}/docker/web/Dockerfile" \
    "${PROJECT_DIR}"

# Tag as latest if not already
if [ "${TAG}" != "latest" ]; then
    docker tag "researchedc-web:${TAG}" "researchedc-web:latest"
fi
echo ""

# Build ws image
echo "Building researchedc-ws:${TAG}..."
DOCKER_BUILDKIT=1 docker build \
    --platform "${PLATFORM}" \
    -t "researchedc-ws:${TAG}" \
    -f "${PROJECT_DIR}/docker/ws/Dockerfile" \
    "${PROJECT_DIR}"

if [ "${TAG}" != "latest" ]; then
    docker tag "researchedc-ws:${TAG}" "researchedc-ws:latest"
fi
echo ""

# Push to registry (if --push)
if [ "${PUSH}" = true ]; then
    echo "Pushing images..."
    docker push "researchedc-web:${TAG}"
    docker push "researchedc-ws:${TAG}"
    if [ "${TAG}" != "latest" ]; then
        docker push "researchedc-web:latest"
        docker push "researchedc-ws:latest"
    fi
fi

echo "=== Docker Build Complete ==="
docker images --filter=reference='researchedc-*' --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}"
echo ""
echo "Next: bash scripts/smoke-test.sh"
