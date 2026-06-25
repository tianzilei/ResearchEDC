# Phase 10 API Contract And Compatibility Retirement Readiness Plan

**Created:** 2026-06-25
**Status:** Complete (2026-06-25)
**Purpose:** prepare the codebase for larger architecture changes by tightening frontend API contracts, reducing raw `fetch` usage, and defining the remaining compatibility-retirement path.
**Predecessor:** `docs/refactor/phase-9-technical-debt-burndown-and-architecture-prep-plan.md`

## Current Context

Phase 8 and Phase 9 are complete.

Current baseline:

- event workflows no longer use `studyId=0`
- `LegacyEventAdapter` is deleted
- `LegacyFrame` and `/app/legacy/*` route are deleted
- major frontend `/api/legacy/*` callers have been migrated to `/api/v1/*`
- module-native APIs now exist for datasets, filters, subject groups, discrepancy notes, rules, and CRF admin
- SpringDoc is configured for backend OpenAPI at `/api-docs`
- `frontend/src/api/generated.ts` is still a placeholder
- frontend still has raw `fetch` usage across multiple pages

## Phase 10 Goal

Create a practical API-contract foundation before deeper architecture work:

1. choose the API contract strategy
2. prove typed frontend API contracts on one stable domain
3. reduce raw `fetch` in one feature slice
4. document remaining legacy compatibility endpoints and their retirement gates
5. add guardrail checks that prevent regression to retired patterns

## Non-Goals

- Do not migrate every frontend API call in one pass.
- Do not remove all `module/legacy` controllers in one pass.
- Do not require OpenAPI generation to land before a typed pilot can begin.
- Do not redesign frontend routing or feature folders yet.
- Do not change backend persistence or released migrations.

## Workstreams

### Phase 10A: API Contract Strategy

**Decision:** use a two-step strategy.

1. **Immediate:** introduce centrally typed feature API modules for stable domains.
2. **Next:** adopt `openapi-typescript` once the backend OpenAPI spec and frontend dependency flow are ready.

Rationale:

- SpringDoc is already present.
- `openapi-typescript` is the preferred long-term type-generation path.
- Installing new tooling may require network access, so the first slice should not block on dependency download.
- A manually typed feature API module still reduces DTO drift and raw `fetch` immediately.

Preferred OpenAPI generation target:

```text
frontend/src/api/generated.ts
```

Preferred future command:

```bash
pnpm openapi:types
```

### Phase 10B: Typed API Pilot

**Pilot domain:** events.

Why events:

- Phase 8 recently hardened event study context.
- `useEvents.ts` already centralizes event data fetching.
- event DTOs are relatively small and stable.
- event workflows are study-scoped, which exercises the new context contract.

Implementation target:

- add a typed event API module under `frontend/src/api/`
- keep endpoint strings out of page components
- keep hooks in `frontend/src/hooks/useEvents.ts`
- use existing `apiClient`

Exit gate:

- event hooks call a typed API module rather than raw endpoint strings directly
- `EventList.tsx` remains free of direct event fetch calls
- frontend typecheck passes

### Phase 10C: Raw Fetch Reduction

First slice:

- event workflow API calls

Next candidates:

1. studies
2. subjects
3. admin identity
4. data capture attachments
5. auth, if the current raw fetch logic can be wrapped without weakening CSRF/session behavior

Allowed raw `fetch` exceptions:

- file download/blob handling
- FormData upload
- actuator/diagnostic endpoints such as `/actuator/loggers`
- low-level `apiClient`
- auth bootstrap/login/logout until wrapped deliberately

Exit gate:

- one feature area has no page-level raw fetch for ordinary JSON API calls
- exceptions are documented

### Phase 10D: Legacy Compatibility Retirement Readiness

Build a retention/removal ledger for `module/legacy`.

For each controller:

- endpoint base path
- current frontend callers
- external compatibility assumption
- module-native replacement
- retention classification
- deletion gate

Classifications:

- `external-compat-retain`
- `deprecated`
- `ready-to-remove`

Initial known state:

- frontend `/api/legacy/*` callers are expected to be zero after Phase 9
- legacy contract tests still exercise the bridge
- removal must be a deliberate compatibility decision, not a side effect of frontend migration

Exit gate:

- ledger exists
- each legacy controller has a classification
- no controller is deleted without a deletion gate

### Phase 10E: Guardrails

Guardrails should check:

- `shared/src/main/java` remains empty
- no frontend `/api/legacy/*` callers
- no `studyId=0` placeholders in active frontend code
- no new page-level raw `fetch` for ordinary JSON APIs

