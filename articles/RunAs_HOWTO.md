# Run-As (Impersonation) — How It Works

This document describes the Run-As functionality in WebAPI, which allows
an authorized administrator to impersonate another user.

---

## Overview

Run-As lets an administrator log in as a different user without knowing that
user's credentials. This is useful for:

- **Troubleshooting** — reproducing issues that a specific user reports by
  seeing the application through their permissions.
- **Support** — performing actions on behalf of a user who cannot access the
  system themselves.
- **Testing** — verifying that role and permission assignments produce the
  correct behavior for a given user.

When a run-as request succeeds, WebAPI mints a brand-new JWT for the target
user with a fresh session. The administrator effectively becomes that user
for all subsequent requests made with the new token.

---

## Permission

Run-As is gated by the `admin:run-as` permission. Only users whose roles
include this permission can invoke the endpoint. The permission is seeded by
the baseline migration:

```sql
INSERT INTO sec_permission (value, description)
VALUES ('admin:run-as', 'Run as another user');
```

Assign this permission to a role, then assign that role to the administrators
who need impersonation capability.

---

## Endpoint

```
POST /user/runas
```

### Request

| Parameter | Location | Required | Description |
|-----------|----------|----------|-------------|
| `login`   | query string or form body | Yes | The login of the user to impersonate |

The request must include a valid `Authorization: Bearer <jwt>` header for the
calling user (the administrator).

### Successful Response (200 OK)

```json
{
  "login": "targetuser",
  "jwt": "eyJhbGciOiJIUzI1NiIs...",
  "roles": [],
  "message": "Run-as successful"
}
```

The `jwt` field contains a newly minted token whose `sub` claim is the
target user. The caller should replace their current token with this one.

### Error Responses

| Status | Condition | `x-auth-error` Header |
|--------|-----------|-----------------------|
| **403 Forbidden** | Caller does not have the `admin:run-as` permission | — |
| **404 Not Found** | Target user does not exist in the system | `User not found` |

---

## How It Works

```
Administrator                    WebAPI                         Database
     │                             │                               │
     │  POST /user/runas?login=X   │                               │
     │────────────────────────────►│                               │
     │                             │  @PreAuthorize checks         │
     │                             │  admin:run-as permission      │
     │                             │                               │
     │                             │  Look up user "X"             │
     │                             │──────────────────────────────►│
     │                             │◄──────────────────────────────│
     │                             │                               │
     │                             │  Create session for "X"       │
     │                             │──────────────────────────────►│
     │                             │◄──────────────────────────────│
     │                             │                               │
     │                             │  Mint JWT (sub=X, sid=new)    │
     │                             │                               │
     │  { login, jwt, roles, msg } │                               │
     │◄────────────────────────────│                               │
     │                             │                               │
     │  (subsequent requests use   │                               │
     │   the new JWT as user X)    │                               │
```

### Step by Step

1. **Authorization check** — Spring Security's `@PreAuthorize` verifies that
   the caller has the `admin:run-as` permission before the endpoint method
   executes. If not, a 403 is returned immediately.

2. **Target user lookup** — `AuthorizationService.getUserByLogin()` searches
   for the target user. If the user does not exist, a 404 with the
   `x-auth-error: User not found` header is returned.

3. **Session creation** — A new session is created for the target user via
   `SessionService.createSession()`. This is a standard session entry in
   `sec_session`, identical to one created during normal login.

4. **JWT minting** — A JWT is generated with `sub` = target login and
   `sid` = the new session ID, using the same `JwtService.generateToken()`
   used by all other login flows.

5. **Response** — The JWT is returned in a `LoginService.Result` JSON object.
   The Atlas UI replaces its stored token with this new JWT and reloads the
   user's permissions via `GET /user/me`.

---

## Returning to Your Own Identity

Run-As does not maintain a stack or track the original administrator identity
in the token. To return to your own account, simply log out and log back in
with your own credentials.

---

## Nested Run-As

Nested impersonation is allowed. If the target user also has the `admin:run-as`
permission, they (or the admin acting as them) can invoke `/user/runas` again
to impersonate yet another user. Each call mints a completely independent JWT
and session.

---

## Atlas UI Integration

The Atlas UI already supports Run-As. When a user with `admin:run-as`
permission is logged in, the welcome screen displays a "Run as" input field
and button. The UI:

1. Checks the permission via `isPermitted('admin:run-as')`.
2. Sends `POST /user/runas` with the `login` parameter.
3. On success, stores the returned JWT and calls `loadUserInfo()` to refresh
   the displayed identity and permissions.
4. On failure, displays the error from the `x-auth-error` response header.

No UI code changes are required — the endpoint contract matches the existing
Atlas implementation.

---

## Implementation Files

| File | Role |
|------|------|
| `LoginController.java` | `POST /user/runas` endpoint with `@PreAuthorize` |
| `LoginService.java` | `runAs()` method — user lookup, session creation, JWT minting |
| `AuthorizationService.java` | `getUserByLogin()` facade to the internal `UserService` |

---

## Security Considerations

- **Audit trail** — All actions performed under a run-as session are attributed
  to the target user, not the original administrator. The session ID in the JWT
  can be used to correlate actions back to the run-as event in the session
  table.
- **Session independence** — The administrator's original session remains valid.
  Logging out of the run-as session does not affect the administrator's own
  session.
- **Single-login policy** — If `security.session.single-login` is enabled,
  creating a run-as session for a target user will revoke any existing sessions
  for that user. Be aware of this when impersonating users who are actively
  logged in.
