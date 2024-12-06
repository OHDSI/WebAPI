DROP VIEW ${ohdsiSchema}.cc_generation;
DROP VIEW ${ohdsiSchema}.estimation_analysis_generation;
DROP VIEW ${ohdsiSchema}.pathway_analysis_generation;
DROP VIEW ${ohdsiSchema}.prediction_analysis_generation;
DROP VIEW ${ohdsiSchema}.user_import_job_history;

DROP TABLE ${ohdsiSchema}.BATCH_JOB_INSTANCE CASCADE;
DROP TABLE ${ohdsiSchema}.BATCH_JOB_EXECUTION CASCADE;
DROP TABLE ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS CASCADE;
DROP TABLE ${ohdsiSchema}.BATCH_STEP_EXECUTION CASCADE;
DROP TABLE ${ohdsiSchema}.BATCH_STEP_EXECUTION_CONTEXT CASCADE;
DROP TABLE ${ohdsiSchema}.BATCH_JOB_EXECUTION_CONTEXT CASCADE;

CREATE TABLE ${ohdsiSchema}.BATCH_JOB_INSTANCE  (
	JOB_INSTANCE_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT ,
	JOB_NAME VARCHAR(100) NOT NULL,
	JOB_KEY VARCHAR(32) NOT NULL,
	constraint JOB_INST_UN unique (JOB_NAME, JOB_KEY)
) ;

CREATE TABLE ${ohdsiSchema}.BATCH_JOB_EXECUTION  (
	JOB_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT  ,
	JOB_INSTANCE_ID BIGINT NOT NULL,
	CREATE_TIME TIMESTAMP NOT NULL,
	START_TIME TIMESTAMP DEFAULT NULL ,
	END_TIME TIMESTAMP DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED TIMESTAMP,
	constraint JOB_INST_EXEC_FK foreign key (JOB_INSTANCE_ID)
	references BATCH_JOB_INSTANCE(JOB_INSTANCE_ID)
) ;

CREATE TABLE ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS  (
	JOB_EXECUTION_ID BIGINT NOT NULL ,
	PARAMETER_NAME VARCHAR(100) NOT NULL ,
	PARAMETER_TYPE VARCHAR(100) NOT NULL ,
	PARAMETER_VALUE VARCHAR(2500) ,
	IDENTIFYING CHAR(1) NOT NULL ,
	constraint JOB_EXEC_PARAMS_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE ${ohdsiSchema}.BATCH_STEP_EXECUTION  (
	STEP_EXECUTION_ID BIGINT  NOT NULL PRIMARY KEY ,
	VERSION BIGINT NOT NULL,
	STEP_NAME VARCHAR(100) NOT NULL,
	JOB_EXECUTION_ID BIGINT NOT NULL,
	CREATE_TIME TIMESTAMP NOT NULL,
	START_TIME TIMESTAMP DEFAULT NULL ,
	END_TIME TIMESTAMP DEFAULT NULL ,
	STATUS VARCHAR(10) ,
	COMMIT_COUNT BIGINT ,
	READ_COUNT BIGINT ,
	FILTER_COUNT BIGINT ,
	WRITE_COUNT BIGINT ,
	READ_SKIP_COUNT BIGINT ,
	WRITE_SKIP_COUNT BIGINT ,
	PROCESS_SKIP_COUNT BIGINT ,
	ROLLBACK_COUNT BIGINT ,
	EXIT_CODE VARCHAR(2500) ,
	EXIT_MESSAGE VARCHAR(2500) ,
	LAST_UPDATED TIMESTAMP,
	constraint JOB_EXEC_STEP_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;

CREATE TABLE ${ohdsiSchema}.BATCH_STEP_EXECUTION_CONTEXT  (
	STEP_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT TEXT ,
	constraint STEP_EXEC_CTX_FK foreign key (STEP_EXECUTION_ID)
	references BATCH_STEP_EXECUTION(STEP_EXECUTION_ID)
) ;

CREATE TABLE ${ohdsiSchema}.BATCH_JOB_EXECUTION_CONTEXT  (
	JOB_EXECUTION_ID BIGINT NOT NULL PRIMARY KEY,
	SHORT_CONTEXT VARCHAR(2500) NOT NULL,
	SERIALIZED_CONTEXT TEXT ,
	constraint JOB_EXEC_CTX_FK foreign key (JOB_EXECUTION_ID)
	references BATCH_JOB_EXECUTION(JOB_EXECUTION_ID)
) ;


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
    JOIN ${ohdsiSchema}.batch_job_execution_params cc_id_param
      ON job.job_execution_id = cc_id_param.job_execution_id AND cc_id_param.parameter_name = 'cohort_characterization_id'
    JOIN ${ohdsiSchema}.batch_job_execution_params source_param
      ON job.job_execution_id = source_param.job_execution_id AND source_param.parameter_name = 'source_id'
    LEFT JOIN ${ohdsiSchema}.analysis_generation_info gen_info
      ON job.job_execution_id = gen_info.job_execution_id
  ORDER BY start_time DESC
);

CREATE OR REPLACE VIEW ${ohdsiSchema}.estimation_analysis_generation as
  SELECT
    job.job_execution_id                     id,
    job.create_time                          start_time,
    job.end_time                             end_time,
    job.status                               status,
    job.exit_message                         exit_message,
    CAST(estimation_id_param.parameter_value AS INTEGER) estimation_id,
    CAST(source_param.parameter_value AS INTEGER) source_id,
    passwd_param.parameter_value                  update_password,
    -- Generation info based
    gen_info.hash_code                       hash_code,
    gen_info.created_by_id                   created_by_id,
    -- Execution info based
    exec_info.id                             analysis_execution_id
  FROM ${ohdsiSchema}.batch_job_execution job
    JOIN ${ohdsiSchema}.batch_job_execution_params estimation_id_param ON job.job_execution_id = estimation_id_param.job_execution_id AND estimation_id_param.parameter_name = 'estimation_analysis_id'
    JOIN ${ohdsiSchema}.batch_job_execution_params source_param ON job.job_execution_id = source_param.job_execution_id AND source_param.parameter_name = 'source_id'
    JOIN ${ohdsiSchema}.batch_job_execution_params passwd_param ON job.job_execution_id = passwd_param.job_execution_id AND passwd_param.parameter_name = 'update_password'
    LEFT JOIN ${ohdsiSchema}.ee_analysis_status exec_info ON job.job_execution_id = exec_info.job_execution_id
    LEFT JOIN ${ohdsiSchema}.analysis_generation_info gen_info ON job.job_execution_id = gen_info.job_execution_id;

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
     JOIN ${ohdsiSchema}.batch_job_execution_params pa_id_param
       ON job.job_execution_id = pa_id_param.job_execution_id AND pa_id_param.parameter_name = 'pathway_analysis_id'
     JOIN ${ohdsiSchema}.batch_job_execution_params source_param
       ON job.job_execution_id = source_param.job_execution_id AND source_param.parameter_name = 'source_id'
     LEFT JOIN ${ohdsiSchema}.analysis_generation_info gen_info
       ON job.job_execution_id = gen_info.job_execution_id
   ORDER BY start_time DESC);

