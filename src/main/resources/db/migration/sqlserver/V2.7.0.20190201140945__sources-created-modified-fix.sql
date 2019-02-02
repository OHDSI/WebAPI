-- DECLARE current id
DECLARE @cur_id_val INT;
DECLARE @sql NVARCHAR(MAX);

-- RENAME source table
ALTER TABLE ${ohdsiSchema}.source DROP CONSTRAINT DF_source_SOURCE_DIALECT;
ALTER TABLE ${ohdsiSchema}.source DROP CONSTRAINT source_key_unique;
ALTER TABLE ${ohdsiSchema}.source DROP CONSTRAINT PK_source;

EXEC sp_rename '[${ohdsiSchema}].[source]', 'source_bak';

CREATE SEQUENCE ${ohdsiSchema}.source_sequence
  AS BIGINT
  MINVALUE 1
  NO CYCLE
  CACHE 1;

-- GET current ID for source table
-- and resetting number
SELECT @cur_id_val = coalesce(MAX(SOURCE_ID), 1) FROM ${ohdsiSchema}.source_bak;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.source_sequence RESTART WITH ' + CAST(@cur_id_val as NVARCHAR(20)) + ';';

EXEC sp_executesql @sql;

CREATE TABLE ${ohdsiSchema}.source
(
  SOURCE_ID         int default NEXT VALUE FOR ${ohdsiSchema}.source_sequence not null constraint PK_source primary key nonclustered,
  SOURCE_NAME       varchar(255)                   not null,
  SOURCE_KEY        varchar(50)                    not null constraint [source_key_unique] unique,
  SOURCE_CONNECTION varchar(8000)                  not null,
  SOURCE_DIALECT    varchar(255) CONSTRAINT [DF_source_SOURCE_DIALECT] DEFAULT ('sql server') not null,
  username          nvarchar(255),
  password          nvarchar(255),
  krb_auth_method   varchar(10) default 'PASSWORD' not null,
  keytab_name       varchar(50),
  krb_keytab        varbinary(max),
  krb_admin_server  varchar(50),
  deleted_date      datetime
)

-- Copy previous source configuration and drop table
INSERT INTO ${ohdsiSchema}.source SELECT * from ${ohdsiSchema}.source_bak;
DROP TABLE ${ohdsiSchema}.source_bak;

EXEC sp_rename '[${ohdsiSchema}].[source_daimon]', 'source_daimon_bak';

CREATE SEQUENCE ${ohdsiSchema}.source_daimon_sequence
  AS BIGINT
  MINVALUE 1
  NO CYCLE
  CACHE 1;

-- GET current id for previous source_daimon table
-- and resetting number
SELECT @cur_id_val = coalesce(MAX(source_daimon_id), 1) FROM ${ohdsiSchema}.source_daimon_bak;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.source_daimon_sequence RESTART WITH ' + CAST(@cur_id_val as NVARCHAR(20)) + ';';

EXEC sp_executesql @sql;

CREATE TABLE ${ohdsiSchema}.source_daimon
(
  source_daimon_id int default NEXT VALUE FOR ${ohdsiSchema}.source_sequence not null primary key,
  source_id        int           not null,
  daimon_type      int           not null,
  table_qualifier  varchar(255)  not null,
  priority         int default 0 not null
)

-- Copy previous source configuration and drop table
INSERT INTO ${ohdsiSchema}.source_daimon SELECT * from ${ohdsiSchema}.source_daimon_bak;
DROP TABLE ${ohdsiSchema}.source_daimon_bak;
