
IF (NOT EXISTS (SELECT * 
                 FROM INFORMATION_SCHEMA.TABLES 
                 WHERE TABLE_SCHEMA = 'dbo' 
                 AND  TABLE_NAME = 'source'))
BEGIN
CREATE TABLE [dbo].[source] (
    [SOURCE_ID]         INT            IDENTITY (1, 1) NOT NULL,
    [SOURCE_NAME]       VARCHAR (255)  NOT NULL,
    [SOURCE_KEY]        VARCHAR (50)   NOT NULL,
    [SOURCE_CONNECTION] VARCHAR (8000) NOT NULL,
    [SOURCE_DIALECT]    VARCHAR (255)  CONSTRAINT [DF_source_SOURCE_DIALECT] DEFAULT ('sql server') NOT NULL
)
 ON [PRIMARY];
END;

IF (NOT EXISTS (SELECT * 
                 FROM INFORMATION_SCHEMA.TABLES 
                 WHERE TABLE_SCHEMA = 'dbo' 
                 AND  TABLE_NAME = 'source_daimon'))
BEGIN
CREATE TABLE [dbo].[source_daimon] (
    [source_daimon_id] INT           IDENTITY (1, 1) NOT NULL,
    [source_id]        INT           NOT NULL,
    [daimon_type]      INT           NOT NULL,
    [table_qualifier]  VARCHAR (255) NOT NULL,
    [priority]         INT           DEFAULT ((0)) NOT NULL,
    PRIMARY KEY CLUSTERED ([source_daimon_id] ASC)
) ON [PRIMARY];
END;