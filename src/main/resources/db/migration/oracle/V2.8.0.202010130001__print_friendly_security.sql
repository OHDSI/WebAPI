INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION)
  VALUES (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohortdefinition:printfriendly:cohort:post', 'Get print-friendly HTML of cohort expression');
INSERT INTO ${ohdsiSchema}.SEC_PERMISSION (ID, VALUE, DESCRIPTION)
  VALUES (${ohdsiSchema}.sec_permission_id_seq.nextval, 'cohortdefinition:printfriendly:conceptsets:post', 'Get print-friendly HTML of conceptset list');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'cohortdefinition:printfriendly:cohort:post',
    'cohortdefinition:printfriendly:conceptsets:post'
  ) AND sr.name IN ('Atlas users');