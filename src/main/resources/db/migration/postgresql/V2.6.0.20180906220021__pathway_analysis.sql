CREATE SEQUENCE ${ohdsiSchema}.pathway_analysis_sequence;
CREATE TABLE ${ohdsiSchema}.pathway_analysis
(
  id                 INTEGER DEFAULT NEXTVAL('pathway_analysis_sequence'),
  name               VARCHAR NOT NULL,
  combination_window INTEGER,
  min_cell_count     INTEGER,
  max_depth          INTEGER,
  allow_repeats      BOOLEAN DEFAULT FALSE,
  created_by_id      INTEGER,
  created_date       TIMESTAMP,
  modified_by_id     INTEGER,
  modified_date      TIMESTAMP,
  hash_code          INTEGER,
  CONSTRAINT PK_pathway_analysis PRIMARY KEY (id)
);

CREATE SEQUENCE ${ohdsiSchema}.pathway_cohort_sequence;

CREATE TABLE ${ohdsiSchema}.pathway_target_cohort
(
  id                   INTEGER DEFAULT NEXTVAL('pathway_cohort_sequence'),
  name                 VARCHAR(255) NOT NULL,
  cohort_definition_id INTEGER NOT NULL,
  pathway_analysis_id  INTEGER NOT NULL,
  CONSTRAINT PK_pathway_target_cohort PRIMARY KEY (id),
  CONSTRAINT FK_ptc_cd_id
    FOREIGN KEY (cohort_definition_id)
    REFERENCES ${ohdsiSchema}.cohort_definition (id),
  CONSTRAINT FK_ptc_pa_id
    FOREIGN KEY (pathway_analysis_id)
    REFERENCES ${ohdsiSchema}.pathway_analysis (id)
);

CREATE TABLE ${ohdsiSchema}.pathway_event_cohort
(
  id                   INTEGER DEFAULT NEXTVAL('pathway_cohort_sequence'),
  name                 VARCHAR NOT NULL,
  cohort_definition_id INTEGER NOT NULL,
  pathway_analysis_id  INTEGER NOT NULL,
  CONSTRAINT PK_pathway_event_cohort PRIMARY KEY (id),
  CONSTRAINT FK_pec_cd_id
    FOREIGN KEY (cohort_definition_id)
    REFERENCES ${ohdsiSchema}.cohort_definition (id),
  CONSTRAINT FK_pec_pa_id
    FOREIGN KEY (pathway_analysis_id)
    REFERENCES ${ohdsiSchema}.pathway_analysis (id)
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
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'pathway-analysis:generation:*:design:get', 'Get Pathways Analysis generation design'),
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
  'pathway-analysis:generation:*:design:get',
  'pathway-analysis:*:export:get'
)
AND sr.name IN ('Atlas users');

CREATE OR REPLACE VIEW ${ohdsiSchema}.pathway_analysis_generation as
  (SELECT
  job.job_execution_id                     id,
  job.create_time                          start_time,
  job.end_time                             end_time,
  job.status                               status,
  job.exit_message                         exit_message,
  CAST(pa_id_param.string_val AS INTEGER)  pathway_analysis_id,
  CAST(source_param.string_val AS INTEGER) source_id,
  -- Generation info based
  gen_info.design                          design,
  gen_info.hash_code                       hash_code,
  gen_info.created_by_id                   created_by_id
FROM ${ohdsiSchema}.batch_job_execution job
  JOIN ${ohdsiSchema}.batch_job_execution_params pa_id_param
    ON job.job_execution_id = pa_id_param.job_execution_id AND pa_id_param.key_name = 'pathway_analysis_id'
  JOIN ${ohdsiSchema}.batch_job_execution_params source_param
    ON job.job_execution_id = source_param.job_execution_id AND source_param.key_name = 'source_id'
  LEFT JOIN ${ohdsiSchema}.analysis_generation_info gen_info
    ON job.job_execution_id = gen_info.job_execution_id
ORDER BY start_time DESC);