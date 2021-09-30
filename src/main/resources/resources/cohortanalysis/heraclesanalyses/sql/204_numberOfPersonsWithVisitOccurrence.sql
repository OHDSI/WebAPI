-- 204   Number of persons with at least one visit occurrence, by visit_concept_id by calendar year by gender by age decile
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)
select c1.cohort_definition_id,
  204 as analysis_id,
  --{@CDM_version == '4'}?{
  vo1.place_of_service_CONCEPT_ID as stratum_1,
  --}
  --{@CDM_version == '5'}?{
  vo1.visit_CONCEPT_ID as stratum_1,
  --}
  YEAR(visit_start_date) as stratum_2,
  p1.gender_concept_id as stratum_3,
  floor((year(visit_start_date) - p1.year_of_birth)/10) as stratum_4,
  COUNT_BIG(distinct p1.PERSON_ID) as count_value
into #results_204
from @CDM_schema.person p1
inner join #HERACLES_cohort c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.visit_occurrence vo1
on p1.person_id = vo1.person_id
--{@cohort_period_only == 'true'}?{
WHERE vo1.visit_start_date>=c1.cohort_start_date and vo1.visit_end_date<=c1.cohort_end_date
--}
group by c1.cohort_definition_id,
--{@CDM_version == '4'}?{
vo1.place_of_service_CONCEPT_ID,
--}
--{@CDM_version == '5'}?{
vo1.visit_CONCEPT_ID,
--}
YEAR(visit_start_date),
p1.gender_concept_id,
floor((year(visit_start_date) - p1.year_of_birth)/10)
;