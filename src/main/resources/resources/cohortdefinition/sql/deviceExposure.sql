select C.person_id, C.device_exposure_start_date as start_date, C.device_exposure_end_date as end_date
from 
(
  select de.*, ROW_NUMBER() over (PARTITION BY de.person_id ORDER BY de.device_exposure_start_date) as ordinal
  FROM @CDM_schema.DEVICE_EXPOSURE de
@codesetClause
) C
@joinClause
@whereClause
