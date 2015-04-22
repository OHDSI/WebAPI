select C.person_id, C.procedure_date as start_date, DATEADD(d,1,C.procedure_date) as END_DATE, C.procedure_concept_id as TARGET_CONCEPT_ID
from 
(
  select po.*, ROW_NUMBER() over (PARTITION BY po.person_id ORDER BY po.procedure_date) as ordinal
  FROM @cdm_database_schema.PROCEDURE_OCCURRENCE po
@codesetClause
) C
@joinClause
@whereClause
