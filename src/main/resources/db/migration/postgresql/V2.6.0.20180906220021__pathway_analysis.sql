CREATE SEQUENCE ${ohdsiSchema}.pathway_analysis_sequence;
CREATE TABLE ${ohdsiSchema}.pathway_analysis
(
  id                 INTEGER PRIMARY KEY DEFAULT NEXTVAL('pathway_analysis_sequence'),
  name               VARCHAR NOT NULL,
  combination_window INTEGER,
  min_cell_count     INTEGER,
  max_depth          INTEGER,
  created_by_id      INTEGER,
  created_date       TIMESTAMP,
  modified_by_id     INTEGER,
  modified_date      TIMESTAMP,
  hash_code          INTEGER
);

CREATE SEQUENCE ${ohdsiSchema}.pathway_cohort_sequence;

CREATE TABLE ${ohdsiSchema}.pathway_target_cohort
(
  id                   INTEGER PRIMARY KEY DEFAULT NEXTVAL('pathway_cohort_sequence'),
  name                 VARCHAR NOT NULL,
  cohort_definition_id INTEGER NOT NULL REFERENCES ${ohdsiSchema}.cohort_definition (id),
  pathway_analysis_id  INTEGER NOT NULL REFERENCES ${ohdsiSchema}.pathway_analysis (id)
);

CREATE TABLE ${ohdsiSchema}.pathway_event_cohort
(
  id                   INTEGER PRIMARY KEY DEFAULT NEXTVAL('pathway_cohort_sequence'),
  name                 VARCHAR NOT NULL,
  cohort_definition_id INTEGER NOT NULL REFERENCES ${ohdsiSchema}.cohort_definition (id),
  pathway_analysis_id  INTEGER NOT NULL REFERENCES ${ohdsiSchema}.pathway_analysis (id),
  is_deleted           BOOLEAN DEFAULT FALSE
);

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:post', 'Create Pathways Analysis'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:import:post', 'Import Pathways Analysis'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:get', 'Get Pathways Analyses list'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:*:get', 'Get Pathways Analysis instance'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:*:generation:get', 'Get Pathways Analysis generations list'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:generation:*:get', 'Get Pathways Analysis generation instance'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:generation:*:result:get', 'Get Pathways Analysis generation results'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:*:export:get', 'Export Pathways Analysis');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" IN (
  'pathway-analysis:post',
  'pathway-analysis:import:post',
  'pathway-analysis:get',
  'pathway-analysis:*:get',
  'pathway-analysis:*:generation:get',
  'pathway-analysis:generation:*:get',
  'pathway-analysis:generation:*:result:get',
  'pathway-analysis:*:export:get'
)
AND sr.name IN ('Atlas users');

CREATE OR REPLACE VIEW ${ohdsiSchema}.pathway_analysis_generation as
  (SELECT
  job.job_execution_id                     id,
  job.create_time                          start_time,
  job.end_time                             end_time,
  job.status                               status,
  design_param.string_val                  design,
  hash_code_param.string_val               hash_code,
  CAST(pa_id_param.string_val AS INTEGER)  pathway_analysis_id,
  CAST(source_param.string_val AS INTEGER) source_id
FROM ${ohdsiSchema}.batch_job_execution job
  JOIN ${ohdsiSchema}.batch_job_execution_params design_param
    ON job.job_execution_id = design_param.job_execution_id AND design_param.key_name = 'design'
  JOIN ${ohdsiSchema}.batch_job_execution_params hash_code_param
    ON job.job_execution_id = hash_code_param.job_execution_id AND hash_code_param.key_name = 'hash_code'
  JOIN ${ohdsiSchema}.batch_job_execution_params pa_id_param
    ON job.job_execution_id = pa_id_param.job_execution_id AND pa_id_param.key_name = 'pathway_analysis_id'
  JOIN ${ohdsiSchema}.batch_job_execution_params source_param
    ON job.job_execution_id = source_param.job_execution_id AND source_param.key_name = 'source_id'
ORDER BY start_time DESC);