select c1.concept_id as CONCEPT_ID,  --all rows for all concepts, but you may split by conceptid
	c1.concept_name as CONCEPT_NAME,
	CAST(CASE WHEN isNumeric(num_stratum_1) = 1 THEN num_stratum_1 ELSE null END AS INT) as X_CALENDAR_MONTH,   -- calendar year, note, there could be blanks
	round(1000*(1.0*num.count_value/denom.count_value),5) as Y_PREVALENCE_1000PP  --prevalence, per 1000 persons
from (
	select stratum_1, stratum_2, count_value 
	from @ohdsi_database_schema.heracles_results 
	where analysis_id = 802 and cohort_definition_id = @cohortDefinitionId
	group by stratum_1, stratum_2, count_value 
) num
inner join (
	select stratum_1, count_value 
	from @ohdsi_database_schema.heracles_results 
	where analysis_id = 117 and cohort_definition_id = @cohortDefinitionId
	group by stratum_1, count_value 
) denom on num.stratum_2 = denom.stratum_1  --calendar year
inner join @cdm_database_schema.concept c1 on CAST(num.stratum_1 AS BIGINT) = c1.concept_id
WHERE c1.concept_id = @conceptId
ORDER BY CAST(num.stratum_2 as INT)
