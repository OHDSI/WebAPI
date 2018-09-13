CREATE OR REPLACE VIEW ${ohdsiSchema}.cc_generations as
  SELECT job.job_execution_id                                               id,
         job.create_time                                                    start_time,
         job.end_time                                                       end_time,
         job.status                                                         status,
         design_param.string_val                                            design,
         hash_code_param.string_val                                         hash_code,
         CAST(CAST(cc_id_param.string_val AS VARCHAR2(50)) AS NUMBER(10))   cohort_characterization_id,
         CAST(CAST(source_param.string_val AS VARCHAR2(50)) AS NUMBER(10))  source_id
  FROM OHDSI2.batch_job_execution job
    JOIN ${ohdsiSchema}.batch_job_execution_params design_param
      ON job.job_execution_id = design_param.job_execution_id AND design_param.key_name = 'design'
    JOIN ${ohdsiSchema}.batch_job_execution_params hash_code_param
      ON job.job_execution_id = hash_code_param.job_execution_id AND hash_code_param.key_name = 'hash_code'
    JOIN ${ohdsiSchema}.batch_job_execution_params cc_id_param
      ON job.job_execution_id = cc_id_param.job_execution_id AND cc_id_param.key_name = 'cohort_characterization_id'
    JOIN ${ohdsiSchema}.batch_job_execution_params source_param
      ON job.job_execution_id = source_param.job_execution_id AND source_param.key_name = 'source_id'
  ORDER BY start_time DESC;