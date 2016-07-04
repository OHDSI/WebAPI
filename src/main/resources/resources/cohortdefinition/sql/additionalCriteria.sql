SELECT @indexId as index_id, p.event_id
FROM @eventTable P
LEFT JOIN
(
  @criteriaQuery
) A on A.person_id = P.person_id and @windowCriteria
GROUP BY p.event_id
@occurrenceCriteria

