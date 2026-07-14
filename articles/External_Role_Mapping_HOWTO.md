# External Role Mapping Developer Guide

## Overview

The External Role Mapping subsystem provides a flexible mechanism to map external authentication identities to WebAPI roles. This enables organizations to maintain role definitions within WebAPI while delegating identity and group management to external authentication systems (LDAP, Active Directory, OIDC/OpenID Connect).

### Use Cases

- **LDAP Integration**: Map LDAP directory groups to WebAPI roles for centralized identity management
- **Windows/Active Directory**: Map Windows security groups to WebAPI roles in hybrid environments
- **OIDC/JWT Claims**: Extract and map custom JWT claims from identity providers to WebAPI roles
- **Role Governance**: Maintain a single source of truth for role definitions while accepting external identity claims

### Key Features

- Bidirectional role sync: Automatically add/remove user roles based on external claims
- CSV bulk import/export: Import mappings in bulk with validation and error reporting
- Origin isolation: Separate role contexts for LDAP, Windows, and OIDC to prevent conflicts
- Per-role traceability: Track description and origin for each mapping
- Atomic transactions: Maintain consistency during role synchronization

---

## Architecture

### Design Patterns

**1. Strategy Pattern (Provider Types)**
```
UserOrigin enum: LDAP, WINDOWS, OIDC, SYSTEM
├── Each origin maintains isolated role mappings
├── Login pipeline routes to appropriate mapper based on provider
└── Diff algorithm respects origin boundaries (doesn't remove SYSTEM roles)
```

**2. Mapper Pattern (Non-Bean Services)**
```
LdapGroupToRoleMapper       ┐
WindowsGroupToRoleMapper    ├─ Instantiated inline (no Spring beans)
OidcGroupToRoleMapper       ┘
                            │
                            ├─ Extract claims from authentication
                            ├─ Query ExternalRoleMapService
                            └─ Return mapped role names
```

**3. Data Model (Immutable Records)**
```
ExternalRoleMap (record) ──┐
                           ├─ Strongly-typed domain object
MappingRequest (record)    ├─ Type-safe REST API contracts
ImportResult (record)      └─ Prevents accidental mutations
```

### Component Layering

```
┌─────────────────────────────────────────────────┐
│ REST API Layer                                  │
│ ExternalRoleMappingController                   │
│ ├─ CRUD endpoints (/role/mapping)               │
│ ├─ CSV import (POST, PUT, DELETE)               │
│ └─ Validation & error reporting                 │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│ Service Layer                                   │
│ ExternalRoleMapService                          │
│ ├─ Business logic & transactions                │
│ ├─ CSV parsing & validation                     │
│ ├─ Role resolution (claims → role names)        │
│ ├─ Role synchronization (diff algorithm)        │
│ └─ Origin-aware queries                         │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│ Data Access Layer                               │
│ ExternalRoleMapRepository (Spring Data JPA)     │
│ ├─ SQL queries for origin/claim combinations    │
│ ├─ Efficient bulk lookups                       │
│ └─ Transaction management                       │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│ Database                                        │
│ sec_external_role_map (Flyway migration)        │
│ ├─ Composite key (origin, claim, role_id)       │
│ ├─ Indexed for login performance                │
│ └─ 3NF normalized schema                        │
└─────────────────────────────────────────────────┘
```

### Login Pipeline Integration

```
User Login (LDAP/Windows/OIDC)
         │
         ├─ Authentication succeeds
         │
         ├─ Extract external claims/groups
         │  (LdapGroupToRoleMapper, 
         │   WindowsGroupToRoleMapper,
         │   OidcGroupToRoleMapper)
         │
         ├─ ExternalRoleMapService.resolveRoleNames(origin, claims)
         │  └─ Query: SELECT role_name FROM mappings
         │     WHERE origin = ? AND external_claim IN (...)
         │
         ├─ LoginService.syncUserRoles(login, origin, mappedRoles)
         │  ├─ Fetch current roles for user from origin
         │  ├─ Compute additions (new mappings)
         │  ├─ Compute removals (deleted mappings)
         │  ├─ Preserve SYSTEM origin roles (no removal)
         │  └─ Persist changes
         │
         └─ JWT minted with final roles
            (user_roles = additions ∪ SYSTEM roles)
```

---

## Database Schema

### Table: sec_external_role_map

