select 
	hr1.index_year as X_CALENDAR_YEAR,
	c1.concept_name as SERIES_NAME,
	CONCAT(
	  cast(hr1.age_decile*10 as varchar(11)), '-', cast((hr1.age_decile+1)*10-1 as varchar(11))
	) as TRELLIS_NAME,
	hr1.count_value as NUM_PERSONS,
	round(1000*(1.0*hr1.count_value / t1.count_value),5) as Y_PREVALENCE_1000PP
from (select cohort_definition_id,
	stratum_1 as index_year,
	CAST(CASE WHEN analysis_id = 1814 THEN stratum_2 ELSE null END AS INT) as gender_concept_id,
	CAST(CASE WHEN analysis_id = 1814 THEN stratum_3 ELSE null END AS INT) as age_decile,
	count_value 
	from @ohdsi_database_schema.heracles_results
	where analysis_id = 1814
	and cohort_definition_id = @cohortDefinitionId
	and CAST(CASE WHEN analysis_id = 1814 THEN stratum_2 ELSE null END AS INT) in (8507,8532)
	and CAST(CASE WHEN analysis_id = 1814 THEN stratum_3 ELSE null END AS INT) >= 0
) hr1
	inner join 
(
	select stratum_1 as index_year,
	CAST(CASE WHEN analysis_id = 116 THEN stratum_2 ELSE null END AS INT) as gender_concept_id,
	CAST(CASE WHEN analysis_id = 116 THEN stratum_3 ELSE null END AS INT) as age_decile,
	count_value 
	from @ohdsi_database_schema.heracles_results 
	where analysis_id = 116
	and cohort_definition_id = @cohortDefinitionId
) t1
on hr1.index_year = t1.index_year
and hr1.gender_concept_id = t1.gender_concept_id
and hr1.age_decile = t1.age_decile
inner join
@cdm_database_schema.concept c1
on hr1.gender_concept_id = c1.concept_id
