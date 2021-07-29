-- 1309                Number of measurement records with invalid person_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c1.cohort_definition_id,
  1309 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(o1.measurement_id) as count_value
into #results_1309
from
@CDM_schema.measurement o1
inner join #HERACLES_cohort_subject c1
on o1.person_id = c1.subject_id
left join @CDM_schema.person p1
on p1.person_id = o1.person_id
where p1.person_id is null
group by c1.cohort_definition_id
;