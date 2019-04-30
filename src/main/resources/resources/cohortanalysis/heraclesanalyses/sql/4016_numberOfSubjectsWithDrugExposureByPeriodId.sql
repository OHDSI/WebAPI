-- 4016 Number of subjects with Drug Exposure by period_id, by drug_concept_id, by drug_type_concept_id during the cohort period
{@rollupUtilizationDrug} ? {
with drug_records as
(
    select distinct c1.cohort_definition_id,
      c1.subject_id,
      de1.drug_concept_id,
      de1.drug_type_concept_id,
      de1.drug_exposure_start_date AS drug_exposure_date
    from  #HERACLES_cohort c1
           join @CDM_schema.drug_exposure as de1 on c1.subject_id = de1.person_id
                                                    and de1.drug_exposure_start_date >= c1.cohort_start_date and de1.drug_exposure_start_date <= c1.cohort_end_date
                                                                                                                                                 WHERE de1.drug_concept_id != 0
                                                                                                                                                 and drug_type_concept_id in (@includeDrugTypeUtilization)
),
    atc_rollup as
  (
      select r.cohort_definition_id, r.subject_id, ca.ancestor_concept_id as drug_concept_id, r.drug_type_concept_id, r.drug_exposure_date
      from drug_records r
        join @CDM_schema.concept_ancestor ca on ca.descendant_concept_id = r.drug_concept_id
        join @CDM_schema.concept c on ca.ancestor_concept_id = c.concept_id
      where c.vocabulary_id = 'ATC' and c.concept_class_id in ('ATC 4th', 'ATC 5th', 'ATC 3rd', 'ATC 2nd', 'ATC 1st')
  ),
    rxnorm_rollup as
  (
      select r.cohort_definition_id, r.subject_id, ca.ancestor_concept_id as drug_concept_id, r.drug_type_concept_id, r.drug_exposure_date
      from drug_records r
        join @CDM_schema.concept_ancestor ca on ca.descendant_concept_id = r.drug_concept_id
        join @CDM_schema.concept c on ca.ancestor_concept_id = c.concept_id
      where c.vocabulary_id = 'RxNorm' and c.concept_class_id in ('Ingredient', 'Branded Drug Comp', 'Clinical Drug Comp') and ca.ancestor_concept_id != ca.descendant_concept_id
  )
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, drug_exposure_date
INTO #raw_de_4016
FROM (
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, drug_exposure_date FROM drug_records
UNION ALL
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, drug_exposure_date FROM atc_rollup
UNION ALL
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, drug_exposure_date FROM rxnorm_rollup
) D
;
} : {
with drug_records as
(
    select distinct c1.cohort_definition_id,
      c1.subject_id,
      de1.drug_concept_id,
      de1.drug_type_concept_id,
      de1.drug_exposure_start_date AS drug_exposure_date
    from  #HERACLES_cohort c1
           join @CDM_schema.drug_exposure as de1 on c1.subject_id = de1.person_id
                                                    and de1.drug_exposure_start_date >= c1.cohort_start_date and de1.drug_exposure_start_date <= c1.cohort_end_date
                                                                                                                                                 WHERE de1.drug_concept_id != 0
                                                                                                                                                 and drug_type_concept_id in (@includeDrugTypeUtilization)
)
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, drug_exposure_date
INTO #raw_de_4016
FROM (
select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, drug_exposure_date FROM drug_records
) D
;
}

CREATE INDEX idx_raw_4016_de_date ON #raw_de_4016 (drug_exposure_date);

select cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id, period_id
into #raw_de_p_4016
from #raw_de_4016 r
join #periods_atrisk hp on r.drug_exposure_date >= hp.period_start_date and r.drug_exposure_date < hp.period_end_date
;

-- period_id, drug_concept_id, drug_type_concept_id
select cohort_definition_id
  , period_id as stratum_1
  , drug_concept_id as stratum_2
  , drug_type_concept_id as stratum_3
  , subject_id
into #raw_4016_u1
from #raw_de_p_4016
GROUP BY cohort_definition_id, subject_id, period_id, drug_concept_id, drug_type_concept_id;
-- period_id, drug_concept_id
select cohort_definition_id
  , period_id as stratum_1
  , drug_concept_id as stratum_2
  , 0 as stratum_3
  , subject_id
into #raw_4016_u2
from #raw_de_p_4016
GROUP BY cohort_definition_id, subject_id, period_id, drug_concept_id;
-- drug_concept_id, drug_type_concept_id
select cohort_definition_id
  , 0 as stratum_1
  , drug_concept_id as stratum_2
  , drug_type_concept_id as stratum_3
  , subject_id
into #raw_4016_u3
from #raw_de_4016
GROUP BY cohort_definition_id, subject_id, drug_concept_id, drug_type_concept_id;
-- drug_concept_id
select cohort_definition_id
  , 0 as stratum_1
  , drug_concept_id as stratum_2
  , 0 as stratum_3
  , subject_id
into #raw_4016_u4
from #raw_de_4016
GROUP BY cohort_definition_id, subject_id, drug_concept_id;

with cteRawData(cohort_definition_id, stratum_1, stratum_2, stratum_3, subject_id) as
(

    select cohort_definition_id, stratum_1, stratum_2, stratum_3, subject_id
    from #raw_4016_u1
          UNION ALL

          select cohort_definition_id, stratum_1, stratum_2, stratum_3, subject_id
                                                             from #raw_4016_u2
                                                             UNION ALL

                                                             select cohort_definition_id, stratum_1, stratum_2, stratum_3, subject_id
                                                                                          from #raw_4016_u3
                                                                                                UNION ALL

                                                                                                select cohort_definition_id, stratum_1, stratum_2, stratum_3, subject_id
    from #raw_4016_u4

)
select cohort_definition_id, 4016 as analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value
INTO #results_4016
from
(
select cohort_definition_id
, case when stratum_1 = 0 then cast('' as varchar(255)) else cast(stratum_1 as varchar(255)) end as stratum_1
, case when stratum_2 = 0 then cast('' as varchar(255)) else cast(stratum_2 as varchar(255)) end as stratum_2
, case when stratum_3 = 0 then cast('' as varchar(255)) else cast(stratum_3 as varchar(255)) end as stratum_3
, cast('' as char(1)) as stratum_4, cast('' as char(1)) as stratum_5
, COUNT_BIG(subject_id) as count_value
from cteRawData
group by cohort_definition_id, stratum_1, stratum_2, stratum_3
) D;

TRUNCATE TABLE #raw_4016_u1;
DROP TABLE #raw_4016_u1;

TRUNCATE TABLE #raw_4016_u2;
DROP TABLE #raw_4016_u2;

TRUNCATE TABLE #raw_4016_u3;
DROP TABLE #raw_4016_u3;

TRUNCATE TABLE #raw_4016_u4;
DROP TABLE #raw_4016_u4;

TRUNCATE TABLE #raw_de_4016;
DROP TABLE #raw_de_4016;

TRUNCATE TABLE #raw_de_p_4016;
DROP TABLE #raw_de_p_4016;