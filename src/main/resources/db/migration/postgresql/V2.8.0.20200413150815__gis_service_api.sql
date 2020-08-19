INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'gis:cohort:*:bounds:*:get', 'Get gis bounds for source');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'gis:cohort:*:clusters:*:get', 'Get gis clusters for source');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'gis:cohort:*:density:*:get', 'Get gis density for source');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'gis:person:*:bounds:*:get', 'Get bounds for person');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'gis:cohort:*:bounds:*:get',
    'gis:cohort:*:clusters:*:get',
    'gis:cohort:*:density:*:get',
    'gis:person:*:bounds:*:get'
  ) AND sr.name IN ('Atlas users');