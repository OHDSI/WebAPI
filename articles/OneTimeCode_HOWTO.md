# One-Time Code (OTC) — OAuth2/OIDC Authentication Guide

This document describes how One-Time Codes work in WebAPI, how they support
OAuth2 and OIDC authentication across load-balanced instances, and how they
enable seamless token delivery to frontend applications.

---

## Overview

One-Time Codes (OTC) are single-use, short-lived tokens that wrap pre-minted
JWT authentication tokens. They solve a critical problem in load-balanced OAuth2
environments: securely delivering ID tokens from an OAuth provider to a
frontend application across multiple WebAPI instances without relying on
HTTP sessions or cookies.

When a user authenticates via Google OAuth or any OIDC provider, the flow is:

1. OAuth provider redirects to WebAPI with authorization code
2. WebAPI backend validates credentials and mints a JWT token
3. JWT is wrapped in a One-Time Code and stored in the database
4. Frontend is redirected to the SPA with the OTC as a query parameter
5. SPA calls WebAPI's OTC redemption endpoint with the code
6. WebAPI validates the OTC, marks it as consumed, and returns the JWT
7. SPA stores the JWT and uses it for subsequent authenticated requests

This pattern avoids:
- **Session dependencies:** No HTTP sessions created; stateless across instances
- **Expired tokens in URLs:** OTC lives only 10 minutes; JWT is only revealed at redemption
- **Token leakage in logs/referers:** OTC is single-use; if intercepted, it becomes worthless immediately
- **CORS issues:** Frontend and backend can be on different domains

Key properties of the implementation:

- **Stateless and load-balanced.** Works seamlessly across multiple WebAPI instances
  because OTC records are stored in PostgreSQL, not in-memory sessions.
- **Short-lived.** OTC codes expire after 10 minutes (configurable). Expired codes
  are cleaned up automatically.
- **Single-use.** Once redeemed, the OTC is marked as revoked and cannot be used again.
- **Provider-agnostic.** Works with any OAuth2/OIDC provider (Google, generic OIDC,
  future: Facebook, Entra ID, Keycloak, etc.).
- **Claims preservation.** Custom claims (roles, groups) are embedded in the JWT at
  minting time and available at redemption.

---

## Key Format

Every One-Time Code is a UUID (RFC 4122):

```
550e8400-e29b-41d4-a716-446655440000
```

The OTC itself has no structure or secret component — it is simply a random
UUID. Security is provided by:
- Short expiration (10 minutes)
- Single-use revocation (marked `revoked = true` after redemption)
- Database-backed storage (can only be used by holders of the exact UUID)

---

## Authentication Flow

### OAuth2/OIDC Login

When a user clicks "Login with Google" or another OAuth provider:

```
User clicks "Login with Google" on SPA
    │
    ▼
SPA redirects to WebAPI:
    GET /user/login/google?code=<auth_code>&state=<state>
    │
    ▼
GoogleAuthConfig.googleAuthChain (SecurityFilterChain)
    │
    ├─ Redirect URI resolved via Spring template: {baseUrl}/user/oauth/callback/{registrationId}
    │   ├─ {baseUrl} extracted from incoming request (respects X-Forwarded-* headers)
    │   ├─ {registrationId} automatically replaced with "google"
    │   └─ Result: https://host:port/WebAPI/user/oauth/callback/google
    │
    ├─ oauth2Login filter exchanges auth code for ID token
    │
    ├─ OidcUserService validates ID token signature (JWKS)
    │
    ├─ handleGoogleSuccess() extracts user claims:
    │   ├─ email (or subject if no email)
    │   ├─ name (full name → email → subject)
    │   └─ roles (empty for Google; future: database lookup)
    │
    ├─ LoginService.onSuccess() creates JWT token
    │
    ├─ OneTimeCodeService.generateCode() wraps JWT in OTC:
    │   └─ INSERT INTO sec_one_time_code (code, login, origin, jwt_token, ...)
    │
    ├─ Session invalidated (STATELESS policy; no JSESSIONID)
    │
    └─ Redirect to SPA: ?code=<UUID>
           │
           ▼
SPA receives redirect with OTC query parameter
    │
    ▼
SPA stores OTC and calls WebAPI redemption endpoint:
    GET /user/login/otc?code=<UUID>
    │
    ▼
OtcLoginController.redeemOtc()
    │
    ├─ OneTimeCodeService.validateAndConsume() checks:
    │   ├─ OTC exists and not expired
    │   ├─ OTC not already revoked
    │   └─ Marks as revoked (UPDATE sec_one_time_code SET revoked = true)
    │
    ├─ Returns embedded JWT on success
    │
    └─ Returns 401 if expired, revoked, or invalid
           │
           ▼
SPA extracts JWT from response
    │
    ▼
SPA stores JWT (localStorage, sessionStorage, or memory)
    │
    ▼
SPA sends JWT on all subsequent requests:
    GET /cohortdefinition
    Authorization: Bearer <JWT>
```

