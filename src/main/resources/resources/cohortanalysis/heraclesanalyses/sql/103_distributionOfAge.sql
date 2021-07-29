-- 103   Distribution of age at first observation period
--insert into @results_schema.heracles_results_dist (cohort_definition_id, analysis_id, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)

select c1.cohort_definition_id,
  c1.subject_id,
  year(op1.index_date) - p1.YEAR_OF_BIRTH as count_value
INTO #raw_103
from @CDM_schema.person p1
inner join #HERACLES_cohort_subject c1 on p1.person_id = c1.subject_id
inner join (
select person_id, MIN(observation_period_start_date) as index_date
from @CDM_schema.observation_period
group by PERSON_ID) op1 on p1.PERSON_ID = op1.PERSON_ID
;

WITH cteRawData (cohort_definition_id, count_value) as
(
    select cohort_definition_id, count_value FROM #raw_103
),
    overallStats (cohort_definition_id, avg_value, stdev_value, min_value, max_value, total) as
  (
      select cohort_definition_id,
        avg(1.0 * count_value) as avg_value,
        stdev(count_value) as stdev_value,
        min(count_value) as min_value,
        max(count_value) as max_value,
        count_big(*) as total
      from cteRawData
      group by cohort_definition_id
  ),
    valueStats (cohort_definition_id, count_value, total, accumulated) as
  (
      select cohort_definition_id,
        count_value,
        total,
        SUM(total) over (partition by cohort_definition_id order by count_value ROWS UNBOUNDED PRECEDING) as accumulated
      FROM (
             select cohort_definition_id, count_value, count_big(*) as total
             FROM cteRawData
             GROUP BY cohort_definition_id, count_value
           ) D
  )
select o.cohort_definition_id,
  103 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1,
  cast( '' as varchar(1) ) as stratum_2,
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
into #results_dist_103
from valueStats s
join overallStats o on s.cohort_definition_id = o.cohort_definition_id
GROUP BY o.cohort_definition_id, o.total, o.min_value, o.max_value, o.avg_value, o.stdev_value
;

TRUNCATE TABLE #raw_103;
DROP TABLE #raw_103;