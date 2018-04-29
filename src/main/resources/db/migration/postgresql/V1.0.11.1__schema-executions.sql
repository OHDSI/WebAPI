CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.analysis_execution (
  id              SERIAL,
  analysis_id     INTEGER NOT NULL,
  analysis_type   VARCHAR NOT NULL,
  duration        INTEGER NOT NULL,
  executed        TIMESTAMP(3),
  sec_user_id     INTEGER,
  executionStatus VARCHAR,
  update_password VARCHAR,
  source_id       INTEGER
);

INSERT INTO ${ohdsiSchema}.analysis_execution (analysis_id, analysis_type, duration, executed, sec_user_id, source_id, executionStatus, update_password)
  SELECT
    cca.cca_id,
    'CCA',
    cca.execution_duration,
    cca.executed,
    cca.sec_user_id,
    cca.source_id,
    CASE
    WHEN cca.execution_status = 0
      THEN 'PENDING'
    WHEN cca.execution_status = 1
      THEN 'STARTED'
    WHEN cca.execution_status = 2
      THEN 'RUNNING'
    WHEN cca.execution_status = 3
      THEN 'COMPLETED'
    WHEN cca.execution_status = 4
      THEN 'FAILED'
    END AS status,
    ext.update_password
  FROM
    ${ohdsiSchema}.cca_execution cca LEFT JOIN ${ohdsiSchema}.cca_execution_ext ext ON ext.cca_execution_id = cca.cca_id
  WHERE NOT EXISTS(SELECT 1
                   FROM ${ohdsiSchema}.analysis_execution
                   WHERE analysis_id = cca.cca_id
                         AND analysis_type = 'CCA'
                         AND duration = cca.execution_duration
                         AND executed = cca.executed
                         AND sec_user_id = cca.sec_user_id
                         AND source_id = cca.source_id
  )
;

ALTER TABLE ${ohdsiSchema}.input_files
  ADD COLUMN execution_id INTEGER;

UPDATE ${ohdsiSchema}.input_files f
SET execution_id =
(SELECT a.id
 FROM ${ohdsiSchema}.analysis_execution a
   JOIN ${ohdsiSchema}.cca_execution cca
     ON cca.cca_id = a.analysis_id AND a.analysis_type = 'CCA' AND a.executed = cca.executed
 WHERE cca.cca_execution_id = f.cca_execution_id
 LIMIT 1);

ALTER TABLE ${ohdsiSchema}.output_files
  ADD COLUMN execution_id INTEGER;

UPDATE ${ohdsiSchema}.output_files f
SET execution_id =
(SELECT a.id
 FROM ${ohdsiSchema}.analysis_execution a
   JOIN ${ohdsiSchema}.cca_execution cca
     ON cca.cca_id = a.analysis_id AND a.analysis_type = 'CCA' AND a.executed = cca.executed
 WHERE cca.cca_execution_id = f.cca_execution_id
 LIMIT 1);

ALTER TABLE ${ohdsiSchema}.input_files
  ALTER COLUMN cca_execution_id DROP NOT NULL;

ALTER TABLE ${ohdsiSchema}.output_files
  ALTER COLUMN cca_execution_id DROP NOT NULL;