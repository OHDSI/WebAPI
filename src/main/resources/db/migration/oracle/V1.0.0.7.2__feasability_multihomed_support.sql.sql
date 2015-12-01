ALTER TABLE feasibility_study DROP CONSTRAINT FK_fs_fsgi;

DROP TABLE feas_study_generation_info;

CREATE TABLE feas_study_generation_info (
    study_id           Number(10)      NOT NULL,
    source_id          Number(10)      NOT NULL,
    start_time         Timestamp(3) NULL,
    execution_duration Number(10)      NULL,
    status             Number(10)      NOT NULL,
    is_valid           Number(1)      NOT NULL,
    CONSTRAINT PK_fs_gen_info PRIMARY KEY (study_id,source_id),
    CONSTRAINT FK_fsgi_fs FOREIGN KEY (study_id) REFERENCES feasibility_study (id)
);

ALTER TABLE feasibility_study DROP COLUMN generate_info_id;
