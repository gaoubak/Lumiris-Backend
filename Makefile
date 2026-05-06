include .env
ifneq ("$(wildcard .env.local)", "")
	include .env.local
endif

# Variables
DC            := docker compose
EXEC          := $(DC) exec -T
EXEC_IT       := $(DC) exec
APP           := $(EXEC) app
APP_IT        := $(EXEC_IT) app
MVN           := ./mvnw
MAVEN         := $(APP) ./mvnw

.DEFAULT_GOAL := help
.PHONY: help

## —— 🎯 Main Commands ——————————————————————————————————————
help: ## Shows this help message
	@echo "\033[33mUsage:\033[0m"
	@echo "  make [command]"
	@echo ""
	@echo "\033[33mAvailable commands:\033[0m"
	@grep -E '(^[a-zA-Z0-9_-]+:.*?##.*$$)|(^##)' Makefile | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[32m%-30s\033[0m %s\n", $$1, $$2}' | sed -e 's/\[32m##/[33m/'

## —— 🐳 Docker ———————————————————————————————————————————————
build: ## Build Docker containers
	@echo "🔨 Building containers..."
	@$(DC) build --no-cache

start: ## Start all containers in background
	@echo "🚀 Starting containers..."
	@$(DC) up -d
	@echo "✅ Containers started!"

up: ## Build and start all containers
	@echo "🚀 Building and starting containers..."
	@$(DC) up --build

stop: ## Stop all containers
	@echo "⏸️  Stopping containers..."
	@$(DC) stop

down: ## Stop and remove containers
	@echo "🗑️  Removing containers..."
	@$(DC) down

restart: stop start ## Restart all containers

ps: ## Show running containers
	@$(DC) ps

logs: ## Show app container logs (ctrl+c to exit)
	@$(DC) logs -f app

logs-postgres: ## Show PostgreSQL container logs
	@$(DC) logs -f postgres

logs-redis: ## Show Redis container logs
	@$(DC) logs -f redis

ssh: ## SSH into app container
	@$(APP_IT) sh

ssh-postgres: ## SSH into PostgreSQL container
	@$(EXEC_IT) postgres sh

ssh-redis: ## SSH into Redis container
	@$(EXEC_IT) redis sh

## —— ☕ Maven ————————————————————————————————————————————————
install: ## Install Maven dependencies
	@echo "📦 Installing dependencies..."
	@$(MVN) dependency:resolve

package: ## Build JAR (skip tests)
	@echo "📦 Packaging application..."
	@$(MVN) clean package -DskipTests

compile: ## Compile source code
	@echo "🔨 Compiling..."
	@$(MVN) compile

clean: ## Clean build artifacts
	@echo "🧹 Cleaning..."
	@$(MVN) clean

dependency-tree: ## Show dependency tree
	@$(MVN) dependency:tree

dependency-updates: ## Check for dependency updates
	@$(MVN) versions:display-dependency-updates

## —— 🗄️  Database ————————————————————————————————————————————
db-migrate: ## Run Flyway migrations
	@echo "📊 Running migrations..."
	@$(MVN) flyway:migrate

db-info: ## Show Flyway migration status
	@echo "📊 Migration info..."
	@$(MVN) flyway:info

db-validate: ## Validate Flyway migrations
	@$(MVN) flyway:validate

db-repair: ## Repair Flyway schema history
	@echo "🔧 Repairing schema history..."
	@$(MVN) flyway:repair

db-clean: ## Clean database (CAUTION! drops all objects)
	@echo "⚠️  Cleaning database..."
	@$(MVN) flyway:clean

db-reset: db-clean db-migrate ## Reset database (clean + migrate)

## —— 🧪 Testing ——————————————————————————————————————————————
test: ## Run all tests
	@echo "🧪 Running tests..."
	@$(MVN) test

test-unit: ## Run unit tests only
	@echo "🧪 Running unit tests..."
	@$(MVN) test -Dgroups=unit

test-integration: ## Run integration tests only
	@echo "🧪 Running integration tests..."
	@$(MVN) test -Dgroups=integration

test-coverage: ## Run tests with coverage report
	@echo "📊 Generating coverage report..."
	@$(MVN) test jacoco:report
	@echo "✅ Coverage report: target/site/jacoco/index.html"

test-class: ## Run a specific test class (use: make test-class class=UserServiceTest)
	@$(MVN) test -Dtest=$(class)

## —— ✨ Code Quality —————————————————————————————————————————
checkstyle: ## Run Checkstyle
	@echo "🔍 Running Checkstyle..."
	@$(MVN) checkstyle:check

spotbugs: ## Run SpotBugs static analysis
	@echo "🔬 Running SpotBugs..."
	@$(MVN) spotbugs:check

quality: checkstyle spotbugs test ## Run all quality checks

## —— 🔴 Redis ————————————————————————————————————————————————
redis-cli: ## Access Redis CLI
	@$(EXEC_IT) redis redis-cli

redis-flush: ## Flush all Redis data (CAUTION!)
	@echo "⚠️  Flushing all Redis data..."
	@$(EXEC) redis redis-cli FLUSHALL
	@echo "✅ Redis flushed"

redis-keys: ## Show all Redis keys
	@$(EXEC) redis redis-cli KEYS "*"

redis-monitor: ## Monitor Redis commands in real-time
	@$(EXEC_IT) redis redis-cli MONITOR

redis-ping: ## Test Redis connection
	@$(EXEC) redis redis-cli PING

redis-memory: ## Show Redis memory usage
	@$(EXEC) redis redis-cli INFO memory | grep "used_memory_human"

## —— 🔑 Security —————————————————————————————————————————————
audit: ## Check for security vulnerabilities in dependencies
	@echo "🔐 Running security audit..."
	@$(MVN) dependency-check:check

## —— ℹ️  Information ————————————————————————————————————————
info: ## Show Java and Maven versions
	@echo "📋 System Information:"
	@java --version
	@$(MVN) --version

status: ## Show Docker and application status
	@echo "📊 Docker Status:"
	@$(DC) ps
	@echo ""
	@echo "🔴 Redis Status:"
	@$(EXEC) redis redis-cli PING || echo "❌ Redis not responding"

## —— 🚀 Quick Setup ——————————————————————————————————————————
dev: ## Start all containers with hot reload (Docker dev profile)
	@echo "🚀 Starting dev environment with hot reload..."
	@$(DC) --profile dev up

setup: ## Initial project setup
	@echo "🚀 Setting up project..."
	@make build
	@make start
	@echo "✅ Setup complete! App running on http://localhost:8080"
	@echo "✅ pgAdmin running on http://localhost:5050"

fresh: ## Fresh install (reset everything including volumes)
	@echo "🔄 Fresh install..."
	@$(DC) down -v
	@make build
	@make start
	@echo "✅ Fresh install complete!"
