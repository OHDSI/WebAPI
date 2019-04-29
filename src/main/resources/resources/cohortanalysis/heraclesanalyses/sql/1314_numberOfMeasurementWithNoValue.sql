-- 1314                Number of measurement records with no value (numeric, string, or concept)
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  1314 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(o1.measurement_id) as count_value
into #results_1314
from
@CDM_schema.measurement o1
inner join #HERACLES_cohort_subject c1
on o1.person_id = c1.subject_id
where o1.value_as_number is null
and o1.value_as_concept_id is null
group by c1.cohort_definition_id
;