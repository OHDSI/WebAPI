-- Begin Correlated Criteria
SELECT @indexId as index_id, p.person_id, p.event_id
FROM @eventTable P
LEFT JOIN
(
  @criteriaQuery
) A on A.person_id = P.person_id and @windowCriteria
GROUP BY p.person_id, p.event_id
@occurrenceCriteria
-- End Correlated Criteria
