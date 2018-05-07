-- Rename the primary key for consistency - cohort_definition
EXEC sp_rename '[${ohdsiSchema}].[cohort_definition].[${ohdsiSchema}.PK_cohort_definition]', 'PK_cohort_definition';

-- Rename the primary key for consistency - concept_set
EXEC sp_rename '[${ohdsiSchema}].[concept_set].[${ohdsiSchema}.PK_concept_set]', 'PK_concept_set';

-- Rename the primary key for consistency - concept_set_item
EXEC sp_rename '[${ohdsiSchema}].[concept_set_item].[${ohdsiSchema}.PK_concept_set_item]', 'PK_concept_set_item';
