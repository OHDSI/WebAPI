ALTER TABLE [${ohdsiSchema}].[cohort_generation_info] DROP CONSTRAINT [FK_cohort_generation_info_cohort_definition];

CREATE TABLE [${ohdsiSchema}].[tmp_ms_xx_cohort_generation_info] (
    [id]                 INT      NOT NULL,
    [source_id]          INT      NOT NULL,
    [start_time]         DATETIME NULL,
    [execution_duration] INT      NULL,
    [status]             INT      NOT NULL,
    [is_valid]           BIT      NOT NULL,
    CONSTRAINT [tmp_ms_xx_constraint_PK_cohort_generation_info] PRIMARY KEY CLUSTERED ([id] ASC, [source_id] ASC)
);

INSERT INTO [${ohdsiSchema}].[tmp_ms_xx_cohort_generation_info] ([id], [source_id], [start_time], [execution_duration], [status], [is_valid])
SELECT  [id],
        1,
        [start_time],
        [execution_duration],
        [status],
        [is_valid]
FROM     [${ohdsiSchema}].[cohort_generation_info]
ORDER BY [id] ASC;

DROP TABLE [${ohdsiSchema}].[cohort_generation_info];

EXECUTE sp_rename N'[${ohdsiSchema}].[tmp_ms_xx_cohort_generation_info]', N'cohort_generation_info';

EXECUTE sp_rename N'[${ohdsiSchema}].[tmp_ms_xx_constraint_PK_cohort_generation_info]', N'PK_cohort_generation_info', N'OBJECT';

ALTER TABLE [${ohdsiSchema}].[cohort_generation_info]
  ADD CONSTRAINT [FK_cohort_generation_info_cohort_definition] 
  FOREIGN KEY ([id]) REFERENCES [${ohdsiSchema}].[cohort_definition] ([id]) 
  ON DELETE CASCADE ON UPDATE CASCADE;
