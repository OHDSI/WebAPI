ALTER TABLE ${ohdsiSchema}.cohort_generation_info
	ADD (include_features number(1),
		fail_message varchar2(2000),
		person_count number(19),
		record_count number(19));

UPDATE ${ohdsiSchema}.cohort_generation_info set include_features = 0;

ALTER TABLE ${ohdsiSchema}.cohort_generation_info
	MODIFY include_features number(1) not null;