```sql
CREATE TABLE sec_external_role_map (
    id                  SERIAL PRIMARY KEY,
    origin              VARCHAR(50) NOT NULL,        -- LDAP, WINDOWS, OIDC
    external_claim      VARCHAR(255) NOT NULL,        -- Group DN, UPN, JWT claim
    role_id             INTEGER NOT NULL,             -- FK to sec_role
    description         VARCHAR(500),                  -- Admin notes
    
    CONSTRAINT fk_external_role_map_role 
        FOREIGN KEY (role_id) REFERENCES sec_role(id),
    CONSTRAINT uk_external_role_map_origin_claim_role 
        UNIQUE (origin, external_claim, role_id),
    INDEX idx_external_role_map_origin_claim 
        ON (origin, external_claim)
);

-- Support fast lookups during login
SELECT role_name FROM sec_external_role_map m
JOIN sec_role r ON m.role_id = r.id
WHERE m.origin = ? AND m.external_claim IN (...)
ORDER BY m.id;
```

### Migration File: V2.99.0008__external_role_map.sql

Located in `src/main/resources/db/migration/`

```sql
CREATE SEQUENCE sec_external_role_map_seq START 1 INCREMENT 1;

CREATE TABLE sec_external_role_map (
    id INTEGER NOT NULL DEFAULT nextval('sec_external_role_map_seq'),
    origin VARCHAR(50) NOT NULL,
    external_claim VARCHAR(255) NOT NULL,
    role_id INTEGER NOT NULL,
    description VARCHAR(500),
    PRIMARY KEY (id),
    FOREIGN KEY (role_id) REFERENCES sec_role(id),
    UNIQUE (origin, external_claim, role_id)
);

CREATE INDEX idx_external_role_map_origin_claim 
    ON sec_external_role_map(origin, external_claim);
```

---

## Service Layer

### ExternalRoleMapService

Located in: `org.ohdsi.webapi.security.authz.mapping`

**Core Responsibilities:**
- Manage external role mappings (CRUD)
- Resolve external claims to role names during login
- Synchronize user roles based on mapping changes
- Parse and validate CSV import files
- Track import operation results

#### Public API

```java
// Single mapping operations
ExternalRoleMap createMapping(UserOrigin origin, String externalClaim, 
                              Long roleId, String description)
    throws ValidationException, RoleNotFoundException

void removeMapping(Integer mappingId)
    throws MappingNotFoundException

// Batch operations
List<ExternalRoleMap> getMappingsForOrigin(UserOrigin origin)

// Login pipeline (core operation)
Set<String> resolveRoleNames(UserOrigin origin, Collection<String> externalClaims)
    → Returns role names (strings) for JWT and role sync

// User synchronization
void syncUserRoles(String login, UserOrigin origin, Set<String> targetRoles)
    → Adds/removes roles for user from specified origin only

// CSV operations
ImportResult addMappings(MultipartFile csvFile, UserOrigin origin)
    → Add new mappings, skip duplicates

ImportResult deleteMappings(MultipartFile csvFile, UserOrigin origin)
    → Delete specific mappings, best-effort

ImportResult overwriteMappings(MultipartFile csvFile, UserOrigin origin)
    → Replace all mappings for origin with CSV content
```

#### CSV Format

**Strict 3-column format (RFC 4180 compliant):**
```csv
claim,role_name,description
CN=DataAnalysts,OU=Groups,DC=corp,DC=local,Analytics Team,Maps LDAP security group
CN=DBAdmins,OU=Groups,DC=corp,DC=local,Admin,Database administrative team
```

**Validation Rules:**
- Header row must be exactly: `claim,role_name,description`
- Column order is strict (no reordering)
- Non-empty claims and role names required
- Role names must exist in WebAPI
- Description max length: 500 characters
- No duplicate (origin, claim, role_id) tuples in CSV or database

**Import Modes:**
| Mode | HTTP Verb | Behavior | Idempotent |
|------|-----------|----------|-----------|
| Add | POST | Insert new mappings, skip duplicates | Yes |
| Delete | DELETE | Remove specific mappings, ignore missing | Yes |
| Overwrite | PUT | Replace all mappings for origin | Yes |

#### CSV Import Response

```json
{
  "rows": [
    {
      "externalClaim": "CN=DataAnalysts,OU=Groups,DC=corp,DC=local",
      "roleName": "Analytics Team",
      "status": "ADDED",
      "errorMessage": null
    },
    {
      "externalClaim": "CN=InvalidRole,OU=Groups,DC=corp,DC=local",
      "roleName": "NonExistentRole",
      "status": "ERRORED",
      "errorMessage": "Role not found: NonExistentRole"
    }
  ],
  "summary": {
    "addedCount": 1,
    "removedCount": 0,
    "skippedCount": 0,
    "erroredCount": 1
  }
}
```

