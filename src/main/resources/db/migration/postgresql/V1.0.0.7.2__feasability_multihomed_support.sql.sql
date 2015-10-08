ALTER TABLE feasibility_study DROP CONSTRAINT FK_feasibility_study_feas_study_generation_info;

CREATE TABLE tmp_ms_xx_feas_study_generation_info (
    study_id           INT      NOT NULL,
    source_id          INT      NOT NULL,
    start_time         Timestamp(3) NULL,
    execution_duration INT      NULL,
    status             INT      NOT NULL,
    is_valid           Boolean      NOT NULL,
    CONSTRAINT tmp_ms_xx_constraint_PK_feas_study_generation_info PRIMARY KEY (study_id,source_id)
);

INSERT INTO tmp_ms_xx_feas_study_generation_info (study_id, source_id, start_time, execution_duration, status, is_valid)
SELECT   study_id,
         1,
         start_time,
         execution_duration,
         status,
         is_valid
FROM feas_study_generation_info;

DROP TABLE feas_study_generation_info;

ALTER TABLE tmp_ms_xx_feas_study_generation_info RENAME TO feas_study_generation_info;

ALTER INDEX tmp_ms_xx_constraint_PK_feas_study_generation_info RENAME TO PK_feas_study_generation_info;

ALTER TABLE feasibility_study DROP COLUMN generate_info_id;

ALTER TABLE feas_study_generation_info
  ADD CONSTRAINT FK_feas_study_generation_info_feasibility_study FOREIGN KEY (study_id) REFERENCES feasibility_study (id);
