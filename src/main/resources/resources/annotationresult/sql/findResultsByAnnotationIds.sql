SELECT *
FROM @results_schema.annotation_result s
WHERE s.annotation_id IN ( @idList )
;