IF OBJECT_ID('tempdb..#events_count', 'U') IS NOT NULL
  DROP TABLE #events_count;

WITH qualified_events AS (
  SELECT ROW_NUMBER() OVER (partition by E.subject_id order by E.cohort_start_date) AS event_id, E.subject_id AS person_id, E.cohort_start_date AS start_date, E.cohort_end_date AS end_date, OP.observation_period_start_date AS op_start_date, OP.observation_period_end_date AS op_end_date
  FROM @targetTable E
    JOIN @cdm_database_schema.observation_period OP ON E.subject_id = OP.person_id AND E.cohort_start_date >= OP.observation_period_start_date AND E.cohort_start_date <= OP.observation_period_end_date
  WHERE cohort_definition_id = @cohortId
)
select
  v.person_id as person_id,
  count(*) as value_as_int
into #events_count
from ( @groupQuery ) v
group by v.person_id;

with
  total_cohort_count AS (
    SELECT COUNT(*) cnt FROM @targetTable where cohort_definition_id = @cohortId
  ),
  events_max_value as (
    select max(value_as_int) as max_value from #events_count
  ),
  event_stat_values as (
    select
      count(*) as count_value,
      min(value_as_int) as min_value,
      max(value_as_int) as max_value,
      sum(value_as_int) as sum_value,
      stdev(value_as_int) as stdev_value,
      total_cohort_count.cnt - count(*) as count_no_value,
      total_cohort_count.cnt as population_size
    from #events_count, total_cohort_count
    group by total_cohort_count.cnt
  ),
  event_prep as (select row_number() over (order by value_as_int) as rn, value_as_int, count(*) as people_count from #events_count group by value_as_int),
  events_dist as (
    select s.value_as_int, sum(p.people_count) as people_count
    from event_prep s join event_prep p on p.rn <= s.rn group by s.value_as_int
  ),
  events_p10_value as (
    select min(value_as_int) as p10 from events_dist, event_stat_values where (people_count + count_no_value) >= 0.1 * population_size
  ),
  events_p25_value as (
    select min(value_as_int) as p25 from events_dist, event_stat_values where (people_count + count_no_value) >= 0.25 * population_size
  ),
  events_median_value as (
      select min(value_as_int) as median_value from events_dist, event_stat_values where (people_count + count_no_value) >= 0.5 * population_size
  ),
  events_p75_value as (
    select min(value_as_int) as p75 from events_dist, event_stat_values where people_count + count_no_value >= 0.75 * population_size
  ),
  events_p90_value as (
    select min(value_as_int) as p90 from events_dist, event_stat_values where people_count + count_no_value >= 0.9 * population_size
  )
select
  CAST('DISTRIBUTION' AS VARCHAR(255)) as type,
  CAST('CRITERIA' AS VARCHAR(255)) as fa_type,
  CAST(@covariateId AS BIGINT) as covariate_id,
  CAST('@covariateName' AS VARCHAR(1000)) as covariate_name,
  CAST(@analysisId AS INT) as analysis_id,
  CAST('@analysisName' AS VARCHAR(1000)) as analysis_name,
  CAST(@conceptId AS INT) as concept_id,
  CAST(@cohortId AS BIGINT) as cohort_definition_id,
  CAST(@executionId AS BIGINT) as cc_generation_id,
  CAST(@strataId AS BIGINT) as strata_id,
  CAST('@strataName' AS VARCHAR(255)) as strata_name,
  event_stat_values.count_value,
  CAST(case when count_no_value = 0 then event_stat_values.min_value else 0 end AS float) as min_value,
  event_stat_values.max_value,
  cast(event_stat_values.sum_value / (1.0 * population_size) as float) as avg_value,
  event_stat_values.stdev_value,
  case when population_size * .10 < count_no_value then 0 else events_p10_value.p10 end as p10_value,
  case when population_size * .25 < count_no_value then 0 else events_p25_value.p25 end as p25_value,
  case when population_size * .50 < count_no_value then 0 else events_median_value.median_value end as median_value,
  case when population_size * .75 < count_no_value then 0 else events_p75_value.p75 end as p75_value,
  case when population_size * .90 < count_no_value then 0 else events_p90_value.p90 end as p90_value
INTO #events_dist
from events_max_value, event_stat_values, events_p10_value, events_p25_value, events_median_value, events_p75_value, events_p90_value;

insert into @results_database_schema.cc_results(type, fa_type, covariate_id, covariate_name, analysis_id, analysis_name, concept_id,
  cohort_definition_id, cc_generation_id, strata_id, strata_name, count_value, min_value, max_value, avg_value, stdev_value, p10_value, p25_value, median_value, p75_value, p90_value)
select type, fa_type, covariate_id, covariate_name, analysis_id, analysis_name, concept_id,
  cohort_definition_id, cc_generation_id, strata_id, strata_name, count_value, min_value, max_value, avg_value, stdev_value, p10_value, p25_value, median_value, p75_value, p90_value
FROM #events_dist;

truncate table #events_dist;
drop table #events_dist;

truncate table #events_count;
drop table #events_count;