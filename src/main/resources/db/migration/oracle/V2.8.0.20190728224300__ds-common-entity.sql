ALTER TABLE ${ohdsiSchema}.source ADD created_by_id NUMBER(10) NULL CONSTRAINT FK_source_cid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.source ADD created_date TIMESTAMP WITH TIME ZONE;
ALTER TABLE ${ohdsiSchema}.source ADD modified_by_id NUMBER(10) NULL CONSTRAINT FK_source_mid REFERENCES ${ohdsiSchema}.sec_user(id);
ALTER TABLE ${ohdsiSchema}.source ADD modified_date TIMESTAMP WITH TIME ZONE;