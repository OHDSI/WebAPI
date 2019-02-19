if object_id('${ohdsiSchema}.analysis_execution_id_seq', 'SO') IS NULL
  CREATE SEQUENCE ${ohdsiSchema}.analysis_execution_id_seq START WITH 1;
GO

if object_id('${ohdsiSchema}.pk_analysis_exec', 'PK') IS NOT NULL
  ALTER TABLE ${ohdsiSchema}.analysis_execution DROP CONSTRAINT pk_analysis_exec;
GO
ALTER TABLE ${ohdsiSchema}.analysis_execution ADD id_tmp INT;
GO
UPDATE ${ohdsiSchema}.analysis_execution SET id_tmp = id;
ALTER TABLE ${ohdsiSchema}.analysis_execution DROP COLUMN id;
GO
EXEC sp_rename '${ohdsiSchema}.analysis_execution.id_tmp', 'id', 'COLUMN';
GO
ALTER TABLE ${ohdsiSchema}.analysis_execution ALTER COLUMN id INT NOT NULL;
GO
ALTER TABLE ${ohdsiSchema}.analysis_execution ADD CONSTRAINT pk_analysis_exec PRIMARY KEY (id);
GO

if object_id('${ohdsiSchema}.input_file_seq', 'SO') IS NULL
    CREATE SEQUENCE ${ohdsiSchema}.input_file_seq START WITH 1;
GO

if object_id('${ohdsiSchema}.output_file_seq', 'SO') IS NULL
    CREATE SEQUENCE ${ohdsiSchema}.output_file_seq START WITH 1;
GO

if object_id('${ohdsiSchema}.fk_sof_cca_execution', 'F') IS NOT NULL
    ALTER TABLE ${ohdsiSchema}.input_files DROP CONSTRAINT fk_sof_cca_execution;
GO
ALTER TABLE ${ohdsiSchema}.input_files ALTER COLUMN cca_execution_id INT NULL;
GO

if object_id('${ohdsiSchema}.fk_sif_cca_execution', 'F') IS NOT NULL
    ALTER TABLE ${ohdsiSchema}.output_files DROP CONSTRAINT fk_sif_cca_execution;
GO
ALTER TABLE ${ohdsiSchema}.output_files ALTER COLUMN cca_execution_id INT NULL;
GO

INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'executionservice:execution:status:*:get', 'Read PLE/PLP execution status'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'executionservice:execution:results:*:get', 'Download PLE/PLP execution results'),
  (NEXT VALUE FOR ${ohdsiSchema}.sec_permission_id_seq, 'executionservice:*:*:executions:get', 'List PLE/PLP executions');

INSERT INTO ${ohdsiSchema}.sec_role_permission(id, role_id, permission_id)
  SELECT NEXT VALUE FOR ${ohdsiSchema}.sec_role_permission_sequence, sr.id, sp.id
  FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
  WHERE sp.value IN (
    'executionservice:execution:status:*:get',
    'executionservice:execution:results:*:get',
    'executionservice:*:*:executions:get'
  ) AND sr.name IN ('Atlas users');