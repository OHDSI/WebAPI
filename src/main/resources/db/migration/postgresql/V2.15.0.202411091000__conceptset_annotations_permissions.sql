DELETE FROM ${ohdsiSchema}.sec_role_permission
WHERE permission_id IN (
    SELECT id FROM ${ohdsiSchema}.sec_permission
    WHERE value like '%:annotation:%'
);
DELETE FROM ${ohdsiSchema}.sec_permission
WHERE value like '%:annotation:%';

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:annotation:put', 'Create Concept Set Annotation');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:%s:annotation:get', 'List Concept Set Annotation');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:annotation:get', 'View Concept Set Annotation');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:%s:annotation:*:delete', 'Delete Owner`s Concept Set Annotations');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:annotation:*:delete', 'Delete Any Concept Set Annotation');


INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:*:annotation:put',
	'conceptset:*:annotation:*:delete',
	'conceptset:%s:annotation:*:delete',
    'conceptset:%s:annotation:get',
    'conceptset:*:annotation:get'
    ) AND sr.name IN ('admin');


INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:*:annotation:put',
	'conceptset:%s:annotation:*:delete',
    'conceptset:%s:annotation:get',
    'conceptset:*:annotation:get'
    ) AND sr.name IN ('Atlas users');

ALTER TABLE ${ohdsiSchema}.concept_set_annotation ALTER COLUMN concept_set_version TYPE INTEGER USING (concept_set_version::integer);
