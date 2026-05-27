# ============================================================================
# ResearchEDC — Makefile
#
# Common development and deployment commands.
# Usage: make <target>
# ============================================================================

SHELL := /bin/bash
.DEFAULT_GOAL := help

.PHONY: help setup init-db build start stop restart status logs clean

# ---- General ----
help: ## Show this help
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

# ---- Bare Deploy ----
setup: ## Install host prerequisites
	bash deploy.sh setup

init-db: ## Initialize PostgreSQL databases
	bash deploy.sh init-db

build: ## Build frontend + backend
	bash deploy.sh build

start: ## Start all bare host services
	bash deploy.sh start

stop: ## Stop all bare host services
	bash deploy.sh stop

restart: ## Restart all bare host services
	bash deploy.sh restart

status: ## Show service status
	bash deploy.sh status

logs: ## Tail service logs
	bash deploy.sh logs

clean: ## Remove build artifacts and runtime data
	bash deploy.sh clean
