-- 804   Number of persons with at least one observation occurrence, by observation_concept_id by calendar year by gender by age decile
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
  804 as analysis_id,
  o1.observation_concept_id as stratum_1,
  YEAR(observation_date) as stratum_2,
  p1.gender_concept_id as stratum_3,
  floor((year(observation_date) - p1.year_of_birth)/10) as stratum_4,
  COUNT_BIG(distinct p1.PERSON_ID) as count_value
into #results_804
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.observation o1
on p1.person_id = o1.person_id
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
YEAR(observation_date),
p1.gender_concept_id,
floor((year(observation_date) - p1.year_of_birth)/10)
;