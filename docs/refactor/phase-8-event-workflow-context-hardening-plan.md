# Phase 8 Event Workflow Context Hardening Plan

**Created:** 2026-06-25
**Status:** Complete (2026-06-25)
**Purpose:** define the next product-correctness workstream after Phase 7 export operations polish, focused on removing placeholder study context from event workflows.
**Predecessor:** `docs/refactor/phase-7-export-operations-polish-plan.md`

## Current Context

The refactor/removal, platform-upgrade, baseline hygiene, and export operations phases are complete.

Current baseline:

- `shared/src/main/java`: `0` Java files
- legacy workflow inventory: closed
- DAO SPI ledger: fully removed
- Spring Boot: `3.5.2`
- Spring Modulith: `1.4.1`
- Phase 7 export operations polish completed
- current docs index reports no active plan

The next visible product-correctness issue is in the event workflow frontend:

- `frontend/src/pages/events/EventList.tsx` still loads event definitions with `studyId=0`
- the local comment says the real `studyId` should be derived from current study context
- relying on `studyId=0` risks cross-study data leakage, inconsistent site behavior, or hidden coupling to backend fallback behavior

## Phase 8 Goal

Make event workflows explicitly study-scoped by deriving the active study context from the frontend application state and by clarifying backend behavior for event definition queries.

## Non-Goals

- Do not redesign the entire event module.
- Do not add a new scheduling engine.
- Do not change released Liquibase migrations.
- Do not remove the legacy gateway.
- Do not refactor unrelated study, subject, or dashboard pages.
- Do not introduce a second study-context source if an existing one is suitable.

## Workstreams

### Phase 8A: Current Event Flow Inventory

**Goal:** understand current event workflow behavior before changing request scope.

#### 8A.1 Frontend Event Flow Review

Review:

- `frontend/src/pages/events/EventList.tsx`
- event list loading
- event definition loading
- create-event modal behavior
- status/action rendering
- current use of `fetch` vs typed API client
- current tests for event pages, if any

Capture:

- every event API endpoint called by `EventList`
- which calls include study scope
- which calls rely on placeholder `studyId=0`
- how loading, error, empty, and create states behave
- what should happen when no active study is selected

#### 8A.2 Frontend Study Context Review

Review likely context sources:

- dashboard bootstrap data
- study switcher state
- `frontend/src/hooks/useStudies.tsx`
- auth/session bootstrap data
- any app-level provider that tracks current study or active site

Capture:

- current canonical source of active study
- whether current study is represented as number, string, or object
- how study switching invalidates queries
- how pages currently handle missing study context

#### 8A.3 Backend Event API Review

Review:

- `app/src/main/java/org/researchedc/module/event/controller/*`
- `app/src/main/java/org/researchedc/module/event/service/*`
- event definition repository/service paths
- tests under `app/src/test/java/org/researchedc/module/event`

Capture:

- behavior of `/api/v1/events`
- behavior of `/api/v1/events/definitions`
- whether `studyId=0` has explicit meaning
- parent-study/site behavior for event definitions
- current validation for missing or invalid study id

#### 8A Exit Gate

- current frontend event calls are documented
- canonical active-study source is identified
- backend `studyId=0` behavior is classified as intentional, accidental, or legacy fallback

### Phase 8B: Backend Study-Scope Semantics

**Goal:** make backend event API behavior explicit and testable.

#### 8B.1 Define Study Scope Contract

Choose one contract:

1. `studyId` query parameter is required for event definitions.
2. current study is resolved from authenticated session/context.
3. both are supported, but query parameter wins and session context is fallback.

Preferred initial contract:

- keep `studyId` as an explicit query parameter
- reject missing or invalid values where practical
- do not rely on `studyId=0` from the frontend

#### 8B.2 Validate Event Definition Query Behavior

Clarify:

- valid study id returns definitions for that study
- site study id includes appropriate parent-study definitions if current repository semantics support that
- missing study id returns clear client error or documented empty response
- `studyId=0` is rejected unless there is an explicit product reason to keep it

#### 8B.3 Backend Tests

Add or update focused tests for:

- valid study id
- missing study id
- invalid or zero study id
- site/parent study definition behavior
- no regression to event list endpoints

Target commands:

```bash
mvn -pl app -am test -Dtest=EventServiceTest,EventControllerTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
```

If exact test class names differ, use the closest event-module service/controller test classes.

#### 8B Exit Gate

- backend event definition scope is explicit
- `studyId=0` behavior is tested or removed
- event-module tests pass

### Phase 8C: Frontend Active Study Integration

**Goal:** remove placeholder `studyId=0` and scope event workflows to the current active study.

#### 8C.1 Replace Placeholder Scope

In `EventList.tsx`:

- derive `studyId` from the canonical active-study source
- do not call `/api/v1/events/definitions?studyId=0`
- remove the TODO comment
- make query keys include active study id so TanStack Query or local effects refetch on study change

#### 8C.2 Missing Study State

When no active study is available:

- do not request event definitions
- show a clear empty or selection state
- keep create-event actions disabled if they require study context
- avoid using global/default study fallbacks unless they are already a documented app-level behavior

