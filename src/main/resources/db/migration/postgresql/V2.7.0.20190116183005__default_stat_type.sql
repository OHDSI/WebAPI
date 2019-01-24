ALTER TABLE ${ohdsiSchema}.fe_analysis
  ALTER COLUMN stat_type SET DEFAULT 'PREVALENCE';
  
UPDATE ${ohdsiSchema}.fe_analysis
SET stat_type = 'PREVALENCE'
WHERE stat_type ISNULL;