# Phase 11F: Architecture Adjustment Readiness Review

**Created:** 2026-06-25
**Status:** Complete
**Purpose:** assess readiness for larger architecture changes after Phase 11 debt reduction.

## Review Topics

### 1. OpenAPI Type Generation

**Current state:** `frontend/src/api/generated.ts` is a placeholder. Backend has SpringDoc 2.5.0 configured.

**Recommendation:** Start OpenAPI generation before the next product feature.

**Rationale:**
- All module-native APIs now have consistent DTOs
- Event DTO drift has been corrected (Phase 11A)
- Randomization identity placeholders removed (Phase 11B)
- Generated types will prevent future drift

**Prerequisite:** Run OpenAPI generation against the current backend spec to validate type quality.

### 2. LegacyCrfAdapter

**Current state:** `CrfService` depends on `LegacyCrfAdapter` for `listCrfs()` and `getVersion()` methods.

**Recommendation:** Evaluate separately from endpoint compatibility removal.

**Rationale:**
- `LegacyCrfAdapter` provides read-only data access to legacy CRF tables
- It's an implementation bridge, not an API compatibility layer
- Removing it requires creating module-owned repository queries for CRF data
- This is a larger change that should be planned as a separate phase

**Next step:** Create a focused Phase 12 plan for LegacyCrfAdapter replacement.

### 3. Module/Legacy Retirement

**Current state:** 8 legacy controllers remain (study, subject, CRF, subject group, discrepancy note, rule, rule set, import).

**Recommendation:** Delete in multiple compatibility slices.

**Rationale:**
- Phase 11E retired 2 controllers (dataset, filter) successfully
- Remaining controllers follow the same pattern
- Each slice can be independently verified and rolled back
- Recommended order: subject group → discrepancy note → rule → CRF → import → study/subject

### 4. Frontend Feature Architecture

**Current state:** Frontend has typed API modules for events, exports, subjects, studies, identity, audit, CRF manage, and datasets/filters.

**Recommendation:** Reorganize around typed API/domain boundaries after OpenAPI generation.

**Rationale:**
- Current structure is functional but inconsistent
- Some pages still use page-local apiClient calls
- OpenAPI generation will provide a natural boundary for reorganization
- Defer reorganization until generated types are available

### 5. Backend Module Boundaries

**Current state:** Modules use `@ApplicationModule` and `ModulithVerificationTest` enforces no circular dependencies.

**Recommendation:** Strengthen package-level boundaries before new feature work.

**Rationale:**
- Current module structure is sound
- Anti-corruption layer pattern (internal/adapter/) is working well
- Consider adding package-info.java with `@NonNullApi` for null safety
- Consider adding explicit module dependency documentation

## Readiness Decision

**The codebase is ready for the next architecture phase.**

Completed prerequisites:
- ✅ Event DTO contract drift resolved
- ✅ Unsafe casts removed
- ✅ Randomization identity placeholders removed
- ✅ Guardrails prevent regression
- ✅ First legacy compatibility slice retired
- ✅ Typed API modules established for key domains

Recommended next phase:
1. OpenAPI type generation (Phase 12A)
2. LegacyCrfAdapter replacement (Phase 12B)
3. Remaining legacy controller retirement (Phase 12C)
4. Frontend feature architecture reorganization (Phase 12D)

## Verification

- `pnpm typecheck` — 0 errors
- `pnpm lint` — 0 errors
- `pnpm test --run` — 25/25 pass
- `mvn compile` — BUILD SUCCESS
- `ModulithVerificationTest` — 1/1 pass
