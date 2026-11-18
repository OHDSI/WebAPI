# Trusted Assertion Authentication — Configuration & Setup Guide

This document describes how to sign users in to WebAPI from an authenticating
reverse proxy. The proxy (nginx `auth_request`, oauth2-proxy, Apache
`mod_auth_openidc`, or an institutional SSO gateway) has already signed the
user in. It adds a short-lived **signed JWT**, the *assertion*, to each request
it forwards. WebAPI verifies the assertion against the proxy's JWKS and logs
the user in through the normal login pipeline.

Unlike a plain `REMOTE_USER`-style header, an assertion can't be forged by
someone who reaches WebAPI without going through the proxy. WebAPI trusts the
signature, not the network path.

---

## Overview

When trusted assertion authentication is enabled:

1. The proxy authenticates the browser and adds the assertion header
   (`sso-assertion` by default) to every request it forwards to `/WebAPI/`.
2. Atlas lists the provider from `/auth/providers` and calls
   `GET /user/login/assertion`. WebAPI verifies the assertion and returns a
   WebAPI JWT, as every other provider does.
3. With `enforce-on-every-request` (the default), every later request made with
   that WebAPI JWT must also carry a valid assertion **for the same user**.

Step 3 ties the WebAPI session to the proxy session:

- A leaked WebAPI JWT is useless without a live proxy session.
- Signing out at the proxy takes effect within the assertion's lifetime, not
  the WebAPI JWT's.

API-key requests (see [APIKey_HOWTO.md](APIKey_HOWTO.md)) and anonymous
requests are not affected.

Atlas needs no changes. The provider is listed in `/auth/providers` as an
`ajax` provider with no credentials form.

---

## Architecture

```
Browser
    │  (signed in at the proxy)
    ▼
Authenticating reverse proxy
    │  adds  sso-assertion: <signed JWT>
    ▼
GET /user/login/assertion  (permitAll security chain)
    │
    ▼
AssertionLoginController
    │
    ├─▶ AssertionVerifier ──▶ NimbusJwtDecoder ──HTTPS──▶ proxy's JWKS
    │        (alg allow-list; iss, aud and exp checks)
    ▼
LoginService.onSuccess(AuthenticatedLogin origin=ASSERTION)
    │
    ▼
WebAPI JWT returned to Atlas

Later requests (Authorization: Bearer <WebAPI JWT> + sso-assertion)
    │
    ▼
Spring Security resolves the WebAPI JWT
    │
    ▼
AssertionBindingFilter  ── assertion user must equal the token's user
```

### Key classes

| Class | Package | Responsibility |
|---|---|---|
| `AssertionAuthConfig` | `security.authc.assertion` | Builds the JWT decoder, the permit-all login chain and the binding filter |
| `AssertionAuthProperties` | `security.authc.assertion` | Properties class for `security.auth.assertion.*` |
| `AssertionVerifier` | `security.authc.assertion` | Reads the header, verifies it, and sorts failures into missing, invalid or unavailable |
| `AssertionLoginController` | `security.authc.assertion` | `GET /user/login/assertion`; builds the `AuthenticatedLogin` |
| `AssertionBindingFilter` | `security.authc.assertion` | Per-request check that the assertion user matches the WebAPI JWT user |
| `AuthProviderService` | `auth` | Lists the provider in `/auth/providers` |

---

## Configuration (application.yaml)

All settings live under `security.auth.assertion`. The provider is disabled by
default.

```yaml
security:
  auth:
    assertion:
      enabled: true
      header: sso-assertion                     # request header carrying the JWT
      jwk-set-uri: https://sso.example.org/.well-known/jwks.json
      issuer: https://sso.example.org           # exact iss
      audience: ohdsi-webapi                    # value aud must contain
      algorithms: ES256                         # comma-separated, asymmetric only
      clock-skew: 30s
      jwks-timeout: 5s
      auto-provision: true
      enforce-on-every-request: true
      provider-name: Single Sign-On             # label shown in Atlas
      logout-url: https://sso.example.org/logout
```

`jwk-set-uri`, `issuer` and `audience` are required when the provider is
enabled. WebAPI refuses to start without them.

### Property reference

| Property | Type | Default | Description |
|---|---|---|---|
| `enabled` | boolean | `false` | Turns the provider on |
| `header` | String | `sso-assertion` | Request header that carries the assertion |
| `jwk-set-uri` | String | — | JWKS endpoint with the proxy's public signing keys. **Required** |
| `issuer` | String | — | Exact `iss` the assertion must carry. **Required** |
| `audience` | String | — | Value the assertion's `aud` must contain. **Required** |
| `algorithms` | List | `ES256` | Accepted JWS algorithms. Only asymmetric algorithms are allowed; `HS*` and `none` are refused |
| `clock-skew` | Duration | `30s` | Allowed clock difference when checking `exp` and `nbf` |
| `jwks-timeout` | Duration | `5s` | Connect and read timeout for fetching the JWKS |
| `username-claim` | String | `sub` | Claim holding the WebAPI login. The value is trimmed and lower-cased |
| `given-name-claim` | String | `given_name` | Claim used for the display name |
| `family-name-claim` | String | `family_name` | Claim used for the display name. The login is used when both name claims are empty |
| `auto-provision` | boolean | `true` | Create the WebAPI user on first login. When `false`, unknown users get 403 |
| `enforce-on-every-request` | boolean | `true` | Require a matching assertion on every request made with a WebAPI JWT, not just at login |
| `provider-name` | String | `Single Sign-On` | Name shown for this provider in `/auth/providers` |
| `logout-url` | String | — | Logout URL handed to the client, normally the proxy's own sign-out page |

