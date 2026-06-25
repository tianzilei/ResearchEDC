# Phase 11 Contract Correctness And Architecture Prep Plan

**Created:** 2026-06-25
**Status:** Complete (2026-06-25)
**Purpose:** clear the remaining API contract, placeholder identity, and compatibility-retirement debt before broader feature, architecture, and logic changes.
**Predecessor:** `docs/refactor/phase-10-api-contract-and-compatibility-retirement-readiness-plan.md`

## Current Context

Phase 10 is complete.

Current baseline:

- working tree is clean
- architecture guardrails pass with `0 warning group(s)`
- frontend `/api/legacy/*` callers are zero
- `studyId=0` style frontend fallbacks are cleared
- ordinary business JSON raw `fetch` calls have been moved behind typed API modules for the Phase 10 domains
- remaining direct `fetch` usage is limited to documented exceptions: auth, low-level `apiClient`, uploads, downloads, attachments, and actuator diagnostics
- `docs/refactor/README.md` previously had no active plan
- `frontend/src/api/generated.ts` is still a placeholder for future OpenAPI-generated types
- `module/legacy` controllers remain as compatibility endpoints per `docs/refactor/phase-10-legacy-compatibility-retirement-ledger.md`

Known remaining debt:

- `frontend/src/hooks/useRandomization.ts` still sends `userId=0` in create, update, activate, close, and randomize calls
- `frontend/src/pages/subject/SubjectDetail.tsx` still has an unsafe `as Promise<unknown> as Promise<StudyEvent[]>` cast
- frontend event types do not cleanly match backend event DTO names and shapes
- several business domains still use hand-written typed API modules instead of generated OpenAPI types
- `module/legacy` compatibility endpoints need a deliberate retirement sequence
- `app/src/main/java/org/researchedc/module/crf/service/CrfService.java` still depends on `LegacyCrfAdapter`; this should be evaluated separately from endpoint compatibility removal

## Phase 11 Goal

Make the codebase ready for larger architecture changes by removing the remaining high-signal contract and placeholder debt.

Target outcomes:

1. frontend event DTO types match the backend contract
2. unsafe event DTO casts are removed
3. randomization no longer sends placeholder `userId=0`
4. guardrails prevent reintroducing placeholder identity and unsafe API contract workarounds
5. remaining typed API gaps are inventoried and reduced by small domain slices
6. the first safe `module/legacy` compatibility retirement slice is selected and executed only after contract proof

## Non-Goals

- Do not perform a broad frontend rewrite.
- Do not remove every `module/legacy` controller in one pass.
- Do not redesign persistence or module ownership in this phase.
- Do not modify released Liquibase migrations.
- Do not require OpenAPI generation before fixing known DTO drift.
- Do not replace uploads, downloads, auth bootstrap, or actuator diagnostics merely to eliminate raw `fetch`.

## Workstreams

### Phase 11A: Event DTO Contract Correction

**Problem:** frontend event types and backend event DTOs have drifted.

Observed evidence:

- backend event DTO uses fields such as `studyEventId`, `dateStart`, and `dateEnd`
- frontend event code expects fields such as `id`, `dateStarted`, and `dateEnded`
- `SubjectDetail.tsx` currently forces the return type through `Promise<unknown>`

Execution:

1. inspect `StudyEventDTO.java`, `frontend/src/types/event.ts`, `frontend/src/api/events.ts`, `frontend/src/hooks/useEvents.ts`, `EventList.tsx`, and `SubjectDetail.tsx`
2. align frontend DTO names with the backend response contract
3. add a UI-facing mapper only if the display model genuinely differs from the transport DTO
4. remove the unsafe `Promise<unknown>` cast
5. keep endpoint strings inside the typed event API module

Exit gate:

- no `as Promise<unknown>` cast remains for event data
- event list and subject detail use typed event API data without contract coercion
- `pnpm -C frontend typecheck` passes
- `pnpm -C frontend lint` passes

### Phase 11B: Randomization Identity Placeholder Removal

**Problem:** randomization mutations still send `userId=0`.

Observed evidence:

- `schemes?userId=0`
- `schemes/{id}?userId=0`
- `schemes/{id}/activate?userId=0`
- `schemes/{id}/close?userId=0`
- `randomize?userId=0`

Preferred direction:

- backend should derive the acting user from the authenticated session or security context
- frontend should not invent a user id

Execution:

1. inspect the randomization controller and service signatures
2. decide whether `userId` can become optional or be removed from request parameters
3. preserve compatibility if external clients still send `userId`
4. update the frontend hook to stop sending placeholder identity
5. update or add focused tests around current-user handling if backend logic changes

Exit gate:

- no active frontend source contains `userId=0`
- randomization create, update, activate, close, and randomize flows still typecheck
- backend verification runs if controller or service logic changes

### Phase 11C: Typed API Gap Reduction

**Problem:** Phase 10 established typed API modules, but not every ordinary JSON API domain is fully centralized.

Execution:

