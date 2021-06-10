-- 820   Number of observation records by condition occurrence start month
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)

select c1.cohort_definition_id,
  820 as analysis_id,
  YEAR(observation_date)*100 + month(observation_date) as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct o1.observation_id) as count_value
into #results_820
from
@CDM_schema.observation o1
inner join #HERACLES_cohort c1
on o1.person_id = c1.subject_id
--{@observation_concept_ids != '' | @cohort_period_only == 'true'}?{
WHERE
--{@cohort_period_only == 'true'}?{
o1.observation_date>=c1.cohort_start_date and o1.observation_date<=c1.cohort_end_date
--}
--{@observation_concept_ids != '' & @cohort_period_only == 'true'}?{
AND
--}
--{@observation_concept_ids != ''}?{
o1.observation_concept_id in (@observation_concept_ids)
--}
--}
group by c1.cohort_definition_id,
YEAR(observation_date)*100 + month(observation_date)
;