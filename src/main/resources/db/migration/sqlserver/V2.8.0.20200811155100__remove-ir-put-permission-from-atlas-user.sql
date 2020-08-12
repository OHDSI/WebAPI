DELETE FROM ${ohdsiSchema}.sec_role_permission WHERE
        permission_id in (SELECT id FROM ${ohdsiSchema}.sec_permission sp WHERE sp.value = 'ir:*:put')
        AND
        role_id in (SELECT id FROM ${ohdsiSchema}.sec_role sr WHERE sr.name = 'Atlas users');