UPDATE ${ohdsiSchema}.sec_permission
SET value = REPLACE(value, ':post', ':put')
WHERE value LIKE 'role:%:post';