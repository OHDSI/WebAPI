select concept_id from @vocabulary_database_schema.CONCEPT c
inner join @temp_table t on t.concept_code = c.concept_code and t.vocabulary_id = c.vocabulary_id