insert into @results_database_schema.cc_results (type, fa_type, covariate_id, covariate_name, analysis_id, analysis_name, concept_id,
    count_value, min_value, max_value, avg_value, stdev_value, median_value,
    p10_value, p25_value, p75_value, p90_value, strata_id, strata_name, cohort_definition_id, cc_generation_id)
  select CAST('DISTRIBUTION' AS VARCHAR(255)) as type,
    CAST('PRESET' AS VARCHAR(255)) as fa_type,
    f.covariate_id,
    fr.covariate_name,
    ar.analysis_id,
    ar.analysis_name,
    fr.concept_id,
    f.count_value,
    f.min_value,
    f.max_value,
    f.average_value,
    f.standard_deviation,
    f.median_value,
    f.p10_value,
    f.p25_value,
    f.p75_value,
    f.p90_value,
    @strataId as strata_id,
    CAST(@strataName AS VARCHAR(1000)) as strata_name,
    @cohortId as cohort_definition_id,
    @executionId as cc_generation_id
  from (@features) f
    join (@featureRefs) fr on fr.covariate_id = f.covariate_id and fr.cohort_definition_id = f.cohort_definition_id
    join (@analysisRefs) ar
      on ar.analysis_id = fr.analysis_id and ar.cohort_definition_id = fr.cohort_definition_id
    left join @cdm_database_schema.concept c on c.concept_id = fr.concept_id;
