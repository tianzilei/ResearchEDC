# ResearchEDC

- Version: 0.1
- Last updated: 2026-06-26
- License: GNU LGPL

ResearchEDC is an independently maintained research electronic data capture (EDC) and clinical data management (CDM) platform derived from OpenClinica v3.x.

The project is an experimental modernization effort for investigator-initiated clinical research workflows. It keeps the legacy OpenClinica data model and workflows available while gradually moving core capabilities into a Spring Boot modular monolith, a React SPA, and a separate questionnaire service.

> **Disclaimer:** This repository is provided as-is for learning and research. It is experimental and is not suitable for production clinical use without independent validation, security review, operational hardening, and regulatory assessment.

## Architecture

ResearchEDC is currently a modular monolith with legacy compatibility layers:

| Area | Location | Purpose |
|------|----------|---------|
| Spring Boot app | `app/` | Application entry point, security/config, Spring Modulith modules |
| Modulith modules | `app/src/main/java/org/researchedc/module/` | Study, subject, event, data capture, CRF, export, audit, randomization, identity, dashboard, rule, dataset, filter, subject group, discrepancy note, and OpenRosa modules |
| Shared legacy core | `shared/` | Resource-only module: i18n properties, Liquibase migrations, ODM/XSD templates |
| Legacy web UI | retired | `web/` is absent; needed import/validation compatibility classes were migrated into `app/` |
| SOAP services | retired | `ws/` is absent from the current tree; keep compatibility audits in the legacy-removal plan |
| React SPA | `frontend/` | New `/app/*` application shell and migrated workflows |
| Questionnaire service | `questionnaire-service/` | Python FastAPI service for questionnaire templates, assignments, responses, scoring, and exports |
| Deployment | `deploy/`, `deploy.sh` | Bare-host deployment scripts and reverse-proxy/observability config |

Core runtime stack:

- Java 21, Spring Boot 3.2.x, Spring Framework 6.2.8, Spring Security 6, Hibernate ORM 6.4.4, Liquibase
- React 19, TypeScript strict mode, Vite 6, Ant Design 5, TanStack Query 5
- Python FastAPI, SQLAlchemy, Pydantic v2 for the questionnaire service
- PostgreSQL and Oracle support inherited from the OpenClinica lineage

## Refactor Progress

The long-running refactor follows a strangler pattern: keep legacy behavior working, expose or replace workflows through modules, then delete legacy code only after replacement paths are proven.

Current high-level status:

- Overall tracked legacy-removal progress is **100.0%** by active workflow inventory: 963 of 963 artifacts are removed or closed, with 0 active artifacts remaining. DAO method replacement/removal coverage is **100.0%**: 878 of 878 tracked SPI methods are removed, with 0 unused method-level blockers remaining.
- `legacy-core/` has been consolidated into `shared/` with package rename to `org.researchedc`.
- Legacy code is **not fully removed**. Current baseline still includes `shared/` resource-only files (i18n properties, Liquibase migrations, ODM/XSD templates). The `web/` JSP/SecureController module, `ws/` SOAP module, `shared/domain` Java mappings, `shared/core` Java support, `shared/i18n` Java support, `shared/exception` Java support, `shared/dao` SPI surface, and all `shared/bean` DTOs are absent from the current tree. `shared/src/main/java` contains **0 Java files**.
- Spring XML and Ehcache-era configuration have largely been replaced by Java configuration and modern cache/security wiring.
- Modulith modules exist for study, subject, event, data capture, identity, CRF, export, audit, randomization, dashboard, rule, dataset, filter, subject group, discrepancy note, and OpenRosa.
- React SPA covers major workflows. The legacy frame component remains in the SPA for compatibility, but there are no current `web/` JSP views in the repository.
- Questionnaire service has its own API, data model, scoring engine, and tests.
- `DaoProvider` has been removed; direct `new XxxDAO(...)` / `new StudyConfigService(...)` construction is at 0 active matches across the legacy Java surfaces.
- Legacy DAO consumer work is complete: `DaoProvider`, `LegacyDaoFactory`, `EntityDAO`, direct DAO construction, and the shared DAO SPI files are gone. The Phase 3 DAO method ledger is checked in and currently classifies 878 of 878 tracked methods as removed, with 0 unused rows left to remove and 0 fallback-SQL, legacy-only, or adapter-gap rows remaining.
- Phase B schema ownership is complete. Remaining compatibility hardening lives in module code and uses module-owned ports/repositories rather than legacy SPI names.
- Enterprise UI/functionality and active mail-delivery code paths were removed on 2026-06-09. Email/contact fields remain as compatibility data and are tracked by the follow-up email-field removal plan.
- Phase II (@SuppressWarnings elimination) is **COMPLETE**. Reduced from 168 to 72 annotations (57% reduction). Remaining 72 are all genuine (27 non-deferred) or deferred (45 TableFactory, will self-resolve with SPA strangulation).

Current legacy removal baseline:

