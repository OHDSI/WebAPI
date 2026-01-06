ALTER TABLE ${ohdsiSchema}.output_files ADD media_type VARCHAR(255);

ALTER TABLE ${ohdsiSchema}.output_files DROP CONSTRAINT fk_sif_cca_execution;
ALTER TABLE ${ohdsiSchema}.output_files DROP COLUMN  cca_execution_id;

ALTER TABLE ${ohdsiSchema}.input_files DROP CONSTRAINT  fk_sof_cca_execution;
ALTER TABLE ${ohdsiSchema}.input_files DROP COLUMN  cca_execution_id;

--ALTER TABLE ${ohdsiSchema}.output_files ADD  execution_id INT;
--ALTER TABLE ${ohdsiSchema}.input_files ADD  execution_id INT;

CREATE SEQUENCE  ${ohdsiSchema}.output_file_seq;
CREATE SEQUENCE  ${ohdsiSchema}.input_file_seq;