---

## Architecture

### Key classes

| Class | Package | Responsibility |
|---|---|---|
| `OneTimeCodeEntity` | `security.authc` | JPA entity mapping to `sec_one_time_code` table |
| `OneTimeCodeRepository` | `security.authc` | Spring Data repository; queries by code, login, expiry |
| `OneTimeCodeService` | `security.authc` | Generate, validate, consume OTC codes; periodic cleanup |
| `OtcLoginController` | `security.authc` | REST endpoint for OTC redemption (`/user/login/otc`) |
| `GoogleAuthConfig` | `security.authc` | Spring Security configuration for Google OAuth2 login with template-based redirect URI resolution |
| `OidcAuthConfig` | `security.authc` | Generic OIDC provider configuration with template-based redirect URI and provider-specific role mapping |
| `OidcGroupToRoleMapper` | `security.authc.mapper` | Extracts and maps OIDC provider claims (roles, groups) to WebAPI permissions |

### Database table: `sec_one_time_code`

| Column | Type | Notes |
|---|---|---|
| `code` | `UUID` PK | The One-Time Code itself (random UUID); used as lookup key |
| `login` | `VARCHAR(255)` | The authenticated user's login name |
| `origin` | `VARCHAR(50)` | Provider origin (e.g., `GOOGLE`, `OPENID`, `FACEBOOK`) |
| `jwt_token` | `TEXT` | Complete pre-minted JWT token (embedded at creation, extracted at redemption) |
| `created_at` | `TIMESTAMP` | When the OTC was created |
| `expires_at` | `TIMESTAMP` | When the OTC expires (created_at + TTL) |
| `revoked` | `BOOLEAN` | `true` after successful redemption or explicit revocation |

### Indexes

| Index | Columns | Purpose |
|---|---|---|
| `idx_sec_one_time_code_expires_at` | `expires_at` | Find expired codes for cleanup |
| `idx_sec_one_time_code_login` | `login, expires_at` | Find valid codes for a user (fast user deletion) |
| `idx_sec_one_time_code_lookup` | `code, expires_at, revoked` | Fast redemption path: validate OTC and TTL without table scan |

---

## REST Endpoints

### `GET /user/login/otc` — Redeem an OTC and obtain JWT

**Query parameters:**

| Parameter | Type | Required | Description |
|---|---|---|---|
| `code` | UUID string | Yes | The One-Time Code received from the OAuth provider redirect |

**Response (`200 OK`) on success:**

```json
{
  "user": {
    "id": 123,
    "login": "alice@example.com",
    "name": "Alice Smith"
  },
  "jwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "origin": "GOOGLE"
}
```

| Field | Description |
|---|---|
| `user` | Authenticated user's metadata (id, login, name) |
| `jwt` | Complete JWT token for use in subsequent requests |
| `origin` | The authentication provider that minted this token |

**Response (`401 Unauthorized`) on failure:**

Returned if the OTC is:
- Not found in the database
- Already revoked (previously redeemed)
- Expired (created more than 10 minutes ago)

---

## OAuth Provider Configuration

### Redirect URI Template Resolution

