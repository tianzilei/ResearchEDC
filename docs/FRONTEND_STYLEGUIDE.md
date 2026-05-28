# ResearchEDC Frontend Styleguide

**Scope:** `frontend/` React SPA  
**Last updated:** 2026-05-28  
**Stack:** React 19, TypeScript strict, Vite 6, React Router 7, TanStack Query 5, Ant Design 5, SurveyJS

This guide records the current frontend conventions in the codebase. Use it when adding pages, refactoring UI, or building new questionnaire/randomization/data-capture workflows.

## Product Shape

ResearchEDC is an operational clinical research application. The UI should feel quiet, dense, predictable, and built for repeated work. Prefer scan-friendly tables, compact forms, clear status labels, and restrained page headers over marketing-style layouts.

The application has three main surfaces:

- `/app/*`: authenticated SPA inside `AppLayout`
- `/q/fill/:token`: public questionnaire fill page, optimized for mobile and narrow screens
- `/app/legacy/*`: iframe wrapper around legacy JSP routes

## Application Architecture

Use the existing directory boundaries:

- `src/api/`: API client and generated/manual DTO types
- `src/hooks/`: domain-specific query/mutation hooks
- `src/pages/<domain>/`: route pages
- `src/components/`: shared UI components
- `src/components/form-engine/`: CRF data-entry form primitives
- `src/components/questionnaire-builder/`: SurveyJS questionnaire authoring UI
- `src/providers/`: app-wide context providers
- `src/styles/`: Ant Design theme tokens and global CSS variables
- `src/types/`: frontend-facing TypeScript DTOs

Keep page files responsible for route composition and local UI state. Put reusable API access in hooks, reusable display primitives in components, and shared DTOs in `src/types`.

## Routing

Add authenticated routes under `src/router/index.tsx` as children of `/app`, wrapped by `ProtectedRoute` through `AppLayout`.

Use lazy imports for route pages:

```tsx
const MyPage = lazy(() => import("@/pages/my-domain/MyPage"));
```

Public questionnaire routes stay outside `AppLayout`. They must not depend on authenticated layout state or the side navigation.

## Data Fetching

Prefer `apiClient` from `src/api/client.ts` for backend calls. It handles:

- `credentials: "same-origin"`
- JSON and `FormData`
- CSRF header injection for mutating requests
- consistent `ApiError`

Prefer domain hooks using `useAppQuery` and `useAppMutation`:

```ts
export function useThing(id: number | undefined) {
  return useAppQuery<Thing>({
    queryKey: ["thing", id],
    queryFn: () => apiClient.get<Thing>(`/api/v1/things/${id}`),
    enabled: !!id,
  });
}
```

Mutation hooks should invalidate the smallest stable query key that refreshes affected views.

Avoid raw `fetch` in new code unless there is a clear exception. Existing raw `fetch` call sites are legacy or transitional patterns.

## Authentication And Permissions

Use `useAuth()` for user/session state. Do not inspect cookies directly outside the API/auth layers.

Use `usePermissions()` or `useHasPermission()` for menu and action visibility. Permission names live in `src/types/user.ts`.

Do not hide backend authorization gaps with frontend checks. Frontend permission checks are usability controls; backend permissions remain authoritative.

## Page Composition

Use the existing operational page pattern:

- Top area: compact title/header, optional subtitle, primary action on the right
- Body: `Card`, `Table`, `Tabs`, `Descriptions`, or focused form
- Loading: `SkeletonPage`, `SkeletonTable`, or `Spin` for local sections
- Empty: Ant Design `Empty` or table `locale.emptyText`
- Error: `Alert` or `Result`

Prefer `PageHeader` for new pages unless the page has a strong existing local pattern.

For tables:

- Put table inside a `Card`, usually with `styles={{ body: { padding: 0 } }}` for dense data grids
- Use stable `rowKey`
- Use pagination for long lists
- Render missing values as `-`
- Use `.number-display` for numeric counters

For forms:

- Use Ant Design `Form` with `layout="vertical"` by default
- Use `Form.Item` rules for required fields and validation
- Use `Modal` for short create/edit flows
- Use full pages or multi-step flows for study setup, randomization configuration, and questionnaire design

## Visual Style

The current design system is `Mono-Performance`:

- System font stack only
- Neutral grayscale base
- No decorative gradients, bokeh, background art, or ornamental imagery
- Small radii: 2px, 4px, 6px
- Compact page headings: 18-24px
- Dense spacing: 8, 12, 16, 20, 24px
- Minimal motion; reduced-motion support is already in `global.css`

Use CSS variables from `global.css`:

