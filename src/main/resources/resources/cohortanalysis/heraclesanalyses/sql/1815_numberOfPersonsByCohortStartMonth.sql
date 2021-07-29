-- 1815                Number of persons by cohort start month
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  1815 as analysis_id,
  YEAR(c1.cohort_start_date)*100 + month(c1.cohort_start_date) as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct p1.person_id) as count_value
into #results_1815
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id,
YEAR(c1.cohort_start_date)*100 + month(c1.cohort_start_date)
;