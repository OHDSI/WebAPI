# Database Authentication — Configuration & Setup Guide

This document describes how to configure and deploy database authentication
in WebAPI. Database authentication validates user credentials against a
dedicated authentication database using standard JDBC, and enforces account
lockout after repeated failed login attempts.

---

## Overview

When database authentication is enabled, WebAPI stands up a dedicated JDBC
connection pool (`authDataSource`) pointing to an external authentication
database. On each login attempt the system:

1. Loads the user record from the `auth_user` table.
2. Checks that the account is enabled and not locked.
3. Verifies the password using Spring Security's **delegating password encoder**.
4. On failure: increments the failed-attempt counter and locks the account when
   the configured threshold is reached.
5. On success: resets the failed-attempt counter, clears any lockout, and
   returns an authenticated security context.

The authentication database is completely separate from the WebAPI application
database (`dataSource`) and can be hosted on any RDBMS that has a JDBC driver
(PostgreSQL, SQL Server, Oracle, MySQL, etc.).

---

## Architecture

```
HTTP Request (Basic Auth)
    │
    ▼
SecurityFilterChain  (/user/login/db)
    │
    ▼
DatabaseAuthenticationProvider
    │  ┌─ load user ──────────────┐
    │  │  increment attempts      │
    │  │  reset attempts          │
    │  │  lock user               │
    │  └──────────────────────────┘
    ▼
DatabaseUserDetailsService  ──JDBC──▶  auth_user table
    │                                  (authDataSource)
    ▼
LockoutPolicyProperties
    (max-failed-attempts, lockout-duration)
```

### Key classes

| Class | Package | Responsibility |
|---|---|---|
| `DatabaseAuthConfig` | `security.authc` | Spring Security filter chain & bean wiring |
| `DatabaseAuthenticationProvider` | `security.authc.db` | Credential verification & lockout logic |
| `DatabaseUserDetailsService` | `security.authc.db` | JDBC queries against `auth_user` |
| `DatabaseUser` | `security.authc.db` | Immutable record representing a user row |
| `AuthDataSourceProperties` | `security.authc.db` | Properties class for datasource + pool config |
| `LockoutPolicyProperties` | `security.authc.db` | Properties class for lockout thresholds |
| `AuthDataSource` | `org.ohdsi.webapi` | Creates the `authDataSource` HikariCP bean |

---

## Configuration (application.yaml)

All database authentication settings live under `security.auth.db`:

```yaml
security:
  auth:
    db:
      enabled: true                        # toggle DB authentication on/off

      lockout-policy:
        max-failed-attempts: 5             # lock account after this many failures
        lockout-duration: 30m              # how long the account stays locked

      datasource:
        driver-class-name: org.postgresql.Driver
        url: jdbc:postgresql://localhost:5436/SECURITY_DB
        username: dbsecurity_user
        password: dbsecurity_pass
        schema: security                   # schema containing auth_user table
        connection-test-query: SELECT 1
        connection-test-query-timeout: 2000
        maximum-pool-size: 5
        minimum-idle: 1
        connection-timeout: 5000
        register-mbeans: true
        pool-name: authDataSource
```

### Property reference

| Property | Type | Description |
|---|---|---|
| `enabled` | boolean | Enables/disables the entire DB auth subsystem |
| `lockout-policy.max-failed-attempts` | int | Number of failed logins before lockout |
| `lockout-policy.lockout-duration` | Duration | Lock duration (e.g. `30m`, `1h`, `90s`) |
| `datasource.driver-class-name` | String | JDBC driver class |
| `datasource.url` | String | JDBC connection URL |
| `datasource.username` | String | Database username |
| `datasource.password` | String | Database password |
| `datasource.schema` | String | Schema containing `auth_user`; leave blank if using default schema |
| `datasource.connection-test-query` | String | Validation query for the connection pool |
| `datasource.connection-test-query-timeout` | long | Validation query timeout (ms) |
| `datasource.maximum-pool-size` | int | Max connections in the pool |
| `datasource.minimum-idle` | int | Minimum idle connections |
| `datasource.connection-timeout` | int | Connection acquisition timeout (ms) |
| `datasource.register-mbeans` | boolean | Expose pool metrics via JMX |
| `datasource.pool-name` | String | Name for the HikariCP pool |

### Schema qualification

The `datasource.schema` value is used to qualify all SQL queries at runtime.
For example, if `schema` is set to `security`, queries become:

```sql
SELECT ... FROM security.auth_user WHERE login = ?
```

If `schema` is left blank or omitted, queries use the unqualified table name
(`auth_user`), relying on the database's default schema resolution. Setting the
schema explicitly is recommended for portability across database engines.

---

## Database Setup

### Step 1: Create the schema and table

Run the following DDL against your authentication database. Replace
`{schema}` with your target schema name (e.g. `security`, `auth`, `dbo`):

