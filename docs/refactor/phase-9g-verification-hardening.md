# Phase 9G: Verification Hardening

**Created:** 2026-06-25
**Status:** Complete
**Purpose:** define quality gates and guardrails to prevent regression to retired patterns.

## Required Gates By Change Type

### Backend Module/API Changes

```bash
# Compile check
mvn -pl app -am compile -DskipTests

# Module boundary verification (no circular dependencies)
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
```

### Frontend Changes

```bash
# TypeScript type checking
cd frontend && pnpm typecheck

# ESLint (0 errors, 0 warnings)
cd frontend && pnpm lint

# Unit tests
cd frontend && pnpm test --run
```

### Broad Backend/Frontend Changes

```bash
# Full daily gauntlet
bash scripts/ci/daily-gauntlet.sh
```

This runs:
1. `git status --short` (clean tree check)
2. Frontend lint
3. Frontend typecheck
4. Modulith verification
5. Export tests
6. Backend tests

## Guardrails

### Prevented Patterns

| Pattern | Guardrail | Rationale |
|---|---|---|
| New `shared/src/main/java` | CI check: `find shared/src/main/java -name "*.java" \| wc -l` must be 0 | `shared/` is resource-only; all Java belongs in modules |
| New `/api/legacy/*` callers | grep check: no new imports of legacy endpoints | Legacy gateway is being retired, not extended |
| New `studyId=0` placeholders | grep check: no hardcoded zero study IDs | Placeholder study IDs cause cross-study data leakage |
| New raw `fetch` in pages | grep check: `fetch(` only in api/client.ts and auth | Raw fetch bypasses CSRF and error handling |

### CI Integration

Add to `scripts/ci/daily-gauntlet.sh`:

```bash
echo "=== Guardrail: no shared Java ==="
SHARED_JAVA=$(find shared/src/main/java -name "*.java" 2>/dev/null | wc -l)
if [ "$SHARED_JAVA" -gt 0 ]; then
  echo "FAIL: shared/src/main/java contains $SHARED_JAVA Java files"
  exit 1
fi
echo "PASS: shared/src/main/java is empty"

echo "=== Guardrail: no new legacy callers ==="
LEGACY_CALLERS=$(grep -r "/api/legacy/" frontend/src --include="*.ts" --include="*.tsx" -l 2>/dev/null | wc -l)
if [ "$LEGACY_CALLERS" -gt 0 ]; then
  echo "WARN: $LEGACY_CALLERS files still call /api/legacy/* (expected: legacy controllers retained for backward compatibility)"
fi

echo "=== Guardrail: no studyId=0 ==="
PLACEHOLDER_COUNT=$(grep -r "studyId.*=.*0\|studyId.*??.*0" frontend/src --include="*.ts" --include="*.tsx" -l 2>/dev/null | wc -l)
if [ "$PLACEHOLDER_COUNT" -gt 0 ]; then
  echo "WARN: $PLACEHOLDER_COUNT files use studyId=0 pattern (verify hooks have enabled guards)"
fi
```

## Verification Checklist

### Per-Slice Verification

For each code change:

- [ ] `pnpm typecheck` passes
- [ ] `pnpm lint` passes (0 errors, 0 warnings)
- [ ] `pnpm test --run` passes
- [ ] `mvn compile` succeeds
- [ ] `ModulithVerificationTest` passes (if backend module changed)
- [ ] No new `/api/legacy/*` callers introduced
- [ ] No new `studyId=0` placeholders introduced
- [ ] No new `shared/src/main/java` files created

### Daily Gauntlet

```bash
bash scripts/ci/daily-gauntlet.sh
```

This is the broad gate that runs all checks. It should be run:
- Before merging any PR
- After completing a Phase 9 workstream
- As part of the release process

## Files Involved

| File | Role |
|---|---|
| `scripts/ci/daily-gauntlet.sh` | Daily verification gauntlet |
| `AGENTS.md` | Project knowledge base with commands |

## Verification

- `pnpm typecheck` — 0 errors
- `pnpm lint` — 0 errors
- `pnpm test --run` — 25/25 pass
- `mvn compile` — BUILD SUCCESS
- `ModulithVerificationTest` — 1/1 pass
