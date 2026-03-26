package org.ohdsi.webapi.security.spring;

import org.ohdsi.webapi.security.authz.access.AccessType;
import org.ohdsi.webapi.security.authz.access.EntityType;
import org.ohdsi.webapi.security.authz.AuthorizationService;

import java.util.Arrays;
import java.util.Collection;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

public class WebApiSecurityExpressionRoot
    extends SecurityExpressionRoot
    implements MethodSecurityExpressionOperations {

  private final AuthorizationService authorizationService;

  private Object filterObject;
  private Object returnObject;

  /*
   * ==========================================================
   * Constants for use in @PreAuthorize expressions
   * ==========================================================
   */

  // Provide SpEL-friendly accessor methods (callable as READ, WRITE because of
  // 'get' prefix on method)
  // AccessType
  public final AccessType READ = AccessType.READ;
  public final AccessType WRITE = AccessType.WRITE;

  // EntityType
  public final EntityType COHORT_DEFINITION = EntityType.COHORT_DEFINITION;
  public final EntityType CONCEPT_SET = EntityType.CONCEPT_SET;
  public final EntityType SOURCE = EntityType.SOURCE;

  public WebApiSecurityExpressionRoot(
      Authentication authentication,
      AuthorizationService authorizationService) {
    super(authentication);
    this.authorizationService = authorizationService;
  }

  /*
   * ==========================================================
   * Custom security expressions
   * ==========================================================
   */

  /**
   * Check if the current user is the owner (creator) of an entity
   * 
   * @param entityId   The entity ID
   * @param entityType The type of entity (e.g., COHORT_DEFINITION)
   * @return true if the current user created the entity
   */
  public boolean isOwner(Long entityId, EntityType entityType) {
    return authorizationService.isOwner(entityId, entityType);
  }

  /**
   * Check if the current user has specific access to an entity via {entity}_sec
   * table
   * 
   * @param entityId   The entity ID
   * @param entityType The type of entity (e.g., COHORT_DEFINITION)
   * @param accessType The type of access (READ, WRITE)
   * @return true if the user has the specified access
   */
  public boolean hasEntityAccess(Long entityId, EntityType entityType, AccessType accessType) {
    return authorizationService.hasEntityAccess(entityId, entityType, accessType);
  }

  /**
   * Overloaded form: check if the current user has any of the provided access
   * types for the entity.
   * Accepts a Collection for easier SpEL usage when grouping values via
   * `anyOf(...)`.
   */
  public boolean hasEntityAccess(Long entityId, EntityType entityType, Collection<AccessType> accessTypes) {
    if (accessTypes == null || accessTypes.isEmpty())
      return false;
    for (AccessType at : accessTypes) {
      if (this.hasEntityAccess(entityId, entityType, at)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if the current user has specific access to a source
   * 
   * @param sourceKey  The source key
   * @param accessType The type of access (READ, WRITE)
   * @return true if the user has the specified access
   */
  public boolean hasSourceAccess(String sourceKey, AccessType accessType) {
    return authorizationService.hasSourceAccess(sourceKey, accessType);
  }

  /**
   * Check if the current user has specific access to a source
   * 
   * @param sourceKey  The source key
   * @param accessTypes A collection of AccessType.
   * @return true if the user has the specified access
   */
  public boolean hasSourceAccess(String sourceKey, Collection<AccessType> accessTypes) {
    if (accessTypes == null || accessTypes.isEmpty())
      return false;
    for (AccessType at : accessTypes) {
      if (this.hasSourceAccess(sourceKey, at)) {
        return true;
      }
    }
    return false;
  }  

  /**
   * Helper for SpEL to create a collection of AccessType values: anyOf(READ,
   * WRITE)
   */
  public Collection<AccessType> anyOf(AccessType... accessTypes) {
    if (accessTypes == null)
      return java.util.Collections.emptyList();
    return Arrays.asList(accessTypes);
  }

  /**
   * Check if the current user has a wildcard permission (global entitlement)
   * 
   * @param permission The permission string (e.g., "read:cohort", "write", "*")
   * @return true if the user has the permission
   */
  public boolean isPermitted(String permission) {
    return authorizationService.isPermitted(permission);
  }

  /**
   * Check if the current user has a wildcard permission (global entitlement)
   * 
   * @param permission The permission string (e.g., "read:cohort", "write", "*")
   * @return true if the user has the permission
   */
  public boolean isPermitted(Collection<String> permissions) {
    if (permissions == null || permissions.isEmpty())
      return false;
    for (String p : permissions) {
      if (this.isPermitted(p)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Helper for SpEL to create a collection of String values: anyOf('perm1', 'perm2')
   */
  public Collection<String> anyOf(String... permissions) {
    if (permissions == null)
      return java.util.Collections.emptyList();
    return Arrays.asList(permissions);
  }

  /*
   * ==========================================================
   * Required by MethodSecurityExpressionOperations
   * ==========================================================
   */

  @Override
  public void setFilterObject(Object filterObject) {
    this.filterObject = filterObject;
  }

  @Override
  public Object getFilterObject() {
    return filterObject;
  }

  @Override
  public void setReturnObject(Object returnObject) {
    this.returnObject = returnObject;
  }

  @Override
  public Object getReturnObject() {
    return returnObject;
  }

  @Override
  public Object getThis() {
    return this;
  }
}
