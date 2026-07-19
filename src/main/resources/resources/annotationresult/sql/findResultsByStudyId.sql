SELECT *
FROM @results_schema.annotation_result s
WHERE s.study_id in (@studyId)
;