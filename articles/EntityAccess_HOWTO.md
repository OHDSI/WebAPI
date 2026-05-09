# Entity-level Access HOWTO

This document explains how WebAPI implements entity-level access checks using Spring Security custom expressions. It shows where the custom SpEL methods live, how entity and access types are exposed for use in annotations, and an example using CohortDefinition.

**Overview**

- **What:** Use Spring Security `@PreAuthorize` with custom SpEL helpers (provided by `WebApiSecurityExpressionRoot`) to enforce owner/global-permission/entity-granted access rules on controller and service methods.
- **Why:** Centralized, declarative checks keep authorization logic consistent and readable across endpoints.

**Key components (WebAPI example)**

- **`security.authz.AuthorizationService`**: runtime helper for current principal, permissions, and user context used by expression implementations.
- **`security.authz.access.EntityAccessService`**: core service that answers whether a user has an explicit access grant (READ/WRITE/etc.) for a given entity instance.
- **`{Entity}AccessEntity`**: a JPA/domain entity pattern representing a granted access record for an entity (who, which entity id, which `AccessType`). Use the `{Entity}` placeholder for specific entities (for example `CohortDefinitionAccessEntity` for cohorts).
- **`{Entity}AccessRepository`**: repository pattern that persists and queries the `{Entity}AccessEntity` records.
- **`security.authz.spring.WebApiSecurityExpressionRoot`**: SpEL root that exposes custom methods like `isOwner(...)`, `isPermitted(...)`, and `hasEntityAccess(...)` (and should expose enum constants for `EntityType` and `AccessType` so they are usable in SpEL expressions).

**How the custom expressions are used**

- The `WebApiSecurityExpressionRoot` supplies methods invoked in `@PreAuthorize` expressions. Example helpers used in WebAPI:
  - `isOwner(id, EntityType)` — true when the current user is the owner of the entity.
  - `isPermitted('read:cohort')` — true when the user has a global permission.
  - `hasEntityAccess(id, EntityType, accessSpec)` — true when EntityAccessService reports the required access (where `accessSpec` can be `READ`, `WRITE`, or helper combinators like `anyOf(READ,WRITE)`).

**Note on enums and SpEL**

- To reference entity types and access types in SpEL (for example `COHORT_DEFINITION` or `READ`), expose enum values as fields on the `WebApiSecurityExpressionRoot` instance. For example, add public fields (or getter properties) like:

```java
public final EntityType COHORT_DEFINITION = EntityType.COHORT_DEFINITION;
public final AccessType READ = AccessType.READ;
public final AccessType WRITE = AccessType.WRITE;
```

  - This enables expressions like `hasAnyEntityAccess(#id, COHORT_DEFINITION, anyOf(READ, WRITE))` to resolve within `@PreAuthorize`.

**Adding a new EntityType (summary steps)**

1. Add the new `EntityType` enum value in the central `EntityType` enum (e.g., `CONCEPT_SET`, `MY_NEW_ENTITY`).
2. Add a domain access entity & repository if entity-level grants are stored separately (e.g., `MyNewEntityAccessEntity`, `MyNewEntityAccessRepository`)
3. Ensure `EntityAccessService` knows how to evaluate access for the new entity type (query the correct repository or storage, apply mapping to `EntityType`).
4. Expose the enum value on `WebApiSecurityExpressionRoot` so SpEL annotations can refer to it.
5. Add or update any service methods (e.g., `MyNewEntityService`) and decorate with `@PreAuthorize` expressions as appropriate.

**Example: CohortDefinition (recommended pattern)**

- Target the service method that performs the operation. For read operations the rules in WebAPI are typically:
  - owner of the cohort OR
  - user has global `read:cohort` permission OR
  - user has global `write:cohort` permission (write implies read) OR
  - user has explicit entity access for READ or WRITE

- Example annotation (place on controller or — preferably — the `CohortDefinitionService` method):

```java
@PreAuthorize("isOwner(#id, COHORT_DEFINITION) or isPermitted(anyOf('read:cohort','write:cohort') or hasAnyEntityAccess(#id, COHORT_DEFINITION, anyOf(READ, WRITE))")
public CohortDTO getCohortDefinition(final int id) { ... }
```

