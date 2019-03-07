-- 111   Number of persons by observation period start month
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  111 as analysis_id,
  YEAR(observation_period_start_date)*100 + month(observation_period_START_DATE) as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct op1.PERSON_ID) as count_value
into #results_111
from @CDM_schema.person p1
inner join #HERACLES_cohort_subject c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.observation_period op1
on p1.person_id = op1.person_id
group by c1.cohort_definition_id, YEAR(observation_period_start_date)*100 + month(observation_period_START_DATE)
;