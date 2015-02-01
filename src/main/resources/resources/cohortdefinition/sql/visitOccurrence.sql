select C.person_id, C.visit_start_date as start_date, C.visit_end_date as end_date
from 
(
  select vo.*, ROW_NUMBER() over (PARTITION BY vo.person_id ORDER BY vo.visit_start_date) as ordinal
  FROM @CDM_schema.VISIT_OCCURRENCE vo
@codesetClause
) C
JOIN @CDM_schema.PERSON P on C.person_id = P.person_id
JOIN @CDM_schema.CARE_SITE CS on C.care_site_id = CS.care_site_id
LEFT JOIN @CDM_schema.PROVIDER PR on C.provider_id = PR.provider_id
@whereClause

