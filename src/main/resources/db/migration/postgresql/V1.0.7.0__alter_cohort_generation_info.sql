ALTER TABLE ${ohdsiSchema}.cohort_generation_info
	ADD include_features bit,
		fail_message varchar(2000),
		person_count bigint,
		record_count bigint
;

UPDATE ${ohdsiSchema}.cohort_generation_info set include_features = 0
;

ALTER TABLE ${ohdsiSchema}.cohort_generation_info
	ALTER COLUMN include_features SET NOT NULL;
