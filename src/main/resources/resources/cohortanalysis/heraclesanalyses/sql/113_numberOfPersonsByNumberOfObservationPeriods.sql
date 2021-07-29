-- 113   Number of persons by number of observation periods
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select cohort_definition_id,
  113 as analysis_id,
  op1.num_periods as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(op1.PERSON_ID) as count_value
into #results_113
from
(select cohort_definition_id, person_id, COUNT_BIG(OBSERVATION_period_start_date) as num_periods
from @CDM_schema.observation_period op0
inner join #HERACLES_cohort_subject c1
on op0.person_id = c1.subject_id
group by cohort_definition_id, PERSON_ID) op1
group by cohort_definition_id, op1.num_periods
;