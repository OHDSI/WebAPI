-- 402   Number of persons by condition occurrence start month, by condition_concept_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
  402 as analysis_id,
  co1.condition_concept_id as stratum_1,
  YEAR(condition_start_date)*100 + month(condition_start_date) as stratum_2,
  cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct PERSON_ID) as count_value
into #results_402
from
@CDM_schema.condition_occurrence co1
inner join #HERACLES_cohort c1
on co1.person_id = c1.subject_id
--{@condition_concept_ids != '' | @cohort_period_only == 'true'}?{
WHERE
--{@cohort_period_only == 'true'}?{
co1.condition_start_date>=c1.cohort_start_date and co1.condition_end_date<=c1.cohort_end_date
--}
--{@condition_concept_ids != '' & @cohort_period_only == 'true'}?{
AND
--}
--{@condition_concept_ids != ''}?{
co1.condition_concept_id in (@condition_concept_ids)
--}
--}
group by c1.cohort_definition_id,
co1.condition_concept_id,
YEAR(condition_start_date)*100 + month(condition_start_date)
;