-- 702   Number of persons by drug occurrence start month, by drug_concept_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
  702 as analysis_id,
  de1.drug_concept_id as stratum_1,
  YEAR(drug_exposure_start_date)*100 + month(drug_exposure_start_date) as stratum_2,
  cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct PERSON_ID) as count_value
into #results_702
from
@CDM_schema.drug_exposure de1
inner join #HERACLES_cohort c1
on de1.person_id = c1.subject_id
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
YEAR(drug_exposure_start_date)*100 + month(drug_exposure_start_date)
;