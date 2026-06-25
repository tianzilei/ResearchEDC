# Phase 9E: Unified Study Context Contract

**Created:** 2026-06-25
**Status:** Complete
**Purpose:** define the canonical study/site/user context architecture for the SPA frontend.

## Context Sources Inventory

### 1. Active Study Context (`useCurrentStudy`)

**Source:** `frontend/src/hooks/useStudies.tsx` — `StudyProvider` + `useCurrentStudy()`

| Property | Behavior |
|---|---|
| Storage | `sessionStorage` key `oc_current_study` (JSON-serialized `Study` object) |
| Provider | `<StudyProvider>` wraps the app inside `<AuthProvider>` |
| Getter | `useCurrentStudy()` returns `{ currentStudy, setCurrentStudy, clearCurrentStudy }` |
| Type | `Study \| null` — null when no study is selected |
| Invalidation | `setCurrentStudy()` calls `queryClient.invalidateQueries()` for dashboard, crfs, event-definitions, subject-events, group-classes, randomization |

### 2. Auth/User Context (`useAuth`)

**Source:** `frontend/src/providers/AuthProvider.tsx` — `AuthProvider` + `useAuth()`

| Property | Behavior |
|---|---|
| Storage | In-memory React state (no persistence) |
| Provider | `<AuthProvider>` wraps the app at the top level |
| Getter | `useAuth()` returns `{ user, isAuthenticated, login, logout }` |
| Type | `UserInfo \| null` — contains userId, username, firstName, lastName, roles |
| Bootstrap | Fetches `GET /api/v1/auth/me` on mount |

### 3. Study List (`useStudies`)

**Source:** `frontend/src/hooks/useStudies.tsx` — `useStudies()`

| Property | Behavior |
|---|---|
| Endpoint | `GET /api/v1/studies` |
| Returns | `StudySummary[]` — grouped studies with sites |
| Cache | 5 minutes stale time, query key `["studies"]` |
| Usage | StudySwitcher dropdown, admin pages |

### 4. Dashboard Bootstrap

**Source:** `frontend/src/hooks/useDashboard.ts`

| Property | Behavior |
|---|---|
| Endpoint | `GET /api/v1/dashboard/bootstrap` |
| Returns | User context, study count, module list, pending tasks |
| Cache | Query key `["dashboard"]` |
| Usage | Dashboard page initial load |

## Context Contract

### Canonical Active Study Source

```
useCurrentStudy().currentStudy
```

This is the single source of truth for the active study throughout the application. All pages that need study context must use this hook.

### Rules

1. **No local fallback IDs.** Pages must not invent `studyId=0` or similar placeholders. If `currentStudy` is null, show a "select study" state or disable study-scoped actions.

2. **Query keys include study ID.** All TanStack Query keys that are study-scoped must include the study ID:
   ```typescript
   queryKey: ["feature-name", studyId]
   ```

3. **Study switching invalidates queries.** When `setCurrentStudy()` is called, the following query families are invalidated:
   - `["dashboard"]`
   - `["crfs"]`
   - `["event-definitions"]`
   - `["subject-events"]`
   - `["group-classes"]`
   - `["randomization"]`

4. **Backend APIs use explicit studyId.** All `/api/v1/*` endpoints that are study-scoped accept `studyId` as a query parameter. The backend does not resolve study from session context.

5. **No session-scoped study on backend.** Study context is frontend-only. Backend APIs are stateless with respect to study selection.

### Page Integration Patterns

| Pattern | Example | Behavior |
|---|---|---|
| Hook with `enabled` guard | `useSchemes(studyId)` with `enabled: studyId > 0` | Query disabled when no study selected |
| Conditional rendering | `if (!currentStudy) return <SelectStudy />` | Show selection prompt |
| Derived from URL | `useParams<{ studyId: string }>` | Study from route params (admin pages) |

### Study Switching Flow

1. User selects study in `StudySwitcher`
2. `setCurrentStudy(study)` called
3. `sessionStorage` updated
4. React state updated
5. `queryClient.invalidateQueries()` called for all study-dependent query families
6. TanStack Query refetches data for the new study
7. Components re-render with new data

## Files Involved

| File | Role |
|---|---|
| `frontend/src/hooks/useStudies.tsx` | `StudyProvider`, `useCurrentStudy()`, `useStudies()` |
| `frontend/src/providers/AuthProvider.tsx` | `AuthProvider`, `useAuth()` |
| `frontend/src/components/StudySwitcher.tsx` | Study selection dropdown |
| `frontend/src/pages/Dashboard.tsx` | Dashboard bootstrap |

## Verification

- `pnpm typecheck` — 0 errors
- `pnpm lint` — 0 errors
- `pnpm test --run` — 25/25 pass
