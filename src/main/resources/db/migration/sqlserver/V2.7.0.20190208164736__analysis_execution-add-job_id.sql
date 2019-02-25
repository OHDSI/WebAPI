IF COL_LENGTH('${ohdsiSchema}.analysis_execution', 'job_execution_id') IS NULL
  ALTER TABLE ${ohdsiSchema}.analysis_execution ADD job_execution_id BIGINT;
GO

alter table ${ohdsiSchema}.analysis_execution drop column analysis_id, analysis_type, duration, sec_user_id, executed;
GO

alter table ${ohdsiSchema}.analysis_execution add constraint fk_ee_analysis_source
  foreign key(source_id) references ${ohdsiSchema}.source(source_id);

exec sp_rename '${ohdsiSchema}.analysis_execution', 'ee_analysis_status';
GO