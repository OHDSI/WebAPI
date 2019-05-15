-- 814   Number of observation records with no value (numeric, string, or concept)
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  814 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(o1.observation_id) as count_value
into #results_814
from
@CDM_schema.observation o1
inner join #HERACLES_cohort_subject c1
on o1.person_id = c1.subject_id
where o1.value_as_number is null
and o1.value_as_string is null
and o1.value_as_concept_id is null
group by c1.cohort_definition_id
;