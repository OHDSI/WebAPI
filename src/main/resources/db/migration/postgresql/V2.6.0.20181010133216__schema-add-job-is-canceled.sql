ALTER TABLE ${ohdsiSchema}.cohort_generation_info ADD COLUMN is_canceled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE ${ohdsiSchema}.concept_set_generation_info ADD COLUMN is_canceled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE ${ohdsiSchema}.feas_study_generation_info ADD COLUMN is_canceled BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE ${ohdsiSchema}.ir_execution ADD COLUMN is_canceled BOOLEAN NOT NULL DEFAULT FALSE;