-- DROP any existing objects from previous runs
-- for Oracle only, this section is commented out for reference in case they need to be run manually
-- the other DBMS create scripts can dynamically drop objects instead
-- DROP TABLE concept_set;
-- DROP SEQUENCE concept_set_sequence;
-- DROP TABLE concept_set_item;
-- DROP SEQUENCE concept_set_item_sequence;

CREATE SEQUENCE concept_set_sequence MAXVALUE 9223372036854775807 NOCYCLE;
CREATE TABLE concept_set (
    concept_set_id   INTEGER NOT NULL,
    concept_set_name VARCHAR(255) NOT NULL
);

CREATE SEQUENCE concept_set_item_sequence MAXVALUE 9223372036854775807 NOCYCLE;
CREATE TABLE concept_set_item (
    concept_set_item_id INTEGER NOT NULL,
    concept_set_id      INTEGER NOT NULL,
    concept_id          INTEGER NOT NULL,
    is_excluded         INTEGER NOT NULL,
    include_descendants INTEGER NOT NULL,
    include_mapped      INTEGER NOT NULL
);