WebAPI uses Spring Security's built-in template mechanism for OAuth callback URIs.
Instead of hardcoding callback URLs per environment, Spring automatically resolves:

- **`{baseUrl}`** — Extracted from the incoming HTTP request
  - Protocol (http/https)
  - Hostname and port
  - Context path (e.g., `/WebAPI`)
  - **Respects X-Forwarded headers** for reverse proxy scenarios

- **`{registrationId}`** — The OAuth provider name
  - Automatically replaced (e.g., `google`, `openid`)

**Example:** If a user visits:
```
https://my-domain.example.com/WebAPI/user/login/google
```

Spring resolves the redirect URI template to:
```
https://my-domain.example.com/WebAPI/user/oauth/callback/google
```

This works seamlessly across:
- **Direct deployments** (localhost development)
- **Reverse proxies** (nginx, Apache) — via X-Forwarded-Proto, X-Forwarded-Host, X-Forwarded-Port
- **Load balancers** (any instance can handle the callback)
- **Container orchestration** (Docker, Kubernetes)

### X-Forwarded Headers Configuration

For reverse proxy or load-balanced deployments, enable automatic header handling:

**`application.yaml`:**

```yaml
server:
  forward-headers-strategy: NATIVE
```

With this setting, Spring Security automatically processes:
- `X-Forwarded-Proto` (http/https)
- `X-Forwarded-Host` (hostname)
- `X-Forwarded-Port` (custom port)
- `X-Forwarded-Prefix` (context path prefix)

### Google OAuth

Enable and configure Google OAuth by setting environment variables or
application properties:

**`application.yaml`:**

```yaml
server:
  forward-headers-strategy: NATIVE  # Enable reverse proxy header support

security:
  auth:
    oauth:
      google:
        enabled: true
        apiKey: "YOUR_GOOGLE_CLIENT_ID"
        apiSecret: "YOUR_GOOGLE_CLIENT_SECRET"
      callback:
        ui: "http://localhost/Atlas/#/home"
```

| Property | Required | Description |
|---|---|---|
| `security.auth.oauth.google.enabled` | Yes | Set to `true` to enable Google OAuth |
| `security.auth.oauth.google.apiKey` | Yes | Google OAuth 2.0 Client ID |
| `security.auth.oauth.google.apiSecret` | Yes | Google OAuth 2.0 Client Secret |
| `security.auth.oauth.callback.ui` | Yes | Frontend SPA URL to redirect to after OTC generation (e.g., SPA home page) |
| `server.forward-headers-strategy` | Yes (for proxies) | Set to `NATIVE` for reverse proxy deployments to enable X-Forwarded-* header processing |

To obtain Google credentials:
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create or select a project
3. Enable the Google+ API
4. Create an OAuth 2.0 Client ID credential (Web application type)
5. Add **one authorized redirect URI** for your deployment:
   - **Local development:** `http://localhost:8080/WebAPI/user/oauth/callback/google`
   - **Production via proxy:** `https://my-domain.example.com/WebAPI/user/oauth/callback/google`
6. Copy Client ID and Client Secret to your configuration

**Note:** You only need to register the base redirect URI. The `/google` suffix is appended by Spring automatically via the template mechanism.

### Generic OIDC Providers

Any OIDC-compliant provider can be used via the generic OIDC configuration:

**`application.yaml`:**

```yaml
server:
  forward-headers-strategy: NATIVE  # Enable reverse proxy header support

security:
  auth:
    oidc:
      enabled: true
      url: "https://your-idp.example.com"  # OIDC issuer/discovery URL
      clientId: "YOUR_CLIENT_ID"
      clientSecret: "YOUR_CLIENT_SECRET"
      rolesClaim: "roles"  # Name of claim containing user roles/groups
      rolesToUpperCase: true  # Convert roles to uppercase
    oauth:
      callback:
        ui: "https://my-domain.example.com/myapp/#/home"  # Frontend URL
```

