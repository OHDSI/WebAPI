-- -- drop foreign keys. cannot be used inside stored proc
ALTER TABLE ${ohdsiSchema}.output_files NOCHECK CONSTRAINT ALL;

GO

exec ${ohdsiSchema}.remove_identity_from_tables;

GO

ALTER TABLE ${ohdsiSchema}.output_files CHECK CONSTRAINT ALL;

GO