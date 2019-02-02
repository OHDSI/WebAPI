ALTER TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA DROP CONSTRAINT PK_heracles_viz_data;

EXEC sp_rename '[${ohdsiSchema}].[HERACLES_VISUALIZATION_DATA]', 'HERACLES_VISUALIZATION_DATA_bak';

CREATE SEQUENCE ${ohdsiSchema}.HERACLES_VIZ_DATA_SEQUENCE
  AS BIGINT
  START WITH 1
  NO CYCLE
  CACHE 1;

DECLARE @cur_id_val INT;
DECLARE @sql NVARCHAR(MAX);
SELECT @cur_id_val = coalesce(MAX(id), 1) FROM ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA_bak;
SET @sql = N'ALTER SEQUENCE ${ohdsiSchema}.HERACLES_VIZ_DATA_SEQUENCE RESTART WITH ' + CAST(@cur_id_val as NVARCHAR(20)) + ';';

EXEC sp_executesql @sql;

create table ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA
(
  cohort_definition_id int          not null,
  source_id            int          not null,
  visualization_key    varchar(300) not null,
  drilldown_id         int,
  id                   int not null constraint PK_heracles_viz_data default NEXT VALUE FOR ${ohdsiSchema}.HERACLES_VIZ_DATA_SEQUENCE,
  end_time             datetime,
  data                 varchar(max)
) ON [PRIMARY];

INSERT INTO ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA SELECT * FROM ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA_bak;
DROP TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA_bak;
