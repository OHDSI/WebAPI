with primary_events (event_id, person_id, start_date, end_date, op_start_date, op_end_date, visit_occurrence_id) as
(
    @primaryEventsQuery
)
SELECT event_id, person_id, start_date, end_date, op_start_date, op_end_date, visit_occurrence_id
INTO #qualified_events
FROM
(
select pe.event_id, pe.person_id, pe.start_date, pe.end_date, pe.op_start_date, pe.op_end_date, row_number() over (partition by pe.person_id order by pe.start_date @QualifiedEventSort) as ordinal, cast(pe.visit_occurrence_id as bigint) as visit_occurrence_id
FROM primary_events pe
@additionalCriteriaQuery
) QE
@QualifiedLimitFilter
;
