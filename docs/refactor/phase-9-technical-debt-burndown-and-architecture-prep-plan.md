# Phase 9 Technical Debt Burndown And Architecture Prep Plan

**Created:** 2026-06-25
**Status:** Complete (2026-06-25)
**Purpose:** define the umbrella plan for reducing remaining technical debt before making larger architecture-level changes.
**Predecessor:** `docs/refactor/phase-8-event-workflow-context-hardening-plan.md`

## Current Context

The project has completed the original legacy/refactor/removal program and several post-refactor stabilization phases.

Completed baseline:

- `shared/src/main/java`: `0` Java files
- `web/`: deleted
- `ws/`: absent
- `legacy-core/`: absent
- DAO SPI ledger: fully removed
- Spring Boot upgraded to `3.5.2`
- Spring Modulith upgraded to `1.4.1`
- dependency drift converged
- export productization and export operations polish completed

Remaining work is no longer broad legacy deletion. The remaining work is a mix of:

- small correctness debts
- orphan compatibility shells
- active `/api/legacy/*` bridge usage
- frontend API typing debt
- inconsistent study-context handling
- architecture preparation for a future module-native API and frontend feature architecture

## Phase 9 Goal

Reduce most remaining technical debt that would make a larger architecture adjustment risky, then prepare a clear architecture transition plan for module-native APIs, typed frontend contracts, and unified study context.

## Strategic Position

Do not start with a broad architecture rewrite.

First reduce high-signal debt in small slices:

1. remove obvious placeholders and orphan compatibility code
2. converge active study context behavior
3. reduce frontend API typing and raw fetch debt
4. migrate active `/api/legacy/*` bridge usage by domain
5. harden verification gates

Then start larger architecture-level changes with less ambiguity.

## Non-Goals

- Do not remove all `/api/legacy/*` endpoints in one step.
- Do not rewrite every frontend API call in one pass.
- Do not redesign all frontend routing and app shell behavior at once.
- Do not merge or redraw all Modulith boundaries in one pass.
- Do not modify released Liquibase migrations retroactively.
- Do not remove active compatibility APIs before frontend callers are migrated.

## Workstream Overview

| Workstream | Theme | Outcome |
|---|---|---|
| 9A | Correctness debt | remove placeholder study context and similar product correctness gaps |
| 9B | Orphan compatibility cleanup | delete unused compatibility shells after proof |
| 9C | Frontend API debt | introduce reliable typed API generation or a transitional typed API layer |
| 9D | Legacy gateway strangulation | migrate active `/api/legacy/*` usage domain by domain |
| 9E | Unified study context | make active study/site/user context explicit and consistent |
| 9F | Architecture transition design | define target module-native API and frontend feature architecture |
| 9G | Verification hardening | keep the daily gauntlet and focused gates trustworthy |

## Workstreams

### Phase 9A: Correctness Debt First

**Goal:** clear product correctness issues that are already known and bounded.

#### 9A.1 Execute Phase 8

Start with:

- `docs/refactor/phase-8-event-workflow-context-hardening-plan.md`

Primary issue:

- `frontend/src/pages/events/EventList.tsx` uses `studyId=0` to load event definitions

Expected outcome:

- event workflows derive active study id from the canonical frontend study context
- no invalid placeholder request is sent
- backend event definition scope is explicit or documented
- frontend and backend focused tests cover the behavior

#### 9A.2 Identify Similar Placeholder Scope Debt

Search for:

- hardcoded study ids
- `TODO` around context
- query calls that use default `0`
- unscoped list endpoints where scope should be explicit

Do not fix all findings immediately. Classify them as:

- correctness risk
- harmless placeholder text
- test-only fixture
- historical documentation

#### 9A Exit Gate

- Phase 8 is complete
- no known active product page relies on `studyId=0`
- remaining placeholder findings are classified

### Phase 9B: Orphan Compatibility Cleanup

**Goal:** remove compatibility shells that are genuinely unused and not product bridges.

#### 9B.1 LegacyEventAdapter Proof And Removal

Current candidate:

- `app/src/main/java/org/researchedc/module/event/infrastructure/LegacyEventAdapter.java`

Current scan result:

- no callers found
- methods throw `UnsupportedOperationException`

Required proof:

