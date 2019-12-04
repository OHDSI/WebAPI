-- 4011 Distribution of length of stay for inpatient visits per subject by period_id, by visit_concept_id, by visit_type_concept_id during cohort period
{@rollupUtilizationVisit} ? {
select distinct c1.cohort_definition_id,
  c1.subject_id,
  vc1.ancestor_concept_id as visit_concept_id,
  vo1.visit_type_concept_id,
  vo1.visit_start_date,
  datediff(dd,visit_start_date,visit_end_date) + 1 as duration
INTO #raw_4011
FROM #HERACLES_cohort c1
join @CDM_schema.visit_occurrence vo1 on c1.subject_id = vo1.person_id
and vo1.visit_start_date >= c1.cohort_start_date and vo1.visit_start_date <= c1.cohort_end_date
join (
select vc.ancestor_concept_id, vc.descendant_concept_id
from @CDM_schema.concept_ancestor as vc
inner join @CDM_schema.concept as co on vc.descendant_concept_id = co.concept_id
where lower(co.concept_name) like '%inpatient%'
) as vc1 on vo1.visit_concept_id = vc1.descendant_concept_id
where vo1.visit_type_concept_id in (@includeVisitTypeUtilization)
;
} : {
select distinct c1.cohort_definition_id,
  c1.subject_id,
  vo1.visit_concept_id,
  vo1.visit_type_concept_id,
  vo1.visit_start_date,
  datediff(dd,visit_start_date,visit_end_date) + 1 as duration
INTO #raw_4011
FROM #HERACLES_cohort c1
join @CDM_schema.visit_occurrence vo1 on c1.subject_id = vo1.person_id
and vo1.visit_start_date >= c1.cohort_start_date and vo1.visit_start_date <= c1.cohort_end_date
join (select concept_id from @CDM_schema.concept where lower(concept_name) like '%inpatient%') as co
on vo1.visit_concept_id = co.concept_id
where vo1.visit_type_concept_id in (@includeVisitTypeUtilization)
;
}
-- period_id, visit_concept_id, visit_type_concept_id
select cohort_definition_id
  , subject_id
  , period_id as stratum_1
  , visit_concept_id as stratum_2
  , visit_type_concept_id as stratum_3
  , sum(duration) as count_value
into #raw_4011_u1
from #raw_4011
join #periods_atrisk hp on visit_start_date >= hp.period_start_date and visit_start_date < hp.period_end_date
GROUP BY cohort_definition_id, subject_id, period_id, visit_concept_id, visit_type_concept_id
;
-- period_id, visit_concept_id
select cohort_definition_id
  , subject_id
  , period_id as stratum_1
  , visit_concept_id as stratum_2
  , 0 as stratum_3
  , sum(duration) as count_value
into #raw_4011_u2
from #raw_4011
join #periods_atrisk hp on visit_start_date >= hp.period_start_date and visit_start_date < hp.period_end_date
GROUP BY cohort_definition_id, subject_id, period_id, visit_concept_id
;

-- visit_concept_id, visit_type_concept_id
select cohort_definition_id
  , subject_id
  , 0 as stratum_1
  , visit_concept_id as stratum_2
  , visit_type_concept_id as stratum_3
  , sum(duration) as count_value
into #raw_4011_u4
from #raw_4011
GROUP BY cohort_definition_id, subject_id, visit_concept_id, visit_type_concept_id;
-- visit_concept_id
select cohort_definition_id
  , subject_id
  , 0 as stratum_1
  , visit_concept_id as stratum_2
  , 0 as stratum_3
  , sum(duration) as count_value
into #raw_4011_u5
from #raw_4011
GROUP BY cohort_definition_id, subject_id, visit_concept_id;


WITH cteRawData (cohort_definition_id, subject_id, stratum_1, stratum_2, stratum_3, count_value) as
(
    select cohort_definition_id, subject_id, stratum_1, stratum_2, stratum_3, count_value
    FROM #raw_4011_u1
          UNION ALL
          select cohort_definition_id, subject_id, stratum_1, stratum_2, stratum_3, count_value
                                                              FROM #raw_4011_u2
                                                              UNION ALL
    select cohort_definition_id, subject_id, stratum_1, stratum_2, stratum_3, count_value
                                                                              FROM #raw_4011_u4
                                                                              UNION ALL
                                                                              select cohort_definition_id, subject_id, stratum_1, stratum_2, stratum_3, count_value
                                                                                                           FROM #raw_4011_u5
),
    overallStats (cohort_definition_id, stratum_1, stratum_2, stratum_3, avg_value, stdev_value, min_value, max_value, total) as
  (
      select cohort_definition_id,
        stratum_1,
        stratum_2,
        stratum_3,
        avg(1.0 * count_value) as avg_value,
        stdev(count_value) as stdev_value,
        min(count_value) as min_value,
        max(count_value) as max_value,
        count_big(*) as total
      from cteRawData
      group by cohort_definition_id, stratum_1, stratum_2, stratum_3
  ),
    valueStats (cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, total, accumulated) as
  (
      select cohort_definition_id,
        stratum_1,
        stratum_2,
        stratum_3,
        count_value,
        total,
        SUM(total) over (partition by cohort_definition_id, stratum_1, stratum_2, stratum_3 order by count_value ROWS UNBOUNDED PRECEDING) as accumulated
      FROM (
             select cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value, count_big(*) as total
             FROM cteRawData
             GROUP BY cohort_definition_id, stratum_1, stratum_2, stratum_3, count_value
           ) D
  )
select o.cohort_definition_id,
  4011 as analysis_id,
  case when o.stratum_1 = 0 then cast('' as varchar(255)) else cast(o.stratum_1 as varchar(255)) end as stratum_1,
  case when o.stratum_2 = 0 then cast('' as varchar(255)) else cast(o.stratum_2 as varchar(255)) end as stratum_2,
  case when o.stratum_3 = 0 then cast('' as varchar(255)) else cast(o.stratum_3 as varchar(255)) end as stratum_3,
  cast( '' as varchar(255) ) as stratum_4, cast( '' as varchar(255) ) as stratum_5,
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
into #results_dist_4011
from valueStats s
join overallStats o on s.cohort_definition_id = o.cohort_definition_id and s.stratum_1 = o.stratum_1 and s.stratum_2 = o.stratum_2 and s.stratum_3 = o.stratum_3
GROUP BY o.cohort_definition_id, o.stratum_1, o.stratum_2, o.stratum_3, o.total, o.min_value, o.max_value, o.avg_value, o.stdev_value
;

TRUNCATE TABLE #raw_4011_u1;
DROP TABLE #raw_4011_u1;

TRUNCATE TABLE #raw_4011_u2;
DROP TABLE #raw_4011_u2;

TRUNCATE TABLE #raw_4011_u4;
DROP TABLE #raw_4011_u4;

TRUNCATE TABLE #raw_4011_u5;
DROP TABLE #raw_4011_u5;

TRUNCATE TABLE #raw_4011;
DROP TABLE #raw_4011;