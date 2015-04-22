select C.person_id, C.measurement_date as start_date, DATEADD(d,1,C.measurement_date) as END_DATE, C.measurement_concept_id as TARGET_CONCEPT_ID
from 
(
  select m.*, ROW_NUMBER() over (PARTITION BY m.person_id ORDER BY m.measurement_date) as ordinal
  FROM @cdm_database_schema.MEASUREMENT m
@codesetClause
) C
@joinClause
@whereClause
