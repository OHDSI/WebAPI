IF OBJECT_ID('tempdb..#events_count', 'U') IS NOT NULL
  DROP TABLE #events_count;

select
  v.person_id as person_id,
  count(*) as value_as_int
into #events_count
from @temp_database_schema.@targetTable c
  join @cdm_database_schema.@domain_table v on v.person_id = c.subject_id
where
  cohort_definition_id = @cohortId
group by v.person_id;

with
  t1 as (
    select max(value_as_int) as max_value from #events_count
  ),
  t2 as (
    select
      count(*) as count_value,
      min(value_as_int) as min_value,
      max(value_as_int) as max_value,
      avg(value_as_int) as avg_value,
      stdev(value_as_int) as sdtev_value
    from #events_count
  ),
  t3 as (
    select count(*) as p10 from #events_count, t1 where value_as_int < 0.1 * t1.max_value
  ),
  t4 as (
    select count(*) as p25 from #events_count, t1 where value_as_int < 0.25 * t1.max_value
  ),
  t5 as (
    select count(*) as median_value from #events_count, t1 where value_as_int < 0.5 * t1.max_value
  ),
  t6 as (
    select count(*) as p75 from #events_count, t1 where value_as_int < 0.75 * t1.max_value
  ),
  t7 as (
    select count(*) as p90 from #events_count, t1 where value_as_int < 0.9 * t1.max_value
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
  t2.count_value,
  t2.min_value,
  t2.max_value,
  t2.avg_value,
  t2.sdtev_value,
  t3.p10 as p10_value,
  t4.p25 as p25_value,
  t5.median_value,
  t6.p75 as p75_value,
  t7.p90 as p90_value
from t1, t2, t3, t4, t5, t6, t7;

truncate table #events_count;
drop table #events_count;