- Explanation of the expression:
  - `isOwner(#id, COHORT_DEFINITION)` — short-circuits grant if caller created/owns the entity.
  - `isPermitted(anyOf('read:cohort','write:cohort'))` — reading a definition is allowed granted global read/write.
  - `hasAnyEntityAccess(#id, COHORT_DEFINITION, anyOf(READ, WRITE))` — delegate to `EntityAccessService` to check explicit grants for this entity id.

**Implementation checklist**

- **Expose enums on `WebApiSecurityExpressionRoot`:** add fields or getters for `EntityType` and `AccessType` values you want referenced by SpEL.
- **Add `{Entity}AccessRepository`:** implement repository queries for the entity type (e.g., `{Entity}AccessRepository.hasAccess(userId, entityId, accessType)` and `getCreatedById(entityId)`).
- **Ensure `EntityAccessService` supports the entity:** add the new entity to the `switch` statements in `hasEntityAccess()` and `getOwnerId()` so the `EntityType` maps to the correct repository query.
- **Annotate service-level methods:** prefer annotating service methods (not controllers) so access control applies regardless of entrypoint.
- **Unit test expressions:** add tests that exercise the expression root methods and sample annotated methods using mock principals and repositories.

**Next steps / examples to add later**

- Add a worked example for `ConceptSet` showing the access entity, repository and a sample `@PreAuthorize` on its service methods.
- Provide a quick-start snippet to add a new entity type including code diffs for `EntityType`, `WebApiSecurityExpressionRoot`, and the service class.

---

## Granting and Revoking Entity Access

Beyond *checking* access, the system also exposes functionality to **grant** and **revoke** entity-level permissions for roles. This is how administrators (or entity owners) share access to specific entities with other roles in the application.

### How it works

Each entity type that supports access control has a corresponding `sec_{entity}` table with a composite key of `(role_id, entity_id, access_type)`. For example, `sec_cohort_definition` stores rows like `(role_id=5, cohort_definition_id=42, access_type=WRITE)`, meaning role 5 has WRITE access to cohort definition 42.

Granting access inserts a row; revoking access deletes it. These operations flow through a clean layered architecture:

```
PermissionController (REST)
    └─► AuthorizationService (facade)
            └─► EntityAccessService (dispatches by EntityType)
                    └─► {Entity}AccessRepository (JPA save/delete)
```

### The REST endpoints

The `PermissionController` exposes these endpoints under `/permission/access`:

| Method | Path | Purpose |
|--------|------|---------|
| `GET` | `/permission` | List all global permissions |
| `GET` | `/permission/access/suggest?roleSearch=` | Search roles by name |
| `GET` | `/permission/access/{entityType}/{entityId}/{accessType}` | Get roles with a specific access type to an entity |
| `GET` | `/permission/access/{entityType}/{entityId}` | Get roles with WRITE access (convenience) |
| `POST` | `/permission/access/{entityType}/{entityId}/role/{roleId}` | Grant access to a role |
| `DELETE` | `/permission/access/{entityType}/{entityId}/role/{roleId}` | Revoke access from a role |

The POST and DELETE endpoints accept an `AccessRequestDTO` body:
```json
{ "accessType": "READ" }
```
or
```json
{ "accessType": "WRITE" }
```

### Layer-by-layer explanation

#### 1. Access repositories (`{Entity}AccessRepository`)

Each access repository extends `JpaRepository` and provides three query methods beyond the user-oriented `findAccessByUserId`:

```java
// Find which roles have a specific access type to an entity
@Query("SELECT ca.roleId FROM CohortDefinitionAccess ca WHERE ca.cohortDefinitionId = :entityId AND ca.accessType = :accessType")
List<Long> findRoleIdsByEntityIdAndAccessType(@Param("entityId") Long entityId, @Param("accessType") AccessType accessType);

// Find which roles have any access to an entity
@Query("SELECT DISTINCT ca.roleId FROM CohortDefinitionAccess ca WHERE ca.cohortDefinitionId = :entityId")
List<Long> findRoleIdsByEntityId(@Param("entityId") Long entityId);

// Derived delete for revoking a specific grant
void deleteByRoleIdAndCohortDefinitionIdAndAccessType(Long roleId, Long cohortDefinitionId, AccessType accessType);
```

