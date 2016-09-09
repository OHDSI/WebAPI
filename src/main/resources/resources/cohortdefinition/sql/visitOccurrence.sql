-- Begin Visit Occurrence Criteria
select C.person_id, C.visit_occurrence_id as event_id, C.visit_start_date as start_date, C.visit_end_date as end_date, C.visit_concept_id as TARGET_CONCEPT_ID
from 
(
  select vo.*, ROW_NUMBER() over (PARTITION BY vo.person_id ORDER BY vo.visit_start_date, vo.visit_occurrence_id) as ordinal
  FROM @cdm_database_schema.VISIT_OCCURRENCE vo
@codesetClause
) C
@joinClause
@whereClause
-- End Visit Occurrence Criteria
