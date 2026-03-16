package org.ohdsi.webapi.cohortdefinition;

/**
 * Projection that returns the entity along with per-user access hints.
 */
public interface CohortDefinitionWithAccess {
  CohortDefinitionEntity getCohortDefinition();
  Boolean getCanRead();
  Boolean getCanWrite();
}