---

## REST API

### Endpoint: POST /role/mapping

**Create single external role mapping**

**Request:**
```json
{
  "origin": "LDAP",
  "externalClaim": "CN=DataAnalysts,OU=Groups,DC=corp,DC=local",
  "roleId": 42,
  "description": "Maps LDAP security group to Analytics Team role"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "origin": "LDAP",
  "externalClaim": "CN=DataAnalysts,OU=Groups,DC=corp,DC=local",
  "role": {
    "id": 42,
    "name": "Analytics Team"
  },
  "description": "Maps LDAP security group to Analytics Team role"
}
```

**Error Responses:**
- `400 Bad Request` - Invalid origin enum, missing required fields
- `404 Not Found` - Role ID doesn't exist
- `409 Conflict` - Mapping already exists for (origin, claim, role_id)

### Endpoint: GET /role/mapping?origin=LDAP

**List mappings for an origin**

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "origin": "LDAP",
    "externalClaim": "CN=DataAnalysts,OU=Groups,DC=corp,DC=local",
    "role": { "id": 42, "name": "Analytics Team" },
    "description": "..."
  },
  {
    "id": 2,
    "origin": "LDAP",
    "externalClaim": "CN=DBAdmins,OU=Groups,DC=corp,DC=local",
    "role": { "id": 1, "name": "Admin" },
    "description": "..."
  }
]
```

**Query Parameters:**
- `origin` (required): Filter by UserOrigin enum (LDAP, WINDOWS, OIDC)

### Endpoint: GET /role/mapping/all

**List all external role mappings across all origins**

**Response:** `200 OK` - Array of ExternalRoleMap objects (same structure as above)

### Endpoint: DELETE /role/mapping/{id}

**Delete a specific mapping**

**Response:** `204 No Content`

**Error Responses:**
- `404 Not Found` - Mapping ID doesn't exist

### Endpoint: POST /role/mapping/import?origin=LDAP

**Bulk add mappings from CSV file**

**Request:**
```
Content-Type: multipart/form-data

--boundary
Content-Disposition: form-data; name="file"; filename="mappings.csv"
Content-Type: text/csv

claim,role_name,description
CN=DataAnalysts,OU=Groups,DC=corp,DC=local,Analytics Team,LDAP group
CN=DBAdmins,OU=Groups,DC=corp,DC=local,Admin,Database admins
--boundary--
```

**Response:** `200 OK` (with ImportResult)
```json
{
  "rows": [
    { "externalClaim": "CN=DataAnalysts,OU=Groups,DC=corp,DC=local", 
      "roleName": "Analytics Team", "status": "ADDED" },
    { "externalClaim": "CN=DBAdmins,OU=Groups,DC=corp,DC=local", 
      "roleName": "Admin", "status": "ADDED" }
  ],
  "summary": { "addedCount": 2, "removedCount": 0, "skippedCount": 0, "erroredCount": 0 }
}
```

**Query Parameters:**
- `origin` (required): Target origin for mappings

### Endpoint: PUT /role/mapping/import?origin=LDAP

**Replace all mappings for an origin with CSV content**

**Request:** (same multipart format as POST)

**Behavior:**
1. Parse CSV file
2. Calculate diff: (new mappings) - (current mappings)
3. Add new mappings
4. Remove mappings not in CSV
5. Return summary of additions and deletions

**Response:** `200 OK` (ImportResult with add/remove counts)

### Endpoint: DELETE /role/mapping/import?origin=LDAP

**Bulk delete mappings from CSV file**

**Request:** (multipart CSV file, same format)

**Behavior:**
- For each row in CSV, delete mapping for (origin, claim, role_name)
- If mapping doesn't exist, record as SKIPPED (not an error)
- Continue processing all rows even if some fail

**Response:** `200 OK` (ImportResult with removal counts)

---

## Mappers

### Architecture

Mappers are NOT Spring beans (`@Component` removed). They are lightweight, stateless transformation objects instantiated inline in their usage locations. This avoids bean namespace pollution while maintaining type safety and testability.

**Instantiation Pattern:**
```java
public LoginController(..., ExternalRoleMapService externalRoleMapService) {
    this.ldapMapper = new LdapGroupToRoleMapper(externalRoleMapService);
    this.windowsMapper = new WindowsGroupToRoleMapper(externalRoleMapService);
}
```

### LdapGroupToRoleMapper

**Location:** `org.ohdsi.webapi.security.authc`

**Purpose:** Extract LDAP groups from Spring Security authentication and map to WebAPI roles

**Public Methods:**
```java
Set<String> mapGroupsToRoles(Collection<? extends GrantedAuthority> authorities,
                             LdapProviderType providerType)

