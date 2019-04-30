-- 3001     Number of persons by gender
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4)
--@smallcellcount + 9 as count_value to prevent row removal during small cell count
select cohort_definition_id,analysis_id,
  cast(row_index as varchar) as stratum_1, person_id as stratum_2, age_group as stratum_3, gender_concept_id as stratum_4, (@smallcellcount + 9) as count_value
into #results_3001
from (
select c.cohort_definition_id,
3001 as analysis_id,
row_number() over (
partition by cast((cast(year(c.cohort_start_date) - p.year_of_birth as int)) / 10 as int) * 10, p.gender_concept_id
order by p.person_id
) as row_index,
p.person_id,
cast((cast(year(c.cohort_start_date) - p.year_of_birth as int)) / 10 as int) * 10 as age_group,
p.gender_concept_id
from #HERACLES_cohort c
join @CDM_schema.person p
on p.person_id = c.subject_id
) groupings
where row_index <= 5
;