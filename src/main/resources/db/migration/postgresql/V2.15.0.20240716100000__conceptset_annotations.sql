CREATE SEQUENCE ${ohdsiSchema}.concept_set_annotation_sequence;

CREATE TABLE ${ohdsiSchema}.concept_set_annotation
(
    concept_set_annotation_id int4 NOT NULL DEFAULT nextval('${ohdsiSchema}.concept_set_annotation_sequence'),
    concept_set_id integer NOT NULL,
    concept_id integer,
    annotation_details VARCHAR,
    vocabulary_version VARCHAR,
    created_by_id  INTEGER,
    created_date   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (now()),
    modified_by_id INTEGER,
    modified_date  TIMESTAMP WITH TIME ZONE,
    concept_set_version VARCHAR(100),
    copied_from_concept_set_ids VARCHAR(1000),
    CONSTRAINT pk_concept_set_annotation_id PRIMARY KEY (concept_set_annotation_id),
    CONSTRAINT fk_concept_set FOREIGN KEY (concept_set_id)
        REFERENCES ${ohdsiSchema}.concept_set (concept_set_id)
        ON DELETE CASCADE
);

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:annotation:put', 'Create Concept Set Annotation');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:annotation:*:delete', 'Delete Concept Set Annotation');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:%s:annotation:get', 'List Concept Set Annotation');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:annotation:get', 'View Concept Set Annotation');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:copy-annotations', 'Copy Concept Set Annotations');


INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:*:annotation:put',
    'conceptset:annotation:*:delete',
    'conceptset:%s:annotation:get',
    'conceptset:*:annotation:get',
    'conceptset:copy-annotations'
    ) AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:*:annotation:put',
    'conceptset:%s:annotation:get',
    'conceptset:*:annotation:get',
    'conceptset:copy-annotations'
    ) AND sr.name IN ('Atlas users');