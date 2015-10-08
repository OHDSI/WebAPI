-- need to update this to include paging logic
select top 100 subject_id, cohort_start_date, cohort_end_date
from @tableQualifier.cohort
where cohort_definition_id = @cohortDefinitionId
order by subject_id