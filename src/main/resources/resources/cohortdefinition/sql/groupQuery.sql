select @indexId as index_id, event_id
FROM
(
  select event_id FROM
  (
    @criteriaQueries
  ) CQ
  GROUP BY event_id
  @intersectClause
) G
