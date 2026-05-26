# ============================================================================
# ResearchEDF — Makefile
#
# Common development and deployment commands.
# Usage: make <target>
# ============================================================================

SHELL := /bin/bash
.DEFAULT_GOAL := help

.PHONY: help build test scan backup restore release clean

# ---- General ----
help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# ---- Build ----
build: ## Compile all modules (Maven)
	bash scripts/build.sh

# ---- Test ----
test: ## Run Maven tests
	bash scripts/build.sh --skip-enforce

scan: ## Vulnerability scan (requires Trivy)
	bash scripts/scan.sh

# ---- Database ----
db-init: ## Initialize database schema
	bash scripts/db-init-schema.sh

db-validate: ## Validate Hibernate schema
	bash scripts/db-schema-validate.sh

# ---- Backup & Restore ----
backup: ## Create backup
	bash scripts/backup.sh

restore: ## Restore backup (usage: make restore TS=20260517_120000)
	bash scripts/restore.sh $(TS)

# ---- Release ----
release: ## Create a release (usage: make release VERSION=0.1)
	bash scripts/release.sh $(VERSION)

# ---- Clean ----
clean: ## Remove all build artifacts
	rm -rf legacy-core/target web/target ws/target
