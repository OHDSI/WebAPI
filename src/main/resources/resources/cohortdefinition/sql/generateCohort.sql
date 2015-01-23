@codesetQuery

@primaryEventsQuery

select @cohortId as cohort_definition_id, PERSON_ID as SUBJECT_ID, START_DATE as COHORT_START_DATE, END_DATE as COHORT_END_DATE
FROM 
(
  select Raw.*, row_number() over (partition by Raw.PERSON_ID order by Raw.START_DATE) as ordinal
  FROM
  (
    select PERSON_ID, START_DATE, END_DATE
    FROM #PrimaryCriteriaEvents
    @additionalCriteriaQuery
  ) Raw
) Results
WHERE Results.ordinal = 1
;

DROP TABLE #Codesets;
DROP TABLE #PrimaryCriteriaEvents;
