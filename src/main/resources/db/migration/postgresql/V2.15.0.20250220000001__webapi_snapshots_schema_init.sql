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

DO $$
DECLARE
    permission RECORD;
    new_permission_id INTEGER;
    new_permission_value VARCHAR;
    new_role_permission_id INTEGER;
BEGIN
    FOR permission IN
        SELECT p.id AS permission_id, p.value AS permission_value, rp.role_id AS role_id
        FROM ${ohdsiSchema}.sec_permission p
        INNER JOIN ${ohdsiSchema}.sec_role_permission rp
            ON p.id = rp.permission_id
        WHERE p.value ~ '^conceptset:[0-9]+:put$'
    LOOP
        new_permission_value := 'conceptset:' || split_part(permission.permission_value, ':', 2) || ':snapshot:post';

        IF NOT EXISTS (
            SELECT 1
            FROM ${ohdsiSchema}.sec_permission
            WHERE "value" = new_permission_value
        )
        THEN
            new_permission_id := nextval('${ohdsiSchema}.sec_permission_id_seq');

            INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
            VALUES (new_permission_id, new_permission_value, 'Permission to create snapshot for concept set');

            RAISE NOTICE 'Inserted New Permission: % (ID: %)', new_permission_value, new_permission_id;
        ELSE
            SELECT id INTO new_permission_id
            FROM ${ohdsiSchema}.sec_permission
            WHERE "value" = new_permission_value;

            RAISE NOTICE 'Permission Already Exists: % (ID: %)', new_permission_value, new_permission_id;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM ${ohdsiSchema}.sec_role_permission
            WHERE role_id = permission.role_id AND permission_id = new_permission_id
        )
        THEN
            new_role_permission_id := nextval('${ohdsiSchema}.sec_role_permission_sequence');

            INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
            VALUES (new_role_permission_id, permission.role_id, new_permission_id);

            RAISE NOTICE 'Mapped New Permission to Role: % (Role ID: %)', new_permission_value, permission.role_id;
        ELSE
            RAISE NOTICE 'Mapping Already Exists: Permission: % - Role ID: %', new_permission_value, permission.role_id;
        END IF;
    END LOOP;
END $$;