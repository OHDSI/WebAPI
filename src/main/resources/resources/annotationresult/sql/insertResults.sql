INSERT INTO @results_schema.annotation_result (annotation_id, question_id, answer_id, value, type,study_id)
VALUES (@annotation_id, @question_id, @answer_id, @value, @type, @study_id)
;
