# Phase 9F: Architecture Transition Design

**Created:** 2026-06-25
**Status:** Complete
**Purpose:** define the target architecture and migration strategy for module-native APIs, typed frontend contracts, and unified study context.

## Backend Target Architecture

### API Surface

| Layer | Path Pattern | Purpose |
|---|---|---|
| **Product API** | `/api/v1/*` | Module-native endpoints, module-owned DTOs |
| **Legacy Bridge** | `/api/legacy/*` | Temporary backward compatibility (retired incrementally) |
| **Auth** | `/api/v1/auth/*` | Session management, CSRF |

### Module API Principles

1. **`/api/v1/*` is the product API surface.** All new features and migrated domains use module-native endpoints.

2. **`module/legacy` is a temporary bridge, not a destination.** Legacy controllers exist only while frontend callers haven't been migrated. Each legacy controller has a deletion gate tied to zero frontend callers.

3. **Module APIs expose module-owned DTOs.** Each module defines its own DTOs under `module/<name>/dto/`. No shared DTO packages.

4. **Cross-module behavior uses explicit ports.** Modules communicate via:
   - `ApplicationEvents` (Spring Modulith) for async cross-module triggers
   - Named interfaces (Java `interface` contracts) for sync dependencies
   - Never `@Autowired` beans from other modules directly

5. **No shared Java support code is reintroduced.** `shared/src/main/java` remains at 0 files. All new code belongs in appropriate modules.

### Module Structure

```
module/<name>/
├── controller/      # REST controllers (@RestController)
├── dto/             # Module-owned DTOs
├── entity/          # JPA entities (@Entity(name = "Module<Name>"))
├── repository/      # Spring Data repositories
├── service/         # Business logic (@Service)
├── internal/        # Internal implementation details
│   └── adapter/     # Legacy DAO adapters (anti-corruption layer)
└── package-info.java
```

### Entity Naming Convention

```java
@Entity(name = "Module<Name>")
@Table(name = "module_<name>")
```

This prevents collision with legacy table mappings and makes module ownership explicit.

## Frontend Target Architecture

### API Layer Principles

1. **Feature folders own API hooks, local types, and pages.** Each feature directory contains its own hooks, types, and page components.

2. **Generated or centrally typed API contracts prevent DTO drift.** The `frontend/src/api/generated.ts` file will be replaced with `openapi-typescript` output from the backend OpenAPI spec.

3. **Study context is provided consistently.** All study-scoped pages use `useCurrentStudy()` as the canonical source (see Phase 9E).

4. **Raw `fetch` is limited to exceptional cases.** The `apiClient` wrapper handles CSRF, credentials, and error shaping. Raw `fetch` is only used for:
   - Blob downloads (file export)
   - FormData uploads (file import)
   - Auth endpoints (login/logout)

5. **Legacy iframe route is removed.** The `LegacyFrame.tsx` component and `legacy/*` route have been deleted.

### Frontend Structure

```
frontend/src/
├── api/
│   ├── client.ts           # ApiClient (session-based, CSRF, error handling)
│   └── generated.ts        # OpenAPI-generated types (future)
├── components/             # Shared UI components
│   ├── form-engine/        # Dynamic form rendering
│   ├── StudySwitcher.tsx   # Study selection dropdown
│   └── ...
├── hooks/                  # Custom React hooks
│   ├── useQuery.ts         # useAppQuery / useAppMutation wrappers
│   ├── useStudies.ts       # Study context provider
│   ├── useEvents.ts        # Event data fetching
│   └── ...
├── pages/                  # Route page components
│   ├── events/             # Event management
│   ├── export/             # Export center
│   ├── rules/              # Rule management
│   └── ...
├── types/                  # TypeScript type definitions
└── router/                 # React Router configuration
```

### Data Fetching Pattern

```typescript
// hooks/useFeature.ts
export function useFeatureData(studyId: number | undefined) {
  return useAppQuery<FeatureDTO[]>({
    queryKey: ["feature", studyId],
    queryFn: () =>
      studyId
        ? apiClient.get<FeatureDTO[]>("/api/v1/feature", { studyId })
        : Promise.resolve([]),
    enabled: !!studyId,
  });
}
```

## Migration Strategy

### Phase Sequence

| Phase | Scope | Dependencies |
|---|---|---|
| **9E** | Unified study context contract | Phase 8 complete |
| **9F** | Architecture transition design | 9E complete |
| **10A** | OpenAPI contract generation | SpringDoc available |
| **10B** | Legacy gateway retirement (per domain) | Module-native API exists |
| **10C** | Frontend feature architecture | 10A complete |
| **10D** | Backend Modulith boundary tightening | 10B complete |

### API Contract Generation (10A)

1. Install `openapi-typescript` as dev dependency
2. Configure build script to fetch `/v3/api-docs` from running backend
3. Generate `frontend/src/api/generated.ts` with all module API types
4. Add to CI pipeline: typecheck fails if generated types drift

### Legacy Gateway Retirement (10B)

For each legacy domain:
1. Verify module-native `/api/v1/*` endpoint exists
2. Migrate all frontend callers
3. Add frontend type coverage
4. Verify zero callers with grep
5. Delete legacy controller and DTOs
6. Run ModulithVerificationTest

### Frontend Feature Architecture (10C)

1. Each feature folder contains its own hooks, types, and pages
2. Shared components remain in `components/`
3. API client and generated types remain in `api/`
4. Study context provided via `useCurrentStudy()`

## Rollback Strategy

| Change | Rollback |
|---|---|
| New module controller | Delete controller file, recompile |
| Frontend API migration | Revert hook to use legacy endpoint |
| OpenAPI type generation | Delete generated file, revert to manual types |
| Legacy controller deletion | Restore controller from git history |

## Verification

- `pnpm typecheck` — 0 errors
- `pnpm lint` — 0 errors
- `pnpm test --run` — 25/25 pass
- `mvn compile` — BUILD SUCCESS
- `ModulithVerificationTest` — 1/1 pass
