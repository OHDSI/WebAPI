SELECT *
FROM @results_schema.annotation_result s
WHERE s.question_id in (@annotationId)
;