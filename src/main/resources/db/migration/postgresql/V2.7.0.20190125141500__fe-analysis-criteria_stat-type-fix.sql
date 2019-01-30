ALTER TABLE ${ohdsiSchema}.fe_analysis
  ALTER COLUMN stat_type SET NOT NULL;

UPDATE ${ohdsiSchema}.fe_analysis_criteria
  SET criteria_type =
    CASE WHEN criteria_type IS NULL THEN
      CASE
        WHEN fa.stat_type = 'PREVALENCE' THEN 'CRITERIA_GROUP'
        WHEN fa.stat_type = 'DISTRIBUTION' THEN
          CASE WHEN expression LIKE '{"Criteria":%'
            THEN 'WINDOWED_CRITERIA'
            ELSE 'DEMOGRAPHIC_CRITERIA'
          END
      END
    ELSE
      criteria_type
    END
FROM ${ohdsiSchema}.fe_analysis fa
WHERE fa.id = fe_analysis_criteria.fe_analysis_id;