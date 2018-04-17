-- Rename the primary key for consistency - cohort_definition
EXEC sp_rename '${ohdsiSchema}.PK_cohort_definition_id', '${ohdsiSchema}.PK_cohort_definition', 'OBJECT';

-- Ensure a primary key exists on cohort_definition_details
IF NOT EXISTS (SELECT * FROM sys.key_constraints where [type] = 'PK' AND parent_object_id = OBJECT_ID('${ohdsiSchema}.cohort_definition_details'))
BEGIN
	ALTER TABLE ${ohdsiSchema}.cohort_definition_details ADD CONSTRAINT PK_cohort_definition_details PRIMARY KEY (id);
END

-- Rename the primary key for consistency - concept_set
EXEC sp_rename '${ohdsiSchema}.PK_concept_set_concept_set_id', '${ohdsiSchema}.PK_concept_set', 'OBJECT';

-- Rename the primary key for consistency - concept_set_item
EXEC sp_rename '${ohdsiSchema}.PK_concept_set_item_concept_set_item_id', '${ohdsiSchema}.PK_concept_set_item', 'OBJECT';


