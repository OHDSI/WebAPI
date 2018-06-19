ALTER TABLE ${ohdsiSchema}.concept_set_generation_info
	ADD COLUMN params text
;

UPDATE ${ohdsiSchema}.concept_set_generation_info set params = '{}'
;

ALTER TABLE ${ohdsiSchema}.concept_set_generation_info
	ALTER COLUMN params SET NOT NULL;
