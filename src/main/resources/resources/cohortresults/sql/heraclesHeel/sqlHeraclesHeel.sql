select analysis_id as AttributeName, HERACLES_HEEL_warning as AttributeValue
from HERACLES_HEEL_results
where cohort_definition_id in (@cohortDefinitionId)
order by case when left(HERACLES_HEEL_warning,5) = 'Error' then 1 else 2 end, analysis_id