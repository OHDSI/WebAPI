# Unified Login Pipeline

## Overview

WebAPI implements a **unified authentication pipeline** that consolidates all authentication types (Database, LDAP, Windows/Kerberos, and OIDC) through a single entry point. This design ensures consistent user lifecycle management, role synchronization, and JWT token generation across all authentication methods.

**Key Achievement:** All authentication types converge through `LoginService.onSuccess(AuthenticatedLogin)`, eliminating code duplication and providing a single source of truth for login orchestration.

## Architecture

### The Login Pipeline Flow

```
┌─────────────────────────────────────────────────────────────────┐
│                    HTTP Authentication Request                   │
└────────────┬────────────────────────────────────────────────────┘
             │
             ├──────────────────┬──────────────────┬──────────────┐
             │                  │                  │              │
      ┌──────▼─────┐    ┌──────▼─────┐    ┌──────▼─────┐   ┌──────▼──────┐
      │  Database  │    │    LDAP    │    │  Windows   │   │    OIDC     │
      │   Auth     │    │   Auth     │    │   Auth     │   │   Auth      │
      │ (POST/GET) │    │   (GET)    │    │   (GET)    │   │  (Redirect/ │
      │            │    │            │    │            │   │   Bearer)   │
      └──────┬─────┘    └──────┬─────┘    └──────┬─────┘   └──────┬──────┘
             │                  │                  │              │
             │                  ▼                  ▼              ▼
             │          ┌────────────────────────────────┐
             │          │   Group-to-Role Mappers        │
             │          │ (Phase 1: Return Empty Set)    │
             │          │ (Phase 2: Actual Mapping)      │
             │          └────────────────────────────────┘
             │                    │
             └────────┬───────────┴────────┬────────────┘
                      │                    │
                      ▼                    ▼
         ┌────────────────────────────────────────┐
         │   Build AuthenticatedLogin             │
         │   ├─ login (normalized)                │
         │   ├─ name (display name)               │
         │   ├─ origin (UserOrigin enum)          │
         │   ├─ roles (Set<String>)               │
         │   └─ originAuthentication (for debug)  │
         └────────────────┬─────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────────────┐
         │   LoginService.onSuccess()             │
         │   (Single Orchestration Point)         │
         │                                        │
         │   1. Lowercase login name              │
         │   2. Ensure user exists in DB          │
         │   3. Sync roles by origin              │
         │   4. Create session                    │
         │   5. Generate JWT                      │
         │   6. Return Result                     │
         └────────────────┬─────────────────────┘
                          │
                          ▼
         ┌────────────────────────────────────────┐
         │   LoginService.Result                  │
         │   ├─ login                             │
         │   ├─ jwt (signed token)                │
         │   ├─ roles (final DB roles)            │
         │   └─ message                           │
         └────────────────────────────────────────┘
```

### Core Components

#### 1. **AuthenticatedLogin** — The Bridge Pattern

The `AuthenticatedLogin` class is the **normalization bridge** between disparate authentication sources and the unified login orchestration logic.

**Purpose:** Convert auth-specific data (Spring Authentication, JWT claims, LDAP attributes, etc.) into a standardized format.

**Key Fields:**
```java
public class AuthenticatedLogin {
    private String login;                          // Normalized login (lowercase)
    private String name;                           // Display name
    private UserOrigin origin;                     // Where the user came from
    private Set<String> roles;                     // Pre-extracted WebAPI role names
    private Authentication originAuthentication;   // Original Spring Auth (for debugging)
    private Map<String, Object> attributes;        // Optional auth-specific data
}
```

**Why a Bridge?**
- Different auth methods provide user identity in different ways:
  - Database: direct username/password
  - LDAP: DN + group memberships + attributes
  - Windows: Kerberos SPN + group SIDs
  - OIDC: JWT claims (subject, name, groups/roles)
- Each auth handler extracts what it needs, builds an `AuthenticatedLogin`, and calls `LoginService.onSuccess()`
- LoginService doesn't need to know authentication source details

**Immutability Design:**
- `roles` is an immutable `Set<String>` to prevent accidental modification
- Built via fluent builder pattern with validation
- Ensures consistency across all auth paths

#### 2. **LoginService** — Orchestration Hub

