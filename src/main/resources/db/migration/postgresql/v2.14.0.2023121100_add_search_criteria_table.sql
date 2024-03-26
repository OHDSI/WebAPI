CREATE TABLE ${ohdsiSchema}.concept_set_criteria
(
    concept_set_id integer NOT NULL,
    criteria character varying,
    PRIMARY KEY (concept_set_id),
    CONSTRAINT "conceptSetFk" FOREIGN KEY (concept_set_id)
        REFERENCES ${ohdsiSchema}.concept_set (concept_set_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
        NOT VALID
);
