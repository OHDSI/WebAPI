# Personal API Keys — Configuration & Usage Guide

This document describes how personal API keys work in WebAPI, how they are
implemented, and how to use them for programmatic access to WebAPI services.

---

## Overview

Personal API keys allow authenticated users to generate long-lived credentials
for use by scripts, automated pipelines, and other programmatic clients that
cannot perform an interactive login.

An API key request is completely stateless — no session is created or
maintained. Once a key is presented on a request, the system resolves it to
the owning user's identity and the request is processed exactly as if that
user had authenticated via their normal login method.

Key properties of the implementation:

- **No secrets stored in the database.** Only a BCrypt hash of the key's
  secret component is persisted.
- **O(1) database lookup during authentication.** The key format includes a
  public identifier stored in a uniquely indexed column; this avoids scanning
  every row to find a match before running the expensive BCrypt comparison.
- **Scoped to the creating user.** Keys can only be managed (listed, revoked,
  deleted) by the user who created them.
- **Optional expiry.** Keys can be created with a specific lifetime in days, or
  with no expiry (never expires).
- **Two-stage deletion.** Keys can be soft-disabled (still visible in the list
  but cannot authenticate), or permanently removed from the database.

---

## Key Format

Every API key has the form:

```
wa_<identifier>_<secret>
```

Example:

```
wa_a1b2c3d4e5f6a7b8_hA7xPWLsl8B0ktTgrzvQcMi1rIh0g7oXsNpQqYvFjR2
```

| Component | Description |
|---|---|
| `wa` | Fixed prefix identifying the token as a WebAPI API key |
| `identifier` | 16-character lowercase hex string (8 random bytes). **Not secret** — used for indexed database lookup. |
| `secret` | 43-character Base64-URL string (32 random bytes, no padding). **Secret** — never stored; only its BCrypt hash is persisted. |

The `identifier` portion is stored in plain text in an indexed `UNIQUE` column
(`key_identifier`) in the `sec_api_key` table. This allows the authentication
filter to perform a single indexed lookup on each request before the
computationally expensive BCrypt verification step runs.

---

## Authentication Flow

When a request carries an `X-API-KEY` header, the following sequence occurs:

```
Incoming request  (X-API-KEY: wa_<identifier>_<secret>)
    │
    ▼
ApiKeyAuthFilter  (OncePerRequestFilter — runs before JWT/Bearer filter)
    │
    ├─ Parse key into identifier + secret
    │
    ├─ SELECT * FROM sec_api_key WHERE key_identifier = ?   ← indexed, O(1)
    │
    ├─ Check: disabled = false
    ├─ Check: expires_at IS NULL OR expires_at > NOW()
    │
    ├─ BCrypt.matches(secret, key_hash)                      ← slow by design
    │
    ├─ UPDATE sec_api_key SET last_used_at = NOW()
    │
    └─ Set WebApiAuthenticationToken(principal, sessionId=null) in SecurityContext
           │
           ▼
       Request proceeds normally
       (@PreAuthorize, AuthorizationService, etc. work as for any authenticated user)
```

If the header is absent the filter is a no-op and normal JWT authentication
proceeds on the same request.

If the key is present but invalid (wrong secret, disabled, expired, or unknown
identifier) the filter returns `401 Unauthorized` immediately with no further
processing.

---

## Architecture

### Key classes

| Class | Package | Responsibility |
|---|---|---|
| `ApiKeyAuthFilter` | `security.apikey` | `OncePerRequestFilter`; intercepts `X-API-KEY` header and authenticates the request |
| `ApiKeyService` | `security.apikey` | Key generation, validation, listing, soft-disable, and hard delete |
| `ApiKeyEntity` | `security.apikey` | JPA entity mapping to `sec_api_key` table |
| `ApiKeyRepository` | `security.apikey` | Spring Data repository; `findByKeyIdentifier()` is the hot path |
| `ApiKeyController` | `security.apikey` | REST endpoints for key management (`/user/apikeys`) |
| `JwtAuthConfig` | `security.authc` | Wires `ApiKeyAuthFilter` before `AuthenticationFilter` in the main security chain |

### Database table: `sec_api_key`

| Column | Type | Notes |
|---|---|---|
| `id` | `BIGINT` PK | Internal identifier (not exposed via API) |
| `key_identifier` | `VARCHAR(64)` UNIQUE | Public lookup key; indexed |
| `key_hash` | `VARCHAR(255)` | BCrypt hash of the secret component |
| `user_id` | `BIGINT` FK | References `sec_user(id) ON DELETE CASCADE` |
| `name` | `VARCHAR(255)` | User-provided label |
| `description` | `VARCHAR(1000)` | Optional free-text description |
| `created_at` | `TIMESTAMPTZ` | Creation timestamp |
| `expires_at` | `TIMESTAMPTZ` | Expiry timestamp; `NULL` means never expires |
| `disabled` | `BOOLEAN` | `true` after a soft-revoke |
| `last_used_at` | `TIMESTAMPTZ` | Updated on every successful authentication |

