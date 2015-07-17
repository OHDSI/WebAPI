ALTER TABLE feasibility_study DROP CONSTRAINT FK_fs_fsgi;

DROP TABLE feas_study_generation_info;

CREATE TABLE feas_study_generation_info (
    study_id           INT      NOT NULL,
    source_id          INT      NOT NULL,
    start_time         DATETIME NULL,
    execution_duration INT      NULL,
    status             INT      NOT NULL,
    is_valid           BIT      NOT NULL,
    CONSTRAINT PK_fs_gen_info PRIMARY KEY (study_id,source_id),
    CONSTRAINT FK_fsgi_fs FOREIGN KEY (study_id) REFERENCES feasibility_study (id)
);

ALTER TABLE feasibility_study DROP COLUMN generate_info_id;
