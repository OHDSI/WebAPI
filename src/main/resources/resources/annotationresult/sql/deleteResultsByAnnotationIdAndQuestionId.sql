DELETE FROM @results_schema.annotation_result
WHERE annotation_id = @annotation_id AND question_id = @question_id
;
