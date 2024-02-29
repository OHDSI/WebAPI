INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'),
       'shiny:download:*:*:*:get',
       'Download Shiny Application presenting analysis results';

INSERT INTO ${ohdsiSchema}.sec_permission (id, value, description)
SELECT nextval('${ohdsiSchema}.sec_permission_id_seq'),
       'shiny:publish:*:*:*:get',
       'Publish Shiny Application presenting analysis results to external resource';

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission sp,
     ${ohdsiSchema}.sec_role sr
WHERE sp."value" in
      (
          'shiny:download:*:*:*:get',
          'shiny:publish:*:*:*:get'
      )
  AND sr.name IN ('Atlas users');