-- 1701                Number of records with cohort end date < cohort start date
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, count_value)
select c2.cohort_definition_id,
  1701 as analysis_id,
  cast( '' as varchar(1) ) as stratum_1, cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(c1.subject_ID) as count_value
into #results_1701
from
@results_schema.cohort c1
inner join #HERACLES_cohort_subject c2
on c1.subject_id = c2.subject_id
where c1.cohort_end_date < c1.cohort_start_date
group by c2.cohort_definition_id
;