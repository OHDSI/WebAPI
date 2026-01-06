CREATE TABLE @results_schema.cc_results_temp LIKE @results_schema.cc_results;

INSERT INTO TABLE @results_schema.cc_results_temp SELECT type, cast(case fa_type when 'CRITERIA' then 'CRITERIA_SET' else fa_type end as VARCHAR(255)), cc_generation_id, analysis_id, analysis_name, covariate_id, covariate_name, strata_id, strata_name, time_window, concept_id, count_value, avg_value, stdev_value, min_value, p10_value, p25_value, median_value, p75_value, p90_value, max_value, cohort_definition_id
FROM @results_schema.cc_results;

DROP TABLE @results_schema.cc_results;

ALTER TABLE @results_schema.cc_results_temp RENAME TO @results_schema.cc_results;