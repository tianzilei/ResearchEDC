# Next Refactor And Removal Plan

**Updated:** 2026-06-19
**Status:** retained as a short continuity snapshot. The active master plan is now
[`refactor-removal-roadmap.md`](./refactor-removal-roadmap.md).

## Snapshot

- workflow inventory closure: `100.0%` (`963/963`)
- DAO SPI deletion: `100.0%` (`878/878`)
- `shared/dao`: `0` files
- `web/`: deleted
- `ws/`: absent
- remaining `shared/`: `202` Java files
- code balance by file count: `35%` shared legacy / `65%` module modern

## Active Next Step

The next stage is no longer workflow deletion. It is compatibility strangulation inside `app/` and `shared/`, especially:

1. shared support extraction (`core`, `i18n`, `job`, `patterns`, `exception`)
2. data-import compatibility model migration
3. form validation/discrepancy compatibility isolation
4. module adapter DTO contraction
5. gradual `shared/domain` reduction after caller migration

See [`refactor-removal-roadmap.md`](./refactor-removal-roadmap.md) for sequencing, exit gates, and verification.
