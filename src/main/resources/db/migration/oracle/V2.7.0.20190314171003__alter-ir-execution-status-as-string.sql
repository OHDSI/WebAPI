ALTER TABLE ${ohdsiSchema}.ir_execution RENAME COLUMN status TO int_status;

ALTER TABLE ${ohdsiSchema}.ir_execution ADD status VARCHAR(128);

UPDATE ${ohdsiSchema}.ir_execution SET status = CASE int_status
    WHEN -1 THEN 'ERROR'
    WHEN 0 THEN 'PENDING'
    WHEN 1 THEN 'RUNNING'
    WHEN 2 THEN 'COMPLETE'
    END;

ALTER TABLE ${ohdsiSchema}.ir_execution DROP COLUMN int_status;