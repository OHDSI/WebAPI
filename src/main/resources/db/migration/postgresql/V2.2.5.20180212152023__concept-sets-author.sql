ALTER TABLE ${ohdsiSchema}.concept_set
  ADD COLUMN created_by VARCHAR(255),
  ADD COLUMN modified_by VARCHAR(255),
  ADD COLUMN created_date TIMESTAMP WITH TIME ZONE,
  ADD COLUMN modified_date TIMESTAMP WITH TIME ZONE;