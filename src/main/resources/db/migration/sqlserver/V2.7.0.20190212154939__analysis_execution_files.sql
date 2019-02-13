ALTER TABLE ${ohdsiSchema}.output_files DROP CONSTRAINT fk_sif_cca_execution;
ALTER TABLE ${ohdsiSchema}.output_files DROP COLUMN cca_execution_id;

CREATE SEQUENCE ${ohdsiSchema}.output_file_seq START WITH 1;