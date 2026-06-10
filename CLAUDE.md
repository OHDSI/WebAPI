# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

OHDSI WebAPI is a Java 8 Spring Boot web application (packaged as a WAR) that provides RESTful services for OHDSI tools, particularly Atlas. It connects to one or more CDM (Common Data Model) databases and a PostgreSQL application database. The app runs at `http://localhost:8080/WebAPI`.

## Build & Run

### Local development (PostgreSQL via Docker)

```bash
# Start the embedded Postgres container (one-time setup)
docker create --name postgres-webapi -p 8432:5432 -e POSTGRES_PASSWORD=ohdsi postgres:15.0-alpine
docker start postgres-webapi

# Build and run (skipping tests)
mvn clean install spring-boot:run -Dmaven.test.skip=true -P webapi-postgresql -s dev/settings.xml -f pom.xml
```

The `dev/settings.xml` file configures local PostgreSQL connection properties (localhost:8432, user postgres, password ohdsi) and enables `AtlasRegularSecurity` with a permissive JDBC auth query (any username except `notfound` with password `password` works).

After first login, grant admin rights:
```sql
INSERT INTO sec_user_role (user_id, role_id, origin) VALUES (1000, 2, 'SYSTEM');
```

### Build for other databases

Maven profiles control the target database: `-P webapi-postgresql`, `-P webapi-mssql`, `-P webapi-oracle`, `-P webapi-redshift`, `-P webapi-snowflake`, `-P webapi-bigquery`, `-P webapi-hive`, `-P webapi-impala`, `-P webapi-databricks`, `-P webapi-netezza`, `-P webapi-spark`, `-P webapi-iris`.

### Build without running

```bash
mvn clean install -DskipTests
```

## Testing

Tests use an embedded PostgreSQL (via `io.zonky.test`) — not H2 — because H2 lacks window functions, `md5`, and other features. The platform-specific Zonky binary is auto-selected by Maven OS activation profiles.

```bash
# Run all tests (unit + integration)
mvn clean test

# Run unit tests only (excludes *IT.java)
mvn clean test -DskipITtests=true

# Run integration tests only
mvn clean test -DskipUnitTests=true

# Run a single test class
mvn clean test -Dtest=CohortDefinitionServiceTest

# Run a single integration test
mvn clean verify -Dit.test=SecurityIT -DskipUnitTests=true
```

Unit tests extend `AbstractDatabaseTest`, which spins up a singleton embedded Postgres via `PostgresSingletonRule` and loads test data using DBUnit. Test properties are in `src/test/resources/application-test.properties`.

Integration tests are named `*IT.java` and run via `maven-failsafe-plugin`.

## Architecture

### Framework stack

- **Spring Boot 1.5** with embedded Tomcat; deployed as WAR
- **JAX-RS (Jersey 2.14)** for REST endpoints — services use `@Path`, `@GET`, `@POST`, etc. (not Spring MVC `@RestController`)
- **Spring Data JPA + Hibernate** for persistence against the WebAPI application database (PostgreSQL in dev; SQL Server in prod defaults)
- **Spring Batch** for long-running analysis jobs (cohort generation, characterization, estimation, prediction, incidence rates)
- **Apache Shiro** for authentication/authorization
- **Flyway** for schema migrations (`src/main/resources/db/migration/{postgresql,sqlserver,oracle}/`)

### Data model

There are two distinct database tiers:

1. **WebAPI application database** — stores definitions, jobs, users, security, results metadata. Entities extend `CommonEntity<T>` (`src/main/java/org/ohdsi/webapi/model/CommonEntity.java`), which provides audit fields (createdBy, modifiedBy, dates). JPA repositories use Spring Data.

2. **CDM source databases** — patient-level OMOP CDM data. Connections are registered as `Source` entities, each with `SourceDaimon` records mapping daimon types (`CDM`, `Vocabulary`, `Results`, `Temp`, `CEM`, `CEMResults`) to schema qualifiers. SQL targeting CDM sources goes through `SqlRender`/`SqlTranslate` (OHDSI's cross-dialect SQL library) to be rendered and translated per source dialect.

### Source/Daimon pattern

All multi-source SQL uses `SourceAwareSqlRender` (`src/main/java/org/ohdsi/webapi/sqlrender/SourceAwareSqlRender.java`) to inject CDM, Results, Vocabulary, and Temp schema qualifiers. Services look up sources via `SourceRepository` and pass the resolved `Source` to query builders.

### Service layer conventions

- Services that need database access extend `AbstractDaoService` (which extends `AbstractAdminService`), giving them access to `SourceRepository`, `UserRepository`, `PermissionService`, Spring's `ConversionService`, etc.
- Analysis execution services (`CcServiceImpl`, `PredictionServiceImpl`, `EstimationServiceImpl`, `IrCalculationService`) extend `AnalysisExecutionSupport`, which manages Spring Batch job launching.
- Long-running jobs are Spring Batch jobs composed of `Tasklet` steps. Each analysis type defines its own tasklets (e.g., `GenerateCohortTasklet`, `GenerateCohortCharacterizationTasklet`).
- REST controllers are in the same package as their domain (e.g., `cohortcharacterization/CcController.java`) or in `service/` (legacy).

### Security

Security is configured via the `security.provider` property. Options: `DisabledSecurity` (no auth), `AtlasRegularSecurity` (JDBC/LDAP/OAuth/SAML), `AtlasGoogleSecurity`. The active provider is wired as the `Security` bean. Shiro filter chains are built by `FilterChainBuilder`; authentication realms live in `shiro/realms/`. Permissions are managed through `PermissionManager` and enforced via Shiro annotations and `PermissionService`.

### Package structure by feature

Each analytical domain (`cohortdefinition`, `cohortcharacterization`, `ircalc`, `estimation`, `prediction`, `pathway`) follows a consistent pattern:
- `domain/` — JPA entities
- `dto/` — data transfer objects
- `converter/` or `converter.java` — Spring `Converter` implementations for domain↔DTO mapping
- `repository/` — Spring Data repositories
- `*Service.java` / `*ServiceImpl.java` — business logic
- `*Controller.java` — JAX-RS REST endpoint

### Cross-cutting concerns

- **Versioning** (`versioning/`) — entities support snapshot versioning; `VersionRepository` tracks historical versions.
- **Tagging** (`tag/`) — entities can be tagged; `HasTags<T>` interface marks taggable services.
- **Audit trail** (`audittrail/`) — configurable audit logging via Spring events.
- **Check/validation** (`check/`) — a validation framework with builders, checkers, and validators per domain type.
- **Sensitive info** (`common/sensitiveinfo/`) — role-based redaction of sensitive analysis output fields.
- **Generation cache** (`generationcache/`) — caches and reuses generation results across analyses.

### Configuration

All runtime properties flow through Maven profile properties → `application.properties` via `${placeholder}` substitution. There is no separate `application.yml`. Additional Spring profiles (`application-shiny.properties`, `application-test.properties`) supplement the base properties file.
