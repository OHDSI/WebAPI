ALTER TABLE cohort_generation_info DROP CONSTRAINT FK_cohort_generation_info_cohort_definition;

CREATE TABLE tmp_ms_xx_cohort_generation_info (
    id                 INT      NOT NULL,
    source_id          INT      NOT NULL,
    start_time         Timestamp(3) NULL,
    execution_duration INT      NULL,
    status             INT      NOT NULL,
    is_valid           Boolean      NOT NULL,
    CONSTRAINT tmp_ms_xx_constraint_PK_cohort_generation_info PRIMARY KEY (id, source_id)
);

INSERT INTO tmp_ms_xx_cohort_generation_info (id, source_id, start_time, execution_duration, status, is_valid)
SELECT  id,
        1,
        start_time,
        execution_duration,
        status,
        is_valid
FROM  cohort_generation_info;

DROP TABLE cohort_generation_info;

ALTER TABLE tmp_ms_xx_cohort_generation_info RENAME TO cohort_generation_info;

ALTER INDEX tmp_ms_xx_constraint_PK_cohort_generation_info RENAME TO PK_cohort_generation_info;

ALTER TABLE cohort_generation_info
  ADD CONSTRAINT FK_cohort_generation_info_cohort_definition 
  FOREIGN KEY (id) REFERENCES cohort_definition (id) 
  ON DELETE CASCADE ON UPDATE CASCADE;
