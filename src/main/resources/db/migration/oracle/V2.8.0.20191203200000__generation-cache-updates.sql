ALTER TABLE ${ohdsiSchema}.generation_cache DROP CONSTRAINT uq_gc_result;
ALTER TABLE ${ohdsiSchema}.generation_cache DROP COLUMN result_identifier;
ALTER TABLE ${ohdsiSchema}.generation_cache DROP CONSTRAINT uq_gc_hash;
ALTER TABLE ${ohdsiSchema}.generation_cache ADD design_hash_int INTEGER;
UPDATE ${ohdsiSchema}.generation_cache SET design_hash_int = TO_NUMBER(design_hash);
ALTER TABLE ${ohdsiSchema}.generation_cache DROP COLUMN design_hash;
ALTER TABLE ${ohdsiSchema}.generation_cache RENAME COLUMN design_hash_int TO design_hash;
ALTER TABLE ${ohdsiSchema}.generation_cache MODIFY design_hash NOT NULL;
ALTER TABLE ${ohdsiSchema}.generation_cache ADD CONSTRAINT uq_gc_hash UNIQUE (type, design_hash, source_id);
