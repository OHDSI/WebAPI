CREATE TABLE ${ohdsiSchema}.cca_execution_ext (
  cca_execution_id INTEGER NOT NULL,
  update_password  VARCHAR2(255)
);

ALTER TABLE ${ohdsiSchema}.cca_execution
  ADD CONSTRAINT cca_execution_pk PRIMARY KEY (cca_execution_id);

CREATE TABLE ${ohdsiSchema}.input_files
(
  id               NUMBER(19),
  cca_execution_id NUMBER(19)                 NOT NULL,
  file_contents    LONG RAW,
  file_name        VARCHAR2(255) NOT NULL,
  CONSTRAINT input_files_pkey PRIMARY KEY (id)
);

ALTER TABLE ${ohdsiSchema}.input_files
  ADD CONSTRAINT fk_sof_cca_execution FOREIGN KEY (cca_execution_id)
  REFERENCES ${ohdsiSchema}.cca_execution (cca_execution_id);

CREATE TABLE ${ohdsiSchema}.output_files
(
  id               NUMBER(19),
  cca_execution_id NUMBER(19)                 NOT NULL,
  file_contents    LONG RAW,
  file_name       VARCHAR2(255) NOT NULL,
  CONSTRAINT output_files_pkey PRIMARY KEY (id)
);

ALTER TABLE ${ohdsiSchema}.output_files
  ADD CONSTRAINT fk_sif_cca_execution FOREIGN KEY (cca_execution_id)
  REFERENCES ${ohdsiSchema}.cca_execution (cca_execution_id);
