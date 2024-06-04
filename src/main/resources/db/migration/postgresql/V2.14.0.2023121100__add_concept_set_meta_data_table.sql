CREATE SEQUENCE ${ohdsiSchema}.concept_set_meta_data_sequence;

CREATE TABLE ${ohdsiSchema}.concept_set_meta_data
(
    concept_set_meta_data_id int4 NOT NULL DEFAULT nextval('${ohdsiSchema}.concept_set_meta_data_sequence'),
    concept_set_id integer NOT NULL,
    concept_id integer,
    metadata varchar,
    created_by_id  INTEGER,
    created_date   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    modified_by_id INTEGER,
    modified_date  TIMESTAMP WITH TIME ZONE,
    CONSTRAINT pk_concept_set_meta_data_id PRIMARY KEY (concept_set_meta_data_id),
    CONSTRAINT fk_concept_set FOREIGN KEY (concept_set_id)
        REFERENCES ${ohdsiSchema}.concept_set (concept_set_id)
);
