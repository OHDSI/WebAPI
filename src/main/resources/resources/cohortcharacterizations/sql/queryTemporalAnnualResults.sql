select
       r.type,
       r.fa_type,
       r.cc_generation_id,
       r.analysis_id,
       r.analysis_name,
       r.covariate_id,
       r.covariate_name,
       c.concept_name,
       r.concept_id,
       r.count_value,
       r.avg_value,
       r.cohort_definition_id,
       r.strata_id,
       r.strata_name,
       r.event_year
from @results_database_schema.cc_temporal_annual_results r
  JOIN @vocabulary_schema.concept c on c.concept_id = r.concept_id
where r.cc_generation_id = @cohort_characterization_generation_id
  and r.analysis_id in (@analysis_ids)
  and r.cohort_definition_id in (@cohort_ids)
