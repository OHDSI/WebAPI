select C.person_id, C.measurement_date as start_date, DATEADD(d,1,C.measurement_date) as END_DATE
from 
(
  select m.*, ROW_NUMBER() over (PARTITION BY m.person_id ORDER BY m.measurement_date) as ordinal
  FROM @CDM_schema.MEASUREMENT m
@codesetClause
) C
@joinClause
@whereClause
