-- 1009                Number of condition eras outside valid observation period
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  1009 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct ce1.condition_era_id) as count_value
into #results_1009
from
@CDM_schema.condition_era ce1
inner join #HERACLES_cohort_subject c1
on ce1.person_id = c1.subject_id
left join @CDM_schema.observation_period op1
on op1.person_id = ce1.person_id
and ce1.condition_era_start_date >= op1.observation_period_start_date
and ce1.condition_era_start_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;