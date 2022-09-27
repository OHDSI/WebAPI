ALTER TABLE ${ohdsiSchema}.source ADD COLUMN is_cache_enabled boolean;
UPDATE ${ohdsiSchema}.source set is_cache_enabled = true;
ALTER TABLE ${ohdsiSchema}.source ALTER COLUMN is_cache_enabled SET NOT NULL;
