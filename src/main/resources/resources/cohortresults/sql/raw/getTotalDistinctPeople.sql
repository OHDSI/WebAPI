select count(distinct SUBJECT_ID) total_people
from @tableQualifier.cohort
where cohort_definition_id in (@id)
