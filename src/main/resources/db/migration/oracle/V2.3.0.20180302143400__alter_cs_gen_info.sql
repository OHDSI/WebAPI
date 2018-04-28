ALTER TABLE ${ohdsiSchema}.concept_set_generation_info
	ADD COLUMN params CLOB
;

UPDATE ${ohdsiSchema}.concept_set_generation_info set params = '{}'
;

ALTER TABLE ${ohdsiSchema}.concept_set_generation_info
	ALTER COLUMN params SET NOT NULL;
