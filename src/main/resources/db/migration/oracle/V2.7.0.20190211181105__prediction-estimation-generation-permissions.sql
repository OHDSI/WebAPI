INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
      SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:*:generation:*:post', 'Execute Prediction Generation Job' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
      SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:*:generation:get', 'View Prediction Generations' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
      SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'prediction:generation:*:result:get', 'View Prediction Generation Results' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp.value in ('prediction:*:generation:*:post', 'prediction:*:generation:get', 'prediction:generation:*:result:get')
                       AND sr.name IN ('Atlas users');

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:*:generation:*:post', 'Execute Estimation Generation Job' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:*:generation:get', 'View Estimation Generations' FROM dual;
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description)
  SELECT ${ohdsiSchema}.sec_permission_id_seq.nextval, 'estimation:generation:*:result:get', 'View Estimation Generation Results' FROM dual;

INSERT INTO ${ohdsiSchema}.sec_role_permission (id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission sp, ${ohdsiSchema}.sec_role sr
  WHERE sp.value in ('estimation:*:generation:*:post', 'estimation:*:generation:get', 'estimation:generation:*:result:get')
        AND sr.name IN ('Atlas users');