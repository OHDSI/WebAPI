ALTER TABLE ${ohdsiSchema}.cohort_inclusion DROP CONSTRAINT cohort_inclusion_pkey;
ALTER TABLE ${ohdsiSchema}.cohort_inclusion ADD PRIMARY KEY (cohort_definition_id, rule_sequence);