# Final Refactor Summary

**Updated:** 2026-06-27  
**Status:** Complete / historical

This is the short version of the refactor/removal story.

Use this file when you need the outcome, not the full phase-by-phase evidence.
Keep the rest of `docs/refactor/` as archive material for audits, archaeology,
or proof of how the transition was executed.

## Final Outcome

The large refactor/removal program is complete.

What changed:

- `web/` legacy UI module was removed
- `ws/` SOAP module is absent from the current tree
- `shared/src/main/java` was reduced to `0` Java files
- shared DTOs, shared DAO SPI, shared domain mappings, and the old
  `LegacyDaoFactory` / `EntityDAO` infrastructure were deleted
- Spring Boot modular code now owns the active runtime behavior
- React SPA and module-native REST APIs are the main user-facing path
- ODM contract versioning was completed for OC2-0 compatibility and OC2-1
  email-free output

## Current Repository Shape

- `app/`: active Spring Boot modular monolith
- `frontend/`: active React SPA
- `questionnaire-service/`: active Python FastAPI service
- `shared/`: resources only
- `docs/edc-convergence/`: next planning area after refactor closure

## Completion Signals

- Active legacy workflow inventory: `0`
- Phase 3 DAO ledger: `878/878 removed`
- `shared/src/main/java`: `0` Java files
- `web/`: absent
- `ws/`: absent
- Phase 18 post-cleanup verification: complete

## What To Read Next

- [README.md](../../README.md) - current repository entry point
- [docs/edc-convergence/README.md](../edc-convergence/README.md) - next active
  planning area
- [phase-18-post-cleanup-verification-baseline-plan.md](./phase-18-post-cleanup-verification-baseline-plan.md) -
  final verification closure

## Archive Map

Use the heavier documents only when you need detail:

- [README.md](./README.md) - refactor docs index
- [refactor-removal-roadmap.md](./refactor-removal-roadmap.md) - high-level
  historical roadmap and completion evidence
- [remove-legacy-code-plan.md](./remove-legacy-code-plan.md) - detailed
  removal-plan baseline
- `phase-*.md` - historical execution plans
- `*-ledger.*`, `legacy-workflow-inventory.*` - generated evidence
