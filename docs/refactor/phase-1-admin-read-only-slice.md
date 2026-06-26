# Phase 1 Admin Read-Only Slice

**Status:** complete; generated inventory has no active `phase-1-admin-read-only` artifacts.  
**Inventory source:** `docs/refactor/legacy-workflow-inventory.csv` (`phase_slice=phase-1-admin-read-only`).  
**Slice ledger:** `docs/refactor/phase-1-admin-read-only-ledger.csv` and `docs/refactor/phase-1-admin-read-only-ledger.md`.

## Scope

This is the first low-risk deletion slice from `docs/refactor/remove-legacy-code-plan.md`. It covers admin/status/audit/log/scheduler read-only legacy surfaces, including the candidate artifacts listed in `docs/refactor/legacy-workflow-inventory.md`.

Initial candidate groups:

| Group | Examples | Required replacement proof |
|---|---|---|
| System/status views | `SystemStatusServlet`, `SystemController` system endpoints, `systemStatus.jsp` | SPA/API system status route is default, permission parity is documented, and status fields match legacy output. |
| Audit/log views | `AuditDatabaseServlet`, `AuditLogStudyServlet`, `AuditLogUserServlet`, `ViewLogMessageServlet`, audit JSPs | Audit API/SPA view covers the same filters, row fields, links, and authorization behavior. |
| Scheduler/job read-only views | `ViewJobServlet`, `ViewAllJobsServlet`, `ViewSingleJobServlet`, `ViewSchedulerServlet`, scheduler JSPs | Read-only job/scheduler state is available through module/API route with matching permissions and empty/error states. |
| Tech admin shell | `TechAdminServlet`, `techadmin/index.jsp` | Navigation either routes to SPA admin tools or is formally retired with no registered route. |

## Deletion Gate

No file in this slice may be deleted until all checks are true for that file's workflow:

1. SPA/API route is the default navigation path.
2. Legacy route either redirects to SPA or is no longer registered in `web.xml`, Spring MVC config, or any Boot servlet initializer.
3. Permissions match the relevant `SecureController.mayProceed()` behavior.
4. Audit/status/log field parity is captured by tests, snapshots, or explicit verification notes.
5. JSP include/tag/helper references are gone.
6. Targeted backend tests and relevant frontend checks pass.

## Ledger Result

The ledger started with 51 rows. After the admin read-only deletion slices, all rows are covered/deleted or formally retired, and the generated active inventory has 0 `phase-1-admin-read-only` rows:

| Status | Count | Meaning |
|---|---:|---|
| `covered` | 0 | All covered rows have been deleted. |
| `needs replacement` | 0 | No active admin read-only replacement rows remain. |
| `blocked` | 0 | Tech admin shell was formally retired. |
| `deleted` | 51 | Legacy routes/JSPs removed or retired after replacement and reference proof. |

## Result

`phase-1-admin-read-only` is closed. The remaining active inventory now recommends `phase-1-login-profile` as the next small deletion slice.

## Replacement Progress

Rule schedule replacement added:

- `POST /api/v1/rules/schedule/check` replaces `/healthcheck/runonschedule`.
- `POST /api/v1/rules/schedule/current-date` replaces `/healthcheck/rulecurrentdate`.
- `GET /api/v1/rules/schedule/default-runtime` replaces `/healthcheck/runtime`.

Legacy `/healthcheck` has been unregistered by deleting `ReportController`; `/api/v1/dashboard/health` and `/api/v1/rules/schedule/*` are the replacement routes.

Audit database changelog replacement added:

- `GET /api/v1/audit/database-changelog` replaces the field output of `/AuditDatabase` with `id`, `author`, `fileName`, `dateExecuted`, `md5Sum`, `description`, `comments`, `tag`, and `liquibase`.
- The route is guarded with `@PreAuthorize("hasRole('SYSADMIN')")`, matching `AuditDatabaseServlet.mayProceed()` `ub.isSysAdmin()` behavior.
- `ResearchEdcUserDetailsService` now maps legacy `user_type_id = 1` to `ROLE_SYSADMIN` so module APIs can express sysadmin-only gates.
- `AuditDatabaseServlet` deleted on 2026-06-08 after route registration, JSP reference, and SPA navigation cleanup proof.

Audit user login activity replacement added:

- `GET /api/v1/audit/user-logins` replaces the field output of `/AuditUserActivity` with `id`, `userName`, `userAccountId`, `loginAttemptDate`, `loginStatus`, `loginStatusCode`, and `details`.
- The endpoint accepts the legacy table filters `userName`, `loginAttemptDate`, `loginStatus`, and `details`, returns a Spring `Page`, and defaults to `loginAttemptDate` descending when no sort is supplied.
- The route is guarded with `@PreAuthorize("hasRole('SYSADMIN')")`, matching `AuditUserActivityServlet.mayProceed()` `ub.isSysAdmin()` behavior.
- `AuditUserActivityServlet` deleted on 2026-06-08 after route registration, JSP reference, and SPA navigation cleanup proof.

Audit user event log replacement added:

- `GET /api/v1/audit/users/{userId}/events` replaces the field output of `/AuditLogUser?userLogId=...` with a user summary plus audit event rows containing `id`, `auditDate`, `auditTable`, `userId`, `entityId`, localized and raw reason/action values, study/subject context, column values, `changes`, and `otherInfo`.
- The route is guarded with `@PreAuthorize("hasRole('SYSADMIN')")`, matching `AuditLogUserServlet.mayProceed()` `ub.isSysAdmin()` behavior.
- `AuditLogUserServlet` deleted on 2026-06-08 after route registration, JSP reference, and SPA navigation cleanup proof.
