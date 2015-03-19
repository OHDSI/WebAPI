select 
	--hr1.cohort_definition_id,
	hr1.index_year as X_CALENDAR_YEAR,
	c1.concept_name as SERIES_NAME,
	cast(hr1.age_decile*10 as varchar) + '-' + cast((hr1.age_decile+1)*10-1 as varchar) as TRELLIS_NAME,
	hr1.count_value as NUM_PERSONS,
	round(1000*(1.0*hr1.count_value / t1.count_value),5) as Y_PREVALENCE_1000PP
from (select cohort_definition_id,
	cast(stratum_1 as integer) as index_year,
	cast(stratum_2 as integer) as gender_concept_id,
	cast(stratum_3 as integer) as age_decile,
	count_value 
	from @resultsSchema.dbo.heracles_results
	where analysis_id in (1814)
	and cohort_definition_id in (@cohortDefinitionId)
	and stratum_2 in (8507,8532)
	and stratum_3 >= 0 
	--and stratum_4 <10
) hr1
	inner join 
(
	select cast(stratum_1 as integer) as index_year,
	cast(stratum_2 as integer) as gender_concept_id,
	cast(stratum_3 as integer) as age_decile,
	count_value 
	from @resultsSchema.dbo.heracles_results 
	where analysis_id = 116
	and cohort_definition_id in (@cohortDefinitionId)
) t1
on hr1.index_year = t1.index_year
and hr1.gender_concept_id = t1.gender_concept_id
and hr1.age_decile = t1.age_decile
inner join
@cdmSchema.dbo.concept c1
on hr1.gender_concept_id = c1.concept_id
