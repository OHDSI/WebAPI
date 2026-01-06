INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'prediction:import:post', 'Import PLP analyses'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'estimation:import:post', 'Import PLE analyses');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp.value IN (
  'prediction:import:post',
  'estimation:import:post'
) AND sr.name IN ('Atlas users');