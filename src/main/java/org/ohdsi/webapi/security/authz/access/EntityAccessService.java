package org.ohdsi.webapi.security.authz.access;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.micrometer.core.annotation.Timed;
import org.ohdsi.webapi.security.authz.PermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Service that encapsulates access to entity-specific security repositories.
 * This keeps AuthorizationService clean by delegating entity-specific queries.
 * 
 * Also responsible for assembling the complete {@link UserAuthorizations} object
 * used by {@link org.ohdsi.webapi.security.authz.AuthorizationCacheService}.
 */
@Service
public class EntityAccessService {

  private static final Logger log = LoggerFactory.getLogger(EntityAccessService.class);

  private final CohortDefinitionAccessRepository cohortDefAccessRepo;
  private final ConceptSetAccessRepository conceptSetAccessRepo;
  private final CohortCharacterizationAccessRepository cohortCharAccessRepo;
  private final FeAnalysisAccessRepository feAnalysisAccessRepo;
  private final SourceAccessRepository sourceAccessRepo;
  private final IncidenceRateAccessRepository incidenceRateAccessRepo;
  private final PathwayAccessRepository pathwayAccessRepo;
  private final PermissionRepository permissionRepository;

  public EntityAccessService(CohortDefinitionAccessRepository cohortDefAccessRepo,
      ConceptSetAccessRepository conceptSetAccessRepo,
      CohortCharacterizationAccessRepository cohortCharAccessRepo,
      FeAnalysisAccessRepository feAnalysisAccessRepo,
      SourceAccessRepository sourceAccessRepo,
      IncidenceRateAccessRepository incidenceRateAccessRepo,
      PathwayAccessRepository pathwayAccessRepo,
      PermissionRepository permissionRepository) {
    this.cohortDefAccessRepo = cohortDefAccessRepo;
    this.conceptSetAccessRepo = conceptSetAccessRepo;
    this.cohortCharAccessRepo = cohortCharAccessRepo;
    this.feAnalysisAccessRepo = feAnalysisAccessRepo;
    this.sourceAccessRepo = sourceAccessRepo;
    this.incidenceRateAccessRepo = incidenceRateAccessRepo;
    this.pathwayAccessRepo = pathwayAccessRepo;
    this.permissionRepository = permissionRepository;
  }

  // -------------------------
  // Full UserAuthorizations builder (called by AuthorizationCacheService)
  // -------------------------

  /**
   * Builds the complete {@link UserAuthorizations} for a user.
   * Loads global wildcard permissions and per-entity access maps.
   * This is the expensive operation that gets cached.
   *
   * @param userId The user ID
   * @return The fully populated UserAuthorizations
   */
  @Timed(value = "security.user_authorizations.build",
      description = "Time to build UserAuthorizations (cache miss)")
  public UserAuthorizations buildUserAuthorizations(Long userId) {
    long startNanos = System.nanoTime();

    UserAuthorizations authz = new UserAuthorizations();

    // Global wildcard permissions from sec_permission via user roles
    authz.permissions = permissionRepository.queryUserPermissions(userId);

    // Per-entity access maps built from sec_* tables + ownership
    authz.cohortDefinitionAccess = buildCohortDefinitionAccess(userId);
    authz.conceptSetAccess = buildConceptSetAccess(userId);
    authz.cohortCharacterizationAccess = buildCohortCharacterizationAccess(userId);
    authz.feAnalysisAccess = buildFeAnalysisAccess(userId);
    authz.pathwayAccess = buildPathwayAccess(userId);
    authz.incidenceRateAccess = buildIncidenceRateAccess(userId);
    authz.sourceAccess = buildSourceAccess(userId);

    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    log.debug("Built UserAuthorizations for userId={} in {}ms (permissions={}, cohortDefs={}, conceptSets={}, cohortChars={}, feAnalysis={}, pathwayAnalysis={}, incidenceAnalysis={}, sources={})",
      userId, elapsedMs,
      authz.permissions.size(),
      authz.cohortDefinitionAccess.size(),
      authz.conceptSetAccess.size(),
      authz.cohortCharacterizationAccess.size(),
      authz.feAnalysisAccess.size(),
      authz.pathwayAccess.size(),
      authz.incidenceRateAccess.size(),
      authz.sourceAccess.size());

    return authz;
  }

