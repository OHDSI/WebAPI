ALTER TABLE ${ohdsiSchema}.cca
  ADD COLUMN created_by VARCHAR(255),
  ADD COLUMN modified_by VARCHAR(255);