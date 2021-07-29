-- @analysisId   @analysisName
--insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, count_value)
select c1.cohort_definition_id,
  @analysisId as analysis_id,
  YEAR(@fieldName)*100 + month(@fieldName) as stratum_1,
  cast( '' as varchar(1) ) as stratum_2, cast( '' as varchar(1) ) as stratum_3, cast( '' as varchar(1) ) as stratum_4,
  COUNT_BIG(distinct op1.PERSON_ID) as count_value
into #results_@analysisId
from @CDM_schema.person p1
inner join #HERACLES_cohort_subject c1
on p1.person_id = c1.subject_id
inner join
@CDM_schema.observation_period op1
on p1.person_id = op1.person_id
group by c1.cohort_definition_id, YEAR(@fieldName)*100 + month(@fieldName)
;