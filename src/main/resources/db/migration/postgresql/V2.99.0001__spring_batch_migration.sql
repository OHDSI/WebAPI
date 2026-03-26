-- WebAPI 3.0.0 Migration: Spring Batch 5 schema updates

--= migrate execution params to new shape, keeping old data
ALTER TABLE ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS
DROP CONSTRAINT JOB_EXEC_PARAMS_FK;

ALTER TABLE ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS
RENAME TO BATCH_JOB_EXECUTION_PARAMS_V4;

-- Spring Batch 5 schema updates

CREATE TABLE ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS (
	JOB_EXECUTION_ID BIGINT NOT NULL,
	PARAMETER_NAME VARCHAR(100) NOT NULL,
	PARAMETER_TYPE VARCHAR(100) NOT NULL,
	PARAMETER_VALUE VARCHAR(2500),
	IDENTIFYING CHAR(1) NOT NULL,
	CONSTRAINT JOB_EXEC_PARAMS_FK FOREIGN KEY (JOB_EXECUTION_ID)
	REFERENCES ${ohdsiSchema}.BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
);

INSERT INTO BATCH_JOB_EXECUTION_PARAMS (
  JOB_EXECUTION_ID,
  PARAMETER_NAME,
  PARAMETER_TYPE,
  PARAMETER_VALUE,
  IDENTIFYING
)
SELECT
  JOB_EXECUTION_ID,
  KEY_NAME as PARAMETER_NAME,

  CASE
    WHEN STRING_VAL IS NOT NULL THEN 'java.lang.String'
    WHEN LONG_VAL   IS NOT NULL THEN 'java.lang.Long'
    WHEN DOUBLE_VAL IS NOT NULL THEN 'java.lang.Double'
    WHEN DATE_VAL   IS NOT NULL THEN 'java.time.LocalDateTime'
  END as PARAMETER_TYPE,
  CASE
    WHEN STRING_VAL IS NOT NULL THEN STRING_VAL
    WHEN LONG_VAL   IS NOT NULL THEN LONG_VAL::text
    WHEN DOUBLE_VAL IS NOT NULL THEN DOUBLE_VAL::text
    WHEN DATE_VAL   IS NOT NULL THEN
      to_char(DATE_VAL, 'YYYY-MM-DD"T"HH24:MI:SS')
  END as PARAMETER_VALUE,
  IDENTIFYING
FROM BATCH_JOB_EXECUTION_PARAMS_V4;

CREATE INDEX BJEP_JOB_EXEC_PARAMS_IDX ON ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS (JOB_EXECUTION_ID);

ALTER TABLE ${ohdsiSchema}.BATCH_STEP_EXECUTION
ADD COLUMN IF NOT EXISTS CREATE_TIME TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE ${ohdsiSchema}.BATCH_STEP_EXECUTION
ALTER COLUMN START_TIME DROP NOT NULL;

-- Views that derive from SPRING BATCH

DROP VIEW ${ohdsiSchema}.estimation_analysis_generation;
DROP VIEW ${ohdsiSchema}.prediction_analysis_generation;

CREATE OR REPLACE VIEW ${ohdsiSchema}.cc_generation as (
  SELECT
    -- Spring batch based
    job.job_execution_id                     id,
    job.create_time                          start_time,
    job.end_time                             end_time,
    job.status                               status,
    job.exit_message                         exit_message,
    CAST(cc_id_param.parameter_value AS INTEGER)  cc_id,
    CAST(source_param.parameter_value AS INTEGER) source_id,
    -- Generation info based
    gen_info.hash_code                       hash_code,
    gen_info.created_by_id                   created_by_id
  FROM ${ohdsiSchema}.batch_job_execution job
    JOIN ${ohdsiSchema}.batch_job_execution_params cc_id_param ON job.job_execution_id = cc_id_param.job_execution_id 
      AND cc_id_param.parameter_name = 'cohort_characterization_id'
    JOIN ${ohdsiSchema}.batch_job_execution_params source_param ON job.job_execution_id = source_param.job_execution_id 
      AND source_param.parameter_name = 'source_id'
    JOIN ${ohdsiSchema}.source s on s.source_id = CAST(source_param.parameter_value AS INTEGER)
    LEFT JOIN ${ohdsiSchema}.analysis_generation_info gen_info ON job.job_execution_id = gen_info.job_execution_id
  ORDER BY start_time DESC
);

CREATE OR REPLACE VIEW ${ohdsiSchema}.pathway_analysis_generation as
  (SELECT
     job.job_execution_id                     id,
     job.create_time                          start_time,
     job.end_time                             end_time,
     job.status                               status,
     job.exit_message                         exit_message,
     CAST(pa_id_param.parameter_value AS INTEGER)  pathway_analysis_id,
     CAST(source_param.parameter_value AS INTEGER) source_id,
     -- Generation info based
     gen_info.hash_code                       hash_code,
     gen_info.created_by_id                   created_by_id
   FROM ${ohdsiSchema}.batch_job_execution job
   JOIN ${ohdsiSchema}.batch_job_execution_params pa_id_param ON job.job_execution_id = pa_id_param.job_execution_id 
    AND pa_id_param.parameter_name = 'pathway_analysis_id'
   JOIN ${ohdsiSchema}.batch_job_execution_params source_param ON job.job_execution_id = source_param.job_execution_id 
     AND source_param.parameter_name = 'source_id'
   JOIN ${ohdsiSchema}.source s on s.source_id = CAST(source_param.parameter_value AS INTEGER)
   LEFT JOIN ${ohdsiSchema}.analysis_generation_info gen_info
     ON job.job_execution_id = gen_info.job_execution_id
   ORDER BY start_time DESC);
