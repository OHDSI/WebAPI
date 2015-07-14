if ((select columnproperty(object_id('[${ohdsiSchema}].[EXAMPLEAPP_WIDGET]'),'ID','IsIdentity')) = 0) 
begin
  CREATE TABLE ${ohdsiSchema}.[TEMP_EXAMPLEAPP_WIDGET]
  (
          [ID] [bigint] NOT NULL identity(1,1),
          [NAME] [varchar](50) NULL
  );
  
  SET IDENTITY_INSERT ${ohdsiSchema}.[TEMP_EXAMPLEAPP_WIDGET] ON;

  IF EXISTS(SELECT * FROM ${ohdsiSchema}.[EXAMPLEAPP_WIDGET])
   EXEC('INSERT INTO ${ohdsiSchema}.[TEMP_EXAMPLEAPP_WIDGET] (ID, NAME)
SELECT ID, NAME from ${ohdsiSchema}.[EXAMPLEAPP_WIDGET] TABLOCKX;');

  SET IDENTITY_INSERT ${ohdsiSchema}.[TEMP_EXAMPLEAPP_WIDGET] OFF;

  DROP TABLE ${ohdsiSchema}.[EXAMPLEAPP_WIDGET];

  Exec sp_rename '${ohdsiSchema}.TEMP_EXAMPLEAPP_WIDGET', 'EXAMPLEAPP_WIDGET';

  ALTER TABLE ${ohdsiSchema}.EXAMPLEAPP_WIDGET 
    ADD CONSTRAINT PK_EXAMPLEAPP_WIDGET PRIMARY KEY CLUSTERED ([ID]) 
  ON [PRIMARY];
end
;

IF (EXISTS (SELECT * 
                 FROM INFORMATION_SCHEMA.TABLES 
                 WHERE TABLE_SCHEMA = '${ohdsiSchema}' 
                 AND  TABLE_NAME = 'HIBERNATE_SEQUENCE'))
BEGIN
  DROP TABLE ${ohdsiSchema}.HIBERNATE_SEQUENCE;
END
;
