ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN  created_by_id;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN  created_date;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN  modified_by_id;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN  modified_date;

ALTER TABLE ${ohdsiSchema}.fe_analysis
  ADD COLUMN  created_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN  created_date TIMESTAMP,
  ADD COLUMN  modified_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN  modified_date TIMESTAMP;