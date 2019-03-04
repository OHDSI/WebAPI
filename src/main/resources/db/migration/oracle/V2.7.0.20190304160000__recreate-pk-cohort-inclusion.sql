ALTER TABLE ${ohdsiSchema}.cohort_inclusion DROP PRIMARY KEY;
ALTER TABLE ${ohdsiSchema}.cohort_inclusion ADD CONSTRAINT pk_cohort_incl PRIMARY KEY (cohort_definition_id, rule_sequence);