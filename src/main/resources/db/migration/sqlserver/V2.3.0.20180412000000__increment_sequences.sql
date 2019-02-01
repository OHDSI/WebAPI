-- cca_sequence
DECLARE @sql NVARCHAR(MAX);
DECLARE @id BIGINT;
SELECT @id = coalesce(MAX(cca_id) + 1, 1) FROM ${ohdsiSchema}.cca;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.cca_sequence RESTART WITH ' + CAST(@id AS NVARCHAR(20)) + ';'
EXEC sp_executesql @sql;
GO

-- cohort_definition_sequence
DECLARE @sql NVARCHAR(MAX);
DECLARE @id BIGINT;
SELECT @id = coalesce(MAX(id) + 1, 1) FROM ${ohdsiSchema}.cohort_definition;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.cohort_definition_sequence RESTART WITH ' + CAST(@id AS NVARCHAR(20)) + ';'
EXEC sp_executesql @sql;
GO

-- concept_set_item_sequence
DECLARE @sql NVARCHAR(MAX);
DECLARE @id BIGINT;
SELECT @id = coalesce(MAX(concept_set_item_id) + 1, 1) FROM ${ohdsiSchema}.concept_set_item;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.concept_set_item_sequence RESTART WITH ' + CAST(@id AS NVARCHAR(20)) + ';'
EXEC sp_executesql @sql;
GO

-- concept_set_sequence
DECLARE @sql NVARCHAR(MAX);
DECLARE @id BIGINT;
SELECT @id = coalesce(MAX(concept_set_id) + 1, 1) FROM ${ohdsiSchema}.concept_set;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.concept_set_sequence RESTART WITH ' + CAST(@id AS NVARCHAR(20)) + ';'
EXEC sp_executesql @sql;
GO

-- feasibility_study_sequence
DECLARE @sql NVARCHAR(MAX);
DECLARE @id BIGINT;
SELECT @id = coalesce(MAX(id) + 1, 1) FROM ${ohdsiSchema}.feasibility_study;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.feasibility_study_sequence RESTART WITH ' + CAST(@id AS NVARCHAR(20)) + ';'
EXEC sp_executesql @sql;
GO

-- ir_analysis_sequence
DECLARE @sql NVARCHAR(MAX);
DECLARE @id BIGINT;
SELECT @id = coalesce(MAX(id) + 1, 1) FROM ${ohdsiSchema}.ir_analysis;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.ir_analysis_sequence RESTART WITH ' + CAST(@id AS NVARCHAR(20)) + ';'
EXEC sp_executesql @sql;
GO

-- negative_controls_sequence
DECLARE @sql NVARCHAR(MAX);
DECLARE @id BIGINT;
SELECT @id = coalesce(MAX(id) + 1, 1) FROM ${ohdsiSchema}.concept_set_negative_controls;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.negative_controls_sequence RESTART WITH ' + CAST(@id AS NVARCHAR(20)) + ';'
EXEC sp_executesql @sql;
GO

-- plp_sequence
DECLARE @sql NVARCHAR(MAX);
DECLARE @id BIGINT;
SELECT @id = coalesce(MAX(plp_id) + 1, 1) FROM ${ohdsiSchema}.plp;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.plp_sequence RESTART WITH ' + CAST(@id AS NVARCHAR(20)) + ';'
EXEC sp_executesql @sql;
GO