The `LoginService.onSuccess(AuthenticatedLogin)` method is the **single entry point** for ALL authentication types. It handles the complete login lifecycle:

```java
@Transactional
public Result onSuccess(AuthenticatedLogin authenticatedLogin) {
    // 1. Normalize login name
    String login = authenticatedLogin.getLogin().toLowerCase();
    
    // 2. Ensure user exists (create or update)
    authorizationService.ensureUserExists(
        login, 
        authenticatedLogin.getName(), 
        authenticatedLogin.getOrigin(), 
        defaultRoles
    );
    
    // 3. Sync roles (origin-aware)
    syncRoles(login, authenticatedLogin.getOrigin(), authenticatedLogin.getRoles());
    
    // 4. Create session
    UUID sessionId = sessionService.createSession(login);
    
    // 5. Generate JWT
    String jwt = jwtService.generateToken(login, sessionId.toString(), expiresAt);
    
    // 6. Fetch and return final roles
    String[] roles = authorizationService.getUserRoles(login)
        .stream()
        .map(Role::name)
        .toArray(String[]::new);
    
    return new Result(login, jwt, roles, "Login successful");
}
```

**Key Design Decision: syncRoles() is Origin-Aware**

The `syncRoles()` method **only modifies roles assigned by the current authentication origin:**

```java
private void syncRoles(String login, UserOrigin origin, Set<String> targetRoles) {
    // Get current roles assigned by THIS origin
    List<String> currentOriginRoles = authorizationService.getRolesByOrigin(login, origin);
    
    // Add new roles from this origin
    for (String roleName : targetRoles) {
        if (!currentOriginRoles.contains(roleName)) {
            authorizationService.addUserToRole(roleName, login, origin);
        }
    }
    
    // Remove roles no longer in target (only for this origin)
    for (String roleName : currentOriginRoles) {
        if (!targetRoles.contains(roleName)) {
            authorizationService.removeUserFromRole(roleName, login, origin);
        }
    }
}
```

**Why Origin-Aware Sync?**

Imagine a user authenticated via both LDAP and OIDC:
- LDAP login assigns roles: `["admin", "analyst"]`
- User also has OIDC token with roles: `["analyst"]`
- If OIDC sync removed unmatched roles, it would accidentally remove the LDAP-assigned `admin` role
- Origin-aware sync prevents this: OIDC only removes OIDC-origin roles

#### 3. **Authentication Handlers** — Consistent Pattern

Each authentication method follows the same pattern:

```
1. Authenticate the user (via Spring Security mechanisms)
2. Extract groups/roles from authentication (mappers return empty in Phase 1)
3. Build AuthenticatedLogin with normalized data
4. Call loginService.onSuccess(authenticatedLogin)
5. Return Result (JWT + roles) to client
```

**Database Handler Example** (`LoginController.Database`):
```java
@GetMapping("/db")
public Result getDbLogin(Authentication auth) {
    AuthenticatedLogin login = AuthenticatedLogin.builder()
        .login(auth.getName())
        .name(auth.getName())
        .origin(UserOrigin.DATABASE)
        .roles(Collections.emptySet())  // DB auth has no groups
        .originAuthentication(auth)
        .build();
    
    return loginService.onSuccess(login);
}
```

**LDAP Handler Example** (`LoginController.Ldap`):
```java
@GetMapping("/ldap")
public Result getLdapLogin(Authentication auth) {
    // Groups come from Spring's LDAP authorities
    Set<String> mappedRoles = ldapGroupToRoleMapper.mapGroupsToRoles(
        auth.getAuthorities(),
        LdapProviderType.LDAP
    );
    
    AuthenticatedLogin login = AuthenticatedLogin.builder()
        .login(auth.getName())
        .name(auth.getName())
        .origin(UserOrigin.LDAP)
        .roles(mappedRoles)  // Phase 1: empty, Phase 2: actual groups
        .originAuthentication(auth)
        .build();
    
    return loginService.onSuccess(login);
}
```

