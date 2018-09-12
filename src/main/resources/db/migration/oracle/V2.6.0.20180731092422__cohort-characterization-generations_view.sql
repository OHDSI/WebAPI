CREATE OR REPLACE VIEW ${ohdsiSchema}.cc_generations as
  (SELECT job.job_execution_id,
          MAX(job.create_time)                                                                     date,
          MAX(job.status)                                                                          status,
          MAX(CASE WHEN params.key_name = 'hash_code' THEN params.string_val END)                  hash_code,
          MAX(CASE WHEN params.key_name = 'cohort_characterization_id' THEN params.string_val END) cohort_characterization_id,
          MAX(CASE WHEN params.key_name = 'source_id' THEN params.string_val END)                  source_id
   FROM ${ohdsiSchema}.batch_job_execution job
          JOIN ${ohdsiSchema}.batch_job_execution_params params ON job.job_execution_id = params.job_execution_id
                                                                     AND (params.key_name = 'hash_code' OR
                                                                          params.key_name = 'cohort_characterization_id' OR
                                                                          params.key_name = 'source_id')
   GROUP BY job.job_execution_id);