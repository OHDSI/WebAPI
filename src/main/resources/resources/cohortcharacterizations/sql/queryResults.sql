select
       r.type,
       r.fa_type,
       r.cc_generation_id,
       r.analysis_id,
       r.analysis_name,
       r.covariate_id,
       r.covariate_name,
       r.time_window,
       r.concept_id,
       r.count_value,
       r.avg_value,
       r.stdev_value,
       r.min_value,
       r.p10_value,
       r.p25_value,
       r.median_value,
       r.p75_value,
       r.p90_value,
       r.max_value,
       r.cohort_definition_id
from @cdm_results_schema.cc_results r
where r.cc_generation_id = @cohort_characterization_generation_id
