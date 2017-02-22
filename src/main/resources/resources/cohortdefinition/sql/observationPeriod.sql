-- Begin Observation Period Criteria
select C.person_id, C.observation_period_id as event_id, @startDateExpression as start_date, @endDateExpression as end_date, C.period_type_concept_id as TARGET_CONCEPT_ID
from 
(
        select op.*, ROW_NUMBER() over (PARTITION BY op.person_id ORDER BY op.observation_period_start_date) as ordinal
        FROM @cdm_database_schema.OBSERVATION_PERIOD op
) C
@joinClause
@whereClause
-- End Observation Period Criteria
