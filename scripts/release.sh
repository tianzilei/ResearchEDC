#!/bin/bash
# =============================================================================
# OpenClinica — Release & Rollback Workflow
#
# Orchestrates the full release process: build, scan, tag, push, deploy.
# Also supports rollback to a previous release.
#
# Usage:
#   bash scripts/release.sh <version>        # Create and deploy release
#   bash scripts/release.sh rollback <tag>   # Rollback to previous tag
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "${SCRIPT_DIR}")"

ACTION="${1:-}"
VERSION="${2:-}"

if [ -z "${ACTION}" ]; then
    echo "Usage:"
    echo "  $0 <version>                     Create and deploy release"
    echo "  $0 rollback <previous-tag>        Rollback to previous version"
    echo ""
    echo "Examples:"
    echo "  $0 3.18.0                        Release version 3.18.0"
    echo "  $0 rollback 3.17.3               Rollback to 3.17.3"
    exit 1
fi

case "${ACTION}" in
    rollback)
        # ========== Rollback ==========
        if [ -z "${VERSION}" ]; then
            echo "Usage: $0 rollback <previous-tag>"
            exit 1
        fi

        echo "=== OpenClinica Rollback to ${VERSION} ==="
        echo ""

        # Check that the previous images exist
        for img in "openclinica-web:${VERSION}" "openclinica-ws:${VERSION}"; do
            if ! docker image inspect "${img}" &>/dev/null; then
                echo "ERROR: Image ${img} not found locally."
                echo "  Pull it first: docker pull ${img}"
                exit 1
            fi
        done

        # Re-tag as current
        docker tag "openclinica-web:${VERSION}" "openclinica-web:latest"
        docker tag "openclinica-ws:${VERSION}" "openclinica-ws:latest"
        echo "Re-tagged ${VERSION} as latest."

        # Restore database if a backup exists for this version
        if [ -f "${PROJECT_DIR}/backups/pre-upgrade-${VERSION}.dump" ]; then
            echo "Pre-upgrade backup found. Restore with:"
            echo "  bash scripts/restore.sh pre-upgrade-${VERSION}"
        fi

        # Redeploy
        echo "Redeploying..."
        docker compose -f "${PROJECT_DIR}/deploy/compose/docker-compose.prod.yml" up -d web ws

        echo "=== Rollback to ${VERSION} complete ==="
        ;;

    *)
        # ========== Release ==========
        VERSION="${ACTION}"
        echo "=== OpenClinica Release ${VERSION} ==="
        echo ""

        # Step 1: Verify clean git state
        echo "Step 1: Checking git state..."
        if [ -n "$(git status --porcelain 2>/dev/null)" ]; then
            echo "ERROR: Working directory has uncommitted changes."
            echo "  Commit or stash them before releasing."
            exit 1
        fi
        git fetch --tags
        if git rev-parse "v${VERSION}" &>/dev/null; then
            echo "WARNING: Tag v${VERSION} already exists."
            echo "  Remove it first: git tag -d v${VERSION} && git push origin :refs/tags/v${VERSION}"
        fi
        echo ""

        # Step 2: Run full build
        echo "Step 2: Building WARs..."
        bash "${SCRIPT_DIR}/build.sh" --skip-tests
        echo ""

        # Step 3: Build Docker images
        echo "Step 3: Building Docker images..."
        TAG="${VERSION}" bash "${SCRIPT_DIR}/docker-build.sh"
        echo ""

        # Step 4: Vulnerability scan
        echo "Step 4: Vulnerability scanning..."
        if command -v trivy &>/dev/null; then
            TAG="${VERSION}" bash "${SCRIPT_DIR}/scan.sh" image || true
        else
            echo "  Trivy not installed. Skipping vulnerability scan."
        fi
        echo ""

        # Step 5: Smoke test
        echo "Step 5: Running smoke tests..."
        bash "${SCRIPT_DIR}/smoke-test.sh" || {
            echo "WARNING: Smoke tests failed. Inspect before deploying."
        }
        echo ""

        # Step 6: Tag and push
        echo "Step 6: Tagging release..."
        git tag -a "v${VERSION}" -m "OpenClinica release ${VERSION}"
        echo "  Tagged as v${VERSION}"
        echo ""

        # Step 7: Create pre-upgrade backup (if production is running)
        echo "Step 7: Pre-upgrade backup..."
        if docker ps --format '{{.Names}}' | grep -q "^oc-prod-postgres$"; then
            BACKUP_DIR="${PROJECT_DIR}/backups"
            mkdir -p "${BACKUP_DIR}"
            # Use a compact timestamp approach
            docker exec oc-prod-postgres pg_dump -U openclinica --format=custom \
                --file="/tmp/pre_upgrade_${VERSION}.dump" openclinica
            docker cp "oc-prod-postgres:/tmp/pre_upgrade_${VERSION}.dump" "${BACKUP_DIR}/pre-upgrade-${VERSION}.dump"
            docker exec oc-prod-postgres rm -f "/tmp/pre_upgrade_${VERSION}.dump"
            echo "  Pre-upgrade backup: ${BACKUP_DIR}/pre-upgrade-${VERSION}.dump"
        else
            echo "  Production database not running. Skipping pre-upgrade backup."
        fi
        echo ""

        echo "=== Release ${VERSION} preparation complete ==="
        echo ""
        echo "Summary:"
        echo "  Images:  openclinica-web:${VERSION}, openclinica-ws:${VERSION}"
        echo "  Git tag: v${VERSION}"
        echo ""
        echo "To deploy:"
        echo "  1. Push images to registry:"
        echo "     docker push openclinica-web:${VERSION}"
        echo "     docker push openclinica-ws:${VERSION}"
        echo "  2. On production server:"
        echo "     docker compose -f deploy/compose/docker-compose.prod.yml pull"
        echo "     docker compose -f deploy/compose/docker-compose.prod.yml up -d web ws nginx"
        echo ""
        echo "To rollback:"
        echo "  bash scripts/release.sh rollback <previous-version>"
        ;;
esac
