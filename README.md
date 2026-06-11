# WebAPI-node

A Node.js drop-in replacement for [OHDSI WebAPI](https://github.com/OHDSI/WebAPI) — the backend REST API that powers [Atlas](https://github.com/OHDSI/Atlas).

It is Atlas-compatible: the JSON response shapes match the Java WebAPI exactly, so Atlas works without modification. CDM access is SQL Server only. Application state is stored in SQLite. Authentication is handled upstream by an nginx reverse proxy.

## Differences from Java WebAPI

| Feature | Java WebAPI | WebAPI-node |
|---|---|---|
| Runtime | Java 8 / Spring Boot | Node.js LTS |
| App database | PostgreSQL / SQL Server | SQLite |
| CDM databases | Any OHDSI dialect | SQL Server only |
| Auth | Shiro (JDBC, LDAP, OAuth, SAML) | External (nginx header) |
| Source config | Admin UI + database | Environment variable |
| Cohort generation | CIRCE → SQL | **Not implemented** (returns 501) |
| Analysis execution | Spring Batch + Arachne | **Not implemented** (returns 501) |

Vocabulary search, concept sets, CDM Results (Achilles), Cohort Results (Heracles), and all CRUD for cohort definitions, IR analyses, cohort characterizations, pathway analyses, estimation, and prediction are fully implemented.

## Quick Start

### Docker

```bash
docker compose up
```

Edit `docker-compose.yml` to point `WEBAPI_SOURCES` at your SQL Server CDM. Then point Atlas at `http://localhost:8080`.

### Local development

```bash
npm install
DB_PATH=/tmp/webapi.db WEBAPI_SOURCES='[...]' npm run dev
```

## Configuration

All configuration is through environment variables. No config files.

| Variable | Default | Description |
|---|---|---|
| `PORT` | `8080` | HTTP listen port |
| `HOST` | `0.0.0.0` | HTTP listen address |
| `DB_PATH` | `/data/webapi.db` | Path to the SQLite database file. The directory must exist. |
| `WEBAPI_SOURCES` | `[]` | JSON array of CDM source objects (see below) |
| `WEBAPI_AUTH_HEADER` | `x-forwarded-user` | Request header containing the authenticated username, set by the upstream proxy |
| `WEBAPI_VERSION` | `2.15.1` | Version string reported by `GET /info` |

### CDM Sources

`WEBAPI_SOURCES` is a JSON array. Each entry defines one CDM database connection:

```json
[
  {
    "sourceKey":        "my_cdm",
    "sourceName":       "My CDM",
    "connectionString": "Server=db.example.com,1433;Database=cdm;Encrypt=true;TrustServerCertificate=true;",
    "username":         "sa",
    "password":         "secret",
    "cdmSchema":        "dbo",
    "vocabSchema":      "dbo",
    "resultsSchema":    "results",
    "tempSchema":       "temp"
  }
]
```

Multiple sources are supported. Atlas will show all of them in its source picker.

`vocabSchema` and `cdmSchema` can point to the same schema if the vocabulary tables are co-located with the CDM tables. `resultsSchema` is where Achilles and Heracles pre-computed results are stored.

## Authentication

WebAPI-node does **not** implement authentication itself. It reads a single HTTP request header (configured via `WEBAPI_AUTH_HEADER`) and treats its value as the authenticated username. All requests are granted full access.

In production, place an nginx reverse proxy in front that authenticates users and injects the header:

```nginx
location /WebAPI/ {
    proxy_pass http://webapi:8080/;
    proxy_set_header x-forwarded-user $remote_user;
}
```

Without a proxy (e.g. in development), every request runs as `anonymous`.

## Docker

```dockerfile
FROM node:lts-slim
```

The image mounts a single volume at `/data` for the SQLite database. The database schema is created automatically on first startup via numbered migration files.

```bash
# Build
docker build -t webapi-node .

# Run
docker run -p 8080:8080 \
  -v $(pwd)/data:/data \
  -e WEBAPI_SOURCES='[{"sourceKey":"cdm",...}]' \
  webapi-node
```

## API Coverage

### Fully implemented

| Prefix | Description |
|---|---|
| `GET /info` | Version info |
| `GET /source/sources` | CDM source list |
| `GET /source/:key` | Single source |
| `GET /user/me` | Current user |
| `/vocabulary/:sourceKey/…` | Concept search, lookup, descendants, ancestors, domains, vocabularies |
| `/conceptset/…` | Concept set CRUD + items + expression resolution |
| `/cohortdefinition/…` | Cohort definition CRUD + generation info |
| `/cdmresults/:sourceKey/…` | Achilles reports: dashboard, person, datadensity, death, observation period, domain treemaps + drilldowns |
| `/cohortresults/:sourceKey/…` | Heracles cohort results: dashboard, person, domain treemaps + drilldowns, data completeness |
| `/ir/…` | Incidence rate analysis CRUD + versioning |
| `/cohort-characterization/…` | Cohort characterization CRUD + versioning |
| `/pathway-analysis/…` | Pathway analysis CRUD + versioning |
| `/estimation/…` | Estimation CRUD + versioning |
| `/prediction/…` | Prediction CRUD + versioning |
| `/tag/…` | Tag CRUD + multi-assign/unassign |
| `/:sourceKey/person/:personId` | Patient profile (CDM query) |
| `/notifications/…` | Job activity feed Atlas polls |
| `/job/…` | Job execution status |

### Not implemented (returns 501)

| Endpoint | Reason |
|---|---|
| `POST /cohortdefinition/sql` | Requires [CIRCE](https://github.com/OHDSI/circe-be) Java library |
| `GET /cohortdefinition/:id/generate/:sourceKey` | Requires CIRCE |
| `GET /ir/:id/report/:sourceKey` | Requires pre-generated IR results |
| `POST /:analysisType/:id/generation/:sourceKey` | Requires Arachne execution engine |

## Project Structure

```
src/
├── server.js          — entry point (bind port, start listening)
├── app.js             — Express app (routes wired here)
├── config.js          — env var parsing
├── db.js              — SQLite setup + migrations
├── sources.js         — CDM source pool management
├── sqlrender.js       — @param substitution into SQL templates
├── jobResource.js     — Spring Batch job shape Atlas expects
├── conceptSetExpression.js — JS port of CIRCE concept set SQL builder
├── middleware/
│   ├── user.js        — populate req.user from auth header
│   └── errors.js      — JSON error handler
├── routes/            — one file per URL prefix
└── sql/               — SQL templates (copied from Java WebAPI resources)
    ├── cdmresults/    — Achilles report SQL (~129 files)
    ├── cohortresults/ — Heracles result SQL (~114 files)
    ├── vocabulary/    — concept search SQL
    ├── cohortdefinition/
    └── person/        — patient profile SQL

migrations/            — numbered SQLite schema files (run on startup)
```

## Dependencies

Three runtime packages:

| Package | Purpose |
|---|---|
| `express` | HTTP server and routing |
| `better-sqlite3` | Synchronous SQLite for application state |
| `mssql` | SQL Server driver with connection pooling |

Dev: `nodemon` (hot reload), `jest` (tests).

## SQLite Schema

The application database stores all Atlas-managed definitions. The schema is applied automatically from `migrations/` on first startup.

Key tables: `cohort_definition`, `cohort_definition_details`, `concept_set`, `concept_set_item`, `ir_analysis`, `cc_analysis`, `pathway_analysis`, `estimation`, `prediction`, `tag`, `entity_tag`, `version`, `job`, `notifications_viewed`.

The database file persists across container restarts via the `/data` volume mount. To reset all application state, delete the `.db` file — it will be recreated on next startup.

## License

Apache 2.0
