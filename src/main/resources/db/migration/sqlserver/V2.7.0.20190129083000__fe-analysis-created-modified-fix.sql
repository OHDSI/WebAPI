ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN created_by_id;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN created_date;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN modified_by_id;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN modified_date;

ALTER TABLE ${ohdsiSchema}.fe_analysis
  ADD created_by_id INT NULL, CONSTRAINT FK_fa_su_cid FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

ALTER TABLE ${ohdsiSchema}.fe_analysis
  ADD created_date DATETIME CONSTRAINT DF_fe_analysis_cd DEFAULT(GETDATE());

ALTER TABLE ${ohdsiSchema}.fe_analysis
  ADD modified_by_id INT NULL, CONSTRAINT FK_fa_su_mid FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

ALTER TABLE ${ohdsiSchema}.fe_analysis
  ADD modified_date DATETIME;