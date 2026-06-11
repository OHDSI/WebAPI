select CONCAT(
  cast(cast(CASE WHEN isNumeric(num.stratum_3) = 1 THEN num.stratum_3 ELSE null END AS INT) * 10 AS VARCHAR(11)), '-',
  cast((cast(CASE WHEN isNumeric(num.stratum_3) = 1 THEN num.stratum_3 ELSE null END AS INT) + 1) * 10 - 1 AS VARCHAR(11))) as trellis_name, --age decile
	c2.concept_name as series_name,  --gender
	CAST(CASE WHEN isNumeric(num.stratum_1) = 1 THEN num.stratum_1 ELSE null END AS INT) as x_calendar_year,   -- calendar year, note, there could be blanks
	ROUND(1000*(1.0*num.count_value/denom.count_value),5) as y_prevalence_1000pp,  --prevalence, per 1000 persons,
	num.count_value as num_persons
from 
	(select * from @ohdsi_database_schema.heracles_results where analysis_id = 504 and cohort_definition_id = @cohortDefinitionId) num
	inner join
	(select * from @ohdsi_database_schema.heracles_results where analysis_id = 116 and cohort_definition_id = @cohortDefinitionId) denom on num.stratum_1 = denom.stratum_1  --calendar year
		and num.stratum_2 = denom.stratum_2 --gender
		and num.stratum_3 = denom.stratum_3 --age decile
	inner join @cdm_database_schema.concept c2 on CAST(CASE WHEN isNumeric(num.stratum_2) = 1 THEN num.stratum_2 ELSE null END AS INT) = c2.concept_id
where c2.concept_id in (8507, 8532)
ORDER BY CAST(CASE WHEN isNumeric(num.stratum_1) = 1 THEN num.stratum_1 ELSE null END AS INT)
