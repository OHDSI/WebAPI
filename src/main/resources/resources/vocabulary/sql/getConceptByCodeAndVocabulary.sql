select concept_id from @vocabulary_database_schema.CONCEPT where (concept_code, vocabulary_id) in (@conceptColumns)
