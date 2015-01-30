select C.PERSON_ID, C.DRUG_EXPOSURE_START_DATE as START_DATE, C.drug_exposure_end_date as END_DATE
from 
(
  select de.*, ROW_NUMBER() over (PARTITION BY de.PERSON_ID ORDER BY de.DRUG_EXPOSURE_START_DATE) as ordinal
  FROM @CDM_schema.DRUG_EXPOSURE de
@codesetClause
) C
JOIN @CDM_schema.PERSON P on C.PERSON_ID = P.PERSON_ID
JOIN @CDM_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.PERSON_ID = V.PERSON_ID
LEFT JOIN @CDM_schema.PROVIDER PR on C.PROVIDER_ID = PR.PROVIDER_ID
@whereClause
