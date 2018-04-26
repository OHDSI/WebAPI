CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.cca_execution_ext (
  cca_execution_id INTEGER NOT NULL,
  update_password  VARCHAR
);

ALTER TABLE IF EXISTS ${ohdsiSchema}.input_files
  DROP CONSTRAINT IF EXISTS fk_sof_cca_execution;

ALTER TABLE IF EXISTS ${ohdsiSchema}.output_files
  DROP CONSTRAINT IF EXISTS fk_sif_cca_execution;

ALTER TABLE ${ohdsiSchema}.cca_execution
  DROP CONSTRAINT IF EXISTS cca_execution_pk;

ALTER TABLE ${ohdsiSchema}.cca_execution
  ADD CONSTRAINT cca_execution_pk PRIMARY KEY (cca_execution_id);

CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.input_files
(
  id               BIGINT,
  cca_execution_id BIGINT                 NOT NULL,
  file_contents    BYTEA,
  file_name        CHARACTER VARYING(255) NOT NULL,
  CONSTRAINT input_files_pkey PRIMARY KEY (id)
);

ALTER TABLE ${ohdsiSchema}.input_files
  ADD CONSTRAINT fk_sof_cca_execution FOREIGN KEY (cca_execution_id)
  REFERENCES ${ohdsiSchema}.cca_execution (cca_execution_id)
  ON UPDATE NO ACTION ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS ${ohdsiSchema}.output_files
(
  id               BIGINT,
  cca_execution_id BIGINT                 NOT NULL,
  file_contents    BYTEA,
  file_name        CHARACTER VARYING(255) NOT NULL,
  CONSTRAINT output_files_pkey PRIMARY KEY (id)
);

ALTER TABLE ${ohdsiSchema}.output_files
  ADD CONSTRAINT fk_sif_cca_execution FOREIGN KEY (cca_execution_id)
  REFERENCES ${ohdsiSchema}.cca_execution (cca_execution_id)
  ON UPDATE NO ACTION ON DELETE CASCADE;