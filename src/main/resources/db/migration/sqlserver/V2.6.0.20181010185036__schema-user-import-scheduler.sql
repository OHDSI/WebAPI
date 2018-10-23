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