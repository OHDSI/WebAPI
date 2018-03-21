CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.common_entity (
  id            BIGSERIAL,
  guid          VARCHAR NOT NULL,
  target_entity VARCHAR NOT NULL,
  local_id      INTEGER NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (guid)
);

INSERT INTO ${ohdsiSchema}.common_entity (guid, target_entity, local_id)
  SELECT
    uuid_generate_v4(),
    'org.ohdsi.webapi.cohortdefinition.CohortDefinition',
    id
  FROM ${ohdsiSchema}.cohort_definition
  WHERE NOT EXISTS(SELECT *
                   FROM ${ohdsiSchema}.common_entity
                   WHERE local_id = id
                         AND target_entity = 'org.ohdsi.webapi.cohortdefinition.CohortDefinition');

INSERT INTO ${ohdsiSchema}.common_entity (guid, target_entity, local_id)
  SELECT
    uuid_generate_v4(),
    'org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysis',
    cca_id
  FROM ${ohdsiSchema}.cca
  WHERE NOT EXISTS(SELECT *
                   FROM ${ohdsiSchema}.common_entity
                   WHERE local_id = id
                         AND target_entity = 'org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysis');