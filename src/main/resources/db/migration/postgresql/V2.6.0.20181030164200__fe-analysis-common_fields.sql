ALTER TABLE ${ohdsiSchema}.fe_analysis
  ADD COLUMN created_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id),
  ADD COLUMN modified_by_id INTEGER REFERENCES ${ohdsiSchema}.sec_user(id);

ALTER TABLE ${ohdsiSchema}.fe_analysis ADD created_date Timestamp(3);
ALTER TABLE ${ohdsiSchema}.fe_analysis ADD modified_date Timestamp(3);
