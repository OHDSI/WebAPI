insert into @results_database_schema.cc_results (
     type,
     fa_type,
     covariate_id,
     covariate_name,
     analysis_id,
     analysis_name,
     concept_id,
     count_value,
     avg_value,
     cohort_definition_id,
     cc_generation_id)
select 'PREVALENCE'    as type,
        'CUSTOM_FE' as fa_type,
        covariate_id,
        covariate_name,
        @analysisId as analysis_id,
        @analysisName as analysis_name,
        concept_id,
        sum_value       as count_value,
        average_value   as stat_value,
        @cohortId            as cohort_definition_id,
        @jobId            as cc_generation_id
from (@design) subquery;
