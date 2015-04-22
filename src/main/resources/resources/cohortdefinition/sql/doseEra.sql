select C.person_id, C.dose_era_start_date as start_date, C.dose_era_end_date as end_date, C.drug_concept_id as TARGET_CONCEPT_ID
from 
(
  select de.*, ROW_NUMBER() over (PARTITION BY de.person_id ORDER BY de.dose_era_start_date) as ordinal
  FROM @cdm_database_schema.DOSE_ERA de
@codesetClause
) C
@joinClause
@whereClause
