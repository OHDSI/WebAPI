SELECT @indexId as index_id, e.event_id
FROM @eventTable E
JOIN @cdm_database_schema.PERSON P ON P.PERSON_ID = E.PERSON_ID
@whereClause
GROUP BY e.event_id


