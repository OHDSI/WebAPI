INSERT INTO #inclusionRuleCohorts (inclusion_rule_id, event_id)
select @inclusion_rule_id as inclusion_rule_id, event_id
FROM 
(
  select pe.event_id
  FROM #primary_events pe
  @additionalCriteriaQuery
) Results
;
