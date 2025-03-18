INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:generation:*:temporalresult:get', 'Get cohort characterization generation temporal results');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" IN (
                     'cohort-characterization:generation:*:temporalresult:get'
    )
  AND sr.name IN ('Atlas users');