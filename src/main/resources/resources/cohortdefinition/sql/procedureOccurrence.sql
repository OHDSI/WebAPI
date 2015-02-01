select C.person_id, C.procedure_date as start_date, DATEADD(d,1,C.procedure_date) as END_DATE
from 
(
  select po.*, ROW_NUMBER() over (PARTITION BY po.person_id ORDER BY po.procedure_date) as ordinal
  FROM @CDM_schema.PROCEDURE_OCCURRENCE po
@codesetClause
) C
JOIN @CDM_schema.PERSON P on C.person_id = P.person_id
JOIN @CDM_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id
LEFT JOIN @CDM_schema.PROVIDER PR on C.provider_id = PR.provider_id
@whereClause
