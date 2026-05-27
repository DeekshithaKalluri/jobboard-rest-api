<div align="center">

# 💼 Job Board REST API

**A production-style backend microservice for a job board platform**

Built with Spring Boot · Secured with JWT · Tested · Dockerized · CI/CD on every push

[![CI/CD Pipeline](https://github.com/DeekshithaKalluri/jobboard-api/actions/workflows/ci.yml/badge.svg)](https://github.com/DeekshithaKalluri/jobboard-api/actions)
![Java](https://img.shields.io/badge/Java-17-orange?logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-green?logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql)
![Docker](https://img.shields.io/badge/Docker-ready-blue?logo=docker)
![Tests](https://img.shields.io/badge/Tests-15%20passing-brightgreen)

</div>

---

## What Is This?

This is the **backend API** of a job board platform — think the engine behind Indeed or LinkedIn Jobs.

A recruiter can sign up, log in, and post job listings. A job seeker can browse and search listings without needing an account. JWT tokens ensure only the person who posted a job can edit or delete it.

A React or mobile frontend could plug into this API to build a complete, deployable product.

---

## Features

- **JWT Authentication** — stateless login with secure token generation and validation
- **Job CRUD** — create, read, update, delete job listings with ownership enforcement
- **Public search** — search by title or location without an account
- **Input validation** — all request bodies validated with Jakarta Bean Validation
- **15 automated tests** — unit tests with Mockito, integration tests with MockMvc
- **Dockerized** — run the full stack (app + database) with one command
- **CI/CD pipeline** — GitHub Actions runs tests and builds Docker image on every push

---

## Tech Stack

| Category | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3, Spring MVC |
| Security | Spring Security 6, JWT (jjwt 0.12) |
| Database | PostgreSQL 15, Hibernate, Spring Data JPA |
| Testing | JUnit 5, Mockito, MockMvc |
| DevOps | Docker, Docker Compose, GitHub Actions |
| Build | Maven |

---

## Project Structure

    src/main/java/com/jobboard/api/
        controller/   → REST endpoints (AuthController, JobController)
        service/      → Business logic (JobService)
        repository/   → Database queries (UserRepository, JobRepository)
        model/        → Database entities (User, Job)
        security/     → JWT filter, SecurityConfig, UserDetailsService
        dto/          → Request bodies (LoginRequest, RegisterRequest)

    src/test/java/com/jobboard/api/
        JobServiceTest.java                  → Unit tests with Mockito
        AuthControllerIntegrationTest.java   → Integration tests with MockMvc

---

## API Reference

### Auth
| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| POST | `/api/auth/register` | No | Register a new user |
| POST | `/api/auth/login` | No | Login, returns JWT token |

### Jobs
| Method | Endpoint | Auth Required | Description |
|---|---|---|---|
| GET | `/api/jobs` | No | Get all job listings |
| GET | `/api/jobs/{id}` | No | Get one job by ID |
| GET | `/api/jobs/search?title=X` | No | Search by job title |
| GET | `/api/jobs/search?location=X` | No | Search by location |
| POST | `/api/jobs` | Yes | Create a new listing |
| PUT | `/api/jobs/{id}` | Yes | Update your listing |
| DELETE | `/api/jobs/{id}` | Yes | Delete your listing |

---

## Quickstart

### Run with Docker (no setup needed)

```bash
docker-compose up
```

App is live at `http://localhost:8080`. PostgreSQL is included — nothing else to install.

### Run locally

```bash
# Requires PostgreSQL running on localhost:5432
./mvnw spring-boot:run
```

---

## Walkthrough

### 1. Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"jane","email":"jane@example.com","password":"secret123"}'

# {"message":"User registered successfully"}
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"jane","password":"secret123"}'

# {"token":"eyJ...","type":"Bearer","username":"jane"}
```

### 3. Post a Job
```bash
curl -X POST http://localhost:8080/api/jobs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "title": "Software Engineer",
    "company": "TechCorp",
    "description": "Build amazing products",
    "location": "Austin, TX",
    "salary": 120000,
    "jobType": "FULL_TIME"
  }'
```

### 4. Search Jobs (no login needed)
```bash
curl "http://localhost:8080/api/jobs/search?title=engineer"
curl "http://localhost:8080/api/jobs/search?location=Austin"
```

---

## Tests

```bash
./mvnw test
# Tests run: 15, Failures: 0, Errors: 0
```

**JobServiceTest** — unit tests for business logic using Mockito mocks (no database needed)

**AuthControllerIntegrationTest** — full HTTP tests using MockMvc against a real test database

---

## CI/CD Pipeline

Every push to `main` triggers GitHub Actions to:

1. Spin up a real PostgreSQL 15 container
2. Run all 15 tests against it
3. Build the Docker image — only if every test passes

---

## Design Decisions

**Stateless JWT auth** — no server-side sessions means the API can scale horizontally across multiple instances without sticky sessions.

**Layered architecture** — Controller handles HTTP, Service handles business logic, Repository handles data. Each layer is independently testable.

**Ownership enforcement** — the service layer checks that the authenticated user owns a job listing before allowing updates or deletes.

**Multi-stage Docker build** — a separate build stage compiles the JAR so the final runtime image stays small and doesn't include build tools.
