insert into
 @results_database_schema.cc_results (type,
     fa_type,
     covariate_id,
     covariate_name,
     analysis_id,
     analysis_name,
     concept_id,
     count_value,
     min_value,
     max_value,
     avg_value,
     stdev_value,
     median_value,
     p10_value,
     p25_value,
     p75_value,
     p90_value,
     cohort_definition_id,
      strata_id,
      strata_name,
     cc_generation_id)
 select 'DISTRIBUTION' as type,
        'CUSTOM_FE' as fa_type,
        covariate_id,
        covariate_name,
        @analysisId as analysis_id,
        @analysisName as analysis_name,
        concept_id,
        count_value,
        min_value,
        max_value,
        average_value,
        standard_deviation,
        median_value,
        p10_value,
        p25_value,
        p75_value,
        p90_value,
        @cohortId as cohort_definition_id,
        @strataId as strata_id,
        @strataName as strata_name,
        @jobId as cc_generation_id
 from (@design) subquery;