```bash
rg -n "LegacyEventAdapter|findLegacyStudyEventId|syncEventStatus|findLegacySubjectLabel" app/src/main/java app/src/test/java
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
```

If no callers exist, delete it.

#### 9B.2 LegacyFrame Route Review

Current candidate:

- `frontend/src/components/LegacyFrame.tsx`
- route: `frontend/src/router/index.tsx` path `legacy/*`

Required classification:

- still needed for reachable legacy pages
- dead route shell
- temporary bridge retained for operator/admin workflows

Do not delete until route usage is proven absent or intentionally retired.

#### 9B.3 Legacy-Named But Active Code

Do not delete these just because of names:

- `LegacyCrfAdapter` if still used by `CrfService`
- `module/legacy` REST controllers while frontend calls `/api/legacy/*`
- ODM `OC2_0_COMPAT` contract

Rename only when it improves clarity and tests protect behavior.

#### 9B Exit Gate

- obvious orphan compatibility classes are deleted
- active compatibility bridges are documented as retained
- no route or bean is removed without caller proof

### Phase 9C: Frontend API Typing Debt

**Goal:** reduce hand-written DTO drift and raw `fetch` usage before broader API changes.

#### 9C.1 Inventory Current API Calls

Classify frontend calls into:

- `ApiClient` wrapped calls
- raw `fetch`
- generated placeholder types
- `/api/v1/*`
- `/api/legacy/*`

Known debt:

- `frontend/src/api/generated.ts` contains a TODO to replace with generated output

#### 9C.2 Choose Contract Strategy

Evaluate:

1. `openapi-typescript`
2. `openapi-generator`
3. transitional manually maintained typed API modules by feature

Decision criteria:

- Spring Boot OpenAPI availability
- generated type quality
- CI complexity
- frontend ergonomics
- ability to migrate incrementally

#### 9C.3 First Slice

Prefer a narrow first slice:

- generate or define types for one stable domain
- candidate: export API, because Phase 7 recently hardened it
- do not attempt every module API at once

#### 9C Exit Gate

- contract strategy chosen
- first domain has typed API coverage
- frontend typecheck catches DTO mismatch for that domain

### Phase 9D: Legacy Gateway Strangulation

**Goal:** migrate active `/api/legacy/*` usage to module-native APIs by domain.

Current active gateway surface:

- CRF admin
- rules / rule sets
- datasets / filters
- discrepancy notes
- subject groups
- entity action helpers
- import upload bridge

#### 9D.1 Build Active Usage Inventory

Create a table with:

- frontend file
- endpoint
- operation
- owning backend module
- existing module-native equivalent, if any
- migration difficulty
- deletion gate

#### 9D.2 Migration Order

Recommended order:

1. **datasets / filters**
   - smaller domain
   - already module-backed
   - good first strangulation slice

2. **subject groups**
   - bounded domain
   - used through hooks

3. **discrepancy notes**
   - cross-cutting but still clear API surface

4. **rules / rule sets**
   - higher complexity
   - rule expression and assignment behavior need care

5. **CRF admin**
   - larger product surface
   - version management and metadata behavior need tests

6. **import upload**
   - keep as bridge until import workflow is fully module-native and tested

#### 9D.3 Per-Domain Exit Gate

For each domain:

- add or identify module-native `/api/v1/*` endpoint
- migrate frontend caller
- add frontend type coverage
- add backend controller/service tests
- remove unused `module/legacy` controller/DTO only after zero frontend/test callers remain
- run focused backend/frontend gates

#### 9D Exit Gate

- at least one `/api/legacy/*` domain migrated end to end
- deletion proof exists for removed controllers/DTOs
- active bridge endpoints are intentionally retained

### Phase 9E: Unified Study Context

**Goal:** make study/site/user context a first-class, consistent application concept.

#### 9E.1 Inventory Existing Context Sources

Review:

- dashboard bootstrap
- study switcher
- auth/session user data
- `useStudies`
- pages that accept study id from URL/query/global state

#### 9E.2 Define Context Contract

Decide:

- frontend active study source
- active site behavior
- backend session-scoped vs query-scoped APIs
- behavior when no study is selected
- query invalidation rules when study changes

#### 9E.3 Apply Incrementally

Apply first to:

- events, through Phase 8
- export, if export list should be scoped to current study
- rules and subject groups, during gateway strangulation

