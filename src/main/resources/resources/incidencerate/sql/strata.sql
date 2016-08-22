INSERT INTO #strataCohorts (strata_sequence, event_id)
select @strata_sequence as strata_id, event_id
FROM 
(
  select pe.event_id
  FROM #analysis_events pe
  @additionalCriteriaQuery
) Results
;
