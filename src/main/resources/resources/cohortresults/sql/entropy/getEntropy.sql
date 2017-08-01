select COHORT_DEFINITION_ID, ANALYSIS_ID, STRATUM_1, STRATUM_2, STRATUM_3, STRATUM_4, STRATUM_5, count_value, last_update_time 
from @tableQualifier.heracles_results 
where cohort_definition_id = @cohortDefinitionId 
and analysis_id = 2031
