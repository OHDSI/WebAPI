-- 1814                Number of persons by cohort start year by gender by age decile
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, count_value)
select c1.cohort_definition_id,
  1814 as analysis_id,
  YEAR(c1.cohort_start_date) as stratum_1,
  p1.gender_concept_id as stratum_2,
  floor((year(c1.cohort_start_date) - p1.year_of_birth)/10) as stratum_3,
  cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct p1.PERSON_ID) as count_value
into #results_1814
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
group by c1.cohort_definition_id,
YEAR(c1.cohort_start_date),
p1.gender_concept_id,
floor((YEAR(c1.cohort_start_date) - p1.year_of_birth)/10)
;