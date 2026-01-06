ALTER TABLE ${ohdsiSchema}.cohort_generation_info
  ADD COLUMN created_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id);