-- 1004                Number of persons with at least one condition occurrence, by condition_concept_id by calendar year by gender by age decile
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
  1004 as analysis_id,
  ce1.condition_concept_id as stratum_1,
  YEAR(condition_era_start_date) as stratum_2,
  p1.gender_concept_id as stratum_3,
  floor((year(condition_era_start_date) - p1.year_of_birth)/10) as stratum_4,
  COUNT_BIG(distinct p1.PERSON_ID) as count_value
into #results_1004
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.condition_era ce1
on p1.person_id = ce1.person_id
--{@condition_concept_ids != '' | @cohort_period_only == 'true'}?{
WHERE
--{@cohort_period_only == 'true'}?{
ce1.condition_era_start_date>=c1.cohort_start_date and ce1.condition_era_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'true'}?{
AND
--}
--{@condition_concept_ids != ''}?{
ce1.condition_concept_id in (@condition_concept_ids)
--}
--}
group by c1.cohort_definition_id,
ce1.condition_concept_id,
YEAR(condition_era_start_date),
p1.gender_concept_id,
floor((year(condition_era_start_date) - p1.year_of_birth)/10)
;