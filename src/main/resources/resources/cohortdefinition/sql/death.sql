select C.person_id, C.death_date as start_date, DATEADD(d,1,C.death_date) as end_date
from 
(
  select d.*
  FROM @CDM_schema.DEATH d
@codesetClause
) C
@joinClause
@whereClause