The `save()` method (inherited from `JpaRepository`) handles inserts for granting access.

#### 2. `EntityAccessService`

The `EntityAccessService` dispatches grant/revoke/query operations to the correct repository based on `EntityType`, using `switch` expressions:

```java
// Query: which roles have access?
public List<Long> getRoleIdsForEntity(EntityType entityType, Long entityId, AccessType accessType) {
    return switch (entityType) {
        case COHORT_DEFINITION -> cohortDefAccessRepo.findRoleIdsByEntityIdAndAccessType(entityId, accessType);
        case CONCEPT_SET -> conceptSetAccessRepo.findRoleIdsByEntityIdAndAccessType(entityId, accessType);
        // ... other entity types
    };
}

// Grant: insert a sec_ row
public void grantAccess(EntityType entityType, Long entityId, Long roleId, AccessType accessType) {
    switch (entityType) {
        case COHORT_DEFINITION -> {
            var entity = new CohortDefinitionAccessEntity();
            entity.setRoleId(roleId);
            entity.setCohortDefinitionId(entityId);
            entity.setAccessType(accessType);
            cohortDefAccessRepo.save(entity);
        }
        // ... other entity types
    }
}

// Revoke: delete the sec_ row
public void revokeAccess(EntityType entityType, Long entityId, Long roleId, AccessType accessType) {
    switch (entityType) {
        case COHORT_DEFINITION -> cohortDefAccessRepo.deleteByRoleIdAndCohortDefinitionIdAndAccessType(roleId, entityId, accessType);
        // ... other entity types
    }
}
```

#### 3. `AuthorizationService` (facade)

The `AuthorizationService` wraps `EntityAccessService` and adds cross-cutting concerns (cache invalidation):

```java
@Transactional
public void grantEntityAccess(EntityType entityType, Long entityId, Long roleId, AccessType accessType) {
    this.entityAccessService.grantAccess(entityType, entityId, roleId, accessType);
    this.authorizationCacheService.clearCache();  // invalidate cached UserAuthorizations
}

@Transactional
public void revokeEntityAccess(EntityType entityType, Long entityId, Long roleId, AccessType accessType) {
    this.entityAccessService.revokeAccess(entityType, entityId, roleId, accessType);
    this.authorizationCacheService.clearCache();
}
```

Cache invalidation is critical: without it, users would continue operating under stale permissions until the cache expires naturally.

#### 4. `PermissionController`

The controller is a thin REST layer — it deserializes the request and delegates entirely to `AuthorizationService`:

```java
@PostMapping(value = "/access/{entityType}/{entityId}/role/{roleId}", ...)
public void grantEntityAccess(
        @PathVariable("entityType") EntityType entityType,
        @PathVariable("entityId") Long entityId,
        @PathVariable("roleId") Long roleId,
        @RequestBody AccessRequestDTO accessRequestDTO
) {
    this.authorizationService.grantEntityAccess(entityType, entityId, roleId, accessRequestDTO.getAccessType());
}
```

### Adding grant/revoke support for a new entity type

When you add a new entity type to the application, follow these steps to wire up grant/revoke:

#### Step 1: Create the `sec_` table

Add a migration to create the security table. The table always follows this pattern:

```sql
CREATE TABLE ${ohdsiSchema}.sec_my_entity (
    role_id      BIGINT NOT NULL,
    my_entity_id BIGINT NOT NULL,
    access_type  VARCHAR(50) NOT NULL,
    PRIMARY KEY (role_id, my_entity_id, access_type)
);
```

#### Step 2: Create the access entity class

