INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
  SELECT
    nextval('${ohdsiSchema}.sec_permission_id_seq'),
    'source:*:daimons:*:set-priority:post',
    'Set priority daimons';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT
    sr.id,
    sp.id
  FROM ${ohdsiSchema}.sec_permission sp,
    ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'source:*:daimons:*:set-priority:post'
        AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'source:post', 'Create source');
INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp,
    ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'source:post' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'source:*:put', 'Edit source');
INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp,
    ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'source:*:put' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'source:*:delete', 'Delete source');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'source:*:delete' AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES(nextval('${ohdsiSchema}.sec_permission_id_seq'), 'source:details:*:get', 'Read source details');
INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" = 'source:details:*:get' AND sr.name IN ('admin');
