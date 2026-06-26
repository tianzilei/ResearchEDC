# Post-Cleanup Hardening And Backlog Plan

**Created:** 2026-06-26
**Status:** Draft / reference
**Predecessor:** `docs/refactor/phase-18-post-cleanup-verification-baseline-plan.md`
**Release posture:** no commit requirement, no RC tagging, no release artifact creation

## Context

The repository has moved past the large refactor/removal phase. The legacy servlet
surface, shared Java surface, DAO infrastructure, and broad worktree churn have
been retired or cleaned up. The next work should avoid broad restructuring unless
a verification failure proves it is necessary.

Phase 18 is the post-cleanup verification baseline:

1. close Phase 17 in the refactor plan metadata
2. run the verification matrix
3. do not create release tags
4. do not create RC artifacts
5. do not push or publish as part of the phase

After Phase 18, the project should move into hardening first, then product backlog
slices.

## Recommended Phase Sequence

| Phase | Scope | Goal | Release Action |
|---|---|---|---|
| Phase 18 | Post-cleanup verification baseline | Close Phase 17 and verify the cleaned repository baseline | None |
| Phase 19 | CI and verification hardening | Make the verification baseline repeatable and visible in CI | None unless explicitly requested |
| Phase 20 | Security and deploy hardening | Stabilize runtime, permissions, observability, and deploy operations | None unless explicitly requested |
| Phase 21 | Product backlog slice 1 | Improve the core study / subject / event / data capture workflow | Deferred |

## Hardening Priorities

### 1. Verification And CI

Goal: make the current repository shape repeatable.

- turn the Phase 18 verification matrix into stable CI gates
- keep backend, frontend, and questionnaire service checks independently runnable
- add a line-ending guard after `.gitattributes`
- keep generated OpenAPI/type artifacts checked for staleness
- keep module-boundary verification as a required backend gate

Candidate checks:

```bash
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl app -am -Dtest=OdmExportGeneratorTest,ExportArtifactWriterTest -Dsurefire.failIfNoSpecifiedTests=false
pnpm -C frontend typecheck
pnpm -C frontend lint
pnpm -C frontend test --run
cd questionnaire-service/apps/api && uv run python -m pytest app/tests/ -v
```

### 2. Database And Migration Reliability

Goal: keep PostgreSQL and Oracle schema evolution trustworthy.

- verify clean Liquibase apply for PostgreSQL and Oracle paths
- protect released migrations from edits
- add checksum/drift checks where practical
- keep Phase B trigger/sync validation runnable
- document recovery expectations for migration failures

### 3. Security And Permissions

Goal: make the new REST and SPA surface safe to operate.

- review endpoint authorization by module
- verify CSRF/session/form-login behavior
- test admin, import, export, and download endpoints explicitly
- remove or seal any remaining debug-only routes
- document minimum production security settings

### 4. Import And Export Stability

Goal: protect the highest-risk operational workflows.

- test large import and export inputs
- keep ODM schema validation in the gate
- verify OC2-0 and OC2-1 contract behavior
- review retry, failure details, artifact cleanup, and download expiry
- add operational logging around job lifecycle transitions

### 5. Frontend Product Stability

Goal: improve repeated operator workflows without changing architecture.

- verify SPA refresh behavior under `/app/*`
- standardize 401, 403, 404, and 500 handling
- tighten table pagination, filtering, loading, and empty states
- review Chinese and English UI text for layout overflow
- continue migrating raw fetch callers to typed API clients

### 6. Runtime And Deploy Operations

Goal: make bare-host operation predictable.

- dry-run and document deploy/rollback paths
- verify reverse proxy configuration
- document required environment variables
- check health, readiness, and liveness endpoints
- document backup and restore expectations

### 7. Observability

Goal: make failures diagnosable without attaching a debugger.

- add request correlation identifiers
- structure logs around import/export jobs
- expose meaningful actuator health details
- add metrics for asynchronous job queues and failures
- define frontend error reporting expectations

## Product Backlog Candidates

### Study Management

- study and site management UI completion
- role authorization UI
- study context switching polish
- study status and configuration review screens

### CRF And Data Capture

- CRF preview and editor improvements
- event CRF data-entry workflow
- discrepancy note workflow completion
- field-level validation and audit presentation

### Subject And Event Workflow

- subject enrollment flow
- visit schedule and status board
- visit progress dashboard
- subject/event search and filtering polish

### Rule Engine

- rule builder UI
- rule validation and dry-run
- rule execution audit trail
- safer expression authoring feedback

### Dataset And Export

- dataset builder
- export templates
- export history search
- download permission and expiry policy
- artifact cleanup observability

### Randomization

- randomization configuration UI
- stratification and block algorithm display
- allocation audit trail
- emergency unblinding permission model

### Questionnaire Service

- questionnaire management UI
- scoring result presentation
- binding to study/subject context
- token or session integration strategy with the main app

### OpenAPI And Typed API Expansion

- expand the randomization generated-type pilot to more modules
- continue removing raw fetch usage
- add generated-contract staleness checks
- define breaking-change review rules

## Guardrails

1. Do not reopen broad legacy-removal work unless a concrete regression is found.
2. Do not mix release tagging with hardening cleanup unless explicitly requested.
3. Do not edit released migration files as part of routine cleanup.
4. Keep verification and product backlog slices separate.
5. Prefer small, reviewable commits per area after Phase 18.
6. Treat `origin/master..HEAD` publication as a separate decision from verification.

## Suggested Immediate Next Step

Create `phase-18-post-cleanup-verification-baseline-plan.md` with a narrow scope:

1. mark Phase 17 complete
2. update the refactor index
3. run verification only
4. do not commit unless the user explicitly asks
5. do not tag or prepare an RC