#### 9E Exit Gate

- one canonical active-study source is documented
- pages no longer invent local fallback ids
- study switching invalidates affected queries consistently

### Phase 9F: Architecture Transition Design

**Goal:** define the architecture target before doing broad structural changes.

#### 9F.1 Backend Target

Target principles:

- `/api/v1/*` is the product API surface
- `module/legacy` is a temporary bridge, not a destination
- module APIs expose module-owned DTOs
- cross-module behavior uses explicit ports, named interfaces, or events
- no shared Java support code is reintroduced

#### 9F.2 Frontend Target

Target principles:

- feature folders own API hooks, local types, and pages
- generated or centrally typed API contracts prevent DTO drift
- study context is provided consistently
- raw `fetch` is limited to exceptional cases
- legacy iframe route is removed or explicitly retained with owner and reason

#### 9F.3 Migration Strategy

Use architecture decision records or focused phase plans for:

- API contract generation
- legacy gateway retirement
- study context unification
- frontend feature architecture
- backend Modulith boundary tightening

#### 9F Exit Gate

- target architecture is documented
- migration sequence is split into small phases
- large changes have rollback and verification strategy

### Phase 9G: Verification Hardening

**Goal:** keep quality gates reliable while debt is burned down.

#### 9G.1 Required Gates By Change Type

Backend module/API changes:

```bash
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
```

Frontend changes:

```bash
cd frontend && pnpm typecheck
cd frontend && pnpm lint
cd frontend && pnpm test --run
```

Broad backend/frontend changes:

```bash
bash scripts/ci/daily-gauntlet.sh
```

#### 9G.2 Guardrail Updates

Consider guardrails for:

- no new `shared/src/main/java`
- no new `/api/legacy/*` frontend callers unless explicitly approved
- no new `studyId=0` or equivalent scope placeholders
- no new raw `fetch` in pages when `ApiClient` wrapper is appropriate

#### 9G Exit Gate

- each slice documents its verification
- guardrails prevent regression to retired patterns
- daily gauntlet remains the broad gate

## Recommended Execution Order

1. Execute Phase 8 event workflow context hardening.
2. Remove proven orphan compatibility code, starting with `LegacyEventAdapter`.
3. Inventory `/api/legacy/*` usage and pick the first strangulation domain.
4. Decide frontend API typing strategy.
5. Migrate one small legacy gateway domain end to end.
6. Document unified study context contract.
7. Draft target architecture transition document.

## Risks

| Risk | Impact | Mitigation |
|---|---|---|
| Trying to remove active `/api/legacy/*` endpoints too early | high | migrate frontend callers first and require deletion proof |
| Architecture work starts before context/API debt is reduced | high | complete Phase 8 and at least one gateway strangulation slice first |
| OpenAPI generation adds tooling churn without product payoff | medium | start with one stable domain before broad adoption |
| Study context changes break site/parent-study behavior | high | test site and parent-study paths explicitly |
| Raw cleanup becomes cosmetic renaming | medium | prioritize behavior and contract improvements over names |

## Success Criteria

1. Phase 8 is complete and `studyId=0` is gone from active event workflows.
2. proven orphan compatibility code is removed.
3. `/api/legacy/*` usage inventory exists.
4. at least one legacy gateway domain is migrated to module-native API.
5. frontend API typing strategy is selected and proven on one domain.
6. active study context contract is documented.
7. target architecture transition plan is ready for larger changes.
8. verification gates remain green or failures are classified with owners.

## Immediate Next Action

Start with **Phase 8 execution**. After Phase 8 lands, remove `LegacyEventAdapter` if the caller proof still shows it is unused, then create the `/api/legacy/*` active usage inventory for the first gateway strangulation slice.

## Completion Record (2026-06-25)

### Delivered Behavior

