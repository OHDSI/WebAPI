-- Begin Treatment Line Criteria
select C.person_id, C.treatment_line_id as event_id, C.line_start_date as start_date,
       COALESCE(C.LINE_END_DATE, DATEADD(day, 1, C.LINE_START_DATE)) as end_date,
       CAST(NULL as bigint) as visit_occurrence_id,
       C.line_start_date as sort_date@additionalColumns
from
    (
        select tl.* @ordinalExpression
        FROM @cdm_database_schema.TREATMENT_LINE tl
@codesetClause
    ) C
    @joinClause
@whereClause
-- End Treatment Line Criteria
