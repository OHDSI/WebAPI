INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'user:me:get', 'Get info about current user (myself)');

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT
    ${ohdsiSchema}.SEC_ROLE_PERMISSION_SEQUENCE.nextval,
    sr.id,
    sp.id
  FROM ${ohdsiSchema}.sec_permission sp,
    ${ohdsiSchema}.sec_role sr
  WHERE sp.value = 'user:me:get'
        AND sr.name IN ('public');