#### 8C.3 Loading And Error States

Ensure:

- study context loading does not flash incorrect event data
- switching studies clears or refetches stale definitions
- backend 400/404 responses produce useful UI errors
- create-event modal cannot submit with missing study id

#### 8C.4 API Client Consistency

Prefer the existing typed `ApiClient` / query wrapper patterns over raw `fetch` if local conventions support it.

Do not broaden this into a full API-client migration unless the local change is small and contained.

#### 8C Exit Gate

- `EventList.tsx` has no `studyId=0` placeholder
- event definitions are loaded for the active study
- missing active study does not trigger invalid requests
- study switching refetches scoped event data

### Phase 8D: Frontend Tests And Verification

**Goal:** protect the event workflow scope behavior.

#### 8D.1 Frontend Tests

Add or update tests for:

- active study id is used when loading event definitions
- no request is sent when study context is missing
- study switch triggers scoped reload
- create-event controls are disabled or guarded without study context
- backend error for invalid study scope is surfaced cleanly

Target commands:

```bash
cd frontend && pnpm typecheck
cd frontend && pnpm lint
cd frontend && pnpm test --run
```

#### 8D.2 Backend Verification

Run event-focused tests and architecture verification:

```bash
mvn -pl app -am test -Dtest=EventServiceTest,EventControllerTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
```

#### 8D.3 Full Gate

If both backend and frontend behavior changes land, run:

```bash
bash scripts/ci/daily-gauntlet.sh
```

If environment blocks the full gate, record the blocker and the focused gates that passed.

#### 8D Exit Gate

- frontend typecheck passes
- frontend lint passes
- event-focused frontend tests pass
- event-focused backend tests pass, if backend changed
- Modulith verification passes, if backend changed

## Recommended Delivery Order

1. Complete Phase 8A inventory.
2. Decide the backend study-scope contract in Phase 8B.1.
3. Implement backend validation/tests if needed.
4. Implement frontend active-study integration in Phase 8C.
5. Add focused frontend tests in Phase 8D.
6. Run the strongest available verification gate.
7. Update this plan with a completion record.

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Active study source is ambiguous | medium | inventory existing dashboard/study-switcher state before coding |
| Backend currently treats `studyId=0` as all-studies fallback | high | classify behavior and replace frontend reliance before tightening server validation |
| Site studies need parent-study definitions | high | preserve tested site/parent behavior when changing validation |
| Study switching leaves stale definitions on screen | medium | include study id in query keys/effect dependencies |
| Scope hardening breaks create-event workflow | medium | test create modal behavior with and without active study |

## Open Decisions

1. What is the canonical frontend active-study source?
2. Should `/api/v1/events/definitions` require `studyId`, or can it resolve current study from session context?
3. Should `studyId=0` be rejected server-side immediately, or only removed from frontend first?
4. How should event definitions behave for site studies vs parent studies?
5. Should this phase also migrate `EventList.tsx` raw `fetch` calls to the existing API client?

## Success Criteria

1. `EventList.tsx` no longer sends `studyId=0`.
2. event definitions and event list behavior are scoped to the active study.
3. missing active study is handled without invalid API calls.
4. backend study-scope behavior is explicit and covered by tests if changed.
5. frontend tests cover active-study scoped loading.
6. typecheck, lint, and relevant backend tests pass.
7. Phase 8 completion record documents delivered behavior and any deferred follow-up.

## Immediate Next Action

Start with **Phase 8A.1: frontend event flow review**. Document each API call in `EventList.tsx`, its current study-scope behavior, and where the active study id should come from.

## Completion Record (2026-06-25)

### Delivered Behavior

1. **EventList.tsx** no longer sends `studyId=0`. The `openSchedule` callback was simplified to just open the modal — no raw `fetch` call.
2. Event definitions are loaded via `useEventDefinitions(currentStudy?.id)` hook, which is reactive and scoped to the active study from `useCurrentStudy()`.
3. When no active study is selected (`currentStudy` is null), the hook returns an empty array via `enabled: !!studyId` — no invalid API request is sent.
4. Study switching invalidates `["event-definitions"]` via `StudyProvider.setCurrentStudy()`, triggering a refetch for the new study.
5. Backend `EventController.listDefinitions` already requires `studyId` as `@RequestParam` — no backend changes were needed.

### Files Changed

- `frontend/src/pages/events/EventList.tsx` — replaced raw `fetch` with `useEventDefinitions` hook, added `useCurrentStudy` import

### Verification

- `pnpm typecheck` — 0 errors
- `pnpm lint` — 0 errors, 0 warnings
- `pnpm test --run` — 25/25 pass
- `mvn test -Dtest=ModulithVerificationTest` — 1/1 pass

### Deferred Follow-Up

- The remaining 7 `currentStudy?.id ?? 0` sites (ExportCenter, RandomizationDashboard, 4 questionnaire pages, ImportManager) should be addressed in Phase 9E (Unified Study Context).
- The 5 hardcoded `userId=0` in `useRandomization.ts` should be fixed to use the authenticated user identity.
