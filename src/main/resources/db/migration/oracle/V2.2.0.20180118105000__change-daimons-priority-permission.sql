INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
values (${ohdsiSchema}.sec_permission_id_seq.nextval, 'source:*:daimons:*:set-priority:post', 'Set priority daimons');

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT
    ${ohdsiSchema}.sec_role_permission_sequence.NEXTVAL,
    sr.id,
    sp.id
  FROM ${ohdsiSchema}.sec_permission sp,
    ${ohdsiSchema}.sec_role sr
  WHERE sp.value = 'source:*:daimons:*:set-priority:post'
        AND sr.name IN ('admin');