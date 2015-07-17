ALTER TABLE cohort_generation_info DROP CONSTRAINT FK_cdd_cd;

DROP TABLE cohort_generation_info;

CREATE TABLE cohort_generation_info (
    id                 INT      NOT NULL,
    source_id          INT      NOT NULL,
    start_time         DATETIME NULL,
    execution_duration INT      NULL,
    status             INT      NOT NULL,
    is_valid           BIT      NOT NULL,
    CONSTRAINT PK_cohort_generation_info PRIMARY KEY (id, source_id),
    CONSTRAINT FK_cgi_cd FOREIGN KEY(id)
      REFERENCES cohort_definition (id)
      ON DELETE CASCADE
);
