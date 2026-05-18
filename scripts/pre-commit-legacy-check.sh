#!/bin/bash
# Pre-commit hook: Enforce legacy kernel freeze policy
# Blocks non-bugfix changes to legacy-core/, web/, ws/ modules
# Allowed: only *.java changes that are bug fixes or security patches

LEGACY_DIRS="legacy-core/ web/ ws/"
CHANGED=$(git diff --cached --name-only -- $LEGACY_DIRS)

if [ -z "$CHANGED" ]; then
    exit 0
fi

echo "WARNING: Changes detected in frozen legacy directories:"
echo "$CHANGED" | sed 's/^/  - /'
echo ""
echo "Legacy Kernel is FROZEN. Only bug fixes and security patches are allowed."
echo "New features MUST go into app/module/<name>/"
echo ""
echo "To bypass this check (bug fixes only): ALLOW_LEGACY_CHANGES=1 git commit"
echo ""

if [ "$ALLOW_LEGACY_CHANGES" != "1" ]; then
    echo "Commit BLOCKED by legacy freeze policy."
    echo "Set ALLOW_LEGACY_CHANGES=1 to bypass for bug fixes."
    exit 1
fi
