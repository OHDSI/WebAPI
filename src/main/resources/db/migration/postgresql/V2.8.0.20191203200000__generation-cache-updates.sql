ALTER TABLE ${ohdsiSchema}.generation_cache DROP COLUMN result_identifier;
ALTER TABLE ${ohdsiSchema}.generation_cache ALTER COLUMN design_hash TYPE integer USING design_hash::integer;