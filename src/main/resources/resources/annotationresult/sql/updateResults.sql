UPDATE @results_schema.annotation_result
SET answer_id = @answer_id, value = @value, last_update_time=GETDATE()
WHERE annotation_id = @annotation_id AND question_id = @question_id
;
