ALTER TABLE ${ohdsiSchema}.generation_cache DROP CONSTRAINT uq_gc_result;
ALTER TABLE ${ohdsiSchema}.generation_cache DROP COLUMN result_identifier;
ALTER TABLE ${ohdsiSchema}.generation_cache DROP CONSTRAINT uq_gc_hash;
ALTER TABLE ${ohdsiSchema}.generation_cache ALTER COLUMN design_hash INTEGER;
ALTER TABLE ${ohdsiSchema}.generation_cache ADD CONSTRAINT uq_gc_hash UNIQUE (type, design_hash, source_id);