| Surface | Current Count | Removal Gate |
|---------|---------------|--------------|
| `shared/src/main/java/org/researchedc` | 0 Java files | Shared compatibility DTO/term beans retired; all DTOs migrated to module-owned DTOs |
| `shared/domain` | 0 Java files | Shared Hibernate mappings retired; active mappings live in module-owned entities |
| `shared/core`, `shared/exception` | 0 Java files | Retired; app-owned config loads retained properties |
| `shared/dao` | 0 files | DAO SPI surface deleted; Phase 3 ledger is 878/878 removed |
| `web/` Java | 0 files | Directory deleted; needed compatibility classes migrated to `app/` |
| JSP pages | 0 files | `web/` views deleted |
| Legacy servlet inventory | 0 artifacts | Servlet workflows migrated, retired, or deleted |
| Active legacy workflow inventory | 0 artifacts | 963/963 artifacts removed or closed (100.0%); current regenerated inventory has no active rows |
| `ws/` Java | 0 files | SOAP module is absent; keep compatibility audit if endpoints reappear |

For detailed handoff notes, see [AGENTS.md](./AGENTS.md), [docs/refactor/refactor-removal-roadmap.md](./docs/refactor/refactor-removal-roadmap.md), and [docs/refactor/remove-legacy-code-plan.md](./docs/refactor/remove-legacy-code-plan.md).

## Verification Snapshot

Recent known-good checks recorded in the project handoff:

```bash
mvn -pl app -am compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
cd frontend && pnpm typecheck && pnpm test --run
cd questionnaire-service/apps/api && python -m pytest app/tests/ -v
```

Current baseline from project notes:

| Check | Status |
|-------|--------|
| Backend compile | Passing |
| Modulith verification | Passing |
| Java module tests | 435/435 passing in latest project handoff |
| Frontend typecheck | 0 errors |
| Frontend tests | 25/25 passing |
| Questionnaire service tests | 40/40 passing |
| Chinese encoding | Full-stack UTF-8 verified |
| Import/Export | REST API + pg_dump verified |
| SPA E2E | Login → Dashboard verified |

## Documentation

- [AGENTS.md](./AGENTS.md) — project knowledge base and current refactor handoff
- [docs/refactor/refactor-removal-roadmap.md](./docs/refactor/refactor-removal-roadmap.md) — active legacy strangulation roadmap
- [docs/refactor/next-refactor-removal-plan.md](./docs/refactor/next-refactor-removal-plan.md) — short continuity snapshot for the remaining compatibility work
- [docs/refactor/remove-legacy-code-plan.md](./docs/refactor/remove-legacy-code-plan.md) — current legacy baseline and deletion plan
- [docs/refactor/post-cleanup-hardening-and-backlog-plan.md](./docs/refactor/post-cleanup-hardening-and-backlog-plan.md) — hardening priorities and product backlog candidates
- [MODIFICATIONS.md](./MODIFICATIONS.md) — chronological change log
- [app/AGENTS.md](./app/AGENTS.md) — Spring Boot entry point and Modulith notes
- [shared/AGENTS.md](./shared/AGENTS.md) — shared legacy domain/data-access notes
- [frontend/AGENTS.md](./frontend/AGENTS.md) — React SPA notes
- [questionnaire-service/AGENTS.md](./questionnaire-service/AGENTS.md) — questionnaire service notes

## Next Steps

The legacy removal refactor is **100% complete**. The project is now transitioning from large-scale refactoring to hardening and product development.

**Current phase:** Phase 18 — Post-Cleanup Verification Baseline (verifying the cleaned repository is still buildable and testable).

**Upcoming phases:**

| Phase | Scope | Goal |
|-------|-------|------|
| Phase 19 | CI and verification hardening | Make the verification baseline repeatable and visible in CI |
| Phase 20 | Security and deploy hardening | Stabilize runtime, permissions, observability, and deploy operations |
| Phase 21 | Product backlog slice 1 | Improve the core study / subject / event / data capture workflow |

**Hardening priorities:**

1. **Verification and CI** — turn the verification matrix into stable CI gates
2. **Database and migration reliability** — keep PostgreSQL and Oracle schema evolution trustworthy
3. **Security and permissions** — review endpoint authorization, CSRF/session behavior, admin endpoints
4. **Import and export stability** — test large inputs, protect ODM schema validation, verify OC2-0/OC2-1 contracts
5. **Frontend product stability** — standardize error handling, tighten table UI, continue typed API client migration
6. **Runtime and deploy operations** — dry-run deploy/rollback paths, document environment variables
7. **Observability** — add request correlation, structured logs, actuator health, job metrics

**Product backlog candidates:** study management UI, CRF editor improvements, subject enrollment flow, rule builder UI, dataset builder, randomization config UI, questionnaire management UI, OpenAPI expansion.

For details, see [docs/refactor/post-cleanup-hardening-and-backlog-plan.md](./docs/refactor/post-cleanup-hardening-and-backlog-plan.md).

## Origin And License

ResearchEDC is derived from OpenClinica v3.x and remains subject to the GNU LGPL obligations for code derived from OpenClinica.

- Upstream project: OpenClinica
- Upstream license: GNU LGPL, version 2.1 or later
- Initial fork/import: 2023 approximate
- ResearchEDC rename: 2026-05-20

OpenClinica is a trademark of its respective owner. ResearchEDC is not an official OpenClinica release and is not affiliated with, endorsed by, or sponsored by OpenClinica.
