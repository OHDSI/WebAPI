CREATE SEQUENCE ${ohdsiSchema}.cca_sequence;
GO

ALTER TABLE ${ohdsiSchema}.cca ADD cca_id_tmp INT;
GO

UPDATE ${ohdsiSchema}.cca SET cca_id_tmp = cca_id;
GO

ALTER TABLE ${ohdsiSchema}.cca DROP COLUMN cca_id;
GO

EXEC sp_rename '[${ohdsiSchema}].cca.cca_id_tmp', 'cca_id', 'COLUMN';
GO

ALTER TABLE ${ohdsiSchema}.cca ALTER COLUMN cca_id INT NOT NULL;
GO

ALTER TABLE ${ohdsiSchema}.cca ADD CONSTRAINT PK_cca_cca_id PRIMARY KEY CLUSTERED(cca_id);
GO

DECLARE @cca_id_val INT;
DECLARE @sql NVARCHAR(MAX);
SELECT @cca_id_val = coalesce(MAX(cca_id), 1) FROM ${ohdsiSchema}.cca;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.cca_sequence RESTART WITH ' + CAST(@cca_id_val as NVARCHAR(20)) + ';';
EXEC sp_executesql @sql;
GO

CREATE SEQUENCE ${ohdsiSchema}.cohort_definition_sequence;
GO

ALTER TABLE ${ohdsiSchema}.cohort_definition ADD id_tmp INT;
GO

UPDATE ${ohdsiSchema}.cohort_definition SET id_tmp = id;
GO

ALTER TABLE ${ohdsiSchema}.feasibility_study DROP CONSTRAINT FK_feasibility_study_cohort_definition_result;
GO

ALTER TABLE ${ohdsiSchema}.feasibility_study DROP CONSTRAINT [FK_feasibility_study_cohort_definition_index];
GO

ALTER TABLE ${ohdsiSchema}.cohort_definition_details DROP CONSTRAINT FK_cohort_definition_details_cohort_definition;
GO

ALTER TABLE ${ohdsiSchema}.cohort_generation_info DROP CONSTRAINT FK_cohort_generation_info_cohort_definition;
GO

DECLARE @sql NVARCHAR(MAX), @pk NVARCHAR(512);
SELECT @pk = name FROM sys.key_constraints where [type] = 'PK' AND parent_object_id = OBJECT_ID('${ohdsiSchema}.cohort_definition');
SET @sql = 'ALTER TABLE ${ohdsiSchema}.cohort_definition DROP CONSTRAINT ' + @pk + ';';
EXEC sp_executesql @sql;
GO

ALTER TABLE ${ohdsiSchema}.cohort_definition DROP COLUMN id;
GO

EXEC sp_rename '${ohdsiSchema}.cohort_definition.id_tmp', 'id', 'COLUMN';
GO

ALTER TABLE ${ohdsiSchema}.cohort_definition ALTER COLUMN id INT NOT NULL;
GO

ALTER TABLE ${ohdsiSchema}.cohort_definition ADD CONSTRAINT PK_cohort_definition_id PRIMARY KEY CLUSTERED(id);
GO

ALTER TABLE ${ohdsiSchema}.feasibility_study ADD CONSTRAINT [FK_feasibility_study_cohort_definition_result] FOREIGN KEY([result_def_id])
  REFERENCES [${ohdsiSchema}].[cohort_definition] ([id]);
GO

ALTER TABLE ${ohdsiSchema}.feasibility_study ADD CONSTRAINT [FK_feasibility_study_cohort_definition_index] FOREIGN KEY([index_def_id])
  REFERENCES [${ohdsiSchema}].[cohort_definition] ([id]);
GO

ALTER TABLE ${ohdsiSchema}.cohort_definition_details
  ADD CONSTRAINT FK_cohort_definition_details_cohort_definition
    FOREIGN KEY (id) REFERENCES ${ohdsiSchema}.cohort_definition (id)
  ON UPDATE CASCADE
  ON DELETE CASCADE;
