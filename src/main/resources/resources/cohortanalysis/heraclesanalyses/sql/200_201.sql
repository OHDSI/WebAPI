-- @analysisId                @analysisName
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id, @analysisId as analysis_id,
  --{@CDM_version == '4'}?{
                                vo1.place_of_service_CONCEPT_ID as stratum_1,
  --}
  --{@CDM_version == '5'}?{
                                vo1.visit_CONCEPT_ID as stratum_1,
  --}
                                cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
                                COUNT_BIG(distinct vo1.@fieldName) as count_value
into #results_@analysisId
from
@CDM_schema.visit_occurrence vo1
inner join #HERACLES_cohort c1
on vo1.person_id = c1.subject_id
--{@cohort_period_only == 'true'}?{
WHERE vo1.visit_start_date>=c1.cohort_start_date and vo1.visit_end_date<=c1.cohort_end_date
--}
group by c1.cohort_definition_id,
--{@CDM_version == '4'}?{
vo1.place_of_service_CONCEPT_ID
--}
--{@CDM_version == '5'}?{
vo1.visit_CONCEPT_ID
--}
/* This comment needed to bypass bug in Spark rendering */
;