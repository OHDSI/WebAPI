SELECT *
FROM @results_schema.annotation_result s
WHERE s.annotation_id = @annotationId
ORDER BY last_update_time DESC
LIMIT 1
;