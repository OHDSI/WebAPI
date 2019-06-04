-- Begin Treatment Line Criteria
select C.person_id, C.treatment_line_id as event_id, C.line_start_date as start_date, COALESCE(C.line_end_date, DATEADD(day, 1, C.line_start_date)) as end_date, C.drug_concept_id as TARGET_CONCEPT_ID, CAST(NULL as bigint) as visit_occurrence_id
from
(
  select tl.* @ordinalExpression
  FROM @cdm_database_schema.TREATMENT_LINE tl
@codesetClause
) C
@joinClause
@whereClause
-- End Treatment Line Criteria
