ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria
  ADD COLUMN created_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN created_date TIMESTAMP,
  ADD COLUMN modified_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN modified_date TIMESTAMP;