ALTER TABLE ${ohdsiSchema}.fe_analysis
  MODIFY stat_type DEFAULT 'PREVALENCE';

UPDATE ${ohdsiSchema}.fe_analysis
SET stat_type = 'PREVALENCE'
WHERE stat_type IS NULL;