#!/bin/bash
# =============================================================================
# OpenClinica — Docker Image Build
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

echo "=== OpenClinica Docker Build ==="
echo "  Tag:      ${TAG}"
echo "  Platform: ${PLATFORM}"
echo "  Push:     $([ "${PUSH}" = true ] && echo 'YES' || echo 'no')"
echo ""

# Build web image
echo "Building openclinica-web:${TAG}..."
DOCKER_BUILDKIT=1 docker build \
    --platform "${PLATFORM}" \
    -t "openclinica-web:${TAG}" \
    -f "${PROJECT_DIR}/docker/web/Dockerfile" \
    "${PROJECT_DIR}"

# Tag as latest if not already
if [ "${TAG}" != "latest" ]; then
    docker tag "openclinica-web:${TAG}" "openclinica-web:latest"
fi
echo ""

# Build ws image
echo "Building openclinica-ws:${TAG}..."
DOCKER_BUILDKIT=1 docker build \
    --platform "${PLATFORM}" \
    -t "openclinica-ws:${TAG}" \
    -f "${PROJECT_DIR}/docker/ws/Dockerfile" \
    "${PROJECT_DIR}"

if [ "${TAG}" != "latest" ]; then
    docker tag "openclinica-ws:${TAG}" "openclinica-ws:latest"
fi
echo ""

# Push to registry (if --push)
if [ "${PUSH}" = true ]; then
    echo "Pushing images..."
    docker push "openclinica-web:${TAG}"
    docker push "openclinica-ws:${TAG}"
    if [ "${TAG}" != "latest" ]; then
        docker push "openclinica-web:latest"
        docker push "openclinica-ws:latest"
    fi
fi

echo "=== Docker Build Complete ==="
docker images --filter=reference='openclinica-*' --format "table {{.Repository}}:{{.Tag}}\t{{.Size}}"
echo ""
echo "Next: bash scripts/smoke-test.sh"
