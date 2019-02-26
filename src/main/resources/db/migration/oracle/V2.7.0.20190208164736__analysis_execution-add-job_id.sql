ALTER TABLE ${ohdsiSchema}.analysis_execution ADD job_execution_id NUMBER(19);
ALTER TABLE ${ohdsiSchema}.analysis_execution DROP COLUMN analysis_type;

ALTER TABLE ${ohdsiSchema}.analysis_execution MODIFY ANALYSIS_ID DEFAULT NULL NULL;

DROP TRIGGER ${ohdsiSchema}.ANALYSIS_EXECUTION_BIR;

alter table ${ohdsiSchema}.analysis_execution drop column analysis_id;
alter table ${ohdsiSchema}.analysis_execution drop column duration;
alter table ${ohdsiSchema}.analysis_execution drop column executed;
alter table ${ohdsiSchema}.analysis_execution drop column sec_user_id;
alter table ${ohdsiSchema}.analysis_execution drop column source_id;
alter table ${ohdsiSchema}.analysis_execution drop column update_password;

alter table ${ohdsiSchema}.analysis_execution rename to ee_analysis_status;
