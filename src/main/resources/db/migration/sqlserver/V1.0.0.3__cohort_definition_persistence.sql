IF (NOT EXISTS (SELECT * 
                 FROM INFORMATION_SCHEMA.TABLES 
                 WHERE TABLE_SCHEMA = 'dbo' 
                 AND  TABLE_NAME = 'cohort_definition'))
BEGIN
CREATE TABLE [dbo].[cohort_definition](
	[id] [int] NOT NULL identity(1,1),
	[name] [varchar](255) NOT NULL,
	[description] [varchar](1000) NULL,
	[expression_type] [varchar](50) NULL,
	[created_by] [varchar](255) NULL,
	[created_date] [datetime] NULL,
	[modified_by] [varchar](255) NULL,
	[modified_date] [datetime] NULL,
	CONSTRAINT [PK_cohort_definition] PRIMARY KEY (id) 
) ON [PRIMARY]
END
;

IF (NOT EXISTS (SELECT * 
                 FROM INFORMATION_SCHEMA.TABLES 
                 WHERE TABLE_SCHEMA = 'dbo' 
                 AND  TABLE_NAME = 'cohort_definition_details'))
BEGIN
CREATE TABLE [dbo].[cohort_definition_details](
	[id] [int],
	[expression] [varchar](max) NOT NULL,
	CONSTRAINT PK_cohort_definition_details PRIMARY KEY (id),
	CONSTRAINT FK_cohort_definition_details_cohort_definition 
		FOREIGN KEY (id)
		REFERENCES dbo.cohort_definition(id)
) ON [PRIMARY];

END
;