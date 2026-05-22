
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
- **JWT Authentication**: Stateless token-based auth
- **API Documentation**: Full Swagger / OpenAPI spec

---

## 🏗️ Architecture Overview

### Request Flow:

```
Client (Artisan Dashboard / Consumer Scan)
                ↓
    Spring Security (JWT / Stateless)
                ↓
        REST Controller
                ↓
        Service Layer
                ↓
        └→ PostgreSQL (JPA / Hibernate + Flyway)
```

### Key Architectural Decisions:

1. **Spring Security + JWT**: Stateless token-based authentication
   - **Why?** No server-side session state, scales horizontally

2. **Flyway Migrations**: Versioned database schema
   - **Why?** DPP data models evolve with ESPR regulation — migrations must be auditable

3. **ESPR Compliance Layer**: Business rules enforcing regulation requirements
   - **Why?** The EU ESPR regulation mandates specific data fields and formats for DPPs

---

## 🛠️ Tech Stack

### Backend
- **Spring Boot 3.4.5**: Main framework
- **Java 21**: LTS version
- **Spring Security + JWT (JJWT 0.12.6)**: Authentication & authorization
- **Spring Validation**: Input validation

### Database
- **PostgreSQL 17**: Primary database
- **Flyway**: Schema migrations
- **Hibernate / JPA**: ORM

### Infrastructure
- **Docker + Docker Compose**: Containerization
- **pgAdmin 4**: Database management UI

### Development Tools
- **Spring DevTools**: Hot reload
- **Swagger / SpringDoc 2**: API documentation
- **Testcontainers**: Integration testing with real containers
- **Lombok**: Boilerplate reduction

---

## 🚀 Getting Started

### Prerequisites

- Docker Desktop ([Download](https://www.docker.com/products/docker-desktop))
- Java 21+
- Maven (or use `./mvnw`)

### Step 1: Clone the Repository

```bash
git clone <repo-url>
cd lumiris-backend
```

### Step 2: Configure Environment

```bash
cp .env.example .env
```

Edit `.env` with your values:

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/lumiris
SPRING_DATASOURCE_USERNAME=
SPRING_DATASOURCE_PASSWORD=

# CORS (your frontend URL)
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173

# JWT
JWT_SECRET=
```

### Step 3: Start Development Environment

```bash
make start
```

This will start PostgreSQL and pgAdmin in Docker. Flyway migrations run automatically on first app startup.

### Step 4: Start the API
```bash
make mvn clean install && make mvn spring-boot:run
```

### Step 5: Verify Everything Works

| URL | Service |
|-----|---------|
| `http://localhost:8080/swagger-ui/index.html` | Swagger UI |
| `http://localhost:5050` | pgAdmin (admin@lumiris.com / admin) |
| `http://localhost:8080/actuator/health` | Health check |

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
curl -X POST http://localhost:8080/api/auth/login \
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

## Development Workflow

Docker commands (`make start`, `make stop`, etc.) s'exécutent directement. Les groupes de référence (`make maven`, `make flyway`, `make test`) affichent les commandes à taper. `make mvn <args>` exécute n'importe quelle commande Maven avec les variables d'environnement du `.env` chargées automatiquement.

### Docker Compose

| Command | Description |
|---------|-------------|
| `docker compose up -d` | Start containers in background |
| `docker compose up --build` | Build images and start containers |
| `docker compose stop` | Stop containers without removing them |
| `docker compose down` | Stop and remove containers |
| `docker compose down -v` | Remove containers and volumes (full reset) |
| `docker compose ps` | Show running containers |
| `docker compose logs -f postgres` | Follow PostgreSQL logs |
| `docker compose exec postgres sh` | Open a shell inside the PostgreSQL container |

### Maven

| Command | Description |
|---------|-------------|
| `make mvn spring-boot:run` | Run the application locally |
| `make mvn compile` | Compile source code |
| `make mvn clean package -DskipTests` | Build the JAR without running tests |
| `make mvn clean` | Delete build artifacts (`target/`) |
| `make mvn dependency:resolve` | Download all declared dependencies |
| `make mvn dependency:tree` | Print the full dependency tree |

### Flyway

| Command | Description |
|---------|-------------|
| `make mvn flyway:info` | Show current migration status (applied, pending) |
| `make mvn flyway:migrate` | Apply all pending migrations |
| `make mvn flyway:validate` | Check that applied migrations match scripts on disk |
| `make mvn flyway:repair` | Repair the schema history after a failed migration |
| `make mvn flyway:clean` | Drop all database objects — destroys all data |

### Tests

| Command | Description |
|---------|-------------|
| `make mvn test` | Run all tests |
| `make mvn test -Dgroups=unit` | Run only tests tagged `@Tag("unit")` |
| `make mvn test -Dgroups=integration` | Run only integration tests (requires Docker) |
| `make mvn test -Dtest=MyClassTest` | Run a single test class |
| `make mvn test jacoco:report` | Run tests and generate HTML coverage report (`target/site/jacoco/index.html`) |

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
make mvn test                           # Run all tests
make mvn test -Dgroups=unit             # Unit tests only
make mvn test -Dgroups=integration      # Integration tests (uses Testcontainers)
make mvn test jacoco:report             # HTML report → target/site/jacoco/index.html
```

Integration tests use **Testcontainers** — a real PostgreSQL instance spins up automatically, no manual setup needed.

---

## 📚 API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

---

## 🤝 Contributing

1. **Create a feature branch**: `git checkout -b feature/your-feature`
2. **Make your changes**
3. **Run tests**: `make mvn test`
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
| Start containers | `make start` |
| Stop everything | `make down` |
| Run the app | `make run` |
| Run tests | `make mvn test` |
| View API docs | `http://localhost:8080/swagger-ui/index.html` |
| Open database UI | `http://localhost:5050` |
| Reset database | `make mvn flyway:clean` then `make mvn flyway:migrate` |

---

## 📝 License

This project is proprietary and confidential. All rights reserved © Lumiris.
