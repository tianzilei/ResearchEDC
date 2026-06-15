# Next Refactor And Removal Plan

**Updated:** 2026-06-15

## Current State

| Surface | Before | After | Removed | % |
|---------|--------|-------|---------|---|
| shared/ files | 793 | 278 | 515 | 64.9% |
| shared/ lines | ~80,000 | 33,997 | ~46,000 | ~57% |
| dao/ files | 186 | 75 | 111 | 59.7% |
| web/ | 480 | 0 | 480 | 100% |
| ws/ | 75 | 0 | 75 | 100% |
| SPI methods | 878 | 878 covered | 120 removed | 100% |
| Module files | — | 404 | — | — |

**Code balance:** 278 legacy / 404 modern = 41% legacy / 59% modern (files)

## Status

Dead code is exhausted. All 278 remaining shared files have active callers.

## Next: Dead @OneToMany Collection Cleanup

The explore agent found 41 `@OneToMany` collection getters across 12 domain entities with zero callers from app/ code. These are JPA-mapped but represent unnecessary Hibernate overhead.

Priority: remove dead `@OneToMany` fields that are confirmed unused by module code.

## Next: Dead Inner Classes / Static Fields

Scan remaining files for dead inner classes, static fields, and constants.

## Verification

```bash
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
bash scripts/ci/check-legacy-guardrails.sh
```
