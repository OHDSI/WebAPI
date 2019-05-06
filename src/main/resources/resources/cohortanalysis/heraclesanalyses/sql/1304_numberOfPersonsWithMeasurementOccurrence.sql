-- 1304                Number of persons with at least one measurement occurrence, by measurement_concept_id by calendar year by gender by age decile
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
  1304 as analysis_id,
  o1.measurement_concept_id as stratum_1,
  YEAR(measurement_date) as stratum_2,
  p1.gender_concept_id as stratum_3,
  floor((year(measurement_date) - p1.year_of_birth)/10) as stratum_4,
  COUNT_BIG(distinct p1.PERSON_ID) as count_value
into #results_1304
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.measurement o1
on p1.person_id = o1.person_id
--{@measurement_concept_ids != '' | @cohort_period_only == 'true'}?{
WHERE
--{@cohort_period_only == 'true'}?{
o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
--{@measurement_concept_ids != '' & @cohort_period_only == 'true'}?{
AND
--}
--{@measurement_concept_ids != ''}?{
o1.measurement_concept_id in (@measurement_concept_ids)
--}
--}
group by c1.cohort_definition_id,
o1.measurement_concept_id,
YEAR(measurement_date),
p1.gender_concept_id,
floor((year(measurement_date) - p1.year_of_birth)/10)
;