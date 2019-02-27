INSERT INTO ${ohdsiSchema}.sec_permission(id, "value", "description")
    VALUES
      (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'prediction:*:generation:*:post', 'Execute Prediction Generation Job'),
      (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'prediction:*:generation:get', 'View Prediction Generations'),
      (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'prediction:generation:*:result:get', 'View Prediction Generation Results');

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" in ('prediction:*:generation:*:post', 'prediction:*:generation:get', 'prediction:generation:*:result:get')
                       AND sr.name IN ('Atlas users');

INSERT INTO ${ohdsiSchema}.sec_permission(id, "value", "description")
  VALUES
    (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'estimation:*:generation:*:post', 'Execute Estimation Generation Job'),
    (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'estimation:*:generation:get', 'View Estimation Generations'),
    (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'estimation:generation:*:result:get', 'View Estimation Generation Results');

INSERT INTO ${ohdsiSchema}.sec_role_permission (role_id, permission_id)
  SELECT sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp."value" in ('estimation:*:generation:*:post', 'estimation:*:generation:get', 'estimation:generation:*:result:get')
        AND sr.name IN ('Atlas users');