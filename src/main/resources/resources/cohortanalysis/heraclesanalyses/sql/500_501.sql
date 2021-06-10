-- @analysisId                @analysisName
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  @analysisId as analysis_id,
{@CDM_version == '4'}?{ d1.cause_of_death_concept_id } {@CDM_version == '5'}?{ d1.cause_CONCEPT_ID }    as stratum_1,
cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
COUNT_BIG(distinct d1.PERSON_ID) as count_value
into #results_@analysisId
from
@CDM_schema.death d1
inner join #HERACLES_cohort c1
on d1.person_id = c1.subject_id
--{@cohort_period_only == 'true'}?{
WHERE d1.death_date>=c1.cohort_start_date and d1.death_date<=c1.cohort_end_date
--}
group by c1.cohort_definition_id,
{@CDM_version == '4'}?{ d1.cause_of_death_concept_id } {@CDM_version == '5'}?{ d1.cause_CONCEPT_ID }
;