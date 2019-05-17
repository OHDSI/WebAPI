-- 802   Number of persons by observation occurrence start month, by observation_concept_id
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, count_value)
select c1.cohort_definition_id,
  802 as analysis_id,
  o1.observation_concept_id as stratum_1,
  YEAR(observation_date)*100 + month(observation_date) as stratum_2,
  cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct PERSON_ID) as count_value
into #results_802
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
o1.observation_concept_id,
YEAR(observation_date)*100 + month(observation_date)
;