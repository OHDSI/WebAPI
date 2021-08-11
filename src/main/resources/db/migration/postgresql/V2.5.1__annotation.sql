CREATE TABLE ${ohdsiSchema}.annotation_set (
    set_id integer NOT NULL,
    name  VARCHAR(255) NOT NULL,
    cohort_definition_id integer NOT NULL,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    date_updated TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_annotation_set_cohor_definition_id FOREIGN KEY (cohort_definition_id)
        REFERENCES ${ohdsiSchema}.cohort_definition (id) ON DELETE CASCADE,
    CONSTRAINT annotation_set_pkey PRIMARY KEY (set_id)
);

CREATE INDEX idx_cohort_annotation_set ON ${ohdsiSchema}.annotation_set (cohort_definition_id);

CREATE TABLE ${ohdsiSchema}.annotation_question (
    question_id integer NOT NULL,
    set_id integer NOT NULL,
    question_name  VARCHAR(255) NOT NULL,
    question_type  VARCHAR(50) NOT NULL,
    required Boolean NOT NULL,
    case_question Boolean NOT NULL,
    help_text text,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    date_updated TIMESTAMP WITH TIME ZONE,
    CONSTRAINT annotation_questions_pkey PRIMARY KEY (question_id),
    CONSTRAINT annotation_question_set_fk FOREIGN KEY (set_id)
        REFERENCES ${ohdsiSchema}.annotation_set(set_id) ON DELETE CASCADE
);

CREATE INDEX idx_annotation_set_question ON ${ohdsiSchema}.annotation_question (set_id);


CREATE TABLE ${ohdsiSchema}.annotation_answer (
    answer_id integer NOT NULL,
    question_id integer NOT NULL,
    text character varying(250) NOT NULL,
    value character varying(250) NOT NULL,
    help_text text,
    CONSTRAINT annotation_answer_pkey PRIMARY KEY (answer_id),
    CONSTRAINT annotation_answer_fk FOREIGN KEY (question_id)
        REFERENCES ${ohdsiSchema}.annotation_question(question_id) ON DELETE CASCADE
);

CREATE INDEX idx_annotation_answer_question ON ${ohdsiSchema}.annotation_question (question_id);

CREATE TABLE ${ohdsiSchema}.annotation (
    annotation_id integer NOT NULL,
    subject_id integer NOT NULL,
    cohort_sample_id integer NOT NULL,
    set_id integer NOT NULL,
    date_created TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    --user_id integer,
    CONSTRAINT annotation_pkey PRIMARY KEY (annotation_id),
    CONSTRAINT annotation_set_fk FOREIGN KEY (set_id)
        REFERENCES ${ohdsiSchema}.annotation_set(set_id),
    CONSTRAINT annotation_sample_fk FOREIGN KEY (cohort_sample_id)
            REFERENCES ${ohdsiSchema}.cohort_sample(id),
    CONSTRAINT unq_subject_sample_set UNIQUE(subject_id, cohort_sample_id, set_id)
);

CREATE INDEX idx_annotation_sample_subject ON ${ohdsiSchema}.annotation (cohort_sample_id,subject_id);
CREATE INDEX idx_annotation_set ON ${ohdsiSchema}.annotation (set_id);

CREATE TABLE ${ohdsiSchema}.annotation_study (
   study_id integer NOT NULL,
   cohort_sample_id integer NOT NULL,
   question_set_id integer NOT NULL,
   cohort_definition_id integer NOT NULL,
   source_id integer NOT NULL,
   CONSTRAINT annotation_study_pkey PRIMARY KEY (study_id),
   CONSTRAINT annotation_study_set_fk FOREIGN KEY (question_set_id)
       REFERENCES ${ohdsiSchema}.annotation_set(set_id),
   CONSTRAINT annotation_study_sample_fk FOREIGN KEY (cohort_sample_id)
       REFERENCES ${ohdsiSchema}.cohort_sample(id),
   CONSTRAINT annotation_study_definition_fk FOREIGN KEY (cohort_definition_id)
       REFERENCES ${ohdsiSchema}.cohort_definition(id),
   CONSTRAINT annotation_study_source_fk FOREIGN KEY (source_id)
       REFERENCES ${ohdsiSchema}.source(source_id)
);
