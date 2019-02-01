CREATE TABLE ${ohdsiSchema}.analysis_execution(
  id INTEGER IDENTITY,
  analysis_id INTEGER NOT NULL,
  analysis_type VARCHAR(250) NOT NULL,
  duration INTEGER NOT NULL,
  executed DATETIME,
  sec_user_id INTEGER,
  executionStatus VARCHAR(250),
  update_password VARCHAR(250),
  source_id INTEGER
);

DELETE FROM ${ohdsiSchema}.analysis_execution WHERE analysis_type = 'CCA';

INSERT INTO ${ohdsiSchema}.analysis_execution(analysis_id, analysis_type, duration, executed, sec_user_id, source_id, executionStatus, update_password)
    SELECT cca.cca_id, 'CCA', cca.execution_duration, cca.executed, cca.sec_user_id, cca.source_id,
      CASE cca.execution_status
        WHEN 0 THEN 'PENDING'
        WHEN 1 THEN 'STARTED'
        WHEN 2 THEN 'RUNNING'
        WHEN 3 THEN 'COMPLETED'
        WHEN 4 THEN 'FAILED'
      END as status, ext.update_password FROM
        ${ohdsiSchema}.cca_execution cca LEFT JOIN ${ohdsiSchema}.cca_execution_ext ext ON ext.cca_execution_id = cca.cca_id;

UPDATE ${ohdsiSchema}.input_files SET execution_id =
  (SELECT TOP 1 a.id FROM ${ohdsiSchema}.analysis_execution a JOIN ${ohdsiSchema}.cca_execution cca
    ON cca.cca_id = a.analysis_id AND a.analysis_type = 'CCA' AND a.executed = cca.executed
    WHERE cca.cca_execution_id = ${ohdsiSchema}.input_files.cca_execution_id);

UPDATE ${ohdsiSchema}.output_files SET execution_id =
  (SELECT TOP 1 a.id FROM ${ohdsiSchema}.analysis_execution a JOIN ${ohdsiSchema}.cca_execution cca
    ON cca.cca_id = a.analysis_id AND a.analysis_type = 'CCA' AND a.executed = cca.executed
    WHERE cca.cca_execution_id = ${ohdsiSchema}.output_files.cca_execution_id);

ALTER TABLE ${ohdsiSchema}.input_files ALTER COLUMN cca_execution_id INTEGER NOT NULL;

ALTER TABLE ${ohdsiSchema}.output_files ALTER COLUMN cca_execution_id INTEGER NOT NULL;