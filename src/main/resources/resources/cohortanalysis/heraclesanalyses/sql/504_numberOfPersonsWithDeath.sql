-- 504   Number of persons with a death, by calendar year by gender by age decile
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, count_value)
select c1.cohort_definition_id,
  504 as analysis_id,
  YEAR(death_date) as stratum_1,
  p1.gender_concept_id as stratum_2,
  floor((year(death_date) - p1.year_of_birth)/10) as stratum_3,
  cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct p1.PERSON_ID) as count_value
into #results_504
from @CDM_schema.person p1
inner join
@CDM_schema.death d1
on p1.person_id = d1.person_id
inner join #HERACLES_cohort c1
on d1.person_id = c1.subject_id
--{@cohort_period_only == 'true'}?{
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
group by c1.cohort_definition_id,
YEAR(death_date),
p1.gender_concept_id,
floor((year(death_date) - p1.year_of_birth)/10)
;