| Property | Required | Description |
|---|---|---|
| `security.auth.oidc.enabled` | Yes | Set to `true` to enable OIDC |
| `security.auth.oidc.url` | Yes | OIDC provider's issuer/discovery URL (e.g., `https://your-idp.example.com`). WebAPI will fetch `.well-known/openid-configuration` automatically. |
| `security.auth.oidc.clientId` | Yes | OIDC client ID from your provider |
| `security.auth.oidc.clientSecret` | Yes | OIDC client secret |
| `security.auth.oidc.rolesClaim` | No | Name of the claim in ID token containing user roles/groups (e.g., `roles`, `groups`, `resource_access.webapi.roles`) |
| `security.auth.oidc.rolesToUpperCase` | No | Set to `true` to convert claim roles to uppercase before mapping |
| `security.auth.oidc.externalUrl` | No | Optional. For reverse proxy scenarios where authorization endpoint needs rewriting. |
| `security.auth.oauth.callback.ui` | Yes | Frontend SPA URL to redirect to after OTC generation |

**Registering the redirect URI with your OIDC provider:**

Register a single redirect URI with your provider:
```
https://my-domain.example.com/WebAPI/user/oauth/callback/openid
```

Spring's template mechanism handles the rest. The `/openid` suffix is appended automatically by the `{registrationId}` template replacement.

---

## Using OTC in a Frontend Application

### Step 1: Initiate OAuth login

Direct the user to WebAPI's OAuth login endpoint:

```html
<!-- Login button on SPA -->
<a href="http://localhost:8080/WebAPI/user/login/google">
  Login with Google
</a>
```

Alternatively, open in a new window:

```javascript
window.location.href = "http://localhost:8080/WebAPI/user/login/google";
```

---

### Step 2: Handle the OAuth callback redirect

Google redirects the user back to your SPA with the OTC as a query parameter:

```
http://localhost/Atlas/#/home?code=550e8400-e29b-41d4-a716-446655440000
```

Your SPA should extract the `code` parameter and immediately exchange it for a JWT.

**Example (Vue.js):**

```javascript
// In your router or login component
export default {
  name: 'LoginCallback',
  async mounted() {
    const params = new URLSearchParams(window.location.search);
    const otcCode = params.get('code');
    
    if (otcCode) {
      try {
        const response = await fetch('/WebAPI/user/login/otc?code=' + otcCode);
        if (response.ok) {
          const data = await response.json();
          // Store JWT for future requests
          localStorage.setItem('jwt_token', data.jwt);
          localStorage.setItem('user_login', data.user.login);
          // Redirect to home or dashboard
          this.$router.push('/home');
        } else {
          // Invalid or expired OTC
          this.$router.push('/login?error=invalid_code');
        }
      } catch (error) {
        console.error('Failed to redeem OTC:', error);
        this.$router.push('/login?error=server_error');
      }
    }
  }
};
```

---

### Step 3: Use the JWT for authenticated requests

Store the JWT and include it in the `Authorization` header on all subsequent requests:

```javascript
// Making an authenticated API call
const jwt = localStorage.getItem('jwt_token');

fetch('http://localhost:8080/WebAPI/cohortdefinition', {
  headers: {
    'Authorization': 'Bearer ' + jwt,
    'Content-Type': 'application/json'
  }
})
.then(response => response.json())
.then(data => console.log(data));
```

---

## Example Workflows (bash)

The examples below assume:
- WebAPI is running at `http://localhost:8080`
- Google OAuth is configured and enabled
- Frontend SPA is at `http://localhost/Atlas`

### Simulate a Google OAuth login and obtain JWT

```bash
#!/bin/bash

# This example demonstrates the OTC flow manually for testing.
# In production, use a browser to initiate the OAuth flow.

# Step 1: Simulate user consent and auth code exchange
# (Normally done by browser and handled by GoogleAuthConfig)
# This step is omitted here; assume WebAPI generated an OTC internally

# Step 2: Redeem the OTC to get a JWT
# Substitute the actual OTC from the OAuth redirect

OTC="550e8400-e29b-41d4-a716-446655440000"

RESPONSE=$(curl -s "http://localhost:8080/WebAPI/user/login/otc?code=$OTC")

echo "Response from OTC redemption:"
echo "$RESPONSE" | jq .

# Extract the JWT
JWT=$(echo "$RESPONSE" | jq -r '.jwt')
LOGIN=$(echo "$RESPONSE" | jq -r '.user.login')

echo "JWT: $JWT"
echo "Login: $LOGIN"
```

