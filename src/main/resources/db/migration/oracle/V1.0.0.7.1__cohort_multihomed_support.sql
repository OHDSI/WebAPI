ALTER TABLE cohort_generation_info DROP CONSTRAINT FK_cgi_cd;

DROP TABLE cohort_generation_info;

CREATE TABLE cohort_generation_info (
    id                 Number(10)      NOT NULL,
    source_id          Number(10)      NOT NULL,
    start_time         Timestamp(3) NULL,
    execution_duration Number(10)      NULL,
    status             Number(10)      NOT NULL,
    is_valid           Number(1)      NOT NULL,
    CONSTRAINT PK_cohort_generation_info PRIMARY KEY (id, source_id),
    CONSTRAINT FK_cgi_cd FOREIGN KEY(id)
      REFERENCES cohort_definition (id)
      ON DELETE CASCADE
);
