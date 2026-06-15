# Next Refactor And Removal Plan

**Updated:** 2026-06-15

## Current State

| Surface | Before | Current | Removed | % |
|---------|--------|---------|---------|---|
| shared/ files | 793 | 279 | 514 | 64.8% |
| shared/ lines | ~80,000 | 35,499 | ~44,500 | ~56% |
| dao/ files | 186 | 75 | 111 | 59.7% |
| web/ | 480 | 0 | 480 | 100% |
| ws/ | 75 | 0 | 75 | 100% |
| SPI methods | 878 | 878 covered | 120 removed | 100% |
| Module files | — | 404 | — | — |
| Module lines | — | 27,471 | — | — |

**Code balance:** 279 legacy / 404 modern = 41% legacy / 59% modern (files), 56% legacy / 44% modern (lines)

## Status

Dead code is exhausted. All 279 remaining shared/ files have active callers from module code.

## Next: Line Reduction

Goal: reduce shared/ from 35,499 lines by removing dead methods, fields, and inner classes from alive files.

Priority targets (largest files with most dead surface):

1. **EntityDAO.java deleted** — 3,156 lines removed ✅
2. **ExtractBean.java deleted** — 3,000+ lines removed ✅
3. **Large bean files** — check for dead methods in StudyBean, CRFVersionBean, ItemBean, EventCRFBean, StudySubjectBean, etc.
4. **Large domain entities** — check for dead methods in domain entities
5. **DAO SPI interfaces** — check for dead abstract methods still in interfaces

## Verification

```bash
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
bash scripts/ci/check-legacy-guardrails.sh
```
