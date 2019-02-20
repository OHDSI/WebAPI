DECLARE v_col_exist NUMBER;
BEGIN
  SELECT count(*) INTO v_col_exist FROM all_tab_cols WHERE
    UPPER(column_name) = 'JOB_EXECUTION_ID'
    AND UPPER(table_name) = 'ANALYSIS_EXECUTION' AND owner = '${ohdsiSchema}';
  IF (v_col_exist = 0) THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ${ohdsiSchema}.analysis_execution ADD job_execution_id NUMBER(19)';
  END IF;
END;
/

DECLARE v_col_exist NUMBER;
BEGIN
  SELECT count(*) INTO v_col_exist FROM all_tab_cols WHERE
    UPPER(column_name) = 'ANALYSIS_TYPE'
    AND UPPER(table_name) = 'ANALYSIS_EXECUTION' AND owner = '${ohdsiSchema}';
  IF (v_col_exist > 0) THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ${ohdsiSchema}.analysis_execution DROP COLUMN analysis_type';
  END IF;
END;
/

DECLARE v_col_exist NUMBER;
BEGIN
  SELECT count(*) INTO v_col_exist FROM all_tab_cols WHERE
    UPPER(column_name) = 'MEDIA_TYPE'
    AND UPPER(table_name) = 'OUTPUT_FILES' AND owner = '${ohdsiSchema}';
  IF (v_col_exist = 0) THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ${ohdsiSchema}.output_files ADD media_type VARCHAR2(255)';
  END IF;
END;
/

ALTER TABLE ${ohdsiSchema}.analysis_execution MODIFY ANALYSIS_ID DEFAULT NULL NULL;

UPDATE ${ohdsiSchema}.analysis_execution SET sec_user_id = NULL
  WHERE NOT EXISTS(SELECT * FROM ${ohdsiSchema}.sec_user WHERE id = sec_user_id);

ALTER TABLE ${ohdsiSchema}.analysis_execution ADD CONSTRAINT fk_ae_sec_user FOREIGN KEY(sec_user_id)
  REFERENCES ${ohdsiSchema}.sec_user(id);

DROP TRIGGER ${ohdsiSchema}.ANALYSIS_EXECUTION_BIR;