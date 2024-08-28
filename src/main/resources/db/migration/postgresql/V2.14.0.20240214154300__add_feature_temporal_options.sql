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
        'DemographicsIndexYearMonth'
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
