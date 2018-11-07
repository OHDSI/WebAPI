IF OBJECT_ID('tempdb..#events_count', 'U') IS NOT NULL
  DROP TABLE #events_count;

IF OBJECT_ID('tempdb..#qualified_events', 'U') IS NOT NULL
  DROP TABLE #qualified_events;

SELECT
  ROW_NUMBER() OVER () AS event_id,
  subject_id AS person_id,
  cohort_start_date AS start_date,
  cohort_end_date AS end_date
INTO #qualified_events
FROM @temp_database_schema.@targetTable where cohort_definition_id = @cohortId;

select
  v.person_id as person_id,
  count(*) as value_as_int
into #events_count
from ( @groupQuery ) v
group by v.person_id;

with
  total_cohort_count AS (
    SELECT COUNT(*) cnt FROM @temp_database_schema.@targetTable where cohort_definition_id = @cohortId
  ),
  events_max_value as (
    select max(value_as_int) as max_value from #events_count
  ),
  event_stat_values as (
    select
      count(*) as count_value,
      min(value_as_int) as min_value,
      max(value_as_int) as max_value,
      avg(value_as_int) as avg_value,
      stdev(value_as_int) as sdtev_value
    from #events_count
  ),
  events_p10_value as (
    select count(*) as p10 from #events_count, events_max_value where value_as_int < 0.1 * events_max_value.max_value
  ),
  events_p25_value as (
    select count(*) as p25 from #events_count, events_max_value where value_as_int < 0.25 * events_max_value.max_value
  ),
  events_median_value as (
    select count(*) as median_value from #events_count, events_max_value where value_as_int < 0.5 * events_max_value.max_value
  ),
  events_p75_value as (
    select count(*) as p75 from #events_count, events_max_value where value_as_int < 0.75 * events_max_value.max_value
  ),
  events_p90_value as (
    select count(*) as p90 from #events_count, events_max_value where value_as_int < 0.9 * events_max_value.max_value
  )
insert into @results_database_schema.cc_results(type, fa_type, covariate_id, covariate_name, analysis_id, analysis_name, concept_id,
  cohort_definition_id, cc_generation_id, count_value, min_value, max_value, avg_value, stdev_value, p10_value, p25_value, median_value, p75_value, p90_value)
select
  'DISTRIBUTION' as type,
  'CRITERIA' as fa_type,
  @covariateId as covariate_id,
  '@covariateName' as covariate_name,
  @analysisId as analysis_id,
  '@analysisName' as analysis_name,
  @conceptId as concept_id,
  @cohortId as cohort_definition_id,
  @executionId as cc_generation_id,
  event_stat_values.count_value,
  CASE WHEN event_stat_values.min_value = total_cohort_count.cnt THEN event_stat_values.min_value ELSE 0 END as min_value,
  event_stat_values.max_value,
  event_stat_values.avg_value,
  event_stat_values.sdtev_value,
  events_p10_value.p10 as p10_value,
  events_p25_value.p25 as p25_value,
  events_median_value.median_value,
  events_p75_value.p75 as p75_value,
  events_p90_value.p90 as p90_value
from events_max_value, event_stat_values, events_p10_value, events_p25_value, events_median_value, events_p75_value, events_p90_value, total_cohort_count;

truncate table #qualified_events;
drop table #qualified_events;

truncate table #events_count;
drop table #events_count;