```java
@Entity(name = "MyEntityAccess")
@Table(name = "sec_my_entity")
@IdClass(MyEntityAccessEntity.MyEntityAccessId.class)
public class MyEntityAccessEntity {

    @Id @Column(name = "role_id")
    private Long roleId;

    @Id @Column(name = "my_entity_id")
    private Long myEntityId;

    @Id @Column(name = "access_type") @Enumerated(EnumType.STRING)
    private AccessType accessType;

    // getters, setters, and composite key class (see CohortDefinitionAccessEntity for pattern)
}
```

#### Step 3: Create the access repository

```java
@Repository
public interface MyEntityAccessRepository
    extends JpaRepository<MyEntityAccessEntity, MyEntityAccessEntity.MyEntityAccessId> {

    // For building UserAuthorizations (used by cache)
    @Query("SELECT ma.myEntityId as entityId, ma.accessType as accessType FROM MyEntityAccess ma JOIN UserRole ur ON ur.role.id = ma.roleId WHERE ur.user.id = :userId")
    List<EntityAccessProjection> findAccessByUserId(@Param("userId") Long userId);

    // For querying which roles have access to a specific entity
    @Query("SELECT ma.roleId FROM MyEntityAccess ma WHERE ma.myEntityId = :entityId AND ma.accessType = :accessType")
    List<Long> findRoleIdsByEntityIdAndAccessType(@Param("entityId") Long entityId, @Param("accessType") AccessType accessType);

    @Query("SELECT DISTINCT ma.roleId FROM MyEntityAccess ma WHERE ma.myEntityId = :entityId")
    List<Long> findRoleIdsByEntityId(@Param("entityId") Long entityId);

    // For revoking access
    void deleteByRoleIdAndMyEntityIdAndAccessType(Long roleId, Long myEntityId, AccessType accessType);
}
```

#### Step 4: Add the enum value to `EntityType`

```java
public enum EntityType {
    COHORT_DEFINITION(CohortDefinitionEntity.class),
    // ... existing types
    MY_ENTITY(MyEntity.class);   // <-- add this
}
```

#### Step 5: Wire into `EntityAccessService`

Add your new repository to the constructor, then add `case MY_ENTITY ->` branches to these four methods:

- `getRoleIdsForEntity(EntityType, Long, AccessType)`
- `getRoleIdsForEntity(EntityType, Long)` (overload)
- `grantAccess(EntityType, Long, Long, AccessType)`
- `revokeAccess(EntityType, Long, Long, AccessType)`

Also add a `buildMyEntityAccess(Long userId)` method and wire it into `buildUserAuthorizations()` so the cache includes your entity's access map.

#### Step 6: Update `AuthorizationService.hasEntityAccess()`

Add a `case MY_ENTITY ->` branch to the `hasEntityAccess()` switch expression so `@PreAuthorize` SpEL expressions can check access to your entity type.

#### Step 7: Update `UserAuthorizations`

Add a field to hold your entity's access map:

```java
public Map<Long, EntityGrant> myEntityAccess = new HashMap<>();
```

That's it — the REST endpoints (`/permission/access/{entityType}/...`) will automatically handle your new entity type because the controller dispatches by `EntityType` enum through the shared `AuthorizationService` → `EntityAccessService` pipeline.

### Design notes

- **Cache invalidation**: Every grant/revoke operation clears the authorization cache. This is intentional — entity access changes must take effect immediately. The cache rebuild is lazy (next access check triggers it).
- **No templates**: The legacy system used "permission templates" to generate wildcard permission strings. The new system uses direct `sec_{entity}` table rows — one row per (role, entity, access_type) tuple. This is simpler, auditable, and doesn't require string formatting.
- **Ownership vs. grants**: Ownership (the entity's `created_by_id`) is separate from explicit grants. Owners always have implicit full access. Grants are for sharing access with other roles. Both are merged in `EntityAccessService.build*Access()` when constructing the `UserAuthorizations` cache.
- **WRITE implies READ**: The `EntityGrant.hasAccess()` method enforces that a WRITE grant satisfies READ checks. You don't need to grant both.

---
See the example `@PreAuthorize` expression used in the project for CohortDefinition and inspect the service/controller implementations to mirror placement and semantics.
