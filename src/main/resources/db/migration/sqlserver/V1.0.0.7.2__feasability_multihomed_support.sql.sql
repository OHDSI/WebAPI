ALTER TABLE [dbo].[feasibility_study] DROP CONSTRAINT [FK_feasibility_study_feas_study_generation_info];

CREATE TABLE [dbo].[tmp_ms_xx_feas_study_generation_info] (
    [study_id]           INT      NOT NULL,
    [source_id]          INT      NOT NULL,
    [start_time]         DATETIME NULL,
    [execution_duration] INT      NULL,
    [status]             INT      NOT NULL,
    [is_valid]           BIT      NOT NULL,
    CONSTRAINT [tmp_ms_xx_constraint_PK_feas_study_generation_info] PRIMARY KEY CLUSTERED ([study_id] ASC, [source_id] ASC)
);

IF EXISTS (SELECT TOP 1 1 
           FROM   [dbo].[feas_study_generation_info])
    BEGIN
        INSERT INTO [dbo].[tmp_ms_xx_feas_study_generation_info] ([study_id], [source_id], [start_time], [execution_duration], [status], [is_valid])
        SELECT   [study_id],
                 1,
                 [start_time],
                 [execution_duration],
                 [status],
                 [is_valid]
        FROM     [dbo].[feas_study_generation_info]
        ORDER BY [study_id] ASC;
    END

DROP TABLE [dbo].[feas_study_generation_info];

EXECUTE sp_rename N'[dbo].[tmp_ms_xx_feas_study_generation_info]', N'feas_study_generation_info';

EXECUTE sp_rename N'[dbo].[tmp_ms_xx_constraint_PK_feas_study_generation_info]', N'PK_feas_study_generation_info', N'OBJECT';

ALTER TABLE [dbo].[feasibility_study] DROP COLUMN [generate_info_id];

ALTER TABLE [dbo].[feas_study_generation_info]
    ADD CONSTRAINT [FK_feas_study_generation_info_feasibility_study] FOREIGN KEY ([study_id]) REFERENCES [dbo].[feasibility_study] ([id]);
