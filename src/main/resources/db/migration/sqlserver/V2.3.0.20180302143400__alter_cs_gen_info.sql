ALTER TABLE [${ohdsiSchema}].concept_set_generation_info
	ADD params nvarchar(max)
GO

UPDATE [${ohdsiSchema}].concept_set_generation_info set params = '{}'
GO

ALTER TABLE [${ohdsiSchema}].concept_set_generation_info
	ALTER COLUMN params nvarchar(max) not null
GO

