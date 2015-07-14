IF (EXISTS (SELECT * 
                 FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS 
                 WHERE CONSTRAINT_SCHEMA = '${ohdsiSchema}' 
                 AND  CONSTRAINT_NAME = 'FK_cohort_definition_details_cohort_definition'))
BEGIN
ALTER TABLE ${ohdsiSchema}.cohort_definition_details
  DROP CONSTRAINT FK_cohort_definition_details_cohort_definition
END;

ALTER TABLE ${ohdsiSchema}.cohort_definition_details 
  ADD CONSTRAINT FK_cohort_definition_details_cohort_definition 
    FOREIGN KEY ( id) 
    REFERENCES ${ohdsiSchema}.cohort_definition (id)
      ON UPDATE CASCADE
      ON DELETE CASCADE
;

IF (NOT EXISTS (SELECT * 
                 FROM INFORMATION_SCHEMA.TABLES 
                 WHERE TABLE_SCHEMA = '${ohdsiSchema}' 
                 AND  TABLE_NAME = 'cohort_generation_info'))
BEGIN
CREATE TABLE [${ohdsiSchema}].[cohort_generation_info](
  [id] [int] NOT NULL,
  [start_time] [datetime] NOT NULL,
  [execution_duration] [int] NULL,
  [status] [int] NOT NULL,
  [is_valid] [bit] NOT NULL,
  CONSTRAINT [PK_cohort_generation_info] PRIMARY KEY CLUSTERED ( [id] ASC),
  CONSTRAINT [FK_cohort_generation_info_cohort_definition] FOREIGN KEY([id])
    REFERENCES [${ohdsiSchema}].[cohort_definition] ([id])
    ON UPDATE CASCADE
    ON DELETE CASCADE
) ON [PRIMARY]
END;
