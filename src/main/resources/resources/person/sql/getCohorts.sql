select subject_id, cohort_definition_id, cohort_start_date, cohort_end_date
from @tableQualifier.cohort
where subject_id = @subjectId
