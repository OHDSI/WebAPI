CREATE TABLE ${ohdsiSchema}.cohort_analysis_gen_info (
	source_id int NOT NULL, 
	cohort_id int NOT NULL, 
	last_execution Timestamp(3),
	execution_duration int, 
	fail_message varchar(2000), 
	PRIMARY KEY (source_id, cohort_id)
);

CREATE TABLE ${ohdsiSchema}.cohort_analysis_list_xref (
	source_id int, 
	cohort_id int, 
	analysis_id int);

ALTER TABLE ${ohdsiSchema}.cohort_analysis_gen_info ADD CONSTRAINT FK_cagi_cohort_id FOREIGN KEY (cohort_id) REFERENCES ${ohdsiSchema}.cohort_definition (ID);
ALTER TABLE ${ohdsiSchema}.cohort_analysis_list_xref ADD CONSTRAINT FK_calx_source_id FOREIGN KEY (source_id, cohort_id) REFERENCES ${ohdsiSchema}.cohort_analysis_gen_info (source_id, cohort_id);
