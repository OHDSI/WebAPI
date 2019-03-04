IF OBJECT_ID('${ohdsiSchema}.pk_cohort_incl', 'PK') IS NOT NULL
ALTER TABLE ${ohdsiSchema}.cohort_inclusion DROP CONSTRAINT pk_cohort_incl;

ALTER TABLE ${ohdsiSchema}.cohort_inclusion ADD CONSTRAINT pk_cohort_incl PRIMARY KEY (cohort_definition_id, rule_sequence);