First implementation:

- add a script under `scripts/ci/`
- document it in this plan and the refactor README
- wire it into `daily-gauntlet.sh` only after the standalone script is stable

Exit gate:

- guardrail command exists
- guardrail passes on the current tree
- future integration point is documented

## Recommended Delivery Order

1. Add this Phase 10 plan.
2. Add typed event API module.
3. Update `useEvents.ts` to use the typed module.
4. Add legacy compatibility retirement ledger.
5. Add guardrail script.
6. Run frontend typecheck/lint and guardrail script.
7. Update completion record.

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Manual typed API modules duplicate future generated types | medium | keep the pilot small and align names with expected OpenAPI DTOs |
| Guardrails are too strict for legitimate exceptions | medium | allow documented exceptions for downloads, uploads, auth, and `apiClient` |
| Legacy controller removal is attempted too soon | high | classify endpoints first; deletion requires compatibility decision |
| OpenAPI generation produces noisy type names | medium | pilot manually first, then evaluate generated output before broad adoption |

## Success Criteria

1. Phase 10 plan exists and identifies the contract strategy.
2. event API calls go through a typed frontend API module.
3. no event page-level raw fetch is introduced.
4. legacy compatibility retirement ledger exists.
5. guardrail script exists and passes.
6. frontend typecheck and lint pass.
7. completion record documents delivered behavior and deferred follow-up.

## Immediate Next Action

Create the event typed API module and migrate `frontend/src/hooks/useEvents.ts` to call it.

## Completion Record (2026-06-25)

### Phase 10A: API Contract Strategy - COMPLETE

Decision:

- use centrally typed feature API modules immediately
- adopt `openapi-typescript` later when generated spec workflow is ready
- keep generated output target as `frontend/src/api/generated.ts`

Rationale:

- SpringDoc is already configured
- adding OpenAPI tooling may require dependency/network work
- a typed feature API module reduces drift immediately without blocking on tooling

### Phase 10B: Typed API Pilot - COMPLETE

Pilot domain: events.

Created:

- `frontend/src/api/events.ts`

Changed:

- `frontend/src/hooks/useEvents.ts` now calls `eventApi`
- event endpoint strings are centralized in the API module
- hooks still own TanStack Query keys and invalidation behavior

### Phase 10C: Raw Fetch Reduction - STARTED

Event workflow ordinary JSON calls now go through the typed event API module.

Additional raw-fetch reduction completed after the initial Phase 10 pilot:

- study pages now use `studyApi`
- subject pages now use `subjectApi`
- feature flags now use `studyApi`
- audit viewer now uses `auditApi`
- CRF admin now uses `crfManageApi`
- export job manager now uses `exportApi`
- identity admin pages now use `identityApi`

Deferred raw-fetch debt is now limited to documented exceptions:

- file download/blob handling
- FormData upload
- actuator diagnostics (`/actuator/loggers`)
- auth bootstrap/login/logout until wrapped deliberately

### Phase 10D: Legacy Compatibility Retirement Readiness - COMPLETE

Created:

- `docs/refactor/phase-10-legacy-compatibility-retirement-ledger.md`

Current classification:

- frontend `/api/legacy/*` callers are zero
- `module/legacy` controllers are retained only as compatibility endpoints
- most legacy controllers are classified as `deprecated`
- study/subject compatibility endpoints are retained last because they are more likely external integration surfaces

### Phase 10E: Guardrails - COMPLETE

Created:

- `scripts/ci/check-architecture-guardrails.sh`

Current checks:

- `shared/src/main/java` remains empty
- frontend does not call `/api/legacy/*`
- frontend `studyId=0`-style fallbacks are reported as warnings
- page/component/hooks raw fetch usage is reported as a warning

The warning behavior is intentional for the first guardrail slice because existing raw fetch and `currentStudy?.id ?? 0` patterns remain as classified technical debt.

### Verification

| Gate | Result |
|---|---|
| `pnpm -C frontend typecheck` | 0 errors |
| `pnpm -C frontend lint` | 0 errors |
| `bash scripts/ci/check-architecture-guardrails.sh` | pass, 0 warning groups after follow-up raw-fetch cleanup |

### Deferred Follow-Up

1. Replace `frontend/src/api/generated.ts` with generated OpenAPI types.
2. Add an `openapi:types` frontend script after choosing/installing the generator.
3. Replace remaining allowed raw `fetch` exceptions only when there is a clear API-layer benefit.
4. Decide whether/when to retire each `module/legacy` controller from the compatibility ledger.
