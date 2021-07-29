-- 1804                Number of persons by duration from cohort start to cohort end, in 30d increments
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  1804 as analysis_id,
  floor(DATEDIFF(dd, c1.cohort_start_date, c1.cohort_end_date)/30) as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct p1.person_id) as count_value
into #results_1804
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id, floor(DATEDIFF(dd, c1.cohort_start_date, c1.cohort_end_date)/30)
;