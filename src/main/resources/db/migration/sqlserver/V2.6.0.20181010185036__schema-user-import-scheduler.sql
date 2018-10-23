CREATE SEQUENCE ${ohdsiSchema}.user_import_job_seq START WITH 0;

CREATE TABLE ${ohdsiSchema}.user_import_job(
  id BIGINT NOT NULL CONSTRAINT df_user_import_job_id DEFAULT NEXT VALUE FOR ${ohdsiSchema}.user_import_job_seq,
  is_enabled BIT NOT NULL DEFAULT 0,
  start_date DATETIMEOFFSET,
  frequency VARCHAR(100) NOT NULL,
  recurring_times INTEGER NOT NULL,
  recurring_until_date DATETIMEOFFSET,
  cron  VARCHAR(255) NOT NULL,
  last_executed_at DATETIMEOFFSET,
  executed_times INTEGER DEFAULT 0 NOT NULL,
  is_closed BIT DEFAULT 0 NOT NULL,
  provider_type VARCHAR(100) NOT NULL,
  preserve_roles BIT NOT NULL DEFAULT 1,
  CONSTRAINT pk_user_import_job PRIMARY KEY(id)
);

CREATE TABLE ${ohdsiSchema}.user_import_job_weekdays(
  user_import_job_id BIGINT NOT NULL,
  day_of_week VARCHAR(100) NOT NULL,
  CONSTRAINT pk_user_import_job_weekdays PRIMARY KEY(user_import_job_id, day_of_week)
);

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'user:import:job:get', 'List user import jobs'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'user:import:job:post', 'Create new user import job'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'user:import:job:*:put', 'Update user import job'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'user:import:job:*:get', 'Get user import job'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'user:import:job:*:delete', 'Delete user import job'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'user:import:job:*:history:get', 'Get user import history');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'user:import:job:get',
    'user:import:job:post',
    'user:import:job:*:put',
    'user:import:job:*:get',
    'user:import:job:*:delete',
    'user:import:job:*:history:get')
  AND sr.name IN ('admin');

CREATE VIEW ${ohdsiSchema}.user_import_job_history
  AS
    SELECT
      job.job_execution_id as id,
      job.start_time as start_time,
      job.end_time as end_time,
      job.status as status,
      job.exit_code as exit_code,
      job.exit_message as exit_message,
      name_param.STRING_VAL as job_name,
      provider_param.STRING_VAL as provider_type,
      author_param.STRING_VAL as author
    FROM
      ${ohdsiSchema}.BATCH_JOB_EXECUTION job
      JOIN ${ohdsiSchema}.BATCH_JOB_INSTANCE instance ON instance.JOB_INSTANCE_ID = job.JOB_INSTANCE_ID
      JOIN ${ohdsiSchema}.batch_job_execution_params name_param
        ON job.job_execution_id = name_param.job_execution_id AND name_param.KEY_NAME = 'jobName'
      JOIN ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS provider_param
        ON job.JOB_EXECUTION_ID = provider_param.JOB_EXECUTION_ID AND provider_param.KEY_NAME = 'provider'
      JOIN ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS author_param
        ON job.JOB_EXECUTION_ID = author_param.JOB_EXECUTION_ID AND author_param.KEY_NAME = 'jobAuthor'
    WHERE
      instance.JOB_NAME = 'usersImport'