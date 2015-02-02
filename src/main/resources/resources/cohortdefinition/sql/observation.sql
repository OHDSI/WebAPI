select C.person_id, C.observation_date as start_date, DATEADD(d,1,C.observation_date) as END_DATE
from 
(
  select o.*, ROW_NUMBER() over (PARTITION BY o.person_id ORDER BY o.observation_date) as ordinal
  FROM @CDM_schema.OBSERVATION o
@codesetClause
) C
@joinClause
@whereClause
