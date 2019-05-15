ALTER TABLE ${ohdsiSchema}.output_files ADD IF NOT EXISTS media_type VARCHAR(255);

ALTER TABLE ${ohdsiSchema}.output_files DROP CONSTRAINT IF EXISTS fk_sif_cca_execution;
ALTER TABLE ${ohdsiSchema}.output_files DROP COLUMN IF EXISTS cca_execution_id;

ALTER TABLE ${ohdsiSchema}.input_files DROP CONSTRAINT IF EXISTS fk_sof_cca_execution;
ALTER TABLE ${ohdsiSchema}.input_files DROP COLUMN IF EXISTS cca_execution_id;

ALTER TABLE ${ohdsiSchema}.output_files ADD IF NOT EXISTS execution_id INT;
ALTER TABLE ${ohdsiSchema}.input_files ADD IF NOT EXISTS execution_id INT;

CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.output_file_seq;
CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.input_file_seq;