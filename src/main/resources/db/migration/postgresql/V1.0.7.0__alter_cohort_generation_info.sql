ALTER TABLE ${ohdsiSchema}.cohort_generation_info
	ADD COLUMN include_features boolean,
	ADD COLUMN fail_message varchar(2000),
	ADD COLUMN person_count bigint,
	ADD COLUMN record_count bigint
;

UPDATE ${ohdsiSchema}.cohort_generation_info set include_features = 'f'
;

ALTER TABLE ${ohdsiSchema}.cohort_generation_info
	ALTER COLUMN include_features SET NOT NULL;
