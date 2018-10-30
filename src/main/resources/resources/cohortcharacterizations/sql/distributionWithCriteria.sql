IF OBJECT_ID('tempdb..#events_count', 'U') IS NOT NULL
  DROP TABLE #events_count;

IF OBJECT_ID('tempdb..#people_events', 'U') IS NOT NULL
  DROP TABLE #people_events;

IF OBJECT_ID('tempdb..#criteria_events', 'U') IS NOT NULL
  DROP TABLE #criteria_events;

select
  person_id,
  @domain_id_field as event_id,
  @domain_start_date as op_start_date,
  COALESCE(@domain_end_date, DATEADD(day, 1, @domain_start_date)) as op_end_date
into #people_events
from @cdm_database_schema.@domain_table dt
join @temp_database_schema.@targetTable c on dt.person_id = c.subject_id;

select person_id, event_id
into #criteria_events
from (@events_criteria) ec;

select
  v.person_id as person_id,
  count(*) as value_as_int
into #events_count
from @temp_database_schema.@targetTable c
  join #criteria_events v on v.person_id = c.subject_id
where
  cohort_definition_id = @cohortId
group by v.person_id;

with
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
  event_stat_values.min_value,
  event_stat_values.max_value,
  event_stat_values.avg_value,
  event_stat_values.sdtev_value,
  events_p10_value.p10 as p10_value,
  events_p25_value.p25 as p25_value,
  events_median_value.median_value,
  events_p75_value.p75 as p75_value,
  events_p90_value.p90 as p90_value
from events_max_value, event_stat_values, events_p10_value, events_p25_value, events_median_value, events_p75_value, events_p90_value;

truncate table #people_events;
drop table #people_events;

truncate table #criteria_events;
drop table #criteria_events;

truncate table #events_count;
drop table #events_count;