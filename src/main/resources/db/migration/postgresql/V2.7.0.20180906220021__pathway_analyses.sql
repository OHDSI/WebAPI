CREATE SEQUENCE ${ohdsiSchema}.pathway_analyses_sequence;
CREATE TABLE ${ohdsiSchema}.pathway_analyses
(
  id                 INTEGER PRIMARY KEY DEFAULT NEXTVAL('pathway_analyses_sequence'),
  name               VARCHAR NOT NULL,
  combination_window INTEGER,
  min_cell_count     INTEGER,
  max_depth          INTEGER,
  created_by         INTEGER,
  created_at         TIMESTAMP,
  updated_by         INTEGER,
  updated_at         TIMESTAMP
);

CREATE SEQUENCE ${ohdsiSchema}.pathway_cohorts_sequence;

CREATE TABLE ${ohdsiSchema}.pathway_target_cohorts
(
  id                   INTEGER PRIMARY KEY DEFAULT NEXTVAL('pathway_cohorts_sequence'),
  name                 VARCHAR NOT NULL,
  cohort_definition_id INTEGER NOT NULL REFERENCES ${ohdsiSchema}.cohort_definition (id),
  pathway_analysis_id  INTEGER NOT NULL REFERENCES ${ohdsiSchema}.pathway_analyses (id)
);

CREATE TABLE ${ohdsiSchema}.pathway_event_cohorts
(
  id                   INTEGER PRIMARY KEY DEFAULT NEXTVAL('pathway_cohorts_sequence'),
  name                 VARCHAR NOT NULL,
  cohort_definition_id INTEGER NOT NULL REFERENCES ${ohdsiSchema}.cohort_definition (id),
  pathway_analysis_id  INTEGER NOT NULL REFERENCES ${ohdsiSchema}.pathway_analyses (id),
  is_deleted           BOOLEAN DEFAULT FALSE
);

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analyses:post', 'Create Pathways Analysis'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analyses:get', 'Get Pathways Analyses list');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" IN (
  'pathway-analyses:post',
  'pathway-analyses:get'
)
AND sr.name IN ('Atlas users');