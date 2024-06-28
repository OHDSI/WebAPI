ALTER SEQUENCE ${ohdsiSchema}.concept_set_meta_data_sequence RENAME TO concept_set_annotation_sequence;

ALTER TABLE ${ohdsiSchema}.concept_set_meta_data RENAME TO concept_set_annotation;

ALTER TABLE ${ohdsiSchema}.concept_set_annotation RENAME COLUMN concept_set_meta_data_id TO concept_set_annotation_id;
ALTER TABLE ${ohdsiSchema}.concept_set_annotation RENAME COLUMN metadata TO annotation_details;

UPDATE ${ohdsiSchema}.sec_permission
SET value = 'conceptset:update:*:annotation:put', description = 'Update Concept Set Annotation'
WHERE value = 'conceptset:update:*:metadata:put' AND description = 'Update Concept Set Metadata';

UPDATE ${ohdsiSchema}.sec_permission
SET value = 'conceptset:*:annotation:put', description = 'Create Concept Set Annotation'
WHERE value = 'conceptset:*:metadata:put' AND description = 'Create Concept Set Metadata';

UPDATE ${ohdsiSchema}.sec_permission
SET value = 'conceptset:%s:delete', description = 'Delete Concept Set Annotation'
WHERE value = 'conceptset:%s:delete' AND description = 'Delete Concept Set Metadata';

UPDATE ${ohdsiSchema}.sec_permission
SET value = 'conceptset:%s:annotation:get', description = 'List Concept Set Annotation'
WHERE value = 'conceptset:%s:metadata:get' AND description = 'List Concept Set Metadata';

UPDATE ${ohdsiSchema}.sec_permission
SET value = 'conceptset:*:annotation:get', description = 'View Concept Set Annotation'
WHERE value = 'conceptset:*:metadata:get' AND description = 'View Concept Set Metadata';

ALTER TABLE ${ohdsiSchema}.concept_set_annotation
ADD COLUMN vocabulary_version VARCHAR;