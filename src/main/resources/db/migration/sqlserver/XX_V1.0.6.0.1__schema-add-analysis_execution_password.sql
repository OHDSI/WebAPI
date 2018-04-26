CREATE TABLE ${ohdsiSchema}.cca_execution_ext (
  cca_execution_id INTEGER NOT NULL,
  update_password  VARCHAR(255)
);

ALTER TABLE ${ohdsiSchema}.input_files
  DROP CONSTRAINT fk_sof_cca_execution;

ALTER TABLE ${ohdsiSchema}.output_files
  DROP CONSTRAINT fk_sif_cca_execution;

ALTER TABLE ${ohdsiSchema}.cca_execution
  DROP CONSTRAINT cca_execution_pk;

ALTER TABLE ${ohdsiSchema}.cca_execution
  ADD CONSTRAINT cca_execution_pk PRIMARY KEY (cca_execution_id);

CREATE TABLE ${ohdsiSchema}.input_files
(
  id               BIGINT,
  cca_execution_id BIGINT                 NOT NULL,
  file_contents    VARBINARY(max),
  file_name        VARCHAR(255) NOT NULL,
  CONSTRAINT input_files_pkey PRIMARY KEY (id)
);

ALTER TABLE ${ohdsiSchema}.input_files
  ADD CONSTRAINT fk_sof_cca_execution FOREIGN KEY (cca_execution_id)
  REFERENCES ${ohdsiSchema}.cca_execution (cca_execution_id)
  ON UPDATE NO ACTION ON DELETE CASCADE;

CREATE TABLE ${ohdsiSchema}.output_files
(
  id               BIGINT,
  cca_execution_id BIGINT                 NOT NULL,
  file_contents    VARBINARY(max),
  file_name        VARCHAR(255) NOT NULL,
  CONSTRAINT output_files_pkey PRIMARY KEY (id)
);

ALTER TABLE ${ohdsiSchema}.output_files
  ADD CONSTRAINT fk_sif_cca_execution FOREIGN KEY (cca_execution_id)
  REFERENCES ${ohdsiSchema}.cca_execution (cca_execution_id)
  ON UPDATE NO ACTION ON DELETE CASCADE;
