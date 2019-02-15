ALTER TABLE ${ohdsiSchema}.analysis_execution ADD job_execution_id NUMBER(19);
ALTER TABLE ${ohdsiSchema}.analysis_execution DROP COLUMN analysis_type;
ALTER TABLE ${ohdsiSchema}.output_files ADD media_type VARCHAR2(255);
ALTER TABLE ${ohdsiSchema}.analysis_execution MODIFY ANALYSIS_ID DEFAULT NULL NULL;

UPDATE ${ohdsiSchema}.analysis_execution SET sec_user_id = NULL
  WHERE NOT EXISTS(SELECT * FROM ${ohdsiSchema}.sec_user WHERE id = sec_user_id);

ALTER TABLE ${ohdsiSchema}.analysis_execution ADD CONSTRAINT fk_ae_sec_user FOREIGN KEY(sec_user_id)
  REFERENCES ${ohdsiSchema}.sec_user(id);

DROP TRIGGER ${ohdsiSchema}.ANALYSIS_EXECUTION_BIR;