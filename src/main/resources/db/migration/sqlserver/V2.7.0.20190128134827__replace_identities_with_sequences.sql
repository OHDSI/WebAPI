-- Table 'analysis_execution'
CREATE SEQUENCE ${ohdsiSchema}.analysis_execution_sequence;
GO

ALTER TABLE ${ohdsiSchema}.analysis_execution ADD id_tmp INT;
GO

UPDATE ${ohdsiSchema}.analysis_execution SET id_tmp = id;
GO

IF OBJECT_ID('${ohdsiSchema}.pk_analysis_exec', 'PK') IS NOT NULL
ALTER TABLE ${ohdsiSchema}.analysis_execution DROP CONSTRAINT pk_analysis_exec;
GO

ALTER TABLE ${ohdsiSchema}.analysis_execution DROP COLUMN id;
GO

EXEC sp_rename '[${ohdsiSchema}].analysis_execution.id_tmp', 'id', 'COLUMN';
GO

ALTER TABLE ${ohdsiSchema}.analysis_execution ALTER COLUMN id INT NOT NULL;
GO

ALTER TABLE ${ohdsiSchema}.analysis_execution
    ADD CONSTRAINT df_analysis_execution_id DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.analysis_execution_sequence) FOR id;
GO

ALTER TABLE ${ohdsiSchema}.analysis_execution ADD CONSTRAINT pk_analysis_exec PRIMARY KEY (id);
GO

DECLARE @analysis_execution_id_val INT;
DECLARE @sql NVARCHAR(MAX);
SELECT @analysis_execution_id_val = coalesce(MAX(id), 1) FROM ${ohdsiSchema}.analysis_execution;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.analysis_execution_sequence RESTART WITH ' + CAST(@analysis_execution_id_val as NVARCHAR(20)) + ';';
EXEC sp_executesql @sql;
GO

-- Table 'cca_execution'
CREATE SEQUENCE ${ohdsiSchema}.cca_execution_sequence;
GO

ALTER TABLE ${ohdsiSchema}.cca_execution ADD cca_execution_id_tmp INT;
GO

UPDATE ${ohdsiSchema}.cca_execution SET cca_execution_id_tmp = cca_execution_id;
GO

IF OBJECT_ID('${ohdsiSchema}.fk_sif_cca_execution') IS NOT NULL
ALTER TABLE ${ohdsiSchema}.output_files DROP CONSTRAINT fk_sif_cca_execution;
GO

IF OBJECT_ID('${ohdsiSchema}.fk_sof_cca_execution') IS NOT NULL
ALTER TABLE ${ohdsiSchema}.input_files DROP CONSTRAINT fk_sof_cca_execution;
GO

IF OBJECT_ID('${ohdsiSchema}.cca_execution_pk', 'PK') IS NOT NULL
ALTER TABLE ${ohdsiSchema}.cca_execution DROP CONSTRAINT cca_execution_pk;
GO

ALTER TABLE ${ohdsiSchema}.cca_execution DROP COLUMN cca_execution_id;
GO

EXEC sp_rename '[${ohdsiSchema}].cca_execution.cca_execution_id_tmp', 'cca_execution_id', 'COLUMN';
GO

ALTER TABLE ${ohdsiSchema}.cca_execution ALTER COLUMN cca_execution_id INT NOT NULL;
GO

ALTER TABLE ${ohdsiSchema}.cca_execution
    ADD CONSTRAINT df_cca_execution_id DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.cca_execution_sequence) FOR cca_execution_id;
GO

ALTER TABLE ${ohdsiSchema}.cca_execution ADD CONSTRAINT cca_execution_pk PRIMARY KEY (cca_execution_id);
GO

ALTER TABLE ${ohdsiSchema}.output_files
  ADD CONSTRAINT fk_sif_cca_execution FOREIGN KEY (cca_execution_id)
  REFERENCES ${ohdsiSchema}.cca_execution (cca_execution_id)
  ON DELETE CASCADE;
GO

ALTER TABLE ${ohdsiSchema}.input_files
  ADD CONSTRAINT fk_sof_cca_execution FOREIGN KEY (cca_execution_id)
  REFERENCES ${ohdsiSchema}.cca_execution (cca_execution_id)
  ON DELETE CASCADE;
GO

DECLARE @cca_execution_id_val INT;
DECLARE @sql NVARCHAR(MAX);
SELECT @cca_execution_id_val = coalesce(MAX(cca_execution_id), 1) FROM ${ohdsiSchema}.cca_execution;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.cca_execution_sequence RESTART WITH ' + CAST(@cca_execution_id_val as NVARCHAR(20)) + ';';
EXEC sp_executesql @sql;
GO

-- Table 'HERACLES_VISUALIZATION_DATA'
CREATE SEQUENCE ${ohdsiSchema}.heracles_visualization_data_sequence;
GO

ALTER TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA ADD id_tmp INT;
GO

UPDATE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA SET id_tmp = id;
GO

ALTER TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA DROP CONSTRAINT pk_heracles_viz_data;
GO

ALTER TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA DROP COLUMN id;
GO

EXEC sp_rename '[${ohdsiSchema}].HERACLES_VISUALIZATION_DATA.id_tmp', 'id', 'COLUMN';
GO

ALTER TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA ALTER COLUMN id INT NOT NULL;
GO

ALTER TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA
    ADD CONSTRAINT df_heracles_vis_data_id DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.heracles_visualization_data_sequence) FOR id;
GO

ALTER TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA ADD CONSTRAINT pk_heracles_viz_data PRIMARY KEY CLUSTERED(id asc);
GO

DECLARE @heracles_vis_data_id_val INT;
DECLARE @sql NVARCHAR(MAX);
SELECT @heracles_vis_data_id_val = coalesce(MAX(id), 1) FROM ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.heracles_visualization_data_sequence RESTART WITH ' + CAST(@heracles_vis_data_id_val as NVARCHAR(20)) + ';';
EXEC sp_executesql @sql;
GO