GO

ALTER TABLE ${ohdsiSchema}.cohort_generation_info ADD CONSTRAINT [FK_cohort_generation_info_cohort_definition] FOREIGN KEY([id])
REFERENCES [${ohdsiSchema}].[cohort_definition] ([id])
  ON UPDATE CASCADE
  ON DELETE CASCADE;
GO

DECLARE @sql NVARCHAR(MAX);
DECLARE @cohort_definition_id_val INT;
SELECT @cohort_definition_id_val = coalesce(MAX(id), 1) FROM ${ohdsiSchema}.cohort_definition;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.cohort_definition_sequence RESTART WITH ' + CAST(@cohort_definition_id_val AS NVARCHAR(20)) + ';';
EXEC sp_executesql @sql;
GO

CREATE SEQUENCE ${ohdsiSchema}.concept_set_sequence;
GO

ALTER TABLE ${ohdsiSchema}.concept_set ADD concept_set_id_tmp INT;
GO

UPDATE ${ohdsiSchema}.concept_set SET concept_set_id_tmp = concept_set_id;
GO

IF EXISTS (SELECT * FROM sys.objects where name = 'FK_concept_set_generation_info_concept_set')
BEGIN
	ALTER TABLE ${ohdsiSchema}.concept_set_generation_info DROP CONSTRAINT FK_concept_set_generation_info_concept_set
END
GO

DECLARE @sql NVARCHAR(MAX), @pk NVARCHAR(512);
SELECT @pk = name FROM sys.key_constraints where [type] = 'PK' AND parent_object_id = OBJECT_ID('${ohdsiSchema}.concept_set');
SET @sql = 'ALTER TABLE ${ohdsiSchema}.concept_set DROP CONSTRAINT ' + @pk + ';';
EXEC sp_executesql @sql;
GO

ALTER TABLE ${ohdsiSchema}.concept_set DROP COLUMN concept_set_id;
GO

EXEC sp_rename '${ohdsiSchema}.concept_set.concept_set_id_tmp', 'concept_set_id', 'COLUMN';
GO

ALTER TABLE ${ohdsiSchema}.concept_set ALTER COLUMN concept_set_id INT NOT NULL;
GO

ALTER TABLE ${ohdsiSchema}.concept_set ADD CONSTRAINT PK_concept_set_concept_set_id PRIMARY KEY CLUSTERED(concept_set_id);
GO

ALTER TABLE ${ohdsiSchema}.concept_set_generation_info ADD CONSTRAINT [FK_concept_set_generation_info_concept_set] FOREIGN KEY([concept_set_id])
  REFERENCES [${ohdsiSchema}].[concept_set] ([concept_set_id])
  ON UPDATE CASCADE
  ON DELETE CASCADE;
GO

DECLARE @sql NVARCHAR(MAX);
DECLARE @concept_set_id_val INT;
SELECT @concept_set_id_val = coalesce(MAX(concept_set_id), 1) FROM ${ohdsiSchema}.concept_set;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.concept_set_sequence RESTART WITH ' + CAST(@concept_set_id_val AS NVARCHAR(20)) + ';';
EXEC sp_executesql @sql;
GO

CREATE SEQUENCE ${ohdsiSchema}.concept_set_item_sequence;
GO

ALTER TABLE ${ohdsiSchema}.concept_set_item ADD concept_set_item_id_tmp INT;
GO

UPDATE ${ohdsiSchema}.concept_set_item SET concept_set_item_id_tmp = concept_set_item_id;
GO

ALTER TABLE ${ohdsiSchema}.concept_set_item DROP COLUMN concept_set_item_id;
GO

EXEC sp_rename '${ohdsiSchema}.concept_set_item.concept_set_item_id_tmp', 'concept_set_item_id', 'COLUMN';
GO

ALTER TABLE ${ohdsiSchema}.concept_set_item ALTER COLUMN concept_set_item_id INT NOT NULL;
GO

