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

DECLARE v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM all_constraints WHERE
    UPPER(constraint_name) = 'FK_SIF_CCA_EXECUTION' AND UPPER(owner) = UPPER('${ohdsiSchema}');
  IF (v_exists > 0) THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ${ohdsiSchema}.output_files DROP CONSTRAINT fk_sif_cca_execution';
  END IF;
END;
/

DECLARE v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_TAB_COLS WHERE
    UPPER(table_name) = 'OUTPUT_FILES' AND UPPER(column_name) = 'CCA_EXECUTION_ID'
    AND UPPER(owner) = UPPER('${ohdsiSchema}');
  IF (v_exists > 0) THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ${ohdsiSchema}.output_files DROP COLUMN cca_execution_id';
  END IF;
END;
/

DECLARE v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM all_constraints WHERE
    UPPER(constraint_name) = 'FK_SOF_CCA_EXECUTION' AND UPPER(owner) = UPPER('${ohdsiSchema}');
  IF (v_exists > 0) THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ${ohdsiSchema}.input_files DROP CONSTRAINT fk_sof_cca_execution';
  END IF;
END;
/

DECLARE v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_TAB_COLS WHERE
    UPPER(table_name) = 'INPUT_FILES' AND UPPER(column_name) = 'CCA_EXECUTION_ID'
    AND UPPER(owner) = UPPER('${ohdsiSchema}');
  IF (v_exists > 0) THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ${ohdsiSchema}.input_files DROP COLUMN cca_execution_id';
  END IF;
END;
/

DECLARE v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_TAB_COLS WHERE
    UPPER(table_name) = 'OUTPUT_FILES' AND UPPER(column_name) = 'EXECUTION_ID'
    AND UPPER(owner) = UPPER('${ohdsiSchema}');
  IF (v_exists = 0) THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ${ohdsiSchema}.output_files ADD execution_id NUMBER(10)';
  END IF;
END;
/

DECLARE v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM ALL_TAB_COLS WHERE
    UPPER(table_name) = 'INPUT_FILES' AND UPPER(column_name) = 'EXECUTION_ID'
    AND UPPER(owner) = UPPER('${ohdsiSchema}');
  IF (v_exists = 0) THEN
    EXECUTE IMMEDIATE 'ALTER TABLE ${ohdsiSchema}.input_files ADD execution_id NUMBER(10)';
  END IF;
END;
/

DECLARE v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM all_sequences WHERE
    UPPER(sequence_name) = 'OUTPUT_FILE_SEQ' AND UPPER(sequence_owner) = UPPER('${ohdsiSchema}');
  IF (v_exists = 0) THEN
    EXECUTE IMMEDIATE 'CREATE SEQUENCE ${ohdsiSchema}.output_file_seq';
  END IF;
END;
/


DECLARE v_exists NUMBER;
BEGIN
  SELECT COUNT(*) INTO v_exists FROM all_sequences WHERE
    UPPER(sequence_name) = 'INPUT_FILE_SEQ' AND UPPER(sequence_owner) = UPPER('${ohdsiSchema}');
  IF (v_exists = 0) THEN
    EXECUTE IMMEDIATE 'CREATE SEQUENCE ${ohdsiSchema}.input_file_seq';
  END IF;
END;
/