**OIDC Handler Example** (`OidcAuthConfig.handleSuccess`):
```java
public void handleSuccess(OAuth2User user, ...) {
    OidcUser oidcUser = (OidcUser) user;
    
    // Extract roles from JWT claims
    Set<String> mappedRoles = oidcGroupToRoleMapper.extractAndMapRoles(
        oidcUser.getClaims(),
        rolesClaim,
        rolesToUpperCase
    );
    
    AuthenticatedLogin login = AuthenticatedLogin.builder()
        .login(oidcUser.getSubject())
        .name(oidcUser.getFullName())
        .origin(UserOrigin.OIDC)
        .roles(mappedRoles)  // Phase 1: empty, Phase 2: JWT claims
        .originAuthentication(originalOAuth2Auth)
        .build();
    
    Result result = loginService.onSuccess(login);
    redirectToUI(result.jwt);
}
```

## Design Principles

### 1. **Single Responsibility**
- Each component has one reason to change:
  - **Handlers:** How to authenticate this type
  - **Mappers:** How to extract groups from this type
  - **LoginService:** How to complete the login lifecycle
  - **AuthorizationService:** How to manage users and roles

### 2. **Separation of Concerns**
- Authentication ≠ Authorization
  - Handlers focus on authentication (proving identity)
  - LoginService + AuthorizationService focus on authorization (roles and permissions)
- Group mapping separated from login orchestration
  - Mappers are pluggable (Phase 1: empty, Phase 2: real logic)
  - Allows independent testing and evolution

### 3. **No Premature Optimization**
- **Phase 1:** Mappers return empty sets, users get default roles only
- **Phase 2:** Implement group-to-role mapping when infrastructure is clear
- Avoids coupling to LDAP import job infrastructure prematurely

### 4. **Transactional Consistency**
- `onSuccess()` is `@Transactional` — all DB operations atomic
- User creation, role sync, session creation all succeed or all rollback together
- Prevents partial login state

### 5. **Extensibility**
- New auth types add handlers following the same pattern
- New group mapping providers add mappers following the same interface
- AuthenticatedLogin fields allow carrying arbitrary auth-specific data

## How Each Authentication Type Flows

