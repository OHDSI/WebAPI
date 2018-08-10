DELETE FROM ${ohdsiSchema}.CONCEPT_SET_GENERATION_INFO;

DROP TABLE ${ohdsiSchema}.CONCEPT_SET_NEGATIVE_CONTROLS;
DROP SEQUENCE ${ohdsiSchema}.negative_controls_sequence;
CREATE SEQUENCE ${ohdsiSchema}.negative_controls_sequence;
CREATE TABLE ${ohdsiSchema}.CONCEPT_SET_NEGATIVE_CONTROLS (
    id INTEGER NOT NULL DEFAULT NEXTVAL('negative_controls_sequence'),
    evidence_job_id BIGINT NOT NULL,
    source_id INTEGER NOT NULL,
    concept_set_id INTEGER NOT NULL,
    CONSTRAINT PK_CONCEPT_SET_NC PRIMARY KEY (id)
);