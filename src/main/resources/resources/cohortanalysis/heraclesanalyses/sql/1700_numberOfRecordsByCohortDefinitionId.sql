-- 1700              Number of records by cohort_definition_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c2.cohort_definition_id,
  1700 as analysis_id,
  c1.cohort_definition_id as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(c1.subject_ID) as count_value
into #results_1700
from
@results_schema.cohort c1
inner join #HERACLES_cohort_subject c2
on c1.subject_id = c2.subject_id
group by c2.cohort_definition_id,
c1.cohort_definition_id
;