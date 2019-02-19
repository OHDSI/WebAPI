CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.analysis_execution_id_seq;

CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.input_file_seq;

CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.output_file_seq;

ALTER TABLE ${ohdsiSchema}.input_files DROP CONSTRAINT IF EXISTS fk_sof_cca_execution;

ALTER TABLE ${ohdsiSchema}.input_files ALTER COLUMN cca_execution_id DROP NOT NULL;

ALTER TABLE ${ohdsiSchema}.output_files DROP CONSTRAINT IF EXISTS fk_sif_cca_execution;

ALTER TABLE ${ohdsiSchema}.output_files ALTER COLUMN cca_execution_id DROP NOT NULL;

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'executionservice:execution:status:*:get', 'Read PLE/PLP execution status'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'executionservice:execution:results:*:get', 'Download PLE/PLP execution results'),
  (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'executionservice:*:*:executions:get', 'List PLE/PLP executions');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT nextval('${ohdsiSchema}.sec_role_permission_sequence'), sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'executionservice:execution:status:*:get',
    'executionservice:execution:results:*:get',
    'executionservice:*:*:executions:get'
  ) AND sr.name IN ('Atlas users');