ALTER TABLE ${ohdsiSchema}.analysis_execution ADD job_execution_id BIGINT;

alter table ${ohdsiSchema}.analysis_execution drop column analysis_id;
ALTER TABLE ${ohdsiSchema}.analysis_execution DROP COLUMN analysis_type;
alter table ${ohdsiSchema}.analysis_execution drop column duration;
alter table ${ohdsiSchema}.analysis_execution drop column executed;
alter table ${ohdsiSchema}.analysis_execution drop column sec_user_id;
alter table ${ohdsiSchema}.analysis_execution drop column source_id;
alter table ${ohdsiSchema}.analysis_execution drop column update_password;

alter table ${ohdsiSchema}.analysis_execution rename to ee_analysis_status;