---

## REST Endpoints

All endpoints require the caller to be authenticated (interactive JWT session
or an existing API key). The caller can only see and manage their own keys.

### `POST /user/apikeys` — Create a key

**Request body (JSON):**

```json
{
  "name": "my-pipeline-key",
  "description": "Used by the nightly ETL job",
  "expiresInDays": 180
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | string | Yes | Short human-readable label |
| `description` | string | No | Optional longer description |
| `expiresInDays` | integer | No | Days until the key expires. `null` or `0` means it never expires. |

**Response (`201 Created`):**

```json
{
  "name": "my-pipeline-key",
  "keyIdentifier": "a1b2c3d4e5f6a7b8",
  "rawKey": "wa_a1b2c3d4e5f6a7b8_hA7xPWLsl8B0ktTgrzvQcMi1rIh0g7oXsNpQqYvFjR2",
  "createdAt": "2026-06-24T14:30:00Z",
  "expiresAt": "2026-12-21T14:30:00Z"
}
```

> **Important:** `rawKey` is returned exactly once. Store it securely
> immediately. It cannot be retrieved again — only the BCrypt hash is kept in
> the database.

---

### `GET /user/apikeys` — List keys

Returns metadata for all keys owned by the authenticated user. Secrets and
hashes are never included.

**Response (`200 OK`):**

```json
[
  {
    "name": "my-pipeline-key",
    "description": "Used by the nightly ETL job",
    "keyIdentifier": "a1b2c3d4e5f6a7b8",
    "createdAt": "2026-06-24T14:30:00Z",
    "expiresAt": "2026-12-21T14:30:00Z",
    "disabled": false,
    "lastUsedAt": "2026-06-24T18:45:00Z"
  }
]
```

---

### `DELETE /user/apikeys/{keyIdentifier}` — Revoke or remove a key

The `keyIdentifier` path segment is the 16-character hex identifier from the
creation response (the part between the first and second `_` in the full key).

| Request | Effect |
|---|---|
| `DELETE /user/apikeys/{keyIdentifier}` | **Soft-disable.** Sets `disabled = true`. The key record remains visible in the list but can no longer authenticate. |
| `DELETE /user/apikeys/{keyIdentifier}?remove` | **Hard delete.** Permanently removes the row from the database. |

**Response:** `204 No Content` on success, `404 Not Found` if the key does not
exist or does not belong to the authenticated user.

---

## Using an API Key

Include the raw key in the `X-API-KEY` request header on any WebAPI request:

```
X-API-KEY: wa_a1b2c3d4e5f6a7b8_hA7xPWLsl8B0ktTgrzvQcMi1rIh0g7oXsNpQqYvFjR2
```

The `Authorization: Bearer` header is **not** used for API key authentication.
If both headers are present, the API key takes precedence (the filter runs
first and short-circuits JWT processing for that request).

---

## Example Scripts (bash)

The examples below assume:
- WebAPI is running at `http://localhost:8080`
- The user has first obtained a JWT via their normal login method (DB login
  shown here; substitute `windows`, `openid`, or LDAP as appropriate)

### Step 0: Login and capture a JWT

```bash
# Login with DB credentials and extract the JWT
TOKEN=$(curl -s -u alice:password1 http://localhost:8080/user/login/db \
  | sed -n 's/.*"jwt"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')

echo "JWT: $TOKEN"
```

---

### Create an API key (expires in 180 days)

```bash
RESPONSE=$(curl -s -X POST http://localhost:8080/user/apikeys \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "my-pipeline-key",
    "description": "Nightly ETL job",
    "expiresInDays": 180
  }')

echo "$RESPONSE"

# Extract the raw key for use in subsequent requests
API_KEY=$(echo "$RESPONSE" | sed -n 's/.*"rawKey"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
KEY_IDENTIFIER=$(echo "$RESPONSE" | sed -n 's/.*"keyIdentifier"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')

echo "API Key:    $API_KEY"
echo "Identifier: $KEY_IDENTIFIER"
```

> **Save `$API_KEY` now.** This is the only time it will be returned.

---

### Create a non-expiring API key

```bash
curl -s -X POST http://localhost:8080/user/apikeys \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "permanent-key",
    "description": "No expiry"
  }'
```

---

### Use an API key to call a WebAPI endpoint

```bash
# Call GET /user/me using only the API key — no JWT needed
curl -s \
  -H "X-API-KEY: $API_KEY" \
  http://localhost:8080/user/me
```

Expected response contains the `user` block for the key's owner and their
`authz` (permissions) block, identical to an interactive session for that user.

```bash
# Call any other protected endpoint the same way
curl -s \
  -H "X-API-KEY: $API_KEY" \
  http://localhost:8080/cohortdefinition
```

---

### List all API keys for the current user

```bash
# Using JWT session
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/user/apikeys

# Or using an existing API key (keys can manage themselves)
curl -s \
  -H "X-API-KEY: $API_KEY" \
  http://localhost:8080/user/apikeys
```

