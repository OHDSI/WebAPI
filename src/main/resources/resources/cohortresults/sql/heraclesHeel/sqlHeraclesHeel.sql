select analysis_id as ATTRIBUTE_NAME, HERACLES_HEEL_warning as ATTRIBUTE_VALUE
from @ohdsi_database_schema.HERACLES_HEEL_results
where cohort_definition_id in (@cohortDefinitionId)
order by case when left(HERACLES_HEEL_warning,5) = 'Error' then 1 else 2 end, analysis_id