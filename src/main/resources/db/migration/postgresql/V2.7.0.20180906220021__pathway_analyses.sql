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
  updated_at         TIMESTAMP,
  hash_code          INTEGER
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

CREATE OR REPLACE VIEW ${ohdsiSchema}.pathway_analysis_generations as
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