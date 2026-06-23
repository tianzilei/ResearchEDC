# Next Refactor And Removal Plan

**Updated:** 2026-06-23
**Status:** retained as a short continuity snapshot. The active master plan is now
[`refactor-removal-roadmap.md`](./refactor-removal-roadmap.md).

## Snapshot

- workflow inventory closure: `100.0%` (`963/963`)
- DAO SPI deletion: `100.0%` (`878/878`)
- `shared/dao`: `0` files
- `web/`: deleted
- `ws/`: absent
- remaining `shared/`: `13` Java files, all under `shared/bean`
- shared support/domain/DAO Java packages: `0` files
- code balance by file count: `5%` shared legacy / `95%` module modern

## Active Next Step

The next stage is no longer workflow deletion. It is compatibility strangulation inside `app/` and `shared/`, especially:

1. DTO/term bean contraction at retained compatibility edges
2. data-import compatibility model migration
3. form validation/discrepancy compatibility isolation
4. module adapter DTO contraction
5. guard shared support/domain/DAO packages against reintroduction

See [`refactor-removal-roadmap.md`](./refactor-removal-roadmap.md) for sequencing, exit gates, and verification.
