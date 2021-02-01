CREATE TABLE annotation_set (
    set_id integer NOT NULL,
    cohort_name character varying(150) NOT NULL,
    cohort_source character varying(25) NOT NULL,
    cohort_id integer NOT NULL,
    name character varying(150) NOT NULL,
    -- date_created date NOT NULL,
    -- date_updated date,
    CONSTRAINT annotation_set_pkey PRIMARY KEY (set_id)
);

ALTER TABLE annotation_set OWNER TO ohdsi_admin;

CREATE TABLE annotation (
    annotation_id integer NOT NULL,
    subject_id integer NOT NULL,
    cohort_id integer NOT NULL,
    set_id integer NOT NULL,
    user_id integer,
    CONSTRAINT annotation_pkey PRIMARY KEY (annotation_id),
    CONSTRAINT annotation_set_fk FOREIGN KEY (set_id) REFERENCES annotation_set(set_id),
    CONSTRAINT unq_subject_cohort_set UNIQUE(subject_id, cohort_id, set_id)
);

ALTER TABLE annotation OWNER TO ohdsi_admin;


CREATE TABLE annotation_question (
    question_id integer NOT NULL,
    set_id integer NOT NULL,
    question_name character varying(250) NOT NULL,
    question_type character varying(25) NOT NULL,
    required Boolean NOT NULL,
    case_question Boolean NOT NULL,
    help_text text,
    -- date_created date NOT NULL,
    -- date_updated date,
    CONSTRAINT annotation_questions_pkey PRIMARY KEY (question_id),
    CONSTRAINT annotation_question_set_fk FOREIGN KEY (set_id) REFERENCES annotation_set(set_id)
);

ALTER TABLE annotation_question OWNER TO ohdsi_admin;


CREATE TABLE annotation_answer (
    answer_id integer NOT NULL,
    question_id integer NOT NULL,
    -- set_id integer NOT NULL,
    text character varying(250) NOT NULL,
    value character varying(250) NOT NULL,
    help_text text,
    -- date_created date NOT NULL,
    -- date_updated date,
    CONSTRAINT annotation_answer_pkey PRIMARY KEY (answer_id),
    CONSTRAINT annotation_answer_fk FOREIGN KEY (question_id) REFERENCES annotation_question(question_id)
    -- CONSTRAINT annotation_answer_set_fk FOREIGN KEY (set_id) REFERENCES annotation_set(set_id)
);

ALTER TABLE annotation_answer OWNER TO ohdsi_admin;


CREATE TABLE annotation_result (
  result_id integer NOT NULL,
  annotation_id integer NOT NULL,
  set_id integer NOT NULL,
  question_id integer NOT NULL,
  answer_id bigint NOT NULL,
  subject_id integer NOT NULL,
  sample_name character varying(100) NOT NULL,
  value character varying(250) NOT NULL,
  type character varying(50) NOT NULL,
  -- author_id integer NOT NULL,
  -- date_reviewed date NOT NULL,
  CONSTRAINT annotation_result_pkey PRIMARY KEY (result_id),
  CONSTRAINT annotation_result_question_fk FOREIGN KEY (question_id)
      REFERENCES annotation_question (question_id) MATCH SIMPLE
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
  -- CONSTRAINT annotation_result_author_fk FOREIGN KEY (author_id)
  --     REFERENCES annotation_author (author_id) MATCH SIMPLE
  --     ON UPDATE NO ACTION
  --     ON DELETE NO ACTION,
  CONSTRAINT annotation_result_as_fk FOREIGN KEY (set_id)
      REFERENCES annotation_set(set_id)
      ON UPDATE NO ACTION
      ON DELETE NO ACTION,
  CONSTRAINT annotation_result_ans_fk FOREIGN KEY (answer_id)
      REFERENCES annotation_answer(answer_id)
      ON UPDATE NO ACTION
      ON DELETE NO ACTION
);

ALTER TABLE annotation_result OWNER TO ohdsi_admin;
