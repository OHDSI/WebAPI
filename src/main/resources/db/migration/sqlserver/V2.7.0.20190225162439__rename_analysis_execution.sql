alter table ${ohdsiSchema}.analysis_execution drop constraint fk_ae_sec_user;

alter table ${ohdsiSchema}.analysis_execution drop column duration, sec_user_id, executed;

alter table ${ohdsiSchema}.analysis_execution add constraint fk_ee_analysis_source
  foreign key(source_id) references ${ohdsiSchema}.source(source_id);

exec sp_rename '${ohdsiSchema}.analysis_execution', 'ee_analysis_status';
GO
