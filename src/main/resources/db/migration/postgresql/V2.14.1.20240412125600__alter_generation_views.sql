DROP VIEW ${ohdsiSchema}.cc_generation;
DROP VIEW ${ohdsiSchema}.estimation_analysis_generation;
DROP VIEW ${ohdsiSchema}.pathway_analysis_generation;
DROP VIEW ${ohdsiSchema}.prediction_analysis_generation;

CREATE OR REPLACE VIEW ${ohdsiSchema}.cc_generation as (
  SELECT
    -- Spring batch based
    job.job_execution_id                     id,
    job.create_time                          start_time,
    job.end_time                             end_time,
    job.status                               status,
    job.exit_message                         exit_message,
    CAST(cc_id_param.string_val AS INTEGER)  cc_id,
    CAST(source_param.string_val AS INTEGER) source_id,
    -- Generation info based
    gen_info.hash_code                       hash_code,
    gen_info.created_by_id                   created_by_id
  FROM ${ohdsiSchema}.batch_job_execution job
    JOIN ${ohdsiSchema}.batch_job_execution_params cc_id_param ON job.job_execution_id = cc_id_param.job_execution_id 
      AND cc_id_param.key_name = 'cohort_characterization_id'
    JOIN ${ohdsiSchema}.batch_job_execution_params source_param ON job.job_execution_id = source_param.job_execution_id 
      AND source_param.key_name = 'source_id'
    JOIN ${ohdsiSchema}.source s on s.source_id = CAST(source_param.string_val AS INTEGER)
    LEFT JOIN ${ohdsiSchema}.analysis_generation_info gen_info ON job.job_execution_id = gen_info.job_execution_id
  ORDER BY start_time DESC
);

CREATE OR REPLACE VIEW ${ohdsiSchema}.estimation_analysis_generation as
  SELECT
    job.job_execution_id                     id,
    job.create_time                          start_time,
    job.end_time                             end_time,
    job.status                               status,
    job.exit_message                         exit_message,
    CAST(estimation_id_param.string_val AS INTEGER) estimation_id,
    CAST(source_param.string_val AS INTEGER) source_id,
    passwd_param.string_val                  update_password,
    -- Generation info based
    gen_info.hash_code                       hash_code,
    gen_info.created_by_id                   created_by_id,
    -- Execution info based
    exec_info.id                             analysis_execution_id
  FROM ${ohdsiSchema}.batch_job_execution job
    JOIN ${ohdsiSchema}.batch_job_execution_params estimation_id_param ON job.job_execution_id = estimation_id_param.job_execution_id 
      AND estimation_id_param.key_name = 'estimation_analysis_id'
    JOIN ${ohdsiSchema}.batch_job_execution_params source_param ON job.job_execution_id = source_param.job_execution_id 
      AND source_param.key_name = 'source_id'
    JOIN ${ohdsiSchema}.batch_job_execution_params passwd_param ON job.job_execution_id = passwd_param.job_execution_id 
      AND passwd_param.key_name = 'update_password'
    JOIN ${ohdsiSchema}.source s on s.source_id = CAST(source_param.string_val AS INTEGER)
    LEFT JOIN ${ohdsiSchema}.ee_analysis_status exec_info ON job.job_execution_id = exec_info.job_execution_id
    LEFT JOIN ${ohdsiSchema}.analysis_generation_info gen_info ON job.job_execution_id = gen_info.job_execution_id;

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
     gen_info.hash_code                       hash_code,
     gen_info.created_by_id                   created_by_id
   FROM ${ohdsiSchema}.batch_job_execution job
   JOIN ${ohdsiSchema}.batch_job_execution_params pa_id_param ON job.job_execution_id = pa_id_param.job_execution_id 
    AND pa_id_param.key_name = 'pathway_analysis_id'
   JOIN ${ohdsiSchema}.batch_job_execution_params source_param ON job.job_execution_id = source_param.job_execution_id 
     AND source_param.key_name = 'source_id'
   JOIN ${ohdsiSchema}.source s on s.source_id = CAST(source_param.string_val AS INTEGER)
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
    CAST(plp_id_param.string_val AS INTEGER) prediction_id,
    CAST(source_param.string_val AS INTEGER) source_id,
    passwd_param.string_val                  update_password,
    -- Generation info based
    gen_info.hash_code                       hash_code,
    gen_info.created_by_id                   created_by_id,
    -- Execution info based
    exec_info.id                             analysis_execution_id
  FROM ${ohdsiSchema}.batch_job_execution job
    JOIN ${ohdsiSchema}.batch_job_execution_params plp_id_param ON job.job_execution_id = plp_id_param.job_execution_id 
      AND plp_id_param.key_name = 'prediction_analysis_id'
    JOIN ${ohdsiSchema}.batch_job_execution_params source_param ON job.job_execution_id = source_param.job_execution_id 
      AND source_param.key_name = 'source_id'
    JOIN ${ohdsiSchema}.batch_job_execution_params passwd_param ON job.job_execution_id = passwd_param.job_execution_id 
      AND passwd_param.key_name = 'update_password'
    JOIN ${ohdsiSchema}.source s on s.source_id = CAST(source_param.string_val AS INTEGER)
    LEFT JOIN ${ohdsiSchema}.ee_analysis_status exec_info ON job.job_execution_id = exec_info.job_execution_id
    LEFT JOIN ${ohdsiSchema}.analysis_generation_info gen_info ON job.job_execution_id = gen_info.job_execution_id;