  // -------------------------
  // Per-entity access map builders
  // -------------------------

  /**
   * Build the cohort definition access map for a user.
   * Queries the sec_cohort_definition table via roles assigned to the user,
   * then merges owned cohort definitions as implicit WRITE access
   * (ownership implies WRITE, and WRITE implies READ).
   *
   * @param userId The user ID
   * @return Map of cohortDefinitionId → Set of granted AccessTypes
   */
  public Map<Long, EntityGrant> buildCohortDefinitionAccess(Long userId) {
    // Collect role-based grants
    Map<Long, Set<AccessType>> roleGrants = new HashMap<>();
    for (EntityAccessProjection p : cohortDefAccessRepo.findAccessByUserId(userId)) {
      roleGrants.computeIfAbsent(p.getEntityId(), k -> EnumSet.noneOf(AccessType.class))
                .add(p.getAccessType());
    }

    // Collect owned entity IDs
    Set<Long> ownedIds = new java.util.HashSet<>();
    for (Integer ownedId : cohortDefAccessRepo.findOwnedCohortDefinitionIds(userId)) {
      ownedIds.add(ownedId.longValue());
    }

    // Merge into EntityGrant map
    Map<Long, EntityGrant> access = new HashMap<>();

    // Start with role-granted entities
    for (Map.Entry<Long, Set<AccessType>> entry : roleGrants.entrySet()) {
      Long entityId = entry.getKey();
      access.put(entityId, new EntityGrant(entry.getValue(), ownedIds.contains(entityId)));
    }

    // Add owned entities that had no role-based grants
    for (Long ownedId : ownedIds) {
      access.computeIfAbsent(ownedId, k -> new EntityGrant(EnumSet.noneOf(AccessType.class), true));
    }

    return access;
  }

  /**
   * Build the concept set access map for a user.
   * Queries the sec_concept_set table via roles assigned to the user,
   * then merges owned concept sets as implicit WRITE access.
   *
   * @param userId The user ID
   * @return Map of conceptSetId → EntityGrant
   */
  public Map<Long, EntityGrant> buildConceptSetAccess(Long userId) {
    // Collect role-based grants
    Map<Long, Set<AccessType>> roleGrants = new HashMap<>();
    for (EntityAccessProjection p : conceptSetAccessRepo.findAccessByUserId(userId)) {
      roleGrants.computeIfAbsent(p.getEntityId(), k -> EnumSet.noneOf(AccessType.class))
                .add(p.getAccessType());
    }

    // Collect owned entity IDs
    Set<Long> ownedIds = new java.util.HashSet<>();
    for (Integer ownedId : conceptSetAccessRepo.findOwnedConceptSetIds(userId)) {
      ownedIds.add(ownedId.longValue());
    }

    // Merge into EntityGrant map
    Map<Long, EntityGrant> access = new HashMap<>();

    // Start with role-granted entities
    for (Map.Entry<Long, Set<AccessType>> entry : roleGrants.entrySet()) {
      Long entityId = entry.getKey();
      access.put(entityId, new EntityGrant(entry.getValue(), ownedIds.contains(entityId)));
    }

    // Add owned entities that had no role-based grants
    for (Long ownedId : ownedIds) {
      access.computeIfAbsent(ownedId, k -> new EntityGrant(EnumSet.noneOf(AccessType.class), true));
    }

    return access;
  }

  /**
   * Build the cohort characterization access map for a user.
   * Queries the sec_cohort_characterization table via roles assigned to the user,
   * then merges owned cohort characterizations as implicit WRITE access.
   *
   * @param userId The user ID
   * @return Map of cohortCharacterizationId → EntityGrant
   */
  public Map<Long, EntityGrant> buildCohortCharacterizationAccess(Long userId) {
    // Collect role-based grants
    Map<Long, Set<AccessType>> roleGrants = new HashMap<>();
    for (EntityAccessProjection p : cohortCharAccessRepo.findAccessByUserId(userId)) {
      roleGrants.computeIfAbsent(p.getEntityId(), k -> EnumSet.noneOf(AccessType.class))
                .add(p.getAccessType());
    }

    // Collect owned entity IDs
    Set<Long> ownedIds = new java.util.HashSet<>();
    for (Long ownedId : cohortCharAccessRepo.findOwnedCohortCharacterizationIds(userId)) {
      ownedIds.add(ownedId);
    }

    // Merge into EntityGrant map
    Map<Long, EntityGrant> access = new HashMap<>();

    // Start with role-granted entities
    for (Map.Entry<Long, Set<AccessType>> entry : roleGrants.entrySet()) {
      Long entityId = entry.getKey();
      access.put(entityId, new EntityGrant(entry.getValue(), ownedIds.contains(entityId)));
    }

    // Add owned entities that had no role-based grants
    for (Long ownedId : ownedIds) {
      access.computeIfAbsent(ownedId, k -> new EntityGrant(EnumSet.noneOf(AccessType.class), true));
    }

    return access;
  }