ALTER TABLE ${ohdsiSchema}.concept_set_item ADD CONSTRAINT PK_concept_set_item_concept_set_item_id PRIMARY KEY CLUSTERED(concept_set_item_id);
GO

DECLARE @sql NVARCHAR(MAX);
DECLARE @concept_set_item_id_val INT;
SELECT @concept_set_item_id_val = coalesce(MAX(concept_set_item_id), 1) FROM ${ohdsiSchema}.concept_set_item;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.concept_set_item_sequence RESTART WITH ' + CAST(@concept_set_item_id_val AS NVARCHAR(20)) + ';'
EXEC sp_executesql @sql;
GO

CREATE SEQUENCE ${ohdsiSchema}.negative_controls_sequence;
GO

ALTER TABLE ${ohdsiSchema}.concept_set_negative_controls ADD id_tmp INT;
GO

UPDATE ${ohdsiSchema}.concept_set_negative_controls SET id_tmp = id;
GO

DECLARE @sql NVARCHAR(MAX), @pk NVARCHAR(512);
SELECT @pk = name FROM sys.key_constraints where [type] = 'PK' AND parent_object_id = OBJECT_ID('${ohdsiSchema}.concept_set_negative_controls');
SET @sql = 'ALTER TABLE ${ohdsiSchema}.concept_set_negative_controls DROP CONSTRAINT ' + @pk + ';';
EXEC sp_executesql @sql;
GO

ALTER TABLE ${ohdsiSchema}.concept_set_negative_controls DROP COLUMN id;
GO

EXEC sp_rename '${ohdsiSchema}.concept_set_negative_controls.id_tmp', 'id', 'COLUMN';
GO

ALTER TABLE ${ohdsiSchema}.concept_set_negative_controls ALTER COLUMN id INT NOT NULL;
GO

ALTER TABLE ${ohdsiSchema}.concept_set_negative_controls ADD CONSTRAINT PK_concept_set_negative_controls_id PRIMARY KEY CLUSTERED(id);
GO

DECLARE @sql NVARCHAR(MAX);
DECLARE @id_val INT;
SELECT @id_val = coalesce(MAX(id), 1) FROM ${ohdsiSchema}.concept_set_negative_controls;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.negative_controls_sequence RESTART WITH ' + CAST(@id_val AS NVARCHAR(20)) + ';';
EXEC sp_executesql @sql;
GO

CREATE SEQUENCE ${ohdsiSchema}.feasibility_study_sequence;
GO

ALTER TABLE ${ohdsiSchema}.feasibility_study ADD id_tmp INT;
GO

UPDATE ${ohdsiSchema}.feasibility_study SET id_tmp = id;
GO

ALTER TABLE ${ohdsiSchema}.feasibility_inclusion DROP CONSTRAINT FK_feasibility_inclusion_feasibility_study;
GO

ALTER TABLE ${ohdsiSchema}.feas_study_generation_info DROP CONSTRAINT FK_feas_study_generation_info_feasibility_study;
GO

DECLARE @sql NVARCHAR(MAX), @pk NVARCHAR(512);
SELECT @pk = name FROM sys.key_constraints where [type] = 'PK' AND parent_object_id = OBJECT_ID('${ohdsiSchema}.feasibility_study');
SET @sql = 'ALTER TABLE ${ohdsiSchema}.feasibility_study DROP CONSTRAINT ' + @pk + ';';
EXEC sp_executesql @sql;
GO

ALTER TABLE ${ohdsiSchema}.feasibility_study DROP COLUMN id;
GO

EXEC sp_rename '${ohdsiSchema}.feasibility_study.id_tmp', 'id', 'COLUMN';
GO

ALTER TABLE ${ohdsiSchema}.feasibility_study ALTER COLUMN id INT NOT NULL;
GO

ALTER TABLE ${ohdsiSchema}.feasibility_study ADD CONSTRAINT PK_feasibility_study_id PRIMARY KEY CLUSTERED(id);
GO

