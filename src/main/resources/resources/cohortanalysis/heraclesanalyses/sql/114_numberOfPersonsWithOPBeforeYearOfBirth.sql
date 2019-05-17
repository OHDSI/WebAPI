-- 114   Number of persons with observation period before year-of-birth
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select cohort_definition_id,
  114 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(p1.PERSON_ID) as count_value
into #results_114
from
@CDM_schema.person p1
inner join (select cohort_definition_id, person_id, MIN(year(OBSERVATION_period_start_date)) as first_obs_year
from @CDM_schema.observation_period op0
inner join #HERACLES_cohort_subject c1
on op0.person_id = c1.subject_id
group by cohort_definition_id, PERSON_ID) op1
on p1.person_id = op1.person_id
where p1.year_of_birth > op1.first_obs_year
group by cohort_definition_id
;