### Database Authentication
1. User submits username/password
2. Spring's `DaoAuthenticationProvider` validates against DB
3. Handler builds `AuthenticatedLogin` with `origin=DATABASE`
4. No groups (RDBMS auth doesn't have group concept)
5. Roles come from default roles only

### LDAP Authentication
1. User submits credentials
2. Spring's `LdapAuthenticationProvider` validates against LDAP server
3. **Phase 1:** `LdapGroupToRoleMapper` returns empty set
4. **Phase 2:** Mapper will query `sec_role_group` table for LDAP group→role mappings
5. Handler builds `AuthenticatedLogin` with `origin=LDAP` and mapped roles
6. `syncRoles()` adds/removes LDAP-origin roles

### Windows/Kerberos Authentication
1. Browser sends Kerberos token via `Authorization: Negotiate`
2. Spring's `NegotiateSecurityFilter` (via Waffle) validates token
3. Spring extracts Windows group SIDs as authorities
4. **Phase 1:** `WindowsGroupToRoleMapper` returns empty set
5. **Phase 2:** Mapper will query ACTIVE_DIRECTORY provider for SID→role mappings
6. Handler builds `AuthenticatedLogin` with `origin=WINDOWS` and mapped roles

### OIDC Authentication (Redirect Flow)
1. User clicks "Login with OIDC Provider"
2. Browser redirected to OIDC provider login page
3. OIDC provider redirects back with authorization code
4. Spring exchanges code for ID token + access token
5. Handler extracts claims from ID token
6. **Phase 1:** `OidcGroupToRoleMapper` returns empty set
7. **Phase 2:** Mapper will extract roles from configurable JWT claim paths
8. Handler builds `AuthenticatedLogin` with `origin=OIDC` and mapped roles
9. Redirects to UI with JWT in fragment

### OIDC Authentication (Direct Bearer Token)
1. API client sends JWT in `Authorization: Bearer` header
2. Spring validates signature via `JwtDecoder`
3. Handler extracts claims directly from JWT
4. **Phase 1:** `OidcGroupToRoleMapper` returns empty set
5. **Phase 2:** Mapper extracts roles from token claims
6. Handler builds `AuthenticatedLogin` with `origin=OIDC` and mapped roles
7. Returns JSON with new JWT (refreshed expiration)

## UserOrigin Tracking

Each user's authentication history is tracked via `UserOrigin` enum:

```java
enum UserOrigin {
    SYSTEM,        // Programmatically created
    AD,            // Active Directory (legacy)
    LDAP,          // LDAP authentication
    WINDOWS,       // Kerberos/SPNEGO
    KERBEROS,      // Alternative Kerberos tracking
    GOOGLE,        // OAuth2 Google
    FACEBOOK,      // OAuth2 Facebook
    DATABASE,      // Local database credentials
    OIDC           // Generic OIDC provider
}
```

**Purpose:**
- Tracks which authentication method created/updated the user
- Enables origin-aware role synchronization
- Supports audit trails and analytics

**User Lifecycle Example:**
```
1. User logs in via LDAP
   → UserEntity created with origin=LDAP
   → LDAP-origin roles assigned

2. User logs in via OIDC
   → UserEntity origin updated to reflect most recent auth? (TBD)
   → OIDC-origin roles added alongside LDAP-origin roles
   → Origin-aware sync ensures neither auth method removes other's roles

3. Admin manually assigns role
   → Role created with origin=SYSTEM
   → Survives all future logins regardless of auth method
```

## Phase 1 vs Phase 2: Group Mapping Strategy

### Why We Deferred Group Mapping

The original design attempt to implement group-to-role mapping immediately ran into **fundamental infrastructure issues:**

1. **LDAP/Windows Groups:** The `sec_role_group` table + `RoleGroupEntity` are tightly coupled to the LDAP import job
   - Import job manages LDAP group discovery, caching, and lifecycle
   - Using it for login-time group mapping requires careful synchronization
   - Risk of stale mappings or unexpected interactions

2. **OIDC Roles:** JWT claim structure varies by OIDC provider
   - Some put roles in `realm_access.roles` (Keycloak)
   - Others use `roles` directly
   - Some use custom claim paths
   - Configurable extraction logic needed, but interaction with existing role filtering unclear

3. **Architectural Mismatch:** The provisioning module (import jobs, bulk operations) and authentication module (login handlers) serve different purposes
   - Import jobs: Periodic bulk sync of users/groups from external source
   - Login handlers: Individual user login, just-in-time provisioning
   - Merging these concerns prematurely creates technical debt

### Phase 1 Solution: Placeholders
All mappers return empty `Set<String>`:
- Users are assigned **default roles only** at login
- No automatic group-based role assignment
- Allows unified pipeline to work immediately
- Baseline for testing and refinement

### Phase 2 Implementation Plan

**Prerequisites:**
1. Clarify relationship between import job and login-time group mapping
2. Design for LDAP group caching/freshness
3. Define OIDC role claim extraction strategy
4. Potentially create new entities/repositories for group mappings

**Implementation:**
1. Implement `LdapGroupToRoleMapper.mapGroupsToRoles()` with real group lookups
2. Implement `WindowsGroupToRoleMapper.mapGroupsToRoles()` for AD integration
3. Implement `OidcGroupToRoleMapper.extractAndMapRoles()` with claim parsing
4. Add configuration for OIDC claim paths and role filtering
5. Add tests for each mapper against real external systems
6. Consider caching for performance

## Extension Points

### Adding a New Authentication Type

1. Create handler class (e.g., `OAuthHandler`)
2. Implement authentication via Spring Security
3. Extract roles via a mapper if applicable
4. Build `AuthenticatedLogin` instance
5. Call `loginService.onSuccess(authenticatedLogin)`

### Adding a New External System Integration

1. Create a new `GroupToRoleMapper` implementation
2. Implement `mapGroupsToRoles()` to query your system's group structure
3. Inject into the authentication handler
4. Handler calls mapper, passes results to `AuthenticatedLogin`

### Modifying Role Synchronization Logic

1. Update `LoginService.syncRoles()` if you need different origin-awareness behavior
2. Or override in `AuthorizationService` if policy change needed
3. Remember: Changes here affect **all** authentication types

## Testing Strategy

- **Unit tests** for each mapper (Phase 2)
- **Integration tests** for each auth handler + LoginService
- **End-to-end tests** for complete login flows
- **Multi-auth tests:** Same user logging in via different methods

