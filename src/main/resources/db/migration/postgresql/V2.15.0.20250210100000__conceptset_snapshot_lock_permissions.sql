

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:snapshot:post', 'Invoke Concept Set Snapshot Action';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:snapshots:get', 'List Concept Set Snapshots';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:*:snapshot:get', 'Get current Concept Set Snapshot';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:locked:post', 'Batch-Check Concept Set Locked Snapshots';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'), 'conceptset:snapshot-items:post', 'Fetch Concept Set Snapshot Items';


INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:*:snapshot:post',
    'conceptset:*:snapshots:get',
    'conceptset:*:snapshot:get',
    'conceptset:locked:post',
    'conceptset:snapshot-items:post'
    ) AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
    'conceptset:*:snapshots:get',
    'conceptset:*:snapshot:get',
    'conceptset:locked:post',
    'conceptset:snapshot-items:post'
    ) AND sr.name IN ('Atlas users');
