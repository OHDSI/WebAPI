CREATE SEQUENCE ${ohdsiSchema}.source_sequence
  AS BIGINT
  START WITH 1
  NO CYCLE;
GO

-- Create sid,,
ALTER TABLE ${ohdsiSchema}.source ADD sid INT;
GO

UPDATE ${ohdsiSchema}.source SET sid = SOURCE_ID;
GO

-- DROP constraints and columns,,
ALTER TABLE ${ohdsiSchema}.source DROP CONSTRAINT PK_source;
GO

ALTER TABLE ${ohdsiSchema}.source DROP COLUMN SOURCE_ID;
GO

-- sid is column,,
EXEC sp_rename '[${ohdsiSchema}].source.sid', 'SOURCE_ID', 'COLUMN';
GO

ALTER TABLE ${ohdsiSchema}.source ALTER COLUMN SOURCE_ID INT NOT NULL;
GO


ALTER TABLE ${ohdsiSchema}.source ADD CONSTRAINT DF_source_id DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.source_sequence) FOR SOURCE_ID;
GO

ALTER TABLE ${ohdsiSchema}.source ADD CONSTRAINT PK_source PRIMARY KEY clustered(SOURCE_ID asc);
GO

-- DECLARE current id
DECLARE @cur_id_val INT;
DECLARE @sql NVARCHAR(MAX);

-- GET current ID for source table
-- and resetting number
SELECT @cur_id_val = coalesce(MAX(SOURCE_ID), 1) FROM ${ohdsiSchema}.source;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.source_sequence RESTART WITH ' + CAST(@cur_id_val as NVARCHAR(20)) + ';';

EXEC sp_executesql @sql;
GO


CREATE SEQUENCE ${ohdsiSchema}.source_daimon_sequence
  AS BIGINT
  START WITH 1
  NO CYCLE;
GO

-- Create source diamon id,,
ALTER TABLE ${ohdsiSchema}.source_daimon ADD sd_id INT;
GO

UPDATE ${ohdsiSchema}.source_daimon SET sd_id = source_daimon_id;
GO

-- DROP previous column
DECLARE @pk nvarchar(512);
SELECT @pk = Name FROM SYS.KEY_CONSTRAINTS WHERE [type] = 'PK' AND PARENT_OBJECT_ID = OBJECT_ID('${ohdsiSchema}.source_daimon')

IF @pk IS NOT NULL
EXEC('ALTER TABLE ${ohdsiSchema}.source_daimon DROP CONSTRAINT ' + @pk);
GO

ALTER TABLE ${ohdsiSchema}.source_daimon DROP COLUMN source_daimon_id;
GO

-- sd_id is column,,
EXEC sp_rename '[${ohdsiSchema}].source_daimon.sd_id', 'source_daimon_id', 'COLUMN';
GO

ALTER TABLE ${ohdsiSchema}.source_daimon ALTER COLUMN source_daimon_id INT NOT NULL;
GO

ALTER TABLE ${ohdsiSchema}.source_daimon ADD CONSTRAINT DF_source_daimon_id DEFAULT (NEXT VALUE FOR ${ohdsiSchema}.source_daimon_sequence) FOR source_daimon_id;
GO

ALTER TABLE ${ohdsiSchema}.source_daimon ADD CONSTRAINT PK_source_daimon PRIMARY KEY clustered(source_daimon_id asc);
GO

-- DECLARE current id
DECLARE @cur_id_val INT;
DECLARE @sql NVARCHAR(MAX);

-- GET current id for previous source_daimon table
-- and resetting number
SELECT @cur_id_val = coalesce(MAX(source_daimon_id), 1) FROM ${ohdsiSchema}.source_daimon;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.source_daimon_sequence RESTART WITH ' + CAST(@cur_id_val as NVARCHAR(20)) + ';';

EXEC sp_executesql @sql;
GO
