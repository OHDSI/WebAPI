alter table ${ohdsiSchema}.analysis_execution drop constraint fk_ae_sec_user;

alter table ${ohdsiSchema}.analysis_execution drop column duration;
alter table ${ohdsiSchema}.analysis_execution drop column sec_user_id;
alter table ${ohdsiSchema}.analysis_execution drop column executed;

alter table ${ohdsiSchema}.analysis_execution add constraint fk_ee_analysis_source
  foreign key(source_id) references ${ohdsiSchema}.source(source_id);

alter table ${ohdsiSchema}.analysis_execution rename to ee_analysis_status;
