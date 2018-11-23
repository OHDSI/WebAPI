ALTER TABLE ${ohdsiSchema}.fe_analysis_criteria ADD criteria_type VARCHAR(255)
GO

UPDATE fc
    SET criteria_type = CASE WHEN fa.stat_type = 'PREVALENCE' THEN 'CRITERIA_GROUP'
                    WHEN fa.stat_type = 'DISTRIBUTION' THEN 'WINDOWED_CRITERIA' END
FROM ${ohdsiSchema}.fe_analysis_criteria fc
JOIN ${ohdsiSchema}.fe_analysis fa ON fa.id = fc.fe_analysis_id;