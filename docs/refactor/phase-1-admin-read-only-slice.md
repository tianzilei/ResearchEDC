# Phase 1 Admin Read-Only Slice

**Status:** first route deletion committed in progress.  
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
2. Legacy route either redirects to SPA or is no longer registered in `web.xml`/Spring MVC config/`LegacyServletConfig`.
3. Permissions match the relevant `SecureController.mayProceed()` behavior.
4. Audit/status/log field parity is captured by tests, snapshots, or explicit verification notes.
5. JSP include/tag/helper references are gone.
6. Targeted backend tests and relevant frontend checks pass.

## Ledger Result

The initial ledger started with 51 rows. After removing `ReportController` and 3 audit servlets, it tracks 47 active rows plus 4 deleted routes:

| Status | Count | Meaning |
|---|---:|---|
| `covered` | 0 | All covered rows have been deleted. |
| `needs replacement` | 44 | A frontend route or partial API exists, but backend field/permission/audit parity is not fully proven. |
| `blocked` | 2 | Product/route retirement decision is needed before implementation. |
| `deleted` | 4 | Legacy routes removed after replacement and reference proof. |

## First Execution Tasks

1. Complete: `ReportController` `/healthcheck` was removed after replacement APIs were added and reference checks found no production callers.
2. For audit rows, compare legacy JSP/servlet fields and filters against `/api/v1/audit` and `/app/admin/audit-log`.
3. For system/log/job rows, add or identify module-owned backend APIs before deleting JSP/servlet paths.
4. Delete only rows marked `covered` or `retire`, one workflow at a time, after route, permission, output parity, and reference checks pass.

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
