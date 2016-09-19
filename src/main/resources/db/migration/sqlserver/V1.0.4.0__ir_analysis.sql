CREATE TABLE [${ohdsiSchema}].[ir_analysis](
  [id] [int] IDENTITY(1,1) NOT NULL,
  [name] [varchar](255) NOT NULL,
  [description] [varchar](1000) NULL,
  [created_by] [varchar](255) NULL,
  [created_date] [datetime] NULL,
  [modified_by] [varchar](255) NULL,
  [modified_date] [datetime] NULL,
  CONSTRAINT [PK_ir_analysis] PRIMARY KEY CLUSTERED ([id] ASC)
)
;

CREATE TABLE [${ohdsiSchema}].[ir_analysis_details](
  [id] [int],
  [expression] [varchar](max) NOT NULL,
  CONSTRAINT PK_ir_analysis_details PRIMARY KEY (id),
  CONSTRAINT FK_irad_ira 
    FOREIGN KEY (id) REFERENCES ${ohdsiSchema}.ir_analysis(id)
)
;

CREATE TABLE [${ohdsiSchema}].[ir_execution] (
  [analysis_id]         INT      NOT NULL,
  [source_id]           INT      NOT NULL,
  [start_time]          DATETIME NULL,
  [execution_duration]  INT      NULL,
  [status]              INT      NOT NULL,
  [is_valid]            BIT      NOT NULL,
  [message]             VARCHAR(2000) NULL,
  CONSTRAINT [PK_ir_execution] PRIMARY KEY CLUSTERED ([analysis_id] ASC, [source_id] ASC)
)
;

CREATE TABLE ${ohdsiSchema}.ir_strata(
  analysis_id int NOT NULL,
  strata_sequence int NOT NULL,
  name varchar(255) NULL,
  description varchar(1000) NULL
)
;

CREATE TABLE [${ohdsiSchema}].[ir_analysis_result](
  [analysis_id] [int] NOT NULL,
  [target_id] int NOT NULL,
  [outcome_id] int NOT NULL,
  [strata_mask] [bigint] NOT NULL,
  [person_count] [bigint] NOT NULL,
  [time_at_risk] [bigint] NOT NULL,
  [cases] [bigint] NOT NULL
)
;

CREATE TABLE [${ohdsiSchema}].[ir_analysis_strata_stats](
  [analysis_id] [int] NOT NULL,
  [target_id] int NOT NULL,
  [outcome_id] int NOT NULL,
  [strata_sequence] [int] NOT NULL,
  [person_count] [bigint] NOT NULL,
  [time_at_risk] [bigint] NOT NULL,
  [cases] [bigint] NOT NULL
)
;

