package org.ohdsi.webapi.security.authz.access;

import java.util.Map;
import java.util.Set;

public class UserAuthorizations {
  public Set<String> permissions = Set.of();

  /** Authored entities: access grants + ownership status */
  public Map<Long, EntityGrant> cohortDefinitionAccess = Map.of();
  public Map<Long, EntityGrant> conceptSetAccess = Map.of();
  public Map<Long, EntityGrant> cohortCharacterizationAccess = Map.of();
  public Map<Long, EntityGrant> feAnalysisAccess = Map.of();
  public Map<Long, EntityGrant> pathwayAccess = Map.of();
  public Map<Long, EntityGrant> incidenceRateAccess = Map.of();

  /** Infrastructure entities: access grants only (no ownership semantics) */
  public Map<Long, Set<AccessType>> sourceAccess = Map.of();
}
