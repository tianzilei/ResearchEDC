# Refactor Docs Index

**Updated:** 2026-06-19

This directory now has a single active roadmap plus supporting baseline, ledger, and slice documents.

## Primary Entry Points

| Document | Role | Status |
|---|---|---|
| `refactor-removal-roadmap.md` | **Primary source of truth** for current legacy refactor/removal work | Active |
| `remove-legacy-code-plan.md` | Historical baseline, completed phases, and deletion evidence | Baseline / reference |
| `legacy-workflow-inventory.{md,csv}` | Generated inventory showing the workflow-level legacy surface | Generated evidence |
| `phase-3-dao-replacement-ledger.{md,csv}` | Final DAO SPI deletion ledger | Generated evidence |
| `phase-1-email-field-removal-plan.md` | Active follow-up slice for compatibility email/contact field cleanup | Active follow-up |

## Historical Phase Documents

These documents remain valuable as slice history, but they are no longer the active master plan:

| Document Pattern | Role |
|---|---|
| `phase-1-*-slice.md` | Slice-level analysis, deletion sequencing, and historical decisions |
| `phase-1-*-ledger.{md,csv}` | Per-slice inventory / closure evidence |
| `phase-1-spring-mvc-classification.md` | Historical route classification |
| `next-refactor-removal-plan.md` | Short status snapshot retained for continuity; superseded by the roadmap |

## Document Maintenance Rules

1. Update `refactor-removal-roadmap.md` for all new active legacy-refactor work.
2. Keep generated files generated; do not hand-edit inventories or ledgers unless the generation process changes.
3. Treat slice documents as historical unless they are explicitly re-opened by the roadmap.
4. Add new focused follow-up plans only when a workstream needs separate execution detail.
