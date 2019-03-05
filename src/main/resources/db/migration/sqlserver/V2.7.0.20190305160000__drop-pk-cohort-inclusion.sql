IF OBJECT_ID('${ohdsiSchema}.pk_cohort_incl', 'PK') IS NOT NULL
ALTER TABLE ${ohdsiSchema}.cohort_inclusion DROP CONSTRAINT pk_cohort_incl;