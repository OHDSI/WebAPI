CREATE TABLE ${ohdsiSchema}.cohort_analysis_gen_info (
	source_id Number(10) NOT NULL, 
	cohort_id Number(10) NOT NULL, 
	last_execution Timestamp(3),
	execution_duration Number(10), 
	fail_message varchar(2000), 
	PRIMARY KEY (source_id, cohort_id)
);

CREATE TABLE ${ohdsiSchema}.cohort_analysis_list_xref (
	source_id Number(10), 
	cohort_id Number(10), 
	analysis_id Number(10));

ALTER TABLE ${ohdsiSchema}.cohort_analysis_gen_info ADD CONSTRAINT FK_cagi_cohort_id FOREIGN KEY (cohort_id) REFERENCES ${ohdsiSchema}.cohort_definition (ID);
ALTER TABLE ${ohdsiSchema}.cohort_analysis_list_xref ADD CONSTRAINT FK_calx_source_id FOREIGN KEY (source_id, cohort_id) REFERENCES ${ohdsiSchema}.cohort_analysis_gen_info (source_id, cohort_id);
