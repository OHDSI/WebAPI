CREATE SEQUENCE ${ohdsiSchema}.user_import_sequence;

CREATE TABLE ${ohdsiSchema}.user_import (
  id INTEGER,
  provider VARCHAR(255) NOT NULL,
  preserveRoles NUMBER(1) NOT NULL,
  userRoles CLOB,
  roleGroupMapping CLOB,
  CONSTRAINT PK_user_import PRIMARY KEY (id)
);

DROP VIEW ${ohdsiSchema}.user_import_job_history;

CREATE OR REPLACE VIEW ${ohdsiSchema}.user_import_job_history
  AS
    SELECT
      job.job_execution_id as id,
      job.start_time as start_time,
      job.end_time as end_time,
      job.status as status,
      job.exit_code as exit_code,
      job.exit_message as exit_message,
      name_param.STRING_VAL as job_name,
      author_param.STRING_VAL as author,
      CAST(CAST(user_import_param.string_val AS VARCHAR2(50)) AS NUMBER(10)) user_import_id
    FROM
      ${ohdsiSchema}.BATCH_JOB_EXECUTION job
      JOIN ${ohdsiSchema}.BATCH_JOB_INSTANCE instance ON instance.JOB_INSTANCE_ID = job.JOB_INSTANCE_ID
      JOIN ${ohdsiSchema}.batch_job_execution_params name_param
        ON job.job_execution_id = name_param.job_execution_id AND name_param.KEY_NAME = 'jobName'
      JOIN ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS user_import_param
        ON job.JOB_EXECUTION_ID = user_import_param.JOB_EXECUTION_ID AND user_import_param.KEY_NAME = 'user_import_id'
      JOIN ${ohdsiSchema}.BATCH_JOB_EXECUTION_PARAMS author_param
        ON job.JOB_EXECUTION_ID = author_param.JOB_EXECUTION_ID AND author_param.KEY_NAME = 'jobAuthor'
    WHERE
      instance.JOB_NAME = 'usersImport';