  /**
   * Build the feature analysis access map for a user.
   * Queries the sec_fe_analysis table via roles assigned to the user,
   * then merges owned feature analyses as implicit WRITE access.
   *
   * @param userId The user ID
   * @return Map of feAnalysisId → EntityGrant
   */
  public Map<Long, EntityGrant> buildFeAnalysisAccess(Long userId) {
    // Collect role-based grants
    Map<Long, Set<AccessType>> roleGrants = new HashMap<>();
    for (EntityAccessProjection p : feAnalysisAccessRepo.findAccessByUserId(userId)) {
      roleGrants.computeIfAbsent(p.getEntityId(), k -> EnumSet.noneOf(AccessType.class))
                .add(p.getAccessType());
    }

    // Collect owned entity IDs
    Set<Long> ownedIds = new java.util.HashSet<>();
    for (Integer ownedId : feAnalysisAccessRepo.findOwnedFeAnalysisIds(userId)) {
      ownedIds.add(ownedId.longValue());
    }

    // Merge into EntityGrant map
    Map<Long, EntityGrant> access = new HashMap<>();

    // Start with role-granted entities
    for (Map.Entry<Long, Set<AccessType>> entry : roleGrants.entrySet()) {
      Long entityId = entry.getKey();
      access.put(entityId, new EntityGrant(entry.getValue(), ownedIds.contains(entityId)));
    }

    // Add owned entities that had no role-based grants
    for (Long ownedId : ownedIds) {
      access.computeIfAbsent(ownedId, k -> new EntityGrant(EnumSet.noneOf(AccessType.class), true));
    }

    return access;
  }

  /**
   * Build the incidence rate access map for a user.
   * Queries the sec_ir_analysis table via roles assigned to the user,
   * then merges owned incidence rate analyses as implicit WRITE access.
   *
   * @param userId The user ID
   * @return Map of irId → EntityGrant
   */
  public Map<Long, EntityGrant> buildIncidenceRateAccess(Long userId) {
    Map<Long, Set<AccessType>> roleGrants = new HashMap<>();
    for (EntityAccessProjection p : incidenceRateAccessRepo.findAccessByUserId(userId)) {
      roleGrants.computeIfAbsent(p.getEntityId(), k -> EnumSet.noneOf(AccessType.class))
                .add(p.getAccessType());
    }

    // Collect owned entity IDs
    Set<Long> ownedIds = new java.util.HashSet<>();
    for (Integer ownedId : incidenceRateAccessRepo.findOwnedIncidenceRateIds(userId)) {
      ownedIds.add(ownedId.longValue());
    }

    // Merge into EntityGrant map
    Map<Long, EntityGrant> access = new HashMap<>();

    // Start with role-granted entities
    for (Map.Entry<Long, Set<AccessType>> entry : roleGrants.entrySet()) {
      Long entityId = entry.getKey();
      access.put(entityId, new EntityGrant(entry.getValue(), ownedIds.contains(entityId)));
    }

    // Add owned entities that had no role-based grants
    for (Long ownedId : ownedIds) {
      access.computeIfAbsent(ownedId, k -> new EntityGrant(EnumSet.noneOf(AccessType.class), true));
    }

    return access;
  }

