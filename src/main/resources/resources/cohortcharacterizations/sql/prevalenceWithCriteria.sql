insert into @results_database_schema.cc_results (type, fa_type, covariate_id, covariate_name, analysis_id, analysis_name, concept_id, count_value, avg_value, cohort_definition_id, cc_generation_id)
  select 'PREVALENCE' as type,
         'CRITERIA' as fa_type,
    f.covariate_id,
    f.covariate_name,
    f.analysis_id,
    f.analysis_name,
    f.concept_id,
    f.sum_value     as count_value,
    f.percentage_value as stat_value,
    @cohortId as cohort_definition_id,
    @executionId as cc_generation_id
from (select @covariateId as covariate_id,
        '@covariateName' as covariate_name,
        @analysisId as analysis_id,
        '@analysisName' as analysis_name,
        @conceptId as concept_id,
        final_count as sum_value,
        case
          when base_count > 0 then (final_count * 1.0 / base_count * 1.0)
          else 0.0
        end as percentage_value
    from @results_database_schema.cohort_summary_stats
    where
        cohort_definition_id = @cohortId
) f;