### What the assertion must contain

| Claim / header | Requirement |
|---|---|
| `alg` (header) | One of `algorithms` |
| `kid` (header) | A key present in the JWKS |
| `iss` | Equal to `issuer` |
| `aud` | Contains `audience` |
| `exp` | **Present** and in the future, within `clock-skew`. An assertion without `exp` is refused |
| `nbf` | If present, in the past, within `clock-skew` |
| `sub` (or `username-claim`) | Non-blank; becomes the WebAPI login |
| `given_name`, `family_name` | Optional; used for the display name |

Keep assertion lifetimes short; one to five minutes is typical. With
`enforce-on-every-request`, the lifetime is the longest a session can outlive
a sign-out at the proxy.

---

## Roles

Assertions carry no roles. A newly provisioned user gets the roles in
`security.defaultRoles`. Further roles are assigned by an administrator in
Atlas. Role sync on login only touches roles of the login's own origin, so
roles an administrator assigns survive later logins. See
[LoginPipeline.md](LoginPipeline.md).

---

## Proxy Setup

The proxy must:

1. **Replace** any `sso-assertion` header sent by the client with its own,
   never pass a client's copy through. WebAPI would reject a forged one, but
   only the proxy should be supplying it.
2. Send the assertion on **every** request to `/WebAPI/`, not just the login
   call, when `enforce-on-every-request` is on.
3. Pass WebAPI's own 401, 403 and 503 responses back to Atlas unchanged. If an
   `error_page` or redirect-to-login turns them into an HTML page, Atlas can't
   read them.
4. Serve the JWKS somewhere WebAPI can reach over HTTPS.

An nginx example, where the SSO service answers `auth_request` subrequests
and returns the assertion in a response header:

```nginx
location = /_sso_check {
    internal;
    proxy_pass              https://sso.example.org/check;
    proxy_pass_request_body off;
    proxy_set_header        Content-Length "";
    proxy_set_header        X-Original-URI $request_uri;
}

location /WebAPI/ {
    auth_request     /_sso_check;
    auth_request_set $sso_assertion $upstream_http_sso_assertion;

    # Replaces any sso-assertion header the client sent.
    proxy_set_header sso-assertion $sso_assertion;

    # WebAPI's JSON 401/403/503 go back to Atlas as they are.
    proxy_intercept_errors off;

    proxy_pass http://webapi:8080/WebAPI/;
}
```

Atlas's static files need no assertion; only `/WebAPI/` does.

---

## REST Endpoints

### `GET /user/login/assertion` — Exchange an assertion for a WebAPI JWT

No WebAPI token is needed. The endpoint is how one is obtained.

**Request**

```http
GET /WebAPI/user/login/assertion
sso-assertion: eyJhbGciOiJFUzI1NiIsImtpZCI6...
```

**Success (200 OK):** the same JSON body every login endpoint returns, from
`LoginService`, including the WebAPI JWT.

**Errors**

| Status | `x-auth-error` header | Cause |
|---|---|---|
| 401 | `Assertion required` | Header missing or blank |
| 401 | `Invalid assertion` | Bad signature, unknown `kid`, disallowed `alg`, wrong `iss`/`aud`, missing or past `exp`, no username claim, or not a JWT |
| 403 | `User is not registered` | `auto-provision: false` and the user doesn't exist in WebAPI |
| 503 | `Identity provider unavailable` | The JWKS couldn't be fetched |

### Per-request binding

With `enforce-on-every-request: true`, any request authenticated by a WebAPI
JWT is checked after Spring Security resolves the token:

| Status | `x-auth-error` header | Cause |
|---|---|---|
| 401 | `Assertion required` | No assertion on the request |
| 401 | `Invalid assertion` | As above |
| 401 | `Assertion does not match the session user` | The assertion is for a different user. Logged as `SECURITY_AUDIT` |
| 503 | `Identity provider unavailable` | The JWKS couldn't be fetched |

Requests authenticated by an API key, and anonymous requests, pass through
unchecked.

---

## Security Considerations

- **Signature, not network path.** Every assertion is verified against the
  JWKS. Even so, restrict direct access to WebAPI so the proxy is the only
  way in.
- **Algorithm allow-list.** Only the configured asymmetric algorithms are
  accepted. Symmetric (`HS*`) and `none` are refused when the configuration is
  loaded and again when a token is verified.
- **`exp` is mandatory.** Spring's `JwtTimestampValidator` only checks `exp`
  when it is present; this provider also requires it, so no assertion is
  valid forever.
- **Outage is not sign-out.** If the JWKS can't be fetched, WebAPI answers
  503, not 401, so an identity-provider outage doesn't look like every user
  being signed out. JWKS fetches time out after `jwks-timeout`.
- **Session binding.** Leave `enforce-on-every-request` on unless the proxy
  can't add the assertion to every request. With it off, a WebAPI JWT stays
  valid for its own lifetime regardless of the proxy session.
- **Provisioning.** With `auto-provision: true`, anyone the proxy lets
  through gets a WebAPI account with default roles. Set it to `false` if the
  proxy admits a wider population than should use Atlas.
- **User origin.** Users signed in this way are recorded with
  `UserOrigin.ASSERTION`. The origin is stored as a string, so no database
  migration is needed.

---

## Testing

`AssertionVerifierTest`, `AssertionLoginControllerTest` and
`AssertionBindingFilterTest` start a real local JWKS server, so the production
decoder, including its key fetch, is exercised. Run them with:

```sh
./mvnw test -Dtest='Assertion*'
```
