--persons in cohort from cohort start time to cohort end time, in 30d increments
select CAST(CASE WHEN hr1.analysis_id = 1804 THEN hr1.stratum_1 ELSE null END AS INT) as month_year,
  hr1.count_value as count_value, 
round(1.0*hr1.count_value / denom.count_value,5) as percent_value
from (select * from @ohdsi_database_schema.heracles_results where analysis_id = 1804 and cohort_definition_id = @cohortDefinitionId) hr1,
(select count_value from @ohdsi_database_schema.heracles_results where analysis_id = 1 and cohort_definition_id = @cohortDefinitionId) denom
order by CAST(CASE WHEN hr1.analysis_id = 1804 THEN hr1.stratum_1 ELSE null END AS INT) asc
