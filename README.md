<div align="center">
    
# Job Board REST API
A production-style REST API for a job board platform, built with Spring Boot 3, PostgreSQL, JWT authentication, and a full CI/CD pipeline. Users can register, post and manage job listings, and browse or search jobs without authentication.
    
![CI](https://github.com/DeekshithaKalluri/jobboard-api/actions/workflows/ci.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-17-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue)
![Tests](https://img.shields.io/badge/tests-18%20passing-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

</div>

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5, Spring MVC, Spring Security 6 |
| Auth | JWT via jjwt 0.12.6 |
| Database | PostgreSQL 15 + Hibernate 6 + Spring Data JPA |
| Migrations | Flyway |
| Docs | SpringDoc OpenAPI / Swagger UI |
| Testing | JUnit 5, Mockito, MockMvc |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |

---

## Features

- JWT-based registration and login
- Post, edit, and delete job listings (authenticated, owner-only)
- Browse all jobs with pagination and sorting
- Combined search by title, location, and job type
- Global exception handler returning consistent JSON error responses
- Flyway schema migrations
- Swagger UI at `/swagger-ui.html`
- Dockerized with multi-stage build using `amazoncorretto:17-alpine` for ARM64/Apple Silicon support
- GitHub Actions CI pipeline with PostgreSQL service container

---

## API Endpoints

### Auth (public)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and receive a JWT |

### Jobs

| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/jobs` | No | List all jobs (paginated) |
| GET | `/api/jobs/{id}` | No | Get a single job |
| GET | `/api/jobs/search` | No | Search/filter jobs |
| POST | `/api/jobs` | Yes | Create a job listing |
| PUT | `/api/jobs/{id}` | Yes | Update your job listing |
| DELETE | `/api/jobs/{id}` | Yes | Delete your job listing |

**Pagination and sorting parameters** (`GET /api/jobs`):
```
?page=0&size=10&sortBy=createdAt&direction=desc
```

**Search parameters** (`GET /api/jobs/search`):
```
?title=engineer&location=remote&jobType=FULL_TIME&sortBy=salary&direction=asc
```

---

## Running Locally

### Prerequisites

- Java 17
- Maven 3.9+
- PostgreSQL 15

### Database Setup

```sql
CREATE DATABASE jobboard;
CREATE USER jobuser WITH PASSWORD 'jobpass123';
GRANT ALL PRIVILEGES ON DATABASE jobboard TO jobuser;
GRANT ALL ON SCHEMA public TO jobuser;
ALTER DATABASE jobboard OWNER TO jobuser;
```

### Run

```bash
git clone https://github.com/DeekshithaKalluri/jobboard-api.git
cd jobboard-api
./mvnw spring-boot:run
```

API runs at `http://localhost:8080`. Swagger UI at `http://localhost:8080/swagger-ui.html`.

---

## Running with Docker

```bash
docker-compose up
```

This starts both the Spring Boot app and a PostgreSQL 15 container. No local database setup required.

---

## Running Tests

```bash
./mvnw test
```

**18 tests, 0 failures:**
- 8 unit tests (`JobServiceTest`) — Mockito, no Spring context
- 9 integration tests (`AuthControllerIntegrationTest`) — MockMvc, full Spring context with test profile
- 1 context load test (`ApiApplicationTests`)

---

## Project Structure

```
src/
├── main/java/com/jobboard/api/
│   ├── controller/       AuthController.java, JobController.java
│   ├── service/          JobService.java
│   ├── repository/       UserRepository.java, JobRepository.java
│   ├── model/            User.java, Job.java
│   ├── security/         JwtUtils.java, JwtAuthenticationFilter.java,
│   │                     SecurityConfig.java, UserDetailsServiceImpl.java,
│   │                     OpenApiConfig.java
│   ├── exception/        GlobalExceptionHandler.java
│   └── dto/              LoginRequest.java, RegisterRequest.java, JobResponse.java
└── main/resources/
    ├── application.properties
    └── db/migration/V1__init.sql
```

---

## Environment Variables

Copy `.env.example` and fill in your values:

```
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/jobboard
SPRING_DATASOURCE_USERNAME=jobuser
SPRING_DATASOURCE_PASSWORD=jobpass123
JWT_SECRET=your-secret-key-must-be-at-least-32-characters-long
JWT_EXPIRATION=86400000
```

---

## CI/CD

GitHub Actions runs on every push to `main`:

1. Spins up a PostgreSQL 15 service container
2. Runs all 18 tests with environment variables injected
3. Builds a Docker image if tests pass

---

## Challenges and What I Learned

**jjwt 0.12.6 API breaking change** — The `parserBuilder()` method was removed between 0.11.x and 0.12.x. Updated `JwtUtils` to use the new `Jwts.parser().verifyWith().build().parseSignedClaims()` chain after diagnosing the compilation error.

**PostgreSQL schema permissions** — Creating a database user without granting schema ownership caused `permission denied for schema public` errors on startup. Fixed with `GRANT ALL ON SCHEMA public` and `ALTER DATABASE OWNER`.

**Spring Security 403 vs 401** — Spring Security returns 403 by default for both missing and invalid credentials. Added a custom `AuthenticationEntryPoint` to return the semantically correct 401 on authentication failure.

**Docker ARM64 (Apple Silicon)** — `eclipse-temurin:17-jdk-alpine` has no ARM64 manifest. Switched to `amazoncorretto:17-alpine` which has native ARM64 support, resolving the platform mismatch error.

**Flyway on an existing schema** — Flyway refused to run because the database already had tables from earlier `ddl-auto=update` runs. Added `spring.flyway.baseline-on-migrate=true` to allow Flyway to take ownership of an existing schema cleanly.

**Hibernate lazy loading serialization** — `GET /api/jobs` threw a `ByteBuddyInterceptor` 
serialization error when Jackson tried to serialize the lazily-loaded `User` proxy. Fixed 
properly by introducing a `JobResponse` DTO that maps only safe fields (username string instead 
of the full `User` entity), eliminating both the serialization issue and potential password 
hash exposure.

---

## License

MIT — see [LICENSE](LICENSE)
