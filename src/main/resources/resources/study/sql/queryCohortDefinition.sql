select 
  cd1.cohort_definition_id,
  cd1.cohort_definition_name,
  cd1.short_name
from @study_results_schema.cohort_definition cd1
where cd1.cohort_definition_id = @cohort_definition_id
;
