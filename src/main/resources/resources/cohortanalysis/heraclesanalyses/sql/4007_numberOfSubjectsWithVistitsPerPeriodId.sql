-- 4007 Number of subjects with visits by period_id, by visit_concept_id, by visit_type_concept_id during the cohort period
{@rollupUtilizationVisit} ? {
with visit_records (cohort_definition_id, subject_id, visit_concept_id, visit_type_concept_id, visit_start_date, ancestor) as
(
    select distinct c1.cohort_definition_id,
      c1.subject_id,
      vca.ancestor_concept_id as visit_concept_id,
      vo1.visit_type_concept_id,
      vo1.visit_start_date,
      case when vca.ancestor_concept_id = vca.descendant_concept_id then 0 else 1 end as ancestor
    from #HERACLES_cohort c1
          join @CDM_schema.visit_occurrence as vo1 on c1.subject_id = vo1.person_id
                                                      and vo1.visit_start_date >= c1.cohort_start_date and vo1.visit_start_date <= c1.cohort_end_date
                                                                                                                                   join @CDM_schema.concept_ancestor vca on vca.descendant_concept_id = vo1.visit_concept_id
                                                                                                                                                                            where vo1.visit_type_concept_id in (@includeVisitTypeUtilization)
)
select cohort_definition_id, subject_id, visit_concept_id, visit_type_concept_id, visit_start_date, ancestor
INTO #raw_4007
FROM visit_records;
} : {
with visit_records (cohort_definition_id, subject_id, visit_concept_id, visit_type_concept_id, visit_start_date) as
(
    select distinct c1.cohort_definition_id,
      c1.subject_id,
      vo1.visit_concept_id,
      vo1.visit_type_concept_id,
      vo1.visit_start_date
    from #HERACLES_cohort c1
          join @CDM_schema.visit_occurrence as vo1 on c1.subject_id = vo1.person_id
                                                      and vo1.visit_start_date >= c1.cohort_start_date and vo1.visit_start_date <= c1.cohort_end_date
                                                                                                                                   where vo1.visit_type_concept_id in (@includeVisitTypeUtilization)
)
select cohort_definition_id, subject_id, visit_concept_id, visit_type_concept_id, visit_start_date
INTO #raw_4007
FROM visit_records;
}

with cteRawData(cohort_definition_id, stratum_1, stratum_2, stratum_3, subject_id) as
(
  -- period_id, visit_concept_id, visit_type_concept_id
    select distinct cohort_definition_id
      , period_id as stratum_1
      , visit_concept_id as stratum_2
      , visit_type_concept_id as stratum_3
      , subject_id
    from #raw_4007
          join #periods_atrisk hp on visit_start_date >= hp.period_start_date and visit_start_date < hp.period_end_date

                                                            UNION ALL
                                                            -- period_id, visit_concept_id
                                                            select distinct cohort_definition_id
    , period_id as stratum_1
    , visit_concept_id as stratum_2
    , 0 as stratum_3
    , subject_id
      from #raw_4007
      join #periods_atrisk hp on visit_start_date >= hp.period_start_date and visit_start_date < hp.period_end_date

                                                                              UNION ALL

                                                                              -- period_id
                                                                              select distinct cohort_definition_id
    , period_id as stratum_1
    , 0 as stratum_2
    , 0 as stratum_3
    , subject_id
        from #raw_4007
        join #periods_atrisk hp on visit_start_date >= hp.period_start_date and visit_start_date < hp.period_end_date
                                                                                                   {@rollupUtilizationVisit} ? {
                                                                                                   where ancestor = 0
                                                                                                         }

                                                                                                         UNION ALL

                                                                                                         -- visit_concept_id, visit_type_concept_id
                                                                                                         select distinct cohort_definition_id
    , 0 as stratum_1
    , visit_concept_id as stratum_2
    , visit_type_concept_id as stratum_3
    , subject_id
                               from #raw_4007

                               UNION ALL
                               -- period_id, visit_concept_id
                               select distinct cohort_definition_id
    , 0 as stratum_1
    , visit_concept_id as stratum_2
    , 0 as stratum_3
    , subject_id
      from #raw_4007

      UNION ALL
    --
    select distinct cohort_definition_id
    , 0 as stratum_1
    , 0 as stratum_2
    , 0 as stratum_3
    , subject_id
           from #raw_4007
    {@rollupUtilizationVisit} ? {
    where ancestor = 0
    }
)
select cohort_definition_id, 4007 as analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value
INTO #results_4007
from
(
select cohort_definition_id
, case when stratum_1 = 0 then cast('' as varchar(255)) else cast(stratum_1 as varchar(255)) end as stratum_1
, case when stratum_2 = 0 then cast('' as varchar(255)) else cast(stratum_2 as varchar(255)) end as stratum_2
, case when stratum_3 = 0 then cast('' as varchar(255)) else cast(stratum_3 as varchar(255)) end as stratum_3
, cast('' as varchar(255)) as stratum_4, cast('' as varchar(255)) as stratum_5
, COUNT_BIG(subject_id) as count_value
from cteRawData
group by cohort_definition_id, stratum_1, stratum_2, stratum_3
) D;

TRUNCATE TABLE #raw_4007;
DROP TABLE #raw_4007;