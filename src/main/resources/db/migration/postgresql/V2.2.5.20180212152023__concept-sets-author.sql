ALTER TABLE ${ohdsiSchema}.concept_set
  ADD COLUMN IF NOT EXISTS created_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS modified_by VARCHAR(255),
  ADD COLUMN IF NOT EXISTS created_date TIMESTAMP WITH TIME ZONE,
  ADD COLUMN IF NOT EXISTS modified_date TIMESTAMP WITH TIME ZONE;