IF (NOT EXISTS (SELECT * 
		    FROM INFORMATION_SCHEMA.TABLE_CONSTRAINTS 
		    WHERE CONSTRAINT_TYPE = 'PRIMARY KEY' 
                    AND TABLE_NAME = 'concept_set' 
		    AND TABLE_SCHEMA = '${ohdsiSchema}' ))
BEGIN
    ALTER TABLE [${ohdsiSchema}].[concept_set] ADD CONSTRAINT [PK_concept_set] PRIMARY KEY CLUSTERED 
    (
            [concept_set_id] ASC
    )
END
;


CREATE TABLE [${ohdsiSchema}].[concept_set_generation_info](
  [concept_set_id] INT NOT NULL,
  [source_id] INT NOT NULL,
  [generation_type] INT NOT NULL,
  [start_time] DATETIME NOT NULL,
  [execution_duration] INT NULL,
  [status] INT NOT NULL,
  [is_valid] INT NOT NULL,
  CONSTRAINT [PK_concept_set_generation_info] PRIMARY KEY CLUSTERED ( [concept_set_id] ASC, [source_id] ASC ),
  CONSTRAINT [FK_concept_set_generation_info_concept_set] FOREIGN KEY([concept_set_id])
    REFERENCES [${ohdsiSchema}].[concept_set] ([concept_set_id])
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ON [PRIMARY]
;