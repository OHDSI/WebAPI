select C.person_id, C.condition_era_start_date as start_date, C.condition_era_end_date as end_date
from 
(
  select ce.*, ROW_NUMBER() over (PARTITION BY ce.person_id ORDER BY ce.condition_era_start_date) as ordinal
  FROM @CDM_schema.CONDITION_ERA ce
@codesetClause
) C
@joinClause
@whereClause
