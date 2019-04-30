-- 704   Number of persons with at least one drug occurrence, by drug_concept_id by calendar year by gender by age decile
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
  704 as analysis_id,
  de1.drug_concept_id as stratum_1,
  YEAR(drug_exposure_start_date) as stratum_2,
  p1.gender_concept_id as stratum_3,
  floor((year(drug_exposure_start_date) - p1.year_of_birth)/10) as stratum_4,
  COUNT_BIG(distinct p1.PERSON_ID) as count_value
into #results_704
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.drug_exposure de1
on p1.person_id = de1.person_id
--{@drug_concept_ids != '' | @cohort_period_only == 'true'}?{
WHERE
--{@cohort_period_only == 'true'}?{
de1.drug_exposure_start_date>=c1.cohort_start_date and de1.drug_exposure_start_date<=c1.cohort_end_date
--}
--{@drug_concept_ids != '' & @cohort_period_only == 'true'}?{
AND
--}
--{@drug_concept_ids != ''}?{
de1.drug_concept_id in (@drug_concept_ids)
--}
--}
group by c1.cohort_definition_id,
de1.drug_concept_id,
YEAR(drug_exposure_start_date),
p1.gender_concept_id,
floor((year(drug_exposure_start_date) - p1.year_of_birth)/10)
;