  /**
   * Build the pathway analysis access map for a user.
   * Queries the sec_pathway_analysis table via roles assigned to the user,
   * then merges owned pathway analyses as implicit WRITE access.
   *
   * @param userId The user ID
   * @return Map of pathwayAnalysisId → EntityGrant
   */
  public Map<Long, EntityGrant> buildPathwayAccess(Long userId) {
    Map<Long, Set<AccessType>> roleGrants = new HashMap<>();
    for (EntityAccessProjection p : pathwayAccessRepo.findAccessByUserId(userId)) {
      roleGrants.computeIfAbsent(p.getEntityId(), k -> EnumSet.noneOf(AccessType.class))
                .add(p.getAccessType());
    }

    // Collect owned entity IDs
    Set<Long> ownedIds = new java.util.HashSet<>();
    for (Integer ownedId : pathwayAccessRepo.findOwnedPathwayAnalysisIds(userId)) {
      ownedIds.add(ownedId.longValue());
    }

    // Merge into EntityGrant map
    Map<Long, EntityGrant> access = new HashMap<>();

    // Start with role-granted entities
    for (Map.Entry<Long, Set<AccessType>> entry : roleGrants.entrySet()) {
      Long entityId = entry.getKey();
      access.put(entityId, new EntityGrant(entry.getValue(), ownedIds.contains(entityId)));
    }

    // Add owned entities that had no role-based grants
    for (Long ownedId : ownedIds) {
      access.computeIfAbsent(ownedId, k -> new EntityGrant(EnumSet.noneOf(AccessType.class), true));
    }

    return access;
  }

  /**
   * Build the source access map for a user.
   * Queries the sec_source table via roles assigned to the user.
   * Note: Source ownership does NOT imply write access; only explicit
   * role-based grants are considered.
   *
   * @param userId The user ID
   * @return Map of sourceId → Set of granted AccessTypes
   */
  public Map<Long, Set<AccessType>> buildSourceAccess(Long userId) {
    Map<Long, Set<AccessType>> access = new HashMap<>();

    for (EntityAccessProjection p : sourceAccessRepo.findAccessByUserId(userId)) {
      access.computeIfAbsent(p.getEntityId(), k -> EnumSet.noneOf(AccessType.class))
            .add(p.getAccessType());
    }
    
    return access;
  }

  // -------------------------
  // Entity Access Management (grant / revoke / query by entity)
  // -------------------------

  /**
   * Find all role IDs that have a specific access type to an entity.
   *
   * @param entityType The type of entity
   * @param entityId   The entity ID
   * @param accessType The access type to filter by
   * @return List of role IDs with the specified access
   */
  public List<Long> getRoleIdsForEntity(EntityType entityType, Long entityId, AccessType accessType) {
    return switch (entityType) {
      case COHORT_DEFINITION -> cohortDefAccessRepo.findRoleIdsByEntityIdAndAccessType(entityId, accessType);
      case CONCEPT_SET -> conceptSetAccessRepo.findRoleIdsByEntityIdAndAccessType(entityId, accessType);
      case COHORT_CHARACTERIZATION -> cohortCharAccessRepo.findRoleIdsByEntityIdAndAccessType(entityId, accessType);
      case FE_ANALYSIS -> feAnalysisAccessRepo.findRoleIdsByEntityIdAndAccessType(entityId, accessType);
      case INCIDENCE_RATE -> incidenceRateAccessRepo.findRoleIdsByEntityIdAndAccessType(entityId, accessType);
      case PATHWAY_ANALYSIS -> pathwayAccessRepo.findRoleIdsByEntityIdAndAccessType(entityId, accessType);
      case SOURCE -> sourceAccessRepo.findRoleIdsByEntityIdAndAccessType(entityId, accessType);
    };
  }

  /**
   * Find all role IDs that have any access to an entity.
   *
   * @param entityType The type of entity
   * @param entityId   The entity ID
   * @return List of distinct role IDs with any access
   */
  public List<Long> getRoleIdsForEntity(EntityType entityType, Long entityId) {
    return switch (entityType) {
      case COHORT_DEFINITION -> cohortDefAccessRepo.findRoleIdsByEntityId(entityId);
      case CONCEPT_SET -> conceptSetAccessRepo.findRoleIdsByEntityId(entityId);
      case COHORT_CHARACTERIZATION -> cohortCharAccessRepo.findRoleIdsByEntityId(entityId);
      case FE_ANALYSIS -> feAnalysisAccessRepo.findRoleIdsByEntityId(entityId);
      case INCIDENCE_RATE -> incidenceRateAccessRepo.findRoleIdsByEntityId(entityId);
      case PATHWAY_ANALYSIS -> pathwayAccessRepo.findRoleIdsByEntityId(entityId);
      case SOURCE -> sourceAccessRepo.findRoleIdsByEntityId(entityId);
    };
  }