```sql
-- Create the schema if it does not already exist (syntax varies by RDBMS)
-- PostgreSQL:
CREATE SCHEMA IF NOT EXISTS {schema};

-- SQL Server:
-- IF NOT EXISTS (SELECT * FROM sys.schemas WHERE name = '{schema}')
--   EXEC('CREATE SCHEMA [{schema}]');

-- Create the auth_user table
CREATE TABLE {schema}.auth_user (
  login         VARCHAR(100)  PRIMARY KEY,
  password_hash VARCHAR(255)  NOT NULL,
  first_name    VARCHAR(100)  NULL,
  middle_name   VARCHAR(100)  NULL,
  last_name     VARCHAR(100)  NULL,
  enabled       BOOLEAN       NOT NULL DEFAULT TRUE,
  failed_attempts INT         NOT NULL DEFAULT 0,
  locked_until  TIMESTAMP     NULL
);
```

> **Note:** The column names (`login`, `password_hash`, `first_name`, etc.)
> must match exactly — these are referenced directly in the JDBC queries
> within `DatabaseUserDetailsService`.

If you are creating these tables under the 'postgres' account, but are using a differnt login to access these tables, you should grant permissions to the user to the schema and tables.

In this example, the account is named 'secuirty_svc' to the schema 'security':

```sql
-- allow the role to connect to the database
GRANT CONNECT ON DATABASE "SECURITY_DB" TO security_svc;

-- if schema already exists and you want security_svc to own it:
ALTER SCHEMA security OWNER TO security_svc;

-- allow the role to use and create objects in the schema
GRANT USAGE ON SCHEMA security TO security_svc;
GRANT CREATE ON SCHEMA security TO security_svc;

-- grant CRUD on all existing tables in the schema
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA security TO security_svc;

-- grant sequence usage (if you have serial/sequence-backed ids)
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA security TO security_svc;

-- change owner of individual objects if needed
ALTER TABLE security.auth_user OWNER TO security_svc;
-- repeat ALTER TABLE ... OWNER TO for other tables

-- ensure future tables/sequences created by the current owner grant permissions to security_svc
ALTER DEFAULT PRIVILEGES IN SCHEMA security GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO security_svc;
ALTER DEFAULT PRIVILEGES IN SCHEMA security GRANT USAGE, SELECT ON SEQUENCES TO security_svc;
```


### Step 2: Insert user records

Insert users with properly encoded passwords. Replace `{schema}` with the
same schema used above:

```sql
-- Example: plain-text passwords (development/testing ONLY)
INSERT INTO {schema}.auth_user (login, password_hash, first_name, last_name, enabled, failed_attempts)
VALUES ('alice', '{noop}password1', 'Alice', 'Smith', true, 0);

-- Example: bcrypt-hashed passwords (recommended for production)
INSERT INTO {schema}.auth_user (login, password_hash, first_name, last_name, enabled, failed_attempts)
VALUES ('bob', '{bcrypt}$2a$12$BP7MT0qByqj.viRsdsqV6O3b0DaDAlvYGX4j7koGn1lGvEvLoojUm', 'Bob', 'Jones', true, 0); -- password2
```

---

## Password Encoding

WebAPI uses Spring Security's **`DelegatingPasswordEncoder`**, which reads
a `{prefix}` at the start of each stored password hash to determine which
algorithm to use for verification. This means different users in the same table
can use different encoding schemes — enabling incremental migration from weak
to strong hashing without downtime.

### Supported prefixes

| Prefix | Algorithm | Use case |
|---|---|---|
| `{noop}` | No encoding (plain text) | Development and testing only |
| `{bcrypt}` | bcrypt | **Recommended for production** |
| `{scrypt}` | scrypt | High-memory-cost hashing |
| `{argon2}` | Argon2id | Modern, memory-hard hashing |
| `{pbkdf2}` | PBKDF2 | NIST-approved key derivation |
| `{sha256}` | SHA-256 | Legacy (not recommended) |

The prefix **must** be stored as part of the `password_hash` column value.
When a user logs in, Spring Security inspects the prefix, selects the matching
encoder, and verifies the raw password against the stored hash.

### How to generate password hashes

#### Option A: Using a Spring Boot command-line snippet

Create a small Java class or use `jshell` / a unit test:

```java
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

public class HashPassword {
  public static void main(String[] args) {
    PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    // The default delegating encoder uses bcrypt
    String hash = encoder.encode("mySecurePassword");
    System.out.println(hash);
    // Output: {bcrypt}$2a$10$...
  }
}
```

The output string (including the `{bcrypt}` prefix) is the value to insert
into the `password_hash` column.

#### Option B: Using the Spring Boot CLI or a Groovy script

```groovy
@Grab('org.springframework.security:spring-security-crypto:6.4.2')
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

def encoder = new BCryptPasswordEncoder()
println "{bcrypt}" + encoder.encode("mySecurePassword")
```

#### Option C: Using an online bcrypt generator (testing only)

