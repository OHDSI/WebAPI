select count(distinct SUBJECT_ID) total_people
from result_schema.cohort
where cohort_definition_id in (?)
