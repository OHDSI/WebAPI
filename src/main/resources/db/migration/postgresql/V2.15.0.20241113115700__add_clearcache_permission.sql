INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'),
       'cdmresults:clearcache:post',
       'Clear the Achilles and CDM results caches';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp,
     ${ohdsiSchema}.sec_role sr
WHERE sp."value" in
      (
       'cdmresults:clearcache:post'
      )
  AND sr.name IN ('admin');

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'),
       'cdmresults:*:clearcache:post',
       'Clear the Achilles and CDM results caches';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp,
     ${ohdsiSchema}.sec_role sr
WHERE sp."value" in
      (
       'cdmresults:*:clearcache:post'
      )
  AND sr.name IN ('admin');
