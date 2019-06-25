ALTER TABLE ${ohdsiSchema}.cohort_generation_info ADD is_canceled BIT NOT NULL DEFAULT 0;

ALTER TABLE ${ohdsiSchema}.concept_set_generation_info ADD is_canceled BIT NOT NULL DEFAULT 0;

ALTER TABLE ${ohdsiSchema}.feas_study_generation_info ADD is_canceled BIT NOT NULL DEFAULT 0;

ALTER TABLE ${ohdsiSchema}.ir_execution ADD is_canceled BIT NOT NULL DEFAULT 0;