Set<String> mapGroupsToRoles(Set<String> groupNames, 
                             LdapProviderType providerType)
```

**Implementation:**
1. Extract group names from authorities (remove "ROLE_" prefix if present)
2. Query `ExternalRoleMapService.resolveRoleNames(UserOrigin.LDAP, groupNames)`
3. Return mapped role names

**LdapProviderType Support:**
- `LDAP` - Standard LDAP provider
- `ACTIVE_DIRECTORY` - Microsoft Active Directory (DN format compatibility)

**Example Usage:**
```java
// In LoginController.Ldap.login()
Collection<GrantedAuthority> authorities = ldapAuth.getAuthorities();
Set<String> mappedRoles = ldapMapper.mapGroupsToRoles(authorities, 
                                                       LdapProviderType.LDAP);
// mappedRoles = { "Analytics Team", "Viewer" }
```

### WindowsGroupToRoleMapper

**Location:** `org.ohdsi.webapi.security.authc`

**Purpose:** Extract Windows security groups from Spring Security authentication and map to WebAPI roles

**Public Methods:**
```java
Set<String> mapGroupsToRoles(Collection<? extends GrantedAuthority> authorities)

Set<String> mapGroupsToRoles(Set<String> groupNames)
```

**Implementation:**
1. Extract group names from authorities (remove "ROLE_" prefix)
2. Query `ExternalRoleMapService.resolveRoleNames(UserOrigin.WINDOWS, groupNames)`
3. Return mapped role names

**Example Usage:**
```java
// In LoginController.Windows.login()
Collection<GrantedAuthority> authorities = windowsAuth.getAuthorities();
Set<String> mappedRoles = windowsMapper.mapGroupsToRoles(authorities);
// mappedRoles = { "Admin", "Designer" }
```

### OidcGroupToRoleMapper

**Location:** `org.ohdsi.webapi.security.authc`

**Purpose:** Extract and traverse nested JWT claims from OIDC/OpenID Connect providers and map to WebAPI roles

**Public Method:**
```java
Set<String> extractAndMapRoles(Map<String, Object> claims,
                               String roleClaimPath,
                               boolean toUpperCase)
```

**Parameters:**
- `claims` - Deserialized JWT claims map
- `roleClaimPath` - Dot-notation path to roles array (e.g., "realm_access.roles")
- `toUpperCase` - Convert extracted roles to uppercase before mapping

**Implementation:**
1. Navigate JWT claims object using dot-notation path (e.g., "realm_access.roles" → `claims["realm_access"]["roles"]`)
2. Extract roles as Collection<String>
3. Optionally convert to uppercase
4. Query `ExternalRoleMapService.resolveRoleNames(UserOrigin.OIDC, roleSet)`
5. Return mapped role names

**Example JWT Payload:**
```json
{
  "sub": "user123",
  "realm_access": {
    "roles": ["analyst", "viewer"]
  },
  "preferred_username": "john.doe"
}
```

**Example Usage:**
```java
// In OidcAuthConfig.handleSuccess()
Map<String, Object> claims = oidcAuth.getPrincipal().getAttributes();
Set<String> mappedRoles = oidcMapper.extractAndMapRoles(
    claims,
    "realm_access.roles",
    false  // don't uppercase
);
// mappedRoles = { "Analytics Team", "Viewer" }
```

---

## Role Synchronization Algorithm

The `syncUserRoles()` method implements a diff-based synchronization that:

1. **Fetches current roles** for user from specified origin
2. **Computes additions**: Roles in target set but not in current roles
3. **Computes removals**: Roles in current roles but not in target set
4. **Preserves SYSTEM roles**: Never removes roles created with origin=SYSTEM
5. **Applies atomically**: Single database transaction for consistency

**Pseudocode:**
```
syncUserRoles(login, origin, targetRoles):
    currentRoles = UserRoleRepository.findByUserAndOrigin(login, origin)
    toAdd = targetRoles - currentRoles
    toRemove = currentRoles - targetRoles - systemRoles
    
    INSERT INTO user_role (user_id, role_id) FOR EACH role IN toAdd
    DELETE FROM user_role WHERE role_id IN toRemove AND origin = origin
    
    COMMIT
```

**Example:**
```
Before: user has roles { Admin, Viewer } from LDAP origin
        user has roles { Creator } from SYSTEM origin

Mapped: LDAP groups map to { Analytics Team, Viewer }

After: user has roles { Analytics Team, Viewer } from LDAP origin
       user has roles { Creator } from SYSTEM origin (preserved)

