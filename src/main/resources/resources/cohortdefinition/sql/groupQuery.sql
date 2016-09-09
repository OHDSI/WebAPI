-- Begin Criteria Group
select @indexId as index_id, person_id, event_id
FROM
(
  select E.person_id, E.event_id 
  FROM @eventTable E
  LEFT JOIN
  (
    @criteriaQueries
  ) CQ on E.person_id = CQ.person_id and E.event_id = CQ.event_id
  GROUP BY E.person_id, E.event_id
  @occurrenceCountClause
) G
-- End Criteria Group