---

### Use the JWT to call a protected endpoint

```bash
JWT="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -s \
  -H "Authorization: Bearer $JWT" \
  http://localhost:8080/WebAPI/cohortdefinition | jq .
```

---

## Configuration Reference

### OTC Properties

**`security.auth.otc.ttl`** (default: `10m`)

How long a One-Time Code remains valid before expiration.

Format: Spring `Duration` (e.g., `10m`, `600s`, `PT10M`)

```yaml
security:
  auth:
    otc:
      ttl: 10m  # OTC valid for 10 minutes after creation
```

---

### OAuth Callback Properties

**`security.auth.oauth.callback.ui`** (required)

The frontend SPA URL where users should be redirected after successful OAuth
authentication. The OTC will be appended as a query parameter:

```
{callback.ui}?code={OTC}
```

Example:

```yaml
security:
  auth:
    oauth:
      callback:
        ui: "http://localhost/Atlas/#/home"
```

Result after OAuth: `http://localhost/Atlas/#/home?code=550e8400-e29b-41d4-a716-446655440000`

---

### Server Properties for Reverse Proxy Support

**`server.forward-headers-strategy`** (default: `NONE`)

When WebAPI is deployed behind a reverse proxy or load balancer, enable this setting
to automatically process X-Forwarded headers. Required for correct `{baseUrl}` resolution
in OAuth redirect URI templates.

```yaml
server:
  forward-headers-strategy: NATIVE
```

With this enabled, Spring Security processes:
- `X-Forwarded-Proto` → Protocol (http/https)
- `X-Forwarded-Host` → Hostname
- `X-Forwarded-Port` → Custom port
- `X-Forwarded-Prefix` → Context path prefix

**Example:** A request to `https://my-domain.example.com/WebAPI/user/login/google`
with header `X-Forwarded-Host: my-domain.example.com` resolves the redirect URI template
to `https://my-domain.example.com/WebAPI/user/oauth/callback/google` automatically.

---

## Spring Security Template-Based Redirect URI Resolution

WebAPI leverages Spring Security 6.x's native template mechanism to automatically
resolve OAuth2 callback URIs from the incoming HTTP request context. This eliminates
the need for hardcoded callback URLs per environment.

### How It Works

**Template Pattern:**
```
{baseUrl}/user/oauth/callback/{registrationId}
```

**Template Variables:**
- **`{baseUrl}`** — Automatically extracted from request
  - Includes protocol, hostname, port, and context path
  - Respects X-Forwarded headers for reverse proxy scenarios
  - Example: `https://my-domain.example.com:8443/WebAPI`

- **`{registrationId}`** — Provider identifier
  - `google` for Google OAuth
  - `openid` for generic OIDC
  - Automatically replaced by Spring Security

**Resolution Examples:**

| Scenario | Request URL | Resolved Callback URI |
|---|---|---|
| Local development | `http://localhost:8080/WebAPI/user/login/google` | `http://localhost:8080/WebAPI/user/oauth/callback/google` |
| Reverse proxy (nginx) | `https://my-domain.example.com/WebAPI/user/login/google` | `https://my-domain.example.com/WebAPI/user/oauth/callback/google` |
| Non-standard port | `https://my-domain.example.com:9443/WebAPI/user/login/google` | `https://my-domain.example.com:9443/WebAPI/user/oauth/callback/google` |
| Different context path | `https://my-domain.example.com/v3/user/login/google` | `https://my-domain.example.com/v3/user/oauth/callback/google` |

### Enabling X-Forwarded Header Support

For deployments behind reverse proxies (nginx, Apache, AWS ALB, etc.), configure
Spring to process forwarded headers:

```yaml
server:
  forward-headers-strategy: NATIVE
```

