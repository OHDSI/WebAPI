SELECT longitude, latitude, c.subject_id
FROM @resultSchema.cohort c
	JOIN @cdmSchema.location_history lh
		ON c.subject_id = lh.entity_id AND lh.domain_id = 'PERSON'
			 AND c.cohort_start_date <= isNull(lh.end_date, DATEFROMPARTS(2099, 12, 31))
AND c.cohort_end_date >= lh.start_date
JOIN @cdmSchema.location l
ON lh.location_id = l.location_id
WHERE c.cohort_definition_id = {{cohortId}}
AND CAST({{westLongitude}} AS FLOAT) <= longitude AND longitude <= CAST({{eastLongitude}} AS FLOAT)
AND CAST({{northLatitude}} AS FLOAT) >= latitude AND latitude >= CAST({{southLatitude}} AS FLOAT)
