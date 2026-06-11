select stratum_4 gender_concept_id, stratum_3 age_group, stratum_2 person_id
from @tableQualifier.heracles_results
where cohort_definition_id = @cohortDefinitionId
and analysis_id = 3001
