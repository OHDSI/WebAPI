UPDATE ${ohdsiSchema}.cohort_definition SET created_by = 'anonymous' WHERE created_by = 'system';
UPDATE ${ohdsiSchema}.cohort_definition SET modified_by = 'anonymous' WHERE modified_by = 'system';
UPDATE ${ohdsiSchema}.feasibility_study SET created_by = 'anonymous' WHERE created_by = 'system';
UPDATE ${ohdsiSchema}.feasibility_study SET modified_by = 'anonymous' WHERE modified_by = 'system';
UPDATE ${ohdsiSchema}.ir_analysis SET created_by = 'anonymous' WHERE created_by = 'system';
UPDATE ${ohdsiSchema}.ir_analysis SET modified_by = 'anonymous' WHERE modified_by = 'system';
