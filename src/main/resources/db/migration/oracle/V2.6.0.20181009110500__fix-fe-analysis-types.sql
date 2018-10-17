UPDATE ${ohdsiSchema}.fe_analysis
SET stat_type = 'DISTRIBUTION'
WHERE type = 'PRESET' and TO_CHAR(design) IN (
  -- DemographicsAge.sql
  'DemographicsAge',
  -- DemographicsTime.sql
  'DemographicsPriorObservationTime',
  'DemographicsPostObservationTime',
  'DemographicsTimeInCohort',
  -- Chads2.sql
  'Chads2',
  -- Chads2Vasc.sql
  'Chads2Vasc',
  -- ConceptCounts.sql
  'DistinctConditionCountLongTerm',
  'DistinctConditionCountMediumTerm',
  'DistinctConditionCountShortTerm',
  'DistinctIngredientCountLongTerm',
  'DistinctIngredientCountMediumTerm',
  'DistinctIngredientCountShortTerm',
  'DistinctProcedureCountLongTerm',
  'DistinctProcedureCountMediumTerm',
  'DistinctProcedureCountShortTerm',
  'DistinctMeasurementCountLongTerm',
  'DistinctMeasurementCountMediumTerm',
  'DistinctMeasurementCountShortTerm',
  'DistinctObservationCountLongTerm',
  'DistinctObservationCountMediumTerm',
  'DistinctObservationCountShortTerm',
  'VisitCountLongTerm',
  'VisitCountMediumTerm',
  'VisitCountShortTerm',
  'VisitConceptCountLongTerm',
  'VisitConceptCountMediumTerm',
  'VisitConceptCountShortTerm',
  -- MeasurementValue.sql
  'MeasurementValueAnyTimePrior',
  'MeasurementValueLongTerm',
  'MeasurementValueMediumTerm',
  'MeasurementValueShortTerm',
  -- CharlsonIndex.sql
  'CharlsonIndex',
  -- Dcsi.sql
  'Dcsi'
);