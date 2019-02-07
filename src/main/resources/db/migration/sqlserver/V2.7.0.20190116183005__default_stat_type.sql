ALTER TABLE ${ohdsiSchema}.fe_analysis
  ADD CONSTRAINT df_stat_type DEFAULT 'PREVALENCE' FOR stat_type;

UPDATE ${ohdsiSchema}.fe_analysis
SET stat_type = 'PREVALENCE'
WHERE stat_type IS NULL;