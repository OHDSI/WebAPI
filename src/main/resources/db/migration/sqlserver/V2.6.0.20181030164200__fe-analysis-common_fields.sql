ALTER TABLE ${ohdsiSchema}.fe_analysis ADD created_by_id INT NULL, CONSTRAINT FK_cs_su_cid FOREIGN KEY (created_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.fe_analysis ADD modified_by_id INT NULL, CONSTRAINT FK_cs_su_mid FOREIGN KEY (modified_by_id) REFERENCES ${ohdsiSchema}.sec_user(id);

ALTER TABLE ${ohdsiSchema}.fe_analysis ADD created_date DATETIME;
ALTER TABLE ${ohdsiSchema}.fe_analysis ADD modified_date DATETIME;
