ALTER TABLE ${ohdsiSchema}.cohort_definition ALTER COLUMN created_date TYPE TIMESTAMP(3) WITH TIME ZONE;
ALTER TABLE ${ohdsiSchema}.cohort_definition ALTER COLUMN modified_date TYPE TIMESTAMP(3) WITH TIME ZONE;

ALTER TABLE ${ohdsiSchema}.ir_analysis ALTER COLUMN created_date TYPE TIMESTAMP(3) WITH TIME ZONE;
ALTER TABLE ${ohdsiSchema}.ir_analysis ALTER COLUMN modified_date TYPE TIMESTAMP(3) WITH TIME ZONE;

ALTER TABLE ${ohdsiSchema}.plp ALTER COLUMN created TYPE TIMESTAMP(3) WITH TIME ZONE;
ALTER TABLE ${ohdsiSchema}.plp ALTER COLUMN modified TYPE TIMESTAMP(3) WITH TIME ZONE;