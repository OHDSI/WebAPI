ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN created_by_id;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN created_date;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN modified_by_id;
ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria DROP COLUMN modified_date;

ALTER TABLE ${ohdsiSchema}.fe_analysis ADD created_by_id NUMBER(10) NULL CONSTRAINT FK_fa_su_cid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.fe_analysis ADD created_date TIMESTAMP WITH TIME ZONE;
ALTER TABLE ${ohdsiSchema}.fe_analysis ADD modified_by_id NUMBER(10) NULL CONSTRAINT FK_fa_su_mid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.fe_analysis ADD modified_date TIMESTAMP WITH TIME ZONE;