-- Begin Device Exposure Criteria
select C.person_id, C.device_exposure_id as event_id, C.device_exposure_start_date as start_date, C.device_exposure_end_date as end_date, C.device_concept_id as TARGET_CONCEPT_ID
from 
(
  select de.*, ROW_NUMBER() over (PARTITION BY de.person_id ORDER BY de.device_exposure_start_date, de.device_exposure_id) as ordinal
  FROM @cdm_database_schema.DEVICE_EXPOSURE de
@codesetClause
) C
@joinClause
@whereClause
-- End Device Exposure Criteria
