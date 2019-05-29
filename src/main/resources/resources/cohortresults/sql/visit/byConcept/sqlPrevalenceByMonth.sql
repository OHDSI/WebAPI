select c1.concept_id as concept_id,  --all rows for all concepts, but you may split by conceptid
	c1.concept_name as concept_name,
	num.stratum_2 as x_calendar_month,   -- calendar year, note, there could be blanks
	1000*(1.0*num.count_value/denom.count_value) as y_prevalence_1000pp  --prevalence, per 1000 persons
from (
	select stratum_1, stratum_2, count_value 
	from @ohdsi_database_schema.heracles_results 
	where analysis_id = 202 and cohort_definition_id = @cohortDefinitionId
	group by stratum_1, stratum_2, count_value 
) num
inner join (
	select stratum_1, count_value 
	from @ohdsi_database_schema.heracles_results 
	where analysis_id = 117 and cohort_definition_id = @cohortDefinitionId
	group by stratum_1, count_value 
) denom on num.stratum_2 = denom.stratum_1  --calendar year
inner join @cdm_database_schema.concept c1 on CAST(CASE WHEN isNumeric(num.stratum_1) = 1 THEN num.stratum_1 ELSE null END AS INT) = c1.concept_id
where  c1.concept_id = @conceptId
ORDER BY CAST(num.stratum_2 as INT)
