-- Begin Demographic Criteria
SELECT @indexId as index_id, e.person_id, e.event_id
FROM @eventTable E
JOIN @cdm_database_schema.PERSON P ON P.PERSON_ID = E.PERSON_ID
@whereClause
GROUP BY e.person_id, e.event_id
-- End Demographic Criteria
