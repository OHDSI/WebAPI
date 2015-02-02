select C.person_id, C.visit_start_date as start_date, C.visit_end_date as end_date
from 
(
  select vo.*, ROW_NUMBER() over (PARTITION BY vo.person_id ORDER BY vo.visit_start_date) as ordinal
  FROM @CDM_schema.VISIT_OCCURRENCE vo
@codesetClause
) C
@joinClause
@whereClause

