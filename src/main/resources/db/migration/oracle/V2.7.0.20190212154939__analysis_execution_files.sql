ALTER TABLE ${ohdsiSchema}.output_files DROP CONSTRAINT fk_sif_cca_execution;
ALTER TABLE ${ohdsiSchema}.output_files DROP COLUMN cca_execution_id;

ALTER TABLE ${ohdsiSchema}.input_files DROP CONSTRAINT fk_sof_cca_execution;
ALTER TABLE ${ohdsiSchema}.input_files DROP COLUMN cca_execution_id;

CREATE SEQUENCE ${ohdsiSchema}.output_file_seq;
CREATE SEQUENCE ${ohdsiSchema}.input_file_seq;