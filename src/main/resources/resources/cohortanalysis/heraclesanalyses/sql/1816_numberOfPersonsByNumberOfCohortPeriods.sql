-- 1816                Number of persons by number of cohort periods
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select cohort_definition_id,
  1816 as analysis_id,
  num_periods as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct person_id) as count_value
into #results_1816
from
(select c1.cohort_definition_id, p1.person_id, COUNT_BIG(c1.cohort_start_date) as num_periods
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id, p1.person_id) nc1
group by cohort_definition_id, num_periods
;