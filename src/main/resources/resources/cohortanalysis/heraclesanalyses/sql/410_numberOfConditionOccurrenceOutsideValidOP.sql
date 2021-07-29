-- 410   Number of condition occurrence records outside valid observation period
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  410 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct co1.condition_occurrence_id) as count_value
into #results_410
from
@CDM_schema.condition_occurrence co1
inner join #HERACLES_cohort_subject c1
on co1.person_id = c1.subject_id
left join @CDM_schema.observation_period op1
on op1.person_id = co1.person_id
and co1.condition_start_date >= op1.observation_period_start_date
and co1.condition_start_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;