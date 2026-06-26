# Phase 1 Entity Action Slice

**Date:** 2026-06-11
**Status:** Implemented

## Scope

This slice closes the highest-risk gaps in the SPA `EntityAction` remove/restore bridge for study-subject, study-event, and event-CRF workflows. The goal is to keep legacy-generated action links from targeting retired `Remove*`/`Restore*` servlet URLs and route those actions through module-owned REST endpoints instead.

## Changes

- Added explicit subject module endpoints:
  - `PATCH /api/v1/subjects/{id}` restores a subject.
  - `DELETE /api/v1/subjects/enrollment/{id}` removes a study subject.
  - `PATCH /api/v1/subjects/enrollment/{id}` restores a study subject.
- Added event module endpoints:
  - `PATCH /api/v1/events/{id}` restores a study event.
  - `PATCH /api/v1/events/crfs/{crfId}` restores an event CRF.
- Split `SubjectService.removeSubject` from study-subject removal so entity action IDs are no longer resolved by ambiguous subject-first fallback behavior.
- Updated `frontend/src/pages/EntityAction.tsx` so `study-subject` fetches and mutates `/api/v1/subjects/enrollment/{id}`.
- Repointed remaining legacy Java helper links for study-subject, study-event, and event-CRF remove/restore actions to `/app/actions/...` URLs.

## Verification

- `mvn -pl app -am compile -DskipTests`
- `mvn test -pl app -am -Dtest=SubjectServiceTest,EventServiceTest -Dsurefire.failIfNoSpecifiedTests=false`

## Remaining Work

`EntityAction` still includes generic entity support and should stay in the SPA migration coverage table until the remaining unsupported remove/restore entity types are proven or retired. Data entry, import/export compatibility, OpenRosa-style controllers, and layout fragments remain separate blocked Phase 1 categories.
