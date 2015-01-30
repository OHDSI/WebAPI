select C.PERSON_ID, C.condition_start_date as START_DATE, C.condition_end_date as END_DATE
from 
(
        select co.*, ROW_NUMBER() over (PARTITION BY co.PERSON_ID ORDER BY co.CONDITION_START_DATE) as ordinal
        FROM @CDM_schema.CONDITION_OCCURRENCE co
@codesetClause
) C
JOIN @CDM_schema.PERSON P on C.PERSON_ID = P.PERSON_ID
JOIN @CDM_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.PERSON_ID = V.PERSON_ID
LEFT JOIN @CDM_schema.PROVIDER PR on C.PROVIDER_ID = PR.PROVIDER_ID
@whereClause
