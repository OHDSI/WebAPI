CREATE SEQUENCE ${ohdsiSchema}.analysis_execution_sequence;
SELECT setval('${ohdsiSchema}.analysis_execution_sequence', coalesce(max(id), 1)) FROM ${ohdsiSchema}.analysis_execution;
ALTER TABLE ${ohdsiSchema}.analysis_execution ALTER COLUMN id SET DEFAULT nextval('${ohdsiSchema}.analysis_execution_sequence');
-- Delete old sequence
DROP SEQUENCE IF EXISTS ${ohdsiSchema}.analysis_execution_id_seq;

CREATE SEQUENCE ${ohdsiSchema}.cca_execution_sequence;
SELECT setval('${ohdsiSchema}.cca_execution_sequence', coalesce(max(cca_execution_id), 1)) FROM ${ohdsiSchema}.cca_execution;
ALTER TABLE ${ohdsiSchema}.cca_execution ALTER COLUMN cca_execution_id SET DEFAULT nextval('${ohdsiSchema}.cca_execution_sequence');

CREATE SEQUENCE ${ohdsiSchema}.heracles_visualization_data_sequence;
SELECT setval('${ohdsiSchema}.heracles_visualization_data_sequence', coalesce(max(id), 1)) FROM ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA;
ALTER TABLE ${ohdsiSchema}.HERACLES_VISUALIZATION_DATA ALTER COLUMN id SET DEFAULT nextval('${ohdsiSchema}.heracles_visualization_data_sequence');
