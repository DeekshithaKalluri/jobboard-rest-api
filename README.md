# Job Board REST API

A production-style RESTful microservice built with Spring Boot, featuring JWT authentication, PostgreSQL persistence, comprehensive testing, and a fully automated CI/CD pipeline.

![CI/CD Pipeline](https://github.com/DeekshithaKalluri/jobboard-api/actions/workflows/ci.yml/badge.svg)

## What It Does

This API powers a job board platform where:
- **Users** can register and log in securely
- **Authenticated users** can post, update, and delete job listings
- **Anyone** can browse and search jobs without logging in
- **JWT tokens** ensure only the job owner can modify their own listings

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.x / Spring MVC |
| Security | Spring Security 6 + JWT (jjwt 0.12) |
| Database | PostgreSQL 15 |
| ORM | Hibernate / Spring Data JPA |
| Testing | JUnit 5, Mockito, MockMvc |
| Containerization | Docker + Docker Compose |
| CI/CD | GitHub Actions |
| Build Tool | Maven |

## Project Structure
src/
├── main/java/com/jobboard/api/
│   ├── controller/        # REST endpoints (AuthController, JobController)
│   ├── service/           # Business logic (JobService)
│   ├── repository/        # Database queries (UserRepository, JobRepository)
│   ├── model/             # Database entities (User, Job)
│   ├── security/          # JWT filter, UserDetailsService, SecurityConfig
│   └── dto/               # Request objects (LoginRequest, RegisterRequest)
└── test/
├── JobServiceTest.java              # Unit tests with Mockito
└── AuthControllerIntegrationTest.java  # Integration tests with MockMvc

## API Endpoints

### Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | None | Register a new user |
| POST | `/api/auth/login` | None | Login and receive JWT token |

### Jobs
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/api/jobs` | None | Get all jobs |
| GET | `/api/jobs/{id}` | None | Get a job by ID |
| GET | `/api/jobs/search?title=X` | None | Search jobs by title |
| GET | `/api/jobs/search?location=X` | None | Search jobs by location |
| POST | `/api/jobs` | Required | Create a new job listing |
| PUT | `/api/jobs/{id}` | Required | Update your job listing |
| DELETE | `/api/jobs/{id}` | Required | Delete your job listing |

## Running Locally

### Prerequisites
- Java 17
- Docker Desktop

### Option 1 — Docker Compose (recommended)
```bash
docker-compose up
```
The app starts at `http://localhost:8080` with PostgreSQL included.

### Option 2 — Run directly
```bash
# Start PostgreSQL first
brew services start postgresql@15

# Run the app
./mvnw spring-boot:run
```

## Example Usage

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"jane","email":"jane@example.com","password":"secret123"}'
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"jane","password":"secret123"}'
# Returns: {"token":"eyJ...","type":"Bearer","username":"jane"}
```

### Post a Job
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

### Search Jobs
```bash
curl "http://localhost:8080/api/jobs/search?title=engineer"
```

## Running Tests
```bash
./mvnw test
# Tests run: 15, Failures: 0, Errors: 0
```

## CI/CD Pipeline

Every push to `main` automatically:
1. Spins up a PostgreSQL service container
2. Runs all 15 unit and integration tests
3. Builds a Docker image (only if tests pass)

## Key Design Decisions

- **Stateless auth** — JWT tokens mean no server-side sessions; scales horizontally
- **Layered architecture** — Controller → Service → Repository; each layer has one job
- **Input validation** — `@Valid` + Jakarta Bean Validation on all request bodies
- **Ownership checks** — users can only modify their own job listings
- **Multi-stage Docker build** — build stage compiles the JAR; runtime stage is lean
