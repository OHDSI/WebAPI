CREATE TABLE ${ohdsiSchema}.analysis_execution (
  id              INTEGER NOT NULL ,
  analysis_id     INTEGER NOT NULL,
  analysis_type   VARCHAR NOT NULL,
  duration        INTEGER NOT NULL,
  executed        TIMESTAMP,
  sec_user_id     INTEGER,
  executionStatus VARCHAR,
  update_password VARCHAR,
  source_id       INTEGER,
  CONSTRAINT [PK_analysis_execution_id] PRIMARY KEY CLUSTERED ([id] ASC)
);

CREATE SEQUENCE ${ohdsiSchema}.analysis_execution_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE ${ohdsiSchema}.input_files
(
  id               BIGINT IDENTITY(1, 1) NOT NULL,
  cca_execution_id BIGINT,
  execution_id     INTEGER,
  file_contents    VARBINARY,
  file_name        CHARACTER VARYING(255) NOT NULL,
  CONSTRAINT [input_files_pkey] PRIMARY KEY CLUSTERED ([id] ASC)
);

CREATE TABLE ${ohdsiSchema}.output_files
(
  id               BIGINT IDENTITY(1, 1) NOT NULL,
  cca_execution_id BIGINT,
  execution_id     INTEGER NOT NULL,
  file_contents    VARBINARY,
  file_name        CHARACTER VARYING(255) NOT NULL,
  CONSTRAINT [output_files_pkey] PRIMARY KEY ([id] ASC)
);