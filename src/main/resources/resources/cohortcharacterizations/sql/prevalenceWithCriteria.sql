CREATE TABLE @eventsTable(
  event_id BIGINT,
  person_id BIGINT,
  start_date DATE,
  end_date DATE,
  op_start_date DATE,
  op_end_date DATE
);

INSERT INTO @eventsTable(event_id, person_id, start_date, end_date, op_start_date, op_end_date)
SELECT ROW_NUMBER() OVER (partition by E.subject_id order by E.cohort_start_date) AS event_id, E.subject_id AS person_id, E.cohort_start_date AS start_date, E.cohort_end_date AS end_date, OP.observation_period_start_date AS op_start_date, OP.observation_period_end_date AS op_end_date
FROM @targetTable E
  JOIN @cdm_database_schema.observation_period OP ON E.subject_id = OP.person_id AND E.cohort_start_date >= OP.observation_period_start_date AND E.cohort_start_date <= OP.observation_period_end_date
WHERE cohort_definition_id = @cohortId
;

insert into @results_database_schema.cc_results (type, fa_type, covariate_id, covariate_name, analysis_id, analysis_name, concept_id, count_value, avg_value, strata_id, strata_name, cohort_definition_id, cc_generation_id)
  select CAST('PREVALENCE' AS VARCHAR(255)) as type,
         CAST('CRITERIA' AS VARCHAR(255)) as fa_type,
    CAST(@covariateId AS BIGINT) as covariate_id,
    CAST('@covariateName' AS VARCHAR(1000)) as covariate_name,
    CAST(@analysisId AS INTEGER) as analysis_id,
    CAST('@analysisName' AS VARCHAR(1000)) as analysis_name,
    CAST(@conceptId AS INTEGER) as concept_id,
    sum.sum_value as count_value,
    case
      when totals.total > 0 then (sum.sum_value * 1.0 / totals.total * 1.0)
      else 0.0
    end as stat_value,
    CAST(@strataId AS BIGINT) as strata_id,
    CAST(@strataName AS VARCHAR(1000)) as strata_name,
    CAST(@cohortId AS BIGINT) as cohort_definition_id,
    CAST(@executionId AS BIGINT) as cc_generation_id
from (select count(*) as sum_value from(
   select person_id from ( @groupQuery ) pi group by pi.person_id) pci) sum,
  (select count(*) as total from  @temp_database_schema.@totalsTable where cohort_definition_id = @cohortId) totals
;

IF OBJECT_ID('@eventsTable', 'U') IS NOT NULL
  DROP TABLE @eventsTable;