-- 1315                Distribution of numeric values, by measurement_concept_id and unit_concept_id
--insert into @results_schema.heracles_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)

select c1.cohort_definition_id,
  c1.subject_id,
  m1.measurement_concept_id,
  m1.unit_concept_id,
  m1.value_as_number as count_value
INTO #raw_1315
from @CDM_schema.measurement m1
{@cohort_period_only == 'true'} ? {join #HERACLES_cohort c1} : {join #HERACLES_cohort_subject c1}
on m1.person_id = c1.subject_id
where m1.value_as_number is not null and m1.unit_concept_id is not null
{@cohort_period_only == 'true'} ? {AND m1.measurement_date>=c1.cohort_start_date and m1.measurement_date<=c1.cohort_end_date }
{@measurement_concept_ids != ''} ? {AND m1.measurement_concept_id in (@measurement_concept_ids)}
;

WITH cteRawData as
(
    select cohort_definition_id, measurement_concept_id as stratum_1, unit_concept_id as stratum_2, count_value FROM #raw_1315
),
    overallStats as
  (
      select cohort_definition_id,
        stratum_1,
        stratum_2,
        avg(1.0 * count_value) as avg_value,
        stdev(count_value) as stdev_value,
        min(count_value) as min_value,
        max(count_value) as max_value,
        count_big(*) as total
      from cteRawData
      group by cohort_definition_id, stratum_1, stratum_2
  ),
    valueStats as
  (
      select cohort_definition_id,
        stratum_1,
        stratum_2,
        count_value,
        total,
        SUM(total) over (partition by cohort_definition_id, stratum_1, stratum_2 order by count_value ROWS UNBOUNDED PRECEDING) as accumulated
      FROM (
             select cohort_definition_id, stratum_1, stratum_2, count_value, count_big(*) as total
             FROM cteRawData
             GROUP BY cohort_definition_id, stratum_1, stratum_2, count_value
           ) D
  )
select o.cohort_definition_id,
  1315 as analysis_id,
  CAST(o.stratum_1 as VARCHAR) as stratum_1,
  CAST(o.stratum_2 as VARCHAR) as stratum_2,
  cast( '' as varchar(1) ) as stratum_3,cast( '' as varchar(1) ) as stratum_4, cast( '' as varchar(1) ) as stratum_5,
  o.total as count_value,
  o.min_value,
  o.max_value,
  o.avg_value,
  coalesce(o.stdev_value, 0.0) as stdev_value,
  MIN(case when s.accumulated >= .50 * o.total then count_value else o.max_value end) as median_value,
  MIN(case when s.accumulated >= .10 * o.total then count_value else o.max_value end) as p10_value,
  MIN(case when s.accumulated >= .25 * o.total then count_value else o.max_value end) as p25_value,
  MIN(case when s.accumulated >= .75 * o.total then count_value else o.max_value end) as p75_value,
  MIN(case when s.accumulated >= .90 * o.total then count_value else o.max_value end) as p90_value
into #results_dist_1315
from valueStats s
join overallStats o on s.cohort_definition_id = o.cohort_definition_id and s.stratum_1 = o.stratum_1 and s.stratum_2 = o.stratum_2
GROUP BY o.cohort_definition_id, o.stratum_1, o.stratum_2, o.total, o.min_value, o.max_value, o.avg_value, o.stdev_value
;

TRUNCATE TABLE #raw_1315;
DROP TABLE #raw_1315;