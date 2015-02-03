@codesetQuery

@primaryEventsQuery

select @cohortId as cohort_definition_id, person_id as subject_id, start_date as cohort_start_date, end_date as cohort_end_date
FROM 
(
  select Raw.*, row_number() over (partition by Raw.person_id order by Raw.start_date @EventSort) as ordinal
  FROM
  (
    select person_id, start_date, end_date
    FROM #PrimaryCriteriaEvents
    @additionalCriteriaQuery
  ) Raw
) Results
@ResultLimitFilter
;

DROP TABLE #Codesets;
DROP TABLE #PrimaryCriteriaEvents;
