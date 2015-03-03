select C.person_id, C.condition_start_date as start_date, C.condition_end_date as end_date, C.CONDITION_CONCEPT_ID as TARGET_CONCEPT_ID
from 
(
        select co.*, ROW_NUMBER() over (PARTITION BY co.person_id ORDER BY co.condition_start_date) as ordinal
        FROM @CDM_schema.CONDITION_OCCURRENCE co
@codesetClause
) C
@joinClause
@whereClause
