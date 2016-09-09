-- Begin Observation Criteria
select C.person_id, C.observation_id as event_id, C.observation_date as start_date, DATEADD(d,1,C.observation_date) as END_DATE, C.observation_concept_id as TARGET_CONCEPT_ID
from 
(
  select o.*, ROW_NUMBER() over (PARTITION BY o.person_id ORDER BY o.observation_date, o.observation_id) as ordinal
  FROM @cdm_database_schema.OBSERVATION o
@codesetClause
) C
@joinClause
@whereClause
-- End Observation Criteria