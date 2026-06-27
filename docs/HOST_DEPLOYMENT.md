# ResearchEDC Bare Host Deployment

Bare host deployment is driven by `deploy-bare.sh` from the repository root.
The Docker path remains available through `deploy-docker.sh`, but the current
convergence baseline treats bare host as the primary operator path.

## Prerequisites

- Java 21
- Maven
- Node.js and `pnpm`
- Docker for local PostgreSQL and MinIO helpers
- PostgreSQL client tools
- Redis
- Caddy
- `uv` for the questionnaire service

`bash deploy-bare.sh setup` installs the expected Debian/Ubuntu packages and
creates the questionnaire Python environment.

## Commands

```bash
bash deploy-bare.sh setup
bash deploy-bare.sh init-db
bash deploy-bare.sh build
bash deploy-bare.sh start
bash deploy-bare.sh status
bash deploy-bare.sh health
bash deploy-bare.sh logs
bash deploy-bare.sh stop
```

Use `restart` after configuration changes. Use `clean` only when you intend to
remove local build artifacts and runtime data under the repository workspace.

## Ports And Routes

Default ports:

| Service | Port | Purpose |
|---|---:|---|
| App | 8080 | Spring Boot app, SPA, REST APIs |
| Questionnaire | 8000 | FastAPI service |
| Caddy | 80 | External reverse proxy |
| PostgreSQL | 5432 | Database |
| MinIO API | 9000 | Object storage |
| MinIO Console | 9001 | Object storage console |

Caddy routes:

| Route | Target |
|---|---|
| `/app/*` | Spring Boot SPA |
| `/api/*` | Spring Boot REST APIs |
| `/q/*` | Questionnaire service |
| `/actuator/*` | Spring Boot actuator, localhost-only through Caddy |

## Health And Readiness

Use:

```bash
bash deploy-bare.sh health
```

The command checks:

- local app readiness at `/actuator/health`
- local questionnaire readiness at `/health`
- reverse-proxy responses for `/app/`, `/api/v1/auth/me`, and `/q/health`

Operator-facing system status in the SPA uses authenticated dashboard APIs:

- `/api/v1/dashboard/status`
- `/api/v1/dashboard/health`

Do not expose `/actuator/*` publicly. It is for local runtime checks and
restricted admin tooling only.

## Logs And Rollback

Runtime logs are under `logs/`:

```bash
tail -f logs/app.log
tail -f logs/questionnaire.log
tail -f logs/caddy.log
```

For a failed rollout:

1. Run `bash deploy-bare.sh stop`.
2. Restore the previous Git revision or artifact.
3. Run `bash deploy-bare.sh build`.
4. Run `bash deploy-bare.sh start`.
5. Confirm `bash deploy-bare.sh health`.
