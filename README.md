# Mini LeetCode — Spring Boot Portfolio Project

A full-stack coding challenge platform inspired by LeetCode, built with **Java 17**, **Spring Boot 3.2**, **MySQL**, and plain **HTML/CSS/JS**. This project demonstrates layered REST API architecture, JPA persistence, data seeding, and cloud deployment.

---

## Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     HTTP Client                         │
│          (Browser / Postman / curl)                     │
└────────────────────────┬────────────────────────────────┘
                         │ HTTP
┌────────────────────────▼────────────────────────────────┐
│                  Spring Boot App                        │
│  ┌──────────────────────────────────────────────────┐   │
│  │               Controller Layer                   │   │
│  │  UserController  ProblemController  SubCtrl      │   │
│  └────────────────────┬─────────────────────────────┘   │
│  ┌─────────────────── │ ────────────────────────────┐   │
│  │               Service Layer                      │   │
│  │  UserService    ProblemService   SubService       │   │
│  └────────────────────┬─────────────────────────────┘   │
│  ┌─────────────────── │ ────────────────────────────┐   │
│  │            Repository Layer (JPA)                │   │
│  │  UserRepo      ProblemRepo     SubRepo           │   │
│  └────────────────────┬─────────────────────────────┘   │
└───────────────────────┼─────────────────────────────────┘
                        │ JDBC
┌───────────────────────▼─────────────────────────────────┐
│                      MySQL                              │
│            users | problems | submissions               │
└─────────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Layer      | Technology                              |
|------------|-----------------------------------------|
| Language   | Java 17                                 |
| Framework  | Spring Boot 3.2.0                       |
| Database   | MySQL 8 (Railway MySQL plugin)          |
| ORM        | Spring Data JPA / Hibernate             |
| Validation | Jakarta Validation (`@Valid`, `@NotBlank`, etc.) |
| Docs       | Springdoc OpenAPI 2.3 (Swagger UI)      |
| Frontend   | Vanilla HTML + CSS + JS (static files)  |
| Build      | Maven                                   |
| Deploy     | Railway.app                             |

---

## API Endpoints (18 total)

### User Management

| Method | Endpoint                  | Description                          |
|--------|---------------------------|--------------------------------------|
| POST   | `/api/users/register`     | Register a new user                  |
| POST   | `/api/users/login`        | Authenticate (returns user profile)  |
| GET    | `/api/users`              | List all users                       |
| GET    | `/api/users/{id}`         | Get user by ID                       |
| PUT    | `/api/users/{id}`         | Update user profile                  |
| DELETE | `/api/users/{id}`         | Delete a user                        |

### Problems

| Method | Endpoint                          | Description                        |
|--------|-----------------------------------|------------------------------------|
| GET    | `/api/problems`                   | List all problems                  |
| GET    | `/api/problems/{id}`              | Get problem by ID                  |
| POST   | `/api/problems`                   | Create a problem (Admin)           |
| PUT    | `/api/problems/{id}`              | Update a problem (Admin)           |
| DELETE | `/api/problems/{id}`              | Delete a problem (Admin)           |
| GET    | `/api/problems/difficulty/{level}`| Filter by EASY / MEDIUM / HARD     |
| GET    | `/api/problems/search?tag={tag}`  | Search problems by tag keyword     |

### Submissions

| Method | Endpoint                              | Description                         |
|--------|---------------------------------------|-------------------------------------|
| POST   | `/api/submissions`                    | Submit a solution                   |
| GET    | `/api/submissions/{id}`               | Get submission by ID                |
| GET    | `/api/submissions/user/{userId}`      | All submissions for a user          |
| GET    | `/api/submissions/problem/{problemId}`| All submissions for a problem       |
| GET    | `/api/submissions/user/{userId}/stats`| User statistics (accept rate, etc.) |

---

## Database Schema

```sql
-- Users table
CREATE TABLE users (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(120) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role          VARCHAR(10)  NOT NULL DEFAULT 'USER',
    created_at    DATETIME     NOT NULL,
    total_solved  INT          NOT NULL DEFAULT 0,
    rank          VARCHAR(30)
);

-- Problems table
CREATE TABLE problems (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    title              VARCHAR(200) NOT NULL,
    description        TEXT         NOT NULL,
    difficulty         VARCHAR(10)  NOT NULL,  -- EASY|MEDIUM|HARD
    tags               VARCHAR(300),            -- comma-separated
    acceptance_rate    DOUBLE,
    total_submissions  INT,
    example_input      TEXT,
    example_output     TEXT,
    constraints        TEXT
);

-- Submissions table (indexed on user_id and problem_id)
CREATE TABLE submissions (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id           BIGINT       NOT NULL,
    problem_id        BIGINT       NOT NULL,
    code              TEXT         NOT NULL,
    language          VARCHAR(30)  NOT NULL,
    status            VARCHAR(20)  NOT NULL,   -- ACCEPTED|WRONG_ANSWER|TIME_LIMIT|COMPILE_ERROR
    execution_time_ms INT,
    submitted_at      DATETIME     NOT NULL,
    INDEX idx_user_id    (user_id),
    INDEX idx_problem_id (problem_id)
);
```

---

## Sample Data (auto-seeded)

