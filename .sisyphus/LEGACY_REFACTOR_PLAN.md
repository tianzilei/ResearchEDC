# OpenClinica Legacy Code Refactoring Plan

> **Updated:** 2026-06-19
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
- remaining `shared/`: `194` Java files

## Active Direction

The remaining effort is compatibility strangulation inside `app/` and `shared/`, not additional
workflow inventory cleanup.

Use `docs/refactor/refactor-removal-roadmap.md` for:

1. shared support extraction
2. data-import compatibility migration
3. form validation/discrepancy isolation
4. module adapter DTO contraction
5. gradual `shared/domain` reduction
