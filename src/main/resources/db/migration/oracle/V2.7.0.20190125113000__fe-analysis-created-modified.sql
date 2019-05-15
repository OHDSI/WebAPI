ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD created_by_id NUMBER(10) NULL CONSTRAINT FK_fac_su_cid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD created_date TIMESTAMP WITH TIME ZONE;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD modified_by_id NUMBER(10) NULL CONSTRAINT FK_fac_su_mid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD modified_date TIMESTAMP WITH TIME ZONE;