1. inventory remaining page-local `apiClient` usage by domain
2. keep documented exceptions out of scope: auth, uploads, downloads, attachments, actuator diagnostics, and low-level `apiClient`
3. move one small business domain at a time into `frontend/src/api/*`
4. keep hooks responsible for cache/query orchestration, not endpoint string assembly

Candidate domains:

- datasets
- filters
- rules
- questionnaire/profile surfaces, if they are ordinary JSON API calls
- remaining admin pages with repeated endpoint construction

Exit gate:

- each migrated domain has a typed API module
- page components do not assemble ordinary business endpoint strings for that domain
- frontend typecheck and lint pass

### Phase 11D: Guardrail Tightening

**Problem:** Phase 10 guardrails catch broad regressions, but the newly identified debt needs explicit protection after it is fixed.

Add checks for:

- active frontend `userId=0`
- unsafe event contract casts such as `as Promise<unknown>` in event workflows
- new frontend `/api/legacy/*` calls
- new `studyId=0` fallbacks

Keep allowed exceptions documented for:

- auth provider raw `fetch`
- low-level `apiClient`
- uploads and downloads
- attachment binary and form-data flows
- actuator diagnostics

Exit gate:

- architecture guardrail script passes
- new checks do not flag documented exceptions
- guardrail output remains actionable rather than noisy

### Phase 11E: First Legacy Compatibility Retirement Slice

**Problem:** frontend callers are gone, but compatibility endpoints still exist.

Execution:

1. start from `docs/refactor/phase-10-legacy-compatibility-retirement-ledger.md`
2. select one low-risk deprecated controller group
3. verify there are no frontend callers, no test-only assumptions that should remain, and no known external compatibility requirement
4. remove the controller and directly related DTO/test coverage only for that group
5. update the ledger with the deletion result

Preferred first candidates:

- dataset compatibility endpoint
- filter compatibility endpoint
- subject group compatibility endpoint
- rule or rule-set compatibility endpoint

Exit gate:

- one compatibility group is retired or explicitly deferred with evidence
- module-native replacement remains available
- tests and architecture guardrails pass

### Phase 11F: Architecture Adjustment Readiness Review

**Problem:** large architecture changes should start after high-signal debt is either fixed or explicitly accepted.

Review topics:

- whether OpenAPI type generation should start before the next product feature
- whether `LegacyCrfAdapter` is still a compatibility necessity or a removable implementation bridge
- whether `module/legacy` should be deleted in multiple compatibility slices
- whether frontend feature folders should be reorganized around typed API/domain boundaries
- whether backend module public APIs need stronger package-level boundaries before new feature work

Exit gate:

- a short readiness note is added to this plan
- the next large architecture plan has clear prerequisites and a bounded first slice

## Recommended Delivery Order

1. Add this Phase 11 plan and mark it active in the refactor README.
2. Fix event DTO contract drift and remove unsafe casts.
3. Remove randomization `userId=0` placeholders.
4. Tighten architecture guardrails for the fixed patterns.
5. Reduce one additional typed API gap if it is small and low risk.
6. Retire or explicitly defer one `module/legacy` compatibility controller group.
7. Write the Phase 11 readiness note for larger architecture changes.

## Verification Matrix

| Change Type | Required Verification |
|---|---|
| Docs only | `git diff --check` |
| Frontend type or API changes | `pnpm -C frontend typecheck` |
| Frontend component or hook changes | `pnpm -C frontend lint` |
| Guardrail changes | `bash scripts/ci/check-architecture-guardrails.sh` |
| Backend controller or service changes | `mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false` |
| Export contract changes | `mvn test -pl app -am -Dtest=OdmExportGeneratorTest -Dsurefire.failIfNoSpecifiedTests=false` |
| Compatibility endpoint deletion | targeted backend tests plus architecture guardrails |

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Event UI depends on old frontend-only field names | medium | add a small mapper instead of weakening transport DTO types |
| Removing `userId` changes randomization audit semantics | high | inspect backend service flow first and preserve acting-user attribution |
| Guardrails become too strict | medium | keep exceptions explicit and domain-specific |
| Legacy endpoint deletion breaks unknown external clients | high | delete only deprecated, zero-frontend-call endpoints with documented replacement |
| OpenAPI generation creates broad churn | medium | defer generation until known drift is fixed manually |

## Success Criteria

1. Phase 11 is the active refactor plan.
2. event DTO contract drift is resolved.
3. unsafe event `Promise<unknown>` casts are removed.
4. frontend no longer sends `userId=0` for randomization.
5. guardrails prevent reintroducing those patterns.
6. at least one typed API gap is reduced or explicitly classified.
7. at least one `module/legacy` compatibility group is retired or explicitly deferred with evidence.
8. the codebase has a written readiness decision for the next large architecture phase.

## Immediate Next Action

Start with Phase 11A.

Read the backend and frontend event contract files, align the frontend transport DTO to the backend response shape, and remove the unsafe cast in `SubjectDetail.tsx`.
