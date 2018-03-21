CREATE TABLE ${ohdsiSchema}.analysis_execution(
  id INTEGER NOT NULL,
  analysis_id INTEGER NOT NULL,
  analysis_type VARCHAR(255) NOT NULL,
  duration INTEGER NOT NULL,
  executed TIMESTAMP(3),
  sec_user_id INTEGER,
  executionStatus VARCHAR(255),
  update_password VARCHAR(255),
  source_id INTEGER
);

ALTER TABLE ${ohdsiSchema}.analysis_execution ADD (
  CONSTRAINT PK_analysis_execution PRIMARY KEY (id));

CREATE SEQUENCE ${ohdsiSchema}.analysis_execution_sequence START WITH 1;

CREATE OR REPLACE TRIGGER ${ohdsiSchema}.analysis_execution_bir
  BEFORE INSERT ON ${ohdsiSchema}.analysis_execution
  FOR EACH ROW
  BEGIN
    SELECT ${ohdsiSchema}.analysis_execution_sequence.nextval INTO :new.id FROM dual;
  END;

BEGIN

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

ALTER TABLE ${ohdsiSchema}.input_files ADD(execution_id INTEGER);

UPDATE ${ohdsiSchema}.input_files f SET execution_id =
  (SELECT a.id FROM ${ohdsiSchema}.analysis_execution a JOIN ${ohdsiSchema}.cca_execution cca
    ON cca.cca_id = a.analysis_id AND a.analysis_type = 'CCA' AND a.executed = cca.executed
    WHERE cca.cca_execution_id = f.cca_execution_id AND ROWNUM < 2);

ALTER TABLE ${ohdsiSchema}.output_files ADD(execution_id INTEGER);

UPDATE ${ohdsiSchema}.output_files f SET execution_id =
  (SELECT a.id FROM ${ohdsiSchema}.analysis_execution a JOIN ${ohdsiSchema}.cca_execution cca
    ON cca.cca_id = a.analysis_id AND a.analysis_type = 'CCA' AND a.executed = cca.executed
    WHERE cca.cca_execution_id = f.cca_execution_id AND ROWNUM < 2);

ALTER TABLE ${ohdsiSchema}.input_files MODIFY(cca_execution_id NULL);

ALTER TABLE ${ohdsiSchema}.output_files MODIFY(cca_execution_id NULL);

EXCEPTION WHEN OTHERS THEN
  IF SQLCODE != -942 THEN
    RAISE ;
  END IF;
END;