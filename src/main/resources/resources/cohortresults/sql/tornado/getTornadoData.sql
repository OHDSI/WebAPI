select stratum_1 gender_concept_id, stratum_2 age_group, count_value person_count
from @tableQualifier.heracles_results
where cohort_definition_id = @cohortDefinitionId
and analysis_id = 3000