This enables automatic processing of:
- **`X-Forwarded-Proto`** — Original client protocol (http/https)
- **`X-Forwarded-Host`** — Original hostname
- **`X-Forwarded-Port`** — Original port (if non-standard)
- **`X-Forwarded-Prefix`** — Path prefix added by proxy

**Nginx example header forwarding:**
```nginx
location /WebAPI/ {
    proxy_pass http://webapi-backend:8080/WebAPI/;
    proxy_set_header X-Forwarded-Proto $scheme;
    proxy_set_header X-Forwarded-Host $server_name;
    proxy_set_header X-Forwarded-Port $server_port;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
```

### Advantages

✅ **Single registration per provider** — Register only one callback URI with your OAuth provider  
✅ **Load-balanced ready** — Works seamlessly across multiple instances  
✅ **Reverse proxy compatible** — Automatic header processing  
✅ **No hardcoded URLs** — Eliminates environment-specific configuration  
✅ **Works across contexts** — Supports different deployment paths (/WebAPI, /v3, etc.)

---

## Session Management

WebAPI uses a **stateless** session policy for OAuth authentication:

```
SecurityFilterChain → SessionCreationPolicy.STATELESS
├─ No JSESSIONID cookies created
├─ No HTTP session maintained across requests
└─ JWT is the only state (carried in Authorization header)
```

This approach:
- **Simplifies load balancing:** Requests can be routed to any instance
- **Prevents session bleeding:** OAuth session isolated from Windows auth session
- **Enables multi-domain SPA:** Frontend on different domain from backend
- **Stateless during OAuth:** OTC handled via database, not session

---

## Database Cleanup

One-Time Codes are automatically cleaned up via scheduled tasks:

**Expired codes** (created > 10 minutes ago):
```sql
DELETE FROM sec_one_time_code WHERE expires_at < NOW();
```

**Revoked codes** (successfully redeemed):
```sql
DELETE FROM sec_one_time_code WHERE revoked = true;
```

These cleanup tasks run periodically (frequency configured in application).

---

## Troubleshooting

### OTC Redemption Returns 401 Unauthorized

**Possible causes:**

1. **Code not found:** The OTC doesn't exist in the database
   - Verify the code was typed/transmitted correctly
   - Check if the code was generated recently (should be in the database for ~10 minutes)

2. **Code expired:** The OTC was created more than 10 minutes ago
   - OTCs have a short TTL (10 minutes by default)
   - Have the user repeat the login flow to get a fresh OTC

3. **Code already revoked:** The OTC was already redeemed (single-use)
   - OTCs are marked revoked immediately after redemption
   - Have the user repeat the login flow to get a new OTC

**Debug queries:**

```sql
-- Find all OTCs for a specific login
SELECT code, login, created_at, expires_at, revoked
FROM sec_one_time_code
WHERE login = 'alice@example.com'
ORDER BY created_at DESC;

-- Check if code has expired
SELECT code, created_at, expires_at, 
       CASE WHEN expires_at < NOW() THEN 'EXPIRED' ELSE 'VALID' END as status
FROM sec_one_time_code
WHERE code = '550e8400-e29b-41d4-a716-446655440000';

-- Check if code was already used
SELECT code, revoked, created_at
FROM sec_one_time_code
WHERE code = '550e8400-e29b-41d4-a716-446655440000';
```

---

### Google OAuth Returns Error

**"invalid_client" or "unauthorized_client":**
- Verify `security.auth.oauth.google.apiKey` and `apiSecret` are correct
- Verify the redirect URI is registered in Google Cloud Console:
  `{callback.api}/google`

**"access_denied" or user cancels:**
- User clicked "Cancel" on Google's login dialog
- No OTC is generated; user should repeat the flow

---

## See Also

- [LoginPipeline.md](LoginPipeline.md) — High-level overview of authentication flows
- [APIKey_HOWTO.md](APIKey_HOWTO.md) — Personal API key authentication (alternative method)
- [EntityAccess_HOWTO.md](EntityAccess_HOWTO.md) — Authorization and role-based access control
