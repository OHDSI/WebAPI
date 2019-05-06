-- 1006                Distribution of age by condition_concept_id
--insert into @results_schema.heracles_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)

select co1.cohort_definition_id,
  co1.subject_id,
  co1.condition_concept_id,
  p1.gender_concept_id,
  co1.condition_start_year - p1.year_of_birth as count_value
INTO #raw_1006
from @CDM_schema.person p1
join (
select c1.cohort_definition_id, c1.subject_id, ce0.condition_concept_id, min(year(ce0.condition_era_start_date)) as condition_start_year
from @CDM_schema.condition_era ce0
{@cohort_period_only == 'true'} ?
{join #HERACLES_cohort c1} : {join #HERACLES_cohort_subject c1} on ce0.person_id = c1.subject_id
where 1=1
{@cohort_period_only == 'true'} ? {AND ce0.condition_era_start_date>=c1.cohort_start_date and ce0.condition_era_end_date<=c1.cohort_end_date }
{@condition_concept_ids != ''} ? {AND ce0.condition_concept_id in (@condition_concept_ids)}
group by c1.cohort_definition_id, c1.subject_id, ce0.condition_concept_id
) co1 on co1.subject_id = p1.person_id
;

WITH cteRawData as
(
    select cohort_definition_id, condition_concept_id AS stratum_1, gender_concept_id AS stratum_2, count_value FROM #raw_1006
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
  1006 as analysis_id,
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
into #results_dist_1006
from valueStats s
join overallStats o on s.cohort_definition_id = o.cohort_definition_id and s.stratum_1 = o.stratum_1 and s.stratum_2 = o.stratum_2
GROUP BY o.cohort_definition_id, o.stratum_1, o.stratum_2, o.total, o.min_value, o.max_value, o.avg_value, o.stdev_value
;

TRUNCATE TABLE #raw_1006;
DROP TABLE #raw_1006;