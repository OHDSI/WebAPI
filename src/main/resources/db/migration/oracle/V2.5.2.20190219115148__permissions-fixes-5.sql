CREATE SEQUENCE ${ohdsiSchema}.analysis_execution_id_seq START WITH 1;

CREATE SEQUENCE ${ohdsiSchema}.input_file_seq START WITH 1;

CREATE SEQUENCE ${ohdsiSchema}.output_file_seq START WITH 1;

ALTER TABLE ${ohdsiSchema}.input_files DROP CONSTRAINT fk_sof_cca_execution;

ALTER TABLE ${ohdsiSchema}.input_files MODIFY (cca_execution_id NULL);

ALTER TABLE ${ohdsiSchema}.output_files DROP CONSTRAINT fk_sif_cca_execution;

ALTER TABLE ${ohdsiSchema}.output_files MODIFY (cca_execution_id NULL);

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'executionservice:execution:status:*:get', 'Read PLE/PLP execution status');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'executionservice:execution:results:*:get', 'Download PLE/PLP execution results');
INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (${ohdsiSchema}.sec_permission_id_seq.nextval, 'executionservice:*:*:executions:get', 'List PLE/PLP executions');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT ${ohdsiSchema}.sec_role_permission_sequence.nextval, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'executionservice:execution:status:*:get',
    'executionservice:execution:results:*:get',
    'executionservice:*:*:executions:get'
  ) AND sr.name IN ('Atlas users');