---

### Soft-revoke a key (disable, keep record)

The key can no longer authenticate after this, but it remains visible in
`GET /user/apikeys` with `"disabled": true`.

```bash
curl -s -X DELETE \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/user/apikeys/$KEY_IDENTIFIER \
  -w "\nHTTP_CODE:%{http_code}\n"
# Expected: HTTP_CODE:204

# Confirm the key is now disabled
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/user/apikeys
# "disabled": true appears in the record

# Confirm the key no longer authenticates
curl -s \
  -H "X-API-KEY: $API_KEY" \
  http://localhost:8080/user/me \
  -w "\nHTTP_CODE:%{http_code}\n"
# Expected: HTTP_CODE:401
```

---

### Hard-delete a key (permanently remove record)

Appending `?remove` to the DELETE URL permanently removes the row from the
database. The record will no longer appear in `GET /user/apikeys`.

```bash
curl -s -X DELETE \
  -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/user/apikeys/$KEY_IDENTIFIER?remove" \
  -w "\nHTTP_CODE:%{http_code}\n"
# Expected: HTTP_CODE:204

# Confirm the key is gone
curl -s \
  -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/user/apikeys
# The key no longer appears in the list
```

---

### Full end-to-end example

```bash
#!/usr/bin/env bash
# end_to_end_apikey.sh — demonstrates the full API key lifecycle

WEBAPI="http://localhost:8080"

echo "=== Step 1: Login and get a JWT ==="
TOKEN=$(curl -s -u alice:password1 "$WEBAPI/user/login/db" \
  | sed -n 's/.*"jwt"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
[ -z "$TOKEN" ] && { echo "Login failed"; exit 1; }
echo "JWT obtained."

echo ""
echo "=== Step 2: Create an API key (valid 90 days) ==="
RESPONSE=$(curl -s -X POST "$WEBAPI/user/apikeys" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"name":"e2e-test-key","expiresInDays":90}')
echo "$RESPONSE"

API_KEY=$(echo "$RESPONSE" | sed -n 's/.*"rawKey"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
KEY_IDENTIFIER=$(echo "$RESPONSE" | sed -n 's/.*"keyIdentifier"[[:space:]]*:[[:space:]]*"\([^"]*\)".*/\1/p')
echo "Key identifier: $KEY_IDENTIFIER"

echo ""
echo "=== Step 3: Call /user/me using the API key (no JWT) ==="
curl -s -H "X-API-KEY: $API_KEY" "$WEBAPI/user/me"

echo ""
echo "=== Step 4: List keys ==="
curl -s -H "Authorization: Bearer $TOKEN" "$WEBAPI/user/apikeys"

echo ""
echo "=== Step 5: Soft-revoke the key ==="
curl -s -X DELETE \
  -H "Authorization: Bearer $TOKEN" \
  "$WEBAPI/user/apikeys/$KEY_IDENTIFIER" \
  -w "\nHTTP_CODE:%{http_code}\n"

echo ""
echo "=== Step 6: Confirm key is disabled (expects 401) ==="
curl -s -H "X-API-KEY: $API_KEY" "$WEBAPI/user/me" \
  -w "\nHTTP_CODE:%{http_code}\n"

echo ""
echo "=== Step 7: Hard-delete the key ==="
curl -s -X DELETE \
  -H "Authorization: Bearer $TOKEN" \
  "$WEBAPI/user/apikeys/$KEY_IDENTIFIER?remove" \
  -w "\nHTTP_CODE:%{http_code}\n"

echo ""
echo "=== Done ==="
```

---

## Security Considerations

### Secret never stored
The full `rawKey` string is generated in memory, returned once in the creation
response, and immediately discarded. Only `BCrypt(secret)` is written to
the database. There is no recovery path — if a key is lost, revoke it and
create a new one.

### BCrypt work factor
The service uses BCrypt with a strength factor of 12. This is intentionally
slow to resist offline brute-force attacks against stolen database rows. The
O(1) identifier lookup ensures that the BCrypt step only runs once per request
against a single, already-identified record.

### Identifier collision safety
The identifier is 8 cryptographically random bytes (64 bits of entropy). The
service retries up to 5 times on a unique-constraint violation before throwing
an error. At any realistic scale, collisions will never occur.

### Ownership enforcement
Both `revoke()` and `delete()` in `ApiKeyService` verify that the key's owning
user login matches the caller's authenticated login before making any change.
A 404 is returned (rather than 403) when ownership fails, to avoid revealing
whether a given identifier exists in the system.

### Transport security
API keys must only be transmitted over HTTPS in production. Sending an API key
over plain HTTP exposes it to network interception.

### Key rotation
There is no automatic rotation. Users are responsible for rotating keys on a
schedule appropriate to their environment. The `expiresInDays` parameter
enforces a hard lifetime; set it to a finite value for any key used in
long-running automated processes.
