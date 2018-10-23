CREATE SEQUENCE ${ohdsiSchema}.user_import_job_seq;

CREATE TABLE ${ohdsiSchema}.user_import_job(
  id BIGINT NOT NULL DEFAULT nextval('${ohdsiSchema}.user_import_job_seq'),
  is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
  start_date TIMESTAMP WITH TIME ZONE,
  frequency VARCHAR NOT NULL,
  recurring_times INTEGER NOT NULL,
  recurring_until_date TIMESTAMP WITH TIME ZONE,
  cron  VARCHAR NOT NULL,
  last_executed_at TIMESTAMP WITH TIME ZONE,
  executed_times INTEGER DEFAULT 0 NOT NULL,
  is_closed BOOLEAN DEFAULT FALSE NOT NULL,
  provider_type VARCHAR NOT NULL,
  preserve_roles BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT pk_user_import_job PRIMARY KEY(id)
);

CREATE TABLE ${ohdsiSchema}.user_import_job_weekdays(
  user_import_job_id BIGINT NOT NULL,
  day_of_week VARCHAR NOT NULL,
  CONSTRAINT pk_user_import_job_weekdays PRIMARY KEY(user_import_job_id, day_of_week)
);

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'user:import:job:get', 'List user import jobs'),
  (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'user:import:job:post', 'Create new user import job'),
  (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'user:import:job:*:put', 'Update user import job'),
  (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'user:import:job:*:get', 'Get user import job'),
  (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'user:import:job:*:delete', 'Delete user import job'),
  (NEXTVAL('${ohdsiSchema}.sec_permission_id_seq'), 'user:import:job:*:history:get', 'Get user import history');

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