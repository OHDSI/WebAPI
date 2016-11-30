INSERT INTO #strataCohorts (strata_sequence, person_id, event_id)
select @strata_sequence as strata_id, person_id, event_id
FROM 
(
  select pe.person_id, pe.event_id
  FROM #analysis_events pe
  @additionalCriteriaQuery
) Results
;
