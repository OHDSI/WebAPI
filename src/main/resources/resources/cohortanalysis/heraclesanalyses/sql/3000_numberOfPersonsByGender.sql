-- 3000     Number of persons by gender
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c.cohort_definition_id,
  3000 as analysis_id,
  p.gender_concept_id as stratum_1,
  cast((cast(year(c.cohort_start_date) - p.year_of_birth as int)) / 10 as int) * 10 as stratum_2,
  cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  count_big(distinct p.person_id) as count_value
into #results_3000
from #HERACLES_cohort c
join @CDM_schema.person p
on p.person_id = c.subject_id
group by c.cohort_definition_id, p.gender_concept_id, cast((cast(year(c.cohort_start_date) - p.year_of_birth as int)) / 10 as int) * 10
;
