# Refactor Docs Index

**Updated:** 2026-06-26

This directory contains historical refactor plans plus supporting baseline, ledger, and slice documents.
Current source snapshot: workflow inventory closed, DAO SPI ledger fully removed, `web/` and `ws/` absent, `shared/src/main/java` reduced to `0` Java files, platform upgraded to Spring Boot 3.5.2 / Modulith 1.4.1, and dependency drift converged.

**Active plan:** `phase-13-release-stabilization-and-readiness-plan.md` - full-suite stabilization, Java 21 toolchain reproducibility, generated-contract workflow, legacy-removal aftercare, and release candidate readiness.

## Primary Entry Points

| Document | Role | Status |
|---|---|---|
| `refactor-removal-roadmap.md` | Historical baseline and verification record for completed legacy refactor/removal work | Baseline / reference |
| `remove-legacy-code-plan.md` | Historical baseline, completed phases, and deletion evidence | Baseline / reference |
| `legacy-workflow-inventory.{md,csv}` | Generated inventory showing the workflow-level legacy surface | Generated evidence |
| `phase-3-dao-replacement-ledger.{md,csv}` | Final DAO SPI deletion ledger | Generated evidence |
| `phase-1-email-field-removal-plan.md` | Historical follow-up slice for compatibility email/contact field cleanup | Complete / historical |
| `phase-1-email-contract-versioning-plan.md` | Detailed E3 execution plan for versioned ODM contract cleanup | Complete / historical |
| `post-refactor-product-hardening-plan.md` | Historical plan for the first post-refactor hardening phase | Complete / historical |
| `post-hardening-stabilization-plan.md` | Historical record for the completed stabilization phase | Complete / historical |
| `phase-4-export-productization-plan.md` | Export productization, broader verification, and Java 26 tooling follow-up | Complete / historical |
| `phase-5-platform-upgrade-plan.md` | Platform/toolchain upgrade: Spring Boot 3.5.2, Modulith 1.4.1, ArchUnit 1.4.1 | Complete / historical |
| `phase-6-post-upgrade-baseline-hygiene-plan.md` | Post-upgrade baseline hygiene: dependency convergence, verification, doc re-baselining, product candidate selection | Complete / historical |
| `phase-7-export-operations-polish-plan.md` | Export operations polish: filtering, failure info, retry, download UX, frontend polish | Complete / historical |
| `phase-8-event-workflow-context-hardening-plan.md` | Event workflow context hardening and legacy event adapter retirement | Complete / historical |
| `phase-9-technical-debt-burndown-and-architecture-prep-plan.md` | Technical debt burndown and architecture preparation | Complete / historical |
| `phase-10-api-contract-and-compatibility-retirement-readiness-plan.md` | API contract readiness, typed API pilot, raw fetch cleanup, and legacy compatibility ledger | Complete / historical |
| `phase-11-contract-correctness-and-architecture-prep-plan.md` | Contract correctness, placeholder identity cleanup, guardrail tightening, and architecture readiness | Complete / historical |
| `phase-12-openapi-and-architecture-transition-plan.md` | OpenAPI type generation, LegacyCrfAdapter replacement planning, compatibility retirement, and bounded architecture transition | Complete / historical |
| `phase-13-release-stabilization-and-readiness-plan.md` | Full-suite stabilization, Java 21 toolchain reproducibility, generated-contract workflow, legacy-removal aftercare, and release candidate readiness | Active |

## Historical Phase Documents

These documents remain valuable as slice history, but they are completed or historical - not the current execution plan:

| Document Pattern | Role |
|---|---|
| `phase-1-*-slice.md` | Slice-level analysis, deletion sequencing, and historical decisions |
| `phase-1-*-ledger.{md,csv}` | Per-slice inventory / closure evidence |
| `phase-1-spring-mvc-classification.md` | Historical route classification |
| `next-refactor-removal-plan.md` | Short status snapshot retained for continuity; superseded by the roadmap |

## Document Maintenance Rules

1. Create a new focused plan for new active work; keep completed roadmap files historical unless a regression explicitly reopens them.
2. Keep generated files generated; do not hand-edit inventories or ledgers unless the generation process changes.
3. Treat slice documents as historical unless they are explicitly re-opened by a new plan.
4. Add new focused follow-up plans only when a workstream needs separate execution detail.
