insert into @results_database_schema.cc_results (type, covariate_id, covariate_name, analysis_id, analysis_name, concept_id, count_value, avg_value, cohort_definition_id, cc_generation_id)
  select 'PREVALENCE' as type,
    f.covariate_id,
    fr.covariate_name,
    ar.analysis_id,
    ar.analysis_name,
    fr.concept_id,
    f.sum_value     as count_value,
    f.average_value as stat_value,
    @cohortId as cohort_definition_id,
    @executionId as cc_generation_id
  from (@features) f
    join (@featureRefs) fr on fr.covariate_id = f.covariate_id and fr.cohort_definition_id = f.cohort_definition_id
    join (@analysisRefs) ar
      on ar.analysis_id = fr.analysis_id and ar.cohort_definition_id = fr.cohort_definition_id
    left join @cdm_database_schema.concept c on c.concept_id = fr.concept_id;