- `--text`, `--text-secondary`, `--text-muted`
- `--bg-layout`, `--bg-secondary`, `--panel`
- `--border`, `--border-light`, `--border-strong`
- `--accent`, `--danger`, `--warning`, `--success`, `--info`
- `--radius-sm`, `--radius-md`, `--radius-lg`

Use status label classes for state chips:

```tsx
<span className="status status-success">ACTIVE</span>
```

Available classes: `status-success`, `status-warning`, `status-danger`, `status-info`, `status-default`.

Avoid hard-coded colors in new code. If a color is needed, add a theme token or CSS variable first. Hard-coded colors still exist in older pages and should be removed opportunistically.

## Ant Design Usage

Use Ant Design components for core UI:

- `Table` for entity lists
- `Form`, `Input`, `Select`, `DatePicker`, `InputNumber` for input
- `Modal` for bounded create/edit tasks
- `Tabs` for peer sections
- `Descriptions` for read-only records
- `Alert`, `Result`, `Empty`, `Skeleton`, `Spin` for states
- `Tag` only when semantic status classes are not enough

Prefer `styles={{ body: { ... } }}` over deprecated `bodyStyle`.

Use `@ant-design/icons` for icon buttons. Do not introduce hand-written SVG icons for common actions.

## Internationalization

The app uses `react-i18next` with `en` and `zh`.

For new user-facing text, prefer translation keys in:

- `src/locales/en/translation.json`
- `src/locales/zh/translation.json`

Some legacy pages still use inline Chinese/English text. New pages should avoid adding more untranslated strings unless matching a local transitional page.

## Forms And CRF Data Entry

Use the form engine for CRF item rendering:

- `DataEntryForm`
- `FormField`
- `FormStatus`

Data-entry rules:

- Sort CRF items by `ordinal`
- Disable fields through `isFieldDisabled(statusConfig)`
- Use auto-save only when `onSave` exists and the record is editable
- Keep status transitions explicit: `INITIAL`, `DRAFT`, `SUBMITTED`, `LOCKED`, `FROZEN`, `SIGNED`

When adding CRF controls, extend `FormField` and cover it with a focused test.

## Questionnaire UI

Questionnaire authoring uses SurveyJS schema transformed through `QuestionnaireBuilder`.

Builder rules:

- Keep `SurveyDef` as the internal editable structure
- Convert to/from SurveyJS JSON at boundaries
- Keep page/question operations local and predictable
- Use SurveyJS `Model` only for preview/rendering, not as the editing source of truth

Public fill page rules:

- Keep layout narrow and mobile-friendly
- Avoid relying on authenticated providers or study context
- Draft and submit actions must show clear loading and success/error feedback
- Future fingerprint, temp-link, and e-sign flows should be implemented here without coupling to `AppLayout`

## Legacy Interop

Use `LegacyFrame` for JSP screens that are not yet strangulated. Do not add new iframe wrappers when a proper SPA route can be built.

Legacy bridge APIs live under `/api/legacy/*`; new module APIs should use `/api/v1/*`.

## TypeScript

Keep TypeScript strict clean:

- Prefer typed DTOs in `src/types`
- Avoid `any`; if a legacy boundary requires it, isolate and narrow quickly
- Avoid non-null assertions; use early returns
- Use `unknown` for caught errors and narrow with `instanceof Error`
- Use stable query key arrays with domain prefixes

Use the `@/` alias for imports from `src`.

## Testing

Use Vitest and React Testing Library for component/domain behavior. Existing examples:

- `StudySwitcher.test.tsx`
- `form-engine/DataEntryForm.test.tsx`
- `form-engine/FormField.test.tsx`
- `form-engine/FormStatus.test.ts`

Add tests when:

- Form status or validation behavior changes
- A shared component changes
- A hook changes cache keys or mutation invalidation
- A page adds non-trivial workflow logic

Use Playwright for smoke/e2e checks under `frontend/e2e`.

## Commands

```bash
cd frontend
pnpm typecheck
pnpm test --run
pnpm lint
pnpm build
pnpm test:e2e
```

Current known quality state:

- `pnpm typecheck`: expected 0 errors
- Vitest: expected 25 passing tests
- ESLint: known warnings/errors may exist; do not add new ones

## Implementation Checklist

Before finishing frontend work:

- Route is registered and reachable
- API calls use `apiClient` or a justified exception
- Loading, empty, error, and success states exist
- Buttons/actions are permission-aware where applicable
- Layout works at desktop and mobile widths if user-facing
- Text uses i18n keys for new stable UI
- No new one-off color palette or decorative styling
- Typecheck passes
- Focused tests are added or updated when behavior changes
