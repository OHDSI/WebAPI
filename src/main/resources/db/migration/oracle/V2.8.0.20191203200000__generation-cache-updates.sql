ALTER TABLE ${ohdsiSchema}.generation_cache DROP COLUMN result_identifier;
ALTER TABLE ${ohdsiSchema}.generation_cache MODIFY design_hash INTEGER;