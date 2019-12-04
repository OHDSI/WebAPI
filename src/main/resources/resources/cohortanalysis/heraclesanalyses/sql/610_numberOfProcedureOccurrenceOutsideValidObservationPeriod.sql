-- 610   Number of procedure occurrence records outside valid observation period
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  610 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct po1.procedure_occurrence_id) as count_value
into #results_610
from
@CDM_schema.procedure_occurrence po1
inner join #HERACLES_cohort_subject c1
on po1.person_id = c1.subject_id
left join @CDM_schema.observation_period op1
on op1.person_id = po1.person_id
and po1.procedure_date >= op1.observation_period_start_date
and po1.procedure_date <= op1.observation_period_end_date
where op1.person_id is null
group by c1.cohort_definition_id
;