ALTER TABLE ${ohdsiSchema}.fe_analysis MODIFY (stat_type NOT NULL);

MERGE INTO ${ohdsiSchema}.fe_analysis_criteria fac
USING
(
  SELECT fc.id,
         CASE WHEN fc.criteria_type IS NULL THEN
          CASE
            WHEN fa.stat_type = 'PREVALENCE' THEN 'CRITERIA_GROUP'
            WHEN fa.stat_type = 'DISTRIBUTION' THEN
              CASE WHEN expression LIKE '{"Criteria":%'
                THEN 'WINDOWED_CRITERIA'
                ELSE 'DEMOGRAPHIC_CRITERIA'
              END
          END
        ELSE
          fc.criteria_type
        END as criteria_type
  FROM ${ohdsiSchema}.fe_analysis_criteria fc
    JOIN ${ohdsiSchema}.fe_analysis fa ON fa.id = fc.fe_analysis_id
) data ON (fac.id = data.id)
WHEN MATCHED THEN UPDATE
SET fac.criteria_type = data.criteria_type;