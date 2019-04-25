-- 107   Length of observation (days) of first observation period by age decile
--insert into @results_schema.heracles_results_dist (cohort_definition_id, analysis_id, stratum_1, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)
-- stratum_1 (in this case, gender concept_id)

select c1.cohort_definition_id,
  CAST(floor((year(op1.observation_period_START_DATE) - p1.YEAR_OF_BIRTH)/10) AS INT) as age_decile,
  DATEDIFF(dd,op1.observation_period_start_date, op1.observation_period_end_date) as count_value
INTO #raw_107
from @CDM_schema.person p1
inner join #HERACLES_cohort_subject c1 on p1.person_id = c1.subject_id
inner join (
select person_id,
observation_period_START_DATE,
observation_period_END_DATE,
ROW_NUMBER() over (PARTITION by person_id order by observation_period_start_date asc) as rn1
from @CDM_schema.observation_period
) op1 on p1.PERSON_ID = op1.PERSON_ID
where op1.rn1 = 1
;

WITH cteRawData as
(
    select cohort_definition_id, age_decile as stratum_1, count_value FROM #raw_107
),
    overallStats as
  (
      select cohort_definition_id,
        stratum_1,
        avg(1.0 * count_value) as avg_value,
        stdev(count_value) as stdev_value,
        min(count_value) as min_value,
        max(count_value) as max_value,
        count_big(*) as total
      from cteRawData
      group by cohort_definition_id, stratum_1
  ),
    valueStats as
  (
      select cohort_definition_id,
        stratum_1,
        count_value,
        total,
        SUM(total) over (partition by cohort_definition_id, stratum_1 order by count_value ROWS UNBOUNDED PRECEDING) as accumulated
      FROM (
             select cohort_definition_id, stratum_1, count_value, count_big(*) as total
             FROM cteRawData
             GROUP BY cohort_definition_id, stratum_1, count_value
           ) D
  )
select o.cohort_definition_id,
  107 as analysis_id,
  CAST(o.stratum_1 as VARCHAR) as stratum_1,
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
into #results_dist_107
from valueStats s
join overallStats o on s.cohort_definition_id = o.cohort_definition_id and s.stratum_1 = o.stratum_1
GROUP BY o.cohort_definition_id, o.stratum_1, o.total, o.min_value, o.max_value, o.avg_value, o.stdev_value
;

TRUNCATE TABLE #raw_107;
DROP TABLE #raw_107;