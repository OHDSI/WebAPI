select C.person_id, C.drug_era_start_date as start_date, C.drug_era_end_date as end_date
from 
(
  select de.*, ROW_NUMBER() over (PARTITION BY de.person_id ORDER BY de.drug_era_start_date) as ordinal
  FROM @CDM_schema.DRUG_ERA de
@codesetClause
) C
JOIN @CDM_schema.PERSON P on C.person_id = P.person_id
@whereClause
