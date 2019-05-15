ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD criteria_type VARCHAR(255);

MERGE INTO ${ohdsiSchema}.fe_analysis_criteria fac
USING
(
  SELECT fc.id,
         CASE WHEN fa.stat_type = 'PREVALENCE' THEN 'CRITERIA_GROUP'
            WHEN fa.stat_type = 'DISTRIBUTION' THEN 'WINDOWED_CRITERIA' END as criteria_type
  FROM ${ohdsiSchema}.fe_analysis_criteria fc
    JOIN ${ohdsiSchema}.fe_analysis fa ON fa.id = fc.fe_analysis_id
) data ON (fac.id = data.id)
WHEN MATCHED THEN UPDATE
SET fac.criteria_type = data.criteria_type;