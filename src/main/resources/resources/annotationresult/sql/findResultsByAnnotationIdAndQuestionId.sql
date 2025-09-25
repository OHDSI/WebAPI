SELECT *
FROM @results_schema.annotation_result s
WHERE s.annotation_id = @annotation_id AND s.question_id = @question_id
;