ALTER TABLE ${ohdsiSchema}.feasibility_inclusion ADD CONSTRAINT FK_feasibility_inclusion_feasibility_study
  FOREIGN KEY (study_id) REFERENCES ${ohdsiSchema}.feasibility_study (id);
GO

ALTER TABLE [${ohdsiSchema}].[feas_study_generation_info]
  ADD CONSTRAINT [FK_feas_study_generation_info_feasibility_study] FOREIGN KEY ([study_id]) REFERENCES [${ohdsiSchema}].[feasibility_study] ([id]);
GO

DECLARE @sql NVARCHAR(MAX);
DECLARE @id_val INT;
SELECT @id_val = coalesce(MAX(id), 1) FROM ${ohdsiSchema}.feasibility_study;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.feasibility_study_sequence RESTART WITH ' + CAST(@id_val AS NVARCHAR(20)) + ';';
EXEC sp_executesql @sql;
GO

CREATE SEQUENCE ${ohdsiSchema}.ir_analysis_sequence;
GO

ALTER TABLE ${ohdsiSchema}.ir_analysis ADD id_tmp INT;
GO

UPDATE ${ohdsiSchema}.ir_analysis SET id_tmp = id;
GO

ALTER TABLE ${ohdsiSchema}.ir_analysis_details DROP CONSTRAINT FK_irad_ira;
GO

DECLARE @sql NVARCHAR(MAX), @pk NVARCHAR(512);
SELECT @pk = name FROM sys.key_constraints where [type] = 'PK' AND parent_object_id = OBJECT_ID('${ohdsiSchema}.ir_analysis');
SET @sql = 'ALTER TABLE ${ohdsiSchema}.ir_analysis DROP CONSTRAINT ' + @pk + ';';
EXEC sp_executesql @sql;
GO

ALTER TABLE ${ohdsiSchema}.ir_analysis DROP COLUMN id;
GO

EXEC sp_rename '${ohdsiSchema}.ir_analysis.id_tmp', 'id', 'COLUMN'
GO

ALTER TABLE ${ohdsiSchema}.ir_analysis ALTER COLUMN id INT NOT NULL;
GO

ALTER TABLE ${ohdsiSchema}.ir_analysis ADD CONSTRAINT PK_ir_analysis_id PRIMARY KEY CLUSTERED(id);
GO

ALTER TABLE ${ohdsiSchema}.ir_analysis_details ADD CONSTRAINT FK_irad_ira
    FOREIGN KEY (id) REFERENCES ${ohdsiSchema}.ir_analysis(id);
GO

DECLARE @sql NVARCHAR(MAX);
DECLARE @id_val INT;
SELECT @id_val = coalesce(MAX(id), 1) FROM ${ohdsiSchema}.ir_analysis;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.ir_analysis_sequence RESTART WITH ' + CAST(@id_val AS NVARCHAR(20)) + ';';
EXEC sp_executesql @sql;
GO

CREATE SEQUENCE ${ohdsiSchema}.plp_sequence;
GO

ALTER TABLE ${ohdsiSchema}.plp ADD plp_id_tmp INT;
GO

UPDATE ${ohdsiSchema}.plp SET plp_id_tmp = plp_id;
GO

ALTER TABLE ${ohdsiSchema}.plp DROP COLUMN plp_id;
GO

EXEC sp_rename '${ohdsiSchema}.plp.plp_id_tmp', 'plp_id', 'COLUMN';
GO

ALTER TABLE ${ohdsiSchema}.plp ALTER COLUMN plp_id INT NOT NULL;
GO

ALTER TABLE ${ohdsiSchema}.plp ADD CONSTRAINT PK_plp_plp_id PRIMARY KEY CLUSTERED(plp_id);
GO

DECLARE @sql NVARCHAR(MAX);
DECLARE @plp_id_val INT;
SELECT @plp_id_val = coalesce(MAX(plp_id), 1) FROM ${ohdsiSchema}.plp;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.plp_sequence RESTART WITH ' + CAST(@plp_id_val AS NVARCHAR(20)) + ';';
GO
