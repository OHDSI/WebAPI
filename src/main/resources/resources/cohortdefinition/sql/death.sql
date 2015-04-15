select C.person_id, C.death_date as start_date, DATEADD(d,1,C.death_date) as end_date, C.cause_concept_id as TARGET_CONCEPT_ID
from 
(
  select d.*
  FROM @cdm_database_schema.DEATH d
@codesetClause
) C
@joinClause
@whereClause

