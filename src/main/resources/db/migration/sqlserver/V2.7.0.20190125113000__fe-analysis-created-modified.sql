ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria
  ADD created_by_id INT NULL, CONSTRAINT FK_fac_su_cid FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria
  ADD created_date DATETIME CONSTRAINT DF_fe_analysis_criteria_cd DEFAULT(GETDATE());

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria
  ADD modified_by_id INT NULL, CONSTRAINT FK_fac_su_mid FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria
  ADD modified_date DATETIME;