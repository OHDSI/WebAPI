-- 909   Number of drug eras outside valid observation period
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  909 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct de1.drug_era_id) as count_value
into #results_909
from
@CDM_schema.drug_era de1
inner join #HERACLES_cohort_subject c1
on de1.person_id = c1.subject_id
left join @CDM_schema.observation_period op1
on op1.person_id = de1.person_id
and de1.drug_era_start_date >= op1.observation_period_start_date
and de1.drug_era_start_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;