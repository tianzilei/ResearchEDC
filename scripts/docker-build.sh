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
TAG="${TAG:-0.1}"
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
echo "Building researchedf-web:${TAG}..."
DOCKER_BUILDKIT=1 docker build \
    --platform "${PLATFORM}" \
    -t "researchedf-web:${TAG}" \
    -f "${PROJECT_DIR}/docker/web/Dockerfile" \
    "${PROJECT_DIR}"

# Tag as latest if not already
if [ "${TAG}" != "latest" ]; then
    docker tag "researchedf-web:${TAG}" "researchedf-web:latest"
fi
echo ""

# Build ws image
echo "Building researchedf-ws:${TAG}..."
DOCKER_BUILDKIT=1 docker build \
    --platform "${PLATFORM}" \
    -t "researchedf-ws:${TAG}" \
    -f "${PROJECT_DIR}/docker/ws/Dockerfile" \
    "${PROJECT_DIR}"

if [ "${TAG}" != "latest" ]; then
    docker tag "researchedf-ws:${TAG}" "researchedf-ws:latest"
fi
echo ""

# Push to registry (if --push)
if [ "${PUSH}" = true ]; then
    echo "Pushing images..."
    docker push "researchedf-web:${TAG}"
    docker push "researchedf-ws:${TAG}"
    if [ "${TAG}" != "latest" ]; then
        docker push "researchedf-web:latest"
        docker push "researchedf-ws:latest"
    fi
fi

echo "=== Docker Build Complete ==="
docker images --filter=reference='researchedf-*' --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}"
echo ""
echo "Next: bash scripts/smoke-test.sh"
