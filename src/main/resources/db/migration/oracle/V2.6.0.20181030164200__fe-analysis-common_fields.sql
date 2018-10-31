ALTER TABLE ${ohdsiSchema}.fe_analysis ADD created_by_id NUMBER(10) NULL CONSTRAINT FK_fe_cid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.fe_analysis ADD modified_by_id NUMBER(10) NULL CONSTRAINT FK_fe_mid REFERENCES ${ohdsiSchema}.sec_user(id);

ALTER TABLE ${ohdsiSchema}.fe_analysis ADD created_date Timestamp(3);
ALTER TABLE ${ohdsiSchema}.fe_analysis ADD modified_date Timestamp(3);
