# Phase 1 Email Field Removal Slice

**Status:** product-facing request/contact and email-field paths retired; Java entity mappings removed; database/ODM compatibility surfaces remain.
**Plan:** `docs/refactor/phase-1-email-field-removal-plan.md`.

## Scope

This slice removes stale product entry points that still implied email-backed account/study/contact workflows after mail delivery and Enterprise support flows were retired.

## Removed

| Area | Change |
|---|---|
| SPA request study | Deleted `frontend/src/pages/RequestStudy.tsx`, which posted to retired `/api/v1/legacy/request-study`. |
| SPA routing | Removed `/app/request-study` from `frontend/src/router/index.tsx`. |
| Legacy route redirects | Removed `/RequestAccount`, `/RequestStudy`, and `/Contact` redirects from `WebMvcConfig`. |
| Legacy JSP links | Removed `RequestAccount` links from inactive sidebars. |
| Legacy footer/static links | Removed `/app/contact` and static `Contact` links. |
| Audit fixture | Replaced email-specific audit fixture values with a neutral `phone` column. |

## Compatibility References Kept

- Existing migration XML, sync trigger SQL, and database email columns: retained per the plan's out-of-scope rules and kept inert by database write-boundary triggers. Runtime i18n bundles no longer keep unreferenced email label keys.
- ODM metadata export contact fields: retained until downstream contract review or versioned export contract replacement.

## Result

No frontend source file now references email fields, request-account routes, request-study routes, contact SPA routes, or the retired `/api/v1/legacy/request-study` endpoint. Active Java entity APIs no longer expose retired email compatibility fields.
