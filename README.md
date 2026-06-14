# ResearchEDC

- Version: 0.1
- Last updated: 2026-06-14
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
| Shared legacy core | `shared/` | Remaining legacy beans, DAOs, services, rules, jobs, Hibernate entities, Liquibase migrations |
| Legacy web UI | retired | `web/` is absent; needed import/validation compatibility classes were migrated into `app/` |
| SOAP services | retired | `ws/` is absent from the current tree; keep compatibility audits in the legacy-removal plan |
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

- Overall tracked legacy-removal progress is about **88.1%** by active workflow inventory: 848 of 963 artifacts are removed or closed, with 115 active artifacts remaining. DAO method replacement/removal coverage is **92.9%**: 822 of 885 tracked SPI methods are module-backed or removed, leaving 63 unused rows as the main Phase 3 blocker.
- `legacy-core/` has been consolidated into `shared/` with package rename to `org.researchedc`.
- Legacy code is **not fully removed**. Current baseline still includes `shared/` legacy beans/DAOs/services. The `web/` JSP/SecureController module and `ws/` SOAP module are absent from the current tree.
- Spring XML and Ehcache-era configuration have largely been replaced by Java configuration and modern cache/security wiring.
- Modulith modules exist for study, subject, event, data capture, identity, CRF, export, audit, randomization, dashboard, rule, dataset, filter, subject group, discrepancy note, OpenRosa, and legacy gateway functions.
- React SPA covers major workflows. The legacy frame component remains in the SPA for compatibility, but there are no current `web/` JSP views in the repository.
- Questionnaire service has its own API, data model, scoring engine, and tests.
- `DaoProvider` has been removed; direct `new XxxDAO(...)` / `new StudyConfigService(...)` construction is at 0 active matches across the legacy Java surfaces.
- Legacy DAO consumer work is mostly complete: `DaoProvider` and direct DAO construction are gone, and the target DAO families have been SPI-widened. The DAO implementation files still remain until module-owned replacements are proven under production workflows. The Phase 3 DAO method ledger is checked in and currently classifies 757 of 885 tracked methods as module-backed, with 63 unused rows left to remove, 65 rows already removed, and 0 fallback-SQL, legacy-only, or adapter-gap rows remaining.
- Phase B schema ownership is complete. Remaining legacy deletion work is proving every DAO SPI method and workflow is module-owned, repository-backed, or unused before legacy DAO `.java` files can be deleted.
- Enterprise UI/functionality and active mail-delivery code paths were removed on 2026-06-09. Email/contact fields remain as compatibility data and are tracked by the follow-up email-field removal plan.
- Phase II (@SuppressWarnings elimination) is **COMPLETE**. Reduced from 168 to 72 annotations (57% reduction). Remaining 72 are all genuine (27 non-deferred) or deferred (45 TableFactory, will self-resolve with SPA strangulation).

Current legacy removal baseline:

| Surface | Current Count | Removal Gate |
|---------|---------------|--------------|
| `shared/src/main/java/org/researchedc` | 504 Java files | Shared beans/services/DAO/domain callers replaced or proven unused |
| `shared/dao` | 88 Java files | 98/186 DAO-surface files removed (52.7%); remaining SPI implementations require module-owned repository/service proof; Phase 3 ledger is 757/885 module-backed methods with 63 unused rows left |
| `web/` Java | 0 files | Directory deleted; needed compatibility classes migrated to `app/` |
| JSP pages | 0 files | `web/` views deleted |
| Legacy servlet inventory | 0 artifacts | Servlet workflows migrated, retired, or deleted |
| Active legacy workflow inventory | 115 artifacts | 848/963 artifacts removed or closed (88.1%); every remaining artifact has owner category, replacement proof, or compatibility gate |
| `ws/` Java | 0 files | SOAP module is absent; keep compatibility audit if endpoints reappear |

For detailed handoff notes, see [AGENTS.md](./AGENTS.md), [.sisyphus/LEGACY_REFACTOR_PLAN.md](./.sisyphus/LEGACY_REFACTOR_PLAN.md), and [docs/refactor/remove-legacy-code-plan.md](./docs/refactor/remove-legacy-code-plan.md).

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
| Java module tests | 432/432 passing in latest project handoff |
| Frontend typecheck | 0 errors |
| Frontend tests | 25/25 passing |
| Questionnaire service tests | 39/39 passing |
| Chinese encoding | Full-stack UTF-8 verified |
| Import/Export | REST API + pg_dump verified |
| SPA E2E | Login → Dashboard verified |

## Documentation

- [AGENTS.md](./AGENTS.md) — project knowledge base and current refactor handoff
- [.sisyphus/LEGACY_REFACTOR_PLAN.md](./.sisyphus/LEGACY_REFACTOR_PLAN.md) — legacy strangulation plan and progress
- [docs/refactor/remove-legacy-code-plan.md](./docs/refactor/remove-legacy-code-plan.md) — current legacy baseline and deletion plan
- [MODIFICATIONS.md](./MODIFICATIONS.md) — chronological change log
- [app/AGENTS.md](./app/AGENTS.md) — Spring Boot entry point and Modulith notes
- [shared/AGENTS.md](./shared/AGENTS.md) — shared legacy domain/data-access notes
- [frontend/AGENTS.md](./frontend/AGENTS.md) — React SPA notes
- [questionnaire-service/AGENTS.md](./questionnaire-service/AGENTS.md) — questionnaire service notes

## Origin And License

ResearchEDC is derived from OpenClinica v3.x and remains subject to the GNU LGPL obligations for code derived from OpenClinica.

- Upstream project: OpenClinica
- Upstream license: GNU LGPL, version 2.1 or later
- Initial fork/import: 2023 approximate
- ResearchEDC rename: 2026-05-20

OpenClinica is a trademark of its respective owner. ResearchEDC is not an official OpenClinica release and is not affiliated with, endorsed by, or sponsored by OpenClinica.
