# frontend/ - React 19 SPA

**Module:** Modern single-page application frontend  
**Files:** ~94 TypeScript/TSX files (src), ~109 total (excluding node_modules)  

> React 19 + TypeScript 5.8 strict + Vite 6 + Ant Design 5. Serves at `/app/*` path behind Nginx.
> Build output goes to `frontend/dist/` (Vite `outDir: "dist"`), served by Nginx in production.

## STRUCTURE

```
frontend/src/
├── main.tsx             # React entry point
├── api/                 # API client layer
│   ├── client.ts        # Fetch-based ApiClient (JSON + FormData)
│   └── generated.ts     # Auto-generated API types
├── components/          # Shared UI components
│   ├── form-engine/     # Dynamic form rendering
│   ├── questionnaire-builder/
│   ├── StudySwitcher.tsx
│   ├── DiscrepancyNotes.tsx
│   ├── LegacyFrame.tsx  # iframe wrapper for legacy JSP
│   └── SkeletonCard.tsx
├── config/              # App configuration
│   └── index.ts
├── hooks/               # Custom React hooks (12 hooks)
│   ├── useQuery.ts      # useAppQuery / useAppMutation wrappers (TanStack Query)
│   ├── useStudies.ts    # Study data fetching
│   ├── useEvents.ts     # Event data fetching
│   └── ... (useCrf, useDataCapture, useRules, etc.)
├── i18n/                # Internationalization setup
├── layouts/             # Page layout components
├── locales/             # Translation files
├── pages/               # Route page components (12 page groups)
│   ├── studies/
│   ├── subject/
│   ├── events/
│   ├── datacapture/
│   ├── crf/
│   ├── randomization/
│   ├── rules/
│   ├── export/
│   ├── questionnaire/
│   ├── admin/
│   └── ... (Dashboard, Login, Profile, etc.)
├── providers/           # React context providers (Auth, etc.)
├── router/              # React Router 7 configuration
├── styles/              # Global styles / theme overrides
└── types/               # TypeScript type definitions
```

## CONVENTIONS

- **Framework:** React 19 + TypeScript 5.8 strict + Vite 6
- **UI:** Ant Design 5 with `ConfigProvider` theme
- **Routing:** React Router 7, browser router with `/app/*` prefix
- **Data fetching:** TanStack Query 5 via typed `useAppQuery`/`useAppMutation` (see `useQuery.ts`)
- **API client:** Fetch-based `ApiClient` class supporting JSON and FormData
- **Auth:** Keycloak OIDC via `AuthProvider` context (session token flow)
- **Quality:** `pnpm typecheck` (0 errors) | `pnpm lint` (0 errors) | `pnpm build` (no warnings)
- **Testing:** Vitest + `test-setup.ts`, 4 test files (`.test.tsx`)

## API LAYER

The frontend communicates with the backend through:
- **`client.ts`** — Base `ApiClient` with token management, JSON/FormData serialization, error handling
- **`generated.ts`** — TypeScript interfaces matching backend DTOs (manually updated)
- **REST API** — Backend controllers at `/api/v1/*` return JSON
- **Legacy bridge** — `LegacyFrame.tsx` wraps JSP pages in iframes for `/legacy/*` path
- **Questionnaire service** — Separate Python FastAPI at `/q/*` path (Nginx proxied)

## ANTI-PATTERNS

- **NEVER** import from `node_modules` directly — use configured aliases
- **NEVER** mix API calls in components — use hooks from `hooks/`
- **ALWAYS** use `useAppQuery`/`useAppMutation` wrappers (not raw TanStack Query)
- **AVOID** direct DOM manipulation — let React/Virtual DOM handle updates
- **DO NOT** bypass `ApiClient` for API calls — it handles auth tokens consistently
