ALTER TABLE ${ohdsiSchema}.sec_user ADD last_viewed_notifications_time DATETIMEOFFSET;

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
VALUES
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'notifications:viewed:post', 'Remember last viewed notification timestamp'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'notifications:viewed:get', 'Get last viewed notification timestamp'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'notifications:get', 'Get notifications');


INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'notifications:viewed:post',
  'notifications:viewed:get',
  'notifications:get'
)
AND sr.name IN ('Atlas users');
