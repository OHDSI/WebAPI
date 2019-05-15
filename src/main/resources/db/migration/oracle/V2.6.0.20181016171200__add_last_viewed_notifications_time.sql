ALTER TABLE ${ohdsiSchema}.sec_user ADD last_viewed_notifications_time TIMESTAMP WITH TIME ZONE;

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'notifications:viewed:post', 'Remember last viewed notification timestamp' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'notifications:viewed:get', 'Get last viewed notification timestamp' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'notifications:get', 'Get notifications' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
SELECT ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval, sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'notifications:viewed:post',
  'notifications:viewed:get',
  'notifications:get'
)
AND sr.name IN ('Atlas users');
