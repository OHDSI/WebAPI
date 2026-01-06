UPDATE ${ohdsiSchema}.fe_analysis
SET domain = 'MEASUREMENT'
WHERE type = 'PRESET' 
AND design IN (
    'MeasurementRangeGroupShortTerm',
    'MeasurementRangeGroupLongTerm',
    'MeasurementRangeGroupMediumTerm',
    'MeasurementRangeGroupAnyTimePrior',
    'MeasurementValueLongTerm',
    'MeasurementValueShortTerm',
    'MeasurementValueMediumTerm',
    'MeasurementValueAnyTimePrior'
)
;