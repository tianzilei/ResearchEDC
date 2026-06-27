# ResearchEDC

ResearchEDC is an independently maintained research electronic data capture
(EDC) and clinical data management (CDM) platform derived from OpenClinica
v3.x.

It now runs as a modernized baseline with:

- a Java 21 Spring Boot modular monolith in `app/`
- a React 19 SPA in `frontend/`
- a Python questionnaire service in `questionnaire-service/`
- resource-only shared support in `shared/` for migrations, i18n, and ODM/XSD
  assets

> Disclaimer: This repository is provided as-is for learning and research. It
> is not suitable for production clinical use without independent validation,
> security review, operational hardening, and regulatory assessment.

## Current State

- Refactor/removal program: complete
- `web/` legacy UI module: removed
- `ws/` SOAP module: absent
- `shared/src/main/java`: `0` Java files
- Current active planning entry point: `docs/edc-convergence/`
- Repository history: cleaned and repacked to remove old `target-old` binary
  baggage

Current verification baseline:

- Backend compile: passing
- Modulith verification: passing
- Export tests: passing
- Frontend typecheck/lint/tests: passing
- Questionnaire service tests: passing

## Architecture

| Area | Location | Role |
|---|---|---|
| Spring Boot app | `app/` | Main application entry point, security, config, and REST surface |
| Modulith modules | `app/src/main/java/org/researchedc/module/` | Study, subject, event, data capture, CRF, export, audit, randomization, identity, dashboard, rule, dataset, filter, subject group, discrepancy note, and OpenRosa |
| Shared resources | `shared/` | Liquibase migrations, i18n bundles, ODM/XSD/XSLT templates |
| React SPA | `frontend/` | `/app/*` user interface |
| Questionnaire service | `questionnaire-service/` | FastAPI service for questionnaire templates, assignments, responses, scoring, and exports |
| Deployment | `deploy/`, `deploy-bare.sh`, `deploy-docker.sh` | Bare-host and Docker deployment scripts plus reverse-proxy / observability config |

Runtime stack:

- Java 21, Spring Boot 3.5.2, Spring Framework 6.2.8, Spring Security 6,
  Hibernate ORM 6.4.4, Liquibase
- React 19, TypeScript 5.8 strict mode, Vite 6, Ant Design 5, TanStack Query 5
- Python FastAPI, SQLAlchemy, Pydantic v2

## Quick Start

Setup and environment notes:

- [docs/SETUP.md](./docs/SETUP.md)
- [docs/HOST_DEPLOYMENT.md](./docs/HOST_DEPLOYMENT.md)

Common verification commands:

```bash
# Backend
mvn clean compile -DskipTests
mvn test -pl app -am -Dtest=ModulithVerificationTest -Dsurefire.failIfNoSpecifiedTests=false
mvn test -pl app -am -Dtest=OdmExportGeneratorTest,ExportArtifactWriterTest -Dsurefire.failIfNoSpecifiedTests=false

# Frontend
pnpm -C frontend install
pnpm -C frontend typecheck
pnpm -C frontend lint
pnpm -C frontend test --run

# Questionnaire service
cd questionnaire-service/apps/api
uv run python -m pytest app/tests/ -v
```

## Documentation

Start here:

- [AGENTS.md](./AGENTS.md) - current project knowledge base
- [docs/edc-convergence/README.md](./docs/edc-convergence/README.md) - next
  active planning area for product audit and convergence
- [docs/refactor/final-refactor-summary.md](./docs/refactor/final-refactor-summary.md) -
  concise summary of the completed refactor/removal program
- [MODIFICATIONS.md](./MODIFICATIONS.md) - chronological change log

Area-specific notes:

- [app/AGENTS.md](./app/AGENTS.md)
- [shared/AGENTS.md](./shared/AGENTS.md)
- [frontend/AGENTS.md](./frontend/AGENTS.md)
- [questionnaire-service/AGENTS.md](./questionnaire-service/AGENTS.md)

## Roadmap

The refactor/removal phase is closed. The next workstream is no longer legacy
cleanup; it is product audit and convergence:

1. `docs/edc-convergence/phase-0-full-product-audit-report.md`
2. `docs/edc-convergence/phase-1-edc-usability-convergence-plan.md`
3. `docs/product/researchedc-final-open-source-modular-plan.md`

## Origin And License

ResearchEDC is derived from OpenClinica v3.x and remains subject to GNU LGPL
obligations for derived code.

- Upstream project: OpenClinica
- Upstream license: GNU LGPL 2.1 or later
- ResearchEDC is not an official OpenClinica release and is not affiliated
  with, endorsed by, or sponsored by OpenClinica
