select subject_id, cohort_start_date, cohort_end_date 
from (
	select row_number() over (order by subject_id) row_limit, subject_id, cohort_start_date, cohort_end_date
	from @tableQualifier.cohort
	where cohort_definition_id = @cohortDefinitionId
) ordered
where row_limit between @min and @max