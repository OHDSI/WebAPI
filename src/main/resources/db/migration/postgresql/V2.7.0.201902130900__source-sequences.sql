-- Source sequence
CREATE SEQUENCE ${ohdsiSchema}.source_sequence START WITH 1 INCREMENT BY 1 MAXVALUE 9223372036854775807 NO CYCLE;
SELECT setval('${ohdsiSchema}.source_sequence', coalesce(max(source_id), 1)) FROM ${ohdsiSchema}.source;

ALTER TABLE ${ohdsiSchema}.source ALTER COLUMN source_id SET DEFAULT nextval('${ohdsiSchema}.source_sequence');

-- Source_daimon sequence
CREATE SEQUENCE ${ohdsiSchema}.source_daimon_sequence START WITH 1 INCREMENT BY 1 MAXVALUE 9223372036854775807 NO CYCLE;
SELECT setval('${ohdsiSchema}.source_daimon_sequence', coalesce(max(source_daimon_id), 1)) FROM ${ohdsiSchema}.source_daimon;

ALTER TABLE ${ohdsiSchema}.source_daimon ALTER COLUMN source_daimon_id SET DEFAULT nextval('${ohdsiSchema}.source_daimon_sequence');
