INSERT INTO ${ohdsiSchema}.sec_permission(id, value, description) VALUES
    (nextval('${ohdsiSchema}.sec_permission_id_seq'), 'cohort-characterization:generation:*:temporalresult:get', 'Get cohort characterization generation temporal results');

INSERT INTO ${ohdsiSchema}.sec_role_permission(role_id, permission_id)
SELECT sr.id, sp.id
FROM ${ohdsiSchema}.sec_permission SP, ${ohdsiSchema}.sec_role sr
WHERE sp."value" IN (
                     'cohort-characterization:generation:*:temporalresult:get'
    )
  AND sr.name IN ('Atlas users');

ALTER TABLE ${ohdsiSchema}.fe_analysis
    ADD COLUMN IF NOT EXISTS supports_annual BOOLEAN default false,
    ADD COLUMN IF NOT EXISTS supports_temporal BOOLEAN default false;

UPDATE ${ohdsiSchema}.fe_analysis
    SET supports_annual = TRUE
WHERE design in (
    'ConditionOccurrenceAnyTimePrior',
    'ConditionOccurrenceLongTerm',
    'ConditionOccurrenceMediumTerm',
    'ConditionOccurrenceShortTerm',
    'ConditionOccurrencePrimaryInpatientAnyTimePrior',
    'ConditionOccurrencePrimaryInpatientLongTerm',
    'ConditionOccurrencePrimaryInpatientMediumTerm',
    'ConditionOccurrencePrimaryInpatientShortTerm',
    'ConditionEraAnyTimePrior',
    'ConditionEraLongTerm',
    'ConditionEraMediumTerm',
    'ConditionEraShortTerm',
    'ConditionEraOverlapping',
    'ConditionEraStartLongTerm',
    'ConditionEraStartMediumTerm',
    'ConditionEraStartShortTerm',
    'DrugExposureAnyTimePrior',
    'DrugExposureLongTerm',
    'DrugExposureMediumTerm',
    'DrugExposureShortTerm',
    'DrugEraAnyTimePrior',
    'DrugEraLongTerm',
    'DrugEraMediumTerm',
    'DrugEraShortTerm',
    'DrugEraOverlapping',
    'DrugEraStartLongTerm',
    'DrugEraStartMediumTerm',
    'DrugEraStartShortTerm',
    'ProcedureOccurrenceAnyTimePrior',
    'ProcedureOccurrenceLongTerm',
    'ProcedureOccurrenceMediumTerm',
    'ProcedureOccurrenceShortTerm',
    'DeviceExposureAnyTimePrior',
    'DeviceExposureLongTerm',
    'DeviceExposureMediumTerm',
    'DeviceExposureShortTerm',
    'MeasurementAnyTimePrior',
    'MeasurementLongTerm',
    'MeasurementMediumTerm',
    'MeasurementShortTerm'
    'ObservationAnyTimePrior',
    'ObservationLongTerm',
    'ObservationMediumTerm',
    'ObservationShortTerm'
);

UPDATE ${ohdsiSchema}.fe_analysis
    SET supports_temporal = TRUE
WHERE
    design in (
        'DemographicsGender',
        'DemographicsAge',
        'DemographicsAgeGroup',
        'DemographicsRace',
        'DemographicsEthnicity',
        'DemographicsIndexYear',
        'DemographicsIndexMonth',
        'DemographicsPriorObservationTime',
        'DemographicsPostObservationTime',
        'DemographicsTimeInCohort',
        'DemographicsIndexYearMonth',
        'CareSiteId',
        'ConditionOccurrence',
        'ConditionOccurrencePrimaryInpatient',
        'ConditionEraStart',
        'ConditionEraOverlap',
        'ConditionEraGroupStart',
        'ConditionEraGroupOverlap',
        'DrugExposure',
        'DrugEraStart',
        'DrugEraOverlap',
        'DrugEraGroupStart',
        'DrugEraGroupOverlap',
        'ProcedureOccurrence',
        'DeviceExposure',
        'Measurement',
        'MeasurementValue',
        'MeasurementRangeGroup',
        'MeasurementValueAsConcept',
        'Observation',
        'ObservationValueAsConcept',
        'CharlsonIndex',
        'Dcsi',
        'Chads2',
        'Chads2Vasc',
        'Hfrs',
        'DistinctConditionCount',
        'DistinctIngredientCount',
        'DistinctProcedureCount',
        'DistinctMeasurementCount',
        'DistinctObservationCount',
        'VisitCount',
        'VisitConceptCount'
);

CREATE SEQUENCE IF NOT EXISTS ${ohdsiSchema}.cc_analysis_seq;

ALTER TABLE ${ohdsiSchema}.cc_analysis
    ADD COLUMN IF NOT EXISTS id BIGINT NOT NULL DEFAULT nextval('cc_analysis_seq'),
    ADD COLUMN IF NOT EXISTS include_annual BOOLEAN DEFAULT false,
    ADD COLUMN IF NOT EXISTS include_temporal BOOLEAN DEFAULT false;

SELECT setval('${ohdsiSchema}.cc_analysis_seq', COALESCE(MAX(id) + 1, 1), false) FROM ${ohdsiSchema}.cc_analysis;

ALTER TABLE ${ohdsiSchema}.cc_analysis
    DROP CONSTRAINT cc_analysis_pkey;
ALTER TABLE ${ohdsiSchema}.cc_analysis
    ADD CONSTRAINT cc_analysis_pkey PRIMARY KEY (id);
