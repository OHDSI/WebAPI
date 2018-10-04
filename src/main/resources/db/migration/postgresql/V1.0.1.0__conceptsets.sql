DROP TABLE IF EXISTS ${ohdsiSchema}.concept_set;
DROP SEQUENCE IF EXISTS ${ohdsiSchema}.concept_set_sequence;
CREATE SEQUENCE ${ohdsiSchema}.concept_set_sequence MAXVALUE 9223372036854775807 NO CYCLE;
CREATE TABLE ${ohdsiSchema}.concept_set (
    concept_set_id   INTEGER NOT NULL DEFAULT NEXTVAL('concept_set_sequence'),
    concept_set_name VARCHAR(255) NOT NULL
);

DROP TABLE IF EXISTS ${ohdsiSchema}.concept_set_item;
DROP SEQUENCE IF EXISTS ${ohdsiSchema}.concept_set_item_sequence;
CREATE SEQUENCE ${ohdsiSchema}.concept_set_item_sequence MAXVALUE 9223372036854775807 NO CYCLE;
CREATE TABLE ${ohdsiSchema}.concept_set_item (
    concept_set_item_id INTEGER NOT NULL DEFAULT NEXTVAL('concept_set_item_sequence'),
    concept_set_id      INTEGER NOT NULL,
    concept_id          INTEGER NOT NULL,
    is_excluded         INTEGER NOT NULL,
    include_descendants INTEGER NOT NULL,
    include_mapped      INTEGER NOT NULL
);
