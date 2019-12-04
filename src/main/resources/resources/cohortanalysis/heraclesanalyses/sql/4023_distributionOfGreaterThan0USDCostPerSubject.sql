-- 4023	Distribution of greater than 0 US$ cost per subject by period_id, by drug_concept_id, by drug_type_concept_id, by cost_concept_id, by cost_type_concept_id, by cost_type_concept_id during the cohort period
{@rollupUtilizationDrug} ? {
with drug_records(cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, drug_exposure_start_date, drug_exposure_id, cost) as
(
    select c1.cohort_definition_id,
      c1.subject_id,
      de1.drug_concept_id,
      de1.drug_type_concept_id,
      cst1.cost_concept_id,
      cst1.cost_type_concept_id,
      de1.drug_exposure_start_date,
      de1.drug_exposure_id,
      cost
    from #HERACLES_cohort c1
          join @CDM_schema.drug_exposure de1 on c1.subject_id = de1.person_id
                                                  and de1.drug_exposure_start_date >= c1.cohort_start_date and de1.drug_exposure_start_date < c1.cohort_end_date
                                                                                                                                                join @CDM_schema.cost cst1 on c1.subject_id = cst1.person_id
                                                                                                                                                                                and cst1.person_id = de1.person_id
                                                                                                                                                                                                   and de1.visit_occurrence_id = cst1.cost_event_id
                                                                                                                                                                                                                                     where cost >= 0
                                                                                                                                                                                                                                     and currency_concept_id in (@includeCurrency)
    and cost_concept_id in (@includeCostConcepts)
    and drug_type_concept_id in (@includeDrugTypeUtilization)
),
    atc_rollup (cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, drug_exposure_start_date, drug_exposure_id, cost) as
  (
      select r.cohort_definition_id, r.subject_id, ca.ancestor_concept_id as drug_concept_id, r.drug_type_concept_id, r.cost_concept_id, r.cost_type_concept_id, r.drug_exposure_start_date, r.drug_exposure_id, r.cost
      from drug_records r
        join @CDM_schema.concept_ancestor ca on ca.descendant_concept_id = r.drug_concept_id
        join @CDM_schema.concept c on ca.ancestor_concept_id = c.concept_id
      where c.vocabulary_id = 'ATC' and c.concept_class_id in ('ATC 4th', 'ATC 5th', 'ATC 3rd', 'ATC 2nd', 'ATC 1st')
  ),
    rxnorm_rollup (cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, drug_exposure_start_date, drug_exposure_id, cost) as
  (
      select r.cohort_definition_id, r.subject_id, ca.ancestor_concept_id as drug_concept_id, r.drug_type_concept_id, r.cost_concept_id, r.cost_type_concept_id, r.drug_exposure_start_date, r.drug_exposure_id, r.cost
      from drug_records r
        join @CDM_schema.concept_ancestor ca on ca.descendant_concept_id = r.drug_concept_id
        join @CDM_schema.concept c on ca.ancestor_concept_id = c.concept_id
      where c.vocabulary_id = 'RxNorm' and c.concept_class_id in ('Ingredient', 'Branded Drug Comp', 'Clinical Drug Comp') and ca.ancestor_concept_id != ca.descendant_concept_id
  )
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, drug_exposure_start_date, drug_exposure_id, cost, ancestor
INTO #raw_cost_4023
FROM (
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, drug_exposure_start_date, drug_exposure_id, cost, 0 as ancestor FROM drug_records
UNION ALL
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, drug_exposure_start_date, drug_exposure_id, cost, 1 as ancestor FROM atc_rollup
UNION ALL
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, drug_exposure_start_date, drug_exposure_id, cost, 1 as ancestor FROM rxnorm_rollup
) D
;
create index ix_rc_visit_date on #raw_cost_4023 (drug_exposure_start_date);

select cohort_definition_id, subject_id, hp.period_id, drug_exposure_id,drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, cost, ancestor
into #raw_period_4023
from #raw_cost_4023
join #periods_atrisk hp on drug_exposure_start_date >= hp.period_start_date and drug_exposure_start_date < hp.period_end_date
;
} : {
with drug_records(cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, drug_exposure_start_date, drug_exposure_id, cost) as
(
    select c1.cohort_definition_id,
      c1.subject_id,
      de1.drug_concept_id,
      de1.drug_type_concept_id,
      cst1.cost_concept_id,
      cst1.cost_type_concept_id,
      de1.drug_exposure_start_date,
      de1.drug_exposure_id,
      cost
    from #HERACLES_cohort c1
          join @CDM_schema.drug_exposure de1 on c1.subject_id = de1.person_id
                                                  and de1.drug_exposure_start_date >= c1.cohort_start_date and de1.drug_exposure_start_date < c1.cohort_end_date
                                                                                                                                                join @CDM_schema.cost cst1 on c1.subject_id = cst1.person_id
                                                                                                                                                                                and cst1.person_id = de1.person_id
                                                                                                                                                                                                   and de1.visit_occurrence_id = cst1.cost_event_id
                                                                                                                                                                                                                                     where cost >= 0
                                                                                                                                                                                                                                     and currency_concept_id in (@includeCurrency)
    and cost_concept_id in (@includeCostConcepts)
    and drug_type_concept_id in (@includeDrugTypeUtilization)
)
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, drug_exposure_start_date, drug_exposure_id, cost
INTO #raw_cost_4023
FROM (
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, drug_exposure_start_date, drug_exposure_id, cost FROM drug_records
) D
;
create index ix_rc_visit_date on #raw_cost_4023 (drug_exposure_start_date);

select cohort_definition_id, subject_id, hp.period_id, drug_exposure_id,drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, cost
into #raw_period_4023
from #raw_cost_4023
join #periods_atrisk hp on drug_exposure_start_date >= hp.period_start_date and drug_exposure_start_date < hp.period_end_date
;
}



