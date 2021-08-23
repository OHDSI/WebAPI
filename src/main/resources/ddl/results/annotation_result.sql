IF OBJECT_ID('@results_schema.annotation_result', 'U') IS NULL
CREATE TABLE @results_schema.annotation_result(
    annotation_id integer NOT NULL,
    question_id integer NOT NULL,
    answer_id bigint NOT NULL,
    study_id int NOT NULL,
    value VARCHAR(255) NOT NULL,
    type VARCHAR(255) NOT NULL,
    last_update_time datetime DEFAULT GETDATE()
);
