# ResearchEDC

- Version: 0.1
- Last updated: 2026-05-29
- License: GNU LGPL

ResearchEDC is an independently maintained research electronic data capture (EDC) and clinical data management (CDM) platform derived from OpenClinica v3.x.

The project is an experimental modernization effort for investigator-initiated clinical research workflows. It keeps the legacy OpenClinica data model and workflows available while gradually moving core capabilities into a Spring Boot modular monolith, a React SPA, and a separate questionnaire service.

> **Disclaimer:** This repository is provided as-is for learning and research. It is experimental and is not suitable for production clinical use without independent validation, security review, operational hardening, and regulatory assessment.

## Architecture

ResearchEDC is currently a modular monolith with legacy compatibility layers:

| Area | Location | Purpose |
|------|----------|---------|
| Spring Boot app | `app/` | Application entry point, security/config, Spring Modulith modules |
| Modulith modules | `app/src/main/java/org/researchedc/module/` | Study, subject, event, data capture, CRF, export, audit, randomization, identity, dashboard, and supporting modules |
| Shared legacy core | `shared/` | Legacy beans, DAOs, services, rules, jobs, Hibernate entities, Liquibase migrations |
| Legacy web UI | `web/` | JSP/SecureController workflows kept for compatibility during strangulation |
| SOAP services | `ws/` | Legacy Spring WS endpoints and adapters |
| React SPA | `frontend/` | New `/app/*` application shell and migrated workflows |
| Questionnaire service | `questionnaire-service/` | Python FastAPI service for questionnaire templates, assignments, responses, scoring, and exports |
| Deployment | `deploy/`, `deploy.sh` | Bare-host deployment scripts and reverse-proxy/observability config |

Core runtime stack:

- Java 21, Spring Boot 3.2.x, Spring Framework 6.1.x, Spring Security 6, Hibernate 6, Liquibase
- React 19, TypeScript strict mode, Vite, Ant Design, TanStack Query
- Python FastAPI, SQLAlchemy, Pydantic v2 for the questionnaire service
- PostgreSQL and Oracle support inherited from the OpenClinica lineage

## Refactor Progress

The long-running refactor follows a strangler pattern: keep legacy behavior working, expose or replace workflows through modules, then delete legacy code only after replacement paths are proven.

Current high-level status:

- `legacy-core/` has been consolidated into `shared/` with package rename to `org.researchedc`.
- Spring XML and Ehcache-era configuration have largely been replaced by Java configuration and modern cache/security wiring.
- Modulith modules exist for study, subject, event, data capture, identity, CRF, export, audit, randomization, dashboard, rule, dataset, filter, subject group, discrepancy note, notification, and legacy gateway functions.
- React SPA covers major workflows and keeps remaining JSP pages reachable through the legacy frame path.
- Questionnaire service has its own API, data model, scoring engine, and tests.
- `DaoProvider` has been removed; direct `new XxxDAO(...)` / `new StudyConfigService(...)` construction is at 0 active matches across the legacy Java surfaces.
- The `StudyDAO` / `StudySubjectDAO` / `SubjectDAO` / `UserAccountDAO` consumer family has been widened to SPI interfaces and is now limited to implementation/factory/adapter boundaries.
- Remaining legacy deletion work is mainly replacing concrete DAO families such as `CRFDAO`, extracting DAO-heavy web/ws workflows, and providing module-owned or repository-backed implementations before deleting legacy DAO classes.

Latest documented refactor slice:

- Commit `10f0f6ea2` refactored high-volume legacy DAO consumers to SPI interfaces.
- A follow-up WS CRF slice is in progress, widening selected SOAP/import helpers from `CRFDAO` to `ICrfDAO`.

For detailed handoff notes, see [AGENTS.md](./AGENTS.md) and [.sisyphus/LEGACY_REFACTOR_PLAN.md](./.sisyphus/LEGACY_REFACTOR_PLAN.md).

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
| Frontend typecheck | 0 errors |
| Frontend tests | 25/25 passing |
| Questionnaire service tests | 31/31 passing |

## Documentation

- [AGENTS.md](./AGENTS.md) — project knowledge base and current refactor handoff
- [.sisyphus/LEGACY_REFACTOR_PLAN.md](./.sisyphus/LEGACY_REFACTOR_PLAN.md) — legacy strangulation plan and progress
- [MODIFICATIONS.md](./MODIFICATIONS.md) — chronological change log
- [app/AGENTS.md](./app/AGENTS.md) — Spring Boot entry point and Modulith notes
- [shared/AGENTS.md](./shared/AGENTS.md) — shared legacy domain/data-access notes
- [web/AGENTS.md](./web/AGENTS.md) — legacy web UI notes
- [ws/AGENTS.md](./ws/AGENTS.md) — SOAP service notes
- [frontend/AGENTS.md](./frontend/AGENTS.md) — React SPA notes
- [questionnaire-service/AGENTS.md](./questionnaire-service/AGENTS.md) — questionnaire service notes

## Origin And License

ResearchEDC is derived from OpenClinica v3.x and remains subject to the GNU LGPL obligations for code derived from OpenClinica.

- Upstream project: OpenClinica
- Upstream license: GNU LGPL, version 2.1 or later
- Initial fork/import: 2023 approximate
- ResearchEDC rename: 2026-05-20

OpenClinica is a trademark of its respective owner. ResearchEDC is not an official OpenClinica release and is not affiliated with, endorsed by, or sponsored by OpenClinica.
