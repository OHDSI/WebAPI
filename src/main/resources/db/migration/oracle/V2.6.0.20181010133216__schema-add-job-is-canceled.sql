ALTER TABLE ${ohdsiSchema}.cohort_generation_info ADD is_canceled NUMBER(1) DEFAULT 0 NOT NULL;
ALTER TABLE ${ohdsiSchema}.cohort_generation_info ADD is_choose_demographic NUMBER(1) NULL DEFAULT 0;
ALTER TABLE ${ohdsiSchema}.cohort_generation_info ADD cc_generate_id INTEGER NULL;

ALTER TABLE ${ohdsiSchema}.concept_set_generation_info ADD is_canceled NUMBER(1) DEFAULT 0 NOT NULL;

ALTER TABLE ${ohdsiSchema}.feas_study_generation_info ADD is_canceled NUMBER(1) DEFAULT 0 NOT NULL;

ALTER TABLE ${ohdsiSchema}.ir_execution ADD is_canceled NUMBER(1) DEFAULT 0 NOT NULL;