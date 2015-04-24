@codesetQuery
@primaryEventsQuery

DELETE FROM @target_database_schema.@target_cohort_table where cohort_definition_id = @cohort_definition_id;
INSERT INTO @target_database_schema.@target_cohort_table (cohort_definition_id, subject_id, cohort_start_date, cohort_end_date)
select @cohort_definition_id as cohort_definition_id, person_id as subject_id, start_date as cohort_start_date, end_date as cohort_end_date
FROM 
(
  select RawEvents.*, row_number() over (partition by RawEvents.person_id order by RawEvents.start_date @EventSort) as ordinal
  FROM
  (
    select pe.person_id, pe.start_date, pe.end_date
    FROM #PrimaryCriteriaEvents pe
    @additionalCriteriaQuery
  ) RawEvents
) Results
@ResultLimitFilter
;

TRUNCATE TABLE #Codesets;
DROP TABLE #Codesets;

TRUNCATE TABLE #PrimaryCriteriaEvents;
DROP TABLE #PrimaryCriteriaEvents;
