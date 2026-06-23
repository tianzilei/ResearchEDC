# OpenClinica Legacy Code Refactoring Plan

> **Updated:** 2026-06-23
> **Role:** handoff pointer retained for continuity.

## Current Source Of Truth

- Active roadmap: `docs/refactor/refactor-removal-roadmap.md`
- Historical baseline and completed-phase evidence: `docs/refactor/remove-legacy-code-plan.md`
- Generated workflow evidence: `docs/refactor/legacy-workflow-inventory.{md,csv}`
- Generated DAO evidence: `docs/refactor/phase-3-dao-replacement-ledger.{md,csv}`

## Snapshot

- workflow inventory closure: `963/963` (`100.0%`)
- DAO SPI deletion: `878/878` removed (`100.0%`)
- `shared/dao`: empty
- `web/`: deleted
- `ws/`: absent
- remaining `shared/`: `13` Java files, all under `shared/bean`
- shared support/domain/DAO Java packages: `0` files

## Active Direction

The remaining effort is compatibility strangulation inside `app/` and `shared/`, not additional
workflow inventory cleanup. Current execution estimate is roughly 5 focused slices to retire the
remaining shared DTO/term bean surface.

Use `docs/refactor/refactor-removal-roadmap.md` for:

1. DTO/term bean contraction at retained compatibility edges
2. data-import compatibility migration
3. form validation/discrepancy isolation
4. module adapter DTO contraction
5. guard shared support/domain/DAO packages against reintroduction
