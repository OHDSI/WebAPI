-- -- drop foreign keys. cannot be used inside stored proc
ALTER TABLE ${ohdsiSchema}.output_files NOCHECK CONSTRAINT ALL;

-- ALTER TABLE ${ohdsiSchema}.output_files DROP CONSTRAINT fk_sif_cca_execution;

GO

exec ${ohdsiSchema}.remove_identity_from_tables;

GO

-- --recreate foreign keys
-- ALTER TABLE ${ohdsiSchema}.output_files
--              ADD CONSTRAINT fk_sif_cca_execution FOREIGN KEY (cca_execution_id)
--              REFERENCES ${ohdsiSchema}.cca_execution (cca_execution_id)
--              ON UPDATE NO ACTION ON DELETE CASCADE;

ALTER TABLE ${ohdsiSchema}.output_files CHECK CONSTRAINT ALL;

GO