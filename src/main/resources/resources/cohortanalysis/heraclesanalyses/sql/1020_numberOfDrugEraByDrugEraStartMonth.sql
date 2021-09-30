-- 1020                Number of drug era records by drug era start month
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  1020 as analysis_id,
  YEAR(condition_era_start_date)*100 + month(condition_era_start_date) as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct ce1.condition_era_id) as count_value
into #results_1020
from
@CDM_schema.condition_era ce1
inner join #HERACLES_cohort c1
on ce1.person_id = c1.subject_id
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
YEAR(condition_era_start_date)*100 + month(condition_era_start_date)
;