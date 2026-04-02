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
  private final PermissionRepository permissionRepository;

  public EntityAccessService(CohortDefinitionAccessRepository cohortDefAccessRepo,
      ConceptSetAccessRepository conceptSetAccessRepo,
      CohortCharacterizationAccessRepository cohortCharAccessRepo,
      FeAnalysisAccessRepository feAnalysisAccessRepo,
      SourceAccessRepository sourceAccessRepo,
      PermissionRepository permissionRepository) {
    this.cohortDefAccessRepo = cohortDefAccessRepo;
    this.conceptSetAccessRepo = conceptSetAccessRepo;
    this.cohortCharAccessRepo = cohortCharAccessRepo;
    this.feAnalysisAccessRepo = feAnalysisAccessRepo;
    this.sourceAccessRepo = sourceAccessRepo;
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
    authz.sourceAccess = buildSourceAccess(userId);

    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
    log.debug("Built UserAuthorizations for userId={} in {}ms (permissions={}, cohortDefs={}, conceptSets={}, cohortChars={}, sources={})",
        userId, elapsedMs,
        authz.permissions.size(),
        authz.cohortDefinitionAccess.size(),
        authz.conceptSetAccess.size(),
        authz.cohortCharacterizationAccess.size(),
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

}
