# Next Refactor And Removal Plan

**Updated:** 2026-06-24
**Status:** historical continuity snapshot. This file is superseded by
[`refactor-removal-roadmap.md`](./refactor-removal-roadmap.md) for the completed refactor baseline and
[`phase-5-platform-upgrade-plan.md`](./phase-5-platform-upgrade-plan.md) for the completed platform upgrade.

## Snapshot

- workflow inventory closure: `100.0%` (`963/963`)
- DAO SPI deletion: `100.0%` (`878/878`)
- `shared/dao`: `0` files
- `web/`: deleted
- `ws/`: absent
- remaining `shared/`: `0` Java files
- shared support/domain/DAO Java packages: `0` files
- code balance by file count: `0%` shared legacy / `100%` module modern

## Current Posture

There is no active legacy-refactor next step. Workflow deletion, DAO SPI deletion, shared Java retirement, export productization, and the Phase 5 platform upgrade are complete.

Open new work as a focused plan only when there is a concrete post-upgrade target, such as dependency convergence, verification hardening, or product feature delivery.
