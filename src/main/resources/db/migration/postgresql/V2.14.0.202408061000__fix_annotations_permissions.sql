UPDATE ${ohdsiSchema}.sec_permission
SET value = 'conceptset:annotation:*:delete', description = 'Delete Concept Set Annotation'
WHERE value = 'conceptset:annotation:%s:delete';