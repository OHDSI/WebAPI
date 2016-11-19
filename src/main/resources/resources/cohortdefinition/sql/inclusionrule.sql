INSERT INTO #inclusionRuleCohorts (inclusion_rule_id, person_id, event_id)
select @inclusion_rule_id as inclusion_rule_id, person_id, event_id
FROM 
(
  select pe.person_id, pe.event_id
  FROM #qualified_events pe
  @additionalCriteriaQuery
) Results
;
