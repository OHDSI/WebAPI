select C.PERSON_ID, C.condition_start_date as START_DATE, C.condition_end_date as END_DATE, C.observation_period_id
from 
(
        select co.*, op.observation_period_id, ROW_NUMBER() over (PARTITION BY co.PERSON_ID ORDER BY co.CONDITION_START_DATE) as ordinal
        FROM @CDM_schema.CONDITION_OCCURRENCE co
@codesetJoin
        join @CDM_schema.OBSERVATION_PERIOD op on op.PERSON_ID = co.PERSON_ID and co.CONDITION_START_DATE between op.OBSERVATION_PERIOD_START_DATE and OBSERVATION_PERIOD_END_DATE
) C
JOIN @CDM_schema.PERSON P on C.PERSON_ID = P.PERSON_ID
JOIN @CDM_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.PERSON_ID = V.PERSON_ID
LEFT JOIN @CDM_schema.PROVIDER PR on C.PROVIDER_ID = PR.PROVIDER_ID
@whereClause