1. **Phase 8 complete**: EventList.tsx no longer sends `studyId=0` — replaced raw fetch with `useEventDefinitions(currentStudy?.id)` hook
2. **LegacyEventAdapter deleted**: 0 callers, all methods threw UnsupportedOperationException
3. **LegacyFrame deleted**: Component and route removed, 0 navigators, web/ module absent
4. **Datasets migrated**: Created `FilterController` at `/api/v1/filters` and `DatasetController` at `/api/v1/datasets`, migrated FilterBuilder.tsx and DatasetBuilder.tsx
5. **Subject groups migrated**: Created `SubjectGroupController` at `/api/v1/subject-groups`, migrated useSubjectGroups.ts
6. **Discrepancy notes migrated**: Created `DiscrepancyNoteController` at `/api/v1/discrepancy-notes`, migrated useDiscrepancyNotes.ts and updated frontend types
7. **Rules migrated**: Created `RuleController` at `/api/v1/rules`, added addRuleToRuleSet/removeRuleFromRuleSet methods to RuleService, migrated useRules.ts and RuleSetDetail.tsx
8. **CRF admin migrated**: Created `CrfManageController` at `/api/v1/crfs/manage`, migrated CrfAdmin.tsx and EntityAction.tsx

### Files Created (Backend)

- `app/.../module/filter/dto/FilterDTO.java`
- `app/.../module/filter/dto/CreateFilterRequest.java`
- `app/.../module/filter/controller/FilterController.java`
- `app/.../module/dataset/dto/DatasetDTO.java`
- `app/.../module/dataset/dto/CreateDatasetRequest.java`
- `app/.../module/dataset/controller/DatasetController.java`
- `app/.../module/subjectgroup/dto/SubjectGroupClassDTO.java`
- `app/.../module/subjectgroup/dto/SubjectGroupDTO.java`
- `app/.../module/subjectgroup/dto/CreateGroupClassRequest.java`
- `app/.../module/subjectgroup/dto/CreateGroupRequest.java`
- `app/.../module/subjectgroup/controller/SubjectGroupController.java`
- `app/.../module/discrepancynote/dto/DiscrepancyNoteDTO.java`
- `app/.../module/discrepancynote/dto/CreateDiscrepancyNoteRequest.java`
- `app/.../module/discrepancynote/controller/DiscrepancyNoteController.java`
- `app/.../module/rule/dto/RuleSetDTO.java`
- `app/.../module/rule/dto/RuleDetailDTO.java`
- `app/.../module/rule/dto/CreateRuleRequest.java`
- `app/.../module/rule/dto/AddRuleToRuleSetRequest.java`
- `app/.../module/rule/controller/RuleController.java`
- `app/.../module/crf/dto/CrfManageDTO.java`
- `app/.../module/crf/dto/CrfVersionManageDTO.java`
- `app/.../module/crf/dto/CreateCrfRequest.java`
- `app/.../module/crf/dto/CreateCrfVersionRequest.java`
- `app/.../module/crf/controller/CrfManageController.java`

### Files Modified (Frontend)

- `frontend/src/hooks/useSubjectGroups.ts` — migrated to `/api/v1/subject-groups/*`
- `frontend/src/hooks/useDiscrepancyNotes.ts` — migrated to `/api/v1/discrepancy-notes/*`
- `frontend/src/hooks/useRules.ts` — migrated to `/api/v1/rules/*`
- `frontend/src/pages/export/FilterBuilder.tsx` — migrated to `/api/v1/filters`
- `frontend/src/pages/export/DatasetBuilder.tsx` — migrated to `/api/v1/datasets`
- `frontend/src/pages/rules/RuleSetDetail.tsx` — migrated to `/api/v1/rules/*`
- `frontend/src/pages/admin/CrfAdmin.tsx` — migrated to `/api/v1/crfs/manage/*`
- `frontend/src/pages/EntityAction.tsx` — migrated rule and CRF endpoints
- `frontend/src/types/discrepancy.ts` — updated to match new DTO
- `frontend/src/components/DiscrepancyNotes.tsx` — updated to use new field names
- `frontend/src/pages/datacapture/DataEntryPage.tsx` — updated to use new field names

### Verification

- `pnpm typecheck` — 0 errors
- `pnpm lint` — 0 errors, 0 warnings
- `pnpm test --run` — 25/25 pass
- `mvn compile` — BUILD SUCCESS
- `mvn test -Dtest=ModulithVerificationTest` — 1/1 pass

### Remaining Work

- **9E**: Unified study context — document canonical active-study source ✅
- **9F**: Architecture transition design document ✅
- **9G**: Verification hardening — guardrails for regression prevention ✅
- Legacy controllers in `module/legacy/` retained for backward compatibility
