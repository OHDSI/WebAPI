CREATE SEQUENCE ${ohdsiSchema}.CONCEPT_SET_SNAPSHOT_METADATA_SEQUENCE MAXVALUE 9223372036854775807 NO CYCLE;

CREATE SEQUENCE ${ohdsiSchema}.CONCEPT_SET_ITEM_SNAPSHOTS_SEQUENCE
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 9223372036854775807
    NO CYCLE;

CREATE SEQUENCE ${ohdsiSchema}.INCLUDED_CONCEPTS_SNAPSHOTS_SEQUENCE
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 9223372036854775807
    NO CYCLE;

CREATE SEQUENCE ${ohdsiSchema}.INCLUDED_SOURCE_CODES_SNAPSHOTS_SEQUENCE
    START WITH 1
    INCREMENT BY 1
    MAXVALUE 9223372036854775807
    NO CYCLE;

CREATE TABLE ${ohdsiSchema}.CONCEPT_SET_SNAPSHOT_METADATA (
    ID INTEGER NOT NULL PRIMARY KEY DEFAULT NEXTVAL('${ohdsiSchema}.CONCEPT_SET_SNAPSHOT_METADATA_SEQUENCE'),
    CONCEPT_SET_ID INTEGER NOT NULL,
    ACTION VARCHAR(10),
    LOCKED_DATE TIMESTAMP,
    LOCKED_BY VARCHAR(200),
    MESSAGE VARCHAR(2000),
    VOCABULARY_BUNDLE_NAME VARCHAR(200),
    VOCABULARY_BUNDLE_SCHEMA VARCHAR(200),
    VOCABULARY_BUNDLE_VERSION VARCHAR(200),
    CONCEPT_SET_VERSION VARCHAR(200)
);

CREATE TABLE ${ohdsiSchema}.CONCEPT_SET_ITEM_SNAPSHOTS (
    ID INTEGER NOT NULL PRIMARY KEY DEFAULT NEXTVAL('${ohdsiSchema}.CONCEPT_SET_ITEM_SNAPSHOTS_SEQUENCE'),
    SNAPSHOT_METADATA_ID INTEGER NOT NULL,
    CONCEPT_ID INTEGER NOT NULL,
    CONCEPT_NAME VARCHAR(255) NOT NULL,
    DOMAIN_ID VARCHAR(20) NOT NULL,
    VOCABULARY_ID VARCHAR(20) NOT NULL,
    CONCEPT_CLASS_ID VARCHAR(20) NOT NULL,
    STANDARD_CONCEPT VARCHAR(1),
    CONCEPT_CODE VARCHAR(50) NOT NULL,
    VALID_START_DATE DATE NOT NULL,
    VALID_END_DATE DATE NOT NULL,
    INVALID_REASON VARCHAR(1),
    IS_EXCLUDED INTEGER NOT NULL,
    INCLUDE_DESCENDANTS INTEGER NOT NULL,
    INCLUDE_MAPPED INTEGER NOT NULL,
    FOREIGN KEY (SNAPSHOT_METADATA_ID) REFERENCES ${ohdsiSchema}.CONCEPT_SET_SNAPSHOT_METADATA(ID)
);

CREATE TABLE ${ohdsiSchema}.INCLUDED_CONCEPTS_SNAPSHOTS (
    ID INTEGER NOT NULL PRIMARY KEY DEFAULT NEXTVAL('${ohdsiSchema}.INCLUDED_CONCEPTS_SNAPSHOTS_SEQUENCE'),
    SNAPSHOT_METADATA_ID INTEGER NOT NULL,
    CONCEPT_ID INTEGER NOT NULL,
    CONCEPT_NAME VARCHAR(255) NOT NULL,
    DOMAIN_ID VARCHAR(20) NOT NULL,
    VOCABULARY_ID VARCHAR(20) NOT NULL,
    CONCEPT_CLASS_ID VARCHAR(20) NOT NULL,
    STANDARD_CONCEPT VARCHAR(1),
    CONCEPT_CODE VARCHAR(50) NOT NULL,
    VALID_START_DATE DATE NOT NULL,
    VALID_END_DATE DATE NOT NULL,
    INVALID_REASON VARCHAR(1),
    FOREIGN KEY (SNAPSHOT_METADATA_ID) REFERENCES ${ohdsiSchema}.CONCEPT_SET_SNAPSHOT_METADATA(ID)
);

CREATE TABLE ${ohdsiSchema}.INCLUDED_SOURCE_CODES_SNAPSHOTS (
    ID INTEGER NOT NULL PRIMARY KEY DEFAULT NEXTVAL('${ohdsiSchema}.INCLUDED_SOURCE_CODES_SNAPSHOTS_SEQUENCE'),
    SNAPSHOT_METADATA_ID INTEGER NOT NULL,
    CONCEPT_ID INTEGER NOT NULL,
    CONCEPT_NAME VARCHAR(255) NOT NULL,
    DOMAIN_ID VARCHAR(20) NOT NULL,
    VOCABULARY_ID VARCHAR(20) NOT NULL,
    CONCEPT_CLASS_ID VARCHAR(20) NOT NULL,
    STANDARD_CONCEPT VARCHAR(1),
    CONCEPT_CODE VARCHAR(50) NOT NULL,
    VALID_START_DATE DATE NOT NULL,
    VALID_END_DATE DATE NOT NULL,
    INVALID_REASON VARCHAR(1),
    FOREIGN KEY (SNAPSHOT_METADATA_ID) REFERENCES ${ohdsiSchema}.CONCEPT_SET_SNAPSHOT_METADATA(ID)
);

-- This script inserts a new permission of type "conceptset:%s:snapshot:post" and maps it to the existing roles if such permission does not exist
--for each concept set which has a "conceptset:%s:put" permission. This is made to allow making snapshot actions to old concept sets
--which were created before the snapshot/lock feature was deployed

DROP TABLE IF EXISTS temp_migration;

CREATE TEMP TABLE temp_migration (
    from_perm_id INT,
    new_value CHARACTER VARYING(255)
);

INSERT INTO temp_migration (from_perm_id, new_value)
SELECT 
    p.id AS from_perm_id,
    'conceptset:' || split_part(p.value, ':', 2) || ':snapshot:post' AS new_value
FROM 
    ${ohdsiSchema}.sec_permission p
WHERE 
    p.value ~ '^conceptset:[0-9]+:put$';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT 
    nextval('${ohdsiSchema}.sec_permission_id_seq'),
    tm.new_value AS value,
    'Permission to create snapshot for concept set'
FROM 
    temp_migration tm
LEFT JOIN 
    ${ohdsiSchema}.sec_permission sp ON tm.new_value = sp.value
WHERE 
    sp.id IS NULL;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
SELECT 
    nextval('${ohdsiSchema}.sec_role_permission_sequence'),
    srp.role_id,
    sp.id AS permission_id
FROM 
    temp_migration tm
JOIN 
    ${ohdsiSchema}.sec_permission sp ON tm.new_value = sp.value
JOIN 
    ${ohdsiSchema}.sec_role_permission srp ON tm.from_perm_id = srp.permission_id
LEFT JOIN 
    ${ohdsiSchema}.sec_role_permission rp ON srp.role_id = rp.role_id AND sp.id = rp.permission_id
WHERE 
    rp.id IS NULL;

DROP TABLE temp_migration;