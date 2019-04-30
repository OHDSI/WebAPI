-- @analysisId              @analysisName
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  @analysisId as analysis_id,
  o1.measurement_CONCEPT_ID as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct o1.@fieldName) as count_value
into #results_@analysisId
from
@CDM_schema.measurement o1
inner join #HERACLES_cohort c1
on o1.person_id = c1.subject_id
--{@measurement_concept_ids != '' | @cohort_period_only == 'true'}?{
WHERE
--{@cohort_period_only == 'true'}?{
o1.measurement_date>=c1.cohort_start_date and o1.measurement_date<=c1.cohort_end_date
--}
--{@measurement_concept_ids != '' & @cohort_period_only == 'true'}?{
AND
--}
--{@measurement_concept_ids != ''}?{
o1.measurement_concept_id in (@measurement_concept_ids)
--}
--}
group by c1.cohort_definition_id,
o1.measurement_CONCEPT_ID
;