

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:snapshot:post', 'Invoke Concept Set Snapshot Action';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:list-snapshots:get', 'List Concept Set Snapshots';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:check-locked:post', 'Batch-Check Concept Set Locked Snapshots';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:get-snapshot-items:post', 'Fetch Concept Set Snapshot Items';


INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:*:snapshot:post',
    'conceptset:*:list-snapshots:get',
    'conceptset:check-locked:post',
    'conceptset:get-snapshot-items:post'
    ) AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:*:list-snapshots:get',
    'conceptset:check-locked:post',
    'conceptset:get-snapshot-items:post'
    ) AND sr.name IN ('Atlas users');