| Users    | Role  | Solved |
|----------|-------|--------|
| admin    | ADMIN | 42     |
| alice    | USER  | 18     |
| bob      | USER  | 7      |
| charlie  | USER  | 3      |
| diana    | USER  | 55     |

10 problems seeded (3 EASY, 4 MEDIUM, 3 HARD) with 20 sample submissions.

Default password for all seeded users: `password123` (admin: `admin123`)

---

## Running Locally

### Prerequisites
- Java 17+
- Maven 3.8+
- MySQL 8 running locally

### Setup

```bash
# 1. Clone the repo
git clone https://github.com/your-username/minileetcode
cd minileetcode

# 2. Create the database
mysql -u root -p -e "CREATE DATABASE minileetcode;"

# 3. Set environment variables (or edit application.properties)
export DATABASE_URL=jdbc:mysql://localhost:3306/minileetcode
export DATABASE_USER=root
export DATABASE_PASSWORD=yourpassword

# 4. Run the application
./mvnw spring-boot:run

# 5. Open http://localhost:8080 in your browser
```

### API Documentation
Swagger UI: http://localhost:8080/swagger-ui.html  
OpenAPI JSON: http://localhost:8080/api-docs

---

## Deploying to Railway

1. Push the project to GitHub
2. Log in to [Railway.app](https://railway.app) and create a new project
3. Click **Deploy from GitHub Repo** and select this repository
4. Add a **MySQL** plugin to the project
5. Set the following environment variables:
   ```
   DATABASE_URL      → from Railway MySQL plugin (JDBC URL)
   DATABASE_USER     → from Railway MySQL plugin
   DATABASE_PASSWORD → from Railway MySQL plugin
   SPRING_PROFILES_ACTIVE → prod
   ```
6. Railway will auto-build using Nixpacks and deploy

The `railway.json` configures the build and health check path (`/api/problems`).

---

## Design Decisions

### Why SHA-256 instead of BCrypt?
This is a portfolio/demo project without Spring Security. SHA-256 keeps the dependency tree small. A production app should use `BCryptPasswordEncoder`.

### Why comma-separated tags?
Tags are stored as `"array,dp,greedy"` for simplicity. In production, a normalised `problem_tags` join table would be used. This is called out explicitly as a deliberate trade-off.

### Why simulated judge?
A real online judge requires a sandboxed code execution environment (Docker containers, seccomp, resource limits). That is outside the scope of a REST API portfolio demo. The service simulates realistic verdict distributions.

### Layered Architecture
The project strictly follows `Controller → Service → Repository`:
- **Controllers** only handle HTTP (parse request, call service, return `ResponseEntity`)
- **Services** contain all business logic and call repositories
- **Repositories** are pure Spring Data JPA interfaces

---

## Skills Demonstrated

| Skill | Evidence |
|---|---|
| Spring Boot REST APIs | 18 endpoints across 3 controllers |
| Spring Data JPA | Entity mapping, custom JPQL queries, indexes |
| Layered architecture | Controller → Service → Repository separation |
| Input validation | `@Valid`, `@NotBlank`, `@Email`, `@Size` on DTOs |
| Error handling | `ResponseEntity` with meaningful HTTP status codes |
| Data modelling | 3 normalised entities with enums and constraints |
| Static frontend | Responsive dark-theme SPA calling REST APIs |
| OpenAPI docs | Swagger UI auto-generated from annotations |
| Cloud deployment | Railway.app with env-var config and health checks |
| Idiomatic Java | Streams, Optional, switch expressions, Lombok |

---

## Project Structure

```
src/main/java/com/minileetcode/
├── MiniLeetCodeApplication.java     # Entry point
├── config/
│   ├── DataSeeder.java              # CommandLineRunner - seeds demo data
│   └── OpenApiConfig.java           # Swagger UI customisation
├── controller/
│   ├── UserController.java          # 6 user endpoints
│   ├── ProblemController.java       # 7 problem endpoints
│   └── SubmissionController.java    # 5 submission endpoints
├── service/
│   ├── UserService.java             # User business logic + password hashing
│   ├── ProblemService.java          # Problem CRUD + acceptance rate
│   └── SubmissionService.java       # Submit + stats + judge simulation
├── repository/
│   ├── UserRepository.java          # JPA queries for users
│   ├── ProblemRepository.java       # JPA queries for problems
│   └── SubmissionRepository.java    # JPA queries + aggregate stats
├── model/
│   ├── User.java                    # JPA entity
│   ├── Problem.java                 # JPA entity
│   └── Submission.java              # JPA entity
└── dto/
    ├── UserDto.java                 # Request/response DTO + fromEntity()
    ├── ProblemDto.java              # Request/response DTO + fromEntity()
    ├── SubmissionDto.java           # Request/response DTO + fromEntity()
    ├── LoginRequest.java            # Login credentials
    └── StatsDto.java                # User submission statistics

src/main/resources/
├── application.properties           # Local dev config
├── application-prod.properties      # Railway production config
└── static/
    ├── index.html                   # SPA frontend
    ├── css/style.css                # Dark theme styles
    └── js/app.js                    # Fetch-based API calls
```

---

## License
MIT — free to use as a portfolio reference or learning resource.
