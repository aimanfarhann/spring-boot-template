# Spring Boot Template

A reusable starting point for REST APIs: Spring Boot 4.1, Java 21, PostgreSQL, Flyway, Maven.

## Prerequisites

- JDK 21
- Docker (for the local database and for the integration tests)

You don't need Maven installed: use the bundled wrapper (`./mvnw`, or `mvnw.cmd` on Windows).

## Run locally

```bash
cp .env.example .env          # optional: only needed to change DB name, credentials or port
docker compose up -d          # start PostgreSQL
./mvnw spring-boot:run        # start the app on http://localhost:8080 using the dev profile
```

| URL | Purpose |
|---|---|
| http://localhost:8080/swagger-ui.html | Swagger UI (dev only) |
| http://localhost:8080/v3/api-docs | OpenAPI spec (dev only) |
| http://localhost:8080/actuator/health | Health check |

Stop the database with `docker compose down`. Its data lives in a named volume, so add `-v` to wipe it.

**No compose?** Run `TestSpringBootTemplateApplication` (under `src/test`) from your IDE. It starts the app with a throwaway Testcontainers database.

## Build and test

```bash
./mvnw verify            # compile, run all tests, check formatting, package the jar
./mvnw spotless:apply    # auto-format the code (Palantir style)
./mvnw test -Dtest=ProductServiceTest   # run a single test class
```

`verify` fails if code isn't formatted. Run `spotless:apply` before committing.

| Test type | Example | Runs against |
|---|---|---|
| Unit | `ProductServiceTest` | Mockito mocks, no Spring context |
| Web slice | `ProductControllerTest` | `@WebMvcTest` with the service mocked |
| Repository | `ProductRepositoryTest` | Real PostgreSQL via Testcontainers |
| Smoke | `SpringBootTemplateApplicationTests` | Full context + Testcontainers |

## Project layout

Code is grouped **by feature**, so everything for one feature lives in one package and can be deleted as a unit.

```
src/main/java/com/example/template/
├── SpringBootTemplateApplication.java
├── common/
│   └── exception/        # Domain exceptions + GlobalExceptionHandler (ProblemDetail responses)
└── product/              # Example feature: copy it, then delete it
    ├── Product.java              # JPA entity
    ├── ProductRepository.java
    ├── ProductService.java       # Business rules, transactions; returns DTOs
    ├── ProductController.java    # /api/v1/products
    └── dto/                      # Request/response records + mapping
src/main/resources/
├── application.yml       # Shared settings
├── application-dev.yml   # Local defaults (match docker-compose.yml)
├── application-prod.yml  # No defaults: env vars required
└── db/migration/         # Flyway scripts (V1__..., V2__...)
```

## Conventions

- **API:** routes under `/api/v1`. Responses are DTOs returned directly, with status codes for outcomes.
- **Errors:** every error is an [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) `ProblemDetail`. Throw `ResourceNotFoundException` (404) or `ResourceConflictException` (409) from services; validation failures return 400 with an `errors` map of field to message.
- **Schema:** Flyway owns it. Hibernate runs with `ddl-auto=validate`, so a mismatched entity fails at startup. Never edit an applied migration; add a new `V{n}__description.sql`.
- **Transactions:** services are `@Transactional(readOnly = true)` at class level; write methods override with `@Transactional`. `open-in-view` is off, so map entities to DTOs inside the service.
- **Mapping:** hand-written, e.g. `ProductResponse.from(product)`. No Lombok, no MapStruct.
- **IDs and time:** `Long` identity IDs. Use `Instant` for timestamps (stored in UTC).

## Configuration

| Variable | Used in | Default (dev) |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | all | `dev` |
| `DB_URL` | dev, prod | `jdbc:postgresql://localhost:5432/app` |
| `DB_USERNAME` | dev, prod | `app` |
| `DB_PASSWORD` | dev, prod | `app` |

`prod` has no defaults, so startup fails if any variable is missing. It also disables Swagger and logs as structured JSON (ECS).

## Adding a feature

1. Copy `product/` to `yourfeature/` and rename the classes.
2. Add a migration: `src/main/resources/db/migration/V2__create_yourfeature_table.sql`.
3. Copy and adapt the three `product` test classes.
4. Run `./mvnw spotless:apply verify`.

## Starting a new project from this template

1. Copy the folder (without `target/` or `.git/`) and run `git init`.
2. In `pom.xml`, change `groupId`, `artifactId`, `name` and `description`.
3. Rename the package `com.example.template` (your IDE's refactor → rename package handles imports), and rename `SpringBootTemplateApplication`, `TestSpringBootTemplateApplication` and `SpringBootTemplateApplicationTests`.
4. In `application.yml`, update `spring.application.name`. In `application-dev.yml`, update the `logging.level` package.
5. Once your first real feature exists, delete the example: `product/`, its tests and `V1__create_product_table.sql`, then renumber your migrations to start at `V1`.

## Not included (add per project)

Authentication (Spring Security), CORS, pagination, auditing fields, Dockerfile, CI.
For a container image without a Dockerfile: `./mvnw spring-boot:build-image`.
