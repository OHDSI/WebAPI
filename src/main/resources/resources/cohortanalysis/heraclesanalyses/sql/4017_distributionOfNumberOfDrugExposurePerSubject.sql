-- 4017 Distribution of number of Drug Exposure records per subject, by period_id, by drug_concept_id during the cohort period
{@rollupUtilizationDrug} ? {
with drug_records as
(
    select distinct c1.cohort_definition_id,
      c1.subject_id,
      de1.drug_concept_id,
      de1.drug_type_concept_id,
      de1.drug_exposure_start_date AS drug_exposure_date,
      de1.drug_exposure_id
    from  #HERACLES_cohort c1
           join @CDM_schema.drug_exposure as de1 on c1.subject_id = de1.person_id
                                                    and de1.drug_exposure_start_date >= c1.cohort_start_date and de1.drug_exposure_start_date <= c1.cohort_end_date
                                                                                                                                                 WHERE de1.drug_concept_id != 0
                                                                                                                                                 and drug_type_concept_id in (@includeDrugTypeUtilization)
),
    atc_rollup as
  (
      select r.cohort_definition_id, subject_id, ca.ancestor_concept_id as drug_concept_id, r.drug_type_concept_id, r.drug_exposure_date, r.drug_exposure_id
      from drug_records r
        join @CDM_schema.concept_ancestor ca on ca.descendant_concept_id = r.drug_concept_id
        join @CDM_schema.concept c on ca.ancestor_concept_id = c.concept_id
      where c.vocabulary_id = 'ATC' and c.concept_class_id in ('ATC 4th', 'ATC 5th', 'ATC 3rd', 'ATC 2nd', 'ATC 1st')
  ),
    rxnorm_rollup as
  (
      select r.cohort_definition_id, subject_id, ca.ancestor_concept_id as drug_concept_id, r.drug_type_concept_id, r.drug_exposure_date, r.drug_exposure_id
      from drug_records r
        join @CDM_schema.concept_ancestor ca on ca.descendant_concept_id = r.drug_concept_id
        join @CDM_schema.concept c on ca.ancestor_concept_id = c.concept_id
      where c.vocabulary_id = 'RxNorm' and c.concept_class_id in ('Ingredient', 'Branded Drug Comp', 'Clinical Drug Comp') and ca.ancestor_concept_id != ca.descendant_concept_id
  )
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, drug_exposure_date, drug_exposure_id
INTO #raw_de_4017
FROM (
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, drug_exposure_date, drug_exposure_id FROM drug_records
UNION ALL
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, drug_exposure_date, drug_exposure_id FROM atc_rollup
UNION ALL
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, drug_exposure_date, drug_exposure_id FROM rxnorm_rollup
) D
;
} : {
with drug_records as
(
    select distinct c1.cohort_definition_id,
      c1.subject_id,
      de1.drug_concept_id,
      de1.drug_type_concept_id,
      de1.drug_exposure_start_date AS drug_exposure_date,
      de1.drug_exposure_id
    from  #HERACLES_cohort c1
           join @CDM_schema.drug_exposure as de1 on c1.subject_id = de1.person_id
                                                    and de1.drug_exposure_start_date >= c1.cohort_start_date and de1.drug_exposure_start_date <= c1.cohort_end_date
                                                                                                                                                 WHERE de1.drug_concept_id != 0
                                                                                                                                                 and drug_type_concept_id in (@includeDrugTypeUtilization)
)
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, drug_exposure_date, drug_exposure_id
INTO #raw_de_4017
FROM (
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, drug_exposure_date, drug_exposure_id FROM drug_records
) D
;
}
CREATE INDEX idx_raw_4017_de_date ON #raw_de_4017 (drug_exposure_date);

select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, period_id, drug_exposure_id
into #raw_de_p_4017
from #raw_de_4017 r
join #periods_atrisk hp on r.drug_exposure_date >= hp.period_start_date and r.drug_exposure_date < hp.period_end_date
;

-- period_id, drug_concept_id, drug_type_concept_id
select cohort_definition_id
  , period_id as stratum_1
  , drug_concept_id as stratum_2
  , drug_type_concept_id as stratum_3
  , subject_id
  , count(distinct drug_exposure_id) as count_value
into #raw_4017_u1
from #raw_de_p_4017
GROUP BY cohort_definition_id, subject_id, period_id, drug_concept_id, drug_type_concept_id;
-- period_id, drug_concept_id
select cohort_definition_id
  , period_id as stratum_1
  , drug_concept_id as stratum_2
  , 0 as stratum_3
  , subject_id
  , count(distinct drug_exposure_id) as count_value
into #raw_4017_u2
from #raw_de_p_4017
GROUP BY cohort_definition_id, subject_id, period_id, drug_concept_id;
-- drug_concept_id, drug_type_concept_id
select cohort_definition_id
  , 0 as stratum_1
  , drug_concept_id as stratum_2
  , drug_type_concept_id as stratum_3
  , subject_id
  , count(distinct drug_exposure_id) as count_value
into #raw_4017_u3
from #raw_de_4017
GROUP BY cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id;
-- drug_concept_id
select cohort_definition_id
  , 0 as stratum_1
  , drug_concept_id as stratum_2
  , 0 as stratum_3
  , subject_id
  , count(distinct drug_exposure_id) as count_value
into #raw_4017_u4
from #raw_de_4017
GROUP BY cohort_definition_id, subject_id, drug_concept_id;

with cteRawData(cohort_definition_id, stratum_1, stratum_2, stratum_3, subject_id, count_value) as
(

    select cohort_definition_id, stratum_1, stratum_2, stratum_3, subject_id, count_value
    from #raw_4017_u1
          UNION ALL

          select cohort_definition_id, stratum_1, stratum_2, stratum_3, subject_id, count_value
                                                             from #raw_4017_u2
                                                             UNION ALL

    select cohort_definition_id, stratum_1, stratum_2, stratum_3, subject_id, count_value
                                                                              from #raw_4017_u3
                                                                              UNION ALL

                                                                              select cohort_definition_id, stratum_1, stratum_2, stratum_3, subject_id, count_value
                                                                                                           from #raw_4017_u4

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
  4017 as analysis_id,
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
into #results_dist_4017
from valueStats s
join overallStats o on s.cohort_definition_id = o.cohort_definition_id and s.stratum_1 = o.stratum_1 and s.stratum_2 = o.stratum_2 and s.stratum_3 = o.stratum_3
GROUP BY o.cohort_definition_id, o.stratum_1, o.stratum_2, o.stratum_3, o.total, o.min_value, o.max_value, o.avg_value, o.stdev_value
;

TRUNCATE TABLE #raw_4017_u1;
DROP TABLE #raw_4017_u1;

TRUNCATE TABLE #raw_4017_u2;
DROP TABLE #raw_4017_u2;

TRUNCATE TABLE #raw_4017_u3;
DROP TABLE #raw_4017_u3;

TRUNCATE TABLE #raw_4017_u4;
DROP TABLE #raw_4017_u4;

TRUNCATE TABLE #raw_de_4017;
DROP TABLE #raw_de_4017;

TRUNCATE TABLE #raw_de_p_4017;
DROP TABLE #raw_de_p_4017;