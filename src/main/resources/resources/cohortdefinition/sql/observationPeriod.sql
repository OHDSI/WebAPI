select C.person_id, C.observation_period_start_date as start_date, C.observation_period_end_date as end_date
from 
(
        select op.*, ROW_NUMBER() over (PARTITION BY op.person_id ORDER BY op.observation_period_start_date) as ordinal
        FROM @CDM_schema.OBSERVATION_PERIOD op
) C
JOIN @CDM_schema.PERSON P on C.person_id = P.person_id
@whereClause
