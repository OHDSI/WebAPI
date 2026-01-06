ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD criteria_type VARCHAR;

UPDATE fe_analysis_criteria
    SET criteria_type = CASE WHEN fa.stat_type = 'PREVALENCE' THEN 'CRITERIA_GROUP'
                    WHEN fa.stat_type = 'DISTRIBUTION' THEN 'WINDOWED_CRITERIA' END
FROM ${ohdsiSchema}.fe_analysis fa
WHERE fa.id = fe_analysis_criteria.fe_analysis_id;