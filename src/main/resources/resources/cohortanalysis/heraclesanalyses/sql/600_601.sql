-- @analysisId                @analysisName
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  @analysisId as analysis_id,
  po1.procedure_CONCEPT_ID as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct po1.@fieldName) as count_value
into #results_@analysisId
from
@CDM_schema.procedure_occurrence po1
inner join #HERACLES_cohort c1
on po1.person_id = c1.subject_id
--{@procedure_concept_ids != '' | @cohort_period_only == 'true'}?{
WHERE
--{@cohort_period_only == 'true'}?{
po1.procedure_date>=c1.cohort_start_date and po1.procedure_date<=c1.cohort_end_date
--}
--{@procedure_concept_ids != '' & @cohort_period_only == 'true'}?{
AND
--}
--{@procedure_concept_ids != ''}?{
po1.procedure_concept_id in (@procedure_concept_ids)
--}
--}
group by c1.cohort_definition_id,
po1.procedure_CONCEPT_ID
;