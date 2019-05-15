-- 4006 Distribution of observation period days per subject, by period_id during cohort period

WITH cteRawData (cohort_definition_id, subject_id, stratum_1, count_value) as
(
    select c1.cohort_definition_id,
      c1.subject_id,
      cast(hp.period_id as varchar(50)) as stratum_1,
      sum(datediff(dd,
                   case when hp.period_start_date < c1.cohort_start_date then c1.cohort_start_date else hp.period_start_date end,
                   case when hp.period_end_date > c1.cohort_end_date then c1.cohort_end_date else hp.period_end_date end)
      ) as count_value
    from #HERACLES_cohort c1
          join #periods_atrisk hp on not (hp.period_end_date <= c1.cohort_start_date or hp.period_start_date >  c1.cohort_end_date) -- find overlapping periods with the cohort's observation periods
                                                                                                                                    group by c1.cohort_definition_id, c1.subject_id, hp.period_id

                                                                                                                                                                                     union all

                                                                                                                                                                                     select c1.cohort_definition_id,
    c1.subject_id,
    cast('' as varchar(1)) as stratum_1,
                           sum(datediff(dd,c1.cohort_start_date,c1.cohort_end_date)) as count_value
                                                                                     from #HERACLES_cohort c1
                                                                                     group by c1.cohort_definition_id, c1.subject_id
),
    overallStats (cohort_definition_id, stratum_1, avg_value, stdev_value, min_value, max_value, total) as
  (
      select cohort_definition_id, stratum_1,
        avg(1.0 * count_value) as avg_value,
        stdev(count_value) as stdev_value,
        min(count_value) as min_value,
        max(count_value) as max_value,
        count_big(*) as total
      from cteRawData
      group by cohort_definition_id, stratum_1
  ),
    valueStats (cohort_definition_id, stratum_1, count_value, total, accumulated) as
  (
      select cohort_definition_id, stratum_1,
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
  4006 as analysis_id,
  o.stratum_1,
  cast( '' as varchar(1) ) as stratum_2,
  cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4, cast( '' as varchar(1) ) as stratum_5,
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
into #results_dist_4006
from valueStats s
join overallStats o on s.cohort_definition_id = o.cohort_definition_id and s.stratum_1 = o.stratum_1
GROUP BY o.cohort_definition_id, o.stratum_1, o.total, o.min_value, o.max_value, o.avg_value, o.stdev_value
;