Generate a bcrypt hash at a site like [bcrypt-generator.com](https://bcrypt-generator.com/),
then prepend `{bcrypt}` to the result before inserting into the database.

> **Important:** Never use `{noop}` in production. Plain-text passwords in
> the database are a critical security vulnerability. Always use `{bcrypt}`
> or a stronger algorithm.

### Migrating from plain-text to bcrypt

If you initially seeded users with `{noop}` passwords for development, you can
upgrade them in-place:

1. Generate the bcrypt hash for each password (using any method above).
2. Update the `password_hash` column:
   ```sql
   UPDATE {schema}.auth_user
   SET password_hash = '{bcrypt}$2a$10$...'
   WHERE login = 'alice';
   ```
3. No code changes are needed — `DelegatingPasswordEncoder` automatically
   handles the new prefix on the next login.

---

## Authentication Flow

### Login request

```
POST /user/login/db
Authorization: Basic base64(username:password)
```

Or via curl:

```bash
curl -u alice:password1 http://localhost:8080/WebAPI/user/login/db
```

### Success path

1. Spring Security's `BasicAuthenticationFilter` extracts credentials.
2. `DatabaseAuthenticationProvider.authenticate()` is called.
3. `DatabaseUserDetailsService.loadUserByLogin(login)` queries `auth_user`.
4. Account enabled and lockout checks pass.
5. `PasswordEncoder.matches(rawPassword, storedHash)` verifies the password.
6. Failed-attempt counter is reset to 0, lockout cleared.
7. An authenticated `UsernamePasswordAuthenticationToken` is returned.
8. The login controller mints a JWT for subsequent API access.

### Failure path

1. If the user does not exist or is disabled → `DisabledException`.
2. If the account is locked → `LockedException` (includes retry time).
3. If the password is wrong → `BadCredentialsException`:
   - Failed-attempt counter is incremented.
   - If the counter reaches `max-failed-attempts`, the account is locked
     until `now + lockout-duration`.

### Lockout behavior

- After `max-failed-attempts` consecutive failures, the account is locked.
- `locked_until` is set to the current time plus `lockout-duration`.
- While locked, all login attempts are rejected with a `LockedException`
  regardless of whether the password is correct.
- After the lockout period expires, the next successful login resets the
  counter and clears the lock.

---

## Troubleshooting

| Symptom | Likely cause | Fix |
|---|---|---|
| `DisabledException: User not found or disabled` | User does not exist in `auth_user` or `enabled = false` | Verify the record exists and `enabled` is `true` |
| `LockedException: Account locked until ...` | Too many failed attempts | Wait for lockout to expire, or manually reset: `UPDATE {schema}.auth_user SET failed_attempts = 0, locked_until = NULL WHERE login = '...'` |
| `BadCredentialsException: Invalid credentials` | Wrong password or incorrect password hash format | Verify `password_hash` includes the correct prefix (e.g. `{bcrypt}`) |
| `Failed to initialize connection to DB` | Wrong JDBC URL, credentials, or driver | Check `security.auth.db.datasource.*` settings; verify the database is reachable |
| Queries fail with "table not found" | Schema mismatch | Ensure `datasource.schema` matches the schema where `auth_user` was created |

---

## Example: Full setup for PostgreSQL

```sql
-- 1. Create the schema
CREATE SCHEMA IF NOT EXISTS security;

-- 2. Create the table
CREATE TABLE security.auth_user (
  login         VARCHAR(100)  PRIMARY KEY,
  password_hash VARCHAR(255)  NOT NULL,
  first_name    VARCHAR(100)  NULL,
  middle_name   VARCHAR(100)  NULL,
  last_name     VARCHAR(100)  NULL,
  enabled       BOOLEAN       NOT NULL DEFAULT TRUE,
  failed_attempts INT         NOT NULL DEFAULT 0,
  locked_until  TIMESTAMP     NULL
);

-- 3. Create a database user for WebAPI to connect with
CREATE USER dbsecurity_user WITH PASSWORD 'dbsecurity_pass';
GRANT USAGE ON SCHEMA security TO dbsecurity_user;
GRANT SELECT, INSERT, UPDATE, DELETE ON security.auth_user TO dbsecurity_user;

-- 4. Insert users (bcrypt passwords for production)
INSERT INTO security.auth_user (login, password_hash, first_name, last_name, enabled, failed_attempts)
VALUES ('admin', '{bcrypt}$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin', 'User', true, 0);
```

Corresponding `application.yaml`:

```yaml
security:
  auth:
    db:
      enabled: true
      lockout-policy:
        max-failed-attempts: 5
        lockout-duration: 30m
      datasource:
        driver-class-name: org.postgresql.Driver
        url: jdbc:postgresql://localhost:5436/SECURITY_DB
        username: dbsecurity_user
        password: dbsecurity_pass
        schema: security
        connection-test-query: SELECT 1
        connection-test-query-timeout: 2000
        maximum-pool-size: 5
        minimum-idle: 1
        connection-timeout: 5000
        register-mbeans: true
        pool-name: authDataSource
```