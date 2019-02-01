ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN IF EXISTS created_by_id;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN IF EXISTS created_date;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN IF EXISTS modified_by_id;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN IF EXISTS modified_date;

ALTER TABLE ${ohdsiSchema}.fe_analysis
  ADD COLUMN IF NOT EXISTS created_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN IF NOT EXISTS created_date TIMESTAMP,
  ADD COLUMN IF NOT EXISTS modified_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN IF NOT EXISTS modified_date TIMESTAMP;