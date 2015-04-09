IF OBJECT_ID('Codesets', 'U') IS NOT NULL --This should only do something in Oracle
  drop table Codesets;

IF OBJECT_ID('tempdb..#Codesets', 'U') IS NOT NULL
  drop table #Codesets;
  
IF OBJECT_ID('PrimaryCriteriaEvents', 'U') IS NOT NULL --This should only do something in Oracle
  drop table PrimaryCriteriaEvents;

IF OBJECT_ID('tempdb..#PrimaryCriteriaEvents', 'U') IS NOT NULL
  drop table #PrimaryCriteriaEvents;
  
  
@codesetQuery
@primaryEventsQuery

DELETE FROM @targetSchema.@targetTable where cohort_definition_id = @cohortDefinitionId;
INSERT INTO @targetSchema.@targetTable (cohort_definition_id, subject_id, cohort_start_date, cohort_end_date)
select @cohortDefinitionId as cohort_definition_id, person_id as subject_id, start_date as cohort_start_date, end_date as cohort_end_date
FROM 
(
  select RawEvents.*, row_number() over (partition by RawEvents.person_id order by RawEvents.start_date @EventSort) as ordinal
  FROM
  (
    select person_id, start_date, end_date
    FROM #PrimaryCriteriaEvents
    @additionalCriteriaQuery
  ) RawEvents
) Results
@ResultLimitFilter
;

TRUNCATE TABLE #Codesets;
DROP TABLE #Codesets;

TRUNCATE TABLE #PrimaryCriteriaEvents;
DROP TABLE #PrimaryCriteriaEvents;
