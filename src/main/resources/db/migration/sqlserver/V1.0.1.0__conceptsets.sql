IF (NOT EXISTS (SELECT * 
                 FROM INFORMATION_SCHEMA.TABLES 
                 WHERE TABLE_SCHEMA = 'dbo' 
                 AND  TABLE_NAME = 'concept_set'))
BEGIN
CREATE TABLE [dbo].[concept_set] (
    [concept_set_id]   INT           IDENTITY (1, 1) NOT NULL,
    [concept_set_name] VARCHAR (255) NOT NULL
);
END;

IF (NOT EXISTS (SELECT * 
                 FROM INFORMATION_SCHEMA.TABLES 
                 WHERE TABLE_SCHEMA = 'dbo' 
                 AND  TABLE_NAME = 'concept_set_item'))
BEGIN
CREATE TABLE [dbo].[concept_set_item] (
    [concept_set_item_id] INT IDENTITY (1, 1) NOT NULL,
    [concept_set_id]      INT NOT NULL,
    [concept_id]          INT NOT NULL,
    [is_excluded]         INT NOT NULL,
    [include_descendants] INT NOT NULL,
    [include_mapped]      INT NOT NULL
);
END;


