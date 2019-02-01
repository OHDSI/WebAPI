ALTER TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA DROP CONSTRAINT PK_heracles_viz_data;

EXEC sp_rename '[${ohdsiSchema}].[HERACLES_VISUALIZATION_DATA]', 'HERACLES_VISUALIZATION_DATA_bak';

CREATE SEQUENCE ${ohdsiSchema}.HERACLES_VIZ_DATA_SEQUENCE
  AS BIGINT
  START WITH 0
  NO CYCLE
  CACHE 1;

create table ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA
(
  cohort_definition_id int          not null,
  source_id            int          not null,
  visualization_key    varchar(300) not null,
  drilldown_id         int,
  id                   int default NEXT VALUE FOR ${ohdsiSchema}.HERACLES_VIZ_DATA_SEQUENCE,
  end_time             datetime,
  data                 varchar(max)
);

INSERT INTO ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA SELECT * FROM HERACLES_VISUALIZATION_DATA_bak;
DROP TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA_bak;
