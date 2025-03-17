ALTER TABLE ${ohdsiSchema}.cohort_generation_info ADD is_demographic BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE ${ohdsiSchema}.cohort_generation_info ADD cc_generate_id INTEGER NULL;