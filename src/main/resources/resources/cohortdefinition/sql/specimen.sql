select C.person_id, C.specimen_date as start_date, DATEADD(d,1,C.specimen_date) as end_date
from 
(
  select s.*, ROW_NUMBER() over (PARTITION BY s.person_id ORDER BY s.specimen_date) as ordinal
  FROM @CDM_schema.SPECIMEN s
@codesetClause
) C
@joinClause
@whereClause