Action: ADD Analytics Team
        REMOVE Admin
        PRESERVE Creator (different origin)
```

---

## Configuration

### Application Properties

Define OIDC role claim path in `application.yaml`:

```yaml
security:
  oidc:
    roleClaimPath: "realm_access.roles"  # Dot-notation path to roles
    roleUppercase: false                  # Convert roles to uppercase
    providerUrl: "https://keycloak.example.com/auth"
    clientId: "webapi-client"
    clientSecret: "${OIDC_CLIENT_SECRET}"
```

### Provider Configuration

**LDAP Configuration:**
```yaml
security:
  ldap:
    providerType: "LDAP"           # or ACTIVE_DIRECTORY
    url: "ldap://ldap.example.com"
    baseDn: "dc=corp,dc=local"
    userSearchBase: "ou=Users"
    groupSearchBase: "ou=Groups"
```

**Windows/IWA Configuration:**
```yaml
security:
  windows:
    enabled: true
    domain: "CORP"
    groupProvider: "active-directory"
```

---

## Testing

### Unit Tests for Role Mapping

```java
class ExternalRoleMapServiceTest {
    
    @Test
    void testResolveRoleNames_MapsLdapGroupToRole() {
        // Given: LDAP mapping CN=Analysts → Analytics Team
        // When: resolveRoleNames(LDAP, { "CN=Analysts,OU=Groups,DC=corp,DC=local" })
        // Then: Returns { "Analytics Team" }
    }
    
    @Test
    void testSyncUserRoles_AddsNewMappedRoles() {
        // Given: User has { Viewer } from LDAP
        // When: syncUserRoles(user, LDAP, { Analyst, Viewer })
        // Then: User has { Analyst, Viewer } from LDAP
    }
    
    @Test
    void testSyncUserRoles_PreservesSystemRoles() {
        // Given: User has { Admin } from SYSTEM, { Viewer } from LDAP
        // When: syncUserRoles(user, LDAP, { Analyst })
        // Then: User has { Admin } from SYSTEM, { Analyst } from LDAP
        // (Admin is NOT removed)
    }
}
```

### Integration Tests for Login Pipeline

```java
class ExternalRoleMappingIntegrationTest {
    
    @Test
    void testLdapLoginFlow_AppliesExternalRoleMappings() {
        // 1. Create LDAP mapping: CN=Analysts → Analyst role
        // 2. Authenticate user with LDAP group CN=Analysts
        // 3. Verify user receives JWT with Analyst role
        // 4. Verify sec_user_role table reflects mapping
    }
}
```

---

## Troubleshooting

### Common Issues

**Issue: Mappings not applied after login**
- Verify CSV was imported successfully (check ImportResult for errors)
- Confirm role names are case-sensitive and exact match
- Check that UserOrigin enum matches authentication provider
- Enable debug logging: `logging.level.org.ohdsi.webapi.security.authz.mapping=DEBUG`

**Issue: CSV import fails with validation errors**
- Verify header row is exactly: `claim,role_name,description`
- Ensure all CSV values are UTF-8 encoded
- Check for trailing whitespace in column names
- Validate role_name values exist in WebAPI

**Issue: User roles not syncing during login**
- Verify external mapper is being called (check logs)
- Confirm external claims/groups are being extracted correctly
- Check sec_external_role_map table has correct mappings
- Verify LoginService.syncUserRoles() is invoked after mapper

**Issue: SYSTEM origin roles being removed**
- This indicates a bug in syncUserRoles() - file a bug report
- syncUserRoles() should always preserve origin != target origin
- Verify you're using correct UserOrigin enum value

### Debug Logging

Enable debug logging for external role mapping:

```yaml
logging:
  level:
    org.ohdsi.webapi.security.authz.mapping.ExternalRoleMapService: DEBUG
    org.ohdsi.webapi.security.authc.LdapGroupToRoleMapper: DEBUG
    org.ohdsi.webapi.security.authc.WindowsGroupToRoleMapper: DEBUG
    org.ohdsi.webapi.security.authc.OidcGroupToRoleMapper: DEBUG
```

---

## Summary

External Role Mapping provides a clean, maintainable architecture for integrating external authentication systems with WebAPI's role-based access control. By leveraging immutable records, stateless mappers, and a flexible CSV import system, organizations can:

- ✅ Maintain single source of truth for role definitions
- ✅ Delegate identity management to external providers
- ✅ Sync roles automatically during login
- ✅ Audit mappings through database records
- ✅ Import/export mappings in bulk without downtime
