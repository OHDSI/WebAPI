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

- This enables expressions like `hasEntityAccess(#id, COHORT_DEFINITION, anyOf(READ, WRITE))` to resolve within `@PreAuthorize`.

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
@PreAuthorize("isOwner(#id, COHORT_DEFINITION) or isPermitted(anyOf('read:cohort','write:cohort') or hasEntityAccess(#id, COHORT_DEFINITION, anyOf(READ, WRITE))")
public CohortDTO getCohortDefinition(final int id) { ... }
```

- Explanation of the expression:
  - `isOwner(#id, COHORT_DEFINITION)` — short-circuits grant if caller created/owns the entity.
  - `isPermitted(anyOf('read:cohort','write:cohort'))` — reading a definition is allowed granted global read/write.
  - `hasEntityAccess(#id, COHORT_DEFINITION, anyOf(READ, WRITE))` — delegate to `EntityAccessService` to check explicit grants for this entity id.

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
See the example `@PreAuthorize` expression used in the project for CohortDefinition and inspect the service/controller implementations to mirror placement and semantics.

File: [docs/EntityAccess_HOWTO.md](docs/EntityAccess_HOWTO.md)
