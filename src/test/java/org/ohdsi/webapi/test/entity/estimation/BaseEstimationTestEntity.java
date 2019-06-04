package org.ohdsi.webapi.test.entity.estimation;

import org.junit.After;
import org.junit.Before;
import org.mockito.Spy;
import org.ohdsi.webapi.cohort.CohortRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.EstimationController;
import org.ohdsi.webapi.estimation.EstimationService;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.estimation.repository.EstimationRepository;
import org.ohdsi.webapi.test.entity.BaseTestEntity;
import org.springframework.beans.factory.annotation.Autowired;

import static org.ohdsi.analysis.estimation.design.EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;

public abstract class BaseEstimationTestEntity extends BaseTestEntity {
    @Autowired
    protected EstimationController esController;
    @Autowired
    protected EstimationRepository esRepository;
    @Autowired
    protected EstimationService esService;
    @Autowired
    protected CohortDefinitionDetailsRepository cdRepository;
    protected Estimation firstIncomingEntity;
    protected EstimationDTO firstSavedDTO;
    protected static final String ES_SPECIFICATION =
            "{\"id\":null,\"name\":\"\",\"description\":null,\"version\":\"v2.7.0\",\"skeletonType\":\"ComparativeEffectStudy\"," +
                    "\"skeletonVersion\":\"v0.0.1\",\"cohortDefinitions\":[],\"conceptSets\":[],\"conceptSetCrossReference\":[]," +
                    "\"negativeControls\":[],\"doPositiveControlSynthesis\":false," +
                    "\"positiveControlSynthesisArgs\":{\"modelType\":\"survival\",\"minOutcomeCountForModel\":50,\"minOutcomeCountForInjection\":25," +
                    "\"covariateSettings\":{\"attr_class\":\"covariateSettings\",\"temporal\":false,\"DemographicsGender\":true,\"DemographicsAge\":false," +
                    "\"DemographicsAgeGroup\":true,\"DemographicsRace\":true,\"DemographicsEthnicity\":true,\"DemographicsIndexYear\":true," +
                    "\"DemographicsIndexMonth\":true,\"DemographicsPriorObservationTime\":false,\"DemographicsPostObservationTime\":false," +
                    "\"DemographicsTimeInCohort\":false,\"DemographicsIndexYearMonth\":false,\"ConditionOccurrenceAnyTimePrior\":false," +
                    "\"ConditionOccurrenceLongTerm\":false,\"ConditionOccurrenceMediumTerm\":false,\"ConditionOccurrenceShortTerm\":false," +
                    "\"ConditionOccurrencePrimaryInpatientAnyTimePrior\":false,\"ConditionOccurrencePrimaryInpatientLongTerm\":false," +
                    "\"ConditionOccurrencePrimaryInpatientMediumTerm\":false,\"ConditionOccurrencePrimaryInpatientShortTerm\":false," +
                    "\"ConditionEraAnyTimePrior\":false,\"ConditionEraLongTerm\":false,\"ConditionEraMediumTerm\":false,\"ConditionEraShortTerm\":false," +
                    "\"ConditionEraOverlapping\":false,\"ConditionEraStartLongTerm\":false,\"ConditionEraStartMediumTerm\":false," +
                    "\"ConditionEraStartShortTerm\":false,\"ConditionGroupEraAnyTimePrior\":false,\"ConditionGroupEraLongTerm\":true," +
                    "\"ConditionGroupEraMediumTerm\":false,\"ConditionGroupEraShortTerm\":true,\"ConditionGroupEraOverlapping\":false," +
                    "\"ConditionGroupEraStartLongTerm\":false,\"ConditionGroupEraStartMediumTerm\":false,\"ConditionGroupEraStartShortTerm\":false," +
                    "\"DrugExposureAnyTimePrior\":false,\"DrugExposureLongTerm\":false,\"DrugExposureMediumTerm\":false,\"DrugExposureShortTerm\":false," +
                    "\"DrugEraAnyTimePrior\":false,\"DrugEraLongTerm\":false,\"DrugEraMediumTerm\":false,\"DrugEraShortTerm\":false,\"DrugEraOverlapping\":false," +
                    "\"DrugEraStartLongTerm\":false,\"DrugEraStartMediumTerm\":false,\"DrugEraStartShortTerm\":false,\"DrugGroupEraAnyTimePrior\":false," +
                    "\"DrugGroupEraLongTerm\":true,\"DrugGroupEraMediumTerm\":false,\"DrugGroupEraShortTerm\":true,\"DrugGroupEraOverlapping\":true," +
                    "\"DrugGroupEraStartLongTerm\":false,\"DrugGroupEraStartMediumTerm\":false,\"DrugGroupEraStartShortTerm\":false," +
                    "\"ProcedureOccurrenceAnyTimePrior\":false,\"ProcedureOccurrenceLongTerm\":true,\"ProcedureOccurrenceMediumTerm\":false," +
                    "\"ProcedureOccurrenceShortTerm\":true,\"DeviceExposureAnyTimePrior\":false,\"DeviceExposureLongTerm\":true,\"DeviceExposureMediumTerm\":false," +
                    "\"DeviceExposureShortTerm\":true,\"MeasurementAnyTimePrior\":false,\"MeasurementLongTerm\":true,\"MeasurementMediumTerm\":false," +
                    "\"MeasurementShortTerm\":true,\"MeasurementValueAnyTimePrior\":false,\"MeasurementValueLongTerm\":false,\"MeasurementValueMediumTerm\":false," +
                    "\"MeasurementValueShortTerm\":false,\"MeasurementRangeGroupAnyTimePrior\":false,\"MeasurementRangeGroupLongTerm\":true," +
                    "\"MeasurementRangeGroupMediumTerm\":false,\"MeasurementRangeGroupShortTerm\":false,\"ObservationAnyTimePrior\":false,\"ObservationLongTerm\":true," +
                    "\"ObservationMediumTerm\":false,\"ObservationShortTerm\":true,\"CharlsonIndex\":true,\"Dcsi\":true,\"Chads2\":true,\"Chads2Vasc\":true," +
                    "\"DistinctConditionCountLongTerm\":false,\"DistinctConditionCountMediumTerm\":false,\"DistinctConditionCountShortTerm\":false," +
                    "\"DistinctIngredientCountLongTerm\":false,\"DistinctIngredientCountMediumTerm\":false,\"DistinctIngredientCountShortTerm\":false," +
                    "\"DistinctProcedureCountLongTerm\":false,\"DistinctProcedureCountMediumTerm\":false,\"DistinctProcedureCountShortTerm\":false," +
                    "\"DistinctMeasurementCountLongTerm\":false,\"DistinctMeasurementCountMediumTerm\":false,\"DistinctMeasurementCountShortTerm\":false," +
                    "\"DistinctObservationCountLongTerm\":false,\"DistinctObservationCountMediumTerm\":false,\"DistinctObservationCountShortTerm\":false," +
                    "\"VisitCountLongTerm\":false,\"VisitCountMediumTerm\":false,\"VisitCountShortTerm\":false,\"VisitConceptCountLongTerm\":false," +
                    "\"VisitConceptCountMediumTerm\":false,\"VisitConceptCountShortTerm\":false,\"longTermStartDays\":-365,\"mediumTermStartDays\":-180," +
                    "\"shortTermStartDays\":-30,\"endDays\":0,\"includedCovariateConceptIds\":[],\"addDescendantsToInclude\":false,\"excludedCovariateConceptIds\":[]," +
                    "\"addDescendantsToExclude\":false,\"includedCovariateIds\":[],\"attr_fun\":\"getDbDefaultCovariateData\"},\"prior\":{\"attr_class\":\"cyclopsPrior\"," +
                    "\"priorType\":\"laplace\",\"variance\":1,\"exclude\":0,\"graph\":null,\"neighborhood\":null,\"useCrossValidation\":true,\"forceIntercept\":false}," +
                    "\"control\":{\"attr_class\":\"cyclopsControl\",\"maxIterations\":1000,\"tolerance\":0.000001,\"convergenceType\":\"gradient\",\"cvType\":\"auto\"," +
                    "\"fold\":10,\"lowerLimit\":0.01,\"upperLimit\":20,\"gridSteps\":10,\"cvRepetitions\":1,\"minCVData\":100,\"noiseLevel\":\"quiet\",\"seed\":null," +
                    "\"resetCoefficients\":false,\"startingVariance\":0.01,\"useKKTSwindle\":false,\"tuneSwindle\":10,\"selectorType\":\"auto\",\"initialBound\":2," +
                    "\"maxBoundCount\":5,\"autoSearch\":true,\"algorithm\":\"ccd\"},\"firstExposureOnly\":true,\"washoutPeriod\":183,\"riskWindowStart\":0,\"riskWindowEnd\":30," +
                    "\"addExposureDaysToEnd\":true,\"firstOutcomeOnly\":true,\"removePeopleWithPriorOutcomes\":true,\"maxSubjectsForModel\":250000,\"effectSizes\":[1.5,2,4]," +
                    "\"precision\":0.01,\"outputIdOffset\":10000},\"negativeControlOutcomeCohortDefinition\":{\"occurrenceType\":\"all\",\"detectOnDescendants\":true," +
                    "\"domains\":[\"condition\",\"procedure\"]},\"negativeControlExposureCohortDefinition\":{}," +
                    "\"estimationAnalysisSettings\":{\"estimationType\":\"ComparativeCohortAnalysis\",\"analysisSpecification\":{\"targetComparatorOutcomes\":[]," +
                    "\"cohortMethodAnalysisList\":[]}}}\"";

    @Before
    public void setupDB() throws Exception {
        firstIncomingEntity = new Estimation();
        firstIncomingEntity.setName(NEW_TEST_ENTITY);
        firstIncomingEntity.setType(COMPARATIVE_COHORT_ANALYSIS);
        firstIncomingEntity.setSpecification(ES_SPECIFICATION);
        firstSavedDTO = esController.createEstimation(firstIncomingEntity);
    }

    @After
    public void tearDownDB() {
        esRepository.deleteAll();
    }
}
