# Lumiris Backend API

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.5-6DB33F.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00.svg?style=flat&logo=openjdk)](https://openjdk.org)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED.svg?style=flat&logo=docker)](https://www.docker.com)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-336791.svg?style=flat&logo=postgresql)](https://www.postgresql.org)

> Backend API of **LUMIRIS** — Digital Product Passport platform for French textile artisans. Built with Java 21, Spring Boot 3, PostgreSQL & DPP/ESPR compliance.

---

## 📖 Table of Contents

- [What is Lumiris?](#-what-is-lumiris)
- [Architecture Overview](#-architecture-overview)
- [Tech Stack](#-tech-stack)
- [Getting Started](#-getting-started)
- [Project Structure](#-project-structure)
- [Development Workflow](#-development-workflow)
- [Contributing](#-contributing)

---

## 🎯 What is Lumiris?

**Lumiris** is a Digital Product Passport (DPP) platform designed for French textile artisans, enabling full traceability and transparency of textile products in compliance with the **European ESPR regulation** (Ecodesign for Sustainable Products Regulation).

### What is a Digital Product Passport?

A **DPP** is a digital record attached to a physical product that contains all information about its lifecycle:

- **Origin**: Where the raw materials come from
- **Manufacturing**: Who made it, where, and how
- **Composition**: Materials, certifications, environmental impact
- **Repairability**: Instructions and spare parts availability
- **End of life**: Recycling and disposal guidelines

### Who uses Lumiris?

- **Textile artisans**: Create and manage their product passports
- **Consumers**: Scan a QR code to access full product traceability
- **Regulators**: Verify ESPR compliance for products sold in the EU

### Key Features:

- **DPP Management**: Create, update and publish product passports
- **ESPR Compliance**: Built to meet EU regulation requirements
- **QR Code Generation**: Each product gets a scannable passport
- **OAuth2 Authentication**: Secure login for artisans
- **AI Document Analysis**: Extract product data from PDF documents via Spring AI + OpenAI
- **Session Management**: Redis-backed persistent sessions
- **API Documentation**: Full Swagger / OpenAPI spec

---

## 🏗️ Architecture Overview

### Request Flow:

```
Client (Artisan Dashboard / Consumer Scan)
                ↓
    Spring Security (OAuth2 / Session)
                ↓
        REST Controller
                ↓
        Service Layer
                ↓
        ├→ PostgreSQL (JPA / Hibernate + Flyway)
        ├→ Redis (Session store)
        └→ OpenAI API (Spring AI — PDF & document analysis)
```

### Key Architectural Decisions:

1. **Spring Security + OAuth2**: Authentication via external providers (Google, etc.)
   - **Why?** No password management, secure by default for artisans

2. **Flyway Migrations**: Versioned database schema
   - **Why?** DPP data models evolve with ESPR regulation — migrations must be auditable

3. **Redis Sessions**: Persistent sessions across restarts
   - **Why?** Stateless app, sessions survive container restarts in production

4. **Spring AI + OpenAI**: PDF document reading and data extraction
   - **Why?** Artisans can upload existing product documents and auto-fill passport fields

5. **ESPR Compliance Layer**: Business rules enforcing regulation requirements
   - **Why?** The EU ESPR regulation mandates specific data fields and formats for DPPs

---

## 🛠️ Tech Stack

### Backend
- **Spring Boot 3.4.5**: Main framework
- **Java 21**: LTS version
- **Spring Security + OAuth2**: Authentication & authorization
- **Spring AI 1.0.0**: OpenAI integration & PDF document reader
- **Spring Validation**: Input validation

### Database
- **PostgreSQL 17**: Primary database
- **Flyway**: Schema migrations
- **Hibernate / JPA**: ORM

### Infrastructure
- **Redis 7**: Session storage
- **Docker + Docker Compose**: Containerization
- **pgAdmin 4**: Database management UI

### Development Tools
- **Spring DevTools**: Hot reload
- **Swagger / SpringDoc 2**: API documentation
- **Testcontainers**: Integration testing with real containers
- **Lombok**: Boilerplate reduction

---

## 🚀 Getting Started

### Repo layout (3 sibling repos)

```
~/Dev/Lumiris/
├── Lumiris-Front/     # Bun + Turbo monorepo (4 Next.js apps)
├── Lumiris-Backend/   # ← this repo
└── Lumiris-Infra/     # Docker stack (Postgres, Redis, MinIO, Traefik, monitoring)
```

The local infrastructure (Postgres, Redis, pgAdmin, MinIO, Mailhog, Traefik, monitoring) has moved to **`../Lumiris-Infra/`**. This repo only ships the Spring Boot app; it expects the datastores to already be running.

### Prerequisites

- Java 21+
- Maven (or use `./mvnw`)
- The Lumiris-Infra stack — see `../Lumiris-Infra/docs/LOCAL.md` for prerequisites (Docker, mkcert, tmux, …)

### Step 1: Boot the local infrastructure

```bash
cd ../Lumiris-Infra
make check          # one-shot prereq sanity
make setup          # /etc/hosts + mkcert + .env
make up             # Postgres + Redis + MinIO + Mailhog + Traefik
```

Or — to start infra + backend + fronts in one command (tmux session) :

```bash
cd ../Lumiris-Infra && make all-up
tmux attach -t lumiris
```

### Step 2: Configure backend environment

```bash
cd ../Lumiris-Backend
cp .env.example .env
```

The defaults in `.env.example` match `../Lumiris-Infra/local/.env`. Full reference:

| Variable | Default | Notes |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/lumiris` | Postgres JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `lumiris` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `lumiris_local_dev_only` | DB password |
| `SPRING_DATA_REDIS_HOST` | `localhost` | Redis host |
| `SPRING_DATA_REDIS_PORT` | `6379` | Redis port |
| `SPRING_DATA_REDIS_PASSWORD` | `lumiris_local_dev_only` | Redis password (set on `redis-server --requirepass`) |
| `SPRING_DATA_REDIS_SSL` | `false` | Set `true` in prod / Cloudflare R2 etc. |
| `STORAGE_S3_ENDPOINT` | `http://localhost:9000` | MinIO endpoint (S3 wire-protocol) |
| `STORAGE_S3_REGION` | `eu-central-1` | Region label (free-form for MinIO) |
| `STORAGE_S3_ACCESS_KEY_ID` | `lumiris_app` | MinIO app credentials |
| `STORAGE_S3_SECRET_ACCESS_KEY` | `lumiris_app_secret_dev_only` | MinIO app credentials |
| `STORAGE_S3_PATH_STYLE` | `true` | Path-style required for MinIO |
| `STORAGE_S3_BUCKET_UPLOADS` | `lumiris-uploads` | User uploads bucket |
| `STORAGE_S3_BUCKET_ASSETS` | `lumiris-assets` | Static assets bucket |
| `STORAGE_S3_BUCKET_BACKUPS` | `lumiris-backups` | Backups bucket |
| `MAIL_HOST` | `localhost` | SMTP host (Mailhog) |
| `MAIL_PORT` | `1025` | SMTP port |
| `JWT_SECRET` | (placeholder) | Must be ≥ 256 bits |
| `OPENAI_API_KEY` | `sk-fake-…` | Optional, Spring AI fallback |
| `CORS_ALLOWED_ORIGINS` | `https://lumiris.local,…` | Comma-separated origins |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | `http://localhost:4318` | otel-collector HTTP receiver |
| `OTEL_SAMPLING` | `1.0` | Trace sampling probability |

> **No AWS dependency.** Object storage uses the official MinIO Java SDK (`io.minio:minio`), which speaks the S3 wire protocol but does not depend on any Amazon library. The same client targets MinIO locally and Cloudflare R2 in production.

### Step 3: Run Spring Boot

```bash
./mvnw spring-boot:run
```

Hot reload is on via Spring DevTools. The backend is then reachable both directly (`http://localhost:8080`) and via Traefik (`https://api.lumiris.local`).

### Step 4: Verify Everything Works

| URL                                                | Service                                  |
| -------------------------------------------------- | ---------------------------------------- |
| `http://localhost:8080/actuator/health`            | Aggregate health (direct)                |
| `http://localhost:8080/actuator/health/readiness`  | Readiness probe (db + redis + storage)   |
| `http://localhost:8080/actuator/health/liveness`   | Liveness probe (ping + disk)             |
| `http://localhost:8080/actuator/prometheus`        | Prometheus scrape endpoint               |
| `https://api.lumiris.local/actuator/health`        | Aggregate health (via Traefik)           |
| `http://localhost:8080/swagger-ui/index.html`      | Swagger UI                               |
| `https://pgadmin.lumiris.local`                    | pgAdmin (`make up-tools` in Infra repo)  |

---

## 🌐 Endpoints

Public (no auth) :

- `POST /api/auth/login` — exchange credentials for a JWT
- `POST /api/telemetry/web-vitals` — Web Vitals ingestion (rate-limited 100 req/min/IP, no auth)
- `GET  /actuator/health` / `health/readiness` / `health/liveness`
- `GET  /actuator/prometheus` — Prometheus metrics (Micrometer + OTLP)

JWT-protected :

- `POST /api/storage/upload-url` — returns a presigned PUT URL (15 min TTL) for the requested bucket alias (`uploads` / `assets` / `backups`)
- `GET  /api/storage/download-url?bucket=…&key=…` — returns a presigned GET URL (1 h TTL)

### Health composition

`/actuator/health/readiness` aggregates :

- `db` — Hikari datasource probe
- `redis` — Lettuce `PING`
- `storage` — MinIO `bucketExists()` on the `uploads` bucket, with `endpoint` + `latencyMs` in details

`/actuator/health/liveness` aggregates `ping` + `diskSpace`.

---

## 🔐 Seed Accounts

The database is pre-seeded with one account per role for local development:

| Role | Email | Password |
|------|-------|----------|
| `ADMIN` | `admin@lumiris.com` | `admin123` |
| `ARTISAN` | `artisan@lumiris.com` | `artisan123` |
| `CLIENT` | `client@lumiris.com` | `client123` |
| `REPAIRER` | `repairer@lumiris.com` | `repairer123` |

To get a JWT token:

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@lumiris.com","password":"admin123"}'
```

---

## 📁 Project Structure

```
lumiris-backend/
├── src/
│   ├── main/
│   │   ├── java/com/minoh/lumiris_backend/
│   │   │   ├── config/          # Security, CORS configuration
│   │   │   ├── controller/      # REST endpoints (DPP, artisan, product)
│   │   │   ├── service/         # Business logic & ESPR compliance rules
│   │   │   ├── entity/          # JPA entities (Product, Passport, Artisan...)
│   │   │   ├── repository/      # Spring Data repositories
│   │   │   └── dto/             # Data Transfer Objects
│   │   └── resources/
│   │       ├── application.yaml      # App configuration
│   │       └── db/migration/         # Flyway migrations (V1__*.sql)
│   └── test/                         # Tests (JUnit 5 + Testcontainers)
├── docker-compose.yaml               # Docker services
├── Dockerfile                        # Multi-stage production build
├── Makefile                          # Developer commands
├── .env                              # Local environment variables
└── .env.example                      # Environment template
```

---

## 💻 Development Workflow

### Common Commands

```bash
make help               # Show all available commands

# Docker
make dev                # Start full dev environment (hot reload)
make up                 # Build and start production containers
make down               # Stop all containers
make logs               # Follow app logs
make ps                 # Show running containers

# Database
make db-info            # Show Flyway migration status
make db-migrate         # Run pending migrations
make db-validate        # Validate migrations
make db-reset           # Reset database (clean + migrate)

# Testing
make test               # Run all tests
make test-unit          # Run unit tests only
make test-integration   # Run integration tests (Testcontainers)
make test-coverage      # Run tests with HTML coverage report

# Redis
make redis-cli          # Open Redis CLI
make redis-flush        # Flush all Redis data
make redis-keys         # Show all Redis keys

# Utilities
make ssh                # SSH into app container
make clean              # Remove containers + volumes + build artifacts
```

### Adding a New DPP Field (ESPR Compliance)

1. **Create a Flyway migration** in `src/main/resources/db/migration/`:

```sql
-- V2__add_espr_repairability_score.sql
ALTER TABLE product_passport
ADD COLUMN repairability_score DECIMAL(3,1),
ADD COLUMN repairability_index VARCHAR(10);
```

2. **Update the Entity**:

```java
@Entity
public class ProductPassport {
    @Column
    private BigDecimal repairabilityScore;

    @Column
    private String repairabilityIndex;
}
```

3. **Add validation** (ESPR requires score between 0 and 10):

```java
@DecimalMin("0.0") @DecimalMax("10.0")
private BigDecimal repairabilityScore;
```

---

## 🧪 Testing

```bash
make test               # Run all tests
make test-unit          # Unit tests only
make test-integration   # Integration tests (uses Testcontainers)
make test-coverage      # HTML report → target/site/jacoco/index.html
```

Integration tests use **Testcontainers** — a real PostgreSQL instance spins up automatically, no manual setup needed.

---

## 📚 API Documentation

- **Swagger UI** (dev): `http://localhost:8081/swagger-ui/index.html`
- **Swagger UI** (prod): `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8081/v3/api-docs`

---

## 🤝 Contributing

1. **Create a feature branch**: `git checkout -b feature/your-feature`
2. **Make your changes**
3. **Run tests**: `make test`
4. **Commit** following [Conventional Commits](https://www.conventionalcommits.org/):
   ```
   feat: add repairability score to product passport
   fix: correct Flyway migration order for ESPR fields
   docs: update DPP compliance requirements
   ```
5. **Push and open a Pull Request**

---

## 🚀 Quick Reference

| Task | Command |
|------|---------|
| Start dev (hot reload) | `make dev` |
| Start production | `make up` |
| Run tests | `make test` |
| View API docs | `http://localhost:8081/swagger-ui/index.html` |
| Open database UI | `http://localhost:5050` |
| SSH into container | `make ssh` |
| View logs | `make logs` |
| Reset database | `make db-reset` |
| Stop everything | `make down` |

---

## 📝 License

This project is proprietary and confidential. All rights reserved © Lumiris.
