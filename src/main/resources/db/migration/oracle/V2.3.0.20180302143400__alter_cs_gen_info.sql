ALTER TABLE ${ohdsiSchema}.concept_set_generation_info ADD params CLOB;

UPDATE ${ohdsiSchema}.concept_set_generation_info set params = '{}';

ALTER TABLE ${ohdsiSchema}.concept_set_generation_info MODIFY (params NOT NULL);
