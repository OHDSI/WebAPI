WITH qualified_events AS (
    SELECT ROW_NUMBER() OVER () AS event_id, subject_id AS person_id, cohort_start_date AS start_date, cohort_end_date AS end_date
    FROM @temp_database_schema.@targetTable where cohort_definition_id = @cohortId
)
insert into @results_database_schema.cc_results (type, fa_type, covariate_id, covariate_name, analysis_id, analysis_name, concept_id, count_value, avg_value, cohort_definition_id, cc_generation_id)
  select 'PREVALENCE' as type,
         'CRITERIA' as fa_type,
    @covariateId as covariate_id,
    '@covariateName' as covariate_name,
    @analysisId as analysis_id,
    '@analysisName' as analysis_name,
    @conceptId as concept_id,
    sum.sum_value as count_value,
    case
      when totals.total > 0 then (sum.sum_value * 1.0 / totals.total * 1.0)
      else 0.0
    end as stat_value,
    @cohortId as cohort_definition_id,
    @executionId as cc_generation_id
from (select count(*) as sum_value from(
   select person_id from ( @groupQuery ) pi group by pi.person_id) pci) sum,
  (select count(*) as total from  @temp_database_schema.@totalsTable where cohort_definition_id = @cohortId) totals
;
