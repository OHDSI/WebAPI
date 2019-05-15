-- 1851                Number of events by duration from cohort start to all occurrences of observation, by observation_concept_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
  1851 as analysis_id,
  o1.observation_concept_id as stratum_1,
  case when c1.cohort_start_date = o1.observation_date then 0
  when c1.cohort_start_date < o1.observation_date then floor(DATEDIFF(dd, c1.cohort_start_date, o1.observation_date)/30)+1
  when c1.cohort_start_date > o1.observation_date then floor(DATEDIFF(dd, c1.cohort_start_date, o1.observation_date)/30)-1
  end as stratum_2,
  cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct p1.person_id) as count_value
into #results_1851
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.observation o1
on p1.person_id = o1.person_id
--{@observation_concept_ids != ''}?{
where o1.observation_concept_id in (@observation_concept_ids)
--}
group by c1.cohort_definition_id,
o1.observation_concept_id,
case when c1.cohort_start_date = o1.observation_date then 0
when c1.cohort_start_date < o1.observation_date then floor(DATEDIFF(dd, c1.cohort_start_date, o1.observation_date)/30)+1
when c1.cohort_start_date > o1.observation_date then floor(DATEDIFF(dd, c1.cohort_start_date, o1.observation_date)/30)-1
end
;