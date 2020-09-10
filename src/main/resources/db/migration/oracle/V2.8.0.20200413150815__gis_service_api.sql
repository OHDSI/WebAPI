INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'gis:cohort:*:bounds:*:get', 'Get gis bounds for source');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'gis:cohort:*:clusters:*:get', 'Get gis clusters for source');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'gis:cohort:*:density:*:get', 'Get gis density for source');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'gis:person:*:bounds:*:get', 'Get bounds for person');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'gis:cohort:*:bounds:*:get',
    'gis:cohort:*:clusters:*:get',
    'gis:cohort:*:density:*:get',
    'gis:person:*:bounds:*:get'
  ) AND sr.name IN ('Atlas users');