select cohort_definition_id
  , subject_id
  , period_id as stratum_1
  , drug_concept_id as stratum_2
  , drug_type_concept_id as stratum_3
  , cost_concept_id as stratum_4
  , cost_type_concept_id as stratum_5
  , sum(cost) as count_value
into #raw_4023_u1
from #raw_period_4023
GROUP BY subject_id, period_id, drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, cohort_definition_id;

select cohort_definition_id
  , subject_id
  , period_id as stratum_1
  , drug_concept_id as stratum_2
  , 0 as stratum_3
  , cost_concept_id as stratum_4
  , cost_type_concept_id as stratum_5
  , sum(cost) as count_value
into #raw_4023_u2
from #raw_period_4023
GROUP BY subject_id, period_id, drug_concept_id, cost_concept_id, cost_type_concept_id, cohort_definition_id;

select cohort_definition_id
  , subject_id
  , period_id as stratum_1
  , 0 as stratum_2
  , 0 as stratum_3
  , cost_concept_id as stratum_4
  , cost_type_concept_id as stratum_5
  , sum(cost) as count_value
into #raw_4023_u3
from #raw_period_4023
{@rollupUtilizationDrug} ? {
where ancestor = 0
}
GROUP BY subject_id, period_id, cost_concept_id, cost_type_concept_id, cohort_definition_id;

select cohort_definition_id
  , subject_id
  , 0 as stratum_1
  , drug_concept_id as stratum_2
  , drug_type_concept_id as stratum_3
  , cost_concept_id as stratum_4
  , cost_type_concept_id as stratum_5
  , sum(cost) as count_value
into #raw_4023_u4
from #raw_cost_4023
GROUP BY subject_id, drug_concept_id, drug_type_concept_id, cost_concept_id, cost_type_concept_id, cohort_definition_id;

select cohort_definition_id
  , subject_id
  , 0 as stratum_1
  , drug_concept_id as stratum_2
  , 0 as stratum_3
  , cost_concept_id as stratum_4
  , cost_type_concept_id as stratum_5
  , sum(cost) as count_value
into #raw_4023_u5
from #raw_cost_4023
GROUP BY subject_id, drug_concept_id, cost_concept_id, cost_type_concept_id, cohort_definition_id;

select cohort_definition_id
  , subject_id
  , 0 as stratum_1
  , 0 as stratum_2
  , 0 as stratum_3
  , cost_concept_id as stratum_4
  , cost_type_concept_id as stratum_5
  , sum(cost) as count_value
