-- 510   Number of death records outside valid observation period
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  510 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct d1.PERSON_ID) as count_value
into #results_510
from
@CDM_schema.death d1
inner join #HERACLES_cohort_subject c1
on d1.person_id = c1.subject_id
left join @CDM_schema.observation_period op1
on d1.person_id = op1.person_id
and d1.death_date >= op1.observation_period_start_date
and d1.death_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;