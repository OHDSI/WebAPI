select count(subject_id)
from @tableQualifier.cohort
where cohort_definition_id = @cohortDefinitionId