into #raw_4023_u6
from #raw_cost_4023
{@rollupUtilizationDrug} ? {
where ancestor = 0
}
GROUP BY subject_id, cost_concept_id, cost_type_concept_id, cohort_definition_id;

WITH cteRawData (cohort_definition_id, subject_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value) as
(
    select cohort_definition_id, subject_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value
    FROM #raw_4023_u1
          UNION ALL

          select cohort_definition_id, subject_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value
                                                              FROM #raw_4023_u2
                                                                   UNION ALL

                                                                   select cohort_definition_id, subject_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value
    FROM #raw_4023_u3
    UNION ALL

    select cohort_definition_id, subject_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value
                                                                                                    FROM #raw_4023_u4
                                                                                                    UNION ALL

                                                                                                    select cohort_definition_id, subject_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value
                                                                                                                                                                                                    FROM #raw_4023_u5
                                                                                                                                                                                                    UNION ALL

                                                                                                                                                                                                    select cohort_definition_id, subject_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value
    FROM #raw_4023_u6
),
    overallStats (cohort_definition_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, avg_value, stdev_value, min_value, max_value, total) as
  (
      select cohort_definition_id,
        stratum_1,
        stratum_2,
        stratum_3,
        stratum_4,
        stratum_5,
        avg(1.0 * count_value) as avg_value,
        stdev(count_value) as stdev_value,
        min(count_value) as min_value,
        max(count_value) as max_value,
        count_big(*) as total
      from cteRawData
      group by cohort_definition_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5
  ),
    valueStats (cohort_definition_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value, total, accumulated) as
  (
      select cohort_definition_id,
        stratum_1,
        stratum_2,
        stratum_3,
        stratum_4,
        stratum_5,
        count_value,
        total,
        SUM(total) over (partition by cohort_definition_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5 order by count_value ROWS UNBOUNDED PRECEDING) as accumulated
      FROM (
             select cohort_definition_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value, count_big(*) as total
             FROM cteRawData
             GROUP BY cohort_definition_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value
           ) D
  )
select o.cohort_definition_id,
  4023 as analysis_id,
  case when o.stratum_1 = 0 then cast('' as varchar(255)) else cast(o.stratum_1 as varchar(255)) end as stratum_1,
  case when o.stratum_2 = 0 then cast('' as varchar(255)) else cast(o.stratum_2 as varchar(255)) end as stratum_2,
  case when o.stratum_3 = 0 then cast('' as varchar(255)) else cast(o.stratum_3 as varchar(255)) end as stratum_3,
  case when o.stratum_4 = 0 then cast('' as varchar(255)) else cast(o.stratum_4 as varchar(255)) end as stratum_4,
  case when o.stratum_5 = 0 then cast('' as varchar(255)) else cast(o.stratum_5 as varchar(255)) end as stratum_5,
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
into #results_dist_4023
from valueStats s
join overallStats o on s.cohort_definition_id = o.cohort_definition_id
and s.stratum_1 = o.stratum_1 and s.stratum_2 = o.stratum_2 and s.stratum_3 = o.stratum_3 and s.stratum_4 = o.stratum_4 and s.stratum_5 = o.stratum_5
GROUP BY o.cohort_definition_id, o.stratum_1, o.stratum_2, o.stratum_3, o.stratum_4, o.stratum_5, o.total, o.min_value, o.max_value, o.avg_value, o.stdev_value
;


TRUNCATE TABLE #raw_4023_u1;
DROP TABLE #raw_4023_u1;

TRUNCATE TABLE #raw_4023_u2;
DROP TABLE #raw_4023_u2;

TRUNCATE TABLE #raw_4023_u3;
DROP TABLE #raw_4023_u3;

TRUNCATE TABLE #raw_4023_u4;
DROP TABLE #raw_4023_u4;

TRUNCATE TABLE #raw_4023_u5;
DROP TABLE #raw_4023_u5;

TRUNCATE TABLE #raw_4023_u6;
DROP TABLE #raw_4023_u6;

TRUNCATE TABLE #raw_cost_4023;
DROP TABLE #raw_cost_4023;

TRUNCATE TABLE #raw_period_4023;
DROP TABLE #raw_period_4023;