CREATE OR REPLACE VIEW ${ohdsiSchema}.prediction_analysis_generation as
  SELECT
    job.job_execution_id                     id,
    job.create_time                          start_time,
    job.end_time                             end_time,
    job.status                               status,
    job.exit_message                         exit_message,
    CAST(plp_id_param.parameter_value AS INTEGER) prediction_id,
    CAST(source_param.parameter_value AS INTEGER) source_id,
    passwd_param.parameter_value                  update_password,
    -- Generation info based
    gen_info.hash_code                       hash_code,
    gen_info.created_by_id                   created_by_id,
    -- Execution info based
    exec_info.id                             analysis_execution_id
  FROM ${ohdsiSchema}.batch_job_execution job
    JOIN ${ohdsiSchema}.batch_job_execution_params plp_id_param ON job.job_execution_id = plp_id_param.job_execution_id AND plp_id_param.parameter_name = 'prediction_analysis_id'
    JOIN ${ohdsiSchema}.batch_job_execution_params source_param ON job.job_execution_id = source_param.job_execution_id AND source_param.parameter_name = 'source_id'
    JOIN ${ohdsiSchema}.batch_job_execution_params passwd_param ON job.job_execution_id = passwd_param.job_execution_id AND passwd_param.parameter_name = 'update_password'
    LEFT JOIN ${ohdsiSchema}.ee_analysis_status exec_info ON job.job_execution_id = exec_info.job_execution_id
    LEFT JOIN ${ohdsiSchema}.analysis_generation_info gen_info ON job.job_execution_id = gen_info.job_execution_id;


CREATE OR REPLACE VIEW ${ohdsiSchema}.user_import_job_history
  AS
    SELECT
      job.job_execution_id as id,
      job.start_time as start_time,
      job.end_time as end_time,
      job.status as status,
      job.exit_code as exit_code,
      job.exit_message as exit_message,
      name_param.parameter_value as job_name,
      author_param.parameter_value as author,
      CAST(user_import_param.parameter_value AS INTEGER) user_import_id
    FROM
      ${ohdsiSchema}.BATCH_JOB_EXECUTION job
      JOIN ${ohdsiSchema}.BATCH_JOB_INSTANCE instance ON instance.JOB_INSTANCE_ID = job.JOB_INSTANCE_ID
      JOIN ${ohdsiSchema}.batch_job_execution_params name_param
        ON job.job_execution_id = name_param.job_execution_id AND name_param.parameter_name = 'jobName'
      JOIN ${ohdsiSchema}.batch_job_execution_params user_import_param
        ON job.job_execution_id = user_import_param.job_execution_id AND user_import_param.parameter_name = 'user_import_id'
      JOIN ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS author_param
        ON job.JOB_EXECUTION_ID = author_param.JOB_EXECUTION_ID AND author_param.parameter_name = 'jobAuthor'
    WHERE
      instance.JOB_NAME = 'usersImport';