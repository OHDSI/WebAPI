-- [UNTESTED] Add mode_id to cohort_inclusion_result table
DECLARE
  v_col_exists NUMBER 
BEGIN
  SELECT count(*) INTO v_col_exists
    FROM user_tab_cols
    WHERE column_name = 'mode_id'
      AND table_name = 'cohort_inclusion_result';
 
   IF (v_col_exists = 0) THEN
      EXECUTE IMMEDIATE 'ALTER TABLE cohort_inclusion_result ADD mode_id INT NOT NULL DEFAULT 0';
   ELSE
    DBMS_OUTPUT.PUT_LINE('The column mode_id already exists');
  END IF;
END;
/

-- [UNTESTED] Add mode_id to cohort_inclusion_stats table
DECLARE
  v_col_exists NUMBER 
BEGIN
  SELECT count(*) INTO v_col_exists
    FROM user_tab_cols
    WHERE column_name = 'mode_id'
      AND table_name = 'cohort_inclusion_stats';
 
   IF (v_col_exists = 0) THEN
      EXECUTE IMMEDIATE 'ALTER TABLE cohort_inclusion_stats ADD mode_id INT NOT NULL DEFAULT 0';
   ELSE
    DBMS_OUTPUT.PUT_LINE('The column mode_id already exists');
  END IF;
END;
/

-- [UNTESTED] Add mode_id to cohort_summary_stats table
DECLARE
  v_col_exists NUMBER 
BEGIN
  SELECT count(*) INTO v_col_exists
    FROM user_tab_cols
    WHERE column_name = 'mode_id'
      AND table_name = 'cohort_summary_stats';
 
   IF (v_col_exists = 0) THEN
      EXECUTE IMMEDIATE 'ALTER TABLE cohort_summary_stats ADD mode_id INT NOT NULL DEFAULT 0';
   ELSE
    DBMS_OUTPUT.PUT_LINE('The column mode_id already exists');
  END IF;
END;
/