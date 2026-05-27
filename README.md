<div align="center">

# 💼 Job Board REST API

**A production-style backend microservice for a job board platform**

Built with Spring Boot · Secured with JWT · Tested · Dockerized · CI/CD on every push

A backend REST API I built to practice production-level Java development.
It handles user auth, job listings, and search — the kind of thing that
powers any real hiring platform.

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

I built this to understand how real backend systems work end to end — from
taking an HTTP request to storing data in a database and sending a response.
It models a simplified job board: users sign up, post jobs, and search listings.

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

## What I Learned Building This

**JWT is stateless by design.** The server never stores the token — it just
validates the signature on each request. That's why it scales well.

**Spring Security 6 changed a lot.** The old WebSecurityConfigurerAdapter is
gone. I had to learn the new lambda-style SecurityFilterChain, which honestly
makes more sense once you understand it.

**Integration tests need a real database.** I tried H2 in-memory first but
ran into dialect issues with PostgreSQL-specific constraints. Using the actual
PostgreSQL instance with create-drop made the tests reliable.

**Docker Compose made local dev much easier.** Instead of worrying about
PostgreSQL being started, one command brings up the whole stack.

**GitHub Actions runs on Linux.** My local machine is Apple Silicon (ARM),
which broke the eclipse-temurin Docker image. Had to switch to amazoncorretto
which has proper ARM64 support.

---

## Challenges I Hit

- **jjwt version mismatch** — upgraded from 0.11.5 to 0.12.6 mid-build because
  the API changed (`parserBuilder()` was removed). Had to update all three method
  calls in JwtUtils.
- **PostgreSQL permissions** — jobuser didn't have schema create rights by default
  in PostgreSQL 15. Fixed with `GRANT ALL ON SCHEMA public TO jobuser`.
- **Spring Security returning 403 vs 401** — unauthenticated requests were returning
  403 Forbidden instead of 401 Unauthorized. Fixed by adding a custom
  AuthenticationEntryPoint to the security config.
- **Maven using wrong Java** — Maven defaulted to Java 26 even after installing
  Java 17. Fixed by setting JAVA_HOME explicitly in .zshrc.
