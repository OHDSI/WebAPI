package org.ohdsi.webapi.shiny;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.ircalc.AnalysisReport;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisDetails;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExportExpression;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.service.IRAnalysisResource;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class IncidenceRatesShinyPackagingServiceTest {

    @Mock
    private IncidenceRateAnalysisRepository repository;
    @Spy
    private ManifestUtils manifestUtils;
    @Spy
    private FileWriter fileWriter;
    @Mock
    private IRAnalysisResource irAnalysisResource;
    @Mock
    private SourceRepository sourceRepository;
    @Spy
    private ObjectMapper objectMapper;

    @InjectMocks
    private IncidenceRatesShinyPackagingService sut;

    private final Integer analysisId = 1;
    private final String sourceKey = "sourceKey";

    @Test
    public void shouldGetBrief() {
        IncidenceRateAnalysis incidenceRateAnalysis = createIncidenceRateAnalysis();

        when(repository.findOne(analysisId)).thenReturn(incidenceRateAnalysis);
        Source source = new Source();
        source.setSourceId(3);
        when(sourceRepository.findBySourceKey("sourceKey")).thenReturn(source);
        ApplicationBrief brief = sut.getBrief(analysisId, sourceKey);
        assertEquals(brief.getName(), "ir_" + analysisId + "_" + sourceKey);
        assertEquals(brief.getTitle(), "Incidence_1_gv1x3_sourceKey");
        assertEquals(brief.getDescription(), incidenceRateAnalysis.getDescription());
    }

    @Test
    public void shouldPopulateAppDataWithValidData() throws JsonProcessingException {
        Integer generationId = 1;
        String sourceKey = "source";

        Source source = new Source();
        source.setSourceId(3);
        when(sourceRepository.findBySourceKey("source")).thenReturn(source);

        IncidenceRateAnalysis analysis = Mockito.mock(IncidenceRateAnalysis.class, Answers.RETURNS_DEEP_STUBS.get());
        when(analysis.getDetails().getExpression()).thenReturn("{}");
        when(repository.findOne(generationId)).thenReturn(analysis);

        CohortDTO targetCohort = new CohortDTO();
        targetCohort.setId(101);
        targetCohort.setName("Target Cohort");

        CohortDTO outcomeCohort = new CohortDTO();
        outcomeCohort.setId(201);
        outcomeCohort.setName("Outcome Cohort");


        IncidenceRateAnalysisExportExpression expression = new IncidenceRateAnalysisExportExpression();
        expression.outcomeCohorts.add(outcomeCohort);
        expression.targetCohorts.add(targetCohort);

        when(objectMapper.readValue("{}", IncidenceRateAnalysisExportExpression.class)).thenReturn(expression);
        AnalysisReport analysisReport = new AnalysisReport();
        analysisReport.summary = new AnalysisReport.Summary();
        when(irAnalysisResource.getAnalysisReport(1, "source", 101, 201)).thenReturn(analysisReport);

        CommonShinyPackagingService.ShinyAppDataConsumers dataConsumers = mock(CommonShinyPackagingService.ShinyAppDataConsumers.class, Answers.RETURNS_DEEP_STUBS.get());

        sut.populateAppData(generationId, sourceKey, dataConsumers);

        verify(dataConsumers.getAppProperties(), times(11)).accept(anyString(), anyString());
        verify(dataConsumers.getTextFiles(), times(1)).accept(anyString(), anyString());
        verify(dataConsumers.getJsonObjects(), times(1)).accept(anyString(), any());
    }

    @Test
    public void shouldReturnIncidenceType() {
        assertEquals(sut.getType(), CommonAnalysisType.INCIDENCE);
    }

    private IncidenceRateAnalysis createIncidenceRateAnalysis() {
        IncidenceRateAnalysis incidenceRateAnalysis = new IncidenceRateAnalysis();

        IncidenceRateAnalysisDetails incidenceRateAnalysisDetails = new IncidenceRateAnalysisDetails(incidenceRateAnalysis);
        incidenceRateAnalysisDetails.setExpression("{\"ConceptSets\":[],\"targetIds\":[11,7],\"outcomeIds\":[12,6],\"timeAtRisk\":{\"start\":{\"DateField\":\"StartDate\",\"Offset\":0},\"end\":{\"DateField\":\"EndDate\",\"Offset\":0}},\"studyWindow\":null,\"strata\":[{\"name\":\"Male\",\"description\":null,\"expression\":{\"Type\":\"ALL\",\"Count\":null,\"CriteriaList\":[],\"DemographicCriteriaList\":[{\"Age\":null,\"Gender\":[{\"CONCEPT_ID\":8507,\"CONCEPT_NAME\":\"MALE\",\"STANDARD_CONCEPT\":null,\"STANDARD_CONCEPT_CAPTION\":\"Unknown\",\"INVALID_REASON\":null,\"INVALID_REASON_CAPTION\":\"Unknown\",\"CONCEPT_CODE\":\"M\",\"DOMAIN_ID\":\"Gender\",\"VOCABULARY_ID\":\"Gender\",\"CONCEPT_CLASS_ID\":null}],\"Race\":null,\"Ethnicity\":null,\"OccurrenceStartDate\":null,\"OccurrenceEndDate\":null}],\"Groups\":[]}},{\"name\":\"Female\",\"description\":null,\"expression\":{\"Type\":\"ALL\",\"Count\":null,\"CriteriaList\":[],\"DemographicCriteriaList\":[{\"Age\":null,\"Gender\":[{\"CONCEPT_ID\":8532,\"CONCEPT_NAME\":\"FEMALE\",\"STANDARD_CONCEPT\":null,\"STANDARD_CONCEPT_CAPTION\":\"Unknown\",\"INVALID_REASON\":null,\"INVALID_REASON_CAPTION\":\"Unknown\",\"CONCEPT_CODE\":\"F\",\"DOMAIN_ID\":\"Gender\",\"VOCABULARY_ID\":\"Gender\",\"CONCEPT_CLASS_ID\":null}],\"Race\":null,\"Ethnicity\":null,\"OccurrenceStartDate\":null,\"OccurrenceEndDate\":null}],\"Groups\":[]}}],\"targetCohorts\":[{\"id\":11,\"name\":\"All population-IR\",\"hasWriteAccess\":false,\"hasReadAccess\":false,\"expression\":{\"cdmVersionRange\":\">=5.0.0\",\"PrimaryCriteria\":{\"CriteriaList\":[{\"ConditionOccurrence\":{\"ConditionTypeExclude\":false}},{\"DrugExposure\":{\"DrugTypeExclude\":false}}],\"ObservationWindow\":{\"PriorDays\":0,\"PostDays\":0},\"PrimaryCriteriaLimit\":{\"Type\":\"First\"}},\"ConceptSets\":[],\"QualifiedLimit\":{\"Type\":\"First\"},\"ExpressionLimit\":{\"Type\":\"First\"},\"InclusionRules\":[],\"CensoringCriteria\":[],\"CollapseSettings\":{\"CollapseType\":\"ERA\",\"EraPad\":0},\"CensorWindow\":{}}},{\"id\":7,\"name\":\"Test Cohort 4\",\"hasWriteAccess\":false,\"hasReadAccess\":false,\"expressionType\":\"SIMPLE_EXPRESSION\",\"expression\":{\"cdmVersionRange\":\">=5.0.0\",\"PrimaryCriteria\":{\"CriteriaList\":[{\"DrugExposure\":{\"CodesetId\":0,\"DrugTypeExclude\":false}}],\"ObservationWindow\":{\"PriorDays\":30,\"PostDays\":0},\"PrimaryCriteriaLimit\":{\"Type\":\"First\"}},\"ConceptSets\":[{\"id\":0,\"name\":\"celecoxib\",\"expression\":{\"items\":[{\"concept\":{\"CONCEPT_ID\":1118084,\"CONCEPT_NAME\":\"celecoxib\",\"STANDARD_CONCEPT\":\"S\",\"STANDARD_CONCEPT_CAPTION\":\"Standard\",\"INVALID_REASON\":\"V\",\"INVALID_REASON_CAPTION\":\"Valid\",\"CONCEPT_CODE\":\"140587\",\"DOMAIN_ID\":\"Drug\",\"VOCABULARY_ID\":\"RxNorm\",\"CONCEPT_CLASS_ID\":\"Ingredient\"},\"isExcluded\":false,\"includeDescendants\":true,\"includeMapped\":true}]}},{\"id\":1,\"name\":\"Major gastrointestinal (GI) bleeding\",\"expression\":{\"items\":[{\"concept\":{\"CONCEPT_ID\":4280942,\"CONCEPT_NAME\":\"Acute gastrojejunal ulcer with perforation\",\"STANDARD_CONCEPT\":\"S\",\"STANDARD_CONCEPT_CAPTION\":\"Standard\",\"INVALID_REASON\":\"V\",\"INVALID_REASON_CAPTION\":\"Valid\",\"CONCEPT_CODE\":\"66636001\",\"DOMAIN_ID\":\"Condition\",\"VOCABULARY_ID\":\"SNOMED\",\"CONCEPT_CLASS_ID\":\"Clinical Finding\"},\"isExcluded\":false,\"includeDescendants\":true,\"includeMapped\":false},{\"concept\":{\"CONCEPT_ID\":28779,\"CONCEPT_NAME\":\"Bleeding esophageal varices\",\"STANDARD_CONCEPT\":\"S\",\"STANDARD_CONCEPT_CAPTION\":\"Standard\",\"INVALID_REASON\":\"V\",\"INVALID_REASON_CAPTION\":\"Valid\",\"CONCEPT_CODE\":\"17709002\",\"DOMAIN_ID\":\"Condition\",\"VOCABULARY_ID\":\"SNOMED\",\"CONCEPT_CLASS_ID\":\"Clinical Finding\"},\"isExcluded\":false,\"includeDescendants\":true,\"includeMapped\":false},{\"concept\":{\"CONCEPT_ID\":198798,\"CONCEPT_NAME\":\"Dieulafoy's vascular malformation\",\"STANDARD_CONCEPT\":\"S\",\"STANDARD_CONCEPT_CAPTION\":\"Standard\",\"INVALID_REASON\":\"V\",\"INVALID_REASON_CAPTION\":\"Valid\",\"CONCEPT_CODE\":\"109558001\",\"DOMAIN_ID\":\"Condition\",\"VOCABULARY_ID\":\"SNOMED\",\"CONCEPT_CLASS_ID\":\"Clinical Finding\"},\"isExcluded\":false,\"includeDescendants\":true,\"includeMapped\":false},{\"concept\":{\"CONCEPT_ID\":4112183,\"CONCEPT_NAME\":\"Esophageal varices with bleeding, associated with another disorder\",\"STANDARD_CONCEPT\":\"S\",\"STANDARD_CONCEPT_CAPTION\":\"Standard\",\"INVALID_REASON\":\"V\",\"INVALID_REASON_CAPTION\":\"Valid\",\"CONCEPT_CODE\":\"195475003\",\"DOMAIN_ID\":\"Condition\",\"VOCABULARY_ID\":\"SNOMED\",\"CONCEPT_CLASS_ID\":\"Clinical Finding\"},\"isExcluded\":false,\"includeDescendants\":true,\"includeMapped\":false},{\"concept\":{\"CONCEPT_ID\":194382,\"CONCEPT_NAME\":\"External hemorrhoids\",\"STANDARD_CONCEPT\":\"S\",\"STANDARD_CONCEPT_CAPTION\":\"Standard\",\"INVALID_REASON\":\"V\",\"INVALID_REASON_CAPTION\":\"Valid\",\"CONCEPT_CODE\":\"23913003\",\"DOMAIN_ID\":\"Condition\",\"VOCABULARY_ID\":\"SNOMED\",\"CONCEPT_CLASS_ID\":\"Clinical Finding\"},\"isExcluded\":false,\"includeDescendants\":false,\"includeMapped\":false},{\"concept\":{\"CONCEPT_ID\":192671,\"CONCEPT_NAME\":\"Gastrointestinal hemorrhage\",\"STANDARD_CONCEPT\":\"S\",\"STANDARD_CONCEPT_CAPTION\":\"Standard\",\"INVALID_REASON\":\"V\",\"INVALID_REASON_CAPTION\":\"Valid\",\"CONCEPT_CODE\":\"74474003\",\"DOMAIN_ID\":\"Condition\",\"VOCABULARY_ID\":\"SNOMED\",\"CONCEPT_CLASS_ID\":\"Clinical Finding\"},\"isExcluded\":false,\"includeDescendants\":true,\"includeMapped\":false},{\"concept\":{\"CONCEPT_ID\":196436,\"CONCEPT_NAME\":\"Internal hemorrhoids\",\"STANDARD_CONCEPT\":\"S\",\"STANDARD_CONCEPT_CAPTION\":\"Standard\",\"INVALID_REASON\":\"V\",\"INVALID_REASON_CAPTION\":\"Valid\",\"CONCEPT_CODE\":\"90458007\",\"DOMAIN_ID\":\"Condition\",\"VOCABULARY_ID\":\"SNOMED\",\"CONCEPT_CLASS_ID\":\"Clinical Finding\"},\"isExcluded\":false,\"includeDescendants\":false,\"includeMapped\":false},{\"concept\":{\"CONCEPT_ID\":4338225,\"CONCEPT_NAME\":\"Peptic ulcer with perforation\",\"STANDARD_CONCEPT\":\"S\",\"STANDARD_CONCEPT_CAPTION\":\"Standard\",\"INVALID_REASON\":\"V\",\"INVALID_REASON_CAPTION\":\"Valid\",\"CONCEPT_CODE\":\"88169003\",\"DOMAIN_ID\":\"Condition\",\"VOCABULARY_ID\":\"SNOMED\",\"CONCEPT_CLASS_ID\":\"Clinical Finding\"},\"isExcluded\":false,\"includeDescendants\":true,\"includeMapped\":false},{\"concept\":{\"CONCEPT_ID\":194158,\"CONCEPT_NAME\":\"Perinatal gastrointestinal hemorrhage\",\"STANDARD_CONCEPT\":\"S\",\"STANDARD_CONCEPT_CAPTION\":\"Standard\",\"INVALID_REASON\":\"V\",\"INVALID_REASON_CAPTION\":\"Valid\",\"CONCEPT_CODE\":\"48729005\",\"DOMAIN_ID\":\"Condition\",\"VOCABULARY_ID\":\"SNOMED\",\"CONCEPT_CLASS_ID\":\"Clinical Finding\"},\"isExcluded\":false,\"includeDescendants\":true,\"includeMapped\":false}]}}],\"QualifiedLimit\":{\"Type\":\"All\"},\"ExpressionLimit\":{\"Type\":\"First\"},\"InclusionRules\":[{\"name\":\"No prior GI\",\"expression\":{\"Type\":\"ALL\",\"CriteriaList\":[{\"Criteria\":{\"ConditionOccurrence\":{\"CodesetId\":1}},\"StartWindow\":{\"Start\":{\"Coeff\":-1},\"End\":{\"Days\":0,\"Coeff\":1},\"UseIndexEnd\":false,\"UseEventEnd\":false},\"RestrictVisit\":false,\"IgnoreObservationPeriod\":false,\"Occurrence\":{\"Type\":1,\"Count\":0,\"IsDistinct\":false}}],\"DemographicCriteriaList\":[],\"Groups\":[]}}],\"CensoringCriteria\":[],\"CollapseSettings\":{\"CollapseType\":\"ERA\",\"EraPad\":0},\"CensorWindow\":{\"StartDate\":\"2010-04-01\",\"EndDate\":\"2010-12-01\"}}}],\"outcomeCohorts\":[{\"id\":12,\"name\":\"Diabetes-IR\",\"hasWriteAccess\":false,\"hasReadAccess\":false,\"expression\":{\"cdmVersionRange\":\">=5.0.0\",\"PrimaryCriteria\":{\"CriteriaList\":[{\"ConditionOccurrence\":{\"CodesetId\":0,\"First\":true,\"ConditionTypeExclude\":false}}],\"ObservationWindow\":{\"PriorDays\":365,\"PostDays\":0},\"PrimaryCriteriaLimit\":{\"Type\":\"First\"}},\"ConceptSets\":[{\"id\":0,\"name\":\"Diabetes-IR\",\"expression\":{\"items\":[{\"concept\":{\"CONCEPT_ID\":201826,\"CONCEPT_NAME\":\"Type 2 diabetes mellitus\",\"STANDARD_CONCEPT\":\"S\",\"STANDARD_CONCEPT_CAPTION\":\"Standard\",\"INVALID_REASON\":\"V\",\"INVALID_REASON_CAPTION\":\"Valid\",\"CONCEPT_CODE\":\"44054006\",\"DOMAIN_ID\":\"Condition\",\"VOCABULARY_ID\":\"SNOMED\",\"CONCEPT_CLASS_ID\":\"Clinical Finding\"},\"isExcluded\":false,\"includeDescendants\":true,\"includeMapped\":false}]}}],\"QualifiedLimit\":{\"Type\":\"First\"},\"ExpressionLimit\":{\"Type\":\"First\"},\"InclusionRules\":[{\"name\":\"Age over 18\",\"expression\":{\"Type\":\"ALL\",\"CriteriaList\":[],\"DemographicCriteriaList\":[{\"Age\":{\"Value\":18,\"Op\":\"gte\"}}],\"Groups\":[]}}],\"CensoringCriteria\":[],\"CollapseSettings\":{\"CollapseType\":\"ERA\",\"EraPad\":0},\"CensorWindow\":{}}},{\"id\":6,\"name\":\"TEST COHORT 2\",\"hasWriteAccess\":false,\"hasReadAccess\":false,\"expressionType\":\"SIMPLE_EXPRESSION\",\"expression\":{\"cdmVersionRange\":\">=5.0.0\",\"PrimaryCriteria\":{\"CriteriaList\":[{\"DrugEra\":{\"CodesetId\":0}}],\"ObservationWindow\":{\"PriorDays\":0,\"PostDays\":0},\"PrimaryCriteriaLimit\":{\"Type\":\"All\"}},\"ConceptSets\":[{\"id\":0,\"name\":\"Simvastatin1\",\"expression\":{\"items\":[{\"concept\":{\"CONCEPT_ID\":1539403,\"CONCEPT_NAME\":\"Simvastatin\",\"STANDARD_CONCEPT\":\"S\",\"STANDARD_CONCEPT_CAPTION\":\"Standard\",\"INVALID_REASON\":\"V\",\"INVALID_REASON_CAPTION\":\"Valid\",\"CONCEPT_CODE\":\"36567\",\"DOMAIN_ID\":\"Drug\",\"VOCABULARY_ID\":\"RxNorm\",\"CONCEPT_CLASS_ID\":\"Ingredient\"},\"isExcluded\":false,\"includeDescendants\":true,\"includeMapped\":false}]}}],\"QualifiedLimit\":{\"Type\":\"First\"},\"ExpressionLimit\":{\"Type\":\"All\"},\"InclusionRules\":[],\"EndStrategy\":{\"DateOffset\":{\"DateField\":\"EndDate\",\"Offset\":0}},\"CensoringCriteria\":[],\"CollapseSettings\":{\"CollapseType\":\"ERA\",\"EraPad\":0},\"CensorWindow\":{}}}]}");

        incidenceRateAnalysis.setId(analysisId);
        incidenceRateAnalysis.setName("Analysis Name");
        incidenceRateAnalysis.setDescription("Analysis Description");
        incidenceRateAnalysis.setDetails(incidenceRateAnalysisDetails);
        return incidenceRateAnalysis;
    }

    private AnalysisReport createAnalysisReport(int targetId, int outcomeId) {
        AnalysisReport analysisReport = new AnalysisReport();
        analysisReport.summary = new AnalysisReport.Summary();
        analysisReport.summary.targetId = targetId;
        analysisReport.summary.outcomeId = outcomeId;
        return analysisReport;
    }
}
