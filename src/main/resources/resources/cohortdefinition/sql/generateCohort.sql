@codesetQuery
@primaryEventsQuery

DELETE FROM @targetTable where cohort_definition_id = @cohortDefinitionId;
INSERT INTO @targetTable (cohort_definition_id, subject_id, cohort_start_date, cohort_end_date)
select @cohortDefinitionId as cohort_definition_id, person_id as subject_id, start_date as cohort_start_date, end_date as cohort_end_date
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