  /**
   * Grant access to an entity for a specific role.
   * Inserts a row in the appropriate sec_{entity} table.
   * If the grant already exists, this is a no-op (JPA save on existing composite key).
   *
   * @param entityType The type of entity
   * @param entityId   The entity ID
   * @param roleId     The role ID to grant access to
   * @param accessType The access type to grant (READ or WRITE)
   */
  public void grantAccess(EntityType entityType, Long entityId, Long roleId, AccessType accessType) {
    switch (entityType) {
      case COHORT_DEFINITION -> {
        var entity = new CohortDefinitionAccessEntity();
        entity.setRoleId(roleId);
        entity.setCohortDefinitionId(entityId);
        entity.setAccessType(accessType);
        cohortDefAccessRepo.save(entity);
      }
      case CONCEPT_SET -> {
        var entity = new ConceptSetAccessEntity();
        entity.setRoleId(roleId);
        entity.setConceptSetId(entityId);
        entity.setAccessType(accessType);
        conceptSetAccessRepo.save(entity);
      }
      case COHORT_CHARACTERIZATION -> {
        var entity = new CohortCharacterizationAccessEntity();
        entity.setRoleId(roleId);
        entity.setCohortCharacterizationId(entityId);
        entity.setAccessType(accessType);
        cohortCharAccessRepo.save(entity);
      }
      case FE_ANALYSIS -> {
        var entity = new FeAnalysisAccessEntity();
        entity.setRoleId(roleId);
        entity.setFeAnalysisId(entityId);
        entity.setAccessType(accessType);
        feAnalysisAccessRepo.save(entity);
      }
      case INCIDENCE_RATE -> {
        var entity = new IncidenceRateAccessEntity();
        entity.setRoleId(roleId);
        entity.setIrId(entityId);
        entity.setAccessType(accessType);
        incidenceRateAccessRepo.save(entity);
      }
      case PATHWAY_ANALYSIS -> {
        var entity = new PathwayAccessEntity();
        entity.setRoleId(roleId);
        entity.setPathwayAnalysisId(entityId);
        entity.setAccessType(accessType);
        pathwayAccessRepo.save(entity);
      }
      case SOURCE -> {
        var entity = new SourceAccessEntity();
        entity.setRoleId(roleId);
        entity.setSourceId(entityId);
        entity.setAccessType(accessType);
        sourceAccessRepo.save(entity);
      }
    }
  }

  /**
   * Revoke a specific access type from a role for an entity.
   * Deletes the row in the appropriate sec_{entity} table.
   * If the grant does not exist, this is a no-op.
   *
   * @param entityType The type of entity
   * @param entityId   The entity ID
   * @param roleId     The role ID to revoke access from
   * @param accessType The access type to revoke (READ or WRITE)
   */
  public void revokeAccess(EntityType entityType, Long entityId, Long roleId, AccessType accessType) {
    switch (entityType) {
      case COHORT_DEFINITION -> cohortDefAccessRepo.deleteByRoleIdAndCohortDefinitionIdAndAccessType(roleId, entityId, accessType);
      case CONCEPT_SET -> conceptSetAccessRepo.deleteByRoleIdAndConceptSetIdAndAccessType(roleId, entityId, accessType);
      case COHORT_CHARACTERIZATION -> cohortCharAccessRepo.deleteByRoleIdAndCohortCharacterizationIdAndAccessType(roleId, entityId, accessType);
      case FE_ANALYSIS -> feAnalysisAccessRepo.deleteByRoleIdAndFeAnalysisIdAndAccessType(roleId, entityId, accessType);
      case INCIDENCE_RATE -> incidenceRateAccessRepo.deleteByRoleIdAndIrIdAndAccessType(roleId, entityId, accessType);
      case PATHWAY_ANALYSIS -> pathwayAccessRepo.deleteByRoleIdAndPathwayAnalysisIdAndAccessType(roleId, entityId, accessType);
      case SOURCE -> sourceAccessRepo.deleteByRoleIdAndSourceIdAndAccessType(roleId, entityId, accessType);
    }
  }

}
