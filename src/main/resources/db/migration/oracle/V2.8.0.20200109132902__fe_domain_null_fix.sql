UPDATE ${ohdsiSchema}.fe_analysis
SET domain = 'MEASUREMENT'
WHERE type = 'PRESET' 
AND to_char(design) IN (
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