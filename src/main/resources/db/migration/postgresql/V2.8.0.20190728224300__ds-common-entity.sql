ALTER TABLE ${ohdsiSchema}.source
  ADD COLUMN created_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN created_date DATE,
  ADD COLUMN modified_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN modified_date DATE;