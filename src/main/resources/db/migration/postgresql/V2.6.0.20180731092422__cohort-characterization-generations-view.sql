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
  gen_info.design                          design,
  gen_info.hash_code                       hash_code,
  gen_info.created_by_id                   created_by_id
FROM ${ohdsiSchema}.batch_job_execution job
  JOIN ${ohdsiSchema}.batch_job_execution_params cc_id_param
    ON job.job_execution_id = cc_id_param.job_execution_id AND cc_id_param.key_name = 'cohort_characterization_id'
  JOIN ${ohdsiSchema}.batch_job_execution_params source_param
    ON job.job_execution_id = source_param.job_execution_id AND source_param.key_name = 'source_id'
  LEFT JOIN ${ohdsiSchema}.analysis_generation_info gen_info
    ON job.job_execution_id = gen_info.job_execution_id
ORDER BY start_time DESC

);

-- TODO indexes