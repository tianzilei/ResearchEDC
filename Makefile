# ============================================================================
# ResearchEDF — Makefile
#
# Common development and deployment commands.
# Usage: make <target>
# ============================================================================

SHELL := /bin/bash
.DEFAULT_GOAL := help

# ---- Configuration ----
COMPOSE_DEV   := deploy/compose/docker-compose.dev.yml
COMPOSE_TEST  := deploy/compose/docker-compose.test.yml
COMPOSE_PROD  := deploy/compose/docker-compose.prod.yml
TAG           ?= 0.1

.PHONY: help build docker-build smoke-test test scan \
        up-dev down-dev up-test down-test \
        db-init db-validate \
        backup restore release \
        logs shell clean

# ---- General ----
help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# ---- Build ----
build: ## Compile all modules (Maven)
	bash scripts/build.sh

docker-build: ## Build Docker images
	TAG=$(TAG) bash scripts/docker-build.sh

# ---- Test ----
test: ## Run Maven tests
	bash scripts/build.sh --skip-enforce

smoke-test: ## Run Docker Compose smoke test
	bash scripts/smoke-test.sh

scan: ## Vulnerability scan (requires Trivy)
	TAG=$(TAG) bash scripts/scan.sh

# ---- Database ----
db-init: ## Initialize database schema in Docker
	bash scripts/db-init-schema.sh

db-validate: ## Validate Hibernate schema in Docker
	bash scripts/db-schema-validate.sh

# ---- Development ----
up-dev: ## Start development stack
	docker compose -f $(COMPOSE_DEV) up --build -d

down-dev: ## Stop development stack
	docker compose -f $(COMPOSE_DEV) down

logs-dev: ## Tail development logs
	docker compose -f $(COMPOSE_DEV) logs -f

# ---- Testing Environment ----
up-test: ## Start test stack
	docker compose -f $(COMPOSE_TEST) up --build -d

down-test: ## Stop test stack
	docker compose -f $(COMPOSE_TEST) down -v

# ---- Production ----
up-prod: ## Start production stack
	docker compose -f $(COMPOSE_PROD) up -d

down-prod: ## Stop production stack
	docker compose -f $(COMPOSE_PROD) down

logs-prod: ## Tail production logs
	docker compose -f $(COMPOSE_PROD) logs -f

# ---- Backup & Restore ----
backup: ## Create backup
	bash scripts/backup.sh

restore: ## Restore backup (usage: make restore TS=20260517_120000)
	bash scripts/restore.sh $(TS)

# ---- Release ----
release: ## Create a release (usage: make release VERSION=3.18.0)
	bash scripts/release.sh $(VERSION)

# ---- Developer Utilities ----
logs: ## Show web container logs
	docker compose -f $(COMPOSE_DEV) logs web

shell: ## Open shell in web container
	docker compose -f $(COMPOSE_DEV) exec web bash

psql: ## Open PostgreSQL shell
	docker compose -f $(COMPOSE_DEV) exec postgres psql -U researchedf -d researchedf

clean: ## Remove all build artifacts
	rm -rf legacy-core/target web/target ws/target
	docker compose -f $(COMPOSE_DEV) down -v
