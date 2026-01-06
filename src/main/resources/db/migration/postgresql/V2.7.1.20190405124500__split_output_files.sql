CREATE TABLE ${ohdsiSchema}.output_file_contents (
  output_file_id INTEGER,
  file_contents BYTEA,
  CONSTRAINT output_file_contents_pkey PRIMARY KEY (output_file_id)
);

ALTER TABLE ${ohdsiSchema}.output_file_contents
  ADD CONSTRAINT fk_ofc_of_id FOREIGN KEY (output_file_id)
  REFERENCES ${ohdsiSchema}.output_files (id)
  ON UPDATE NO ACTION ON DELETE CASCADE;

INSERT INTO ${ohdsiSchema}.output_file_contents (output_file_id, file_contents)
SELECT id, file_contents
FROM ${ohdsiSchema}.output_files;

ALTER TABLE ${ohdsiSchema}.output_files DROP COLUMN file_contents;