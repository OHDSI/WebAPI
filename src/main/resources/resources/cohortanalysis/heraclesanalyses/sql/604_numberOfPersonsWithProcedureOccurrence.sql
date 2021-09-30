-- 604   Number of persons with at least one procedure occurrence, by procedure_concept_id by calendar year by gender by age decile
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
  604 as analysis_id,
  po1.procedure_concept_id as stratum_1,
  YEAR(procedure_date) as stratum_2,
  p1.gender_concept_id as stratum_3,
  floor((year(procedure_date) - p1.year_of_birth)/10) as stratum_4,
  COUNT_BIG(distinct p1.PERSON_ID) as count_value
into #results_604
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.procedure_occurrence po1
on p1.person_id = po1.person_id
--{@procedure_concept_ids != '' | @cohort_period_only == 'true'}?{
WHERE
--{@cohort_period_only == 'true'}?{
po1.procedure_date>=c1.cohort_start_date and po1.procedure_date<=c1.cohort_end_date
--}
--{@procedure_concept_ids != '' & @cohort_period_only == 'true'}?{
AND
--}
--{@procedure_concept_ids != ''}?{
po1.procedure_concept_id in (@procedure_concept_ids)
--}
--}
group by c1.cohort_definition_id,
po1.procedure_concept_id,
YEAR(procedure_date),
p1.gender_concept_id,
floor((year(procedure_date) - p1.year_of_birth)/10)
;